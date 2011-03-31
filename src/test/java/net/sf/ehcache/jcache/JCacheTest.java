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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import net.sf.ehcache.AbstractCacheTest;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.StopWatch;
import net.sf.ehcache.ThreadKiller;
import net.sf.ehcache.exceptionhandler.CountingExceptionHandler;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.loader.CountingCacheLoader;
import net.sf.ehcache.loader.ExceptionThrowingLoader;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheEntry;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;
import net.sf.jsr107cache.CacheStatistics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for a Cache
 *
 * Since expiration is rounded on seconds, we need to at least go up to the last
 * millisecond before the next second in many of the tests
 *
 * @author Greg Luck
 * @version $Id:JCacheTest.java 318 2007-01-25 01:48:35Z gregluck $
 */
public class JCacheTest extends AbstractCacheTest {

    private static final Logger LOG = LoggerFactory.getLogger(JCacheTest.class);

    /**
     * setup test
     */
    @Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        System.gc();
        Thread.sleep(100);
        System.gc();
        manager.removeCache("test1jcache");
        manager.removeCache("test2jcache");
        manager.removeCache("test3jcache");
        manager.removeCache("test4jcache");
    }


    /**
     * teardown
     * limits to what we can do here under jsr107
     */
    @Override
	@After
    public void tearDown() throws Exception {
        //gettest1jcacheCache().clear();
        //getTest2Cache().clear();
        //getTest4Cache().clear();
        manager.getEhcache("sampleCache1").removeAll();
    }

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
    protected JCache getTest1Cache() throws CacheException {
        Cache cache = CacheManager.getInstance().getCache("test1jcache");
        if (cache == null) {
            //sampleCache1
            Map env = new HashMap();
            env.put("name", "test1jcache");
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
            cache = CacheManager.getInstance().getCacheFactory().createCache(env);
            CacheManager.getInstance().registerCache("test1jcache", cache);
        }
        return (JCache) CacheManager.getInstance().getCache("test1jcache");
    }

    private Cache getTest2Cache() throws CacheException {
        Cache cache = CacheManager.getInstance().getCache("test2jcache");
        if (cache == null) {
            Map env = new HashMap();
            env.put("name", "test2jcache");
            env.put("maxElementsInMemory", "1");
            env.put("overflowToDisk", "true");
            env.put("eternal", "false");
            env.put("timeToLiveSeconds", "1");
            env.put("timeToIdleSeconds", "0");
            cache = CacheManager.getInstance().getCacheFactory().createCache(env);
            CacheManager.getInstance().registerCache("test2jcache", cache);
        }
        return CacheManager.getInstance().getCache("test2jcache");
    }

    private Cache getTest4Cache() throws CacheException {
        Cache cache = CacheManager.getInstance().getCache("test4jcache");
        if (cache == null) {
            Map env = new HashMap();
            env.put("name", "test4jcache");
            env.put("maxElementsInMemory", "1000");
            env.put("overflowToDisk", "true");
            env.put("eternal", "true");
            env.put("timeToLiveSeconds", "0");
            env.put("timeToIdleSeconds", "0");
            cache = CacheManager.getInstance().getCacheFactory().createCache(env);
            CacheManager.getInstance().registerCache("test4jcache", cache);
        }
        return CacheManager.getInstance().getCache("test4jcache");
    }

    /**
     * Checks we cannot use a cache after shutdown
     * test cannot be implemented due to lack of lifecycle support in jsr107
     */
//    @Test public void testUseCacheAfterManagerShutdown() throws CacheException {

    /**
     * Checks we cannot use a cache outside the manager
     * Is the jsr107 silent on whether you can do this?
     */
//    @Test public void testUseCacheOutsideManager() throws CacheException {

    /**
     * Checks when and how we can set the cache name.
     * This is not allowed in jsr107
     */
    //@Test public void testSetCacheName() throws CacheException {

    /**
     * Test using a cache which has been removed and replaced.
     * Is the jsr107 silent on whether you can do this?
     */
//    @Test public void testStaleCacheReference() throws CacheException {

    /**
     * Tests getting the cache name
     * there is no getName method in jsr107
     *
     * @throws Exception
     */
//    @Test public void testCacheWithNoIdle() throws Exception {

    /**
     * Test expiry based on time to live
     * <cache name="sampleCacheNoIdle"
     * maxElementsInMemory="1000"
     * eternal="false"
     * timeToLiveSeconds="5"
     * overflowToDisk="false"
     * />
     */
    @Test
    public void testExpiryBasedOnTimeToLiveWhenNoIdle() throws Exception {
        net.sf.ehcache.Cache ehcache = manager.getCache("sampleCacheNoIdle");
        JCache jCache = new JCache(ehcache);
        manager.replaceEhcacheWithJCache(ehcache, jCache);

        Cache cache = manager.getJCache("sampleCacheNoIdle");
        cache.put("key1", "value1");
        cache.put("key2", "value1");
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));

        //Test time to idle. Should not idle out because not specified
        Thread.sleep(2000);
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));

        //Test time to live.
        Thread.sleep(5020);
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }

    /**
     * Test expiry based on time to live where an Eelment override is set on TTL
     * jsr107 does not support TTL overrides per put.
     */
