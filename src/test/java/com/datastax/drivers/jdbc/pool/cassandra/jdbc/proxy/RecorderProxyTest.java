package com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy;


import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


public class RecorderProxyTest {

  @Before
  public void setUp() throws Exception {
  }
  
  @Test
  public void testProxy() throws Exception {
    
    Thread.currentThread().getStackTrace();
    IFoo foo = (IFoo) RecorderProxy.newInstance(new Foo());
    
    foo.setAge((byte) 13);
    foo.setLastName("Johnson");
    foo.setName("Mr", "Brian");
    foo.setNonRecordable(Long.valueOf(100));
    
    // Assert the results.
    List<Invocation> invocations = ((Recorder) foo).getInvocations();
    assertEquals(3, invocations.size());
    assertInvocation(invocations.get(0), "setAge", (byte) 13);
    assertInvocation(invocations.get(1), "setLastName", "Johnson");
    assertInvocation(invocations.get(2), "setName", "Mr", "Brian");

    // Now let's re-create the object based on the recorded invocations
    
    IFoo clonedFoo = (IFoo) RecorderProxy.newInstance(new Foo());
    assertEquals(0, ((Recorder) clonedFoo).getInvocations().size());

    // Copy
    ((Recorder) foo).applyInvocationsOn(clonedFoo);

    // Assert the results.
    invocations = ((Recorder) clonedFoo).getInvocations();
    assertEquals(3, invocations.size());
    assertInvocation(invocations.get(0), "setAge", (byte) 13);
    assertInvocation(invocations.get(1), "setLastName", "Johnson");
    assertInvocation(invocations.get(2), "setName", "Mr", "Brian");
  }
  
  
  private void assertInvocation(Invocation invocation, String methodName, Object... params) {
    assertEquals(methodName, invocation.getMethod().getName());
    assertArrayEquals(params, invocation.getArguments());
  }

  interface IFoo {

    void setName(String prefix, String name);

    void setLastName(String lastName);

    void setAge(byte age);

    void setNonRecordable(Long aLong);
  }

  class Foo implements IFoo {

    @Recordable
    public void setName(String prefix, String name) {
      // NO-OP
    }
    
    @Recordable
    public void setLastName(String lastName) {
      // NO-OP
    }

    @Recordable
    public void setAge(byte age) {
      // NO-OP
    }

    public void setNonRecordable(Long aLong) {
      // NO-OP
    }
  }

}
