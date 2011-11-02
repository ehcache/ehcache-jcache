package net.sf.ehcache.jcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;

import javax.cache.CacheLoader;
import java.util.Collection;
import java.util.Map;

/**
* Created by IntelliJ IDEA.
* User: ddcryang
* Date: 10/28/11
* Time: 1:26 PM
* To change this template use File | Settings | File Templates.
*/
public class JCacheCacheLoaderAdapter<K,V> implements net.sf.ehcache.loader.CacheLoader {
    private javax.cache.CacheLoader cacheLoader;

    public JCacheCacheLoaderAdapter(CacheLoader cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    public CacheLoader getJCacheCacheLoader() {
        return cacheLoader;
    }

    public void setCacheLoader(CacheLoader cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    /**
     * loads an object. Application writers should implement this
     * method to customize the loading of cache object. This method is called
     * by the caching service when the requested object is not in the cache.
     * <p/>
     *
     * @param key the key identifying the object being loaded
     * @return The object that is to be stored in the cache.
     * @throws net.sf.ehcache.CacheException
     */
    @Override
    public Object load(Object key) throws CacheException {
        return cacheLoader.load(key);
    }

    /**
     * loads multiple object. Application writers should implement this
     * method to customize the loading of cache object. This method is called
     * by the caching service when the requested object is not in the cache.
     * <p/>
     *
     * @param keys a Collection of keys identifying the objects to be loaded
     * @return A Map of objects that are to be stored in the cache.
     * @throws net.sf.ehcache.CacheException
     */
    @Override
    public Map loadAll(Collection keys) {
        return cacheLoader.loadAll(keys);
    }

    /**
     * Load using both a key and an argument.
     * <p/>
     * JCache will call through to the load(key) method, rather than this method, where the argument is null.
     *
     * @param key      the key to load the object for
     * @param argument can be anything that makes sense to the loader
     * @return the Object loaded
     * @throws net.sf.ehcache.CacheException
     */
    @Override
    public Object load(Object key, Object argument) {
        return load(key);
    }

    /**
     * Load using both a key and an argument.
     * <p/>
     * JCache will use the loadAll(key) method where the argument is null.
     *
     * @param keys     the keys to load objects for
     * @param argument can be anything that makes sense to the loader
     * @return a map of Objects keyed by the collection of keys passed in.
     * @throws net.sf.ehcache.CacheException
     */
    @Override
    public Map loadAll(Collection keys, Object argument) {
        return loadAll(keys);
    }

    /**
     * Gets the name of a CacheLoader
     *
     * @return the name of this CacheLoader
     */
    @Override
    public String getName() {
        return this.getClass().toString();
    }

    /**
     * Creates a clone of this extension. This method will only be called by ehcache before a
     * cache is initialized.
     * <p/>
     * Implementations should throw CloneNotSupportedException if they do not support clone
     * but that will stop them from being used with defaultCache.
     *
     * @return a clone
     * @throws CloneNotSupportedException if the extension could not be cloned.
     */
    @Override
    public net.sf.ehcache.loader.CacheLoader clone(Ehcache cache) throws CloneNotSupportedException {
        JCacheCacheLoaderAdapter clone = (JCacheCacheLoaderAdapter)super.clone();
        return clone;
    }

    /**
     * Notifies providers to initialise themselves.
     * <p/>
     * This method is called during the Cache's initialise method after it has changed it's
     * status to alive. Cache operations are legal in this method.
     *
     * @throws net.sf.ehcache.CacheException
     */
    @Override
    public void init() {
    }

    /**
     * Providers may be doing all sorts of exotic things and need to be able to clean up on
     * dispose.
     * <p/>
     * Cache operations are illegal when this method is called. The cache itself is partly
     * disposed when this method is called.
     *
     * @throws net.sf.ehcache.CacheException
     */
    @Override
    public void dispose() throws CacheException {
    }

    /**
     * @return the status of the extension
     */
    @Override
    public net.sf.ehcache.Status getStatus() {
        return null;
    }
}
