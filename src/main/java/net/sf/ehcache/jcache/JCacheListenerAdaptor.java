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

import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.CacheException;

import net.sf.jsr107cache.CacheListener;

/**
 * This adaptor permits JCACHE {@link CacheListener}s to be registered as Ehcache {@link CacheEventListener}s.
 * @author Greg Luck
 * @version $Id: JCacheListenerAdaptor.java 744 2008-08-16 20:10:49Z gregluck $
 */
public class JCacheListenerAdaptor implements CacheEventListener {

    private CacheListener cacheListener;

    /**
     * Creates an adaptor that delegates to a {@link CacheListener}
     * @param cacheListener the JCACHE listener.
     */
    public JCacheListenerAdaptor(CacheListener cacheListener) {
        this.cacheListener = cacheListener;
    }


    /**
     * Called immediately after an element has been removed. The remove method will block until
     * this method returns.
     * <p/>
     * As the {@link net.sf.ehcache.Element} has been removed, only what was the key of the element is known.
     * <p/>
     * This method delegates to onRemove. After the last element is removed, a call to onClear is also made.
     * @param cache   the cache emitting the notification
     * @param element just deleted
     */
    public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
        if (element != null) {
            cacheListener.onRemove(element.getObjectKey());
            if (cache.getSize() == 0) {
                cacheListener.onClear();
            }
        }
    }

    /**
     * Called immediately after an element has been put into the cache. The {@link net.sf.ehcache.Cache#put(net.sf.ehcache.Element)} method
     * will block until this method returns.
     * <p/>
     * Implementers may wish to have access to the Element's fields, including value, so the element is provided.
     * Implementers should be careful not to modify the element. The effect of any modifications is undefined.
     *
     * @param cache   the cache emitting the notification
     * @param element the element which was just put into the cache.
     */
    public void notifyElementPut(final Ehcache cache, final Element element) throws CacheException {
        if (element != null) {
            cacheListener.onPut(element.getObjectKey());
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
     * <p/>
     * This method delegates to onPut in the underlying CacheListener, because JCACHE CacheListener does not
     * have update notifications.
     * @param cache   the cache emitting the notification
     * @param element the element which was just put into the cache.
     */
    public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {
        if (element != null) {
            cacheListener.onPut(element.getObjectKey());
        }
    }

    /**
     * Called immediately after an element is <i>found</i> to be expired. The
     * {@link net.sf.ehcache.Cache#remove(Object)} method will block until this method returns.
     * <p/>
     * As the {@link net.sf.ehcache.Element} has been expired, only what was the key of the element is known.
     * <p/>
     * Elements are checked for expiry in ehcache at the following times:
     * <ul>
     * <li>When a get request is made
     * <li>When an element is spooled to the diskStore in accordance with a MemoryStore eviction policy
     * <li>In the DiskStore when the expiry thread runs, which by default is
     * {@link net.sf.ehcache.Cache#DEFAULT_EXPIRY_THREAD_INTERVAL_SECONDS}
     * </ul>
     * If an element is found to be expired, it is deleted and this method is notified.
     * <p/>
     * JCACHE CacheListener does not support an expiry notification, so onEvict is called instead. Expiry is a
     * type of eviction.
     *
     * @param cache   the cache emitting the notification
     * @param element the element that has just expired
     *                <p/>
     *                Deadlock Warning: expiry will often come from the <code>DiskStore</code> expiry thread. It holds a lock to the
     *                DiskStorea the time the notification is sent. If the implementation of this method calls into a
     *                synchronized <code>Cache</code> method and that subsequently calls into DiskStore a deadlock will result.
     *                Accordingly implementers of this method should not call back into Cache.
     */
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        if (element != null) {
            cacheListener.onEvict(element.getObjectKey());
        }
    }

    /**
     * Called immediately after an element is evicted from the cache. Evicted in this sense
     * means evicted from one store and not moved to another, so that it exists nowhere in the
     * local cache.
     * <p/>
     * In a sense the Element has been <i>removed</i> from the cache, but it is different,
     * thus the separate notification.
     *
     * @param cache   the cache emitting the notification
     * @param element the element that has just been evicted
     */
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        if (element != null) {
            cacheListener.onEvict(element.getObjectKey());
        }
    }

    /**
     * Called during {@link net.sf.ehcache.Ehcache#removeAll()} to indicate that the all
     * elements have been removed from the cache in a bulk operation. The usual
     * {@link #notifyElementRemoved(net.sf.ehcache.Ehcache,net.sf.ehcache.Element)}
     * is not called.
     * <p/>
     * This notification exists because clearing a cache is a special case. It is often
     * not practical to serially process notifications where potentially millions of elements
     * have been bulk deleted.
     * <p/>
     * Note: There is no analogue in JCACHE to this method. It is not possible to know what
     * elements were removed. Accordingly, no notification is done.
     *
     * @param cache the cache emitting the notification
     */
    public void notifyRemoveAll(final Ehcache cache) {
        cacheListener.onClear();
    }

    /**
     * Give the listener a chance to cleanup and free resources when no longer needed.
     * <p/>
     * JCACHE CacheListener does not support on dispose. This method does not delegate to anything.
     * JCACHE CacheListener implementations should consider registering a CacheManagerEventListener
     * so that they know when a cache is removed and they can perform an cleanup required.
     */
    public void dispose() {
        //noop
    }

    /**
     * Gets the underlying CacheListener
     * @return the underlying CacheListener
     */
    public CacheListener getCacheListener() {
        return cacheListener;
    }

    /**
     * Creates a clone of this listener. This method will only be called by ehcache before a cache is initialized.
     * <p/>
     * This may not be possible for listeners after they have been initialized. Implementations should throw
     * CloneNotSupportedException if they do not support clone.
     * @return a clone
     * @throws CloneNotSupportedException if the listener could not be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        //shut
        super.clone();
        throw new CloneNotSupportedException("Cannot clone JCacheListenerAdaptor");
    }


}
