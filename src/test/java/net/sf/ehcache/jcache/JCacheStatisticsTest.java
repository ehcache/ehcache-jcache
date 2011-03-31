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
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheStatistics;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Tests for the statistics class
 *
 * @author Greg Luck
 * @version $Id:JCacheStatisticsTest.java 318 2007-01-25 01:48:35Z gregluck $
 */
public class JCacheStatisticsTest extends AbstractCacheTest {


    private static final Logger LOG = LoggerFactory.getLogger(JCacheStatisticsTest.class);

    /**
     * Test statistics directly from Statistics Object
     */
    @Test
    public void testStatisticsFromStatisticsObject() throws InterruptedException {
        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("testStatistics", 1, true, false, 5, 2);
        manager.addCache(ehcache);
        ehcache.setStatisticsEnabled(true);
        JCache cache = new JCache(ehcache, null);
        exerciseStatistics(cache);

        //Exercise aftter setting accuracy
        ehcache = new net.sf.ehcache.Cache("testStatistics2", 1, true, false, 5, 2);
        manager.addCache(ehcache);
        ehcache.setStatisticsEnabled(true);
        cache = new JCache(ehcache, null);
        cache.setStatisticsAccuracy(CacheStatistics.STATISTICS_ACCURACY_NONE);
        exerciseStatistics(cache);

        ehcache = new net.sf.ehcache.Cache("testStatistics4", 1, true, false, 5, 2);
        manager.addCache(ehcache);
        ehcache.setStatisticsEnabled(true);
        cache = new JCache(ehcache, null);
        cache.setStatisticsAccuracy(CacheStatistics.STATISTICS_ACCURACY_BEST_EFFORT);
        exerciseStatistics(cache);

    }

    private void exerciseStatistics(Cache cache) throws InterruptedException {
        cache.put("key1", "value1");
        cache.put("key2", "value1");
        //key1 should be in the Disk Store
        cache.get("key1");

        CacheStatistics statistics = cache.getCacheStatistics();
        assertEquals(1, statistics.getCacheHits());
        assertEquals(0, statistics.getCacheMisses());

        //key 1 should now be in the LruMemoryStore
        cache.get("key1");

        statistics = cache.getCacheStatistics();
        assertEquals(2, statistics.getCacheHits());
        assertEquals(0, statistics.getCacheMisses());

        //Let the idle expire
        Thread.sleep(5020);

        //key 1 should now be expired
        cache.get("key1");
        statistics = cache.getCacheStatistics();
        assertEquals(2, statistics.getCacheHits());
        assertEquals(1, statistics.getCacheMisses());
        assertNotNull(statistics.toString());
    }


    /**
     * CacheStatistics should always be sensible when the cache has not started.
     */
    @Test
    public void testCacheStatisticsDegradesElegantlyWhenCacheDisposed() {
        Ehcache ehcache = new net.sf.ehcache.Cache("test", 1, true, false, 5, 2);
        Cache cache = new JCache(ehcache, null);
        try {
            CacheStatistics statistics = cache.getCacheStatistics();
            fail();
        } catch (IllegalStateException e) {
            assertEquals("The test Cache is not alive.", e.getMessage());
        }

    }


