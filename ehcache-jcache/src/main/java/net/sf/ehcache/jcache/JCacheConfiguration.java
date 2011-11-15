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


import net.sf.ehcache.config.CacheConfiguration;

import javax.cache.CacheLoader;
import javax.cache.CacheWriter;
import javax.cache.Caching;
import javax.cache.InvalidConfigurationException;
import javax.cache.OptionalFeature;
import javax.cache.transaction.IsolationLevel;
import javax.cache.transaction.Mode;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Configuration for a JSR107 Cache
 *
 * @author Ryan Gardner
 */
public class JCacheConfiguration implements javax.cache.CacheConfiguration {
    private static final boolean DEFAULT_WRITE_THROUGH = false;
    private static final boolean DEFAULT_READ_THROUGH = false;

    private final AtomicBoolean writeThrough;
    private final AtomicBoolean readThrough;

    private volatile IsolationLevel isolationLevel;
    private volatile Mode transactionMode;

    // we could, in theory, just set the values on the underlying CacheConfiguration but then the units
    // will be lost and we wont pass the TCK
    private final Duration[] timeToLive;

    private final CacheConfiguration cacheConfiguration;
    private JCache jcache;

    private JCacheConfiguration(boolean readThrough,
                                boolean writeThrough,
                                boolean storeByValue,
                                boolean statisticsEnabled,
                                IsolationLevel isolationLevel, Mode transactionMode,
                                Duration[] timeToLive) {
        this.readThrough = new AtomicBoolean(readThrough);
        this.writeThrough = new AtomicBoolean(writeThrough);

        this.cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setCopyOnRead(storeByValue);
        cacheConfiguration.setCopyOnWrite(storeByValue);

        cacheConfiguration.statistics(statisticsEnabled);
        this.isolationLevel = isolationLevel;
        this.transactionMode = transactionMode;
        // use the setter to set these, which will set the value in the underlying ehcache
        this.timeToLive = timeToLive;
        for (ExpiryType expiryType : ExpiryType.values()) {
            setExpiry(expiryType, timeToLive[expiryType.ordinal()]);
        }
    }

    /**
     * Create a JCacheConfiguration that wraps the existing ehCacheConfiguration
     *
     * @param ehCacheConfiguration a {@link net.sf.ehcache.config.CacheConfiguration} to wrap
     */
    public JCacheConfiguration(CacheConfiguration ehCacheConfiguration) {
        this.readThrough = new AtomicBoolean(DEFAULT_READ_THROUGH);
        this.writeThrough = new AtomicBoolean(DEFAULT_WRITE_THROUGH);

        timeToLive = new Duration[ExpiryType.values().length];
        for (int i = 0; i < timeToLive.length; i++) {
            timeToLive[i] = Duration.ETERNAL;
        }

        if (ehCacheConfiguration != null) {
            this.cacheConfiguration = ehCacheConfiguration;
        } else {
            this.cacheConfiguration = new CacheConfiguration();
        }
    }


    /**
     * Whether the cache is a read-through cache. A CacheLoader should be configured for read through caches
     * which is called on a cache miss.
     * <p/>
     * Default value is false.
     *
     * @return true if the cache is read-through
     */
    @Override
    public boolean isReadThrough() {
        return this.readThrough.get();
    }

    /***
     * Set the backing cache to expose more configuration.
     *
     * @param jcache the backing cache.
     */
    void setJCache(JCache jcache) {
        this.jcache = jcache;
    }

    /**
     * Gets the registered {@link javax.cache.CacheLoader}, if any.
     *
     * @return the {@link javax.cache.CacheLoader} or null if none has been set.
     */
    @Override
    public CacheLoader getCacheLoader() {
        JCacheCacheLoaderAdapter cacheLoaderAdapter = jcache.getCacheLoaderAdapter();
        return (cacheLoaderAdapter != null) ? cacheLoaderAdapter.getJCacheCacheLoader() : null;
    }

    /**
     * Gets the registered {@link javax.cache.CacheWriter}, if any.
     *
     * @return
     */
    @Override
    public CacheWriter getCacheWriter() {
        JCacheCacheWriterAdapter cacheWriterAdapter = jcache.getCacheWriterAdapter();
        return (cacheWriterAdapter != null) ? cacheWriterAdapter.getJCacheCacheWriter() : null;
    }

