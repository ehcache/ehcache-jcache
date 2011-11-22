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

import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.OptionalFeature;
import javax.cache.spi.CachingProvider;
import java.net.URL;
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


    /** {@inheritDoc} */
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
     * <p/>
     * JSR107 has unique CacheManagers per classLoader. The CacheManager in EHCache 2.5 wont allow multiple cacheManagers
     * with the same name to be created so this method will sometimes change the underlying ehcache cachemanager name
     * to be unique (which will make it more difficult to pull it back)
     *
     * @param name        name of the CacheManager to create
     * @param classLoader
     * @return a CacheManager configured with that name
     */
    private net.sf.ehcache.CacheManager configureEhCacheManager(String name, ClassLoader classLoader) {
        net.sf.ehcache.CacheManager cacheManager;

        Configuration config = getInitialConfigurationForCacheManager(name, classLoader);

        // in ehcache 2.5.0 it started enforcing that CacheManagers could only be created once per name.
        // but we have to at one per classloader with the same name in order to pass the TCK tests
        //
        // appending the toString of the classLoader will allow us to pass the TCK.
        //
        // Once ehcache's CacheManager can handle returning CacheManagers with the same name
        // and different classLoaders (perhaps using the same underlying cache?) this workaround
        // can be removed
        //
        config.setName(name + classLoader.toString());
        LOG.debug("CacheName was set to {} used with classLoader {}", name, classLoader.toString());

        cacheManager = net.sf.ehcache.CacheManager.create(config);

        return cacheManager;
    }

    /**
     * This gets the initial configuration - either from a named cache file or from the default config
     * returned from ehCache.
     *
     * @param name cache manager name
     * @param classLoader classloader to use to retrieve resources
     * @return the initial configuration for the cache manager
     */
    private Configuration getInitialConfigurationForCacheManager(String name, ClassLoader classLoader) {
        String defaultName = "ehcache-" + name + ".xml";
        Configuration configuration;

        URL configResource = null;
        if (name != Caching.DEFAULT_CACHE_MANAGER_NAME) {
            configResource = classLoader.getResource(defaultName);
        }
        if (configResource != null) {
            configuration = ConfigurationFactory.parseConfiguration(configResource);
        } else {
            configuration = ConfigurationFactory.parseConfiguration();
        }
        return configuration;
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
     * or {@link OptionalFeature#ANNOTATIONS} but does support
     * {@link OptionalFeature#STORE_BY_REFERENCE}
     */
    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        switch (optionalFeature) {
            case ANNOTATIONS:
                return true;
            case TRANSACTIONS:
                return false;
            case STORE_BY_REFERENCE:
                return true;
            default:
                return false;
        }
    }
}
