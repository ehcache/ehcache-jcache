/**
 *  Copyright 2003-2010 Terracotta, Inc.
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

import net.sf.ehcache.statistics.LiveCacheStatistics;

import java.io.Serializable;
import java.util.Date;

/**
 * Adapt the EHCache statistics to be accessible via the {@link javax.cache.CacheStatistics}
 *
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheStatistics implements javax.cache.CacheStatistics, Serializable {
    private final LiveCacheStatistics statistics;
    private final JCache cache;
    private Date dateFrom;

    /**
     * Create a JCacheStatistics adapter
     *
     * @param cache      the jsr107 ehcache cache
     * @param statistics a {@link net.sf.ehcache.statistics.LiveCacheStatistics} object.
     */
    public JCacheStatistics(final JCache cache, final LiveCacheStatistics statistics) {
        this.statistics = statistics;
        this.cache = cache;
    }


    /**
     * {@inheritDoc}
     * 
     * Clears the statistics counters to 0 for the associated Cache.
     */
    @Override
    public void clear() {
        this.statistics.clearStatistics();
        this.dateFrom = new Date();
    }

    /**
     * The date from which statistics have been accumulated. Because statistics can be cleared, this is not necessarily
     * since the cache was started.
     *
     * @return the date statistics started being accumulated
     */
    @Override
    public Date getStartAccumulationDate() {
        return this.dateFrom;
    }


    /**
     * {@inheritDoc}
     * 
     * The number of get requests that were satisfied by the cache.
     */
    @Override
    public long getCacheHits() {
        return statistics.getCacheHitCount();
    }

    /**
     * {@inheritDoc}
     * 
     * {@link #getCacheHits} divided by the total number of gets.
     * This is a measure of cache efficiency.
     */
    @Override
    public float getCacheHitPercentage() {
        if (statistics.getCacheHitCount() == 0 && statistics.getCacheMissCount() == 0) {
            return Float.POSITIVE_INFINITY;
        }
        return (statistics.getCacheHitCount() / (statistics.getCacheHitCount() + statistics.getCacheMissCount()));
    }

    /**
     * {@inheritDoc}
     * 
     * A miss is a get request which is not satisfied.
     * 
     * In a simple cache a miss occurs when the cache does not satisfy the request.
     * 
     * In a caches with multiple tiered storage, a miss may be implemented as a miss
     * to the cache or to the first tier.
     * 
     * In a read-through cache a miss is an absence of the key in teh cache which will trigger a call to a CacheLoader. So it is
     * still a miss even though the cache will load and return the value.
     * 
     * Refer to the implementation for precise semantics.
     */
    @Override
    public long getCacheMisses() {
        return statistics.getCacheMissCount();
    }

    /**
     * {@inheritDoc}
     * 
     * Returns the percentage of cache accesses that did not find a requested entry in the cache.
     */
    @Override
    public float getCacheMissPercentage() {
        if (statistics.getCacheHitCount() == 0 && statistics.getCacheMissCount() == 0) {
            return Float.POSITIVE_INFINITY;
        }
        return (statistics.getCacheMissCount() / (statistics.getCacheHitCount() + statistics.getCacheMissCount()));
    }

    /**
     * {@inheritDoc}
     * 
     * The total number of requests to the cache. This will be equal to the sum of the hits and misses.
     * 
     * A "get" is an operation that returns the current or previous value. It does not include checking for the existence
     * of a key.
     */
    @Override
    public long getCacheGets() {
        return statistics.getCacheHitCount() + statistics.getCacheMissCount();
    }

    /**
     * {@inheritDoc}
     * 
     * The total number of puts to the cache.
     * 
     * A put is counted even if it is immediately evicted. A replace includes a put and remove.
     */
    @Override
    public long getCachePuts() {
        return statistics.getPutCount();
    }

    /**
     * {@inheritDoc}
     * 
     * The total number of removals from the cache. This does not include evictions, where the cache itself
     * initiates the removal to make space.
     * 
     * A replace is a put that overwrites a mapping and is not considered a remove.
     */
    @Override
    public long getCacheRemovals() {
        return statistics.getRemovedCount();
    }

    /**
     * {@inheritDoc}
     * 
     * The total number of evictions from the cache. An eviction is a removal initiated by the cache itself to free
     * up space. An eviction is not treated as a removal and does not appear in the removal counts.
     */
    @Override
    public long getCacheEvictions() {
        return statistics.getEvictedCount();
    }

    /**
     * {@inheritDoc}
     * 
     * The mean time to execute gets.
     * 
     * In a read-through cache the time taken to load an entry on miss is not included in get time.
     */
    @Override
    public float getAverageGetMillis() {
        return (statistics.getAverageGetTimeMillis());
    }

    /**
     * {@inheritDoc}
     * 
     * The mean time to execute puts.
     */
    @Override
    public float getAveragePutMillis() {
        throw new UnsupportedOperationException("getAveragePutMillis is not implemented in net.sf.ehcache.jcache.JCacheStatistics");
    }

    /**
     * {@inheritDoc}
     * 
     * The mean time to execute removes.
     */
    @Override
    public float getAverageRemoveMillis() {
        throw new UnsupportedOperationException("getAverageRemoveMillis is not implemented in net.sf.ehcache.jcache.JCacheStatistics");
    }
}
