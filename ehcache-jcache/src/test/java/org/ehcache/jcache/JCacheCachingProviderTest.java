package org.ehcache.jcache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class JCacheCachingProviderTest {

    @Test
    public void testLoadsXMLFile() {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        assertNull(cacheManager.getCache("foo"));
        final Cache<Object, Object> sampleCache = cacheManager.getCache("sampleCache");
        assertNotNull(sampleCache);
        sampleCache.put("key", "value");
        final Ehcache unwrapped = sampleCache.unwrap(Ehcache.class);
        final Element element = unwrapped.get("key");
        assertThat(element.isEternal(), is(false));
        assertThat(element.getTimeToIdle(), is(360));
        assertThat(element.getTimeToLive(), is(1000));
    }
    
    @Test
    public void testCreatingCacheInRuntime() {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        String cacheName = "nonExistingCache";
        assertNull(cacheManager.getCache(cacheName));
        
        Configuration configuration = new JCacheConfiguration(new MutableConfiguration());
        cacheManager.createCache(cacheName, configuration);
        
        final Cache<Object, Object> sampleCache = cacheManager.getCache(cacheName);
        assertNotNull(sampleCache);
        sampleCache.put("key", "value");
        final Ehcache unwrapped = sampleCache.unwrap(Ehcache.class);
        final Element element = unwrapped.get("key");
        assertEquals("value", element.getObjectValue());
        assertThat(element.isEternal(), is(true));
        assertThat(element.getTimeToIdle(), is(0));
        assertThat(element.getTimeToLive(), is(0));
    }
    
    @Test
    public void testShuttingDownProviderWithManagerNotFoundByClassloader() {
        JCacheCachingProvider provider = new JCacheCachingProvider();

        JCacheManager manager = new JCacheManager(null, new net.sf.ehcache.CacheManager(), null, null);
        provider.shutdown(manager);
    }
}
