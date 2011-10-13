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
 * System properties used by Hector.
 *
 * @author Ran Tavory (rantav@gmail.com)
 *
 */
public enum SystemProperties {

  /**
   * Should hector perform name resolution?
   * Default: no; Do not perform name resolution
   * For example: -DHECTOR_PERFORM_NAME_RESOLUTION=true
   */
  HECTOR_PERFORM_NAME_RESOLUTION,

  /**
   * What's the default socket timeout for thrift in miliseconds.
   * Default: system settings (about a minute or more)
   * Example: -DCASSANDRA_THRIFT_SOCKET_TIMEOUT=5000
   */
  CASSANDRA_THRIFT_SOCKET_TIMEOUT,
}
