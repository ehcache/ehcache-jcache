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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.ObjectExistsException;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * A container for {@link Ehcache}s that maintain all aspects of their lifecycle.
 * <p/>
 * CacheManager may be either be a singleton if created with factory methods, or multiple instances may exist,
 * in which case resources required by each must be unique.
 * <p/>
 * A CacheManager holds references to JCache objects and manages their creation and lifecycle.
 *
 * @author Greg Luck
 */
public class JCacheManager extends CacheManager {


    /**
     * The Singleton Instance.
     */
    protected static JCacheManager singleton;


    private static final Logger LOG = LoggerFactory.getLogger(JCacheManager.class);

    /**
     * JCaches managed by this manager. For each JCache there is a backing ehcache stored in the ehcaches map.
     */
    protected Map jCaches = new HashMap();



    /**
     * An constructor for CacheManager, which takes a configuration object, rather than one created by parsing
     * an ehcache.xml file. This constructor gives complete control over the creation of the CacheManager.
     * <p/>
     * Care should be taken to ensure that, if multiple CacheManages are created, they do now overwrite each others
     * disk store files, as would happend if two were created which used the same diskStore path.
     * <p/>
     * This method does not act as a singleton. Callers must maintain their own reference to it.
     * <p/>
     * Note that if one of the {@link #create()}  methods are called, a new singleton instance will be created,
     * separate from any instances created in this method.
     *
     * @param configuration
     * @throws CacheException
     */
    public JCacheManager(Configuration configuration) throws CacheException {
        super(configuration);
    }

    /**
     * An ordinary constructor for CacheManager.
     * This method does not act as a singleton. Callers must maintain a reference to it.
     * Note that if one of the {@link #create()}  methods are called, a new singleton will be created,
     * separate from any instances created in this method.
     *
     * @param configurationFileName an xml configuration file available through a file name. The configuration
     *                              {@link java.io.File} is created
     *                              using new <code>File(configurationFileName)</code>
     * @throws CacheException
     * @see #create(String)
     */
    public JCacheManager(String configurationFileName) throws CacheException {
        super(configurationFileName);
    }

    /**
     * An ordinary constructor for CacheManager.
     * This method does not act as a singleton. Callers must maintain a reference to it.
     * Note that if one of the {@link #create()}  methods are called, a new singleton will be created,
     * separate from any instances created in this method.
     * <p/>
     * This method can be used to specify a configuration resource in the classpath other
     * than the default of \"/ehcache.xml\":
     * <pre>
     * URL url = this.getClass().getResource("/ehcache-2.xml");
     * </pre>
     * Note that {@link Class#getResource} will look for resources in the same package unless a leading "/"
     * is used, in which case it will look in the root of the classpath.
     * <p/>
     * You can also load a resource using other class loaders. e.g. {@link Thread#getContextClassLoader()}
     *
     * @param configurationURL an xml configuration available through a URL.
     * @throws CacheException
     * @see #create(java.net.URL)
     * @since 1.2
     */
    public JCacheManager(URL configurationURL) throws CacheException {
        super(configurationURL);
    }

    /**
     * An ordinary constructor for CacheManager.
     * This method does not act as a singleton. Callers must maintain a reference to it.
     * Note that if one of the {@link #create()}  methods are called, a new singleton will be created,
     * separate from any instances created in this method.
     *
     * @param configurationInputStream an xml configuration file available through an inputstream
     * @throws CacheException
     * @see #create(java.io.InputStream)
     */
    public JCacheManager(InputStream configurationInputStream) throws CacheException {
        super(configurationInputStream);
    }

    /**
     * Constructor.
     *
     * @throws CacheException
     */
    public JCacheManager() throws CacheException {
        //default config will be done
        super();
    }