//    @Test public void testExpiryBasedOnTimeToLiveWhenNoIdleElementOverride() throws Exception {

    /**
     * Test overflow to disk = false
     */
    @Test
    public void testNoOverflowToDisk() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testNoOverflowToDisk", 1, false, false, 500, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);
        cache.put("key1", "value1");
        cache.put("key2", "value1");
        assertNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
    }


    /**
     * Test isEmpty
     */
    @Test
    public void testIsEmpty() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testIsEmpty", 1, true, false, 500, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);
        assertTrue(cache.isEmpty());

        cache.put("key1", "value1");
        assertFalse(cache.isEmpty());
        cache.put("key2", "value1");
        assertFalse(cache.isEmpty());


        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
    }


    /**
     * Create a JCache from an inactive Ehcache and try adding to CacheManager.
     * <p/>
     * Check that getting JCache, Ehcache and Cache all make sense.
     */
    @Test
    public void testEhcacheConstructor() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testJCacheCreation", 1, true, false, 500, 200);
        JCache cache = new JCache(ehcache);
        manager.addCache(cache);

        JCache jCacheFromCacheManager = manager.getJCache("testJCacheCreation");
        assertTrue(jCacheFromCacheManager.isEmpty());

        Ehcache ehcacheFromCacheManager = manager.getEhcache("testJCacheCreation");
        assertEquals(0, ehcacheFromCacheManager.getSize());

        net.sf.ehcache.Cache cacheFromCacheManager = manager.getCache("testJCacheCreation");
        assertEquals(0, cacheFromCacheManager.getSize());

        assertEquals(jCacheFromCacheManager.getBackingCache(), ehcacheFromCacheManager);

        assertEquals(jCacheFromCacheManager.getBackingCache(), cacheFromCacheManager);

        assertEquals(ehcacheFromCacheManager, cacheFromCacheManager);

    }


    /**
     * Test isEmpty
     */
    @Test
    public void testEvict() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testEvict", 1, true, false, 1, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);
        assertTrue(cache.isEmpty());

        cache.put("key1", "value1");
        cache.put("key2", "value1");
        //no invalid
        cache.evict();
        assertFalse(cache.isEmpty());

        Thread.sleep(1999);
        cache.evict();
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));

        cache.put("key1", "value1");
        cache.put("key2", "value1");
        Thread.sleep(1999);
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));

    }

    /**
     * Test containsKey
     */
    @Test
    public void testContainsKey() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testContainsKey", 1, true, false, 500, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);
        assertFalse(cache.containsKey("key1"));
        assertFalse(cache.containsKey("key2"));

        cache.put("key1", "value1");
        assertTrue(cache.containsKey("key1"));
        assertFalse(cache.containsKey("key2"));

        cache.put("key2", "value1");
        assertTrue(cache.containsKey("key1"));
        assertTrue(cache.containsKey("key2"));

        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
    }


    /**
     * Test containsValue
     */
    @Test
    public void testContainsValue() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testContainsValue", 2, true, false, 500, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);
        assertFalse(cache.containsValue("value1"));
        assertFalse(cache.containsValue(null));

        cache.put("key1", null);
        assertFalse(cache.containsValue("value1"));
        assertTrue(cache.containsValue(null));

        cache.put("key2", "value1");
        assertTrue(cache.containsValue("value1"));
        assertTrue(cache.containsValue(null));

        assertNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
    }


    /**
     * Test the get method.
     */
    @Test
    public void testGet() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testGet", 10, true, false, 500, 200);
        manager.addCache(ehcache);
        JCache jcache = new JCache(manager.getCache("sampleCache1"));
        CountingCacheLoader specificCacheLoader = new CountingCacheLoader();
        specificCacheLoader.setName("SpecificCacheLoader");

        //existing entry with dog value, no loader
        jcache.put("key", "dog");
        Object value = jcache.get("key");
        assertEquals("dog", value);

        //existing entry with dog value, with loader
        CountingCacheLoader countingCacheLoader = new CountingCacheLoader();
        jcache.setCacheLoader(countingCacheLoader);
        value = jcache.get("key");
        assertEquals("dog", value);

        //existing entry with dog value, with loader and loaderArgument
        countingCacheLoader = new CountingCacheLoader();
        jcache.setCacheLoader(countingCacheLoader);
        value = jcache.get("key", "1");
        assertEquals("dog", value);

        //no entry with matching key in cache, no loader
        jcache.remove("key");
        jcache.setCacheLoader(null);
        jcache.put("key", null);
        value = jcache.get("key");
        assertNull(value);

        //no entry with matching key in cache, no loader, and loaderArgument
        jcache.remove("key");
        jcache.setCacheLoader(null);
        jcache.put("key", null);
        value = jcache.get("key", "loaderArgument");
        assertNull(value);

        //no entry with matching key in cache, with loader
        jcache.remove("key");
        jcache.setCacheLoader(countingCacheLoader);
        value = jcache.get("key");
        assertEquals(new Integer(0), value);
        jcache.remove("key");
        value = jcache.get("key");
        assertEquals(new Integer(1), value);

        //As above with an overridden loader
        jcache.remove("key");
        jcache.setCacheLoader(countingCacheLoader);
        value = jcache.get("key", new CountingCacheLoader());
        //counter back to 0 because this we overrode with a new loader
        assertEquals(new Integer(0), value);

        //no entry with no matching key in cache, with loader and loaderArgument. Our loader just returns the
        // loader name catendated with the loaderArgument
        jcache.remove("key");
        jcache.setCacheLoader(countingCacheLoader);
        value = jcache.get("key", "argumentValue");
        assertEquals("CountingCacheLoader:argumentValue", value);

        //As above with an overridden loader
        jcache.remove("key");
        jcache.setCacheLoader(countingCacheLoader);
        value = jcache.get("key", specificCacheLoader, "argumentValue");
        assertEquals("SpecificCacheLoader:argumentValue", value);

        //check original still works
        jcache.remove("key");
        jcache.setCacheLoader(countingCacheLoader);
        value = jcache.get("key", "argumentValue");
        assertEquals("CountingCacheLoader:argumentValue", value);

        //cache hit
        jcache.put("key2", "value");
        value = jcache.get("key2");
        assertEquals("value", value);

    }

    /**
     * Test the loader name method.
     */
    @Test
    public void testLoaderName() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testName", 10, true, false, 500, 200);
        manager.addCache(ehcache);
        JCache jcache = new JCache(manager.getCache("sampleCache1"));

        //existing entry with dog value, with loader
        CountingCacheLoader countingCacheLoader = new CountingCacheLoader();
        jcache.setCacheLoader(countingCacheLoader);

        assertEquals("CountingCacheLoader", jcache.getCacheLoader().getName());

    }

    /**
     * Test the get values method.
     */
    @Test
    public void testGetValues() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testGetValue", 2, true, false, 500, 200);
        manager.addCache(ehcache);

        CountingCacheLoader countingCacheLoader = new CountingCacheLoader();
        JCache jcache = new JCache(manager.getCache("sampleCache1"), countingCacheLoader);

        /** A class which is not Serializable */
        class NonSerializable {
        }

        List keys = new ArrayList();
        for (int i = 0; i < 1000; i++) {
            keys.add(new Integer(i));
        }
        
        jcache.loadAll(keys);
        jcache.put(new Integer(1), new Date());
        jcache.put(new Integer(2), new NonSerializable());
        Thread.sleep((long) (3000 * StopWatch.getSpeedAdjustmentFactor()));
        assertEquals(1000, jcache.size());

        Collection values = jcache.values();
        assertEquals(1000, values.size());

    }

    /**
     * Tests putAll
     */
    @Test
    public void testPutAll() {
        Ehcache ehcache = new net.sf.ehcache.Cache("testPutAll", 2, true, false, 500, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);
        assertTrue(cache.isEmpty());

        cache.putAll(null);
        assertTrue(cache.isEmpty());

        cache.putAll(new HashMap());
        assertTrue(cache.isEmpty());

        Map map = new HashMap();
        for (int i = 0; i < 10; i++) {
            map.put(i + "", new Date());
        }

        cache.putAll(map);
        assertEquals(10, cache.size());
    }

    /**
     * Tests clear()
     */
    @Test
    public void testClear() {
        Ehcache ehcache = new net.sf.ehcache.Cache("testClear", 2, true, false, 500, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        assertTrue(cache.isEmpty());
        cache.clear();
        assertTrue(cache.isEmpty());

        cache.put("1", new Date());
        cache.clear();
        assertTrue(cache.isEmpty());
    }


    /**
     * Test the keyset method
     */
    @Test
    public void testKeySet() {
        Ehcache ehcache = new net.sf.ehcache.Cache("testKeySet", 2, true, false, 500, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        for (int i = 0; i < 10; i++) {
            cache.put(i + "", new Date());
        }
        //duplicate
        cache.put(0 + "", new Date());
        Set set = cache.keySet();
        assertEquals(10, set.size());
    }


    /**
     * Performance tests for a range of Memory Store - Disk Store combinations.
     * <p/>
     * This demonstrates that a memory only store is approximately an order of magnitude
     * faster than a disk only store.
     * <p/>
     * It also shows that double the performance of a Disk Only store can be obtained
     * with a maximum memory size of only 1. Accordingly a Cache created without a
     * maximum memory size of less than 1 will issue a warning.
     * <p/>
     * Threading changes were made in v1.41 of DiskStore. The before and after numbers are shown.
     */
    @Test
    public void testProportionMemoryAndDiskPerformance() throws Exception {
        StopWatch stopWatch = new StopWatch();
        long time = 0;

        //Memory only Typical 192ms
        Ehcache ehcache = new net.sf.ehcache.Cache("testMemoryOnly", 5000, false, false, 5, 2);
        manager.addCache(ehcache);
        Cache memoryOnlyCache = new JCache(ehcache);

        time = stopWatch.getElapsedTime();
        for (int i = 0; i < 5000; i++) {
            Integer key = new Integer(i);
            memoryOnlyCache.put(new Integer(i), "value");
            memoryOnlyCache.get(key);
        }
        time = stopWatch.getElapsedTime();
        LOG.info("Time for MemoryStore: " + time);
        assertTrue("Time to put and get 5000 entries into MemoryStore", time < 300);

        //Set size so that all elements overflow to disk.
        // 1245 ms v1.38 DiskStore
        // 273 ms v1.42 DiskStore
        Ehcache diskOnlyEhcache = new net.sf.ehcache.Cache("testDiskOnly", 0, true, false, 5, 2);
        manager.addCache(diskOnlyEhcache);
        Cache diskOnlyCache = new JCache(diskOnlyEhcache);
        time = stopWatch.getElapsedTime();
        for (int i = 0; i < 5000; i++) {
            Integer key = new Integer(i);
            diskOnlyCache.put(key, "value");
            diskOnlyCache.get(key);
        }
        time = stopWatch.getElapsedTime();
        LOG.info("Time for DiskStore: " + time);
        assertTrue("Time to put and get 5000 entries into DiskStore was less than 2 sec", time < 2000);

        // 1 Memory, 999 Disk
        // 591 ms v1.38 DiskStore
        // 56 ms v1.42 DiskStore
        Ehcache m1d999Ehcache = new net.sf.ehcache.Cache("m1d999Cache", 1, true, false, 5, 2);
        manager.addCache(m1d999Ehcache);
        Cache m1d999Cache = new JCache(m1d999Ehcache);
        time = stopWatch.getElapsedTime();
        for (int i = 0; i < 5000; i++) {
            Integer key = new Integer(i);
            m1d999Cache.put(key, "value");
            m1d999Cache.get(key);
        }
        time = stopWatch.getElapsedTime();
        LOG.info("Time for m1d999Cache: " + time);
        assertTrue("Time to put and get 5000 entries into m1d999Cache", time < 2000);

        // 500 Memory, 500 Disk
        // 669 ms v1.38 DiskStore
        // 47 ms v1.42 DiskStore
        Ehcache m500d500Ehcache = new net.sf.ehcache.Cache("m500d500Cache", 500, true, false, 5, 2);
        manager.addCache(m500d500Ehcache);
        Cache m500d500Cache = new JCache(m500d500Ehcache);

        time = stopWatch.getElapsedTime();
        for (int i = 0; i < 5000; i++) {
            Integer key = new Integer(i);
            m500d500Cache.put(key, "value");
            m500d500Cache.get(key);
        }
        time = stopWatch.getElapsedTime();
        LOG.info("Time for m500d500Cache: " + time);
        assertTrue("Time to put and get 5000 entries into m500d500Cache", time < 2000);

    }

    /**
     * Test Caches with persistent stores dispose properly. Tests:
     * <ol>
     * <li>No exceptions are thrown on dispose
     * <li>You cannot re add a cache after it has been disposed and removed
     * <li>You can create a new cache with the same name
     * </ol>
     * jsr107 does not support lifecycles.
     */
//    @Test public void testCreateAddDisposeAdd() throws CacheException {

    /**
     * Test expiry based on time to live
     */
    @Test
    public void testExpiryBasedOnTimeToLive() throws Exception {
        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("testExpiryBasedOnTimeToLive", 1, true, false, 3, 0);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        cache.put("key1", "value1");
        cache.put("key2", "value1");

        //Test time to live
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        Thread.sleep(1020);
        //Test time to live
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        Thread.sleep(1020);
        //Test time to live
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        Thread.sleep(1959);
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }

    /**
     * Test expiry based on time to live where the TTL is set in the put
     */
    @Test
    public void testExpiryBasedOnTimeToLiveTTL() throws Exception {

        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("testExpiryBasedOnTimeToLiveTTL", 1, true, false, 3, 0);
        manager.addCache(ehcache);
        JCache cache = new JCache(ehcache);

        cache.put("key1", "value1", 1);
        //default
        cache.put("key2", "value1", 0);

        //Test time to live
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));

        Thread.sleep(1999);
        //Test time to live
        assertNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));

        Thread.sleep(2000);
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));

    }


    /**
     * Test expiry based on time to live.
     * This test uses peek, which behaves the same as get
     */
    @Test
    public void testExpiryBasedOnTimeToLiveUsingPeek() throws Exception {
        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("testExpiryBasedOnTimeToLiveUsingPeek", 1, true, false, 3, 0);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        cache.put("key1", "value1");
        cache.put("key2", "value1");

        //Test time to live
        assertNotNull(cache.peek("key1"));
        assertNotNull(cache.peek("key2"));
        Thread.sleep(1020);
        //Test time to live
        assertNotNull(cache.peek("key1"));
        assertNotNull(cache.peek("key2"));
        Thread.sleep(1020);
        //Test time to live
        assertNotNull(cache.peek("key1"));
        assertNotNull(cache.peek("key2"));
        Thread.sleep(1969);
        assertNull(cache.peek("key1"));
        assertNull(cache.peek("key2"));
    }

