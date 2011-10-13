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
package com.datastax.drivers.jdbc.pool.cassandra.service;

import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HPoolExhaustedException;

/**
 * Policy what to do when the connection pool is exhausted.
 * @author Ran Tavory (rantav@gmail.com)
 *
 */
public enum ExhaustedPolicy {
  /**
   * If the pool is full, fail with the exception {@link PoolExhaustedException}
   */
  WHEN_EXHAUSTED_FAIL, 
  /**
   * When pool exhausted, grow.
   */
  WHEN_EXHAUSTED_GROW, 
  /**
   * Block the requesting thread when the pool is exhausted until new connections are available.
   */
  WHEN_EXHAUSTED_BLOCK
}