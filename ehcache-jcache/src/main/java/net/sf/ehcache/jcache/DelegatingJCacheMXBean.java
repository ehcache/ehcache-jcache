package net.sf.ehcache.jcache;

import org.jsr107.ri.DelegatingCacheMXBean;
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
