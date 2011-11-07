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
import javax.cache.OptionalFeature;
import javax.cache.spi.CachingProvider;


/**
 * A JSR107 adapter for EHCache.
 * <p/>
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