//    /**
//     * Tests that a cache created from defaults will expire as per
//     * the default expiry policy.
//     * Caches cannot be created from default in jsr107
//     */
//    @Test public void testExpiryBasedOnTimeToLiveForDefault() throws Exception {

    /**
     * Test expiry based on time to live.
     * <p/>
     * Elements are put quietly back into the cache after being cloned.
     * The elements should expire as if the putQuiet had not happened.
     * jsr107
     */
//    @Test public void testExpiryBasedOnTimeToLiveAfterPutQuiet() throws Exception {


    /**
     * Test expiry based on time to live
     */
    @Test
    public void testNoIdleOrExpiryBasedOnTimeToLiveForEternal() throws Exception {
        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("testNoIdleOrExpiryBasedOnTimeToLiveForEternal", 1, true, false, 5, 0);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        cache.put("key1", "value1");
        cache.put("key2", "value1");

        //Test time to live
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));

        //Check that we did not idle out
        Thread.sleep(1000);
        assertNotNull(cache.get("key1"));
        assertNotNull("Key should not be null", cache.get("key2"));

        //Check that we did not expire out
        Thread.sleep(6000);
        assertNull("Key should be null", cache.get("key1"));
        assertNull("Key should be null", cache.get("key2"));
    }

    /**
     * Test expiry based on time to idle.
     */
    @Test
    public void testExpiryBasedOnTimeToIdle() throws Exception {
        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("testExpiryBasedOnTimeToIdle", 1, true, false, 6, 2);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        cache.put("key1", "value1");
        cache.put("key2", "value1");

        //Test time to idle
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        Thread.sleep(2999);
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));

        //Test effect of get
        cache.put("key1", "value1");
        cache.put("key2", "value1");
        Thread.sleep(1020);
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));

        Thread.sleep(3999);
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }

    /**
     * Test expiry based on time to idle.
     * jsr107 has no put quiet
     */
