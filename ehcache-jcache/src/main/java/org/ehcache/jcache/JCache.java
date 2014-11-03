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
package org.ehcache.jcache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryListener;
import javax.cache.expiry.Duration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.cache.processor.MutableEntry;

/**
 * Implementation of a {@link Cache} that wraps an underlying ehcache.
 *
 * @param <K> the type of keys used by this JCache
 * @param <V> the type of values that are loaded by this JCache
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCache<K, V> implements Cache<K, V> {

    private static final Object NOT_THERE = new Object();

    private final JCacheConfiguration<K, V> cfg;
    private final Ehcache ehcache;
    private final JCacheManager cacheManager;
    private final CacheLoader<K, V> cacheLoader;
    private final CacheWriter cacheWriter;
    private volatile boolean closed = false;

    public JCache(final JCacheManager cacheManager, final JCacheConfiguration<K, V> cfg, final Ehcache ehcache) {
        if(ehcache == null) throw new NullPointerException();
        this.cacheManager = cacheManager;
        this.cfg = cfg;
        this.ehcache = ehcache;
        final Factory<CacheLoader<K, V>> cacheLoaderFactory = cfg.getCacheLoaderFactory();
        if (cacheLoaderFactory != null) {
            this.cacheLoader = cacheLoaderFactory.create();
        } else {
            this.cacheLoader = null;
        }
        final Factory<CacheWriter<? super K,? super V>> cacheWriterFactory = cfg.getCacheWriterFactory();
        if (cacheWriterFactory != null) {
            this.cacheWriter = cacheWriterFactory.create();
        } else {
            this.cacheWriter = null;
        }
        ehcache.registerCacheWriter(new JCacheCacheWriterAdapter<K, V>(cacheWriter, cfg.getKeyType(), cfg.getValueType()));

        final Iterable<CacheEntryListenerConfiguration<K, V>> cacheEntryListenerConfigurations = cfg.getInitialCacheEntryListenerConfigurations();
        if(cacheEntryListenerConfigurations != null) {
            for (CacheEntryListenerConfiguration<K, V> listenerCfg : cacheEntryListenerConfigurations) {
                registerCacheEntryListener(listenerCfg);
            }
        }

    }


    @Override
    public V get(final K key) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        final Element element = getElement(key);
        if (element == null) {
            V value = null;
            if(cfg.isReadThrough()) {
                value = load(key);
            }
            return value;
        }
        return cfg.getValueType().cast(element.getObjectValue());
    }

    V load(K key) {
        V value;
        ehcache.acquireWriteLockOnKey(key);
        final Element e = ehcache.get(key);
        if(e != null) {
            return (V)e.getObjectValue();
        }
        try {
            try {
                value = cacheLoader.load(key);
            } catch (Exception ex) {
                throw new CacheLoaderException(ex);
            }
            if(value != null) {
                putWithoutWriter(key, value);
            }
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
        return value;
    }

    private Element getElement(final K key) {
        final Element element = ehcache.get(key);
        if (element == null)
            return null;
        final Duration expiryForUpdate = cfg.getExpiryPolicy().getExpiryForAccess();
        if(expiryForUpdate != null && expiryForUpdate.isZero()) {
            ehcache.removeElement(element);
        }
        return element;
    }

    @Override
    public Map<K, V> getAll(final Set<? extends K> keys) {
        checkNotClosed();
        for (K key : keys) {
            if(key == null) throw new NullPointerException();
        }
        final Map<K, V> result = new HashMap<K, V>();
        final Map<Object, Element> all = ehcache.getAll(keys);
        for (Map.Entry<Object, Element> entry : all.entrySet()) {
            final Element e = entry.getValue();
            final K key = (K)entry.getKey();
            if(key != null) {
                V value = null;
                if(e != null) {
                    value = (V)e.getObjectValue();
                } else if (cfg.isReadThrough()) {
                    value = load(key);
                }
                if (value != null) {
                    result.put(key, value);
                }
            }
        }
        return result;
    }

    @Override
    public boolean containsKey(final K key) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        return ehcache.isKeyInCache(key);
    }

    @Override
    public void loadAll(final Set<? extends K> keys, final boolean replaceExistingValues, final CompletionListener completionListener) {
        checkNotClosed();
        if(keys == null) {
            throw new NullPointerException();
        }
        for (K key : keys) {
            if (key == null) throw new NullPointerException();
        }
        if(cacheLoader == null) {
            if (completionListener != null) {
                completionListener.onCompletion();
            }
            return;
        }
        cacheManager.getExecutorService().submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (K key : keys) {
                    try {
                        ehcache.acquireWriteLockOnKey(key);
                        try {
                            if (!ehcache.isKeyInCache(key) || replaceExistingValues) {
                                final V value = cacheLoader.load(key);
                                if (value != null) {
                                    JCache.this.putWithoutWriter(key, value);
                                }
                            }
                        } finally {
                            ehcache.releaseWriteLockOnKey(key);
                        }
                    } catch (Exception e) {
                        if (completionListener != null) {
                            completionListener.onException(new CacheLoaderException(e));
                        }
                        return null;
                    }
                }
                if (completionListener != null) {
                    completionListener.onCompletion();
                }
                return null;
            }
        });
    }

    @Override
    public void put(final K key, final V value) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        if(value == null) throw new NullPointerException();
        ehcache.acquireWriteLockOnKey(key);
        try {
            final Duration expiry;
            final boolean inCache = ehcache.isKeyInCache(key);
            if(inCache) {
                expiry = cfg.getExpiryPolicy().getExpiryForUpdate();
            } else {
                expiry = cfg.getExpiryPolicy().getExpiryForCreation();
            }
            final Element element = new Element(key, value);
            if(setTimeTo(cfg.overrideDefaultExpiry(), expiry, element)) {
                putAndWriteIfNeeded(element);
            } else if(inCache) {
                removeAndWriteIfNeeded(key);
            }
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
    }

    void putWithoutWriter(final K key, final V value) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        if(value == null) throw new NullPointerException();
        ehcache.acquireWriteLockOnKey(key);
        try {
            final Duration expiry;
            final boolean inCache = ehcache.isKeyInCache(key);
            if(inCache) {
                expiry = cfg.getExpiryPolicy().getExpiryForUpdate();
            } else {
                expiry = cfg.getExpiryPolicy().getExpiryForCreation();
            }
            final Element element = new Element(key, value);
            if(setTimeTo(cfg.overrideDefaultExpiry(), expiry, element)) {
                ehcache.put(element);
            } else if(inCache) {
                ehcache.remove(key);
            }
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
    }

    private boolean setTimeTo(final boolean overrideDefaults, final Duration duration, final Element element) {

        if(!overrideDefaults) {
            return true;
        }

        if (duration != null) {
            if (duration.isZero()) {
                return false;
            }
            if (duration.isEternal()) {
                element.setEternal(true);
            } else {
                final int d = (int)TimeUnit.SECONDS.convert(duration.getDurationAmount(), duration.getTimeUnit());
                element.setTimeToLive(d == 0 ? 1 : d);
                element.setTimeToIdle(d);
            }
        }
        return true;
    }

    @Override
    public V getAndPut(final K key, final V value) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        if(value == null) throw new NullPointerException();
        ehcache.acquireWriteLockOnKey(key);
        try {
            final Element previousElement = ehcache.get(key);
            final Element element = new Element(key, value);
            final Duration expiry;
            final boolean inCache = ehcache.isKeyInCache(key);
            if(inCache) {
                expiry = cfg.getExpiryPolicy().getExpiryForUpdate();
            } else {
                expiry = cfg.getExpiryPolicy().getExpiryForCreation();
            }
            if(setTimeTo(cfg.overrideDefaultExpiry(), expiry, element)) {
                putAndWriteIfNeeded(element);
            } else if(inCache) {
                removeAndWriteIfNeeded(key);
            }

            return previousElement == null ? null : (V) previousElement.getObjectValue();
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
    }

    private boolean removeAndWriteIfNeeded(final K key) {
        if (cfg.isWriteThrough()) {
            ehcache.acquireWriteLockOnKey(key);
            final Element previous = ehcache.getQuiet(key);
            try {
                return ehcache.removeWithWriter(key);
            } catch (RuntimeException e) {
                if(previous != null) {
                    ehcache.putQuiet(previous);
                }
                throw new CacheWriterException(e);
            }
        } else {
            if (ehcache.isKeyInCache(key)) {
                return ehcache.remove(key);
            }
            return false;
        }
    }

    private void putAndWriteIfNeeded(final Element element) {
        if (cfg.isWriteThrough()) {
            try {
                ehcache.putWithWriter(element);
            } catch (RuntimeException e) {
                ehcache.removeElement(element);
                throw new CacheWriterException(e);
            }
        } else {
            ehcache.put(element);
        }
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        checkNotClosed();
        final Collection<Element> elements = new HashSet<Element>(map.size(), 1f);
        final Collection<Entry> entries = new HashSet<Entry>();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            if(entry.getValue() == null) throw new NullPointerException();
            final Element e = new Element(entry.getKey(), entry.getValue());
            final Duration expiry;
            if(ehcache.isKeyInCache(entry.getKey())) {
                expiry = cfg.getExpiryPolicy().getExpiryForUpdate();
            } else {
                expiry = cfg.getExpiryPolicy().getExpiryForCreation();
            }
            if(setTimeTo(cfg.overrideDefaultExpiry(), expiry, e)) {
                elements.add(e);
                if (cfg.isWriteThrough()) {
                    entries.add(new JCacheEntry(e, cfg.getKeyType(), cfg.getValueType()));
                }
            } else {
                ehcache.remove(entry.getKey());
            }
        }
        for (Element element : elements) {
            ehcache.put(element);
        }
        if (cfg.isWriteThrough()) {
            try {
                try {
                    cacheWriter.writeAll(entries);
                } catch (RuntimeException e) {
                    throw new CacheWriterException(e);
                }
            } catch (Exception e) {
                for (Entry entry : entries) {
                    ehcache.remove(entry.getKey());
                }
                throw new CacheWriterException(e);
            }
        }
    }

    @Override
    public boolean putIfAbsent(final K key, final V value) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        if(value == null) throw new NullPointerException();
        ehcache.acquireWriteLockOnKey(key);
        try {
            if (!ehcache.isKeyInCache(key)) {
                final Element element = new Element(key, value);
                final Duration expiryForCreation;
                expiryForCreation = cfg.getExpiryPolicy().getExpiryForCreation();
                if(setTimeTo(cfg.overrideDefaultExpiry(), expiryForCreation, element)) {
                    putAndWriteIfNeeded(element);
                }
                return true;
            }
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
        return false;
    }

    @Override
    public boolean remove(final K key) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        return removeAndWriteIfNeeded(key);
    }

    @Override
    public boolean remove(final K key, final V oldValue) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        if(oldValue == null) throw new NullPointerException();
        ehcache.acquireWriteLockOnKey(key);
        try {
            if(ehcache.isKeyInCache(key)) {
                final Element e = ehcache.get(key);
                if(e != null && e.getObjectValue().equals(oldValue)) {
                    removeAndWriteIfNeeded(key);
                    return true;
                } else if (e != null) {
                    final Duration expiryForAccess = cfg.getExpiryPolicy().getExpiryForAccess();
                    if(expiryForAccess != null && expiryForAccess.isZero()) {
                        removeAndWriteIfNeeded(key);
                    }
                }
            } else {
                ehcache.get(NOT_THERE);
            }
            return false;
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
    }

    @Override
    public V getAndRemove(final K key) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        ehcache.acquireWriteLockOnKey(key);
        try {
            Element previousElement;
            previousElement = ehcache.get(key);
            removeAndWriteIfNeeded(key);
            return previousElement == null ? null : (V) previousElement.getObjectValue();
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        if(oldValue == null) throw new NullPointerException();
        if(newValue == null) throw new NullPointerException();
        final Element element = new Element(key, newValue);
        ehcache.acquireWriteLockOnKey(key);
        try {
            final Element current = ehcache.get(key);
            if(current != null) {
                if(!current.getObjectValue().equals(oldValue)) {
                    final Duration expiryForAccess = cfg.getExpiryPolicy().getExpiryForAccess();
                    if(expiryForAccess != null && expiryForAccess.isZero()) {
                        ehcache.remove(key);
                    }
                } else {
                    final Duration expiry = cfg.getExpiryPolicy().getExpiryForUpdate();
                    if(setTimeTo(cfg.overrideDefaultExpiry(), expiry, element)) {
                        ehcache.put(new Element(key, newValue));
                        return true;
                    } else {
                        ehcache.remove(key);
                    }
                }
            }
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
        return false;
    }

    @Override
    public boolean replace(final K key, final V value) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        if(value == null) throw new NullPointerException();
        ehcache.acquireWriteLockOnKey(key);
        try {
            final Element element = new Element(key, value);
            final Duration expiry;
            final boolean inCache = ehcache.isKeyInCache(key);
            if(inCache) {
                ehcache.get(key);
                expiry = cfg.getExpiryPolicy().getExpiryForUpdate();
                if (setTimeTo(cfg.overrideDefaultExpiry(), expiry, element)) {
                    putAndWriteIfNeeded(element);
                    return true;
                } else {
                    removeAndWriteIfNeeded(key);
                }
                return true;
            }
            ehcache.get(NOT_THERE);
            return false;
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
    }

    @Override
    public V getAndReplace(final K key, final V value) {
        checkNotClosed();
        if(key == null) throw new NullPointerException();
        if(value == null) throw new NullPointerException();
        ehcache.acquireWriteLockOnKey(key);
        try {
            Duration expiry;
            final Element previous = ehcache.get(key);
            if(previous != null) {
                expiry = cfg.getExpiryPolicy().getExpiryForUpdate();
                final Element element = new Element(key, value);
                if (setTimeTo(cfg.overrideDefaultExpiry(), expiry, element)) {
                    putAndWriteIfNeeded(element);
                    return (V)previous.getObjectValue();
                }
            }
            return null;
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
    }

    @Override
    public void removeAll(final Set<? extends K> keys) {
        checkNotClosed();
        if(keys == null) throw new NullPointerException();
        for (K key : keys) {
            if(key == null)
                throw new NullPointerException();
        }
        checkNotClosed();
        if(cfg.isWriteThrough()) {
            for (K key : keys) {
                ehcache.acquireWriteLockOnKey(key);
                try {
                    final Element previous = ehcache.getQuiet(key);
                    try {
                        ehcache.removeWithWriter(key);
                    } catch (RuntimeException e) {
                        if(previous != null) {
                            ehcache.putQuiet(previous);
                        }
                        throw new CacheWriterException(e);
                    }
                } finally {
                    ehcache.releaseWriteLockOnKey(key);
                }
            }
        } else {
            if (cfg.isStatisticsEnabled()) {
                for (K key : keys) {
                    if (ehcache.isKeyInCache(key)) {
                        ehcache.remove(key);
                    }
                }
            } else {
                ehcache.removeAll(keys);
            }
        }
    }

    @Override
    public void removeAll() {
        checkNotClosed();
        if (cfg.isWriteThrough()) {
            for (Object key : ehcache.getKeys()) {
                ehcache.acquireWriteLockOnKey(key);
                final Element previous = ehcache.getQuiet(key);
                try {
                    try {
                        ehcache.removeWithWriter(key);
                    } catch (RuntimeException e) {
                        if(previous != null) {
                            ehcache.putQuiet(previous);
                        }
                        throw new CacheWriterException(e);
                    }
                } finally {
                    ehcache.releaseWriteLockOnKey(key);
                }
            }
        } else {
            if (cfg.isStatisticsEnabled()) {
                for (Object o : ehcache.getKeys()) {
                    final Element element = ehcache.getQuiet(o);
                    if(element != null) {
                        if(element.getTimeToLive() == 1 && element.getTimeToIdle() == 0) {
                            ehcache.removeQuiet(element.getObjectKey());
                        } else {
                            ehcache.remove(element.getObjectKey());
                        }
                    }
                }
            } else {
                ehcache.removeAll();
            }
        }
    }

    @Override
    public void clear() {
        checkNotClosed();
        ehcache.removeAll();
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(final Class<C> clazz) {
        if(clazz.isAssignableFrom(cfg.getClass())) {
            return clazz.cast(cfg);
        }
        return null;
    }

    @Override
    public <T> T invoke(final K key, final EntryProcessor<K, V, T> entryProcessor, final Object... arguments) throws EntryProcessorException {
        checkNotClosed();
        if(key == null) {
            throw new NullPointerException();
        }
        if(entryProcessor == null) {
            throw new NullPointerException();
        }
        final T outcome;
        ehcache.acquireWriteLockOnKey(key);
        try {
            Element element = ehcache.get(key);
            try {
                boolean fromLoader = false;
                if(element == null) {
                    if(cfg.isReadThrough() && load(key) != null) {
                        element = ehcache.get(key);
                        fromLoader = true;
                    }
                }
                final JMutableEntry<K, V> entry = new JMutableEntry<K, V>(this, element, key, fromLoader);
                outcome = entryProcessor.process(entry, arguments);
                entry.apply(this);
            } catch (RuntimeException t) {
                if(t instanceof CacheException) {
                    throw t;
                }
                throw new EntryProcessorException(t);
            }
        } finally {
            ehcache.releaseWriteLockOnKey(key);
        }
        return outcome;
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(final Set<? extends K> keys, final EntryProcessor<K, V, T> entryProcessor, final Object... arguments) {
        checkNotClosed();
        if(entryProcessor == null) {
            throw new NullPointerException();
        }
        final Map<K, EntryProcessorResult<T>> results = new HashMap<K, EntryProcessorResult<T>>();
        for (K key : keys) {
            final T result = invoke(key, entryProcessor, arguments);
            if(result != null) {
                results.put(key, new EntryProcessorResult<T>() {
                    @Override
                    public T get() throws EntryProcessorException {
                        return result;
                    }
                });
            }
        }
        return results;
    }

    @Override
    public String getName() {
        return ehcache.getName();
    }

    @Override
    public CacheManager getCacheManager() {
        checkNotClosed();
        return cacheManager;
    }

    @Override
    public void close() {
        cacheManager.shutdown(this);
    }

    void shutdown() {
        closed = true;
        ehcache.dispose();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        if(clazz.isAssignableFrom(ehcache.getClass())) {
            return clazz.cast(ehcache);
        }
        if(clazz.isAssignableFrom(getClass())) {
            return clazz.cast(this);
        }
        return null;
    }

    @Override
    public void registerCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        final Factory<CacheEntryListener<? super K, ? super V>> factory = cacheEntryListenerConfiguration.getCacheEntryListenerFactory();
        final CacheEntryListener cacheEntryListener = factory.create();
        final JCacheListenerAdapter<K, V> cacheEventListener = new JCacheListenerAdapter<K, V>(cacheEntryListener, this, cacheEntryListenerConfiguration);
        if(cfg.addCacheEntryListenerConfiguration(cacheEntryListenerConfiguration, cacheEventListener)) {
            ehcache.getCacheEventNotificationService().registerListener(cacheEventListener);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void deregisterCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        JCacheListenerAdapter<K, V> adapter = cfg.removeCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
        if(adapter != null) {
            ehcache.getCacheEventNotificationService().unregisterListener(adapter);
        }
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        checkNotClosed();
        return new JEntryIterator<K, V>(this);
    }

    private void checkNotClosed() {
        if(closed) throw new IllegalStateException();
    }

    private static class JEntryIterator<K, V> implements Iterator<Entry<K, V>> {
        private final Iterator<K> keyIterator;
        private final JCache<K, V> jCache;
        private Entry<K, V> next;
        private Entry<K, V> current;

        public JEntryIterator(final JCache jCache) {
            this.jCache = jCache;
            this.keyIterator = jCache.ehcache.getKeys().iterator();
            advance();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Entry<K, V> next() {
            if(next == null) {
                throw new NoSuchElementException();
            }
            current = next;
            final Duration expiryForAccess = jCache.cfg.getExpiryPolicy().getExpiryForAccess();
            advance();
            if(expiryForAccess != null && expiryForAccess.isZero()) {
                remove();
            }
            return current;
        }

        @Override
        public void remove() {
            if(current == null) {
                throw new IllegalStateException();
            }
            jCache.ehcache.acquireWriteLockOnKey(current.getKey());
            try {
                final Element element = jCache.ehcache.getQuiet(current.getKey());
                if(element != null && element.getObjectValue().equals(current.getValue())) {
                    jCache.removeAndWriteIfNeeded(current.getKey());
                }
            } finally {
                jCache.ehcache.releaseWriteLockOnKey(current.getKey());
            }
        }

        private void advance() {
            next = null;
            while(keyIterator.hasNext() && next == null) {
                Element e = jCache.getElement(keyIterator.next());
                if(e != null) {
                    next = new JCacheEntry<K, V>(e, jCache.cfg.getKeyType(), jCache.cfg.getValueType());
                }
            }
        }
    }

    private static class JMutableEntry<K, V> implements MutableEntry<K, V> {
        private final JCache<K, V> jCache;
        private final K key;
        private final boolean fromLoader;
        private final V initialValue;
        private volatile V newValue;
        private volatile boolean deleted;
        private volatile boolean skipDelete;

        public JMutableEntry(final JCache<K, V> jCache, final Element element, final K key, final boolean fromLoader) {
            this.jCache = jCache;
            this.key = key;
            this.fromLoader = fromLoader;
            if (element != null) {
                initialValue = (V)element.getObjectValue();
            } else {
                initialValue = null;
            }
            newValue = initialValue;
        }

        @Override
        public boolean exists() {
            return !deleted && newValue != null;
        }

        @Override
        public void remove() {
            skipDelete = initialValue == null && newValue != null;
            newValue = null;
            deleted = true;
        }

        @Override
        public void setValue(final V value) {
            if(value == null) {
                throw new EntryProcessorException();
            }
            deleted = false;
            newValue = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            if(newValue != initialValue) return newValue;
            if (initialValue != null && !fromLoader) {
                final Duration expiryForAccess = jCache.cfg.getExpiryPolicy().getExpiryForAccess();
                if (expiryForAccess != null && expiryForAccess.isZero()) {
                    remove();
                }
            }
            return initialValue;
        }

        @Override
        public <T> T unwrap(final Class<T> clazz) {
            throw new UnsupportedOperationException("Implement me!");
        }

        void apply(final JCache<K, V> jCache) {
            if(deleted && !skipDelete) {
                jCache.remove(key);
            }
            if(newValue != initialValue && newValue != null) {
                jCache.put(key, newValue);
            }
        }
    }
}
