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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

public class LeastActiveBalancingPolicyTest extends BaseBalancingPolicyTest {

  private LeastActiveBalancingPolicy leastActiveBalancingPolicy;
  
  @Test
  public void testGetPoolOk() {
    leastActiveBalancingPolicy = new LeastActiveBalancingPolicy();
    assertEquals(poolWith5Active, leastActiveBalancingPolicy.getPool(pools, null));
    assertEquals(poolWith5Active, leastActiveBalancingPolicy.getPool(pools, null));
    assertEquals(poolWith5Active, leastActiveBalancingPolicy.getPool(pools, null));    
    Mockito.when(poolWith5Active.getNumActive()).thenReturn(8);
    assertEquals(poolWith7Active, leastActiveBalancingPolicy.getPool(pools, null));
    assertEquals(poolWith7Active, leastActiveBalancingPolicy.getPool(pools, null));
    assertEquals(poolWith7Active, leastActiveBalancingPolicy.getPool(pools, null));
    Mockito.when(poolWith5Active.getNumActive()).thenReturn(4);
    assertEquals(poolWith5Active, leastActiveBalancingPolicy.getPool(pools, null));
    assertEquals(poolWith5Active, leastActiveBalancingPolicy.getPool(pools, null));
    assertEquals(poolWith5Active, leastActiveBalancingPolicy.getPool(pools, null));
  }
  
  @Test
  public void testSkipExhausted() {    
    leastActiveBalancingPolicy = new LeastActiveBalancingPolicy();
    assertEquals(poolWith7Active, leastActiveBalancingPolicy.getPool(pools, new HashSet<CassandraHost>(Arrays.asList(new CassandraHost("127.0.0.1:9160")))));
    assertEquals(poolWith5Active, leastActiveBalancingPolicy.getPool(pools, new HashSet<CassandraHost>(Arrays.asList(new CassandraHost("127.0.0.2:9161")))));
  }
  
  @Test
  public void testShuffleOnAllEqual() {
    ConcurrentHClientPool poolWith5Active2 = Mockito.mock(ConcurrentHClientPool.class);    
    Mockito.when(poolWith5Active2.getNumActive()).thenReturn(5);
    Mockito.when(poolWith5Active2.getCassandraHost()).thenReturn(new CassandraHost("127.0.0.4:9163"));
    ConcurrentHClientPool poolWith5Active3 = Mockito.mock(ConcurrentHClientPool.class);    
    Mockito.when(poolWith5Active3.getNumActive()).thenReturn(5);
    Mockito.when(poolWith5Active3.getCassandraHost()).thenReturn(new CassandraHost("127.0.0.5:9164"));
    
    pools.add(poolWith5Active2);
    pools.add(poolWith5Active3);
    
    leastActiveBalancingPolicy = new LeastActiveBalancingPolicy();
    // should hit all three equal hosts over the course of 50 runs
    Set<CassandraHost> foundHosts = new HashSet<CassandraHost>(3);
    for (int i = 0; i < 50; i++) {
      HClientPool foundPool = leastActiveBalancingPolicy.getPool(pools, null);
      foundHosts.add(foundPool.getCassandraHost());
      assert 5 == foundPool.getNumActive();        
    }
    assertEquals(3, foundHosts.size());
  }
}
