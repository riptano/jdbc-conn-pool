/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.drivers.jdbc.pool.cassandra.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.drivers.jdbc.pool.cassandra.connection.CassandraHost;
import com.datastax.drivers.jdbc.pool.cassandra.connection.HConnectionManager;


/**
 * Connection handle wrapper around a JDBC connection.
 *
 */
public class CassandraConnectionHandle implements Connection {
  
  /** Class logger. */
  protected static Logger logger = LoggerFactory.getLogger(CassandraConnectionHandle.class);
  
  private Connection conn;
  private HConnectionManager manager;
  private CassandraHost cassandraHost;
  private long useageStartTime;

  public boolean isClosed = false;
  
  public CassandraConnectionHandle(HConnectionManager manager, Connection conn, CassandraHost cassandraHost) {
    this.manager = manager;
    this.conn = conn;
    this.cassandraHost = cassandraHost;
  }
  
  public HConnectionManager getManager() {
    return manager;
  }

  public void setManager(HConnectionManager manager) {
    this.manager = manager;
  }

  public CassandraConnectionHandle(Connection conn, CassandraHost cassandraHost) {
    this.conn = conn;
    this.cassandraHost = cassandraHost;
  }
  
  protected SQLException markPossiblyBroken(SQLException e) {
    releaseConnection();
    return e;
  }

  private void releaseConnection() {
    try {
      if (!conn.isClosed())
        manager.releaseClient(this);
    } catch (SQLException e1) {
      logger.warn("connection.isClosed() threw Ex. Ignoring it.");
    }
  }
  
  public void startToUse() {
    useageStartTime = System.currentTimeMillis();
  }

  /**
   * @return Time in MS since it was used.
   */
  public long getSinceLastUsed() {
    return System.currentTimeMillis() - useageStartTime;
  }
  
  /**
   * Checks if the connection is (logically) closed and throws an exception if it is.
   * 
   * @throws SQLException
   *             on error
   * 
   * 
   */
  private void checkClosed() throws SQLException {
    if (this.conn.isClosed()) {
      throw new SQLException("Connection is closed!");
    }
  }

  public Connection getInternalConnection() {
    return conn;
  }

  public void setInternalConnection(Connection conn) {
    this.conn = conn;
  }

  public CassandraHost getCassandraHost() {
    return cassandraHost;
  }

  public void setCassandraHost(CassandraHost cassandraHost) {
    this.cassandraHost = cassandraHost;
  }

  @Override
  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
    checkClosed();
    try {
      return this.conn.isWrapperFor(arg0);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }



