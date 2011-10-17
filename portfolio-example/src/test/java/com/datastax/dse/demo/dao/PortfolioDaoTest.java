package com.datastax.dse.demo.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.datastax.dse.demo.DemoBaseEmbededServerSetupTest;
import com.datastax.dse.demo.domain.Portfolio;
import com.datastax.dse.demo.domain.Stock;
import com.datastax.dse.demo.domain.StockHistory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/portfolio-demo-test-context.xml"})
public class PortfolioDaoTest extends DemoBaseEmbededServerSetupTest {

  @Resource
  private PortfolioDao portfolioDao;
  
  @Resource
  private JdbcTemplate jdbcTemplate;
  
  @Test
  public void get_portfolios_by_range() {
    assertNotNull(portfolioDao.getPortfoliosLazy("", 10));
    assertEquals(1,portfolioDao.getPortfoliosLazy("", 1).size());
  }
  
  @Test
  public void load_portfolio() {
    Portfolio p1 = portfolioDao.loadPortfolio("168");
    assertEquals("168",p1.getName());
    assertEquals(4458.59, p1.getPrice(), 0);
    assertEquals(3, p1.getConstituents().size());
    Portfolio p2 = portfolioDao.loadPortfolio("236");
    assertEquals("236",p2.getName());
    assertEquals(2603.61, p2.getPrice(), 0);
    assertEquals(5, p2.getConstituents().size());
  }

  @Test
  public void load_stocks_from_tickers() {
    List<Stock> stocks = portfolioDao.loadStocks("BLU","CJS","DAL","BSX","CHK","DNB","MCI","SR");
    assertNotNull(stocks);
    assertEquals(8,stocks.size());
  }
  
  @Test
  public void load_historicals_from_tickers() {
    List<StockHistory> results = portfolioDao.loadHistoricals(5, "BLU","CJS","DAL","BSX","CHK","DNB","MCI","SR");
    assertNotNull(results);
    assertEquals(8,results.size());
    results = portfolioDao.loadHistoricals(1, "BLU","CJS","DAL","BSX","CHK","DNB","MCI","SR");
    assertEquals(1, results.get(0).getPriceHistory().size());
  }
  
  @Before
  public void setup() {
    insertTestData(PORTFOLIOS_INSERT);    
    insertTestData(STOCKS_INSERT);
    insertTestData(STOCKS_HIST);
  }
  
  
  private void insertTestData(String insert) {      
    jdbcTemplate.execute(insert);    
  }
  
  private static final String PORTFOLIOS_INSERT = "BEGIN BATCH " 
    + "INSERT INTO Portfolios (KEY, BLU, CJS, DAL) VALUES (168,'19', '7', '38') "
    + "INSERT INTO Portfolios (KEY, BSX, CHK, DNB, MCI, SR) VALUES (236,'32', '27', '7','8','3') "
    + "APPLY BATCH"; 
  
  private static final String STOCKS_INSERT = "BEGIN BATCH " 
    + "INSERT INTO Stocks (KEY, price) VALUES (BLU,'91.50') "
    + "INSERT INTO Stocks (KEY, price) VALUES (CJS,'70.09') "
    + "INSERT INTO Stocks (KEY, price) VALUES (DAL,'58.67') "
    + "INSERT INTO Stocks (KEY, price) VALUES (BSX,'24.11') "
    + "INSERT INTO Stocks (KEY, price) VALUES (CHK,'20.07') "
    + "INSERT INTO Stocks (KEY, price) VALUES (DNB,'31.12') "
    + "INSERT INTO Stocks (KEY, price) VALUES (MCI,'97.88') "
    + "INSERT INTO Stocks (KEY, price) VALUES (SR,'96.44') "
    + "APPLY BATCH";
  
  private static final String STOCKS_HIST = "BEGIN BATCH " 
    + "INSERT INTO StockHist (KEY,'2011-10-06','2011-10-05','2011-10-04') VALUES (BLU,'98.50','98.10','97.49') "
    + "INSERT INTO StockHist (KEY,'2011-10-06','2011-10-05','2011-10-04') VALUES (CJS,'70.09','68.54','66.12') "
    + "INSERT INTO StockHist (KEY,'2011-10-06','2011-10-05','2011-10-04') VALUES (DAL,'58.67','65.10','66.19') "
    + "INSERT INTO StockHist (KEY,'2011-10-06','2011-10-05','2011-10-04') VALUES (BSX,'24.11','22.88','20.67') "
    + "INSERT INTO StockHist (KEY,'2011-10-06','2011-10-05','2011-10-04') VALUES (CHK,'20.07','23.83','25.10') "
    + "INSERT INTO StockHist (KEY,'2011-10-06','2011-10-05','2011-10-04') VALUES (DNB,'31.12','24.18','23.99') "
    + "INSERT INTO StockHist (KEY,'2011-10-06','2011-10-05','2011-10-04') VALUES (MCI,'97.88','101.11','101.98') "
    + "INSERT INTO StockHist (KEY,'2011-10-06','2011-10-05','2011-10-04') VALUES (SR,'96.44','95.30','94.60') "
    + "APPLY BATCH";
}
