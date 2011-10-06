package com.datastax.drivers.jdbc.pool.cassandra.exceptions;


/**
 * Indicates that a client pool has been exhausted.
 */
public final class HPoolExhaustedException extends HPoolException {

  private static final long serialVersionUID = -6200999597951673383L;

  public HPoolExhaustedException(String msg) {
    super(msg);
  }

  public HPoolExhaustedException(Throwable t) {
    super(t);
  }
}
