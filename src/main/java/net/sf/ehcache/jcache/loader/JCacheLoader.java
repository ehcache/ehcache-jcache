/**
 *  Copyright 2003-2008 Luck Consulting Pty Ltd
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


package net.sf.ehcache.jcache.loader;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;

import java.util.Collection;
import java.util.Map;


/**
 * Extends JCache CacheLoader with load methods that take an argument in addition to a key
 *
 * @author Greg Luck
 * @version $Id$
 */
public interface JCacheLoader extends net.sf.jsr107cache.CacheLoader, net.sf.ehcache.loader.CacheLoader {


    /**
     * Load using both a key and an argument.
     * <p/>
     * JCache will call through to the load(key) method, rather than this method, where the argument is null.
     *
     * @param key      the key to load the object for
     * @param argument can be anything that makes sense to the loader
     * @return the Object loaded
     */
    Object load(Object key, Object argument);

    /**
     * Load using both a key and an argument.
     * <p/>
     * JCache will use the loadAll(key) method where the argument is null.
     *
     * @param keys     the keys to load objects for
     * @param argument can be anything that makes sense to the loader
     * @return a map of Objects keyed by the collection of keys passed in.
     */
    Map loadAll(Collection keys, Object argument);

    /**
     * Gets the name of a CacheLoader
     *
     * @return the name of this CacheLoader
     */
    String getName();

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
    net.sf.ehcache.loader.CacheLoader clone(Ehcache cache) throws CloneNotSupportedException;


    /**
     * Notifies providers to initialise themselves.
     * <p/>
     * This method is called during the Cache's initialise method after it has changed it's
     * status to alive. Cache operations are legal in this method.
     *
     * @throws net.sf.ehcache.CacheException
     */
    void init();

    /**
     * Providers may be doing all sorts of exotic things and need to be able to clean up on
     * dispose.
     * <p/>
     * Cache operations are illegal when this method is called. The cache itself is partly
     * disposed when this method is called.
     *
     * @throws net.sf.ehcache.CacheException
     */
    void dispose() throws net.sf.ehcache.CacheException;


    /**
     * @return the status of the extension
     */
    Status getStatus();
}

