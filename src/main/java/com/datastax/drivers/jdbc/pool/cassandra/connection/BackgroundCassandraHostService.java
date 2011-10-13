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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.datastax.drivers.jdbc.pool.cassandra.utils.DaemonThreadPoolFactory;


public abstract class BackgroundCassandraHostService {

  protected final ScheduledExecutorService executor;

  protected final HConnectionManager connectionManager;
  protected final CassandraHostConfigurator cassandraHostConfigurator;

  protected ScheduledFuture<?> sf;
  protected int retryDelayInSeconds;

  public BackgroundCassandraHostService(HConnectionManager connectionManager,
      CassandraHostConfigurator cassandraHostConfigurator) {
    executor = Executors.newScheduledThreadPool(1, new DaemonThreadPoolFactory(getClass()));
    this.connectionManager = connectionManager;
    this.cassandraHostConfigurator = cassandraHostConfigurator;
    
  }

  abstract void shutdown();

  abstract void applyRetryDelay();



  public int getRetryDelayInSeconds() {
    return retryDelayInSeconds;
  }

  public void setRetryDelayInSeconds(int retryDelayInSeconds) {
    this.retryDelayInSeconds = retryDelayInSeconds;
  }
  


}

