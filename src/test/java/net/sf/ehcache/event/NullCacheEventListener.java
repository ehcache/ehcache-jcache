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

package net.sf.ehcache.event;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Null Object Pattern implementation of CacheEventListener. It simply logs the calls made.
 *
 * @author Greg Luck
 * @version $Id: NullCacheEventListener.java 796 2008-10-09 02:39:03Z gregluck $
 * @since 1.2
 */
public class NullCacheEventListener implements CacheEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(NullCacheEventListener.class);


    /**
     * {@inheritDoc}
     */
    public void notifyElementRemoved(final Ehcache cache, final Element element) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("notifyElementRemoved called for cache " + cache + " for element with key " + element.getObjectKey());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void notifyElementPut(final Ehcache cache, final Element element) {
        if (LOG.isTraceEnabled()) {
            Object key = null;
            if (element != null) {
                key = element.getObjectKey();
            }
            LOG.trace("notifyElementPut called for cache " + cache + " for element with key " + key);
        }
    }

    /**
     * Called immediately after an element has been put into the cache and the element already
     * existed in the cache. This is thus an update.
     * <p/>
     * The {@link net.sf.ehcache.Cache#put(net.sf.ehcache.Element)} method
     * will block until this method returns.
     * <p/>
     * Implementers may wish to have access to the Element's fields, including value, so the element is provided.
     * Implementers should be careful not to modify the element. The effect of any modifications is undefined.
     *
     * @param cache   the cache emitting the notification
     * @param element the element which was just put into the cache.
     */
    public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {
        if (LOG.isTraceEnabled()) {
            Object key = null;
            if (element != null) {
                key = element.getObjectKey();
            }
            LOG.trace("notifyElementUpdated called for cache " + cache + " for element with key " + key);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("notifyElementExpired called for cache " + cache + " for element with key " + element.getObjectKey());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        //
    }

    /**
     * {@inheritDoc}
     */
    public void notifyRemoveAll(final Ehcache cache) {
        //
    }

    /**
     * Give the replicator a chance to cleanup and free resources when no longer needed
     */
    public void dispose() {
        //nothing to do
    }

    /**
     * Creates a clone of this listener. This method will only be called by ehcache before a cache is initialized.
     * <p/>
     * This may not be possible for listeners after they have been initialized. Implementations should throw
     * CloneNotSupportedException if they do not support clone.
     *
     * @return a clone
     * @throws CloneNotSupportedException if the listener could not be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
