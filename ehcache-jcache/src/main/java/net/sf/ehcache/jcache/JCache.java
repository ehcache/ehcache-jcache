package net.sf.ehcache.jcache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;


public class JCache<K, V> implements Cache<K, V> {
    private static final int CACHE_LOADER_THREADS = 2;
    private final ExecutorService executorService = Executors.newFixedThreadPool(CACHE_LOADER_THREADS);

    private static final Logger LOG = LoggerFactory.getLogger(JCache.class);

    /**
     * An Ehcache backing instance
     */
    private Ehcache ehcache;
    private CacheManager cacheManager;
    private JCacheCacheLoaderAdapter cacheLoaderAdapter;
    private JCacheCacheWriterAdapter cacheWriterAdapter;

    private JCacheConfiguration configuration;

    public Ehcache getEhcache() {
        return ehcache;
    }

    /**
     * A constructor for JCache.
     * <p/>
     * JCache is an adaptor for an Ehcache, and therefore requires an Ehcache in its constructor.
     * <p/>
     *
     * @param ehcache An ehcache
     * @see "class description for recommended usage"
     * @since 1.4
     */
    public JCache(Ehcache ehcache, CacheManager cacheManager, ClassLoader classLoader) {
        this.ehcache = ehcache;
        this.cacheManager = cacheManager;
        this.configuration = new JCacheConfiguration(ehcache.getCacheConfiguration());
    }

    private void checkStatusStarted() {
        if (!JCacheStatusAdapter.adaptStatus(ehcache.getStatus()).equals(Status.STARTED)) {
            throw new IllegalStateException("The cache status is not STARTED");
        }
    }

    /**
     * Gets an entry from the cache.
     * <p/>
     *
     * @param key the key whose associated value is to be returned
     * @return the element, or null, if it does not exist.
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws NullPointerException       if the key is null
     * @throws javax.cache.CacheException if there is a problem fetching the value
     * @see java.util.Map#get(Object)
     */
    @Override
    public V get(Object key) throws CacheException {
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

    private V getFromLoader(Object key) {
        Cache.Entry<K, V> entry = (Entry<K, V>) cacheLoaderAdapter.load(key);
        if (entry != null) {
            ehcache.put(new Element(entry.getKey(), entry.getValue()));
            return entry.getValue();
        } else {
            return null;
        }

    }


    /**
     * The getAll method will return, from the cache, a {@link java.util.Map} of the objects
     * associated with the Collection of keys in argument "keys". If the objects
     * are not in the cache, the associated cache loader will be called. If no
     * loader is associated with an object, a null is returned.  If a problem
     * is encountered during the retrieving or loading of the objects, an
     * exception will be thrown.
     * <p/>
     *
     * @param keys The keys whose associated values are to be returned.
     * @return The entries for the specified keys.
     * @throws NullPointerException       if keys is null or if keys contains a null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem fetching the values.
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
     * Returns <tt>true</tt> if this cache contains a mapping for the specified
     * key.  More formally, returns <tt>true</tt> if and only if
     * this cache contains a mapping for a key <tt>k</tt> such that
     * <tt>key.equals(k)</tt>.  (There can be at most one such mapping.)
     * <p/>
     *
     * @param key key whose presence in this cache is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified key
     * @throws NullPointerException       if key is null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException it there is a problem checking the mapping
     * @see java.util.Map#containsKey(Object)
     */
    @Override
    public boolean containsKey(Object key) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        return ehcache.isKeyInCache(key);
    }

