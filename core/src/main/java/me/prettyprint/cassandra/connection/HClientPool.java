package me.prettyprint.cassandra.connection;

import java.sql.Connection;

/**
 * Main interface for Cassandra Pool.
 */
import me.prettyprint.hector.api.exceptions.HectorException;

public interface HClientPool extends PoolMetric {
  public Connection borrowClient() throws HectorException;
  public CassandraHost getCassandraHost();
  public int getNumBeforeExhausted();
  public boolean isExhausted();
  public int getMaxActive();
  public String getStatusAsString();
  public void releaseClient(Connection conn) throws HectorException;
  void shutdown();
}