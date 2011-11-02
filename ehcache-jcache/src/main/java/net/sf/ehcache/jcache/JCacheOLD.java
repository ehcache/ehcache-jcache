///**
// *  Copyright 2003-2008 Luck Consulting Pty Ltd
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
package net.sf.ehcache.jcache;
//
//import net.sf.ehcache.Ehcache;
//import net.sf.ehcache.Element;
//import net.sf.ehcache.event.CacheEventListener;
//import net.sf.ehcache.jcache.loader.JCacheLoader;
//import net.sf.ehcache.loader.CacheLoader;
//
//import javax.cache.Cache;
//import javax.cache.CacheConfiguration;
//import javax.cache.CacheEntry;
//import javax.cache.CacheException;
//import javax.cache.CacheListener;
//import javax.cache.CacheManager;
//import javax.cache.CacheStatistics;
//import javax.cache.Status;
//import javax.cache.event.CacheEntryListener;
//import javax.cache.event.NotificationScope;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Warning: This class is related to the JSR107 specification, which is in draft. It is subject to change without notice.
// * <p/>
// * A cache implementation that matches the draft JCACHE specification.
// * <p/>
// * WARNING: The JCache specification is in draft and this API will change up until the time that JCACHE is finalised.
// * <p/>
// * It is not possible for one class to implement both JCACHE and Ehcache
// * in the same class due to conflicts with method signatures on get and remove.
// * <p/>
// * This implementation is an adaptor to Ehcache, and will exhibit the same underlying characteristics as Ehcache. Additional features
// * have been added to Ehcache to match the JCache features. All of these features can be configured in ehcache.xml.
// * <p/>
// * The current JCache CacheManager class, available in the draft, is considered unworkable. Instead, use the Ehcache <code>cacheManager.getJCache(String name)</code>
// * to get a JCache. The JCache CacheManager is unlikely to make it into the final spec.
// * <p/>
// * The recommended creational pattern for JCache is one of:
// * <ol>
// * <li>Add a cache to ehcache.xml and use <code>cacheManager.getJCache(String name)</code> to access it.
// * <li>Create a JCache from an Ehcache, using one of the constructors in this class. Two types of CacheLoader can be specified, in which
// * case the underlying Ehcache loader is replaced by the one specified.
// * <li>Create an Ehcache from a Cache using its constructor, then a JCache, and then add it to the ehcache CacheManager using <code>cacheManager.addCache(JCache jCache)</code>
// * </ol>
// *
// * @author Greg Luck
// * @version $Id: JCache.java 796 2008-10-09 02:39:03Z gregluck $
// * @since 1.3
// */
public class JCacheOLD<K,V> /*implements javax.cache.Cache<K,V>*/ {
//
//    private static final Logger LOG = LoggerFactory.getLogger(JCache.class);
//
//    /**
//     * An Ehcache backing instance
//     */
//    private Ehcache cache;
//
//
//    /**
//     * A constructor for JCache.
//     * <p/>
//     * JCache is an adaptor for an Ehcache, and therefore requires an Ehcache in its constructor.
//     * <p/>
//     *
//     * @param cache An ehcache
//     * @see "class description for recommended usage"
//     * @since 1.4
//     */
//    public JCacheOLD(Ehcache cache) {
//        this.cache = cache;
//    }
//
//    /**
//     * A constructor for JCache.
//     * <p/>
//     * JCache is an adaptor for an Ehcache, and therefore requires an Ehcache in its constructor.
//     * <p/>
//     *
//     * @param cache       An ehcache
//     * @param cacheLoader used to load entries when they are not in the cache. If this is null, it is
//     *                    set for the cache. If null, the JCache will inherit the CacheLoader set for the backing ehcache.
//     *                    If specified, the underlying ehcache will have it's loader(s) replaced.
//     * @see "class description for recommended usage"
//     * @since 1.3
//     */
//    public JCacheOLD(Ehcache cache, javax.cache.CacheLoader cacheLoader) {
//        this.cache = cache;
//        setCacheLoaderNullCheck((JCacheLoader) cacheLoader);
//    }
//
//    /**
//     * A constructor for JCache.
//     * <p/>
//     * JCache is an adaptor for an Ehcache, and therefore requires an Ehcache in its constructor.
//     * <p/>
//     *
//     * @param cache       An ehcache
//     * @param cacheLoader used to load entries when they are not in the cache. If this is null, it is
//     *                    set for the cache. If null, the JCache will inherit the CacheLoader set for the backing ehcache.
//     *                    If specified, the underlying ehcache will have it's loader replaced.
//     * @see "class description for recommended usage"
//     * @since 1.3
//     */
//    public JCacheOLD(Ehcache cache, JCacheLoader cacheLoader) {
//        this.cache = cache;
//        setCacheLoaderNullCheck(cacheLoader);
//    }
//
//    /**
//     * Setter for the CacheLoader. Changing the CacheLoader takes immediate effect.
//     *
//     * @param cacheLoader the loader to dynamically load new cache entries. This replaces the CacheLoader in the underlying ehcache.
//     */
//    public void setCacheLoader(JCacheLoader cacheLoader) {
//        setCacheLoaderNullCheck(cacheLoader);
//    }
//
//
//    private void setCacheLoaderNullCheck(JCacheLoader cacheLoader) {
//        if (cacheLoader != null) {
//            List<CacheLoader> loaders = new ArrayList<CacheLoader>(cache.getRegisteredCacheLoaders());
//            for (CacheLoader loader : loaders) {
//                cache.unregisterCacheLoader(loader);
//            }
//            cache.registerCacheLoader(cacheLoader);
//        }
//    }
//
//    /**
//     * Add a listener to the list of cache listeners. The behaviour of JCACHE and Ehcache listeners is a little
//     * different. See {@link JCacheListenerAdaptor} for details on how each event is adapted.
//     *
//     * @param cacheListener a JCACHE CacheListener
//     * @see JCacheListenerAdaptor
//     */
//    public void addListener(CacheListener cacheListener) {
//        JCacheListenerAdaptor cacheListenerAdaptor = new JCacheListenerAdaptor(cacheListener);
//        cache.getCacheEventNotificationService().registerListener(cacheListenerAdaptor);
//    }
//
//    /**
//     * The evict method will remove objects from the cache that are no longer valid.
//     * Objects where the specified expiration time has been reached.
//     * <p/>
//     * This implementation synchronously checks each store for expired elements. Note that the DiskStore
//     * has an expiryThread that runs periodically to do the same thing, and that the MemoryStore lazily checks
//     * for expiry on overflow and peek, thus reducing the utility of calling this method.
//     */
//    public void evict() {
//        cache.evictExpiredElements();
//    }
//
//    /**
//     * The getAll method will return, from the cache, a Map of the objects associated with the Collection of keys in argument "keys".
//     * If the objects are not in the cache, the associated cache loader will be called. If no loader is associated with an object,
//     * a null is returned. If a problem is encountered during the retrieving or loading of the objects, an exception will be thrown.
//     * If the "arg" argument is set, the arg object will be passed to the CacheLoader.loadAll method. The cache will not dereference
//     * the object. If no "arg" value is provided a null will be passed to the loadAll method. The storing of null values in the cache
//     * is permitted, however, the get method will not distinguish returning a null stored in the cache and not finding the object in
//     * the cache. In both cases a null is returned.
//     * <p/>
//     * The Ehcache native API provides similar functionality to loaders using the
//     * decorator <code>net.sf.ehcache.constructs.blocking.SelfPopulatingCache</code>
//     * <p/>
//     * Note. If the getAll exceeds the maximum cache
//     * size, the returned map will necessarily be less than the number specified.
//     *
//     * @param keys the keys to load
//     * @return a Map populated from the Cache. If there are no elements, an empty Map is returned.
//     */
//    public Map getAll(Collection keys) throws CacheException {
//        return getAll(keys, null);
//    }
//
//    /**
//     * The getAll method will return, from the cache, a Map of the objects associated with the Collection of keys in argument "keys".
//     * If the objects are not in the cache, the associated cache loader will be called. If no loader is associated with an object,
//     * a null is returned. If a problem is encountered during the retrieving or loading of the objects, an exception will be thrown.
//     * If the "arg" argument is set, the arg object will be passed to the CacheLoader.loadAll method. The cache will not dereference
//     * the object. If no "arg" value is provided a null will be passed to the loadAll method. The storing of null values in the cache
//     * is permitted, however, the get method will not distinguish returning a null stored in the cache and not finding the object in
//     * the cache. In both cases a null is returned.
//     * <p/>
//     * This version takes a loader argument, which is essentially an easy place to pass the loader some extra information
//     * at runtime.
//     * <p/>
//     * The Ehcache native API provides similar functionality to loaders using the
//     * decorator <code>net.sf.ehcache.constructs.blocking.SelfPopulatingCache</code>
//     * <p/>
//     * Note. If the getAll exceeds the maximum cache
//     * size, the returned map will necessarily be less than the number specified.
//     *
//     * @param keys           the keys to load
//     * @param loaderArgument this can be anything at all. It only needs to make sense to the loader
//     * @return a Map populated from the Cache. If there are no elements, an empty Map is returned.
//     */
//    public Map getAll(Collection keys, Object loaderArgument) throws CacheException {
//        return cache.getAllWithLoader(keys, loaderArgument);
//    }
//
//    /**
//     * Gets the first of the CacheLoaders registered in this cache.
//     * Ehcache allows for a chain of cache loaders, JCache only one.
//     *
//     * @return the loader, or null if there is none
//     */
//    public CacheLoader getCacheLoader() {
//        List<CacheLoader> loaders = cache.getRegisteredCacheLoaders();
//        if (loaders.size() >= 1) {
//            return cache.getRegisteredCacheLoaders().get(0);
//        } else {
//            return null;
//        }
//    }
//
//
//    /**
//     * returns the CacheEntry object associated with the key.
//     *
//     * @param key the key to look for in the cache
//     */
//    public CacheEntry getCacheEntry(Object key) {
//        Element element = cache.get(key);
//        if (element != null) {
//            return new net.sf.ehcache.jcache.JCacheEntry(element);
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * Gets an immutable Statistics object representing the Cache statistics at the time. How the statistics are calculated
//     * depends on the statistics accuracy setting. The only aspect of statistics sensitive to the accuracy setting is
//     * object size. How that is calculated is discussed below.
//     * <h3>Best Effort Size</h3>
//     * This result is returned when the statistics accuracy setting is {@link CacheStatistics#STATISTICS_ACCURACY_BEST_EFFORT}.
//     * <p/>
//     * The size is the number of {@link Element}s in the {@link net.sf.ehcache.store.MemoryStore} plus
//     * the number of {@link Element}s in the {@link net.sf.ehcache.store.DiskStore}.
//     * <p/>
//     * This number is the actual number of elements, including expired elements that have
//     * not been removed. Any duplicates between stores are accounted for.
//     * <p/>
//     * Expired elements are removed from the the memory store when
//     * getting an expired element, or when attempting to spool an expired element to
//     * disk.
//     * <p/>
//     * Expired elements are removed from the disk store when getting an expired element,
//     * or when the expiry thread runs, which is once every five minutes.
//     * <p/>
//     * <h3>Guaranteed Accuracy Size</h3>
//     * This result is returned when the statistics accuracy setting is {@link CacheStatistics#STATISTICS_ACCURACY_GUARANTEED}.
//     * <p/>
//     * This method accounts for elements which might be expired or duplicated between stores. It take approximately
//     * 200ms per 1000 elements to execute.
//     * <h3>Fast but non-accurate Size</h3>
//     * This result is returned when the statistics accuracy setting is {@link CacheStatistics#STATISTICS_ACCURACY_NONE}.
//     * <p/>
//     * The number given may contain expired elements. In addition if the DiskStore is used it may contain some double
//     * counting of elements. It takes 6ms for 1000 elements to execute. Time to execute is O(log n). 50,000 elements take
//     * 36ms.
//     *
//     * @return the number of elements in the ehcache, with a varying degree of accuracy, depending on accuracy setting.
//     * @throws IllegalStateException if the cache is not {@link net.sf.ehcache.Status#STATUS_ALIVE}
//     */
//    public CacheStatistics getCacheStatistics() throws IllegalStateException {
//        return new JCacheStatisticsOLD(cache.getStatistics());
//    }
//
//    /**
//     * The load method provides a means to "pre load" the cache. This method will, asynchronously, load the specified
//     * object into the cache using the associated cacheloader. If the object already exists in the cache, no action is
//     * taken. If no loader is associated with the object, no object will be loaded into the cache. If a problem is
//     * encountered during the retrieving or loading of the object, an exception should be logged. If the "arg" argument
//     * is set, the arg object will be passed to the CacheLoader.load method. The cache will not dereference the object.
//     * If no "arg" value is provided a null will be passed to the load method. The storing of null values in the cache
//     * is permitted, however, the get method will not distinguish returning a null stored in the cache and not finding
//     * the object in the cache. In both cases a null is returned.
//     * <p/>
//     * The Ehcache native API provides similar functionality to loaders using the
//     * decorator <code>net.sf.ehcache.constructs.blocking.SelfPopulatingCache</code>
//     *
//     * @param key key whose associated value to be loaded using the associated cacheloader if this cache doesn't contain it.
//     */
//    public void load(final Object key) throws CacheException {
//        cache.load(key);
//    }
//
//    /**
//     * The loadAll method provides a means to "pre load" objects into the cache. This method will, asynchronously, load
//     * the specified objects into the cache using the associated cache loader. If the an object already exists in the
//     * cache, no action is taken. If no loader is associated with the object, no object will be loaded into the cache.
//     * If a problem is encountered during the retrieving or loading of the objects, an exception (to be defined)
//     * should be logged. The getAll method will return, from the cache, a Map of the objects associated with the
//     * Collection of keys in argument "keys". If the objects are not in the cache, the associated cache loader will be
//     * called. If no loader is associated with an object, a null is returned. If a problem is encountered during the
//     * retrieving or loading of the objects, an exception (to be defined) will be thrown. If the "arg" argument is set,
//     * the arg object will be passed to the CacheLoader.loadAll method. The cache will not dereference the object.
//     * If no "arg" value is provided a null will be passed to the loadAll method.
//     * <p/>
//     * keys - collection of the keys whose associated values to be loaded into this cache by using the associated
//     * cacheloader if this cache doesn't contain them.
//     * <p/>
//     * The Ehcache native API provides similar functionality to loaders using the
//     * decorator <code>net.sf.ehcache.constructs.blocking.SelfPopulatingCache</code>
//     */
//    public void loadAll(final Collection keys) throws CacheException {
//        loadAll(keys, null);
//    }
//
//    /**
//     * The loadAll method provides a means to "pre load" objects into the cache. This method will, asynchronously, load
//     * the specified objects into the cache using the associated cache loader. If the an object already exists in the
//     * cache, no action is taken. If no loader is associated with the object, no object will be loaded into the cache.
//     * If a problem is encountered during the retrieving or loading of the objects, an exception (to be defined)
//     * should be logged. The getAll method will return, from the cache, a Map of the objects associated with the
//     * Collection of keys in argument "keys". If the objects are not in the cache, the associated cache loader will be
//     * called. If no loader is associated with an object, a null is returned. If a problem is encountered during the
//     * retrieving or loading of the objects, an exception (to be defined) will be thrown. If the "arg" argument is set,
//     * the arg object will be passed to the CacheLoader.loadAll method. The cache will not dereference the object.
//     * If no "arg" value is provided a null will be passed to the loadAll method.
//     * <p/>
//     * This version takes a loader argument, which is essentially an easy place to pass the loader some extra information
//     * at runtime.
//     * <p/>
//     * The Ehcache native API provides similar functionality to loaders using the
//     * decorator. See net.sf.ehcache.constructs.blocking.SelfPopulatingCache
//     *
//     * @param keys           the collection of the keys whose associated values to be loaded into this cache by using the associated
//     *                       cacheloader if this cache doesn't contain them.
//     * @param loaderArgument this can be anything at all. It only needs to make sense to the loader
//     * @throws javax.cache.CacheException
//     *          if something goes wrong with the load
//     */
//    public void loadAll(final Collection keys, final Object loaderArgument) throws CacheException {
//        cache.loadAll(keys, loaderArgument);
//    }
//
//    /**
//     * The peek method will return the object associated with "key" if it currently exists (and is valid) in the cache.
//     * If not, a null is returned. With "peek" the CacheLoader will not be invoked and other caches in the system will not be searched.
//     * <p/>
//     * In ehcache peek behaves the same way as {@link #get}
//     *
//     * @param key
//     * @return the value stored in the cache by key, or null if it does not exist
//     */
//    public Object peek(Object key) {
//        Element element = cache.get(key);
//        if (element != null) {
//            return element.getObjectValue();
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * Remove a listener from the list of cache listeners
//     *
//     * @param cacheListener a JCACHE CacheListener
//     * @see JCacheListenerAdaptor
//     */
//    public void removeListener(CacheListener cacheListener) {
//        Set<CacheEventListener> listeners = cache.getCacheEventNotificationService().getCacheEventListeners();
//        for (CacheEventListener listener : listeners) {
//            if (listener instanceof JCacheListenerAdaptor) {
//                CacheListener registeredCacheListener = ((JCacheListenerAdaptor) listener).getCacheListener();
//                if (cacheListener == registeredCacheListener) {
//                    cache.getCacheEventNotificationService().unregisterListener(listener);
//                }
//            }
//        }
//    }
//
//    /**
//     * Returns the number of key-value mappings in this map.  If the
//     * map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
//     * <tt>Integer.MAX_VALUE</tt>.
//     *
//     * @return the number of key-value mappings in this map.
//     */
//    public int size() {
//        return cache.getSize();
//    }
//
//    /**
//     * Returns <tt>true</tt> if this map contains no key-value mappings.
//     *
//     * @return <tt>true</tt> if this map contains no key-value mappings.
//     */
//    public boolean isEmpty() {
//        return (size() == 0);
//    }
//
//    /**
//     * Returns <tt>true</tt> if this map contains a mapping for the specified
//     * key.  More formally, returns <tt>true</tt> if and only if
//     * this map contains a mapping for a key <tt>k</tt> such that
//     * <tt>(key==null ? k==null : key.equals(k))</tt>.  (There can be
//     * at most one such mapping.)
//     *
//     * @param key key whose presence in this map is to be tested.
//     * @return <tt>true</tt> if this map contains a mapping for the specified
//     *         key.
//     */
//    public boolean containsKey(Object key) {
//        return cache.isKeyInCache(key);
//    }
//
//    /**
//     * Returns the {@link javax.cache.CacheStatistics} MXBean associated with the cache.
//     * May return null if the cache does not support statistics gathering.
//     *
//     * @return the CacheStatisticsMBean
//     * @throws IllegalStateException if the cache is not {@link javax.cache.Status#STARTED}
//     */
//    @Override
//    public CacheStatistics getStatistics() {
//        throw new UnsupportedOperationException("getStatistics is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Atomically associates the specified value with the specified key in this cache
//     * <p/>
//     * If the cache previously contained a mapping for
//     * the key, the old value is replaced by the specified value.  (A cache
//     * <tt>c</tt> is said to contain a mapping for a key <tt>k</tt> if and only
//     * if {@link #containsKey(Object) c.containsKey(k)} would return
//     * <tt>true</tt>.)
//     * <p/>
//     * The the previous value is returned, or null if there was no value associated
//     * with the key previously.
//     *
//     * @param key   key with which the specified value is to be associated
//     * @param value value to be associated with the specified key
//     * @return the value associated with the key at the start of the operation or null if none was associated
//     * @throws NullPointerException       if key is null or if value is null
//     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
//     * @throws javax.cache.CacheException if there is a problem doing the put
//     * @see java.util.Map#put(Object, Object)
//     * @see #put(Object, Object)
//     * @see #getAndReplace(Object, Object)
//     */
//    @Override
//    public V getAndPut(K key, V value) throws CacheException {
//        throw new UnsupportedOperationException("getAndPut is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Atomically associates the specified key with the given value if it is
//     * not already associated with a value.
//     * <p/>
//     * This is equivalent to
//     * <pre>
//     *   if (!cache.containsKey(key)) {}
//     *       cache.put(key, value);
//     *       return true;
//     *   } else {
//     *       return false;
//     *   }</pre>
//     * except that the action is performed atomically.
//     *
//     * In contrast to the corresponding ConcurrentMap operation, does not return
//     * the previous value.
//     *
//     * @param key   key with which the specified value is to be associated
//     * @param value value to be associated with the specified key
//     * @return true if a value was set.
//     * @throws NullPointerException       if key is null or value is null
//     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
//     * @throws javax.cache.CacheException if there is a problem doing the put
//     * @see java.util.concurrent.ConcurrentMap#putIfAbsent(Object, Object)
//     */
//    @Override
//    public boolean putIfAbsent(K key, V value) throws CacheException {
//        throw new UnsupportedOperationException("putIfAbsent is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Returns <tt>true</tt> if this map maps one or more keys to the
//     * specified value.  More formally, returns <tt>true</tt> if and only if
//     * this map contains at least one mapping to a value <tt>v</tt> such that
//     * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation
//     * will probably require time linear in the map size for most
//     * implementations of the <tt>Map</tt> interface.
//     * <p/>
//     * Warning: This method is extremely slow. Ehcache is designed for efficient
//     * retrieval using keys, not values.
//     *
//     * @param value value whose presence in this map is to be tested.
//     * @return <tt>true</tt> if this map maps one or more keys to the
//     *         specified value.
//     */
//    public boolean containsValue(Object value) {
//        long start = System.currentTimeMillis();
//
//        boolean inCache = cache.isValueInCache(value);
//        long end = System.currentTimeMillis();
//
//        if (LOG.isWarnEnabled()) {
//            LOG.warn("Performance Warning: containsValue is not recommended. This call took "
//                    + (end - start) + " ms");
//        }
//        return inCache;
//    }
//
//    /**
//     * The get method will return, from the cache, the object associated with
//     * the argument "key".
//     * <p/>
//     * If the object is not in the cache, the associated
//     * cache loader will be called. If no loader is associated with the object,
//     * a null is returned.
//     * <p/>
//     * If a problem is encountered during the retrieving
//     * or loading of the object, an exception (to be defined) will be thrown. (Until it is
//     * defined, the ehcache implementation throws a RuntimeException.)
//     * <p/>
//     * If the "arg" argument is set, the arg object will be passed to the
//     * CacheLoader.load method.  The cache will not dereference the object.
//     * If no "arg" value is provided a null will be passed to the load method.
//     * <p/>
//     * The storing of null values in the cache is permitted, however, the get
//     * method will not distinguish returning a null stored in the cache and
//     * not finding the object in the cache. In both cases a null is returned.
//     * <p/>
//     * Cache statistics are only updated for the initial attempt to get the cached entry.
//     *
//     * @param key key whose associated value is to be returned.
//     * @return the value to which this map maps the specified key, or
//     *         <tt>null</tt> if the map contains no mapping for this key after an attempt has been
//     *         made to load it.
//     * @throws RuntimeException JSR107 should really throw a CacheException here, but the
//     *                          spec does not allow it. Instead throw a RuntimeException if the underlying load method
//     *                          throws a CacheException.
//     * @see #containsKey(Object)
//     */
//    public V get(Object key) throws RuntimeException {
//        return get(key, V);
//    }
//
//
//    /**
//     * Same as {@link #get(Object)} except a CacheLoader argument is provided.
//     *
//     * @param key            key whose associated value is to be returned.
//     * @param loaderArgument anything at all that a CacheLoader might find useful to load
//     *                       the entry. If the loaderArgument is null, this method is the same as {@link #get(Object)}
//     * @return the value to which this map maps the specified key, or
//     *         <tt>null</tt> if the map contains no mapping for this key after an attempt has been
//     *         made to load it.
//     * @throws RuntimeException JSR107 should really throw a CacheException here, but the
//     *                          spec does not allow it. Instead throw a RuntimeException if the underlying load method
//     *                          throws a CacheException.
//     */
//    public Object get(Object key, Object loaderArgument) throws RuntimeException {
//        return get(key, null, loaderArgument);
//    }
//
//
//    /**
//     * The get method will return, from the cache, the object associated with
//     * the argument "key".
//     * <p/>
//     * If the object is not in the cache, the associated
//     * cache loader will be called. If no loader is associated with the object,
//     * a null is returned.
//     * <p/>
//     * If a problem is encountered during the retrieving
//     * or loading of the object, an exception (to be defined) will be thrown. (Until it is
//     * defined, the ehcache implementation throws a RuntimeException.)
//     * <p/>
//     * If the "argument" argument is set, the arg object will be passed to the
//     * CacheLoader.load method.  The cache will not dereference the object.
//     * If no "argument" value is provided a null will be passed to the load method.
//     * <p/>
//     * The storing of null values in the cache is permitted, however, the get
//     * method will not distinguish returning a null stored in the cache and
//     * not finding the object in the cache. In both cases a null is returned.
//     * <p/>
//     * Cache statistics are only updated for the initial attempt to get the cached entry.
//     *
//     * @param key    key whose associated value is to be returned.
//     * @param loader A specific CacheLoader to use in place of the default one.
//     * @return the value to which this map maps the specified key, or
//     *         <tt>null</tt> if the map contains no mapping for this key after an attempt has been
//     *         made to load it.
//     * @throws RuntimeException JSR107 should really throw a CacheException here, but the
//     *                          spec does not allow it. Instead throw a RuntimeException if the underlying load method
//     *                          throws a CacheException.
//     * @see #containsKey(Object)
//     */
//    public Object get(Object key, JCacheLoader loader) throws RuntimeException {
//        return get(key, loader, null);
//    }
//
//
//    /**
//     * The get method will return, from the cache, the object associated with
//     * the argument "key".
//     * <p/>
//     * If the object is not in the cache, the associated
//     * cache loader will be called. If no loader is associated with the object,
//     * a null is returned.
//     * <p/>
//     * If a problem is encountered during the retrieving
//     * or loading of the object, an exception (to be defined) will be thrown. (Until it is
//     * defined, the ehcache implementation throws a RuntimeException.)
//     * <p/>
//     * If the "loaderArgument" argument is set, it will be passed to the
//     * CacheLoader.load method.  The cache will not dereference the object.
//     * If no "loaderArgument" value is provided a null will be passed to the load method.
//     * <p/>
//     * The storing of null values in the cache is permitted, however, the get
//     * method will not distinguish returning a null stored in the cache and
//     * not finding the object in the cache. In both cases a null is returned.
//     * <p/>
//     * Cache statistics are only updated for the initial attempt to get the cached entry.
//     *
//     * @param key            key whose associated value is to be returned.
//     * @param loader         A specific CacheLoader to use in place of the default one.
//     * @param loaderArgument an Object with acts as a loaderArgument. It can contain anything that makes sense to the loader.
//     * @return the value to which this map maps the specified key, or
//     *         <tt>null</tt> if the map contains no mapping for this key after an attempt has been
//     *         made to load it.
//     * @throws RuntimeException JSR107 should really throw a CacheException here, but the
//     *                          spec does not allow it. Instead throw a RuntimeException if the underlying load method
//     *                          throws a CacheException.
//     * @see #containsKey(Object)
//     */
//    public Object get(Object key, JCacheLoader loader, Object loaderArgument) throws RuntimeException {
//        Element element = cache.getWithLoader(key, loader, loaderArgument);
//        if (element == null) {
//            return null;
//        } else {
//            return element.getObjectValue();
//        }
//    }
//
//    /**
//     * Associates the specified value with the specified key in this map
//     * (optional operation).  If the map previously contained a mapping for
//     * this key, the old value is replaced by the specified value.  (A map
//     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
//     * if {@link #containsKey(Object) m.containsKey(k)} would return
//     * <tt>true</tt>.))
//     *
//     * @param key   key with which the specified value is to be associated.
//     * @param value value to be associated with the specified key.
//     * @return previous value associated with specified key, or <tt>null</tt>
//     *         if there was no mapping for key.  A <tt>null</tt> return can
//     *         also indicate that the map previously associated <tt>null</tt>
//     *         with the specified key, if the implementation supports
//     *         <tt>null</tt> values.
//     */
//    public Object put(Object key, Object value) {
//        return put(key, value, 0);
//    }
//
//
//    /**
//     * Associates the specified value with the specified key in this map
//     * (optional operation).  If the map previously contained a mapping for
//     * this key, the old value is replaced by the specified value.  (A map
//     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
//     * if {@link #containsKey(Object) m.containsKey(k)} would return
//     * <tt>true</tt>.))
//     *
//     * @param key               key with which the specified value is to be associated.
//     * @param value             value to be associated with the specified key.
//     * @param timeToLiveSeconds the time this entry will live, overriding the default. If timeToLive
//     *                          is set to 0, the default will be applied.
//     * @return previous value associated with specified key, or <tt>null</tt>
//     *         if there was no mapping for key.  A <tt>null</tt> return can
//     *         also indicate that the map previously associated <tt>null</tt>
//     *         with the specified key, if the implementation supports
//     *         <tt>null</tt> values.
//     */
//    public Object put(Object key, Object value, int timeToLiveSeconds) {
//        Element element = null;
//        if (cache.isKeyInCache(key)) {
//            element = cache.getQuiet(key);
//        }
//        Element newElement = new Element(key, value);
//        if (timeToLiveSeconds != 0) {
//            newElement.setTimeToLive(timeToLiveSeconds);
//        }
//        cache.put(newElement);
//
//
//        if (element != null) {
//            return element.getObjectValue();
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * Removes the mapping for this key from this map if it is present
//     * (optional operation).   More formally, if this map contains a mapping
//     * from key <tt>k</tt> to value <tt>v</tt> such that
//     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
//     * is removed.  (The map can contain at most one such mapping.)
//     * <p/>
//     * <p>Returns the value to which the map previously associated the key, or
//     * <tt>null</tt> if the map contained no mapping for this key.  (A
//     * <tt>null</tt> return can also indicate that the map previously
//     * associated <tt>null</tt> with the specified key if the implementation
//     * supports <tt>null</tt> values.)  The map will not contain a mapping for
//     * the specified  key once the call returns.
//     *
//     * @param key key whose mapping is to be removed from the map.
//     * @return previous value associated with specified key, or <tt>null</tt>
//     *         if there was no mapping for key.
//     */
//    public Object remove(Object key) {
//        Element element = cache.get(key);
//        cache.remove(key);
//        if (element != null) {
//            return element.getObjectValue();
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * Atomically removes the mapping for a key only if currently mapped to the given value.
//     * <p/>
//     * This is equivalent to
//     * <pre>
//     *   if (cache.containsKey(key) &amp;&amp; cache.get(key).equals(oldValue)) {
//     *       cache.remove(key);
//     *       return true;
//     *   } else {
//     *       return false;
//     *   }</pre>
//     * except that the action is performed atomically.
//     *
//     * @param key      key whose mapping is to be removed from the cache
//     * @param oldValue value expected to be associated with the specified key
//     * @return returns false if there was no matching key
//     * @throws NullPointerException       if key is null
//     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
//     * @throws javax.cache.CacheException if there is a problem doing the put
//     * @see java.util.Map#remove(Object)
//     */
//    @Override
//    public boolean remove(Object key, V oldValue) throws CacheException {
//        throw new UnsupportedOperationException("remove is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Atomically removes the entry for a key only if currently mapped to a given value.
//     * <p/>
//     * This is equivalent to
//     * <pre>
//     *   if (cache.containsKey(key)) {
//     *       V oldValue = cache.get(key);
//     *       cache.remove(key);
//     *       return oldValue;
//     *   } else {
//     *       return null;
//     *   }</pre>
//     * except that the action is performed atomically.
//     *
//     * @param key key with which the specified value is associated
//     * @return the value if one existed or null if no mapping existed for this key
//     * @throws NullPointerException       if the specified key or value is null.
//     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
//     * @throws javax.cache.CacheException if there is a problem during the remove
//     * @see java.util.Map#remove(Object)
//     */
//    @Override
//    public V getAndRemove(Object key) throws CacheException {
//        throw new UnsupportedOperationException("getAndRemove is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Atomically replaces the entry for a key only if currently mapped to a given value.
//     * <p/>
//     * This is equivalent to
//     * <pre>
//     *   if (cache.containsKey(key) &amp;&amp; cache.get(key).equals(oldValue)) {
//     *       cache.put(key, newValue);
//     *       return true;
//     *   } else {
//     *       return false;
//     *   }</pre>
//     * except that the action is performed atomically.
//     *
//     * @param key      key with which the specified value is associated
//     * @param oldValue value expected to be associated with the specified key
//     * @param newValue value to be associated with the specified key
//     * @return <tt>true</tt> if the value was replaced
//     * @throws NullPointerException       if key is null or if the values are null
//     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
//     * @throws javax.cache.CacheException if there is a problem during the replace
//     * @see java.util.concurrent.ConcurrentMap#replace(Object, Object, Object)
//     */
//    @Override
//    public boolean replace(K key, V oldValue, V newValue) throws CacheException {
//        throw new UnsupportedOperationException("replace is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Atomically replaces the entry for a key only if currently mapped to some value.
//     * <p/>
//     * This is equivalent to
//     * <pre>
//     *   if (cache.containsKey(key)) {
//     *       cache.put(key, value);
//     *       return true;
//     *   } else {
//     *       return false;
//     *   }</pre>
//     * except that the action is performed atomically.
//     *
//     * In contrast to the corresponding ConcurrentMap operation, does not return
//     * the previous value.
//     *
//     * @param key   key with which the specified value is associated
//     * @param value value to be associated with the specified key
//     * @return <tt>true</tt> if the value was replaced
//     * @throws NullPointerException       if key is null or if value is null
//     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
//     * @throws javax.cache.CacheException if there is a problem during the replace
//     * @see #getAndReplace(Object, Object)
//     * @see java.util.concurrent.ConcurrentMap#replace(Object, Object)
//     */
//    @Override
//    public boolean replace(K key, V value) throws CacheException {
//        throw new UnsupportedOperationException("replace is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Atomically replaces the entry for a key only if currently mapped to some value.
//     * <p/>
//     * This is equivalent to
//     * <pre>
//     *   if (cache.containsKey(key)) {
//     *       V value = cache.get(key, value);
//     *       cache.put(key, value);
//     *       return value;
//     *   } else {
//     *       return null;
//     *   }</pre>
//     * except that the action is performed atomically.
//     *
//     * @param key   key with which the specified value is associated
//     * @param value value to be associated with the specified key
//     * @return the previous value associated with the specified key, or
//     *         <tt>null</tt> if there was no mapping for the key.
//     * @throws NullPointerException       if key is null or if value is null
//     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
//     * @throws javax.cache.CacheException if there is a problem during the replace
//     * @see java.util.concurrent.ConcurrentMap#replace(Object, Object)
//     */
//    @Override
//    public V getAndReplace(K key, V value) throws CacheException {
//        throw new UnsupportedOperationException("getAndReplace is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Removes entries for the specified keys
//     * <p/>
//     *
//     * @param keys the keys to remove
//     * @throws NullPointerException       if keys is null or if it contains a null key
//     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
//     * @throws javax.cache.CacheException if there is a problem during the remove
//     */
//    @Override
//    public void removeAll(Collection<? extends K> keys) throws CacheException {
//        throw new UnsupportedOperationException("removeAll is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Removes all of the mappings from this cache.
//     * <p/>
//     * This is potentially an expensive operation.
//     * <p/>
//     *
//     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
//     * @throws javax.cache.CacheException if there is a problem during the remove
//     * @see java.util.Map#clear()
//     */
//    @Override
//    public void removeAll() throws CacheException {
//        throw new UnsupportedOperationException("removeAll is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Returns a CacheConfiguration.
//     * <p/>
//     * When status is {@link javax.cache.Status#STARTED} an implementation must respect the following:
//     * <ul>
//     * <li>Statistics must be mutable when status is {@link javax.cache.Status#STARTED} ({@link javax.cache.CacheConfiguration#setStatisticsEnabled(boolean)})</li>
//     * </ul>
//     * <p/>
//     * If an implementation permits mutation of configuration to a running cache, those changes must be reflected
//     * in the cache. In the case where mutation is not allowed {@link javax.cache.InvalidConfigurationException} must be thrown on
//     * an attempt to mutate the configuration.
//     *
//     * @return the {@link javax.cache.CacheConfiguration} of this cache
//     */
//    @Override
//    public CacheConfiguration getConfiguration() {
//        throw new UnsupportedOperationException("getConfiguration is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Adds a listener to the notification service. No guarantee is made that listeners will be
//     * notified in the order they were added.
//     * <p/>
//     *
//     * @param cacheEntryListener The listener to add. A listener may be added only once, so the same listener with two difference scopes
//     *                           is not allowed.
//     * @param scope              The notification scope.
//     * @param synchronous        whether to listener should be invoked synchronously
//     * @return true if the listener is being added and was not already added
//     * @throws NullPointerException if any of the arguments are null.
//     */
//    @Override
//    public boolean registerCacheEntryListener(CacheEntryListener<K, V> kvCacheEntryListener, NotificationScope scope, boolean synchronous) {
//        throw new UnsupportedOperationException("registerCacheEntryListener is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Removes a call back listener.
//     *
//     * @param cacheEntryListener the listener to remove
//     * @return true if the listener was present
//     */
//    @Override
//    public boolean unregisterCacheEntryListener(CacheEntryListener<?, ?> cacheEntryListener) {
//        throw new UnsupportedOperationException("unregisterCacheEntryListener is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Return the name of the cache.
//     *
//     * @return the name of the cache.
//     */
//    @Override
//    public String getName() {
//        throw new UnsupportedOperationException("getName is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Gets the CacheManager managing this cache.
//     * <p/>
//     * A cache can be in only one CacheManager.
//     *
//     * @return the manager
//     */
//    @Override
//    public CacheManager getCacheManager() {
//        throw new UnsupportedOperationException("getCacheManager is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Return an object of the specified type to allow access to the provider-specific API. If the provider's
//     * implementation does not support the specified class, the {@link IllegalArgumentException} is thrown.
//     *
//     * @param cls he class of the object to be returned. This is normally either the underlying implementation class or an interface that it implements.
//     * @return an instance of the specified class
//     * @throws IllegalArgumentException if the provider doesn't support the specified class.
//     */
//    @Override
//    public <T> T unwrap(Class<T> cls) {
//        throw new UnsupportedOperationException("unwrap is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Copies all of the mappings from the specified map to this map
//     * (optional operation).  The effect of this call is equivalent to that
//     * of calling {@link #put(Object,Object) put(k, v)} on this map once
//     * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
//     * N specified map.  The behavior of this operation is unspecified if the
//     * specified map is modified while the operation is in progress.
//     *
//     * @param sourceMap Mappings to be stored in this map.
//     */
//
//    public void putAll(Map sourceMap) {
//        if (sourceMap == null) {
//            return;
//        }
//        for (Iterator iterator = sourceMap.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            cache.put(new Element(key, sourceMap.get(key)));
//        }
//    }
//
//    /**
//     * Removes all mappings from this map (optional operation).
//     */
//    public void clear() {
//        cache.removeAll();
//    }
//
//    /**
//     * Returns a set view of the keys contained in this map.  The set is
//     * not live because ehcache is not backed by a simple map.
//     *
//     * @return a set view of the keys contained in this map.
//     */
//    public Set keySet() {
//        List list = cache.getKeys();
//        Set set = new HashSet();
//        set.addAll(list);
//        return set;
//    }
//
//    /**
//     * Returns a collection view of the values contained in this map.  The
//     * collection is backed by the map, so changes to the map are reflected in
//     * the collection, and vice-versa.  If the map is modified while an
//     * iteration over the collection is in progress (except through the
//     * iterator's own <tt>remove</tt> operation), the results of the
//     * iteration are undefined.  The collection supports element removal,
//     * which removes the corresponding mapping from the map, via the
//     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
//     * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations.
//     * It does not support the add or <tt>addAll</tt> operations.
//     * <p/>
//     * Contradicting the above Map contract, whether cache changes after this method returns are not
//     * reflected in the Collection. This is because ehcache is not backed by a single map.
//     *
//     * @return a collection view of the values contained in this map.
//     */
//    public Collection values() {
//        List list = cache.getKeysNoDuplicateCheck();
//        Set set = new HashSet(list.size());
//        for (int i = 0; i < list.size(); i++) {
//            Object key = list.get(i);
//            Element element = cache.get(key);
//            if (element != null) {
//                set.add(element.getObjectValue());
//            }
//        }
//        return set;
//    }
//
//    /**
//     * Returns a set view of the mappings contained in this map.  Each element
//     * in the returned set is a {@link java.util.Map.Entry}.  The set is backed by the
//     * map, so changes to the map are reflected in the set, and vice-versa.
//     * If the map is modified while an iteration over the set is in progress
//     * (except through the iterator's own <tt>remove</tt> operation, or through
//     * the <tt>setValue</tt> operation on a map entry returned by the iterator)
//     * the results of the iteration are undefined.  The set supports element
//     * removal, which removes the corresponding mapping from the map, via the
//     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
//     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not support
//     * the <tt>add</tt> or <tt>addAll</tt> operations.
//     * <p/>
//     * Contradicting the above Map contract, whether or not changes to an entry affect the entry in the cache is undefined.
//     *
//     * @return a set view of the mappings contained in this map.
//     */
//    public Set entrySet() {
//        List list = cache.getKeysNoDuplicateCheck();
//        Set set = new HashSet(list.size());
//        for (int i = 0; i < list.size(); i++) {
//            Object key = list.get(i);
//            Element element = cache.get(key);
//            if (element != null) {
//                set.add(new net.sf.ehcache.jcache.JCacheEntry(element));
//            }
//        }
//        return set;
//    }
//
//    /**
//     * Sets the statistics accuracy.
//     *
//     * @param statisticsAccuracy one of {@link CacheStatistics#STATISTICS_ACCURACY_BEST_EFFORT}, {@link CacheStatistics#STATISTICS_ACCURACY_GUARANTEED}, {@link CacheStatistics#STATISTICS_ACCURACY_NONE}
//     */
//    public void setStatisticsAccuracy(int statisticsAccuracy) {
//        cache.setStatisticsAccuracy(statisticsAccuracy);
//    }
//
//
//    /**
//     * Gets the backing Ehcache
//     */
//    public Ehcache getBackingCache() {
//        return cache;
//    }
//
//
//    /**
//     * Returns a {@link String} representation of the underlying {@link net.sf.ehcache.Cache}.
//     *
//     * @return a string representation of the object.
//     */
//    public String toString() {
//        return cache.toString();
//    }
//
//
//    /**
//     * Notifies providers to start themselves.
//     * <p/>
//     * This method is called during the resource's start method after it has changed it's
//     * status to alive. Cache operations are legal in this method.
//     * <p/>
//     * At the completion of this method invocation {@link #getStatus()} must return {@link javax.cache.Status#STARTED}.
//     *
//     * @throws javax.cache.CacheException
//     */
//    @Override
//    public void start() throws CacheException {
//        throw new UnsupportedOperationException("start is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Providers may be doing all sorts of exotic things and need to be able to clean up on
//     * stop.
//     * <p/>
//     * Cache operations are illegal after this method is called.
//     * A {@link IllegalStateException} will be
//     * <p/>
//     * Resources will change status to {@link javax.cache.Status#STOPPED} when this method completes.
//     * <p/>
//     * Stop must free any JVM resources used.
//     *
//     * @throws javax.cache.CacheException
//     * @throws IllegalStateException      thrown if an operation is performed on a cache unless it is started.
//     */
//    @Override
//    public void stop() throws CacheException {
//        throw new UnsupportedOperationException("stop is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Returns the cache status.
//     * <p/>
//     * This method blocks while the state is changing
//     *
//     * @return one of {@link javax.cache.Status}
//     */
//    @Override
//    public Status getStatus() {
//        throw new UnsupportedOperationException("getStatus is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
//
//    /**
//     * Returns an iterator over a set of elements of type T.
//     *
//     * @return an Iterator.
//     */
//    @Override
//    public Iterator<Entry<K, V>> iterator() {
//        throw new UnsupportedOperationException("iterator is not implemented in net.sf.ehcache.jcache.JCacheOLD");
//    }
}
