package net.sf.ehcache.jcache;

import net.sf.ehcache.Statistics;
import net.sf.ehcache.statistics.LiveCacheStatistics;

import java.io.Serializable;
import java.util.Date;

public class JCacheStatistics implements javax.cache.CacheStatistics, Serializable {
    private LiveCacheStatistics statistics;
    private final javax.cache.Cache cache;
    private Date dateFrom;


    public JCacheStatistics(final javax.cache.Cache cache, LiveCacheStatistics statistics) {
        this.statistics = statistics;
        this.cache = cache;
    }

    /**
     * @return the name of the Cache these statistics are for
     */
    @Override
    public String getName() {
        return cache.getName();
    }

    /**
     * Gets the {@link javax.cache.Status} attribute of the Cache expressed as a String.
     *
     * @return The status value from the Status enum class
     */
    @Override
    public String getStatus() {
        return cache.getStatus().toString();
    }

    /**
     * Clears the statistics counters to 0 for the associated Cache.
     */
    @Override
    public void clearStatistics() {
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
    public Date statsAccumulatingFrom() {
        return this.dateFrom;
    }

    /**
     * The number of get requests that were satisfied by the cache.
     *
     * @return the number of hits
     */
    @Override
    public long getCacheHits() {
        return statistics.getCacheHitCount();
    }

    /**
     * {@link #getCacheHits} divided by the total number of gets.
     * This is a measure of cache efficiency.
     *
     * @return the percentage of successful hits, as a decimal
     */
    @Override
    public float getCacheHitPercentage() {
        return (statistics.getCacheHitCount() / (statistics.getCacheHitCount() + statistics.getCacheMissCount()));
    }

    /**
     * A miss is a get request which is not satisfied.
     * <p/>
     * In a simple cache a miss occurs when the cache does not satisfy the request.
     * <p/>
     * In a caches with multiple tiered storage, a miss may be implemented as a miss
     * to the cache or to the first tier.
     * <p/>
     * In a read-through cache a miss is an absence of the key in teh cache which will trigger a call to a CacheLoader. So it is
     * still a miss even though the cache will load and return the value.
     * <p/>
     * Refer to the implementation for precise semantics.
     *
     * @return the number of misses
     */
    @Override
    public long getCacheMisses() {
        return statistics.getCacheMissCount();
    }

    /**
     * Returns the percentage of cache accesses that did not find a requested entry in the cache.
     *
     * @return the percentage of accesses that failed to find anything
     */
    @Override
    public float getCacheMissPercentage() {
        return (statistics.getCacheMissCount() / (statistics.getCacheHitCount() + statistics.getCacheMissCount()));
    }

    /**
     * The total number of requests to the cache. This will be equal to the sum of the hits and misses.
     * <p/>
     * A "get" is an operation that returns the current or previous value. It does not include checking for the existence
     * of a key.
     *
     * @return the number of gets
     */
    @Override
    public long getCacheGets() {
        return statistics.getCacheHitCount() + statistics.getCacheMissCount();
    }

    /**
     * The total number of puts to the cache.
     * <p/>
     * A put is counted even if it is immediately evicted. A replace includes a put and remove.
     *
     * @return the number of hits
     */
    @Override
    public long getCachePuts() {
        return statistics.getPutCount();
    }

    /**
     * The total number of removals from the cache. This does not include evictions, where the cache itself
     * initiates the removal to make space.
     * <p/>
     * A replace is a put that overwrites a mapping and is not considered a remove.
     *
     * @return the number of hits
     */
    @Override
    public long getCacheRemovals() {        
        return statistics.getRemovedCount();
    }

    /**
     * The total number of evictions from the cache. An eviction is a removal initiated by the cache itself to free
     * up space. An eviction is not treated as a removal and does not appear in the removal counts.
     *
     * @return the number of evictions from the cache
     */
    @Override
    public long getCacheEvictions() {
        return statistics.getEvictedCount();
    }

    /**
     * The mean time to execute gets.
     * <p/>
     * In a read-through cache the time taken to load an entry on miss is not included in get time.
     *
     * @return the time in milliseconds
     */
    @Override
    public long getAverageGetMillis() {
        return (long)(statistics.getAverageGetTimeMillis());
    }

    /**
     * The mean time to execute puts.
     *
     * @return the time in milliseconds
     */
    @Override
    public long getAveragePutMillis() {
        throw new UnsupportedOperationException("getAveragePutMillis is not implemented in net.sf.ehcache.jcache.JCacheStatistics");
    }

    /**
     * The mean time to execute removes.
     *
     * @return the time in milliseconds
     */
    @Override
    public long getAverageRemoveMillis() {
        throw new UnsupportedOperationException("getAverageRemoveMillis is not implemented in net.sf.ehcache.jcache.JCacheStatistics");
    }
}
