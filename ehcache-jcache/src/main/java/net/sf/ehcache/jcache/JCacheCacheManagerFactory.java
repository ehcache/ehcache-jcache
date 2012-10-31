package net.sf.ehcache.jcache;

import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import javax.cache.Caching;
import org.jsr107.ri.AbstractCacheManagerFactory;
import java.net.URL;

/**
 * CacheManagerFactory for {@link JCacheManager}
 *
 * @author Ryan Gardner
 * @since 1.4.0-beta2
 */
public final class JCacheCacheManagerFactory extends AbstractCacheManagerFactory {
    private static Logger LOG = LoggerFactory.getLogger(JCacheCacheManagerFactory.class);

    private static final JCacheCacheManagerFactory INSTANCE = new JCacheCacheManagerFactory();

    private JCacheCacheManagerFactory() {
    }

    @Override
    protected CacheManager createCacheManager(ClassLoader classLoader, String name) {
        return new JCacheManager(name,configureEhCacheManager(name,classLoader), classLoader);
    }

    @Override
    protected ClassLoader getDefaultClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }


    /**
     * Configures the underlying ehcacheManager - either by retrieving it via the
     * {@code ehcache-<NAME>.xml} or by creating a new CacheManager
     * <p/>
     * JSR107 has unique CacheManagers per classLoader. The CacheManager in EHCache 2.5 wont allow multiple cacheManagers
     * with the same name to be created so this method will sometimes change the underlying ehcache cachemanager name
     * to be unique (which will make it more difficult to pull it back)
     *
     * @param name        name of the CacheManager to create
     * @param classLoader
     * @return a CacheManager configured with that name
     */
    private net.sf.ehcache.CacheManager configureEhCacheManager(String name, ClassLoader classLoader) {
        net.sf.ehcache.CacheManager cacheManager;

        Configuration config = getInitialConfigurationForCacheManager(name, classLoader);

        // in ehcache 2.5.0 it started enforcing that CacheManagers could only be created once per name.
        // but we have to at one per classloader with the same name in order to pass the TCK tests
        //
        // appending the toString of the classLoader will allow us to pass the TCK.
        //
        // Once ehcache's CacheManager can handle returning CacheManagers with the same name
        // and different classLoaders (perhaps using the same underlying cache?) this workaround
        // can be removed
        //
        config.setName(name + classLoader.toString());
        LOG.debug("CacheName was set to {} used with classLoader {}", name, classLoader.toString());

        // in ehcache 2.6, it needs to use .newInstance. in 2.5 it needs to use "create" - we could use reflection for this
        // if we want to support both we will have to use reflection
        cacheManager = net.sf.ehcache.CacheManager.newInstance(config);

        return cacheManager;
    }

    /**
     * This gets the initial configuration - either from a named cache file or from the default config
     * returned from ehCache.
     *
     * @param name cache manager name
     * @param classLoader classloader to use to retrieve resources
     * @return the initial configuration for the cache manager
     */
    private Configuration getInitialConfigurationForCacheManager(String name, ClassLoader classLoader) {
        String defaultName = "ehcache-" + name + ".xml";
        Configuration configuration;

        URL configResource = null;
        if (name != Caching.DEFAULT_CACHE_MANAGER_NAME) {
            configResource = classLoader.getResource(defaultName);
        }
        if (configResource != null) {
            configuration = ConfigurationFactory.parseConfiguration(configResource);
        } else {
            configuration = ConfigurationFactory.parseConfiguration();
        }
        return configuration;
    }

    /**
     * Get the singleton instance
     * @return the singleton instance
     */
    public static JCacheCacheManagerFactory getInstance() {
        return INSTANCE;
    }
}
