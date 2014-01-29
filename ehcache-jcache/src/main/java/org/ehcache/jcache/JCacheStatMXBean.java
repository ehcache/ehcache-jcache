package org.ehcache.jcache;

import javax.cache.management.CacheStatisticsMXBean;

/**
 * @author Alex Snaps
 */
public class JCacheStatMXBean extends JCacheMXBean implements CacheStatisticsMXBean {

    public JCacheStatMXBean(final JCache jCache) {
        super(jCache, "Statistics");
    }

    @Override
    public void clear() {
        jCache.clear();
    }

    @Override
    public long getCacheHits() {
        return getEhcache().getStatistics().cacheHitCount();
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
        return getEhcache().getStatistics().cacheMissCount();
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
        return getEhcache().getStatistics().cacheGetOperation().count().value();
    }

    @Override
    public long getCachePuts() {
        return getEhcache().getStatistics().cachePutCount();
    }

    @Override
    public long getCacheRemovals() {
        return getEhcache().getStatistics().cacheRemoveCount();
    }

    @Override
    public long getCacheEvictions() {
        return getEhcache().getStatistics().cacheEvictedCount();
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
