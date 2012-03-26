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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManagerFactory;
import javax.cache.OptionalFeature;
import javax.cache.spi.CachingProvider;
import java.util.HashSet;
import java.util.Set;


/**
 * A JSR107 adapter for EHCache.
 * <p/>
 *
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheCachingProvider implements CachingProvider {
    private static Logger LOG = LoggerFactory.getLogger(JCacheCachingProvider.class);

    private static Set<String> cachesCreated = new HashSet<String>();

    /**
     *{@inheritDoc}
     */
    @Override
    public CacheManagerFactory getCacheManagerFactory() {
        return JCacheCacheManagerFactory.getInstance();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Currently, this JCache decroator
     * does not support {@link OptionalFeature#TRANSACTIONS} but does support
     * {@link OptionalFeature#STORE_BY_REFERENCE}
     */
    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        switch (optionalFeature) {
            case TRANSACTIONS:
                return false;
            case STORE_BY_REFERENCE:
                return true;
            default:
                return false;
        }
    }
}
