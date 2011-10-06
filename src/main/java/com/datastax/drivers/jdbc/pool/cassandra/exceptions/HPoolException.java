package com.datastax.drivers.jdbc.pool.cassandra.exceptions;


/**
 * Base class for Pool Exceptions.
 *
 */
public class HPoolException extends HectorException {

  private static final long serialVersionUID = -4893070614308587017L;

  public HPoolException(String msg) {
    super(msg);
  }

  public HPoolException(Throwable t) {
    super(t);
  }
}
