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

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encoding and decoding utilities.
 *
 * @author Ran Tavory (rantav@gmail.com)
 *
 */
public final class StringUtils {

  private static final Logger log = LoggerFactory.getLogger(StringUtils.class);

  public static final String ENCODING = "utf-8";

  /**
   * Gets UTF-8 bytes from the string.
   *
   * @param s
   * @return
   */
  public static byte[] bytes(String s) {
    try {
      return s.getBytes(ENCODING);
    } catch (UnsupportedEncodingException e) {
      log.error("UnsupportedEncodingException ", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Utility for converting bytes to strings. UTF-8 is assumed.
   * @param bytes
   * @return
   */
  public static String string(byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    try {
      return new String(bytes, ENCODING);
    } catch (UnsupportedEncodingException e) {
      log.error("UnsupportedEncodingException ", e);
      throw new RuntimeException(e);
    }
  }

}
