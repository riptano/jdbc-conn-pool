package com.datastax.drivers.jdbc.pool.cassandra.utils;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ReflectionUtilsTest {

  @Before
  public void setUp() throws Exception {
  }
  
  @Test
  public void testSetPrivateFields() throws Exception {
    Foo foo = new Foo();
    Object obj = ReflectionUtils.getPrivateField(foo, "age");
    assertEquals(1, obj);
    
    // Set the field
    ReflectionUtils.setPrivateField(foo, "age", 4);
    obj = ReflectionUtils.getPrivateField(foo, "age");
    assertEquals(4, obj);
  }

  
  class Foo {
    
    private int age = 1;
  }

}
