package com.datastax.drivers.jdbc.pool.cassandra.exceptions;


/**
 * Error while borrowing or returning object to the pool
 */
public final class HPoolInnactiveException extends HPoolException {

  private static final long serialVersionUID = 674846452472399010L;

  public HPoolInnactiveException(String s){
    super(s);
  }

  public HPoolInnactiveException(Throwable t){
    super(t);
  }
}
