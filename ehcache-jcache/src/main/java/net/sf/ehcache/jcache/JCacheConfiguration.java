package net.sf.ehcache.jcache;


import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.generator.ConfigurationUtil;

import javax.cache.Caching;
import javax.cache.InvalidConfigurationException;
import javax.cache.OptionalFeature;
import javax.cache.transaction.IsolationLevel;
import javax.cache.transaction.Mode;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Set of
 */
public class JCacheConfiguration implements javax.cache.CacheConfiguration {

    private final AtomicBoolean readThrough;
    private final AtomicBoolean writeThrough;

    private volatile IsolationLevel isolationLevel;
    private volatile Mode transactionMode;

    // we could, in theory, just set the values on the underlying CacheConfiguration but then the units
    // will be lost and we wont pass the TCK
    private final Duration[] timeToLive;

    private final CacheConfiguration cacheConfiguration = new CacheConfiguration();

    private static final boolean DEFAULT_READ_THROUGH = false;
    private static final boolean DEFAULT_WRITE_THROUGH = false;


    private JCacheConfiguration(boolean readThrough,
                                boolean writeThrough,
                                boolean storeByValue,
                                boolean statisticsEnabled,
                                IsolationLevel isolationLevel, Mode transactionMode,
                                Duration[] timeToLive) {
        this.readThrough = new AtomicBoolean(readThrough);
        this.writeThrough = new AtomicBoolean(writeThrough);

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

    public JCacheConfiguration(CacheConfiguration ehCacheConfiguration) {
        this.readThrough = new AtomicBoolean(DEFAULT_READ_THROUGH);
        this.writeThrough = new AtomicBoolean(DEFAULT_WRITE_THROUGH);
        timeToLive = new Duration[ExpiryType.values().length];
        for (int i = 0; i < timeToLive.length; i++) {
            timeToLive[i] = Duration.ETERNAL;
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

    /**
     * Sets whether the cache is a read-through cache.
     *
     * @param readThrough the value for readThrough
     * @throws IllegalStateException if the configuration can no longer be changed
     */
    @Override
    public void setReadThrough(boolean readThrough) {
        this.readThrough.set(readThrough);
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
     * Whether the cache is a write-through cache. A CacheWriter should be configured.
     *
     * @param writeThrough set to true for a write-through cache
     */
    @Override
    public void setWriteThrough(boolean writeThrough) {
        this.writeThrough.set(writeThrough);
    }

    /**
     * {@inheritDoc}
     */
    // TODO: is this the best analog to the isStoryByValue?
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
        return isolationLevel;
    }

    @Override
    public Mode getTransactionMode() {
        return transactionMode;
    }


//
//        /**
//         * Gets the registered {@link javax.cache.CacheLoader}, if any.
//         *
//         * @return the {@link javax.cache.CacheLoader} or null if none has been set.
//         */
//        @Override
//        public CacheLoader getCacheLoader() {
//            return jcache.getCacheLoader();
//        }
//
//        /**
//         * Gets the registered {@link javax.cache.CacheWriter}, if any.
//         *
//         * @return
//         */
//        @Override
//        public CacheWriter getCacheWriter() {
//            return jcache.getCacheWriter();
//        }

    @Override
    public void setExpiry(ExpiryType type, Duration duration) {
        if (type == null) {
            throw new NullPointerException("ExpiryType can't be null");
        }
        this.timeToLive[type.ordinal()] = duration;
        if (type == ExpiryType.ACCESSED) {
            cacheConfiguration.setTimeToIdleSeconds(duration.getTimeUnit().toSeconds(duration.getTimeToLive()));
        }
        if (type == ExpiryType.MODIFIED) {
            cacheConfiguration.setTimeToLiveSeconds(duration.getTimeUnit().toSeconds(duration.getTimeToLive()));
        }
        this.timeToLive[type.ordinal()] = duration;
    }

    // since the ehcache configuration could be adjusted out from under this, test if it is still the same
    @Override
    public Duration getExpiry(ExpiryType type) {
        Duration duration = this.timeToLive[type.ordinal()];
        TimeUnit timeUnit = duration.getTimeUnit();
        Long ttl = duration.getTimeToLive();

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
        if (duration.getTimeToLive() == 0) {
            return Duration.ETERNAL;
        } else {
            return duration;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JCacheConfiguration that = (JCacheConfiguration) o;

        if (!cacheConfigurationEquals(cacheConfiguration, that.getCacheConfiguration())) return false;
        if (isolationLevel != that.isolationLevel) return false;
        if (readThrough.get() != that.readThrough.get()) return false;
        if (!Arrays.equals(timeToLive, that.timeToLive)) return false;
        if (transactionMode != that.transactionMode) return false;
        if (writeThrough.get() != that.writeThrough.get()) return false;

        return true;
    }

    // CacheConfiguration doesn't override equals - so we are using this workaround for now
    protected boolean cacheConfigurationEquals(CacheConfiguration a, CacheConfiguration b) {
        return ConfigurationUtil.generateCacheConfigurationText(a)
                .equals(ConfigurationUtil.generateCacheConfigurationText(b));
    }

    protected int cacheConfigurationHashCode(CacheConfiguration a) {
        return ConfigurationUtil.generateCacheConfigurationText(a).hashCode();
    }

    @Override
    public int hashCode() {
        int result = readThrough.hashCode();
        result = 31 * result + writeThrough.hashCode();
        result = 31 * result + (isolationLevel != null ? isolationLevel.hashCode() : 0);
        result = 31 * result + (transactionMode != null ? transactionMode.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(timeToLive);
        result = 31 * result + cacheConfigurationHashCode(this.cacheConfiguration);
        return result;
    }

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

        protected static Duration[] defaultTimeToLive() {
            Duration[] ttl = new Duration[ExpiryType.values().length];
            for (int i = 0; i < ttl.length; i++) {
                ttl[i] = DEFAULT_TIME_TO_LIVE;
            }
            return ttl;
        }
    }
}