    /**
     * The load method provides a means to "pre load" the cache. This method
     * will, asynchronously, load the specified object into the cache using
     * the associated {@link javax.cache.CacheLoader}.
     * If the object already exists in the cache, no action is taken and null is returned.
     * If no loader is associated with the cache
     * no object will be loaded into the cache and null is returned.
     * If a problem is encountered during the retrieving or loading of the object, an exception
     * must be propagated on {@link java.util.concurrent.Future#get()} as a {@link java.util.concurrent.ExecutionException}
     * <p/>
     *
     * @param key the key
     * @return a Future which can be used to monitor execution.
     * @throws NullPointerException       if key is null.
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem doing the load
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
     * The loadAll method provides a means to "pre load" objects into the cache.
     * This method will, asynchronously, load the specified objects into the
     * cache using the associated cache loader. If the an object already exists
     * in the cache, no action is taken. If no loader is associated with the
     * object, no object will be loaded into the cache.  If a problem is
     * encountered during the retrieving or loading of the objects, an
     * exception (to be defined) should be logged.
     * <p/>
     * The getAll method will return, from the cache, a Map of the objects
     * associated with the Collection of keys in argument "keys". If the objects
     * are not in the cache, the associated cache loader will be called. If no
     * loader is associated with an object, a null is returned.  If a problem
     * is encountered during the retrieving or loading of the objects, an
     * exception (to be defined) will be thrown.
     * <p/>
     *
     * @param keys the keys
     * @return a Future which can be used to monitor execution
     * @throws NullPointerException       if keys is null or if keys contains a null.
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem doing the load
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
        FutureTask<Map<K, V>> task = new FutureTask<Map<K, V>>(new JCacheLoaderLoadAllCallable<K, V>(this, keys));
        executorService.submit(task);
        return task;
    }

    /**
     * Returns the {@link javax.cache.CacheStatistics} MXBean associated with the cache.
     * May return null if the cache does not support statistics gathering.
     *
     * @return the CacheStatisticsMBean
     * @throws IllegalStateException if the cache is not {@link javax.cache.Status#STARTED}
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
     * Associates the specified value with the specified key in this cache
     * If the cache previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A cache
     * <tt>c</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #containsKey(Object) c.containsKey(k)} would return
     * <tt>true</tt>.)
     * <p/>
     * In contrast to the corresponding Map operation, does not return
     * the previous value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @throws NullPointerException       if key is null or if value is null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem doing the put
     * @see java.util.Map#put(Object, Object)
     * @see #getAndPut(Object, Object)
     * @see #getAndReplace(Object, Object)
     */
    @Override
    public void put(K key, V value) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        checkValue(value);
        ehcache.put(new Element(key, value));
    }

    /**
     * Atomically associates the specified value with the specified key in this cache
     * <p/>
     * If the cache previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A cache
     * <tt>c</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #containsKey(Object) c.containsKey(k)} would return
     * <tt>true</tt>.)
     * <p/>
     * The the previous value is returned, or null if there was no value associated
     * with the key previously.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the value associated with the key at the start of the operation or null if none was associated
     * @throws NullPointerException       if key is null or if value is null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem doing the put
     * @see java.util.Map#put(Object, Object)
     * @see #put(Object, Object)
     * @see #getAndReplace(Object, Object)
     */
    @Override
    public V getAndPut(K key, V value) throws CacheException {
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
     * Copies all of the mappings from the specified map to this cache.
     * The effect of this call is equivalent to that
     * of calling {@link #put(Object, Object) put(k, v)} on this cache once
     * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
     * specified map.  The behavior of this operation is undefined if the
     * specified cache or map is modified while the operation is in progress.
     *
     * @param map mappings to be stored in this cache
     * @throws NullPointerException       if map is null or if map contains null keys or values.
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem doing the put
     * @see java.util.Map#putAll(java.util.Map)
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
     * Atomically associates the specified key with the given value if it is
     * not already associated with a value.
     * <p/>
     * This is equivalent to
     * <pre>
     *   if (!cache.containsKey(key)) {}
     *       cache.put(key, value);
     *       return true;
     *   } else {
     *       return false;
     *   }</pre>
     * except that the action is performed atomically.
     *
     * In contrast to the corresponding ConcurrentMap operation, does not return
     * the previous value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return true if a value was set.
     * @throws NullPointerException       if key is null or value is null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem doing the put
     * @see java.util.concurrent.ConcurrentMap#putIfAbsent(Object, Object)
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
     * Removes the mapping for a key from this cache if it is present.
     * More formally, if this cache contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The cache can contain at most one such mapping.)
     * <p/>
     * <p>Returns <tt>true</tt> if this cache previously associated the key,
     * or <tt>false</tt> if the cache contained no mapping for the key.
     * <p/>
     * <p>The cache will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return returns false if there was no matching key
     * @throws NullPointerException       if key is null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem doing the put
     * @see java.util.Map#remove(Object)
     */
    @Override
    public boolean remove(Object key) throws CacheException {
        checkStatusStarted();
        checkKey(key);
        return ehcache.remove(key);
    }

    /**
     * Atomically removes the mapping for a key only if currently mapped to the given value.
     * <p/>
     * This is equivalent to
     * <pre>
     *   if (cache.containsKey(key) &amp;&amp; cache.get(key).equals(oldValue)) {
     *       cache.remove(key);
     *       return true;
     *   } else {
     *       return false;
     *   }</pre>
     * except that the action is performed atomically.
     *
     * @param key      key whose mapping is to be removed from the cache
     * @param oldValue value expected to be associated with the specified key
     * @return returns false if there was no matching key
     * @throws NullPointerException       if key is null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem doing the put
     * @see java.util.Map#remove(Object)
     */
    @Override
    public boolean remove(Object key, V oldValue) throws CacheException {
        checkStatusStarted();
        checkKey(key);

        if (containsKey(key) && get(key).equals(oldValue)) {
            return remove(key);
        } else {
            return false;
        }
    }

    /**
     * Atomically removes the entry for a key only if currently mapped to a given value.
     * <p/>
     * This is equivalent to
     * <pre>
     *   if (cache.containsKey(key)) {
     *       V oldValue = cache.get(key);
     *       cache.remove(key);
     *       return oldValue;
     *   } else {
     *       return null;
     *   }</pre>
     * except that the action is performed atomically.
     *
     * @param key key with which the specified value is associated
     * @return the value if one existed or null if no mapping existed for this key
     * @throws NullPointerException       if the specified key or value is null.
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem during the remove
     * @see java.util.Map#remove(Object)
     */
    @Override
    public V getAndRemove(Object key) throws CacheException {
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
     * Atomically replaces the entry for a key only if currently mapped to a given value.
     * <p/>
     * This is equivalent to
     * <pre>
     *   if (cache.containsKey(key) &amp;&amp; cache.get(key).equals(oldValue)) {
     *       cache.put(key, newValue);
     *       return true;
     *   } else {
     *       return false;
     *   }</pre>
     * except that the action is performed atomically.
     *
     * @param key      key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return <tt>true</tt> if the value was replaced
     * @throws NullPointerException       if key is null or if the values are null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem during the replace
     * @see java.util.concurrent.ConcurrentMap#replace(Object, Object, Object)
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue) throws CacheException {
        checkStatusStarted();
        return ehcache.replace(new Element(key, oldValue), new Element(key, newValue));
    }

    /**
     * Atomically replaces the entry for a key only if currently mapped to some value.
     * <p/>
     * This is equivalent to
     * <pre>
     *   if (cache.containsKey(key)) {
     *       cache.put(key, value);
     *       return true;
     *   } else {
     *       return false;
     *   }</pre>
     * except that the action is performed atomically.
     *
     * In contrast to the corresponding ConcurrentMap operation, does not return
     * the previous value.
     *
     * @param key   key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return <tt>true</tt> if the value was replaced
     * @throws NullPointerException       if key is null or if value is null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem during the replace
     * @see #getAndReplace(Object, Object)
     * @see java.util.concurrent.ConcurrentMap#replace(Object, Object)
     */
    @Override
    public boolean replace(K key, V value) throws CacheException {
        checkStatusStarted();
        return (ehcache.replace(new Element(key, value)) != null);
    }

    /**
     * Atomically replaces the entry for a key only if currently mapped to some value.
     * <p/>
     * This is equivalent to
     * <pre>
     *   if (cache.containsKey(key)) {
     *       V value = cache.get(key, value);
     *       cache.put(key, value);
     *       return value;
     *   } else {
     *       return null;
     *   }</pre>
     * except that the action is performed atomically.
     *
     * @param key   key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         <tt>null</tt> if there was no mapping for the key.
     * @throws NullPointerException       if key is null or if value is null
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem during the replace
     * @see java.util.concurrent.ConcurrentMap#replace(Object, Object)
     */
    @Override
    public V getAndReplace(K key, V value) throws CacheException {
        checkStatusStarted();
        Element replaced = ehcache.replace(new Element(key, value));
        return replaced != null ? (V) replaced.getValue() : null;
    }

    /**
     * Removes entries for the specified keys
     * <p/>
     *
     * @param keys the keys to remove
     * @throws NullPointerException       if keys is null or if it contains a null key
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem during the remove
     */
    @Override
    public void removeAll(Collection<? extends K> keys) throws CacheException {
        checkStatusStarted();
        for (K key : keys) {
            remove(key);
        }
    }

    /**
     * Removes all of the mappings from this cache.
     * <p/>
     * This is potentially an expensive operation.
     * <p/>
     *
     * @throws IllegalStateException      if the cache is not {@link javax.cache.Status#STARTED}
     * @throws javax.cache.CacheException if there is a problem during the remove
     * @see java.util.Map#clear()
     */
    @Override
    public void removeAll() throws CacheException {
        checkStatusStarted();
        ehcache.removeAll();
    }

    /**
     * Returns a CacheConfiguration.
     * <p/>
     * When status is {@link javax.cache.Status#STARTED} an implementation must respect the following:
     * <ul>
     * <li>Statistics must be mutable when status is {@link javax.cache.Status#STARTED} ({@link javax.cache.CacheConfiguration#setStatisticsEnabled(boolean)})</li>
     * </ul>
     * <p/>
     * If an implementation permits mutation of configuration to a running cache, those changes must be reflected
     * in the cache. In the case where mutation is not allowed {@link javax.cache.InvalidConfigurationException} must be thrown on
     * an attempt to mutate the configuration.
     *
     * @return the {@link javax.cache.CacheConfiguration} of this cache
     */
    @Override
    public JCacheConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Adds a listener to the notification service. No guarantee is made that listeners will be
     * notified in the order they were added.
     * <p/>
     *
     * @param cacheEntryListener The listener to add. A listener may be added only once, so the same listener with two difference scopes
     *                           is not allowed.
     * @param scope              The notification scope.
     * @param synchronous        whether to listener should be invoked synchronously
     * @return true if the listener is being added and was not already added
     * @throws NullPointerException if any of the arguments are null.
     */
    @Override
    public boolean registerCacheEntryListener(CacheEntryListener<K, V> cacheEntryListener, NotificationScope scope, boolean synchronous) {
        throw new UnsupportedOperationException("registerCacheEntryListener is not implemented in net.sf.ehcache.jcache.JCache");
    }

    /**
     * Removes a call back listener.
     *
     * @param cacheEntryListener the listener to remove
     * @return true if the listener was present
     */
    @Override
    public boolean unregisterCacheEntryListener(CacheEntryListener<?, ?> cacheEntryListener) {
        throw new UnsupportedOperationException("unregisterCacheEntryListener is not implemented in net.sf.ehcache.jcache.JCache");
    }

    /**
     * Return the name of the cache.
     *
     * @return the name of the cache.
     */
    @Override
    public String getName() {
        return this.ehcache.getName();
    }

    /**
     * Gets the CacheManager managing this cache.
     * <p/>
     * A cache can be in only one CacheManager.
     *
     * @return the manager
     */
    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * Return an object of the specified type to allow access to the provider-specific API. If the provider's
     * implementation does not support the specified class, the {@link IllegalArgumentException} is thrown.
     *
     * @param cls he class of the object to be returned. This is normally either the underlying implementation class or an interface that it implements.
     * @return an instance of the specified class
     * @throws IllegalArgumentException if the provider doesn't support the specified class.
     */
    @Override
    public <T> T unwrap(Class<T> cls) {
        if (this.getClass().isAssignableFrom(cls)) {
            return (T) this;
        }
        if (ehcache.getClass().isAssignableFrom(cls)) {
            return (T) ehcache;
        }
        throw new IllegalArgumentException("Can't cast the the specified class");
    }

    /**
     * Notifies providers to start themselves.
     * <p/>
     * This method is called during the resource's start method after it has changed it's
     * status to alive. Cache operations are legal in this method.
     * <p/>
     * At the completion of this method invocation {@link #getStatus()} must return {@link javax.cache.Status#STARTED}.
     *
     * @throws javax.cache.CacheException
     */
    @Override
    public void start() throws CacheException {
        //
    }

    /**
     * Providers may be doing all sorts of exotic things and need to be able to clean up on
     * stop.
     * <p/>
     * Cache operations are illegal after this method is called.
     * A {@link IllegalStateException} will be
     * <p/>
     * Resources will change status to {@link javax.cache.Status#STOPPED} when this method completes.
     * <p/>
     * Stop must free any JVM resources used.
     *
     * @throws javax.cache.CacheException
     * @throws IllegalStateException      thrown if an operation is performed on a cache unless it is started.
     */
    @Override
    public void stop() throws CacheException {
        checkStatusStarted();
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        }
        ehcache.dispose();
    }

    /**
     * Returns the cache status.
     * <p/>
     * This method blocks while the state is changing
     *
     * @return one of {@link javax.cache.Status}
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
    
    private class EhcacheIterator implements Iterator<Entry<K, V>> {
           private final Iterator keyIterator;
           private K lastKey = null;
           
           public EhcacheIterator(Iterator keyIterator) {
               this.keyIterator = keyIterator;
           }
           
           /**
            * @inheritdoc
            */
           public boolean hasNext() {
               return keyIterator.hasNext();
           }
   
           /**
            * @inheritdoc
            */
           public Entry<K,V> next() {
               final K key = (K) keyIterator.next();
               lastKey = key;
               return new JCacheEntry<K, V>(ehcache.get(key));
           }
   
           /**
            * @inheritdoc
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
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     * @author Yannis Cosmadopoulos
     */
    private static class JCacheLoaderCallable<K, V> implements Callable<V> {
        private final JCache<K, V> cache;
        private final K key;

        JCacheLoaderCallable(JCache<K, V> cache, K key) {
            this.cache = cache;
            this.key = key;
        }

        @Override
        public V call() throws Exception {
            //Entry<K, V> entry = cacheLoader.load(key);
            //cache.put(entry.getKey(), entry.getValue());
            return (V) cache.getCacheLoaderAdapter().load(key);
            //Element element = cache.ehcache.getWithLoader(key, (net.sf.ehcache.loader.CacheLoader) cache.ehcache.getRegisteredCacheExtensions().iterator().next(), null);
            //return (V) element.getValue();
        }
    }

    /**
     * Callable used for cache loader.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     * @author Yannis Cosmadopoulos
     */
    private static class JCacheLoaderLoadAllCallable<K, V> implements Callable<Map<K, V>> {
        private final JCache<K, V> cache;
        private final Collection<? extends K> keys;

        JCacheLoaderLoadAllCallable(JCache<K, V> cache, Collection<? extends K> keys) {
            this.cache = cache;
            this.keys = keys;
        }

        @Override
        public Map<K, V> call() throws Exception {
            ArrayList<K> keysNotInStore = new ArrayList<K>();

            //Entry<K, V> entry = cacheLoader.load(key);
            //cache.put(entry.getKey(), entry.getValue());
            Map<K, V> map = cache.ehcache.getAllWithLoader(keys, null);
            return map;
        }
    }

    public static class Builder<K, V> implements CacheBuilder<K, V> {
        private String cacheName;
        private ClassLoader classLoader;

        private JCacheConfiguration cacheConfiguration;
        private CacheLoader<K, V> cacheLoader;
        private CacheWriter<K, V> cacheWriter;

        private final CopyOnWriteArraySet<ListenerRegistration<K, V>> listeners = new CopyOnWriteArraySet<ListenerRegistration<K, V>>();
        private final JCacheConfiguration.Builder configurationBuilder = new JCacheConfiguration.Builder();
        //        private boolean writeThrough;
//        private boolean readThrough;
//        private boolean storeByValue;
//        private boolean enableStats;
        private CacheManager cacheManager;


        //private final JCache.Builder<K, V> cacheBuilder;

        public Builder(String cacheName, CacheManager cacheManager, ClassLoader classLoader) {
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
            cacheConfiguration.getCacheConfiguration().setCopyOnWrite(cacheConfiguration.getCacheConfiguration().isCopyOnWrite());
            cacheConfiguration.getCacheConfiguration().setStatistics(cacheConfiguration.isStatisticsEnabled());

            // not best for default, but for now its good
            cacheConfiguration.getCacheConfiguration().setDiskPersistent(false);

            net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(cacheConfiguration.getCacheConfiguration());
            JCache<K, V> jcache = new JCache<K, V>(cache, this.cacheManager, this.classLoader);

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