//    @Test public void testExpiryBasedOnTimeToIdleAfterPutQuiet() throws Exception {

    /**
     * Test element statistics, including get and getQuiet
     * eternal="false"
     * timeToIdleSeconds="5"
     * timeToLiveSeconds="10"
     * overflowToDisk="true"
     * <p/>
     * jsr107 has no put quiet
     */
    @Test
    public void testElementStatistics() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testElementStatistics", 1, true, false, 5, 2);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        cache.put("key1", "value1");
        cache.put("key2", "value1");

        CacheEntry cacheEntry = cache.getCacheEntry("key1");
        assertEquals("Should be one", 1, cacheEntry.getHits());

        cacheEntry = cache.getCacheEntry("key1");
        assertEquals("Should be two", 2, cacheEntry.getHits());
    }

    /**
     * Check getting a cache entry where it does not exist
     */
    @Test
    public void testNullCacheEntry() {
        Ehcache ehcache = new net.sf.ehcache.Cache("testNullCacheEntry", 1, true, false, 5, 2);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        cache.put("key1", "value1");

        CacheEntry cacheEntry = cache.getCacheEntry("key1");
        assertNotNull(cacheEntry);
        CacheEntry cacheEntry2 = cache.getCacheEntry("keyDoesNotExist");
        assertNull(cacheEntry2);
    }

    /**
     * Test cache statistics, including get.
     * Reconcile CacheEntry stats with cache stats and make sure they agree
     */
    @Test
    public void testCacheStatistics() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testCacheStatistics", 1, true, false, 5, 2);
        manager.addCache(ehcache);
        ehcache.setStatisticsEnabled(true);
        Cache cache = new JCache(ehcache);
        cache.put("key1", "value1");
        cache.put("key2", "value1");

        CacheEntry cacheEntry = cache.getCacheEntry("key1");
        assertEquals("Should be one", 1, cacheEntry.getHits());
        assertEquals("Should be one", 1, cache.getCacheStatistics().getCacheHits());


        cacheEntry = cache.getCacheEntry("key1");
        assertEquals("Should be one", 2, cacheEntry.getHits());
        assertEquals("Should be one", 2, cache.getCacheStatistics().getCacheHits());

        cacheEntry = cache.getCacheEntry("key2");
        assertEquals("Should be one", 1, cacheEntry.getHits());
        assertEquals("Should be one", 3, cache.getCacheStatistics().getCacheHits());

        assertEquals("Should be 0", 0, cache.getCacheStatistics().getCacheMisses());
        cache.get("doesnotexist");
        assertEquals("Should be 1", 1, cache.getCacheStatistics().getCacheMisses());


    }

    /**
     * Checks that getQuiet works how we expect it to
     * not supported in jsr107
     */
