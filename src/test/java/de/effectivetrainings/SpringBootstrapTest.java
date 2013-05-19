package de.effectivetrainings;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * @author Martin Dilger
 * @since: 19.05.13
 */
@ContextConfiguration(value = "classpath:spring-context.xml")
public class SpringBootstrapTest extends AbstractJUnit4SpringContextTests {

    @Test
    public void configure(){
        //nothing to do, just bootstrap
    }
}