    /**
     * We want to be able to use Statistics as a value object.
     * We need to do some magic with the reference held to Cache
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {

        Ehcache ehcache = new net.sf.ehcache.Cache("test", 1, true, false, 5, 2);
        manager.addCache(ehcache);
        ehcache.setStatisticsEnabled(true);
        Cache cache = new JCache(ehcache, null);
        cache.put("key1", "value1");
        cache.put("key2", "value1");
        cache.get("key1");
        cache.get("key1");

        CacheStatistics statistics = cache.getCacheStatistics();
        assertEquals(2, statistics.getCacheHits());
        assertEquals(0, statistics.getCacheMisses());
        assertEquals(CacheStatistics.STATISTICS_ACCURACY_BEST_EFFORT, statistics.getStatisticsAccuracy());
        statistics.clearStatistics();


        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bout);
        oos.writeObject(statistics);
        byte[] serializedValue = bout.toByteArray();
        oos.close();
        CacheStatistics afterDeserializationStatistics = null;
        ByteArrayInputStream bin = new ByteArrayInputStream(serializedValue);
        ObjectInputStream ois = new ObjectInputStream(bin);
        afterDeserializationStatistics = (CacheStatistics) ois.readObject();
        ois.close();

        //Check after Serialization
        assertEquals(2, afterDeserializationStatistics.getCacheHits());
        assertEquals(0, afterDeserializationStatistics.getCacheMisses());
        assertEquals(CacheStatistics.STATISTICS_ACCURACY_BEST_EFFORT, statistics.getStatisticsAccuracy());
        statistics.clearStatistics();

    }


    /**
     * Test statistics directly from Statistics Object
     */
    @Test
    public void testClearStatistics() throws InterruptedException {
        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("test", 1, true, false, 5, 2);
        manager.addCache(ehcache);
        ehcache.setStatisticsEnabled(true);
        Cache cache = new JCache(ehcache, null);

        cache.put("key1", "value1");
        cache.put("key2", "value1");
        //key1 should be in the Disk Store
        cache.get("key1");

        CacheStatistics statistics = cache.getCacheStatistics();
        assertEquals(1, statistics.getCacheHits());
        assertEquals(0, statistics.getCacheMisses());

        //clear stats
        statistics.clearStatistics();
        statistics = cache.getCacheStatistics();
        assertEquals(0, statistics.getCacheHits());
        assertEquals(0, statistics.getCacheMisses());
    }

    /**
     * Tests average get time
     */
    @Test
    public void testAverageGetTime() {
        //set to 0 to make it run slow
        Ehcache ehcache = new net.sf.ehcache.Cache("test", 0, true, false, 5, 2);
        manager.addCache(ehcache);
        ehcache.setStatisticsEnabled(true);
        Cache cache = new JCache(ehcache, null);
        JCacheStatistics statistics = (JCacheStatistics) cache.getCacheStatistics();
        float averageGetTime = statistics.getAverageGetTime();
        assertTrue(0 == statistics.getAverageGetTime());

        for (int i = 0; i < 10000; i++) {
            ehcache.put(new Element("" + i, "value1"));
        }
        ehcache.put(new Element("key1", "value1"));
        ehcache.put(new Element("key2", "value1"));
        for (int i = 0; i < 110000; i++) {
            ehcache.get("" + i);
        }

        statistics = (JCacheStatistics) cache.getCacheStatistics();
        averageGetTime = statistics.getAverageGetTime();
        assertTrue(averageGetTime >= .0000001);
        statistics.clearStatistics();
        statistics = (JCacheStatistics) cache.getCacheStatistics();
        assertTrue(0 == statistics.getAverageGetTime());
    }

    /**
     * Tests eviction statistics
     */
    @Test
    public void testEvictionStatistics() throws InterruptedException {
        //set to 0 to make it run slow
        Ehcache ehcache = new net.sf.ehcache.Cache("test", 10, false, false, 2, 2);
        manager.addCache(ehcache);
        ehcache.setStatisticsEnabled(true);
        Cache cache = new JCache(ehcache, null);
        JCacheStatistics statistics = (JCacheStatistics) cache.getCacheStatistics();
        assertEquals(0L, statistics.getEvictionCount());

        for (int i = 0; i < 10000; i++) {
            ehcache.put(new Element("" + i, "value1"));
        }
        statistics = (JCacheStatistics) cache.getCacheStatistics();
        assertEquals(9990L, statistics.getEvictionCount());

        Thread.sleep(2010);

        //expiries do not count
        statistics = (JCacheStatistics) cache.getCacheStatistics();
        assertEquals(9990L, statistics.getEvictionCount());

        statistics.clearStatistics();

        statistics = (JCacheStatistics) cache.getCacheStatistics();
        assertEquals(0L, statistics.getEvictionCount());

    }


}
