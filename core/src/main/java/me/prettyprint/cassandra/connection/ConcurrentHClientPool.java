package me.prettyprint.cassandra.connection;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import me.prettyprint.cassandra.jdbc.CassandraConnectionHandle;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.exceptions.PoolExhaustedException;

import org.apache.cassandra.cql.jdbc.CassandraDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentHClientPool implements HClientPool {

  private static final Logger log = LoggerFactory.getLogger(ConcurrentHClientPool.class);

  private final ArrayBlockingQueue<CassandraConnectionHandle> availableConnectionQueue;
  private final AtomicInteger activeConnectionCount;
  private final AtomicInteger realActiveConnectionCount;

  private final CassandraHost cassandraHost;
  private final CassandraDataSource ds;

  /** Total threads waiting for connections */
  private final AtomicInteger numBlocked;
  private final AtomicBoolean active;

  private final long maxWaitTimeWhenExhausted;

  public ConcurrentHClientPool(CassandraHost host) throws SQLException {
    this.cassandraHost = host;
    ds = new CassandraDataSource(cassandraHost.getHost(), cassandraHost.getPort(), cassandraHost.getKeyspaceName(),
            cassandraHost.getUser(), cassandraHost.getPassword());

    availableConnectionQueue = new ArrayBlockingQueue<CassandraConnectionHandle>(cassandraHost.getMaxActive(), true);
    // This counter can be offset by as much as the number of threads.
    activeConnectionCount = new AtomicInteger(0);
    realActiveConnectionCount = new AtomicInteger(0);
    numBlocked = new AtomicInteger();
    active = new AtomicBoolean(true);

    maxWaitTimeWhenExhausted = cassandraHost.getMaxWaitTimeWhenExhausted() < 0 ? 0 : cassandraHost.getMaxWaitTimeWhenExhausted();

    for (int i = 0; i < cassandraHost.getMaxActive() / 3; i++) {
        availableConnectionQueue.add(createConnection());
    }

    if ( log.isDebugEnabled() ) {
      log.debug("Concurrent Host pool started with {} active clients; max: {} exhausted wait: {}",
          new Object[]{getNumIdle(),
          cassandraHost.getMaxActive(),
          maxWaitTimeWhenExhausted});
    }
  }


  @Override
  public CassandraConnectionHandle borrowClient() throws SQLException {
    if ( !active.get() ) {
      throw new HectorException("Attempt to borrow on in-active pool: " + getName());
    }

    CassandraConnectionHandle conn = availableConnectionQueue.poll();
    int currentActiveClients = activeConnectionCount.incrementAndGet();

    try {

      if ( conn == null ) {

        if (currentActiveClients <= cassandraHost.getMaxActive()) {
            conn = createConnection();
        } else {
          // We can't grow so let's wait for a connection to become available.
            conn = waitForConnection();
        }

      }

      if ( conn == null ) {
        throw new HectorException("HConnectionManager returned a null client after aquisition - are we shutting down?");
      }
    } catch (RuntimeException e) {
      activeConnectionCount.decrementAndGet();
      throw e;
    }

    realActiveConnectionCount.incrementAndGet();
    return conn;
  }


  private CassandraConnectionHandle waitForConnection() {
    CassandraConnectionHandle conn = null;
    numBlocked.incrementAndGet();

    // blocked take on the queue if we are configured to wait forever
    if ( log.isDebugEnabled() ) {
      log.debug("blocking on queue - current block count {}", numBlocked.get());
    }

    try {
      // wait and catch, creating a new one if the counts have changed. Infinite wait should just recurse.
      if (maxWaitTimeWhenExhausted == 0) {

        while (conn == null && active.get()) {
          try {
            conn = availableConnectionQueue.poll(100, TimeUnit.MILLISECONDS);
          } catch (InterruptedException ie) {
            log.error("InterruptedException poll operation on retry forever", ie);
            break;
          }
        }

      } else {

        try {
          conn = availableConnectionQueue.poll(maxWaitTimeWhenExhausted, TimeUnit.MILLISECONDS);
          if (conn == null) {
            throw new PoolExhaustedException(String.format(
                "maxWaitTimeWhenExhausted exceeded for thread %s on host %s",
                new Object[] { Thread.currentThread().getName(), cassandraHost.getName() }));
          }
        } catch (InterruptedException ie) {
          // monitor.incCounter(Counter.POOL_EXHAUSTED);
          log.error("Cassandra client acquisition interrupted", ie);
        }

      }
    } finally {
      numBlocked.decrementAndGet();
    }

    return conn;
  }


/**
   * Used when we still have room to grow. Return an Connection without
   * having to wait on polling logic. (But still increment all the counters)
   * @return
 * @throws SQLException 
   */
  private CassandraConnectionHandle createConnection() throws SQLException {
    if ( log.isDebugEnabled() ) {
      log.debug("Creation of new connection");
    }
    try {
      return new CassandraConnectionHandle(ds.getConnection(cassandraHost.getUser(), cassandraHost.getPassword()), cassandraHost);
    } catch (SQLException e) {
      log.debug("Unable to open transport to " + cassandraHost.getName());
      throw e;
    }
  }

  /**
   * Controlled shutdown of pool. Go through the list of available clients
   * in the queue and call {@link HThriftClient#close()} on each. Toggles
   * a flag to indicate we are going into shutdown mode. Any subsequent calls
   * will throw an IllegalArgumentException.
   *
   *
   */
  @Override
  public void shutdown() {
    if (!active.compareAndSet(true, false) ) {
      throw new IllegalArgumentException("shutdown() called for inactive pool: " + getName());
    }
    log.info("Shutdown triggered on {}", getName());
    Set<CassandraConnectionHandle> connections = new HashSet<CassandraConnectionHandle>();
    availableConnectionQueue.drainTo(connections);
    if ( connections.size() > 0 ) {
      for (CassandraConnectionHandle conn : connections) {
        closeConnection(conn);
      }
    }
    log.info("Shutdown complete on {}", getName());
  }

  private void closeConnection(CassandraConnectionHandle conn) {
    try {
      conn.getInternalConnection().close();
    } catch (SQLException e) {
      log.error("Error closgin connection for: " + cassandraHost.getHost());
    }
  }


  @Override
  public CassandraHost getCassandraHost() {
    return cassandraHost;
  }

  @Override
  public String getName() {
    return String.format("<ConcurrentCassandraClientPoolByHost>:{%s}", cassandraHost.getName());
  }

  @Override
  public int getNumActive() {
    return realActiveConnectionCount.get();
  }


@Override
public int getNumBeforeExhausted() {
    return cassandraHost.getMaxActive() - realActiveConnectionCount.get();
  }


@Override
  public int getNumBlockedThreads() {
    return numBlocked.intValue();
  }

  @Override
  public int getNumIdle() {
    return availableConnectionQueue.size();
  }

  @Override
  public boolean isExhausted() {
    return getNumBeforeExhausted() == 0;
  }

  @Override
  public int getMaxActive() {
    return cassandraHost.getMaxActive();
  }

  @Override
  public boolean getIsActive() {
    return active.get();
  }

  @Override
  public String getStatusAsString() {
    return String.format(
            "%s; IsActive?: %s; Active: %d; Blocked: %d; Idle: %d; NumBeforeExhausted: %d",
            getName(), getIsActive(), getNumActive(), getNumBlockedThreads(),
            getNumIdle(), getNumBeforeExhausted());
  }

  @Override
  public void releaseClient(CassandraConnectionHandle conn) throws SQLException {
    boolean open;
    try {
      open = !conn.isClosed();
    } catch (SQLException e) {
      // Tight to Cassandra Driver implementation. It should not happen.
      open = false;
    }

    if ( open ) {
      if ( active.get() ) {
        addClientToPoolGently(conn);
      } else {
        log.info("Open client released to in-active pool for host {}. Closing.", cassandraHost);
        closeConnection(conn);
      }
    } else {
      try {
        addClientToPoolGently(createConnection());
      } catch (SQLException e) {
        log.info("Unable to reopen a connection. Bad server. Message: " + e.getMessage());
      }
    }

    realActiveConnectionCount.decrementAndGet();
    activeConnectionCount.decrementAndGet();

    if ( log.isDebugEnabled() ) {
      log.debug("Status of releaseClient {} to queue: {}", cassandraHost.getHost(), open);
    }
  }

  /**
   * Avoids a race condition on adding clients back to the pool if pool is almost full.
   * Almost always a result of batch operation startup and shutdown (when multiple threads
   * are releasing at the same time).
   * @param conn Connection
   */
  private void addClientToPoolGently(CassandraConnectionHandle conn) {
    try {
      availableConnectionQueue.add(conn);
    } catch (IllegalStateException ise) {
      log.error("Capacity hit adding client back to queue. Closing extra");
      closeConnection(conn);
    }
  }
}
