package com.datastax.dse.demo.dao;

import java.nio.charset.CharacterCodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cassandra.cql.jdbc.CassandraResultSet;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Component;

import com.datastax.dse.demo.domain.Portfolio;
import com.datastax.dse.demo.domain.Position;
import com.datastax.dse.demo.domain.Stock;
import com.datastax.dse.demo.domain.StockHistory;

/**
 * Some examples of how to wire up a DAO with the jdbc-pool data source
 * and JdbcTemplate. See individual methods for details.
 * 
 * @author zznate
 */
@Component
public class PortfolioDao {
  
  private Logger log = LoggerFactory.getLogger(PortfolioDao.class);
  
  @Autowired
  private JdbcTemplate jdbcTemplate;
  
  /**
   * Return a List of Portfolio objects with just the name set
   * 
   * @param startPortfolioName
   * @param count
   * @return
   */
  public List<Portfolio> getPortfoliosLazy(String startPortfolioName, int count) {
    // TODO hack setObject proxy in jdbc-pool
    String portfoliosCql = "select FIRST 1000 ''..'' FROM Portfolios WHERE KEY >= ? LIMIT "+count;
    
    List<Portfolio> portfolios = jdbcTemplate.query(portfoliosCql, 
        new SqlParameterValue[]{new SqlParameterValue(Types.VARCHAR, startPortfolioName)},
        new RowMapper<Portfolio>() {

      public Portfolio mapRow(ResultSet rs, int arg1) throws SQLException {
        CassandraResultSet crs = (CassandraResultSet)rs;
        Portfolio portfolio = new Portfolio();
        portfolio.setName(new String(crs.getKey()));

        return portfolio;
      }
      
    });
    
    return portfolios;
  }
  
  /**
   * Load a single {@link Portfolio} object from the provided portfolioName.
   * This load method populates the related positions and sets their prices, 
   * effectively fully building out he {@link Portfolio} for display on the
   * front end.
   * 
   * @param portfolioName
   * @return
   */
  public Portfolio loadPortfolio(String portfolioName) {
    Portfolio portfolio = jdbcTemplate.queryForObject("select FIRST 1000 ''..'' FROM Portfolios WHERE KEY = ?", 
        new SqlParameterValue[]{new SqlParameterValue(Types.VARCHAR, portfolioName)},
        new RowMapper<Portfolio>() {

      public Portfolio mapRow(ResultSet rs, int arg1) throws SQLException {
        CassandraResultSet crs = (CassandraResultSet)rs;
        Portfolio portfolio = new Portfolio();
        portfolio.setName(new String(crs.getKey()));
        List<String> tickers = new ArrayList<String>();
        ResultSetMetaData rsmd = rs.getMetaData();

        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
          // gnarly hack around off-by-one in crs.getColumn(i)
          Column column = crs.getColumn(rsmd.getColumnName(i)).getRawColumn();
          Position position = null;          
          try {
            position = new Position(ByteBufferUtil.string(column.name), 0.0, ByteBufferUtil.toLong(column.value));            
            portfolio.addToConstituents(position);
            tickers.add(position.getTicker());
            // TODO add stockHist data as well
          } catch (CharacterCodingException cce) {
            cce.printStackTrace();
          }          
        }
        portfolio.applyStockPrices(loadStocks(tickers.toArray(new String[]{})));
        return portfolio;
      }
      
    });           
    return portfolio;
  }
  
  /**
   * Return a List of {@link StockHistory} objects - one for each ticker.
   * The days argument limits the number of column
   * @param tickers
   * @return
   */
  public List<StockHistory> loadHistoricals(long days, String ...tickers) {
    List<StockHistory> results = jdbcTemplate.query(String.format("select FIRST %d ''..'' FROM StockHist where key in (%s)", 
        days,
        appendKeys(tickers).toString()), 
        new RowMapper<StockHistory>() {

      public StockHistory mapRow(ResultSet rs, int row)
      throws SQLException {
        CassandraResultSet crs = (CassandraResultSet)rs;
        ResultSetMetaData rsmd = rs.getMetaData();
        StockHistory stockHistory = new StockHistory(new String(crs.getKey()));
        
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
          // gnarly hack around off-by-one in crs.getColumn(i)
          Column column = crs.getColumn(rsmd.getColumnName(i)).getRawColumn();
          try {
            stockHistory.addDate(ByteBufferUtil.string(column.bufferForName()), ByteBufferUtil.toDouble(column.bufferForValue()));
          } catch (CharacterCodingException e) {
            e.printStackTrace();
          }
        }        
        return stockHistory;
      }

    });
    
    return results;
  }
  
  
  /**
   * Load a list of {@link Stock} objects - one per provided ticker. This is assembled as a
   * 'WHERE KEY IN (?,?,?...) clause which uses a similar approach to multiget_slice thrift method.
   * Ie. It parallelizes the selects on the coordinator. 
   *  
   * @param tickers
   * @return
   */
  public List<Stock> loadStocks(String ...tickers) {
    // TODO Gnarly hack until we can get array params working
    List<Stock> stocks = jdbcTemplate.query(String.format("select price FROM Stocks WHERE KEY IN (%s)", 
        appendKeys(tickers).toString()),
        new RowMapper<Stock>() {

      public Stock mapRow(ResultSet rs, int row) throws SQLException {
        CassandraResultSet crs = (CassandraResultSet)rs;
        Stock stock = new Stock();
        stock.setTicker(new String(crs.getKey()));
        stock.setPrice(crs.getDouble("price"));
        return stock;
      }
      
    });
    return stocks;
  }
  
  public void saveOrUpdate(final Portfolio portfolio) {
    StringBuilder colNames = new StringBuilder();
    StringBuilder colVals = new StringBuilder();  
    for (Iterator<Position> iterator = portfolio.getConstituents().iterator(); iterator.hasNext();) {
      Position pos = iterator.next();
      colNames.append(pos.getTicker());
      colVals.append(Long.toString(pos.getShares()));
      if ( iterator.hasNext() ) {
        colNames.append(",");
        colVals.append(",");
      }
    }    
    String statement = String.format("INSERT INTO Portfolios (KEY, %s) VALUES (%s,'%s')", colNames, portfolio.getName(), colVals);
    log.debug("statement: {}", statement);
    jdbcTemplate.execute(statement);
  }

  private StringBuilder appendKeys(String... tickers) {
    StringBuilder keys = new StringBuilder();
    for (int i = 0; i < tickers.length; i++) {
      if ( i > 0 )
        keys.append(",");
      keys.append(tickers[i]);
    }
    return keys;
  }
}
