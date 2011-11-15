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

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CopyStrategyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheException;
import javax.cache.CacheLoader;
import javax.cache.CacheManager;
import javax.cache.CacheStatistics;
import javax.cache.CacheWriter;
import javax.cache.InvalidConfigurationException;
import javax.cache.Status;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.NotificationScope;
import javax.cache.transaction.IsolationLevel;
import javax.cache.transaction.Mode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a {@see Cache} that wraps an underlying {@see net.sf.ehcache.ehcache} cache.
 *
 * @param <K> the type of keys used by this JCache
 * @param <V> the type of values that are loaded by this JCache
 * @author Ryan Gardner
 */
public class JCache<K, V> implements Cache<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(JCache.class);

    private static final int CACHE_LOADER_THREADS = 2;
    private static final int DEFAULT_EXECUTOR_TIMEOUT = 10;

    private final ExecutorService executorService = Executors.newFixedThreadPool(CACHE_LOADER_THREADS);


    private final Set<ScopedListener<K, V>> cacheEntryListeners = new CopyOnWriteArraySet<ScopedListener<K, V>>();

    /**
     * An Ehcache backing instance
     */
    private Ehcache ehcache;
    private JCacheManager cacheManager;
    private JCacheCacheLoaderAdapter cacheLoaderAdapter;
    private JCacheCacheWriterAdapter cacheWriterAdapter;
    private ClassLoader classLoader;

    private JCacheConfiguration configuration;

    /**
     * A constructor for JCache.
     * <p/>
     * JCache is an adaptor for an Ehcache, and therefore requires an Ehcache in its constructor.
     * <p/>
     * <p/>
     * // TODO - perhaps this should not be exposed
     *
     * @param ehcache      An ehcache
     * @param cacheManager the {@see CacheManager} that manages this wrapped ehcache
     * @param classLoader  the classloader to use to serialize / deserialize cache entries
     * @see "class description for recommended usage"
     * @since 1.4
     */
    public JCache(Ehcache ehcache, JCacheManager cacheManager, ClassLoader classLoader) {
        this.cacheManager = cacheManager;
        this.ehcache = ehcache;
        this.classLoader = classLoader;
        this.configuration = new JCacheConfiguration(ehcache.getCacheConfiguration());
    }

    /**
     * Retrieve the underlying ehcache cache.
     *
     * @return the ehcache that this JCache adapter wraps
     */
    public Ehcache getEhcache() {
        return ehcache;
    }


    private void checkStatusStarted() {
        if (!JCacheStatusAdapter.adaptStatus(ehcache.getStatus()).equals(Status.STARTED)) {
            throw new IllegalStateException("The cache status is not STARTED");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(K key) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        Element cacheElement = ehcache.get(key);
        if (cacheElement == null) {
            if (cacheLoaderAdapter != null /* && configuration.isReadThrough() */) {
                return getFromLoader(key);
            } else {
                return null;
            }
        }
        return (V) cacheElement.getValue();
    }

    private V getFromLoader(K key) {
        Cache.Entry<K, V> entry = (Entry<K, V>) cacheLoaderAdapter.load(key);
        if (entry != null) {
            ehcache.put(new Element(entry.getKey(), entry.getValue()));
            return entry.getValue();
        } else {
            return null;
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) throws CacheException {
        checkStatusStarted();
        if (keys == null || keys.contains(null)) {
            throw new NullPointerException("key cannot be null");
        }
        // will throw NPE if keys=null
        HashMap<K, V> map = new HashMap<K, V>(keys.size());
        for (K key : keys) {
            map.put(key, get(key));
        }
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(K key) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        return ehcache.isKeyInCache(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<V> load(K key) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        if (ehcache.getRegisteredCacheLoaders().size() == 0) {
            return null;
        }
        FutureTask<V> task = new FutureTask<V>(new JCacheLoaderCallable<K, V>(this, key));
        executorService.submit(task);
        return task;
    }

    private void checkKey(Object key) {
        if (key == null) {
            throw new NullPointerException("key can't be null");
        }
    }

    private void checkValue(Object value) {
        if (value == null) {
            throw new NullPointerException("value can't be null");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Map<K, V>> loadAll(Collection<? extends K> keys) throws CacheException {
        checkStatusStarted();
        if (keys == null) {
            throw new NullPointerException("keys");
        }
        if (keys.contains(null)) {
            throw new NullPointerException("key");
        }
        if (cacheLoaderAdapter == null) {
            return null;
        }
        FutureTask<Map<K, V>> task =
                new FutureTask<Map<K, V>>(
                        new JCacheLoaderLoadAllCallable<K, V>(
                                this, cacheLoaderAdapter.getJCacheCacheLoader(), keys
                        )
                );
        executorService.submit(task);
        return task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheStatistics getStatistics() {
        checkStatusStarted();
        if (!(configuration.isStatisticsEnabled())) {
            return null;
        } else {
            return new JCacheStatistics(this, ehcache.getLiveCacheStatistics());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(K key, V value) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        checkValue(value);
        ehcache.put(new Element(key, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getAndPut(K key, V value) throws CacheException, NullPointerException, IllegalStateException {
        checkStatusStarted();
        if (key == null || value == null) {
            throw new NullPointerException("Key cannot be null");
        }
        try {
            Element old = ehcache.get(key);
            put(key, value);
            return old != null ? (V) old.getValue() : null;
        } catch (Exception e) {
            throw new CacheException("Unable to getAndPut.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) throws CacheException {
        checkStatusStarted();
        if (map == null || map.containsKey(null)) {
            throw new NullPointerException("Map of keys cannot be null, and no key in map can be null");
        }
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean putIfAbsent(K key, V value) throws CacheException {
        checkStatusStarted();

        boolean present = containsKey(key);
        if (present) {
            return false;
        } else {
            put(key, value);
        }
        return containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(K key) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        return ehcache.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(K key, V oldValue) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        checkValue(oldValue);

        if (containsKey(key) && get(key).equals(oldValue)) {
            return remove(key);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getAndRemove(K key) throws CacheException {
        checkStatusStarted();
        checkKey(key);

        if (containsKey(key)) {
            V oldValue = get(key);
            remove(key);
            return oldValue;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        checkValue(oldValue);
        checkValue(newValue);

        return ehcache.replace(new Element(key, oldValue), new Element(key, newValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replace(K key, V value) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        checkValue(value);
        return (ehcache.replace(new Element(key, value)) != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getAndReplace(K key, V value) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        checkValue(value);
        Element replaced = ehcache.replace(new Element(key, value));
        return replaced != null ? (V) replaced.getValue() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAll(Collection<? extends K> keys) throws CacheException {
        checkStatusStarted();
        for (K key : keys) {
            remove(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAll() throws CacheException {
        checkStatusStarted();
        ehcache.removeAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCacheConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerCacheEntryListener(CacheEntryListener<? super K, ? super V> cacheEntryListener, NotificationScope scope, boolean synchronous) {
        checkValue(cacheEntryListener);
        checkValue(scope);

        JCacheListenerAdapter listener = new JCacheListenerAdapter(cacheEntryListener);
        ScopedListener<K, V> scopedListener = new ScopedListener<K, V>(listener, scope, synchronous);

        ehcache.getCacheEventNotificationService().registerListener(listener, JCacheListenerAdapter.adaptScope(scope));
        return cacheEntryListeners.add(scopedListener);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterCacheEntryListener(CacheEntryListener<?, ?> cacheEntryListener) {
        if (cacheEntryListener == null) {
            return false;
        }

        //Only cacheEntryListener is checked for equality
        JCacheListenerAdapter<K, V> listenerAdapter = new JCacheListenerAdapter<K, V>((CacheEntryListener<K, V>) cacheEntryListener);
        ScopedListener<K, V> scopedListener = new ScopedListener<K, V>(listenerAdapter, null, true);
        ehcache.getCacheEventNotificationService().unregisterListener(listenerAdapter);
        return cacheEntryListeners.remove(scopedListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.ehcache.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T unwrap(Class<T> cls) {
        if (this.getClass().isAssignableFrom(cls)) {
            return (T) this;
        }
        if (cls.isAssignableFrom(Ehcache.class)) {
            return (T) ehcache;
        }
        throw new IllegalArgumentException("Can't cast the the specified class");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws CacheException {
        //
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws CacheException {
        executorService.shutdown();
        try {
            executorService.awaitTermination(DEFAULT_EXECUTOR_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        }
        if (ehcache.getStatus().equals(net.sf.ehcache.Status.STATUS_ALIVE)) {
            ehcache.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status getStatus() {
        return JCacheStatusAdapter.adaptStatus(ehcache.getStatus());
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Entry<K, V>> iterator() {
        checkStatusStarted();
        return new EhcacheIterator(ehcache.getKeys().iterator());
    }

    /**
     * Iterator for EHCache Entries
     *
     * @author Ryan Gardner
     */
    private class EhcacheIterator implements Iterator<Entry<K, V>> {
        private final Iterator keyIterator;
        private K lastKey = null;

        public EhcacheIterator(Iterator keyIterator) {
            this.keyIterator = keyIterator;
        }

        /**
         * {@inheritdoc}
         */
        public boolean hasNext() {
            return keyIterator.hasNext();
        }

        /**
         * {@inheritdoc}
         */
        public Entry<K, V> next() {
            final K key = (K) keyIterator.next();
            lastKey = key;
            return new JCacheEntry<K, V>(ehcache.get(key));
        }

        /**
         * {@inheritdoc}
         */
        public void remove() {
            if (lastKey == null) {
                throw new IllegalStateException();
            }
            ehcache.remove(lastKey);
            lastKey = null;
        }
    }

    protected JCacheCacheLoaderAdapter<K, V> getCacheLoaderAdapter() {
        return this.cacheLoaderAdapter;
    }

    protected JCacheCacheWriterAdapter<K, V> getCacheWriterAdapter() {
        return this.cacheWriterAdapter;
    }

    /**
     * Callable used for cache loader.
     * <p/>
     * (Based on the CacheLoader pattern used in {@link javax.cache.implementation.RICache.RICacheLoaderLoadCallable})
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     * @author Ryan Gardner
     */
    @SuppressWarnings("JavadocReference")
    private static class JCacheLoaderCallable<K, V> implements Callable<V> {
        private final JCache<K, V> cache;
        private final K key;

        JCacheLoaderCallable(JCache<K, V> cache, K key) {
            this.cache = cache;
            this.key = key;
        }

        @Override
        public V call() throws Exception {
            if (key == null) {
                throw new NullPointerException("Can't load null values");
            }
            // the adapter is convenient to pass of to the underlying Ehcache so other things that
            // hit the ehcache directly can call things like getWithLoader() - but here want to speak native JSR107
            // to the CacheLoader here so we retrieve the adapted loader and hit it directly for this load method.
            Cache.Entry<K, V> loadedEntry = (Cache.Entry<K, V>) cache.getCacheLoaderAdapter().getJCacheCacheLoader().load(key);
            V loadedValue = loadedEntry.getValue();
            if (loadedValue == null) {
                throw new NullPointerException("Can't load null values");
            }
            cache.put(loadedEntry.getKey(), loadedEntry.getValue());
            return loadedValue;
        }
    }

    /**
     * Callable used for cache loader.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     * @author Yannis Cosmadopoulos
     * @link javax.cache.implementation.RICache.RICacheLoaderLoadAllCallable
     */
    private static class JCacheLoaderLoadAllCallable<K, V> implements Callable<Map<K, V>> {
        private final JCache<K, V> cache;
        private final Collection<? extends K> keys;
        private final CacheLoader<K, V> cacheLoader;

        JCacheLoaderLoadAllCallable(JCache<K, V> cache, CacheLoader<K, V> cacheLoader, Collection<? extends K> keys) {
            this.cache = cache;
            this.keys = keys;
            this.cacheLoader = cacheLoader;
        }

        @Override
        public Map<K, V> call() throws Exception {
            ArrayList<K> keysNotInStore = new ArrayList<K>();

            // ehcache has an underlying loadAll method that could, potentially,
            // be used instead of this.
            for (K key : keys) {
                if (!cache.containsKey(key)) {
                    keysNotInStore.add(key);
                }
            }
            Map<K, V> value = cacheLoader.loadAll(keysNotInStore);
            cache.putAll(value);
            return value;
        }
    }

    /**
     * A Builder for the JCache wrapper for Ehcache.
     * <p/>
     * Using this builder, you can create a new Cache. When the cache is built by calling the
     * {@link #build()} method the cache will be created and added to the cache manager.
     *
     * @param <K> The type of keys that are used for this cache
     * @param <V> The type of values that are stored in this cache
     * @author Ryan Gardner
     */
    public static class Builder<K, V> implements CacheBuilder<K, V> {
        private String cacheName;
        private ClassLoader classLoader;

        private JCacheConfiguration cacheConfiguration;
        private CacheLoader<K, V> cacheLoader;
        private CacheWriter<K, V> cacheWriter;

        private final CopyOnWriteArraySet<ListenerRegistration<K, V>> listeners = new CopyOnWriteArraySet<ListenerRegistration<K, V>>();
        private final JCacheConfiguration.Builder configurationBuilder = new JCacheConfiguration.Builder();
        private JCacheManager cacheManager;


        /**
         * Create a new CacheBuilder that can be used to initialize a new cache with the
         * {@code cacheName} in the {@code cacheManager} using the specified {@code classLoader}
         *
         * @param cacheName    name of the cache that this cacheBuilder will create
         * @param cacheManager the {@link CacheManager} that will manage this cache when it is built
         * @param classLoader  the classLoader that will be used for this cache
         */
        public Builder(String cacheName, JCacheManager cacheManager, ClassLoader classLoader) {
            this.cacheName = cacheName;
            this.cacheManager = cacheManager;
            this.classLoader = classLoader;
        }


        @Override
        public JCache<K, V> build() {
            if (cacheName == null) {
                throw new InvalidConfigurationException("cache name can't be null");
            }
            cacheConfiguration = configurationBuilder.build();
            if (cacheConfiguration.isReadThrough() && (cacheLoader == null)) {
                throw new InvalidConfigurationException("cacheLoader can't be null on a readThrough cache");
            }
            if (cacheConfiguration.isWriteThrough() && (cacheWriter == null)) {
                throw new InvalidConfigurationException("cacheWriter can't be null on a writeThrough cache");
            }

            cacheConfiguration.getCacheConfiguration().setName(cacheName);

            if (cacheConfiguration.isStoreByValue()) {
                cacheConfiguration.getCacheConfiguration().setCopyOnWrite(true);
                cacheConfiguration.getCacheConfiguration().setCopyOnRead(true);
                CopyStrategyConfiguration copyStrategyConfiguration =
                        cacheConfiguration.getCacheConfiguration().getCopyStrategyConfiguration();
                copyStrategyConfiguration.setCopyStrategyInstance(new JCacheCopyOnWriteStrategy(this.classLoader));
            }


            cacheConfiguration.getCacheConfiguration().setStatistics(cacheConfiguration.isStatisticsEnabled());


            // this needs to be exposed via configuration methods
            cacheConfiguration.getCacheConfiguration().setDiskPersistent(false);

            net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(cacheConfiguration.getCacheConfiguration());
            JCache<K, V> jcache = new JCache<K, V>(cache, this.cacheManager, this.classLoader);
            jcache.configuration = cacheConfiguration;

            Ehcache decoratedCache = new JCacheEhcacheDecorator(cache, jcache);
            jcache.ehcache = decoratedCache;

            if (cacheLoader != null) {
                jcache.cacheLoaderAdapter = (new JCacheCacheLoaderAdapter(cacheLoader));
                // needed for the loadAll
                jcache.ehcache.registerCacheLoader(jcache.cacheLoaderAdapter);
            }
            if (cacheWriter != null) {
                jcache.cacheWriterAdapter = (new JCacheCacheWriterAdapter(cacheWriter));
                // needed for the writeAll
                jcache.ehcache.registerCacheWriter(jcache.cacheWriterAdapter);
            }
            for (ListenerRegistration listenerRegistration : listeners) {
                jcache.registerCacheEntryListener(
                        listenerRegistration.cacheEntryListener,
                        listenerRegistration.scope,
                        listenerRegistration.synchronous
                );
            }

            return jcache;
        }

        /**
         * Set the cache loader.
         *
         * @param cacheLoader the CacheLoader
         * @return the builder
         */
        @Override
        public Builder<K, V> setCacheLoader(CacheLoader<K, V> cacheLoader) {
            if (cacheLoader == null) {
                throw new NullPointerException("cacheLoader");
            }
            this.cacheLoader = cacheLoader;
            return this;
        }

        @Override
        public CacheBuilder<K, V> setCacheWriter(CacheWriter<K, V> cacheWriter) {
            if (cacheWriter == null) {
                throw new NullPointerException("cacheWriter");
            }
            this.cacheWriter = cacheWriter;
            return this;
        }

        @Override
        public CacheBuilder<K, V> registerCacheEntryListener(CacheEntryListener<K, V> listener, NotificationScope scope, boolean synchronous) {
            listeners.add(new ListenerRegistration<K, V>(listener, scope, synchronous));
            return this;
        }

        @Override
        public CacheBuilder<K, V> setStoreByValue(boolean storeByValue) {
            configurationBuilder.setStoreByValue(storeByValue);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setTransactionEnabled(IsolationLevel isolationLevel, Mode mode) {
            configurationBuilder.setTransactionEnabled(isolationLevel, mode);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setStatisticsEnabled(boolean enableStatistics) {
            configurationBuilder.setStatisticsEnabled(enableStatistics);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setReadThrough(boolean readThrough) {
            configurationBuilder.setReadThrough(readThrough);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setWriteThrough(boolean writeThrough) {
            configurationBuilder.setWriteThrough(writeThrough);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setExpiry(CacheConfiguration.ExpiryType type, CacheConfiguration.Duration timeToLive) {
            if (type == null) {
                throw new NullPointerException();
            }
            if (timeToLive == null) {
                throw new NullPointerException();
            }
            configurationBuilder.setExpiry(type, timeToLive);
            return this;
        }
    }

    /**
     * Combine a Listener and its NotificationScope.  Equality and hashcode are based purely on the listener.
     * This implies that the same listener cannot be added to the set of registered listeners more than
     * once with different notification scopes.
     *
     * @author Greg Luck
     */
    private static final class ScopedListener<K, V> {
        private final JCacheListenerAdapter<K, V> listener;
        private final NotificationScope scope;
        private final boolean synchronous;

        private ScopedListener(JCacheListenerAdapter<K, V> listener, NotificationScope scope, boolean synchronous) {
            this.listener = listener;
            this.scope = scope;
            this.synchronous = synchronous;
        }

        private JCacheListenerAdapter<K, V> getListener() {
            return listener;
        }

        private NotificationScope getScope() {
            return scope;
        }

        /**
         * Hash code based on listener
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return listener.hashCode();
        }

        /**
         * Equals based on listener (NOT based on scope) - can't have same listener with two different scopes
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ScopedListener<?, ?> other = (ScopedListener<?, ?>) obj;
            if (listener == null) {
                if (other.listener != null) {
                    return false;
                }
            } else if (!listener.equals(other.listener)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return listener.toString();
        }
    }

    /**
     * A struct :)
     *
     * @param <K>
     * @param <V>
     */
    private static final class ListenerRegistration<K, V> {
        private final CacheEntryListener<K, V> cacheEntryListener;
        private final NotificationScope scope;
        private final boolean synchronous;

        private ListenerRegistration(CacheEntryListener<K, V> cacheEntryListener, NotificationScope scope, boolean synchronous) {
            this.cacheEntryListener = cacheEntryListener;
            this.scope = scope;
            this.synchronous = synchronous;
        }
    }


}
