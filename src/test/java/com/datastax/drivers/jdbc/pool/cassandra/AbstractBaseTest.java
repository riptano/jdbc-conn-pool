package com.datastax.drivers.jdbc.pool.cassandra;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Base class for all Unit tests.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
//ApplicationContext will be loaded from "/applicationContext-test.xml" in the root of the classpath
@ContextConfiguration({"/applicationContext-test.xml"})
public class AbstractBaseTest extends BaseEmbededServerSetupTest{

    @Autowired
    protected ApplicationContext ctx;
    
    @Autowired
    protected JdbcTemplate jdbcTemplate;

}
