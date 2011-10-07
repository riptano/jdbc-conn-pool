package com.datastax.drivers.jdbc.pool.cassandra.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.datastax.drivers.jdbc.pool.cassandra.BaseEmbededServerSetupTest;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HPoolExhaustedException;
import com.datastax.drivers.jdbc.pool.cassandra.jdbc.CassandraConnectionHandle;

public class ConcurrentHClientPoolTest extends BaseEmbededServerSetupTest {

  private CassandraHost cassandraHost;
  private ConcurrentHClientPool clientPool;

  @Before
  public void setupTest() throws Exception {
    setupClient();
    // Wait only one second.
    cassandraHostConfigurator.setMaxWaitTimeWhenExhausted(500);
    cassandraHost = cassandraHostConfigurator.buildCassandraHosts()[0];
    clientPool = new ConcurrentHClientPool(cassandraHost);
  }
  
  @Test
  public void testSpinUp() {
    assertEquals(16, clientPool.getNumIdle());
    assertEquals(50, clientPool.getNumBeforeExhausted());
    assertEquals(0, clientPool.getNumBlockedThreads());
    assertEquals(0, clientPool.getNumActive());
  }
  
  @Test
  public void testShutdown() {
    clientPool.shutdown();
    assertEquals(0, clientPool.getNumIdle());
    assertEquals(0, clientPool.getNumBlockedThreads());
    assertEquals(0, clientPool.getNumActive());
  }
  
  @Test
  public void testBorrowRelease() throws Exception {
    CassandraConnectionHandle conn = clientPool.borrowClient();
    assertEquals(1, clientPool.getNumActive());
    clientPool.releaseClient(conn);
    assertEquals(0, clientPool.getNumActive());
  }
  
  @Test
  public void testBorrowMoreThanActiveConnections() throws Exception {
    CassandraConnectionHandle lastConn = null;
    for (int i = 0; i < 50 ; i++)
      lastConn = clientPool.borrowClient();

    assertFalse(lastConn.isClosed());
    assertEquals(50, clientPool.getNumActive());
    
    try {
      clientPool.borrowClient();
      fail("BorrowClient should throw an ExhaustedException here. Something went wrong.");
    } catch (HPoolExhaustedException e) {
      // Expected
    }

    clientPool.releaseClient(lastConn);

    assertEquals(49, clientPool.getNumActive());

    clientPool.borrowClient();
  }
}
