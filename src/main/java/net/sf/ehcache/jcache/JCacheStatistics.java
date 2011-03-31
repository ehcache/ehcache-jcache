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


import net.sf.ehcache.Statistics;

import net.sf.jsr107cache.CacheStatistics;
import java.io.Serializable;

/**
 * A jsr107 CacheStatistics decorator for an ehcache Statistics class.
 *
 * An immutable Cache statistics implementation}
 * <p/>
 * This is like a value object, with the added ability to clear cache statistics on the cache.
 * That ability does not survive any Serialization of this class. On deserialization the cache
 * can be considered disconnected.
 * <p/>
 * The accuracy of these statistics are determined by the value of {#getStatisticsAccuracy()}
 * at the time the statistic was computed. This can be changed by setting {@link net.sf.ehcache.Cache#setStatisticsAccuracy}.
 * <p/>
 * Because this class maintains a reference to an Ehcache, any references held to this class will precent the Ehcache
 * from getting garbage collected.
 *
 * @author Greg Luck
 * @version $Id: JCacheStatistics.java 744 2008-08-16 20:10:49Z gregluck $
 */
public class JCacheStatistics implements CacheStatistics, Serializable {


    private Statistics statistics;

    /**
     * Constructs an object from an ehcache statistics object
     *
     * @param statistics the Statistics object this object decorates.
     */
    public JCacheStatistics(Statistics statistics) {
            this.statistics = statistics;
    }

    /**
     * Accurately measuring statistics can be expensive. Returns the current accuracy setting used
     * in the creation of these statistics.
     *
     * @return one of {@link #STATISTICS_ACCURACY_BEST_EFFORT}, {@link #STATISTICS_ACCURACY_GUARANTEED}, {@link #STATISTICS_ACCURACY_NONE}
     */
    public int getStatisticsAccuracy() {
        return statistics.getStatisticsAccuracy();
    }

    /**
     * Clears the statistic counters to 0 for the associated Cache.
     */
    public void clearStatistics() {
        statistics.clearStatistics();
    }

    /**
     * The number of times a requested item was found in the cache.
     * <p/>
     * Warning. This statistic is recorded as a long. If the statistic is large than Integer#MAX_VALUE
     * precision will be lost.
     * @return the number of times a requested item was found in the cache
     */
    public int getCacheHits() {
        return (int) statistics.getCacheHits();
    }

    /**
     * Warning. This statistic is recorded as a long. If the statistic is large than Integer#MAX_VALUE
     * precision will be lost.
     * @return the number of times a requested element was not found in the cache
     */
    public int getCacheMisses() {
        return (int) statistics.getCacheMisses();

    }

    /**
     * Gets the number of elements stored in the cache. Caclulating this can be expensive. Accordingly,
     * this method will return three different values, depending on the statistics accuracy setting.
     * <h3>Best Effort Size</h3>
     * This result is returned when the statistics accuracy setting is {@link Statistics#STATISTICS_ACCURACY_BEST_EFFORT}.
     * <p/>
     * The size is the number of {@link net.sf.ehcache.Element}s in the {@link net.sf.ehcache.store.MemoryStore} plus
     * the number of {@link net.sf.ehcache.Element}s in the {@link net.sf.ehcache.store.DiskStore}.
     * <p/>
     * This number is the actual number of elements, including expired elements that have
     * not been removed. Any duplicates between stores are accounted for.
     * <p/>
     * Expired elements are removed from the the memory store when
     * getting an expired element, or when attempting to spool an expired element to
     * disk.
     * <p/>
     * Expired elements are removed from the disk store when getting an expired element,
     * or when the expiry thread runs, which is once every five minutes.
     * <p/>
     * <h3>Guaranteed Accuracy Size</h3>
     * This result is returned when the statistics accuracy setting is {@link Statistics#STATISTICS_ACCURACY_GUARANTEED}.
     * <p/>
     * This method accounts for elements which might be expired or duplicated between stores. It take approximately
     * 200ms per 1000 elements to execute.
     * <h3>Fast but non-accurate Size</h3>
     * This result is returned when the statistics accuracy setting is {@link #STATISTICS_ACCURACY_NONE}.
     * <p/>
     * The number given may contain expired elements. In addition if the DiskStore is used it may contain some double
     * counting of elements. It takes 6ms for 1000 elements to execute. Time to execute is O(log n). 50,000 elements take
     * 36ms.
     *
     * @return the number of elements in the ehcache, with a varying degree of accuracy, depending on accuracy setting.
     */
    public int getObjectCount() {
        return (int) statistics.getObjectCount();
    }

    /**
     * The average time for cache gets since either the cache was created or statistics were cleared
     */
    public float getAverageGetTime() {
        return statistics.getAverageGetTime();
    }

    /**
     * Gets the number of cache evictions, since the cache was created, or statistics were cleared.
     */
    public long getEvictionCount() {
        return statistics.getEvictionCount();
    }

}
