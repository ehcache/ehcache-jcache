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
package org.ehcache.jcache;

import net.sf.ehcache.config.CacheConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.expiry.Duration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

/**
 * Configuration for a JSR107 Cache
 *
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheConfiguration<K, V> implements javax.cache.configuration.CompleteConfiguration<K, V> {

    private final Class<K> keyType;
    private final Class<V> valueType;
    private final ExpiryPolicy expiryPolicy;
    private final ConcurrentMap<CacheEntryListenerConfiguration<K,V>, JCacheListenerAdapter<K, V>> cacheEntryListenerConfigurations = new ConcurrentHashMap<CacheEntryListenerConfiguration<K, V>, JCacheListenerAdapter<K, V>>();
    private final boolean storeByValue;
    private final boolean readThrough;
    private final boolean writeThrough;
    private final Factory<CacheLoader<K,V>> cacheLoaderFactory;
    private final Factory<CacheWriter<? super K,? super V>> cacheWristerFactory;
    private final Factory<ExpiryPolicy> expiryPolicyFactory;
    private final boolean useJCacheExpiry;
    private final Set<CacheEntryListenerConfiguration<K, V>> initialCacheEntryListenerConfigurations;

    private boolean statisticsEnabled;
    private boolean managementEnabled;

    public JCacheConfiguration(final CacheConfiguration cacheConfiguration, final Configuration<K, V> configuration, final Class<K> keyType, final Class<V> valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
        if (configuration instanceof CompleteConfiguration) {
            CompleteConfiguration<K, V> cConfiguration = (CompleteConfiguration<K, V>) configuration;
                Factory<ExpiryPolicy> expiryPolicyFactory = cConfiguration.getExpiryPolicyFactory();
                this.expiryPolicy = expiryPolicyFactory.create();
            storeByValue = configuration.isStoreByValue();
            readThrough = cConfiguration.isReadThrough();
            writeThrough = cConfiguration.isWriteThrough();
            statisticsEnabled = cConfiguration.isStatisticsEnabled();
            managementEnabled = cConfiguration.isManagementEnabled();
            cacheLoaderFactory = cConfiguration.getCacheLoaderFactory();
            cacheWristerFactory = cConfiguration.getCacheWriterFactory();
            this.expiryPolicyFactory = cConfiguration.getExpiryPolicyFactory();
            useJCacheExpiry = this.expiryPolicyFactory != null;
            final HashSet<CacheEntryListenerConfiguration<K, V>> set = new HashSet<CacheEntryListenerConfiguration<K, V>>();
            for (CacheEntryListenerConfiguration<K, V> kvCacheEntryListenerConfiguration : cConfiguration.getCacheEntryListenerConfigurations()) {
                set.add(kvCacheEntryListenerConfiguration);
            }
            initialCacheEntryListenerConfigurations = Collections.unmodifiableSet(set);
        } else {
            if (cacheConfiguration == null) {
                expiryPolicyFactory = EternalExpiryPolicy.factoryOf();
                useJCacheExpiry = true;
                expiryPolicy = expiryPolicyFactory.create();
                storeByValue = true;
                readThrough = false;
                writeThrough = false;
                cacheLoaderFactory = null;
                cacheWristerFactory = null;
                initialCacheEntryListenerConfigurations = new HashSet<CacheEntryListenerConfiguration<K, V>>();
            } else {
                if (cacheConfiguration.isEternal()) {
                    expiryPolicy = EternalExpiryPolicy.factoryOf().create();
                } else {
                    expiryPolicy = new ExpiryPolicy() {
                        @Override
                        public Duration getExpiryForCreation() {
                            if (cacheConfiguration.getTimeToLiveSeconds() > 0) {
                                return new Duration(TimeUnit.SECONDS, cacheConfiguration.getTimeToLiveSeconds());
                            } else if (cacheConfiguration.getTimeToIdleSeconds() > 0) {
                                return new Duration(TimeUnit.SECONDS, cacheConfiguration.getTimeToIdleSeconds());
                            } else {
                                return Duration.ETERNAL;
                            }
                        }

                        @Override
                        public Duration getExpiryForAccess() {
                            if (cacheConfiguration.getTimeToLiveSeconds() > 0) {
                            	return null;
                            } else if (cacheConfiguration.getTimeToIdleSeconds() > 0) {
                                return new Duration(TimeUnit.SECONDS, cacheConfiguration.getTimeToIdleSeconds());
                            } else {
                                return null;
                            }
                        }

                        @Override
                        public Duration getExpiryForUpdate() {
                            if (cacheConfiguration.getTimeToLiveSeconds() > 0) {
                                return new Duration(TimeUnit.SECONDS, cacheConfiguration.getTimeToLiveSeconds());
                            } else if (cacheConfiguration.getTimeToIdleSeconds() > 0) {
                                return new Duration(TimeUnit.SECONDS, cacheConfiguration.getTimeToIdleSeconds());
                            } else {
                                return null;
                            }
                        }
                    };
                }

                expiryPolicyFactory = new FactoryBuilder.SingletonFactory<ExpiryPolicy>(expiryPolicy);
                useJCacheExpiry = false;
                storeByValue = false;
                readThrough = false;
                writeThrough = false;
                cacheLoaderFactory = null;
                cacheWristerFactory = null;
                initialCacheEntryListenerConfigurations = new HashSet<CacheEntryListenerConfiguration<K, V>>();
            }
        }
    }

    public JCacheConfiguration(final CacheConfiguration cacheConfiguration) {
        this(cacheConfiguration, null, null, null);
    }

    public JCacheConfiguration(final Configuration<K, V> configuration) {
        this(null, configuration, configuration.getKeyType(), configuration.getValueType());
    }

    @Override
    public Class<K> getKeyType() {
        return keyType == null ? (Class<K>)Object.class : keyType;
    }

    @Override
    public Class<V> getValueType() {
        return valueType == null ? (Class<V>)Object.class : valueType;
    }

    @Override
    public boolean isStoreByValue() {
        return storeByValue;
    }

    @Override
    public boolean isReadThrough() {
        return readThrough;
    }

    @Override
    public boolean isWriteThrough() {
        return writeThrough;
    }

    @Override
    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    @Override
    public boolean isManagementEnabled() {
        return managementEnabled;
    }

    @Override
    public Iterable<CacheEntryListenerConfiguration<K, V>> getCacheEntryListenerConfigurations() {
        return cacheEntryListenerConfigurations.keySet();
    }

    @Override
    public Factory<CacheLoader<K, V>> getCacheLoaderFactory() {
        return cacheLoaderFactory;
    }

    @Override
    public Factory<CacheWriter<? super K, ? super V>> getCacheWriterFactory() {
        return cacheWristerFactory;
    }

    @Override
    public Factory<ExpiryPolicy> getExpiryPolicyFactory() {
        return expiryPolicyFactory;
    }

    public ExpiryPolicy getExpiryPolicy() {
        return expiryPolicy;
    }

    public boolean addCacheEntryListenerConfiguration(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration, final JCacheListenerAdapter<K, V> cacheEventListener) {
        return cacheEntryListenerConfigurations.putIfAbsent(cacheEntryListenerConfiguration, cacheEventListener) == null;
    }

    public JCacheListenerAdapter<K, V> removeCacheEntryListenerConfiguration(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        return cacheEntryListenerConfigurations.remove(cacheEntryListenerConfiguration);
    }

    void setStatisticsEnabled(final boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    void setManagementEnabled(final boolean managementEnabled) {
        this.managementEnabled = managementEnabled;
    }

    public Iterable<CacheEntryListenerConfiguration<K, V>> getInitialCacheEntryListenerConfigurations() {
        return initialCacheEntryListenerConfigurations;
    }

    public boolean overrideDefaultExpiry() {
        return useJCacheExpiry;
    }
}
