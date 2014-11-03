package org.ehcache.jcache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import static org.hamcrest.CoreMatchers.is;
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
}