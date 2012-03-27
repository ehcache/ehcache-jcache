package net.sf.ehcache.jcache;

import javax.cache.implementation.DelegatingCacheMXBean;
import javax.cache.mbeans.CacheMXBean;


/**
 *
 */
public class DelegatingJCacheMXBean<K,V> extends DelegatingCacheMXBean implements CacheMXBean {
    private JCache<K,V> cache;

    public DelegatingJCacheMXBean(JCache cache) {
        super(cache);
    }

}
