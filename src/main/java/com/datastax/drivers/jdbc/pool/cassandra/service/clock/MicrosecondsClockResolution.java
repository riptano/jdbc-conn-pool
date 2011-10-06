package com.datastax.drivers.jdbc.pool.cassandra.service.clock;

import com.datastax.drivers.jdbc.pool.cassandra.ClockResolution;

/**
 * Milliseconds Resolution used to create clocks.
 *
 * @author Patricio Echague (pechague@gmail.com)
 */
public class MicrosecondsClockResolution extends AbstractClockResolution implements ClockResolution {

  private static final long serialVersionUID = 3371730161836986201L;
  private static final long ONE_THOUSAND = 1000L;

  @Override
  public long createClock() {
    return getSystemMilliseconds() * ONE_THOUSAND;
  }

}
