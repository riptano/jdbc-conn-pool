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
package com.datastax.drivers.jdbc.pool.cassandra.model;

import com.datastax.drivers.jdbc.pool.cassandra.ResultStatus;
import com.datastax.drivers.jdbc.pool.cassandra.connection.CassandraHost;


/**
 * Describes the state of the executed cassandra command.
 * This is a handy call metadata inspector which reports the call's execution time, status,
 * which actual host was connected etc.
 *
 * @author Ran
 * @author zznate
 */
public class ExecutionResult<T> implements ResultStatus {

  private final T value;
  private final long execTime;
  private final CassandraHost cassandraHost;
  
  protected static final String BASE_MSG_FORMAT = "%s took (%dus) for query (%s) on host: %s";
  private static final int MICRO_DENOM = 1000;

  public ExecutionResult(T value, long execTime, CassandraHost cassandraHost) {
    this.value = value;
    this.execTime = execTime;
    this.cassandraHost = cassandraHost;
  }

  /**
   * @return the actual value returned from the query (or null if it was a mutation that doesn't
   * return a value)
   */
  public T get() {
    return value;
  }

  /**
   * @return the execution time, as already recorded, in nanos
   */
  public long getExecutionTimeNano() {
    return execTime;
  }

  /**
   * Execution time is actually recorded in nanos, so we divide this by 1000 
   * make the number more sensible
   * @return
   */
  public long getExecutionTimeMicro() {
    return execTime / MICRO_DENOM;
  }


  @Override
  public String toString() {
    return formatMessage("ExecutionResult", "n/a");
  }
  
  protected String formatMessage(String resultName, String query) {
    return String.format(BASE_MSG_FORMAT, resultName, getExecutionTimeMicro(), query, (cassandraHost != null ? cassandraHost.getName() : "[none]"));
  }

  /** The cassandra host that was actually used to execute the operation */
  public CassandraHost getHostUsed() {
    return this.cassandraHost;
  }


}
