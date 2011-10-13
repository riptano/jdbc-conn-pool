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

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Iterables;


/**
 * Implements a RoundRobin balancing policy based off the contents
 * of the active {@link HClientPool}. If a pool is shutdown by another 
 * thread in the midst of the selection process, we return the pool
 * at position 0
 *
 * @author zznate
 */
public class RoundRobinBalancingPolicy implements LoadBalancingPolicy {

  private static final long serialVersionUID = 1107204068032227079L;
  private int counter;
  
  public RoundRobinBalancingPolicy() {
    counter = 0;
  }
  
  @Override
  public HClientPool getPool(Collection<HClientPool> pools,
      Set<CassandraHost> excludeHosts) {
    HClientPool pool = getPoolSafely(pools);    
    if ( excludeHosts != null && excludeHosts.size() > 0 ) {
      while ( excludeHosts.contains(pool.getCassandraHost()) ) {
        pool = getPoolSafely(pools);
        if ( excludeHosts.size() >= pools.size() )
          break;
      }
    }    
    return pool;
  }
  
  private HClientPool getPoolSafely(Collection<HClientPool> pools) {
    try {
      return Iterables.get(pools, getAndIncrement(pools.size()));
    } catch (IndexOutOfBoundsException e) {
      return pools.iterator().next();
    }       
  }
    
  private int getAndIncrement(int size) {
    int counterToReturn;
    
    // There should not be that much of contention here as
    // the "if" statement plus the increment is executed real fast.
    synchronized (this) {
      if (counter >= 16384) {
        counter = 0;
      }
      counterToReturn = counter++;
    }

    return counterToReturn % size;
  }

  @Override
  public HClientPool createConnection(CassandraHost host) throws SQLException {
  	return new ConcurrentHClientPool(host);
  }
}
