package org.ehcache.jcache;


import org.junit.Test;

import net.sf.ehcache.config.CacheConfiguration;

public class JCacheConfigurationTest {

    @Test
    public void shouldCreateJCacheConfigurationFromOtherInstance() {
        //given
        CacheConfiguration ehCacheConfig = new CacheConfiguration("testCache", 1000);
        JCacheConfiguration originJCacheConfig = new JCacheConfiguration(ehCacheConfig);

        //when
        new JCacheConfiguration(originJCacheConfig);
        
        //then - no Exception
    }
    
}
