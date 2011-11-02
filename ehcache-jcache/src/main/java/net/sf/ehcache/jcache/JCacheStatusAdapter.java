package net.sf.ehcache.jcache;

import javax.cache.Status;

public class JCacheStatusAdapter {
    public static Status adaptStatus(net.sf.ehcache.Status status) {
        if (status.intValue() == net.sf.ehcache.Status.STATUS_ALIVE.intValue()) {
            return Status.STARTED;
        }
        if (status.intValue() == net.sf.ehcache.Status.STATUS_ALIVE.intValue()) {
            return Status.UNINITIALISED;
        }
        if (status.intValue() == net.sf.ehcache.Status.STATUS_UNINITIALISED.intValue()) {
            return Status.UNINITIALISED;
        }
        if (status.intValue() == net.sf.ehcache.Status.STATUS_SHUTDOWN.intValue()) {
            return Status.STOPPED;
        }
        return Status.UNINITIALISED;
    }
}