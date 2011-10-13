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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class AbstractRecorder implements Recorder {

  private List<Invocation> invocations;
  
  @Override
  public void recordInvocation(Invocation inv) {
    invocations.add(inv);
  }
  
  public AbstractRecorder() {
    invocations = new ArrayList<Invocation>();
  }

  public List<Invocation> getInvocations() {
    return invocations;
  }

  @Override
  public void applyInvocationsOn(Object target) {
    for (Invocation inv : invocations) {
      try {
        inv.getMethod().invoke(target, inv.getArguments());
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
    
  }

}
