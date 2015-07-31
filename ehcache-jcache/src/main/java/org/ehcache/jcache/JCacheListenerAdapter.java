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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.event.EventType;

import java.util.ArrayList;


/**
 * Adapt a {@link CacheEntryListener} to the {@link CacheEventListener} interface
 *
 * @param <K> the type of keys used by this JCacheListenerAdapter
 * @param <V> the type of values that are loaded by this JCacheListenerAdapter
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheListenerAdapter<K, V> implements CacheEventListener {
    private final CacheEntryListener<K, V> cacheListener;
    private final CacheEntryEventFilter<? super K,? super V> cacheEntryEventFilter;
    private final JCache<K, V> jCache;
    private final boolean removedListener;
    private final boolean createdListener;
    private final boolean updatedListener;
    private final boolean expiredListener;
    private final boolean oldValueRequired;
    private final boolean synchronous;

    /**
     * Construct an adapter that wraps the {@code cacheListener} to be used by Ehcache
     * <br />
     * The interfaces of {@link CacheEntryListener} are more fine-grained than the
     * CacheEntryListener interface - and may only implement one or more of the following
     * sub-interfaces:
     * {@link CacheEntryRemovedListener}
     * {@link CacheEntryCreatedListener}
     * {@link CacheEntryUpdatedListener}
     * {@link CacheEntryExpiredListener}
     * <br />
     * When this constructor is called, the {@code cacheListener} will be inspected
     * and based upon which sub-interfaces of CacheEntryListener the {@code cacheListener}
     * implements, listeners on the corresponding EHCache events will be adapted to it.
     * <br />
     * It is expected that the EventListener model of JSR107 will change, so this class
     * will likely be refactored several times before the final release of JSR107.
     *
     * @param cacheListener the cacheListener to wrap
     * @param jCache
     * @param cacheEntryListenerConfiguration
     */
    public JCacheListenerAdapter(CacheEntryListener<K, V> cacheListener, final JCache<K, V> jCache,
                                 final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        this.cacheListener = cacheListener;
        this.jCache = jCache;
        removedListener = implementsMethods(CacheEntryRemovedListener.class);
        createdListener = implementsMethods(CacheEntryCreatedListener.class);
        updatedListener = implementsMethods(CacheEntryUpdatedListener.class);
        expiredListener = implementsMethods(CacheEntryExpiredListener.class);
        oldValueRequired = cacheEntryListenerConfiguration.isOldValueRequired();
        synchronous = cacheEntryListenerConfiguration.isSynchronous();
        if(cacheEntryListenerConfiguration.getCacheEntryEventFilterFactory() != null) {
            cacheEntryEventFilter = cacheEntryListenerConfiguration.getCacheEntryEventFilterFactory().create();
        } else {
            cacheEntryEventFilter = null;
        }
    }

    private boolean implementsMethods(Class cls) {
        return cls.isAssignableFrom(cacheListener.getClass());
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an attempt to remove an element. The remove method will block until
     * this method returns.
     * <br />
     * This notification is received regardless of whether the cache had an element matching
     * the removal key or not. If an element was removed, the element is passed to this method,
     * otherwise a synthetic element, with only the key set is passed in.
     * <br />
     * This notification is not called for the following special cases:
     * <ol>
     * <li>removeAll was called. See {@link #notifyRemoveAll(net.sf.ehcache.Ehcache)}
     * <li>An element was evicted from the cache.
     * See {@link #notifyElementEvicted(net.sf.ehcache.Ehcache, net.sf.ehcache.Element)}
     * </ol>
     */
    @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        if (removedListener) {
            final JCacheEntryEventAdapter<K, V> e = new JCacheEntryEventAdapter<K, V>(jCache, element, EventType.REMOVED);
            if (evaluate(e)) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(e);
                if (element != null) {
                    ((CacheEntryRemovedListener<? super K, ? super V>) cacheListener)
                            .onRemoved(
                                arrayList
                            );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an element has been put into the cache. The
     * {@link net.sf.ehcache.Cache#put(net.sf.ehcache.Element)} method
     * will block until this method returns.
     * <br />
     * Implementers may wish to have access to the Element's fields, including value, so the
     * element is provided. Implementers should be careful not to modify the element. The
     * effect of any modifications is undefined.
     */
    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        if (createdListener) {
            final JCacheEntryEventAdapter<K, V> e = new JCacheEntryEventAdapter<K, V>(jCache, element, EventType.CREATED);
            if (evaluate(e)) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(e);
                if (element != null) {
                    ((CacheEntryCreatedListener<K, V>)cacheListener)
                        .onCreated(
                            arrayList
                        );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an element has been put into the cache and the element already
     * existed in the cache. This is thus an update.
     * <br />
     * The {@link net.sf.ehcache.Cache#put(net.sf.ehcache.Element)} method
     * will block until this method returns.
     * <br />
     * Implementers may wish to have access to the Element's fields, including value, so the
     * element is provided. Implementers should be careful not to modify the element. The
     * effect of any modifications is undefined.
     */
    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        if (updatedListener) {
            final JCacheEntryEventAdapter<K, V> e = new JCacheEntryEventAdapter<K, V>(jCache, element, EventType.UPDATED);
            if (evaluate(e)) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(e);
                if (element != null) {
                    ((CacheEntryUpdatedListener<K, V>)cacheListener)
                        .onUpdated(
                            arrayList
                        );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an element is <i>found</i> to be expired. The
     * {@link net.sf.ehcache.Cache#remove(Object)} method will block until this method returns.
     * <br />
     * As the {@link net.sf.ehcache.Element} has been expired, only what was the key of the element is known.
     * <br />
     * Elements are checked for expiry in ehcache at the following times:
     * <ul>
     * <li>When a get request is made
     * <li>When an element is spooled to the diskStore in accordance with a MemoryStore
     * eviction policy
     * <li>In the DiskStore when the expiry thread runs, which by default is
     * {@link net.sf.ehcache.Cache#DEFAULT_EXPIRY_THREAD_INTERVAL_SECONDS}
     * </ul>
     * If an element is found to be expired, it is deleted and this method is notified.
     */
    @Override
    public void notifyElementExpired(Ehcache cache, Element element) {
        if (expiredListener) {
            final JCacheEntryEventAdapter<K, V> e = new JCacheEntryEventAdapter<K, V>(jCache, element, EventType.EXPIRED);
            if (evaluate(e)) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(e);
                if (element != null) {
                    ((CacheEntryExpiredListener<K, V>) cacheListener)
                            .onExpired(
                                arrayList
                            );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an element is evicted from the cache. Evicted in this sense
     * means evicted from one store and not moved to another, so that it exists nowhere in the
     * local cache.
     * <br />
     * In a sense the Element has been <i>removed</i> from the cache, but it is different,
     * thus the separate notification.
     */
    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        if (expiredListener) {
            final JCacheEntryEventAdapter<K, V> e = new JCacheEntryEventAdapter<K, V>(jCache, element, EventType.REMOVED);
            if (evaluate(e)) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(e);
                if (element != null) {
                    ((CacheEntryExpiredListener<K, V>) cacheListener).onExpired(
                            arrayList);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Called during {@link net.sf.ehcache.Ehcache#removeAll()} to indicate that the all
     * elements have been removed from the cache in a bulk operation. The usual
     * {@link #notifyElementRemoved(net.sf.ehcache.Ehcache, net.sf.ehcache.Element)}
     * is not called.
     * <br />
     * This notification exists because clearing a cache is a special case. It is often
     * not practical to serially process notifications where potentially millions of elements
     * have been bulk deleted.
     */
    @Override
    public void notifyRemoveAll(Ehcache cache) {
        // TODO:
        // does ehCache have the ability to pass this up natively? If not, decorating over the native events might not work
        // and we might need to have our JCache adapter layer handle talking to CacheEntryListeners directly (which wouldn't be
        // ideal because then only things happening through the JCache wrapper would be sent out through the event system)
    }

    /**
     * {@inheritDoc}
     *
     * Give the listener a chance to cleanup and free resources when no longer needed
     */
    @Override
    public void dispose() {

    }

    /**
     * {@inheritDoc}
     *
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object {@code x}, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be {@code true}, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be {@code true}, this is not an absolute requirement.
     *
     * By convention, the returned object should be obtained by calling
     * {@code super.clone}.  If a class and all of its superclasses (except
     * {@code Object}) obey this convention, it will be the case that
     * {@code x.clone().getClass() == x.getClass()}.
     *
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by {@code super.clone} before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by {@code super.clone}
     * need to be modified.
     *
     * The method {@code clone} for class {@code Object} performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface {@code Cloneable}, then a
     * {@code CloneNotSupportedException} is thrown. Note that all arrays
     * are considered to implement the interface {@code Cloneable} and that
     * the return type of the {@code clone} method of an array type {@code T[]}
     * is {@code T[]} where T is any reference or primitive type.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     *
     * The class {@code Object} does not itself implement the interface
     * {@code Cloneable}, so calling the {@code clone} method on an object
     * whose class is {@code Object} will result in throwing an
     * exception at run time.
     * @see Cloneable
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JCacheListenerAdapter that = (JCacheListenerAdapter) o;

        if (createdListener != that.createdListener) {
            return false;
        }
        if (expiredListener != that.expiredListener) {
            return false;
        }
        if (removedListener != that.removedListener) {
            return false;
        }
        if (updatedListener != that.updatedListener) {
            return false;
        }
        if (!cacheListener.equals(that.cacheListener)) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = cacheListener.hashCode();
        result = 31 * result + (removedListener ? 1 : 0);
        result = 31 * result + (createdListener ? 1 : 0);
        result = 31 * result + (updatedListener ? 1 : 0);
        result = 31 * result + (expiredListener ? 1 : 0);
        return result;
    }

    private boolean evaluate(final JCacheEntryEventAdapter<K, V> entry) {
        return cacheEntryEventFilter == null || cacheEntryEventFilter.evaluate(entry);
    }

}