//    @Test public void testGetQuietAndPutQuiet() throws Exception {

    /**
     * Test size with put and remove.
     * <p/>
     * It checks that size makes sense, and also that getKeys.size() matches getSize()
     */
    @Test
    public void testSizeWithPutAndRemove() throws Exception {
        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("testSizeWithPutAndRemove", 1, true, true, 0, 0);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        cache.put("key1", "value1");
        cache.put("key2", "value1");

        int sizeFromGetSize = cache.getCacheStatistics().getObjectCount();
        int sizeFromKeys = cache.keySet().size();
        assertEquals(sizeFromGetSize, sizeFromKeys);
        assertEquals(2, cache.getCacheStatistics().getObjectCount());

        /** A class which is not Serializable */
        class NonSerializable {
        }

        cache.put("key1", new NonSerializable());
        cache.put("key1", "value1");

        //key1 should be in the Disk Store
        assertEquals(cache.getCacheStatistics().getObjectCount(), cache.keySet().size());
        assertEquals(2, cache.getCacheStatistics().getObjectCount());
        //there were two of these, so size will now be one
        cache.remove("key1");
        assertEquals(cache.getCacheStatistics().getObjectCount(), cache.keySet().size());
        assertEquals(1, cache.getCacheStatistics().getObjectCount());
        cache.remove("key2");
        assertEquals(cache.getCacheStatistics().getObjectCount(), cache.keySet().size());
        assertEquals(0, cache.getCacheStatistics().getObjectCount());

        //try null values
        cache.clear();
        Object object1 = new Object();
        Object object2 = new Object();
        cache.put(object1, null);
        cache.put(object2, null);
        //Cannot overflow therefore just one
        assertEquals(1, cache.getCacheStatistics().getObjectCount());
        Object nullValue = cache.get(object2);
        assertNull(nullValue);

    }

    /**
     * Test getKeys after expiry
     * <p/>
     * Makes sure that if an element is expired, its key should also be expired
     */
    @Test
    public void testGetKeysAfterExpiry() throws Exception {
        //Set size so the second element overflows to disk.
        Cache cache = getTest2Cache();
        String key1 = "key1";
        cache.put(key1, "value1");
        cache.put("key2", "value1");
        //getSize uses getKeys().size(), so these should be the same
        assertEquals(cache.getCacheStatistics().getObjectCount(), cache.keySet().size());
        //getKeys does not do an expiry check, so the expired elements are counted
        assertEquals(2, cache.getCacheStatistics().getObjectCount());
        String keyFromDisk = (String) cache.getCacheEntry(key1).getKey();
        assertTrue(key1.equals(keyFromDisk));
        Thread.sleep(1999);
        assertEquals(2, cache.keySet().size());
        //getKeysWithExpiryCheck does check and gives the correct answer of 0
        Ehcache ehcache = ((JCache) cache).getBackingCache();
        ehcache.setStatisticsAccuracy(CacheStatistics.STATISTICS_ACCURACY_GUARANTEED);
        assertEquals(0, cache.getCacheStatistics().getObjectCount());
    }

    /**
     * Test size after multiple calls, with put and remove
     */
    @Test
    public void testSizeMultipleCallsWithPutAndRemove() throws Exception {
        //Set size so the second element overflows to disk.
        //Cache cache = new Cache("test3jcache", 1, true, true, 0, 0);
        Cache cache = getTest2Cache();
        cache.put("key1", "value1");
        cache.put("key2", "value1");

        //key1 should be in the Disk Store
        assertEquals(2, cache.getCacheStatistics().getObjectCount());
        assertEquals(2, cache.getCacheStatistics().getObjectCount());
        assertEquals(2, cache.getCacheStatistics().getObjectCount());
        assertEquals(2, cache.getCacheStatistics().getObjectCount());
        assertEquals(2, cache.getCacheStatistics().getObjectCount());
        cache.remove("key1");
        assertEquals(1, cache.getCacheStatistics().getObjectCount());
        assertEquals(1, cache.getCacheStatistics().getObjectCount());
        assertEquals(1, cache.getCacheStatistics().getObjectCount());
        assertEquals(1, cache.getCacheStatistics().getObjectCount());
        assertEquals(1, cache.getCacheStatistics().getObjectCount());
        cache.remove("key2");
        assertEquals(0, cache.getCacheStatistics().getObjectCount());
        assertEquals(0, cache.getCacheStatistics().getObjectCount());
        assertEquals(0, cache.getCacheStatistics().getObjectCount());
        assertEquals(0, cache.getCacheStatistics().getObjectCount());
        assertEquals(0, cache.getCacheStatistics().getObjectCount());
    }

    /**
     * Checks the expense of checking for duplicates
     * JSR107 has only one keyset command. It returns a Set rather than a list, so
     * duplicates are automatically handled.
     * <p/>
     * 31ms for 2000 keys, half in memory and half on disk
     * <p/>
     */
    @Test
    public void testGetKeysPerformance() throws Exception {
        Cache cache = getTest2Cache();

        for (int i = 0; i < 2000; i++) {
            cache.put("key" + i, "value");
        }
        //Add some duplicates
        cache.put("key0", "value");
        cache.put("key1", "value");
        cache.put("key2", "value");
        cache.put("key3", "value");
        cache.put("key4", "value");
        //let the spool be written
        Thread.sleep(1000);
        StopWatch stopWatch = new StopWatch();
        Set keys = cache.keySet();
        assertTrue("Should be 2000 keys. ", keys.size() == 2000);
        long getKeysTime = stopWatch.getElapsedTime();

        LOG.info("Time to get 2000 keys: With Duplicate Check: " + getKeysTime);
        assertTrue("Getting keys took more than 200ms: " + getKeysTime, getKeysTime < 100);
    }

    /**
     * Checks the expense of checking in-memory size
     * 3467890 bytes in 1601ms for JDK1.4.2
     * N/A to jsr107
     */
