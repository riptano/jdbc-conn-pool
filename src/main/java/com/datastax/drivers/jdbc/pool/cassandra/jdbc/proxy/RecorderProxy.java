package com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.jca.cci.connection.NotSupportedRecordFactory;

/**
 * 
 *
 */
public class RecorderProxy implements java.lang.reflect.InvocationHandler, Recorder {

  private static final String RECORDER_METHOD_NAME;
  private Object obj;
  private List<Invocation> invocations;
  
  static {
    RECORDER_METHOD_NAME = Recorder.class.getDeclaredMethods()[0].getName();
  }

  public static Object newInstance(Object obj) {
    return java.lang.reflect.Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                                                    addRecorderInterface(obj.getClass().getInterfaces()),
                                                    new RecorderProxy(obj));
  }

  private static Class<?>[] addRecorderInterface(Class<?>[] interfaces) {
    Class<?>[] newInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
    // Add the recorder interface to the original list so we can cast as Recorder later on and request the 
    // recorded methods.
    newInterfaces[interfaces.length] = Recorder.class;
    assert newInterfaces.length == interfaces.length + 1;
    return newInterfaces;
  }

  private RecorderProxy(Object obj) {
    this.obj = obj;
    invocations = new ArrayList<Invocation>();
  }

  public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
    Object result;

    try {

      // Store the invocation if allowed.
      if (isRecordable(m)) {
        invocations.add(new Invocation(m, args));
      }

      if (m.getDeclaringClass().equals(Recorder.class ))
        return invokeLocal(m, args);
      else
        result = m.invoke(obj, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } catch (Exception e) {
      throw new RuntimeException("unexpected invocation exception: "
          + e.getMessage());
    }

    return result;
  }

  private Object invokeLocal(Method m, Object[] args) throws Throwable {
    return this.getClass().getMethod(m.getName(), m.getParameterTypes()).invoke(this, args);
  }

  /*
   * Invoke the local method when this is called. That way, we don't make the wrapped classes implement Recordable.
   */
  private boolean getInvocationsCalled(Method m) {
    return (m.getName().equals(RECORDER_METHOD_NAME));
  }

  /**
   * Retrieves whether this methods is recordable. A method is recordable if it
   * has been annotated with {@link Recordable} anotation
   * 
   * @param m
   *          a method
   * @return TRUE if this method is recordable. FALSE otherwise.
   * @throws NoSuchMethodException 
   * @throws SecurityException 
   */
  private boolean isRecordable(Method m) throws SecurityException {
    // Since we cannot retrieve the annotations from the Proxy object, let's lookup in the original object.
    try {
      Method originalMethod = getOriginalMethod(m);
      return originalMethod.getAnnotation(Recordable.class) != null;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  private Method getOriginalMethod(Method m) throws SecurityException, NoSuchMethodException {
    return obj.getClass().getMethod(m.getName(), m.getParameterTypes());
  }

  // ---------------- Recorder Interface ----------------------

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
