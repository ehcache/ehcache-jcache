package net.sf.ehcache.jcache;

import net.sf.ehcache.Ehcache;

/**
 * @author Alex Snaps
 */
public class JCacheMXBean {
    protected final JCache jCache;
    private final String name;

    public JCacheMXBean(final JCache jCache, final String name) {
        this.jCache = jCache;
        this.name = name;
    }

    private String sanitize(String string) {
        return string == null ? "" : string.replaceAll(",|:|=|\n", ".");
    }

    String getObjectName() {
        String cacheManagerName = sanitize(jCache.getCacheManager().getURI().toString());
        String cacheName = sanitize(jCache.getName());

        return "javax.cache:type=Cache" + name + ",CacheManager="
                              + cacheManagerName + ",Cache=" + cacheName;
    }

    Ehcache getEhcache() {
        return (Ehcache) jCache.unwrap(Ehcache.class);
    }

}
