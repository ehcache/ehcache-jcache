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

package net.sf.ehcache.jcache;

import net.sf.jsr107cache.CacheListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Greg Luck
 * @version $Id: CountingCacheListener.java 796 2008-10-09 02:39:03Z gregluck $
 */
public class CountingCacheListener implements CacheListener {

    private final List cacheElementsLoaded = Collections.synchronizedList(new ArrayList());
    private final List cacheElementsPut = Collections.synchronizedList(new ArrayList());
    private final List cacheElementsRemoved = Collections.synchronizedList(new ArrayList());
    private final List cacheElementsEvicted = Collections.synchronizedList(new ArrayList());
    private final List cacheClears = Collections.synchronizedList(new ArrayList());

    /**
     * Accessor
     */
    public List getCacheElementsRemoved() {
        return cacheElementsRemoved;
    }

    /**
     * Accessor
     */
    public List getCacheElementsPut() {
        return cacheElementsPut;
    }

    /**
     * Accessor
     */
    public List getCacheElementsEvicted() {
        return cacheElementsEvicted;
    }

    /**
     * Accessor
     */
    public List getCacheRemoveAlls() {
        return cacheClears;
    }


    /**
     * Resets the counters to 0
     */
    public void resetCounters() {
        synchronized (cacheElementsLoaded) {
            cacheElementsLoaded.clear();
        }
        synchronized (cacheElementsRemoved) {
            cacheElementsRemoved.clear();
        }
        synchronized (cacheElementsPut) {
            cacheElementsPut.clear();
        }
        synchronized (cacheElementsEvicted) {
            cacheElementsEvicted.clear();
        }
        synchronized (cacheClears) {
            cacheClears.clear();
        }
    }


    /**
     * Triggered when a cache mapping is created due to the cache loader being consulted
     */
    public void onLoad(Object key) {
        cacheElementsLoaded.add(key);
    }

    /**
     * Triggered when a cache mapping is created due to calling Cache.put()
     */
    public void onPut(Object key) {
        cacheElementsPut.add(key);
    }

    /**
     * Triggered when a cache mapping is removed due to eviction
     */
    public void onEvict(Object key) {
        cacheElementsEvicted.add(key);
    }

    /**
     * Triggered when a cache mapping is removed due to calling Cache.remove()
     */
    public void onRemove(Object key) {
        cacheElementsRemoved.add(key);
    }

    /**
     * Triggered when an clear occurs
     */
    public void onClear() {
        cacheClears.add(new Date());
    }
}
