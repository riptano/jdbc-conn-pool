package com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 */
public class RecorderAwareProxy implements java.lang.reflect.InvocationHandler {

  private Object obj;

  public static Object newInstance(Object obj) {
    return java.lang.reflect.Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                                                    obj.getClass().getInterfaces(),
                                                    new RecorderAwareProxy(obj));
  }

  private RecorderAwareProxy(Object obj) {
    this.obj = obj;
  }

  public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {

    try {
      // Store the invocation if allowed.
      if (isRecordable(m)) {
        ((Recorder) obj).recordInvocation(new Invocation(m, args));
      }

      return m.invoke(obj, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } catch (Exception e) {
      throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
    }
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

}
