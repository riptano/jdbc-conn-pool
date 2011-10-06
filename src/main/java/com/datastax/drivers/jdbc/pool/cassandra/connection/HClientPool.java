package com.datastax.drivers.jdbc.pool.cassandra.connection;

import java.sql.SQLException;

import com.datastax.drivers.jdbc.pool.cassandra.jdbc.CassandraConnectionHandle;


public interface HClientPool extends PoolMetric {
  public CassandraConnectionHandle borrowClient() throws SQLException;
  public CassandraHost getCassandraHost();
  public int getNumBeforeExhausted();
  public boolean isExhausted();
  public int getMaxActive();
  public String getStatusAsString();
  public void releaseClient(CassandraConnectionHandle conn) throws SQLException;
  void shutdown();
}