package com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mark a class to be recordable by {@link RecorderAwareProxy}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Recordable {

}
