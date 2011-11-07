/**
 *  Copyright 2003-2010 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.sf.ehcache.jcache;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.OptionalFeature;
import javax.cache.spi.CachingProvider;
import java.net.URL;


/**
 * A JSR107 adapter for EHCache.
 * <p/>
 *
 * @author Ryan Gardner
 */
public class JCacheCachingProvider implements CachingProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheManager createCacheManager(ClassLoader classLoader, String name) {
        if (name == null) {
            throw new NullPointerException("CacheManager name not specified");
        }
        net.sf.ehcache.CacheManager ehcacheManager = configureEhCacheManager(name, classLoader);
        return new JCacheManager(name, ehcacheManager, classLoader);
    }

    /**
     * Configures the underlying ehcacheManager - either by retrieving it via the
     * {@code ehcache-<NAME>.xml} or by creating a new CacheManager
     *
     * @param name        name of the CacheManager to create
     * @param classLoader
     * @return a CacheManager configured with that name
     */
    private net.sf.ehcache.CacheManager configureEhCacheManager(String name, ClassLoader classLoader) {
        net.sf.ehcache.CacheManager cacheManager;
        String defaultName = "ehcache-" + name + ".xml";

        URL configResource = classLoader.getResource(defaultName);
        if (!name.equals(Caching.DEFAULT_CACHE_MANAGER_NAME) && configResource != null) {
            cacheManager = net.sf.ehcache.CacheManager.create(configResource);
        } else {
            // return the default EhCache singleton if the default CacheManager is requested or there is no mapped
            // config file for that cache manager name
            cacheManager = new net.sf.ehcache.CacheManager();
        }
        return cacheManager;
    }


    /**
     * {@inheritDoc}
     * <p/>
     * By default, use the thread's context ClassLoader.
     */
    @Override
    public ClassLoader getDefaultClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Currently, this JCache decroator
     * does not support {@link OptionalFeature#TRANSACTIONS}
     * or {@link OptionalFeature#ANNOTATIONS} or
     * {@link OptionalFeature#STORE_BY_REFERENCE}
     */
    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        switch (optionalFeature) {
            case ANNOTATIONS:
                return false;
            case TRANSACTIONS:
                return false;
            case STORE_BY_REFERENCE:
                return true;
            default:
                return false;
        }
    }
}
