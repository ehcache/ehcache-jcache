package net.sf.ehcache.jcache;

import javax.cache.CacheManager;
import javax.cache.OptionalFeature;
import javax.cache.spi.CachingProvider;


public class JCacheCachingProvider implements CachingProvider {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CacheManager createCacheManager(ClassLoader classLoader, String name) {
        if (name == null) {
            throw new NullPointerException("CacheManager name not specified");
        }
        return new JCacheManager(name, classLoader);
    }

    /**
     * {@inheritDoc}
     *
     * By default, use the thread's context ClassLoader.
     */
    @Override
    public ClassLoader getDefaultClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * {@inheritDoc}
     *
     * Currently, this JCache decroator
     * does not support {@link OptionalFeature#TRANSACTIONS}
     *  or {@link OptionalFeature#ANNOTATIONS} or
     *  {@link OptionalFeature#STORE_BY_REFERENCE}
     *
     */
    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        switch (optionalFeature) {
            case ANNOTATIONS:
                return false;
            case TRANSACTIONS:
                return false;
            case STORE_BY_REFERENCE:
                return false;
            default:
                return false;
        }
    }
}