//    @Test public void testCalculateInMemorySizePerformanceAndReasonableness() throws Exception {


    /**
     * Expire elements and verify size is correct.
     */
    @Test
    public void testGetSizeAfterExpiry() throws Exception {
        //Set size so the second element overflows to disk.
        Cache cache = getTest2Cache();
        cache.put("key1", "value1");
        cache.put("key2", "value1");

        //Let the idle expire
        Thread.sleep(1999);
        assertEquals(null, cache.get("key1"));
        assertEquals(null, cache.get("key2"));

        assertEquals(0, cache.getCacheStatistics().getObjectCount());
    }

    /**
     * Tests initialisation failures
     * jsr107 has no lifecycle management
     */
//    @Test public void testInitialiseFailures() {


    /**
     * Tests putting nulls throws correct exception
     *
     * @throws Exception
     */
    @Test
    public void testNullTreatment() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testNullTreatment", 1, false, false, 5, 1);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        try {
            // Disabled since memory store doesn't support null keys anymore
//            cache.put(null, null);
//            assertNull(cache.get(null));
//            cache.put(null, "value");
//            assertEquals("value", cache.get(null));
            cache.put("key", null);
            assertEquals(null, cache.get("key"));
            assertFalse(null instanceof Serializable);
        } catch (Exception e) {
        	e.printStackTrace();
            fail("Should not have thrown an Execption");
        }
    }

    /**
     * Tests cache, memory store and disk store sizes from config
     * jsr107 does not breakdowns of store sizes.
     */
    @Test
    public void testSizes() throws Exception {
        Cache cache = getTest1Cache();
        assertEquals(0, cache.getCacheStatistics().getObjectCount());

        for (int i = 0; i < 10010; i++) {
            cache.put("key" + i, "value1");
        }
        assertEquals(10010, cache.getCacheStatistics().getObjectCount());

        //NonSerializable
        cache.put(new Object(), Object.class);

        assertEquals(10011, cache.getCacheStatistics().getObjectCount());

        cache.remove("key4");
        cache.remove("key3");

        assertEquals(10009, cache.getCacheStatistics().getObjectCount());

        cache.clear();
        assertEquals(0, cache.getCacheStatistics().getObjectCount());

    }

    /**
     * Tests flushing the cache
     * jsr107 does not specify a disk store and therefore does not have a flush.
     */
//    @Test public void testFlushWhenOverflowToDisk() throws Exception {

    /**
     * Tests put works correctly for Elements with overriden TTL
     * jsr107 does not support overriding TTL on a per entry basis
     *
     */
