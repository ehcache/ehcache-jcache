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


import net.sf.ehcache.Ehcache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Greg Luck
 * @version $Id: CacheManagerTest.java 796 2008-10-09 02:39:03Z gregluck $
 */
public class CacheManagerTest {


    /**
     * the CacheManager Singleton instance
     */
    protected CacheManager manager;

    /**
     * a CacheManager which is created as an instance
     */
    protected CacheManager instanceManager;

    /**
     * Shutdown managers.
     * Check that the manager is removed from CacheManager.ALL_CACHE_MANAGERS
     */
    @After
    public void tearDown() throws Exception {

    }


    /**
     * Tests the constructors.
     * <p/>
     * The factory method and new return different instances.
     * <p/>
     * getInstance always returns the same instance
     */
    @Test
    public void testCacheManagerConstructor() {
        CacheManager cacheManager = new CacheManager();
        CacheManager cacheManager2 = CacheManager.getInstance();
        CacheManager cacheManager3 = CacheManager.getInstance();
        assertTrue(cacheManager != cacheManager2);
        assertTrue(cacheManager2 == cacheManager3);
    }


    /**
     * CacheManager requires a resource called net.sf.jsr107cache.CacheFactory containing the fully
     * qualified class name of a cache factory be at /META-INF/services/net.sf.jsr107cache.CacheFactory.
     *
     * @throws CacheException
     */
    @Test
    public void testLoadCacheFactory() throws CacheException {

        manager = CacheManager.getInstance();
        CacheFactory cacheFactory = manager.getCacheFactory();
        assertNotNull(cacheFactory);
    }


    /**
     * CacheManager requires a resource called net.sf.jsr107cache.CacheFactory containing the fully
     * qualified class name of a cache factory be at /META-INF/services/net.sf.jsr107cache.CacheFactory.
     * <p/>
     * Create a cache using found factory
     *
     * @throws CacheException
     */
    @Test
    public void testCreateCacheFromFactory() throws CacheException {

        manager = CacheManager.getInstance();
        CacheFactory cacheFactory = manager.getCacheFactory();
        assertNotNull(cacheFactory);

        Map config = new HashMap();
        config.put("name", "test");
        config.put("maxElementsInMemory", "10");
        config.put("memoryStoreEvictionPolicy", "LFU");
        config.put("overflowToDisk", "true");
        config.put("eternal", "false");
        config.put("timeToLiveSeconds", "5");
        config.put("timeToIdleSeconds", "5");
        config.put("diskPersistent", "false");
        config.put("diskExpiryThreadIntervalSeconds", "120");

        Cache cache = cacheFactory.createCache(config);
        assertNotNull(cache);

    }

    /**
     * Tests that the CacheManager was successfully created
     */
    @Test
    public void testCreateCacheManager() throws net.sf.ehcache.CacheException {
        manager = CacheManager.getInstance();
        CacheManager singletonManager2 = CacheManager.getInstance();
        assertNotNull(manager);
        assertEquals(manager, singletonManager2);
    }


    /**
     * Tests that the CacheManager was successfully created
     */
    @Test
    public void testRegisterCache() throws net.sf.ehcache.CacheException {
        manager = CacheManager.getInstance();
        Ehcache ehcache = new net.sf.ehcache.Cache("name", 10, MemoryStoreEvictionPolicy.LFU,
                false, null, false, 10, 10, false, 60, null);
        manager.registerCache("test", new JCache(ehcache, null));
        Cache cache = manager.getCache("test");
        assertNotNull(cache);
    }

    /**
     * Tests that we can use a Cache obtained from CacheManager
     */
    @Test
    public void testUseCache() throws net.sf.ehcache.CacheException {
        manager = CacheManager.getInstance();
        Ehcache ehcache = new net.sf.ehcache.Cache("UseCache", 10, MemoryStoreEvictionPolicy.LFU,
                false, null, false, 10, 10, false, 60, null);
        manager.registerCache("test", new JCache(ehcache, null));
        Cache cache = manager.getCache("test");
        assertNotNull(cache);
    }


}
