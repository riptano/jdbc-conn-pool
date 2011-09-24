package me.prettyprint.cassandra.service;


/**
 * Provides a more meaningful explanation of what the exception is about.
 *
 */
public interface ExceptionsTranslator {
  
  boolean isUnrecoverable(Throwable originalException);
  
  boolean hasTimedout(Throwable originalException);
  
  /**
   * It should be called after exhausting the other options.
   * As it is a generic translation.
   */
  boolean isATransportError(Throwable originalException);
  
  boolean isPoolExhausted(Throwable originalException);
    
}