//    @Test public void testPutWithOverriddenTTLAndTTI() throws Exception {


    /**
     * Tests using elements with null values. They should work as normal.
     *
     * @throws Exception
     */
    @Test
    public void testNonSerializableElement() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testElementWithNonSerializableValue", 1, true, false, 100, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        cache.put("key1", new Object());
        cache.put("key2", new Object());

        //Removed because could not overflow
        assertNull(cache.get("key1"));

        //Second one should be in the MemoryStore and retrievable
        assertNotNull(cache.get("key2"));
    }


    /**
     * Tests what happens when an Element throws an Error on serialization. This mimics
     * what a nasty error like OutOfMemoryError could do.
     * <p/>
     * Before a change to the SpoolAndExpiryThread to handle this situation this test failed and generated
     * the following log message.
     * Jun 28, 2006 7:17:16 PM net.sf.ehcache.store.DiskStore put
     * SEVERE: testThreadKillerCache: Elements cannot be written to disk store because the spool thread has died.
     *
     * @throws Exception
     */
    @Test
    public void testSpoolThreadHandlesThreadKiller() throws Exception {
        Ehcache ehcache = new net.sf.ehcache.Cache("testThreadKiller", 0, true, false, 100, 200);
        manager.addCache(ehcache);
        Cache cache = new JCache(ehcache);

        cache.put("key", new ThreadKiller());
        Thread.sleep(2999);
        cache.put("key1", "one");
        cache.put("key2", "two");

        Thread.sleep(2999);

        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
    }

    /**
     * Tests disk store and memory store size
     * jsr107 does not support getting store sizes
     */
//    @Test public void testGetDiskStoreSize() throws Exception {

    /**
     * Tests that attempting to clone a cache fails with the right exception.
     * jsr107 does not make clone available
     *
     */
//    @Test public void testCloneFailures() throws Exception {


    /**
     * Tests that the toString() method works.
     */
    @Test
    public void testToString() throws CacheException {
        Cache cache = getTest2Cache();
        cache.clear();
        LOG.info(cache.toString());
        assertTrue(cache.toString().indexOf("test2jcache") > -1);
        assertTrue(380 < cache.toString().length());
    }

    /**
     * When does equals mean the same thing as == for an element?
     * NA JSR107 does not have elements
     */
//    @Test public void testEquals() throws CacheException, InterruptedException {

    /**
     * Tests the uniqueness of the GUID
     * Not part of jsr107
     */
