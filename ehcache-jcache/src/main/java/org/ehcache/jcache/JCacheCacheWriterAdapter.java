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

import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.writebehind.operations.SingleOperationType;

import javax.cache.Cache;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Adapt a {@link javax.cache.integration.CacheWriter} to the interface of {@link net.sf.ehcache.writer.CacheWriter}
 *
 * @param <K> the type of keys used by this JCacheCacheLoaderAdapter
 * @param <V> the type of values that are loaded by this JCacheCacheLoaderAdapter
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
class JCacheCacheWriterAdapter<K, V> implements CacheWriter {
    private final javax.cache.integration.CacheWriter<K, V> jsr107CacheWriter;
    private final Class<K> keyType;
    private final Class<V> valueType;

    /**
     * Construct a JCacheCacheWriterAdapter to adapt the {@code jsr107CacheWriter} to the
     * {@link CacheWriter} interface
     *
     * @param jsr107CacheWriter the {@link CacheWriter to adapt}
     */
    public JCacheCacheWriterAdapter(javax.cache.integration.CacheWriter<K, V> jsr107CacheWriter, Class<K> keyType, Class<V> valueType) {
        this.jsr107CacheWriter = jsr107CacheWriter;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    /**
     * {@inheritDoc}
     *
     * Creates a clone of this writer. This method will only be called by ehcache before a
     * cache is initialized.
     * <p/>
     * Implementations should throw CloneNotSupportedException if they do not support clone
     * but that will stop them from being used with defaultCache.
     */
    public CacheWriter clone(Ehcache cache) throws CloneNotSupportedException {
        return (JCacheCacheWriterAdapter) super.clone();
    }

    /**
     * Notifies writer to initialise themselves.
     * <p/>
     * This method is called during the Cache's initialise method after it has changed it's
     * status to alive. Cache operations are legal in this method. If you register a cache writer
     * manually after a cache has been initialised already, this method will be called on the
     * cache writer as soon as it has been registered.
     * <p/>
     * Note that if you reuse cache writer instances or create a factory that returns the
     * same cache writer instance as a singleton, your {@code init} method should be able
     * to handle that situation. Unless you perform this multiple usage of a cache writer yourself,
     * Ehcache will not do this though. So in the majority of the use cases, you don't need to do
     * anything special.
     *
     * @throws net.sf.ehcache.CacheException
     */
    public void init() {

    }

    /**
     * Providers may be doing all sorts of exotic things and need to be able to clean up on
     * dispose.
     * <p/>
     * Cache operations are illegal when this method is called. The cache itself is partly
     * disposed when this method is called.
     *
     * @throws net.sf.ehcache.CacheException if any.
     */
    public void dispose() throws CacheException {

    }

    /**
     * {@inheritDoc}
     *
     * Write the specified value under the specified key to the underlying store.
     * This method is intended to support both key/value creation and value update for a specific key.
     */
    public void write(Element element) throws CacheException {
        jsr107CacheWriter.write(new JCacheEntry<K, V>(element, keyType, valueType));
    }

    /**
     * {@inheritDoc}
     *
     * Write the specified Elements to the underlying store. This method is intended to support both insert and update.
     * If this operation fails (by throwing an exception) after a partial success,
     * the convention is that entries which have been written successfully are to be removed from the specified mapEntries,
     * indicating that the write operation for the entries left in the map has failed or has not been attempted.
     */
    public void writeAll(Collection<Element> elements) throws CacheException {
        Collection<Cache.Entry<? extends K,? extends V>> javaxCacheEntries = new HashSet<Cache.Entry<? extends K, ? extends V>>();
        for (Element e : elements) {
            javaxCacheEntries.add(new JCacheEntry<K, V>(e, keyType, valueType));
        }
        jsr107CacheWriter.writeAll(javaxCacheEntries);
    }

    /**
     * {@inheritDoc}
     *
     * Delete the cache entry from the store
     */
    public void delete(CacheEntry entry) throws CacheException {
        jsr107CacheWriter.delete(entry.getKey());
    }

    /**
     * {@inheritDoc}
     *
     * Remove data and keys from the underlying store for the given collection of keys, if present. If this operation fails
     * (by throwing an exception) after a partial success, the convention is that keys which have been erased successfully
     * are to be removed from the specified keys, indicating that the erase operation for the keys left in the collection
     * has failed or has not been attempted.
     */
    public void deleteAll(Collection<CacheEntry> entries) throws CacheException {
        Set<javax.cache.Cache.Entry<K, V>> javaxCacheEntries = new HashSet<javax.cache.Cache.Entry<K, V>>();
        for (CacheEntry e : entries) {
            javaxCacheEntries.add(new JCacheEntry<K, V>(e.getElement(), keyType, valueType));
        }
        jsr107CacheWriter.deleteAll(javaxCacheEntries);
    }

    /**
     * {@inheritDoc}
     *
     * This method will be called, whenever an Element couldn't be handled by the writer and all
     * the {@link net.sf.ehcache.config.CacheWriterConfiguration#getRetryAttempts() retryAttempts} have been tried.
     * <p>When batching is enabled all the elements in the failing batch will be passed to this methods
     * <p>Try to not throw RuntimeExceptions from this method. Should an Exception occur, it will be logged, but
     * the element will be lost anyways.
     */
    @Override
    public void throwAway(Element element, SingleOperationType operationType, RuntimeException e) {

    }
}
