package net.sf.ehcache.jcache;

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.management.CacheMXBean;

/**
* @author Alex Snaps
*/
public class JCacheManagementMXBean extends JCacheMXBean implements CacheMXBean {

    public JCacheManagementMXBean(final JCache jCache) {
        super(jCache, "Configuration");
    }

    @Override
    public String getKeyType() {
        return jCache.getConfiguration(CompleteConfiguration.class).getKeyType().getName();
    }

    @Override
    public String getValueType() {
        return jCache.getConfiguration(CompleteConfiguration.class).getValueType().getName();
    }

    @Override
    public boolean isReadThrough() {
        return ((CompleteConfiguration) jCache.getConfiguration(CompleteConfiguration.class)).isReadThrough();
    }

    @Override
    public boolean isWriteThrough() {
        return ((CompleteConfiguration) jCache.getConfiguration(CompleteConfiguration.class)).isWriteThrough();
    }

    @Override
    public boolean isStoreByValue() {
        return jCache.getConfiguration(CompleteConfiguration.class).isStoreByValue();
    }

    @Override
    public boolean isStatisticsEnabled() {
        return ((CompleteConfiguration) jCache.getConfiguration(CompleteConfiguration.class)).isStatisticsEnabled();
    }

    @Override
    public boolean isManagementEnabled() {
        return ((CompleteConfiguration) jCache.getConfiguration(CompleteConfiguration.class)).isManagementEnabled();
    }

}
