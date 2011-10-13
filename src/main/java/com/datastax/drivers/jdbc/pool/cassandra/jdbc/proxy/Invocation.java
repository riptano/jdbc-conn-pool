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

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class Invocation {

  private Method method;

  private Object[] arguments;

  public Invocation(Method method, Object[] args) {
    this.method = method;
    this.arguments = expandVarArgs(method.isVarArgs(), args);
  }

  public Method getMethod() {
    return method;
  }

  public Object[] getArguments() {
    return arguments;
  }

  private static Object[] expandVarArgs(final boolean isVarArgs,
      final Object[] args) {
    if (!isVarArgs) {
      return args == null ? new Object[0] : args;
    }
    if (args[args.length - 1] == null) {
      return args;
    }
    Object[] varArgs = createObjectArray(args[args.length - 1]);
    final int nonVarArgsCount = args.length - 1;
    final int varArgsCount = varArgs.length;
    Object[] newArgs = new Object[nonVarArgsCount + varArgsCount];
    System.arraycopy(args, 0, newArgs, 0, nonVarArgsCount);
    System.arraycopy(varArgs, 0, newArgs, nonVarArgsCount, varArgsCount);
    return newArgs;
  }

  private static Object[] createObjectArray(Object array) {
    if (array instanceof Object[]) {
      return (Object[]) array;
    }
    Object[] result = new Object[Array.getLength(array)];
    for (int i = 0; i < Array.getLength(array); i++) {
      result[i] = Array.get(array, i);
    }
    return result;
  }

}
