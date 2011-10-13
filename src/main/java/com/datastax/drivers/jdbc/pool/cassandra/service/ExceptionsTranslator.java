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
 * Provides a more meaningful explanation of what the exception is about.
 *
 */
public interface ExceptionsTranslator {
  
  boolean isUnrecoverable(Throwable originalException);
  
  boolean hasTimedout(Throwable originalException);
  
  /**
   * It should be called after exhausting the other options.
   * As it is a generic translation.
   */
  boolean isATransportError(Throwable originalException);
  
  boolean isPoolExhausted(Throwable originalException);
    
}
