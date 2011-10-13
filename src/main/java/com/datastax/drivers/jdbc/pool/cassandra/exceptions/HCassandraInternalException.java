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
package com.datastax.drivers.jdbc.pool.cassandra.exceptions;

/**
 * Designed to loosely wrap TApplicationException which can be generated
 * by Apache Cassandra under a variety of ambiguous conditions - some of
 * them transient, some of them not. 
 *
 * @author zznate
 */
public class HCassandraInternalException extends HectorException {

  private static final long serialVersionUID = -266109391311421129L;
  
  private int type;
  
  private static final String ERR_MSG = 
    "Cassandra encountered an internal error processing this request: ";
  
  public HCassandraInternalException(String msg) {
    super(ERR_MSG + msg); 
  }

  public HCassandraInternalException(int type, String msg) {
    super(ERR_MSG + "TApplicationError type: " + type + " message:" + msg);
    this.type = type;
  }

  public HCassandraInternalException(String s, Throwable t) {
    super(ERR_MSG + s, t); 
  }

  public HCassandraInternalException(Throwable t) {
    super(t); 
  }

  /**
   * The underlying 'type' directly from TApplicationException#getType
   * @return
   */
  public int getType() {
    return type;
  }
 
 

}