//    @Test public void testGuid() {

    /**
     * Does the Object API work?
     * jsr107 is an object API
     */
    @Test
    public void testAPIObjectCompatibility() throws CacheException {
        Cache cache = getTest1Cache();

        Object objectKey = new Object();
        Object objectValue = new Object();

        cache.put(objectKey, objectValue);

        //Cannot get it back using get
        Object retrievedElement = cache.get(objectKey);
        assertNotNull(retrievedElement);

        //Test that equals works
        assertEquals(objectValue, retrievedElement);

    }


    /**
     * Does the Serializable API work?
     */
    @Test
    public void testAPISerializableCompatibility() throws CacheException {
        //Set size so the second element overflows to disk.
        Cache cache = getTest2Cache();

        //Try object compatibility
        Serializable key = new String("key");
        Serializable serializableValue = new String("retrievedValue");
        cache.put(key, serializableValue);
        cache.put("key2", serializableValue);
        Object retrievedValue = cache.get(key);
        assertEquals(serializableValue, retrievedValue);
    }

    /**
     * Multi-thread read-write test with lots of threads
     * Just use MemoryStore to put max stress on cache
     * Values that work:
     * <p/>
     * The get here will often load data, so it does not give raw cache performance. See the similar test in CacheTest
     * for that.
     */
    @Test
    public void testReadWriteThreads() throws Exception {

        final int size = 10000;
        final int maxTime = (int) (2500 * StopWatch.getSpeedAdjustmentFactor());
        final JCache cache = getTest1Cache();

        CountingCacheLoader countingCacheLoader = (CountingCacheLoader) cache.getCacheLoader();


        long start = System.currentTimeMillis();
        final List executables = new ArrayList();
        final Random random = new Random();

        for (int i = 0; i < size; i++) {
            cache.put("" + i, "value");
        }

        // 50% of the time get data
        for (int i = 0; i < 26; i++) {
            final Executable executable = new Executable() {
                public void execute() throws Exception {
                    final StopWatch stopWatch = new StopWatch();
                    long start = stopWatch.getElapsedTime();
                    cache.get("key" + random.nextInt(size));
                    long end = stopWatch.getElapsedTime();
                    long elapsed = end - start;
                    assertTrue("Get time outside of allowed range: " + elapsed, elapsed < maxTime);
                }
            };
            executables.add(executable);
        }

        //25% of the time add data
        for (int i = 0; i < 10; i++) {
            final Executable executable = new Executable() {
                public void execute() throws Exception {
                    final StopWatch stopWatch = new StopWatch();
                    long start = stopWatch.getElapsedTime();
                    cache.put("key" + random.nextInt(size), "value");
                    long end = stopWatch.getElapsedTime();
                    long elapsed = end - start;
                    assertTrue("Put time outside of allowed range: " + elapsed, elapsed < maxTime);
                }
            };
            executables.add(executable);
        }

        //25% of the time remove the data
        for (int i = 0; i < 10; i++) {
            final Executable executable = new Executable() {
                public void execute() throws Exception {
                    final StopWatch stopWatch = new StopWatch();
                    long start = stopWatch.getElapsedTime();
                    cache.remove("key" + random.nextInt(size));
                    long end = stopWatch.getElapsedTime();
                    long elapsed = end - start;
                    assertTrue("Remove time outside of allowed range: " + elapsed, elapsed < maxTime);
                }
            };
            executables.add(executable);
        }

        //some of the time clear the data
        for (int i = 0; i < 10; i++) {
            final Executable executable = new Executable() {
                public void execute() throws Exception {
                    final StopWatch stopWatch = new StopWatch();
                    long start = stopWatch.getElapsedTime();
                    int randomInteger = random.nextInt(20);
                    if (randomInteger == 3) {
                        cache.clear();
                    }
                    long end = stopWatch.getElapsedTime();
                    long elapsed = end - start;
                    assertTrue("RemoveAll time outside of allowed range: " + elapsed, elapsed < maxTime);
                }
            };
            executables.add(executable);
        }

        //some of the time exercise the loaders through their various methods. Loader methods themselves make no performance
        //guarantees. They should only lock the cache when doing puts and gets, which the time limits on the other threads
        //will check for.
        for (int i = 0; i < 4; i++) {
            final Executable executable = new Executable() {
                public void execute() throws Exception {
                    int randomInteger = random.nextInt(20);
                    List keys = new ArrayList();
                    for (int i = 0; i < 2; i++) {
                        keys.add("key" + random.nextInt(size));
                    }
                    if (randomInteger == 1) {
                        cache.load("key" + random.nextInt(size));
                    }
                    if (randomInteger == 2) {
                        cache.loadAll(keys, null);
                    }
                    //JCache delegates to ehcache's getWithLoader so the get tests above do this already
                    if (randomInteger == 4) {
                        cache.getAll(keys, null);
                    }
                }
            };
            executables.add(executable);
        }


        runThreads(executables);
        long end = System.currentTimeMillis();
        LOG.info("Total time for the test: " + (end - start) + " ms");
        LOG.info("Total loads: " + countingCacheLoader.getLoadCounter());
        LOG.info("Total loadAlls: " + countingCacheLoader.getLoadAllCounter());
    }


    /**
     * Tests the public API load method with a single item
     */
    @Test
    public void testLoad() throws InterruptedException, ExecutionException, CacheException {

        CountingCacheLoader countingCacheLoader = new CountingCacheLoader();
        JCache jcache = new JCache(manager.getCache("sampleCache1"), countingCacheLoader);
        jcache.load("key1");
        Thread.sleep(500);
        assertEquals(1, jcache.size());
        assertEquals(1, countingCacheLoader.getLoadCounter());
    }

    /**
     * Tests that the setLoader method allows the loader to be changed. The load is async so timing is important.
     */
    @Test
    public void testLoadWithDynamicLoaderInjection() throws InterruptedException, ExecutionException, CacheException {

        //null loader so no loading happens
        JCache jCache = new JCache(manager.getCache("sampleCache1"));
        List<CacheLoader> list = new ArrayList<CacheLoader>(jCache.getBackingCache().getRegisteredCacheLoaders());
        for (CacheLoader cacheLoader : list) {
            jCache.getBackingCache().unregisterCacheLoader(cacheLoader);
        }

        jCache.setCacheLoader(null);
        assertEquals(0, jCache.size());
        jCache.load("key1");
        assertEquals(0, jCache.size());


        CountingCacheLoader countingCacheLoader = new CountingCacheLoader();
        jCache.setCacheLoader(countingCacheLoader);

        jCache.load("key1");
        Thread.sleep(1000);

        assertEquals(1, jCache.size());
        assertEquals(1, countingCacheLoader.getLoadCounter());
    }

    /**
     * Test exception handling from JCache
     */
    @Test
    public void testCacheExceptionHandler() {
        JCache jcache = manager.getJCache("exceptionHandlingCache");

        jcache.setCacheLoader(new ExceptionThrowingLoader());

        //throws an exception
        jcache.get("key");

        assertEquals(1, CountingExceptionHandler.HANDLED_EXCEPTIONS.size());
        assertEquals("key", ((CountingExceptionHandler.HandledException) CountingExceptionHandler.HANDLED_EXCEPTIONS.get(0)).getKey());
        assertTrue((((CountingExceptionHandler.HandledException) CountingExceptionHandler.HANDLED_EXCEPTIONS
                .get(0)).getException()) instanceof RuntimeException);
    }

    /**
     * Tests the loadAll Public API method
     */
    @Test
    public void testLoadAll() throws InterruptedException, ExecutionException, CacheException {

        CountingCacheLoader countingCacheLoader = new CountingCacheLoader();
        JCache jcache = new JCache(manager.getCache("sampleCache1"), countingCacheLoader);

        //check null works ok
        jcache.loadAll(null);

        List keys = new ArrayList();
        for (int i = 0; i < 999; i++) {
            keys.add(new Integer(i));
        }

        //pre populate only 1 element
        jcache.put(new Integer(1), "");

        jcache.loadAll(keys);
        Thread.sleep((long) (3000 * StopWatch.getSpeedAdjustmentFactor()));

        assertEquals(999, jcache.size());
        assertEquals(998, countingCacheLoader.getLoadAllCounter());

    }

    /**
     * Tests the getAll Public API method
     */
    @Test
    public void testGetAll() throws InterruptedException, ExecutionException, CacheException {

        JCache jcache = new JCache(manager.getCache("sampleCache1"));

        //check null CacheLoader
        Map map = jcache.getAll(null);
        assertNotNull(map);
        assertEquals(0, map.size());


        CountingCacheLoader countingCacheLoader = new CountingCacheLoader();
        jcache.setCacheLoader(countingCacheLoader);

        //check null
        map = jcache.getAll(null);
        assertNotNull(map);
        assertEquals(0, map.size());


        List keys = new ArrayList();
        for (int i = 0; i < 1000; i++) {
            keys.add(new Integer(i));
        }

        //pre populate only 1 element
        jcache.put(new Integer(1), "");

        Map result = jcache.getAll(keys);
        Object object = result.get(new Integer(1));
        //make sure we are not wrapping with Element.
        assertFalse(object instanceof Element);


        assertEquals(1000, jcache.size());
        assertEquals(999, countingCacheLoader.getLoadAllCounter());

        assertFalse(result.get(new Integer(1)) == null);
        assertFalse(result.get(new Integer(1)) instanceof Element);
    }


}
