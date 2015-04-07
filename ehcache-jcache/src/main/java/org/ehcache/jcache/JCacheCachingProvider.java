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

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;


/**
 * A JSR107 adapter for EHCache.
 * <p/>
 *
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheCachingProvider implements CachingProvider {

    private static final URI URI_DEFAULT;

    private final Map<ClassLoader, ConcurrentMap<URI, JCacheManager>> cacheManagers = new WeakHashMap<ClassLoader, ConcurrentMap<URI, JCacheManager>>();

    static {
        URI uri;
        try {
            URL resource = JCacheCachingProvider.class.getResource("/ehcache.xml");
            if(resource == null) {
                resource = Ehcache.class.getResource("/ehcache-failsafe.xml");
            }
            uri = new URI(resource.toString());
        } catch (URISyntaxException e) {
            uri = null;
        }
        URI_DEFAULT = uri;
    }


    @Override
    public CacheManager getCacheManager() {
        return getCacheManager(getDefaultURI(), getDefaultClassLoader());
    }

    @Override
    public CacheManager getCacheManager(final URI uri, final ClassLoader classLoader) {
        return getCacheManager(uri, classLoader, null);
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader, Properties properties) {
        uri = uri == null ? getDefaultURI() : uri;
        classLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
        properties = new Properties(properties);

        JCacheManager cacheManager;
        final URL configurationURL;
        try {
            configurationURL = uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        ConcurrentMap<URI, JCacheManager> byURI;
        synchronized (cacheManagers) {
            byURI = cacheManagers.get(classLoader);
            if(byURI == null) {
                byURI = new ConcurrentHashMap<URI, JCacheManager>();
                cacheManagers.put(classLoader, byURI);
            }
            cacheManager = byURI.get(uri);
            if(cacheManager == null) {
                final Configuration configuration = ConfigurationFactory.parseConfiguration(configurationURL);
                if(configuration.getName() == null) {
                    configuration.setName(uri.toString() + "::" + classLoader.toString() + "::" + this.toString());
                }
                configuration.setClassLoader(classLoader);
                final net.sf.ehcache.CacheManager ehcacheManager = new net.sf.ehcache.CacheManager(configuration);
                cacheManager = new JCacheManager(this, ehcacheManager, uri, properties);
                byURI.put(uri, cacheManager);
            }
        }
        return cacheManager;
    }

    @Override
    public ClassLoader getDefaultClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public URI getDefaultURI() {
        return URI_DEFAULT;
    }

    @Override
    public Properties getDefaultProperties() {
        throw new UnsupportedOperationException("Implement me!");
    }

    @Override
    public void close() {
        synchronized (cacheManagers) {
            for (Map.Entry<ClassLoader, ConcurrentMap<URI, JCacheManager>> entry : cacheManagers.entrySet()) {
                for (JCacheManager jCacheManager : entry.getValue().values()) {
                    jCacheManager.close();
                }
            }
            cacheManagers.clear();
        }
    }

    @Override
    public void close(final ClassLoader classLoader) {
        synchronized (cacheManagers) {
            final ConcurrentMap<URI, JCacheManager> map = cacheManagers.remove(classLoader);
            if(map != null) {
                for (JCacheManager cacheManager : map.values()) {
                    cacheManager.shutdown();
                }
            }
        }
    }

    @Override
    public void close(final URI uri, final ClassLoader classLoader) {
        synchronized (cacheManagers) {
            final ConcurrentMap<URI, JCacheManager> map = cacheManagers.get(classLoader);
            if(map != null) {
                final JCacheManager jCacheManager = map.remove(uri);
                if(jCacheManager != null) {
                    jCacheManager.shutdown();
                }
            }
        }
    }

    @Override
    public boolean isSupported(final OptionalFeature optionalFeature) {
        return optionalFeature == OptionalFeature.STORE_BY_REFERENCE;
    }

    void shutdown(final JCacheManager jCacheManager) {
        synchronized (cacheManagers) {
            final ConcurrentMap<URI, JCacheManager> map = cacheManagers.get(jCacheManager.getClassLoader());
            if(map != null && map.remove(jCacheManager.getURI()) != null) {
                jCacheManager.shutdown();
            }
        }
    }
    
}
