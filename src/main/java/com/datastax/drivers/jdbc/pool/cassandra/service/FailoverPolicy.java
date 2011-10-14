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

/**
 * What should the client do if a call to cassandra node fails and we suspect that the node is
 * down. (e.g. it's a communication error, not an application error).
 *
 * {@value #FAIL_FAST} will return the error as is to the user and not try anything smart
 *
 * {@value #ON_FAIL_TRY_ONE_NEXT_AVAILABLE} will try one more random server before returning to the
 * user with an error
 *
 * {@value #ON_FAIL_TRY_ALL_AVAILABLE} will try all available servers in the cluster before giving
 * up and returning the communication error to the user.
 *
 */
public enum FailoverPolicy {

  /** On communication failure, just return the error to the client and don't retry */
  FAIL_FAST(0, 0),

  /** On communication error try one more server before giving up */
  ON_FAIL_TRY_ONE_NEXT_AVAILABLE(1, 0),

  /** On communication error try all known servers before giving up */
  ON_FAIL_TRY_ALL_AVAILABLE(Integer.MAX_VALUE - 1, 0);

  public final int numRetries;

  public final int sleepBetweenHostsMilli;

  private FailoverPolicy(int numRetries, int sleepBwHostsMilli) {
    this.numRetries = numRetries;
    sleepBetweenHostsMilli = sleepBwHostsMilli;
  }
}