    /**
     * A factory method to create a singleton CacheManager with default config, or return it if it exists.
     * <p/>
     * The configuration will be read, {@link Ehcache}s created and required stores initialized.
     * When the {@link CacheManager} is no longer required, call shutdown to free resources.
     *
     * @return the singleton CacheManager
     * @throws CacheException if the CacheManager cannot be created
     */
    public static JCacheManager create() throws CacheException {

        synchronized (CacheManager.class) {
            if (singleton == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating new CacheManager with default config");
                }

                singleton = new JCacheManager();
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Attempting to create an existing singleton. Existing singleton returned.");
                }
            }
            return singleton;
        }
    }

    /**
     * A factory method to create a singleton CacheManager with default config, or return it if it exists.
     * <p/>
     * This has the same effect as {@link CacheManager#create}
     * <p/>
     * Same as {@link #create()}
     *
     * @return the singleton CacheManager
     * @throws CacheException if the CacheManager cannot be created
     */
    public static JCacheManager getInstance() throws CacheException {
        return JCacheManager.create();
    }

    /**
     * A factory method to create a singleton CacheManager with a specified configuration.
     *
     * @param configurationFileName an xml file compliant with the ehcache.xsd schema
     *                              <p/>
     *                              The configuration will be read, {@link Ehcache}s created and required stores initialized.
     *                              When the {@link CacheManager} is no longer required, call shutdown to free resources.
     */
    public static JCacheManager create(String configurationFileName) throws CacheException {
        synchronized (CacheManager.class) {
            if (singleton == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating new CacheManager with config file: " + configurationFileName);
                }
                singleton = new JCacheManager(configurationFileName);
            }
            return singleton;
        }
    }

    /**
     * A factory method to create a singleton CacheManager from an URL.
     * <p/>
     * This method can be used to specify a configuration resource in the classpath other
     * than the default of \"/ehcache.xml\":
     * This method can be used to specify a configuration resource in the classpath other
     * than the default of \"/ehcache.xml\":
     * <pre>
     * URL url = this.getClass().getResource("/ehcache-2.xml");
     * </pre>
     * Note that {@link Class#getResource} will look for resources in the same package unless a leading "/"
     * is used, in which case it will look in the root of the classpath.
     * <p/>
     * You can also load a resource using other class loaders. e.g. {@link Thread#getContextClassLoader()}
     *
     * @param configurationFileURL an URL to an xml file compliant with the ehcache.xsd schema
     *                             <p/>
     *                             The configuration will be read, {@link Ehcache}s created and required stores initialized.
     *                             When the {@link CacheManager} is no longer required, call shutdown to free resources.
     */
    public static JCacheManager create(URL configurationFileURL) throws CacheException {
        synchronized (CacheManager.class) {
            if (singleton == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating new CacheManager with config URL: " + configurationFileURL);
                }
                singleton = new JCacheManager(configurationFileURL);

            }
            return singleton;
        }
    }

    /**
     * A factory method to create a singleton CacheManager from a java.io.InputStream.
     * <p/>
     * This method makes it possible to use an inputstream for configuration.
     * Note: it is the clients responsibility to close the inputstream.
     * <p/>
     *
     * @param inputStream InputStream of xml compliant with the ehcache.xsd schema
     *                    <p/>
     *                    The configuration will be read, {@link Ehcache}s created and required stores initialized.
     *                    When the {@link CacheManager} is no longer required, call shutdown to free resources.
     */
    public static JCacheManager create(InputStream inputStream) throws CacheException {
        synchronized (CacheManager.class) {
            if (singleton == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating new CacheManager with InputStream");
                }
                singleton = new JCacheManager(inputStream);
            }
            return singleton;
        }
    }


    /**
     * Gets a draft JSR107 spec JCache.
     * <p/>
     * If a JCache does not exist for the name, but an ehcache does, a new JCache will be created dynamically and added
     * to the list of JCaches managed by this CacheManager.
     * Warning: JCache will change as the specification changes, so no guarantee of backward compatibility is made for this method.
     *
     * @return a JSR 107 Cache, if an object of that type exists by that name, else null
     * @throws IllegalStateException if the cache is not {@link net.sf.ehcache.Status#STATUS_ALIVE}
     */
    public synchronized JCache getJCache(String name) throws IllegalStateException {
        checkStatus();
        if (jCaches.get(name) != null) {
            return (JCache) jCaches.get(name);
        } else {
            Ehcache ehcache = super.getEhcache(name);
            if (ehcache != null) {
                jCaches.put(name, new JCache(ehcache));
            }
        }
        return (JCache) jCaches.get(name);
    }


    /**
     * Adds a {@link net.sf.ehcache.Cache} to the CacheManager.
     * <p/>
     * Memory and Disk stores will be configured for it and it will be added to the map of caches.
     * Also notifies the CacheManagerEventListener after the cache was initialised and added.
     *
     * @param jCache
     * @throws IllegalStateException         if the cache is not {@link net.sf.ehcache.Status#STATUS_UNINITIALISED} before this method is called.
     * @throws net.sf.ehcache.ObjectExistsException
     *                                       if the cache already exists in the CacheManager
     * @throws net.sf.ehcache.CacheException if there was an error adding the cache to the CacheManager
     */
    public synchronized void addCache(JCache jCache) throws IllegalStateException,
            ObjectExistsException, CacheException {
        checkStatus();
        if (jCache == null) {
            return;
        }
        Ehcache backingCache = jCache.getBackingCache();
        addCache(backingCache);
        jCaches.put(backingCache.getName(), jCache);
    }


    /**
     * Replaces in the map of Caches managed by this CacheManager an Ehcache with a JCache decorated version of the <i>same</i> (see Ehcache equals method)
     * Ehcache, in a single synchronized method.
     * <p/>
     * Warning: JCache will change as the specification changes, so no guarantee of backward compatibility is made for this method.
     *
     * @param ehcache
     * @param jCache  A JCache that wraps the original cache.
     * @throws CacheException
     */
    public synchronized void replaceEhcacheWithJCache(Ehcache ehcache, JCache jCache) throws CacheException {
        if (!ehcache.getName().equals(jCache.getBackingCache().getName())) {
            throw new CacheException("Cannot replace ehcache with a JCache where the backing cache has a different name");
        }
        Ehcache backingCache = jCache.getBackingCache();
        if (!ehcache.equals(backingCache)) {
            throw new CacheException("Cannot replace " + backingCache.getName()
                    + " It does not equal the incumbent cache.");
        } else {
            jCaches.put(backingCache.getName(), jCache);
        }
    }


    /**
     * Remove a cache from the CacheManager. The cache is disposed of.
     *
     * @param cacheName the cache name
     * @throws IllegalStateException if the cache is not {@link net.sf.ehcache.Status#STATUS_ALIVE}
     */
    public synchronized void removeCache(String cacheName) throws IllegalStateException {
        super.removeCache(cacheName);
        jCaches.remove(cacheName);
    }

    /**
     * Shuts down the CacheManager.
     * <p/>
     * If the shutdown occurs on the singleton, then the singleton is removed, so that if a singleton access method
     * is called, a new singleton will be created.
     * <p/>
     * By default there is no shutdown hook (ehcache-1.3-beta2 and higher).
     * <p/>
     * Set the system property net.sf.ehcache.enableShutdownHook=true to turn it on.
     */
    public void shutdown() {
        synchronized (CacheManager.class) {
            if (status.equals(Status.STATUS_SHUTDOWN)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("CacheManager already shutdown");
                }
                return;
            }
            super.shutdown();
            //only delete singleton if the singleton is shutting down.
            if (this == singleton) {
                singleton = null;
            }
        }
    }

}
