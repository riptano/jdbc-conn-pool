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
package com.datastax.drivers.jdbc.pool.cassandra.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.cql.jdbc.CassandraDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraHostRetryService extends BackgroundCassandraHostService {

  private static Logger log = LoggerFactory.getLogger(CassandraHostRetryService.class);

  public static final int DEF_QUEUE_SIZE = -1;
  public static final int DEF_RETRY_DELAY = 10;
  private final LinkedBlockingQueue<CassandraHost> downedHostQueue;

  public CassandraHostRetryService(HConnectionManager connectionManager,
      CassandraHostConfigurator cassandraHostConfigurator) {
    super(connectionManager, cassandraHostConfigurator);
    this.retryDelayInSeconds = cassandraHostConfigurator.getRetryDownedHostsDelayInSeconds();
    downedHostQueue = new LinkedBlockingQueue<CassandraHost>(cassandraHostConfigurator.getRetryDownedHostsQueueSize() < 1 
        ? Integer.MAX_VALUE : cassandraHostConfigurator.getRetryDownedHostsQueueSize());
          
    sf = executor.scheduleWithFixedDelay(new RetryRunner(), this.retryDelayInSeconds,this.retryDelayInSeconds, TimeUnit.SECONDS);

    log.info("Downed Host Retry service started with queue size {} and retry delay {}s",
        cassandraHostConfigurator.getRetryDownedHostsQueueSize(),
        retryDelayInSeconds);
  }

  @Override
  void shutdown() {
    log.info("Downed Host retry shutdown hook called");
    if ( sf != null ) {
      sf.cancel(true);
    }
    if ( executor != null ) {
      executor.shutdownNow();
    }
    log.info("Downed Host retry shutdown complete");
  }

  public void add(final CassandraHost cassandraHost) {
    downedHostQueue.add(cassandraHost);
    if ( log.isInfoEnabled() ) {
      log.info("Host detected as down was added to retry queue: {}", cassandraHost.getName());
    }
    
    //schedule a check of this host immediately,
    executor.submit(new Runnable() {
      @Override
      public void run() {
        if(downedHostQueue.contains(cassandraHost) && verifyConnection(cassandraHost)) {
          connectionManager.addCassandraHost(cassandraHost);
          downedHostQueue.remove(cassandraHost);
          return;
        }
      }
    });
  }

  public boolean remove(CassandraHost cassandraHost) {
      return downedHostQueue.remove(cassandraHost);
  }
  
  public boolean contains(CassandraHost cassandraHost) {
    return downedHostQueue.contains(cassandraHost);
  }

  public Set<CassandraHost> getDownedHosts() {
    return Collections.unmodifiableSet(new HashSet<CassandraHost>(downedHostQueue));
  }

  @Override
  public void applyRetryDelay() {
    sf.cancel(false);
    executor.schedule(new RetryRunner(), retryDelayInSeconds, TimeUnit.SECONDS);
  }

  public void flushQueue() {
    downedHostQueue.clear();
    log.info("Downed Host retry queue flushed.");
  }



  class RetryRunner implements Runnable {

    @Override
    public void run() {
      if( downedHostQueue.isEmpty()) {
          log.debug("Retry service fired... nothing to do.");
          return;
      }  
      Iterator<CassandraHost> iter = downedHostQueue.iterator();
      while( iter.hasNext() ) {
        CassandraHost cassandraHost = iter.next();
        if( cassandraHost == null ) {
          continue;
        }
        boolean reconnected = verifyConnection(cassandraHost);
        log.info("Downed Host retry status {} with host: {}", reconnected, cassandraHost.getName());
        if ( reconnected ) {
          connectionManager.addCassandraHost(cassandraHost);
          //we can't call iter.remove() based on return value of connectionManager.addCassandraHost, since
          //that returns false if an error occurs, or if the host already exists
          if(connectionManager.getHosts().contains(cassandraHost)) {
            iter.remove();
          }
        }
      }
    }
  }


  private boolean verifyConnection(CassandraHost cassandraHost) {
    if ( cassandraHost == null ) {
      return false;
    }

    boolean found = false;
    CassandraDataSource ds = new CassandraDataSource(cassandraHost.getHost(),
                                                     cassandraHost.getPort(),
                                                     cassandraHost.getKeyspaceName(),
                                                     cassandraHost.getUser(),
                                                     cassandraHost.getPassword());
    Connection conn = null;
    try {
      conn = ds.getConnection();
      // May be add some queries
    } catch (SQLException e) {
      if (log.isDebugEnabled()) {
        log.debug("Downed {} host still appears to be down: {}", cassandraHost, e);
      } else {
        log.info("Downed {} host still appears to be down: {}", cassandraHost, e.getMessage());
      }
    } finally {
      try {
        conn.close();
      } catch (Exception e) { 
          // Ignoring this. Nothing we can do.
      }
    }

    ds = null;
    conn = null;
    return found;
  }
}
