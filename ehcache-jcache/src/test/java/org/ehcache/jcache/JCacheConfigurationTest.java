package org.ehcache.jcache;


import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


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

    /**
     * Helper to assert that given duration is either null or eternal.
     */
    private void assertDurationNullOrEternal(final Duration duration) {
        if (duration != null) {
            assertThat(duration.isEternal(), is(true));
        }
    }

    /**
     * Verify that pre-configured cache (in ehcache.xml) with eternal=true actually gets an EternalExpiryPolicy
     * and does not remove keys after first retrieval.
     */
    @Test
    public void preconfiguredEternalCache() {
        final CacheManager ehcache = CacheManager.getInstance();
        String cacheName = "simpleEternalCache";

        // sanity check configuration has eternal flag with ehcache native api
        assertThat(ehcache.getCache(cacheName).getCacheConfiguration().isEternal(), is(true));

        // stand up jcache manager and verify with jcache api that preconfigured cache is eternal
        JCacheManager cacheManager = new JCacheManager(null, ehcache, null, null);
        try {
            Cache<Object,Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
            assertThat(cache, notNullValue());

            // verify the jcache configuration impl has the right bits for expire policy
            JCacheConfiguration config = cache.getConfiguration(JCacheConfiguration.class);
            assertThat(config, notNullValue());

            ExpiryPolicy expiryPolicy = config.getExpiryPolicy();
            assertThat(expiryPolicy, notNullValue());

            // for some reason creation returns a value (Duration.ETERNAL), but access/update return null
            assertDurationNullOrEternal(expiryPolicy.getExpiryForCreation());
            assertDurationNullOrEternal(expiryPolicy.getExpiryForAccess());
            assertDurationNullOrEternal(expiryPolicy.getExpiryForUpdate());

            Object key = "foo";
            Object value = "bar";
            Object result = cache.getAndPut(key, value);
            assertThat(result, nullValue());

            // get should return same value
            result = cache.get(key);
            assertThat(result, is(value));

            // again, since its eternal, we get the same, if the policy was not eternal we'd get null here instead
            result = cache.get(key);
            assertThat(result, is(value));

            // and for sanity
            result = cache.get(key);
            assertThat(result, is(value));

            // remove it and its should be gone
            cache.remove(key);
            result = cache.get(key);
            assertThat(result, nullValue());
        }
        finally {
            // NOTE: This will NPE due to configuration above allowing null provider, and expecting non-null in close
            //cacheManager.close();
            ehcache.shutdown();
        }
    }

}
