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


import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheLoader;
import javax.cache.CacheWriter;
import javax.cache.Caching;
import javax.cache.OptionalFeature;
import javax.cache.Status;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.NotificationScope;
import javax.cache.transaction.IsolationLevel;
import javax.cache.transaction.Mode;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * The CacheManager that allows EHCache caches to be retrieved and accessed via JSR107 APIs
 *
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheManager implements javax.cache.CacheManager {
    private static final Logger LOG = LoggerFactory.getLogger(JCacheManager.class);

    private final HashMap<String, Cache<?, ?>> caches = new HashMap<String, Cache<?, ?>>();
    private final HashSet<Class<?>> immutableClasses = new HashSet<Class<?>>();

    private final ClassLoader classLoader;
    private final CacheManager ehcacheManager;


    /**
     * Creates a JCacheManager that uses the name of the cache to configure the underlying EhCache CacheManager
     * via an ehcache-name.xml config file
     *
     * @param name a {@link java.lang.String} object.
     * @param ehcacheManager a {@link net.sf.ehcache.CacheManager} object.
     * @param classLoader a {@link java.lang.ClassLoader} object.
     */
    public JCacheManager(String name, CacheManager ehcacheManager, ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("No classLoader specified");
        }
        if (name == null) {
            throw new NullPointerException("No name specified");
        }
        this.classLoader = classLoader;

        this.ehcacheManager = ehcacheManager;
        this.ehcacheManager.setName(name);
    }

    /**
     * Retrieve the underlying ehcache manager that this JCacheManager uses
     *
     * @return the underlying ehcache manager
     */
    public CacheManager getEhcacheManager() {
        return ehcacheManager;
    }


    /**
     * {@inheritDoc}
     * <p/>
     * The name returned will be that passed in to the constructor {@link #JCacheManager(String, net.sf.ehcache.CacheManager, ClassLoader)}
     */
    @Override
    public String getName() {
        return ehcacheManager.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Returns the status of this CacheManager.
     * <p/>
     */
    @Override
    public Status getStatus() {
        return JCacheStatusAdapter.adaptStatus(ehcacheManager.getStatus());
    }

    /** {@inheritDoc} */
    @Override
    public <K, V> CacheBuilder<K, V> createCacheBuilder(String cacheName) {
        return new JCacheBuilder<K, V>(cacheName, this, classLoader);
    }

    /** {@inheritDoc} */
    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) {
        if (getStatus() != Status.STARTED) {
            throw new IllegalStateException("CacheManager must be started before retrieving a cache");
        }
        synchronized (caches) {
            if (caches.containsKey(cacheName)) {
                return (Cache<K, V>) caches.get(cacheName);
            } else {
                Ehcache ehcache = ehcacheManager.getEhcache(cacheName);
                if (ehcache == null) {
                    return null;
                }
                JCacheEhcacheDecorator decoratedCache = new JCacheEhcacheDecorator<K,V>(ehcache);
                final JCache<K, V> cache = new JCache<K, V>(decoratedCache, this, this.classLoader);
                decoratedCache.setJcache(cache);

                caches.put(cacheName, cache);
                return cache;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<Cache<?, ?>> getCaches() {
        synchronized (caches) {
            String[] cacheNames = ehcacheManager.getCacheNames();
            HashSet<Cache<?, ?>> cacheSet = new HashSet<Cache<?, ?>>(cacheNames.length, 1.0f);
            for (String cacheName : cacheNames) {
                Cache<Object, Object> cache = getCache(cacheName);
                cacheSet.add(cache);
            }
            return Collections.unmodifiableSet(cacheSet);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeCache(String cacheName) {
        if (getStatus() != Status.STARTED) {
            throw new IllegalStateException();
        }
        if (cacheName == null) {
            throw new NullPointerException();
        }
        Cache<?, ?> oldCache;
        synchronized (caches) {
            oldCache = caches.remove(cacheName);
        }
        if (oldCache != null) {
            oldCache.stop();
        }

        return oldCache != null;
    }


    /** {@inheritDoc} */
    @Override
    public javax.transaction.UserTransaction getUserTransaction() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        return Caching.isSupported(optionalFeature);
    }

    /** {@inheritDoc} */
    @Override
    public void registerImmutableClass(Class<?> immutableClass) {
        if (immutableClass == null) {
            throw new NullPointerException();
        }
        immutableClasses.add(immutableClass);
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        if (getStatus() != Status.STARTED) {
            throw new IllegalStateException();
        }
        synchronized (immutableClasses) {
            immutableClasses.clear();
        }
        synchronized (caches) {
            ehcacheManager.shutdown();
            caches.clear();
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> T unwrap(java.lang.Class<T> cls) {
        if (cls.isAssignableFrom(this.getClass())) {
            return cls.cast(this);
        }

        throw new IllegalArgumentException("Unwapping to " + cls + " is not a supported by this implementation");
    }

    /**
     * Construct a CacheBuilder
     *
     * @param <K> the type of keys used by the Cache built by this CacheBuilder
     * @param <V> the type of values that are loaded by the Cache built by this CacheBuilder
     * @author Ryan Gardner
     */
    private class JCacheBuilder<K, V> implements CacheBuilder<K, V> {
        private final JCache.Builder<K, V> cacheBuilder;
        private Pattern namePattern = Pattern.compile("\\S+");

        public JCacheBuilder(String cacheName, JCacheManager jCacheManager, ClassLoader cl) {
            if (cacheName == null) {
                throw new NullPointerException("Cache name cannot be null");
            }
            if (!(namePattern.matcher(cacheName).find())) {
                throw new IllegalArgumentException("Cache name must contain one or more non-whitespace characters");
            }

            cacheBuilder = new JCache.Builder<K, V>(cacheName, jCacheManager, cl);
        }

        @Override
        public JCache<K, V> build() {
            JCache<K, V> cache = cacheBuilder.build();
            addInternal(cache);
            return cache;
        }


        @Override
        public CacheBuilder<K, V> setCacheLoader(CacheLoader<K, V> cacheLoader) {
            cacheBuilder.setCacheLoader(cacheLoader);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setCacheWriter(CacheWriter<K, V> cacheWriter) {
            cacheBuilder.setCacheWriter(cacheWriter);
            return this;
        }

        @Override
        public CacheBuilder<K, V> registerCacheEntryListener(CacheEntryListener<K, V> listener, NotificationScope scope, boolean synchronous) {
            cacheBuilder.registerCacheEntryListener(listener, scope, synchronous);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setStoreByValue(boolean storeByValue) {
            cacheBuilder.setStoreByValue(storeByValue);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setTransactionEnabled(IsolationLevel isolationLevel, Mode mode) {
            cacheBuilder.setTransactionEnabled(isolationLevel, mode);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setStatisticsEnabled(boolean enableStatistics) {
            cacheBuilder.setStatisticsEnabled(enableStatistics);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setReadThrough(boolean readThrough) {
            cacheBuilder.setReadThrough(readThrough);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setWriteThrough(boolean writeThrough) {
            cacheBuilder.setWriteThrough(writeThrough);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setExpiry(javax.cache.CacheConfiguration.ExpiryType type, javax.cache.CacheConfiguration.Duration timeToLive) {
            cacheBuilder.setExpiry(type, timeToLive);
            return this;
        }
    }

    private void addInternal(JCache cache) {

        synchronized (caches) {
            if (caches.containsKey(cache.getName())) {
                ehcacheManager.removeCache(cache.getName());
            }
            // remove the cache if it already exists
            if (ehcacheManager.getEhcache(cache.getName()) != null) {
                ehcacheManager.removeCache(cache.getName());
            }
            caches.remove(cache.getName());
            caches.put(cache.getName(), cache);
            // decorate the cache with a reference to the JCache
            ehcacheManager.addCache(new JCacheEhcacheDecorator(cache.getEhcache(),cache));

        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JCacheManager that = (JCacheManager) o;

        if (caches != null ? !caches.equals(that.caches) : that.caches != null) {
            return false;
        }
        if (classLoader != null ? !classLoader.equals(that.classLoader) : that.classLoader != null) {
            return false;
        }
        if (ehcacheManager != null ? !ehcacheManager.equals(that.ehcacheManager) : that.ehcacheManager != null) {
            return false;
        }
        if (immutableClasses != null ? !immutableClasses.equals(that.immutableClasses) : that.immutableClasses != null) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = caches != null ? caches.hashCode() : 0;
        result = 31 * result + (immutableClasses != null ? immutableClasses.hashCode() : 0);
        result = 31 * result + (classLoader != null ? classLoader.hashCode() : 0);
        result = 31 * result + (ehcacheManager != null ? ehcacheManager.hashCode() : 0);
        return result;
    }
}
