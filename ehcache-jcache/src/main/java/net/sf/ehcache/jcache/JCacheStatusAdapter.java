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

import javax.cache.Status;

/**
 * Adapt a {@link net.sf.ehcache.Status} to a matching {@link Status}
 *
 * @author Ryan Gardner
 * @version $Id: $
 * @since 1.4.0-beta1
 */
public class JCacheStatusAdapter {

    /**
     * Adapt the {@link net.sf.ehcache.Status} to the matching {@link Status}
     *
     * @param status the status to adapt
     * @return a Status that matches the lifecycle phase of the ehcache-specific status
     */
    public static Status adaptStatus(net.sf.ehcache.Status status) {
        if (status.intValue() == net.sf.ehcache.Status.STATUS_ALIVE.intValue()) {
            return Status.STARTED;
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
