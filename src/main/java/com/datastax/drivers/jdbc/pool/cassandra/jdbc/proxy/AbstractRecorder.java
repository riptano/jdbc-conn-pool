package com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class AbstractRecorder implements Recorder {

  private List<Invocation> invocations;
  
  @Override
  public void recordInvocation(Invocation inv) {
    invocations.add(inv);
  }
  
  public AbstractRecorder() {
    invocations = new ArrayList<Invocation>();
  }

  public List<Invocation> getInvocations() {
    return invocations;
  }

  @Override
  public void applyInvocationsOn(Object target) {
    for (Invocation inv : invocations) {
      try {
        inv.getMethod().invoke(target, inv.getArguments());
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
    
  }

}
