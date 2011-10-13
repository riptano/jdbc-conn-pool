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
package com.datastax.drivers.jdbc.pool.cassandra.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides access to private members in classes.
 */
public class ReflectionUtils {

  public static Object getPrivateField(Object o, String fieldName) {
    // Check we have valid arguments...
    assert o != null;
    assert fieldName != null;

    // Go and find the private field...
    final Field fields[] = o.getClass().getDeclaredFields();
    for (int i = 0; i < fields.length; ++i) {
      if (fieldName.equals(fields[i].getName())) {
        try {
          fields[i].setAccessible(true);
          return fields[i].get(o);
        } catch (IllegalAccessException ex) {
          throw new RuntimeException("IllegalAccessException accessing " + fieldName, ex);
        }
      }
    }
    throw new RuntimeException("Field name does not exist: " + fieldName);
  }
  
  public static void setPrivateField(Object o, String fieldName, Object value) {
    try {
      Class<?> c = o.getClass();
      Field f = c.getDeclaredField(fieldName);
      f.setAccessible(true); // gettho solution.
      f.set(o, value); 

      // production code should handle these exceptions more gracefully
    } catch (NoSuchFieldException x) {
      throw new RuntimeException(x);
    } catch (IllegalArgumentException x) {
      throw new RuntimeException(x);
    } catch (IllegalAccessException x) {
      throw new RuntimeException(x);
    }
  }

  public static Object invokePrivateMethod(Object o, String methodName, Object... params) {
    // Check we have valid arguments...
    assert o != null;
    assert methodName != null;
    assert params != null;

    // Go and find the private method...
    final Method methods[] = o.getClass().getDeclaredMethods();
    for (int i = 0; i < methods.length; ++i) {
      if (methodName.equals(methods[i].getName())) {
        try {
          methods[i].setAccessible(true);
          return methods[i].invoke(o, params);
        } catch (IllegalAccessException ex) {
          throw new RuntimeException("IllegalAccessException accessing " + methodName, ex);
        } catch (InvocationTargetException ite) {
          throw new RuntimeException("InvocationTargetException accessing " + methodName, ite);
        }
      }
    }
    throw new RuntimeException("Method '" + methodName + "' not found");
  }
  

}
