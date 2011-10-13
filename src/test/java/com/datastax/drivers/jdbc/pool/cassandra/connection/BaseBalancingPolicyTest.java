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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.mockito.Mockito;

public abstract class BaseBalancingPolicyTest {
  protected List<HClientPool> pools = new ArrayList<HClientPool>();
  
  protected ConcurrentHClientPool poolWith5Active;
  protected ConcurrentHClientPool poolWith7Active;
  protected ConcurrentHClientPool poolWith10Active;
  
  
  @Before
  public void setup() {
    poolWith5Active = Mockito.mock(ConcurrentHClientPool.class);    
    Mockito.when(poolWith5Active.getNumActive()).thenReturn(5);
    poolWith7Active = Mockito.mock(ConcurrentHClientPool.class);
    Mockito.when(poolWith7Active.getNumActive()).thenReturn(7);
    poolWith10Active = Mockito.mock(ConcurrentHClientPool.class);
    Mockito.when(poolWith10Active.getNumActive()).thenReturn(10);
    
    Mockito.when(poolWith5Active.getCassandraHost()).thenReturn(new CassandraHost("127.0.0.1:9160"));
    Mockito.when(poolWith7Active.getCassandraHost()).thenReturn(new CassandraHost("127.0.0.2:9161"));
    Mockito.when(poolWith10Active.getCassandraHost()).thenReturn(new CassandraHost("127.0.0.3:9162"));
    
    pools.add(poolWith5Active);        
    pools.add(poolWith7Active);
    pools.add(poolWith10Active);
  }
  
}