  @Override
  public <T> T unwrap(Class<T> arg0) throws SQLException {
    checkClosed();
    try {
      return this.conn.unwrap(arg0);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void clearWarnings() throws SQLException {
    checkClosed();
    try {
      this.conn.clearWarnings();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void close() throws SQLException {
    releaseConnection();
    //isClosed  = true;
  }

  @Override
  public void commit() throws SQLException {
    checkClosed();
    try {
      this.conn.commit();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    checkClosed();
    try {
      return this.conn.createArrayOf(typeName, elements);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Blob createBlob() throws SQLException {
    checkClosed();
    try {
      return this.conn.createBlob();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Clob createClob() throws SQLException {
    checkClosed();
    try {
      return this.conn.createClob();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public NClob createNClob() throws SQLException {
    checkClosed();
    try {
      return this.conn.createNClob();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    checkClosed();
    try {
      return this.conn.createSQLXML();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Statement createStatement() throws SQLException {
    checkClosed();
    try {
      return new CassandraStatementHandle(this.conn.createStatement(), manager, this);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    checkClosed();
    try {
      return new CassandraStatementHandle(this.conn.createStatement(resultSetType, resultSetConcurrency), manager, this);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    checkClosed();
    try {
      return new CassandraStatementHandle(
          this.conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), manager, this);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    checkClosed();
    try {
      return this.conn.createStruct(typeName, attributes);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    checkClosed();
    try {
      return this.conn.getAutoCommit();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public String getCatalog() throws SQLException {
    checkClosed();
    try {
      return this.conn.getCatalog();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    checkClosed();
    try {
      return this.conn.getClientInfo();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    checkClosed();
    try {
      return this.conn.getClientInfo(name);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public int getHoldability() throws SQLException {
    checkClosed();
    try {
      return this.conn.getHoldability();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    checkClosed();
    try {
      return this.conn.getMetaData();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    checkClosed();
    try {
      return this.conn.getTransactionIsolation();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    checkClosed();
    try {
      return this.conn.getTypeMap();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    checkClosed();
    try {
      return this.conn.getWarnings();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public boolean isClosed() throws SQLException {
    return this.conn.isClosed();
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    checkClosed();
    try {
      return this.conn.isReadOnly();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    checkClosed();
    try {
      return this.conn.isValid(timeout);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    checkClosed();
    try {
      return this.conn.nativeSQL(sql);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    checkClosed();
    try {
      // Currently not supported by Cassandra JDBC Driver
      return this.conn.prepareCall(sql);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    checkClosed();
    try {
      // Currently not supported by Cassandra JDBC Driver
      return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType,
      int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    checkClosed();
    try {
      // Currently not supported by Cassandra JDBC Driver
      return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    checkClosed();
    try {
      return new CassandraPreparedStatementHandle(this.conn.prepareStatement(sql), manager, this);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    checkClosed();
    try {
      return new CassandraPreparedStatementHandle(this.conn.prepareStatement(sql, autoGeneratedKeys), manager, this);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    checkClosed();
    try {
      return new CassandraPreparedStatementHandle(this.conn.prepareStatement(sql, columnIndexes), manager, this);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames)
      throws SQLException {
    checkClosed();
    try {
      return new CassandraPreparedStatementHandle(this.conn.prepareStatement(sql, columnNames), manager, this);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    checkClosed();
    try {
      return new CassandraPreparedStatementHandle(
          this.conn.prepareStatement(sql, resultSetType, resultSetConcurrency), manager, this);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType,
      int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    checkClosed();
    try {
      return new CassandraPreparedStatementHandle(
          this.conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), manager, this);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    checkClosed();
    try {
      // Currently not supported by Cassandra JDBC Driver
      this.conn.releaseSavepoint(savepoint);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void rollback() throws SQLException {
    checkClosed();
    try {
      // Currently not supported by Cassandra JDBC Driver
      this.conn.rollback();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    checkClosed();
    try {
      // Currently not supported by Cassandra JDBC Driver
      this.conn.rollback(savepoint);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    checkClosed();
    try {
      // Currently not supported by Cassandra JDBC Driver. Currently it's always true.
      this.conn.setAutoCommit(autoCommit);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    checkClosed();
    try {
      // Currently ignored by Cassandra JDBC Driver.
      this.conn.setCatalog(catalog);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    this.conn.setClientInfo(properties);
  }

  @Override
  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    this.conn.setClientInfo(name, value);
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    checkClosed();
    try {
      // Currently ignored by Cassandra JDBC Driver.
      this.conn.setHoldability(holdability);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    checkClosed();
    try {
      // Currently ignored by Cassandra JDBC Driver.
      this.conn.setReadOnly(readOnly);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    checkClosed();
    try {
      // Currently not supported by Cassandra JDBC Driver.
      return this.conn.setSavepoint();
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    checkClosed();
    try {
      // Currently not supported by Cassandra JDBC Driver.
      return this.conn.setSavepoint(name);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    checkClosed();
    try {
      // Currently only Connection.TRANSACTION_NONE is supported by Cassandra JDBC Driver.
      this.conn.setTransactionIsolation(level);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    checkClosed();
    try {
      this.conn.setTypeMap(map);
    } catch (SQLException e) {
      throw markPossiblyBroken(e);
    }
  }

}
