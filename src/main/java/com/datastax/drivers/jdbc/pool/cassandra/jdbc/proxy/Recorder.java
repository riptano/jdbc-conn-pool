package com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy;

import java.util.List;

public interface Recorder {

  /**
   * Retrieves the List of invocations executed against this object.
   * @return
   */
  public List<Invocation> getInvocations();
  
  /**
   * Apply the invocation performed on the current object onto <code>target</code>
   * @param target an Object to apply the invocations to
   */
  public void applyInvocationsOn(Object target);

}
