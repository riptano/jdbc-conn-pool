package com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class Invocation {

  private Method method;

  private Object[] arguments;

  public Invocation(Method method, Object[] args) {
    this.method = method;
    this.arguments = expandVarArgs(method.isVarArgs(), args);
  }

  public Method getMethod() {
    return method;
  }

  public Object[] getArguments() {
    return arguments;
  }

  private static Object[] expandVarArgs(final boolean isVarArgs,
      final Object[] args) {
    if (!isVarArgs) {
      return args == null ? new Object[0] : args;
    }
    if (args[args.length - 1] == null) {
      return args;
    }
    Object[] varArgs = createObjectArray(args[args.length - 1]);
    final int nonVarArgsCount = args.length - 1;
    final int varArgsCount = varArgs.length;
    Object[] newArgs = new Object[nonVarArgsCount + varArgsCount];
    System.arraycopy(args, 0, newArgs, 0, nonVarArgsCount);
    System.arraycopy(varArgs, 0, newArgs, nonVarArgsCount, varArgsCount);
    return newArgs;
  }

  private static Object[] createObjectArray(Object array) {
    if (array instanceof Object[]) {
      return (Object[]) array;
    }
    Object[] result = new Object[Array.getLength(array)];
    for (int i = 0; i < Array.getLength(array); i++) {
      result[i] = Array.get(array, i);
    }
    return result;
  }

}
