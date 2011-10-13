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

/**
 * A generic low weight assert utility, very similar with Spring's Assert class,
 * just without the dependency on Spring
 *
 * See for example
 * http://www.jarvana.com/jarvana/view/org/springframework/spring
 * /1.2.9/spring-1.2.9-javadoc.jar!/org/springframework/util/Assert.html
 *
 * @author Ran Tavory
 *
 */
public final class Assert {

  public static void notNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void noneNull(Object... object) {
    for (int i = 0; i < object.length; ++i) {
      if (object[i] == null) {
        throw new NullPointerException("Null not allowed, number " + (i + 1));
      }
    }
  }

  public static void isTrue(boolean b, String message) {
    if (!b) {
      throw new IllegalArgumentException(message);
    }
  }
}