    /**
     * Whether the cache is a write-through cache. A CacheWriter should be configured.
     * <p/>
     * Default value is false.
     *
     * @return true if the cache is write-through
     */
    @Override
    public boolean isWriteThrough() {
        return this.writeThrough.get();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This will return true if the underlying cache is configured as both {@code copyOnRead}
     * and {@code copyOnWrite}
     * {@see net.sf.ehcache.config.CacheConfiguration#isCopyOnRead()}
     * {@see net.sf.ehcache.config.CacheConfiguration#isCopyOnWrite()}
     */
    @Override
    public boolean isStoreByValue() {
        return (cacheConfiguration.isCopyOnRead() && cacheConfiguration.isCopyOnWrite());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatisticsEnabled() {
        return cacheConfiguration.getStatistics();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatisticsEnabled(boolean enableStatistics) {
        cacheConfiguration.statistics(enableStatistics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTransactionEnabled() {
        return isolationLevel != null && transactionMode != null;
    }

    @Override
    public IsolationLevel getTransactionIsolationLevel() {
        return isolationLevel != null ? isolationLevel : IsolationLevel.NONE;
    }

    @Override
    public Mode getTransactionMode() {
        return transactionMode != null ? transactionMode : Mode.NONE;
    }

    /**
     * Set the expiry for this CacheConfiguration and configure the
     * underlying ehcache to match the same configuration
     *
     * @param type Type of expiry to set
     * @param duration duration for that expiry
     */
    private void setExpiry(ExpiryType type, Duration duration) {
        if (type == null) {
            throw new NullPointerException("ExpiryType can't be null");
        }
        this.timeToLive[type.ordinal()] = duration;
        if (type == ExpiryType.ACCESSED) {
            cacheConfiguration.setTimeToIdleSeconds(duration.getTimeUnit().toSeconds(duration.getDurationAmount()));
        }
        if (type == ExpiryType.MODIFIED) {
            cacheConfiguration.setTimeToLiveSeconds(duration.getTimeUnit().toSeconds(duration.getDurationAmount()));
        }
        this.timeToLive[type.ordinal()] = duration;
    }

    // since the ehcache configuration could be adjusted out from under this, test if it is still the same
    @Override
    public Duration getExpiry(ExpiryType type) {
        Duration duration = this.timeToLive[type.ordinal()];
        TimeUnit timeUnit = duration.getTimeUnit();
        Long ttl = duration.getDurationAmount();

        if (type == ExpiryType.ACCESSED) {
            long timeToIdleSeconds = cacheConfiguration.getTimeToIdleSeconds();
            if (timeUnit.toSeconds(ttl) != timeToIdleSeconds) {
                duration = new Duration(TimeUnit.SECONDS, timeToIdleSeconds);
            }
        }
        if (type == ExpiryType.MODIFIED) {
            long timeToLiveSeconds = cacheConfiguration.getTimeToLiveSeconds();
            if (timeUnit.toSeconds(ttl) != timeToLiveSeconds) {
                duration = new Duration(TimeUnit.SECONDS, timeToLiveSeconds);
            }
        }
        if (duration.getDurationAmount() == 0) {
            return Duration.ETERNAL;
        } else {
            return duration;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JCacheConfiguration that = (JCacheConfiguration) o;

        // Once ehcache's CacheConfiguration overrides equals this method can be
        // greatly simplified
        if (isolationLevel != that.isolationLevel) {
            return false;
        }
        if (this.isStatisticsEnabled() != that.isStatisticsEnabled()) {
            return false;
        }
        if (this.isStoreByValue() != that.isStoreByValue()) {
            return false;
        }
        if (this.isTransactionEnabled() != that.isTransactionEnabled()) {
            return false;
        }
        if (this.isReadThrough() != that.isReadThrough()) {
            return false;
        }
        if (this.isWriteThrough() != that.isWriteThrough()) {
            return false;
        }
        if (!Arrays.equals(timeToLive, that.timeToLive)) {
            return false;
        }
        if (transactionMode != that.transactionMode) {
            return false;
        }
        if (writeThrough.get() != that.writeThrough.get()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = readThrough.hashCode();
        result = 31 * result + writeThrough.hashCode();
        result = 31 * result + (this.isStatisticsEnabled() ? 1 : 0);
        result = 31 * result + (this.isStoreByValue() ? 1 : 0);
        result = 31 * result + (this.isTransactionEnabled() ? 1 : 0);
        result = 31 * result + (isolationLevel != null ? isolationLevel.hashCode() : 0);
        result = 31 * result + (transactionMode != null ? transactionMode.hashCode() : 0);
        result = 31 * result + (isolationLevel != null ? isolationLevel.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(timeToLive);
        // once ehcache's cacheConfiguration overrides hashcode, this method can be redone
        return result;
    }

    /**
     * Return the underlying {@see CacheConfiguration} that this JCacheConfiguration wraps
     *
     * @return the wrapped CacheConfiguration
     */
    public CacheConfiguration getCacheConfiguration() {
        return cacheConfiguration;
    }

    /**
     * Builds the config
     *
     * @author Yannis Cosmadopoulos
     */
    public static class Builder {
        private static final boolean DEFAULT_READ_THROUGH = false;
        private static final boolean DEFAULT_WRITE_THROUGH = false;
        private static final boolean DEFAULT_STORE_BY_VALUE = true;
        private static final boolean DEFAULT_STATISTICS_ENABLED = false;
        private static final Duration DEFAULT_TIME_TO_LIVE = Duration.ETERNAL;
        private static final IsolationLevel DEFAULT_TRANSACTION_ISOLATION_LEVEL = null;
        private static final Mode DEFAULT_TRANSACTION_MODE = null;

        private boolean readThrough = DEFAULT_READ_THROUGH;
        private boolean writeThrough = DEFAULT_WRITE_THROUGH;
        private boolean storeByValue = DEFAULT_STORE_BY_VALUE;
        private boolean statisticsEnabled = DEFAULT_STATISTICS_ENABLED;
        private IsolationLevel isolationLevel = DEFAULT_TRANSACTION_ISOLATION_LEVEL;
        private Mode transactionMode = DEFAULT_TRANSACTION_MODE;
        private final Duration[] timeToLive;

        /**
         * Constructor
         */
        public Builder() {
            timeToLive = new Duration[ExpiryType.values().length];
            for (int i = 0; i < timeToLive.length; i++) {
                timeToLive[i] = Duration.ETERNAL;
            }
        }

        /**
         * Set whether read through is active
         *
         * @param readThrough whether read through is active
         * @return this Builder instance
         */
        public Builder setReadThrough(boolean readThrough) {
            this.readThrough = readThrough;
            return this;
        }

        /**
         * Set whether write through is active
         *
         * @param writeThrough whether write through is active
         * @return this Builder instance
         */
        public Builder setWriteThrough(boolean writeThrough) {
            this.writeThrough = writeThrough;
            return this;
        }

        /**
         * Set whether store by value is active
         *
         * @param storeByValue whether store by value is active
         * @return this Builder instance
         */
        public Builder setStoreByValue(boolean storeByValue) {
            if (!storeByValue && !Caching.isSupported(OptionalFeature.STORE_BY_REFERENCE)) {
                throw new InvalidConfigurationException("storeByValue");
            }
            this.storeByValue = storeByValue;
            return this;
        }

        /**
         * Set whether statistics are enabled
         *
         * @param statisticsEnabled statistics are enabled
         * @return this Builder instance
         */
        public Builder setStatisticsEnabled(boolean statisticsEnabled) {
            this.statisticsEnabled = statisticsEnabled;
            return this;
        }

        /**
         * Set expiry
         *
         * @param type     ttl type
         * @param duration time to live
         * @return this Builder instance
         */
        public Builder setExpiry(ExpiryType type, Duration duration) {
            if (type == null) {
                throw new NullPointerException();
            }
            if (duration == null) {
                throw new NullPointerException();
            }
            timeToLive[type.ordinal()] = duration;
            return this;
        }

        /**
         * Set whether transactions are enabled
         *
         * @param isolationLevel isolation level
         * @param mode           the transactionMode
         * @return this Builder instance
         */
        public Builder setTransactionEnabled(IsolationLevel isolationLevel, Mode mode) {
            if (!Caching.isSupported(OptionalFeature.TRANSACTIONS)) {
                throw new InvalidConfigurationException("transactionsEnabled");
            }
            this.isolationLevel = isolationLevel;
            this.transactionMode = mode;
            return this;
        }

        /**
         * Create a new RICacheConfiguration instance.
         *
         * @return a new RICacheConfiguration instance
         */
        public JCacheConfiguration build() {
            return new JCacheConfiguration(readThrough, writeThrough, storeByValue, statisticsEnabled,
                    isolationLevel, transactionMode, timeToLive);
        }

        /**
         * Construct a default Duration[] with TimeToLive values that match the spec default
         *
         * @return a Duration[] containing Durations that match the default TimeToLive of the spec
         */
        protected static Duration[] defaultTimeToLive() {
            Duration[] ttl = new Duration[ExpiryType.values().length];
            for (int i = 0; i < ttl.length; i++) {
                ttl[i] = DEFAULT_TIME_TO_LIVE;
            }
            return ttl;
        }
    }
}
