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

package net.sf.ehcache.loader;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.jcache.loader.JCacheLoader;
import net.sf.jsr107cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;



/**
 * A cache loader that counts the number of things it has loaded, useful for testing.
 * <p/>
 * <p/>
 * Each load has a random delay to introduce some nice threading entropy.
 *
 * @author Greg Luck
 * @version $Id: CountingCacheLoader.java 796 2008-10-09 02:39:03Z gregluck $
 */
public class CountingCacheLoader implements JCacheLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CountingCacheLoader.class);

    private volatile int loadCounter;
    private volatile int loadAllCounter;
    private Random random = new Random();
    private String name = "CountingCacheLoader";

    /**
     * loads an object. Application writers should implement this
     * method to customize the loading of cache object. This method is called
     * by the caching service when the requested object is not in the cache.
     * <p/>
     *
     * @param key the key identifying the object being loaded
     * @return The object that is to be stored in the cache.
     * @throws net.sf.jsr107cache.CacheException
     *
     */
    public Object load(Object key) {
        try {
            Thread.sleep(random.nextInt(3) + 1);
        } catch (InterruptedException e) {
            LOG.error("Interrupted");
        }
        return new Integer(loadCounter++);
    }

    /**
     * loads multiple object. Application writers should implement this
     * method to customize the loading of cache object. This method is called
     * by the caching service when the requested object is not in the cache.
     * <p/>
     *
     * @param keys a Collection of keys identifying the objects to be loaded
     * @return A Map of objects that are to be stored in the cache.
     * @throws net.sf.jsr107cache.CacheException
     *
     */

    public Map loadAll(Collection keys) {
        Map map = new HashMap(keys.size());
        for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
            Object key = iterator.next();
            map.put(key, new Integer(loadAllCounter++));
        }
        return map;
    }


    /**
     * @return
     */
    public int getLoadCounter() {
        return loadCounter;
    }

    /**
     * @return
     */
    public int getLoadAllCounter() {
        return loadAllCounter;
    }

    /**
     * Load using both a key and an argument.
     * <p/>
     * JCache will use the load(key) method where the argument is null.
     *
     * @param key
     * @param argument
     * @return
     * @throws net.sf.jsr107cache.CacheException
     *
     */
    public Object load(Object key, Object argument) {
        try {
            Thread.sleep(random.nextInt(3) + 1);
        } catch (InterruptedException e) {
            LOG.error("Interrupted");
        }
        return name + ":" + argument;
//        return load(key);
    }

    /**
     * Load using both a key and an argument.
     * <p/>
     * JCache will use the loadAll(key) method where the argument is null.
     *
     * @param keys
     * @param argument
     * @return
     * @throws net.sf.jsr107cache.CacheException
     *
     */
    public Map loadAll(Collection keys, Object argument) {
        return loadAll(keys);
    }

    /**
     * Gets the name of a CacheLoader
     *
     * @return
     */
    public String getName() {
        return name;
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
    public JCacheLoader clone(Ehcache cache) throws CloneNotSupportedException {
        return null;
    }

    /**
     * Notifies providers to initialise themselves.
     * <p/>
     * This method is called during the Cache's initialise method after it has changed it's
     * status to alive. Cache operations are legal in this method.
     *
     * @throws net.sf.ehcache.CacheException
     */
    public void init() {
        //nothing required
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
    public void dispose() throws net.sf.ehcache.CacheException {
        //nothing required
    }

    /**
     * @return the status of the extension
     */
    public Status getStatus() {
        return null;
    }

    /**
     * Sets the name
     */
    public void setName(String name) {
        this.name = name;
    }


}
