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

import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;


import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;

import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HCassandraInternalException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HInvalidRequestException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HNotFoundException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HPoolException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HPoolExhaustedException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HPoolInnactiveException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HTimedOutException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HUnavailableException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HectorException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HectorTransportException;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.PoolIllegalStateException;

public final class ExceptionsTranslatorImpl implements ExceptionsTranslator {

  public HectorException translate(Throwable original) {
    if (original instanceof HectorException) {
      return (HectorException) original;
    } else if (original instanceof TApplicationException) {
      return new HCassandraInternalException(((TApplicationException)original).getType(), original.getMessage());    
    } else if (original instanceof TTransportException) {
      // if the underlying cause is a scoket timeout, reflect that directly
      // TODO this may be an issue on the Cassandra side which warrants ivestigation.
      // I seem to remember these coming back as TimedOutException previously
      if ( ((TTransportException)original).getCause() instanceof SocketTimeoutException ) {
        return new HTimedOutException(original);
      } else {
        return new HectorTransportException(original);
      }
    } else if (original instanceof org.apache.cassandra.thrift.TimedOutException) {
      return new HTimedOutException(original);
    } else if (original instanceof org.apache.cassandra.thrift.InvalidRequestException) {
      // See bug https://issues.apache.org/jira/browse/CASSANDRA-1862
      // If a bootstrapping node is accessed it responds with IRE. It makes more sense to throw
      // UnavailableException.
      // Hector wraps this caveat and fixes this.
      String why = ((org.apache.cassandra.thrift.InvalidRequestException) original).getWhy();
      if (why != null && why.contains("bootstrap")) {
        return new HUnavailableException(original);
      }
      HInvalidRequestException e = new HInvalidRequestException(original);
      e.setWhy(why);
      return e;
    } else if (original instanceof HPoolExhaustedException ) {
      return (HPoolExhaustedException)original;
    } else if (original instanceof TProtocolException) {
      return new HInvalidRequestException(original);
    } else if (original instanceof org.apache.cassandra.thrift.NotFoundException) {
      return new HNotFoundException(original);
    } else if (original instanceof org.apache.cassandra.thrift.UnavailableException) {
      return new HUnavailableException(original);
    } else if (original instanceof TException) {
      return new HectorTransportException(original);
    } else if (original instanceof NoSuchElementException) {
      return new HPoolExhaustedException(original);
    } else if (original instanceof IllegalStateException) {
      return new PoolIllegalStateException(original);
    } else {
      return new HectorException(original);
    }
  }

  @Override
  public boolean isUnrecoverable(Throwable originalException) {
    Throwable ex = originalException.getCause();
    if (ex == null) 
      return false;
    if (ex instanceof org.apache.cassandra.thrift.InvalidRequestException)
      return true;
    if (ex instanceof TProtocolException)
      return true;
    if (ex instanceof TApplicationException)
      return true;
    if (ex instanceof UnavailableException)
      return true;

    return false;
  }

  @Override
  public boolean hasTimedout(Throwable originalException) {
    Throwable ex = originalException.getCause();
    if (ex == null) 
      return false;
    if (ex instanceof TTransportException && ex.getCause() instanceof SocketTimeoutException)
      return true;
    if (ex instanceof org.apache.cassandra.thrift.TimedOutException)
      return true;

    return false;
  }

  @Override
  public boolean isPoolExhausted(Throwable originalException) {
    if (originalException instanceof HPoolExhaustedException)
      return true;
    if (originalException instanceof HPoolInnactiveException)
      return true;
    if (originalException instanceof HPoolException)
      return true;
    if (originalException instanceof NoSuchElementException)
      return true;

    return false;
  }

  @Override
  public boolean isATransportError(Throwable originalException) {
    Throwable ex = originalException.getCause();
    if (ex == null) 
      return false;
    if (ex instanceof TException)
      return true;
    return false;
  }

}
