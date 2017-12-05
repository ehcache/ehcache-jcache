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


import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheWriterConfiguration;
import net.sf.ehcache.config.CopyStrategyConfiguration;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * The CacheManager that allows EHCache caches to be retrieved and accessed via JSR107 APIs
 *
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheManager implements javax.cache.CacheManager {

    private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private static final int DEFAULT_SIZE = 1000;

    private final JCacheCachingProvider jCacheCachingProvider;
    private final CacheManager cacheManager;
    private final URI uri;
    private final Properties props;
    private final ConcurrentHashMap<String, JCache> allCaches = new ConcurrentHashMap<String, JCache>();
    private volatile boolean closed = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<JCache, JCacheManagementMXBean> cfgMXBeans = new ConcurrentHashMap<JCache, JCacheManagementMXBean>();
    private final ConcurrentMap<JCache, JCacheStatMXBean> statMXBeans = new ConcurrentHashMap<JCache, JCacheStatMXBean>();

    public JCacheManager(final JCacheCachingProvider jCacheCachingProvider, final CacheManager cacheManager, final URI uri, final Properties props) {
        this.jCacheCachingProvider = jCacheCachingProvider;
        this.cacheManager = cacheManager;
        this.uri = uri;
        this.props = props;
        //refreshAllCaches();
    }

    @Override
    public JCacheCachingProvider getCachingProvider() {
        return jCacheCachingProvider;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public ClassLoader getClassLoader() {
        return cacheManager.getConfiguration().getClassLoader();
    }

    @Override
    public Properties getProperties() {
        return props;
    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(final String cacheName, final C configuration) throws IllegalArgumentException {
        checkNotClosed();
        if(configuration == null) {
            throw new NullPointerException();
        }

        JCache<K, V> jCache = allCaches.get(cacheName);
        if (jCache != null) {
            throw new CacheException();
        }
        cacheManager.addCacheIfAbsent(new net.sf.ehcache.Cache(toEhcacheConfig(cacheName, configuration)));
        Ehcache ehcache = cacheManager.getEhcache(cacheName);
        final JCacheConfiguration<K, V> cfg = new JCacheConfiguration<K, V>(configuration);
        jCache = new JCache<K, V>(this, cfg, ehcache);
        JCache<K, V> previous = allCaches.putIfAbsent(cacheName, jCache);
        if(previous != null) {
            // todo validate config
            return previous;
        }
        if(cfg.isStatisticsEnabled()) {
            enableStatistics(cacheName, true);
        }
        if(cfg.isManagementEnabled()) {
            enableManagement(cacheName, true);
        }
        return jCache;
    }

    @Override
    public <K, V> Cache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType) {
        checkNotClosed();
        if(valueType == null) {
            throw new NullPointerException();
        }
        JCache<K, V> jCache = allCaches.get(cacheName);
        if(jCache != null) {
            if(!keyType.isAssignableFrom(jCache.getConfiguration(CompleteConfiguration.class).getKeyType())) {
                throw new ClassCastException();
            }
            if(!valueType.isAssignableFrom(jCache.getConfiguration(CompleteConfiguration.class).getValueType())) {
                throw new ClassCastException();
            }
            return jCache;
        }
        final net.sf.ehcache.Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return null;
        }
        jCache = new JCache<K, V>(this, new JCacheConfiguration<K, V>(null, null, keyType, valueType), cache);
        final JCache<K, V> previous = allCaches.putIfAbsent(cacheName, jCache);
        if(previous != null) {
            jCache = previous;
        }
        if(!keyType.isAssignableFrom(jCache.getConfiguration(CompleteConfiguration.class).getKeyType())) {
            throw new ClassCastException();
        }
        if(!valueType.isAssignableFrom(jCache.getConfiguration(CompleteConfiguration.class).getValueType())) {
            throw new ClassCastException();
        }
        return jCache;
    }

    @Override
    public <K, V> Cache<K, V> getCache(final String cacheName) {
        final JCache<K, V> jCache = allCaches.get(cacheName);
        if(jCache == null) {
            //refreshAllCaches();
            final net.sf.ehcache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                allCaches.put(cacheName, new JCache(this, new JCacheConfiguration(cache.getCacheConfiguration()), cache));
            }
            return allCaches.get(cacheName);
        }
        if(jCache.getConfiguration(CompleteConfiguration.class).getKeyType() != Object.class ||
           jCache.getConfiguration(CompleteConfiguration.class).getValueType() != Object.class) {
            throw new IllegalArgumentException();
        }
        return jCache;
    }

    @Override
    public Iterable<String> getCacheNames() {
        return Collections.unmodifiableSet(new HashSet<String>(allCaches.keySet()));
    }

    @Override
    public void destroyCache(final String cacheName) {
        checkNotClosed();
        final JCache jCache = allCaches.get(cacheName);
        if (jCache != null) {
            jCache.close();
        }
    }

    @Override
    public void enableManagement(final String cacheName, final boolean enabled) {
        checkNotClosed();
        if(cacheName == null) throw new NullPointerException();
        final JCache jCache = allCaches.get(cacheName);
        if(jCache == null) {
            throw new NullPointerException();
        }
        enableManagement(enabled, jCache);
    }

    private void enableManagement(final boolean enabled, final JCache jCache) {
        try {
            if(enabled) {
                registerObject(getOrCreateCfgObject(jCache));
            } else {
                unregisterObject(cfgMXBeans.remove(jCache));
            }
            ((JCacheConfiguration)jCache.getConfiguration(JCacheConfiguration.class)).setManagementEnabled(enabled);
        } catch (NotCompliantMBeanException e) {
            throw new CacheException(e);
        } catch (InstanceAlreadyExistsException e) {
            // throw new CacheException(e);
        } catch (MBeanRegistrationException e) {
            throw new CacheException(e);
        } catch (InstanceNotFoundException e) {
            // throw new CacheException(e);
        } catch (MalformedObjectNameException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void enableStatistics(final String cacheName, final boolean enabled) {
        checkNotClosed();
        if(cacheName == null) throw new NullPointerException();
        final JCache jCache = allCaches.get(cacheName);
        if(jCache == null) {
            throw new NullPointerException();
        }
        enableStatistics(enabled, jCache);
    }

    private void enableStatistics(final boolean enabled, final JCache jCache) {
        try {
            if(enabled) {
                registerObject(getOrCreateStatObject(jCache));
            } else {
                unregisterObject(statMXBeans.remove(jCache));
            }
            ((JCacheConfiguration)jCache.getConfiguration(JCacheConfiguration.class)).setStatisticsEnabled(enabled);
        } catch (NotCompliantMBeanException e) {
            throw new CacheException(e);
        } catch (InstanceAlreadyExistsException e) {
            // throw new CacheException(e);
        } catch (MBeanRegistrationException e) {
            throw new CacheException(e);
        } catch (InstanceNotFoundException e) {
            // throw new CacheException(e);
        } catch (MalformedObjectNameException e) {
            throw new CacheException("Illegal ObjectName for Management Bean. " +
                                     "CacheManager=[" + getURI().toString() + "], Cache=[" + jCache.getName() + "]", e);
        }
    }

    private void registerObject(final JCacheMXBean cacheMXBean) throws NotCompliantMBeanException,
        InstanceAlreadyExistsException, MBeanRegistrationException, MalformedObjectNameException {
        final ObjectName objectName = new ObjectName(cacheMXBean.getObjectName());
        if(mBeanServer.queryNames(objectName, null).isEmpty()) {
            mBeanServer.registerMBean(cacheMXBean, objectName);
        }
    }

    private void unregisterObject(final JCacheMXBean cacheMXBean) throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException {
        if(cacheMXBean == null) return;
        final String name = cacheMXBean.getObjectName();
        final ObjectName objectName = new ObjectName(name);
        for (ObjectName n : mBeanServer.queryNames(objectName, null)) {
            mBeanServer.unregisterMBean(n);
        }
    }

    private JCacheManagementMXBean getOrCreateCfgObject(final JCache jCache) {
        JCacheManagementMXBean cacheMXBean = cfgMXBeans.get(jCache);
        if(cacheMXBean == null) {
            cacheMXBean = new JCacheManagementMXBean(jCache);
            final JCacheManagementMXBean previous = cfgMXBeans.putIfAbsent(jCache, cacheMXBean);
            if(previous != null) {
                cacheMXBean = previous;
            }
        }
        return cacheMXBean;
    }

    private JCacheStatMXBean getOrCreateStatObject(final JCache jCache) {
        JCacheStatMXBean cacheMXBean = statMXBeans.get(jCache);
        if(cacheMXBean == null) {
            cacheMXBean = new JCacheStatMXBean(jCache);
            final JCacheStatMXBean previous = statMXBeans.putIfAbsent(jCache, cacheMXBean);
            if(previous != null) {
                cacheMXBean = previous;
            }
        }
        return cacheMXBean;
    }

    @Override
    public void close() {
        jCacheCachingProvider.shutdown(this);
    }

    void shutdown() {
        closed = true;
        for (JCache jCache : allCaches.values()) {
            jCache.close();
        }
        cacheManager.shutdown();
        allCaches.clear();
    }

    @Override
    public boolean isClosed() {
        return cacheManager.getStatus() == Status.STATUS_SHUTDOWN;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        if(clazz.isAssignableFrom(getClass())) {
            return clazz.cast(this);
        }
        if(clazz.isAssignableFrom(cacheManager.getClass())) {
            return clazz.cast(cacheManager);
        }
        throw new IllegalArgumentException();
    }

    private void refreshAllCaches() {
        for (String s : cacheManager.getCacheNames()) {
            final net.sf.ehcache.Cache cache = cacheManager.getCache(s);
            if(cache != null) {
                allCaches.put(s, new JCache(this, new JCacheConfiguration(cache.getCacheConfiguration()), cache));
            }
        }
    }

    private CacheConfiguration toEhcacheConfig(final String name, final Configuration configuration) {
        final int maxSize = cacheManager.getConfiguration().isMaxBytesLocalHeapSet() ? 0 : DEFAULT_SIZE;
        CacheConfiguration cfg = new CacheConfiguration(name, maxSize);
        cfg.setClassLoader(cacheManager.getConfiguration().getClassLoader());
        if(configuration.isStoreByValue()) {
            final CopyStrategyConfiguration copyStrategyConfiguration = new CopyStrategyConfiguration();
            copyStrategyConfiguration.setCopyStrategyInstance(new JCacheCopyOnWriteStrategy());
            cfg.copyOnRead(true).copyOnWrite(true)
                .addCopyStrategy(copyStrategyConfiguration);
        }
        if(configuration instanceof CompleteConfiguration) {
            if(((CompleteConfiguration)configuration).isWriteThrough()) {
                cfg.addCacheWriter(new CacheWriterConfiguration().writeMode(CacheWriterConfiguration.WriteMode.WRITE_THROUGH));
            }
        }
        return cfg;
    }

    private void checkNotClosed() {
        if(closed) throw new IllegalStateException();
    }

    void shutdown(final JCache jCache) {
        final JCache r = allCaches.remove(jCache.getName());
        if (r == jCache) {
            enableStatistics(false, jCache);
            enableManagement(false, jCache);
            cacheManager.removeCache(jCache.getName());
            jCache.shutdown();
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
