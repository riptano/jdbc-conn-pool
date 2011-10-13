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
package com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy;

import java.util.List;

public interface Recorder {

  /**
   * Record an invocation.
   * @param inv an invocation
   */
  public void recordInvocation(Invocation inv);

  /**
   * Retrieves the List of invocations executed against this object.
   * @return
   */
  public List<Invocation> getInvocations();
  
  /**
   * Apply the invocation performed on the current object onto <code>target</code>
   * @param target an Object to apply the invocations to
   */
  public void applyInvocationsOn(Object target);

}
