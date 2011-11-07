package net.sf.ehcache.jcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.Caching;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

public class JCacheAndEhcacheAccessTest {

    @Test
    public void ehcacheIsBeingPickedAsCacheProvider() {
        Cache foo = Caching.getCacheManager().createCacheBuilder("foo").build();
        assertThat(foo, is(JCache.class));
    }
    
    @Test
    public void ehcacheStartedThenJCacheAccess() {
        URL basicUrl = getClass().getResource("/ehcache-basic.xml");        
        CacheManager basicManager = CacheManager.create(basicUrl);
        Ehcache sampleCache = basicManager.getEhcache("sampleCache");
        
        javax.cache.Cache jcache = Caching.getCacheManager("basic").getCache("sampleCache");
        assertThat((Ehcache) jcache.unwrap(Ehcache.class), is(sameInstance(sampleCache)));
    }

}
