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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Selects the least active host based on the number of active connections.
 * The list of hosts is shuffled on each pass to account for the case
 * where a number of hosts are at the minimum number of connections
 * (ie. they are not busy).
 * 
 * 
 * @author zznate
 */
public class LeastActiveBalancingPolicy implements LoadBalancingPolicy {
  
  private static final long serialVersionUID = 329849818218657061L;
  private static final Logger log = LoggerFactory.getLogger(LeastActiveBalancingPolicy.class);
  
  @Override
  public HClientPool getPool(Collection<HClientPool> pools, Set<CassandraHost> excludeHosts) {
    List<HClientPool> vals = Lists.newArrayList(pools);
    // shuffle pools to avoid always returning the same one when we are not terribly busy
    Collections.shuffle(vals);
    Collections.sort(vals, new ShufflingCompare());
    Iterator<HClientPool> iterator = vals.iterator();
    HClientPool concurrentHClientPool = iterator.next();
    if ( excludeHosts != null && excludeHosts.size() > 0 ) {
      while (iterator.hasNext()) {        
        if ( !excludeHosts.contains(concurrentHClientPool.getCassandraHost()) ) {
          break;
        }
        concurrentHClientPool = (ConcurrentHClientPool) iterator.next();
      }
    }
    return concurrentHClientPool;
  }

  private final class ShufflingCompare implements Comparator<HClientPool> {
    
    public int compare(HClientPool o1, HClientPool o2) {
      if ( log.isDebugEnabled() ) {
        log.debug("comparing 1: {} and count {} with 2: {} and count {}",
          new Object[]{o1.getCassandraHost(), o1.getNumActive(), o2.getCassandraHost(), o2.getNumActive()});
      }
      return o1.getNumActive() - o2.getNumActive();      
    }
  }
  
  @Override
  public HClientPool createConnection(CassandraHost host) throws SQLException {
	  return new ConcurrentHClientPool(host);
  }
}
