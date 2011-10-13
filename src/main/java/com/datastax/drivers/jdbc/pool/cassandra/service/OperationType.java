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
 * Specifies the "type" of operation - read or write.
 * It's used for Speed4j, so should be in sync with hectorLog4j.xml
 * @author Ran Tavory (ran@outbain.com)
 * 
 */
public enum OperationType {
  /** Read operations*/
  READ,
  /** Write operations */
  WRITE,
  /** Meta read operations, such as describe*() */
  META_READ,
  /** Operation on one of the system_ methods */
  META_WRITE,
  /** A borrow client operation */
  BORROW_CLIENT,
  /** a CQL operation */
  CQL;
}