package org.ehcache.jcache;

import javax.cache.management.CacheStatisticsMXBean;

/**
 * @author Alex Snaps
 */
public class JCacheStatMXBean extends JCacheMXBean implements CacheStatisticsMXBean {

    private long pCacheHits;
    private long pCacheMisses;
    private long pCacheGets;
    private long pCachePuts;
    private long pCacheRemovals;
    private long pCacheEvictions;

    public JCacheStatMXBean(final JCache jCache) {
        super(jCache, "Statistics");
    }

    @Override
    public void clear() {
        pCacheHits = getEhcache().getStatistics().cacheHitCount();
        pCacheMisses = getEhcache().getStatistics().cacheMissCount();
        pCacheGets = getEhcache().getStatistics().cacheGetOperation().count().value();
        pCachePuts = getEhcache().getStatistics().cachePutCount();
        pCacheRemovals = getEhcache().getStatistics().cacheRemoveCount();
        pCacheEvictions = getEhcache().getStatistics().cacheEvictedCount();
    }

    @Override
    public long getCacheHits() {
        return getEhcache().getStatistics().cacheHitCount() - pCacheHits;
    }

    @Override
    public float getCacheHitPercentage() {
        final double v = getEhcache().getStatistics().cacheHitRatio();
        if(Double.isNaN(v)) {
            return getEhcache().getStatistics().cacheHitCount() == 0 ? 0f : 100f;
        }
        return (float) v * 100;
    }

    @Override
    public long getCacheMisses() {
        return getEhcache().getStatistics().cacheMissCount() - pCacheMisses;
    }

    @Override
    public float getCacheMissPercentage() {
        final double v = getEhcache().getStatistics().cacheHitRatio();
        if(Double.isNaN(v)) {
            return 0f;
        }
        return (float) (1 - v) * 100;
    }

    @Override
    public long getCacheGets() {
        return getEhcache().getStatistics().cacheGetOperation().count().value() - pCacheGets;
    }

    @Override
    public long getCachePuts() {
        return getEhcache().getStatistics().cachePutCount() - pCachePuts;
    }

    @Override
    public long getCacheRemovals() {
        return getEhcache().getStatistics().cacheRemoveCount() - pCacheRemovals;
    }

    @Override
    public long getCacheEvictions() {
        return getEhcache().getStatistics().cacheEvictedCount() - pCacheEvictions;
    }

    @Override
    public float getAverageGetTime() {
        final float v = getEhcache().getStatistics().cacheGetOperation().latency().average().value().floatValue();
        return Float.isNaN(v) ? 0f : v;
    }

    @Override
    public float getAveragePutTime() {
        final float v = getEhcache().getStatistics().cachePutOperation().latency().average().value().floatValue();
        return Float.isNaN(v) ? 0f : v;
    }

    @Override
    public float getAverageRemoveTime() {
        final float v = getEhcache().getStatistics().cacheRemoveOperation().latency().average().value().floatValue();
        return Float.isNaN(v) ? 0f : v;
    }

}
