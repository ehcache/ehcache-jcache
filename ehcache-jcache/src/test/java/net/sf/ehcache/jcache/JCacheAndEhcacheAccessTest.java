package net.sf.ehcache.jcache;

import net.sf.ehcache.Ehcache;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import java.util.concurrent.TimeUnit;

import static javax.cache.CacheConfiguration.Duration;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JCacheAndEhcacheAccessTest {

    @Test
    public void ehcacheIsBeingPickedAsCacheProvider() {
        Cache foo = Caching.getCacheManager().createCacheBuilder("foo")
                .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new Duration(TimeUnit.MINUTES, 10))
                .setStoreByValue(false)
                .build();
        assertThat(foo, is(notNullValue()));
        assertThat(foo, is(JCache.class));
    }

    @Test
    public void namedEhcacheDotXMLReadWhenOneExists() {
        javax.cache.Cache jcache = Caching.getCacheManager("basic").getCache("sampleCache");
        assertThat(jcache, is(notNullValue()));
        assertThat((jcache.unwrap(Ehcache.class)), is(notNullValue()));
    }

    @Test
    public void namedEhcachePropertiesUsedWhenOneExists() {
        JCache jcache = (JCache) Caching.getCacheManager("basic").getCache("sampleCache");
        assertThat(jcache, is(notNullValue()));
        assertThat("Store by value is only true if copyOnRead and copyOnWrite are both configured in the xml config",
                jcache.getConfiguration().isStoreByValue(), is(false));
        assertThat(jcache.getConfiguration().getExpiry(CacheConfiguration.ExpiryType.ACCESSED).getTimeUnit(),
                is(equalTo(TimeUnit.SECONDS)));
        assertThat(jcache.getConfiguration().getExpiry(CacheConfiguration.ExpiryType.ACCESSED).getDurationAmount(),
                is(360L));

        assertThat(jcache.getConfiguration().getExpiry(CacheConfiguration.ExpiryType.MODIFIED).getTimeUnit(),
                is(equalTo(TimeUnit.SECONDS)));
        assertThat(jcache.getConfiguration().getExpiry(CacheConfiguration.ExpiryType.MODIFIED).getDurationAmount(),
                is(1000L));
        assertThat(jcache.getConfiguration().getCacheConfiguration().isOverflowToDisk(), is(true));
    }

    @Test
    public void nullCacheWhenNoCacheExists() {
        JCache jcache = (JCache) Caching.getCacheManager("basic").getCache("nonexistent-cache");
        assertThat(jcache, nullValue());
    }

}
