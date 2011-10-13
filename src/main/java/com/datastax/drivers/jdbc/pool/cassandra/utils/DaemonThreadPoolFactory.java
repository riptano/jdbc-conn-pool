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
package com.datastax.drivers.jdbc.pool.cassandra.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadPoolFactory implements ThreadFactory {

  private ConcurrentHashMap<String, AtomicInteger> counters = 
      new ConcurrentHashMap<String, AtomicInteger>();
  
  private final String name;

  public DaemonThreadPoolFactory(Class<?> parentClass) {
    this.name = "Hector." + parentClass.getName();
  }

  private int getNextThreadNumber() {
    if(!counters.containsKey(name)) {
      counters.putIfAbsent(name, new AtomicInteger());
    }
    return counters.get(name).incrementAndGet();
  }
  
  @Override
  public Thread newThread(Runnable r) {
      Thread t = new Thread(r);
      t.setDaemon(true);
      t.setName(name + "-" + getNextThreadNumber());
      return t;
  }
    
}