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

import net.sf.ehcache.AbstractCacheTest;
import net.sf.ehcache.loader.CountingCacheLoader;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Greg Luck
 * @version $Id: JCacheFactoryTest.java 796 2008-10-09 02:39:03Z gregluck $
 */
public class JCacheFactoryTest extends AbstractCacheTest {


    /**
     * Gets the sample cache 1
     * <cache name="sampleCache1"
     * maxElementsInMemory="10000"
     * maxElementsOnDisk="1000"
     * eternal="false"
     * timeToIdleSeconds="360"
     * timeToLiveSeconds="1000"
     * overflowToDisk="true"
     * memoryStoreEvictionPolicy="LRU">
     * <cacheEventListenerFactory class="net.sf.ehcache.event.NullCacheEventListenerFactory"/>
     * </cache>
     */
    @Test
    public void testFactoryUsingCompleteEnvironment() throws CacheException {

        //sampleCache1
        Map env = new HashMap();
        env.put("name", "test1factory");
        env.put("maxElementsInMemory", "10000");
        env.put("maxElementsOnDisk", "1000");
        env.put("memoryStoreEvictionPolicy", "LRU");
        env.put("overflowToDisk", "true");
        env.put("eternal", "false");
        env.put("timeToLiveSeconds", "1000");
        env.put("timeToIdleSeconds", "1000");
        env.put("diskPersistent", "false");
        env.put("diskExpiryThreadIntervalSeconds", "120");
        env.put("cacheLoaderFactoryClassName", "net.sf.ehcache.loader.CountingCacheLoaderFactory");
        Cache cache = CacheManager.getInstance().getCacheFactory().createCache(env);
        CacheManager.getInstance().registerCache("factoryTest1", cache);
        assertEquals(((JCache) cache).getCacheLoader().getClass(), CountingCacheLoader.class);
        assertNotNull(CacheManager.getInstance().getCache("factoryTest1"));
    }

    /**
     * A Null cacheloader factory string should not attempt to create it.
     *
     * @throws CacheException
     */
    @Test
    public void testFactoryWithNullCacheLoaderFactory() throws CacheException {
        Cache cache = CacheManager.getInstance().getCache("test2");
        if (cache == null) {
            Map env = new HashMap();
            env.put("name", "test2");
            env.put("maxElementsInMemory", "1");
            env.put("overflowToDisk", "true");
            env.put("eternal", "false");
            env.put("timeToLiveSeconds", "1");
            env.put("timeToIdleSeconds", "0");
            env.put("cacheLoaderFactoryClassName", null);
            cache = CacheManager.getInstance().getCacheFactory().createCache(env);
            CacheManager.getInstance().registerCache("factoryTest2", cache);
            assertNull(((JCache) cache).getCacheLoader());
            assertNotNull(CacheManager.getInstance().getCache("factoryTest2"));
        }
    }

    /**
     * Specifying a CacheLoaderFactory which cannot be found should throw an CacheException
     *
     * @throws CacheException
     */
    @Test
    public void testFactoryWithWrongCacheLoaderFactory() throws CacheException {
        Cache cache = CacheManager.getInstance().getCache("test4");
        if (cache == null) {
            Map env = new HashMap();
            env.put("name", "test4");
            env.put("maxElementsInMemory", "1000");
            env.put("overflowToDisk", "true");
            env.put("eternal", "true");
            env.put("timeToLiveSeconds", "0");
            env.put("timeToIdleSeconds", "0");
            env.put("cacheLoaderFactoryClassName", "net.sf.ehcache.jcache.JCacheTest.Typo");

            try {
                cache = CacheManager.getInstance().getCacheFactory().createCache(env);
            } catch (CacheException e) {
                //expected
            }

        }
    }


}
