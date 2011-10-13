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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class HostTimeoutTrackerTest {

  private HostTimeoutTracker hostTimeoutTracker;

  @Before
  public void setup() {
    // map cassandraHost --> arrayBlockingQueue of timestamp Longs
    // - if abq.size > size, && peekLast.get > time, add host to excludedMap<cassandraHost,timestampOfExclusion> for <suspendTime> ms
    // - background thread to sweep for exclusion time expiration every <sweepInterval> seconds
    // - getExcludedHosts calls excludedMap.keySet
    //
    // HTL with a three timeout trigger durring 500ms intervals
    CassandraHostConfigurator cassandraHostConfigurator = new CassandraHostConfigurator("localhost:9170");
    cassandraHostConfigurator.setHostTimeoutCounter(3);
    HConnectionManager connectionManager = new HConnectionManager("TestCluster", cassandraHostConfigurator);
    hostTimeoutTracker = new HostTimeoutTracker(connectionManager, cassandraHostConfigurator);
  }

  @Test
  public void testTrackHostLatency() {
    CassandraHost cassandraHost = new CassandraHost("localhost:9170");
    assertFalse(hostTimeoutTracker.checkTimeout(cassandraHost));
    assertFalse(hostTimeoutTracker.checkTimeout(cassandraHost));
    assertFalse(hostTimeoutTracker.checkTimeout(cassandraHost));
    try {
      Thread.currentThread().sleep(501);
    } catch (InterruptedException e) {

    }

    assertTrue(hostTimeoutTracker.checkTimeout(cassandraHost));
  }
}
