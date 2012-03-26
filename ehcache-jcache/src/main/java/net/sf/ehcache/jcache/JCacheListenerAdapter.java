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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;


/**
 * Adapt a {@link CacheEntryListener} to the {@link CacheEventListener} interface
 *
 * @param <K> the type of keys used by this JCacheListenerAdapter
 * @param <V> the type of values that are loaded by this JCacheListenerAdapter
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheListenerAdapter<K, V> implements CacheEventListener {
    private CacheEntryListener<K, V> cacheListener;
    private boolean removedListener;
    private boolean createdListener;
    private boolean updatedListener;
    private boolean expiredListener;

    /**
     * Construct an adapter that wraps the {@code cacheListener} to be used by Ehcache
     * <p/>
     * The interfaces of {@link CacheEntryListener} are more fine-grained than the
     * CacheEntryListener interface - and may only implement one or more of the following
     * sub-interfaces:
     * {@link CacheEntryRemovedListener}
     * {@link CacheEntryCreatedListener}
     * {@link CacheEntryUpdatedListener}
     * {@link CacheEntryExpiredListener}
     * <p/>
     * When this constructor is called, the {@code cacheListener} will be inspected
     * and based upon which sub-interfaces of CacheEntryListener the {@code cacheListener}
     * implements, listeners on the corresponding EHCache events will be adapted to it.
     * <p/>
     * It is expected that the EventListener model of JSR107 will change, so this class
     * will likely be refactored several times before the final release of JSR107.
     *
     * @param cacheListener the cacheListener to wrap
     */
    public JCacheListenerAdapter(CacheEntryListener<K, V> cacheListener) {
        this.cacheListener = cacheListener;
        removedListener = implementsMethods(CacheEntryRemovedListener.class);
        createdListener = implementsMethods(CacheEntryCreatedListener.class);
        updatedListener = implementsMethods(CacheEntryUpdatedListener.class);
        expiredListener = implementsMethods(CacheEntryExpiredListener.class);
    }

    private boolean implementsMethods(Class cls) {
        return cacheListener.getClass().isAssignableFrom(cls);
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an attempt to remove an element. The remove method will block until
     * this method returns.
     * <p/>
     * This notification is received regardless of whether the cache had an element matching
     * the removal key or not. If an element was removed, the element is passed to this method,
     * otherwise a synthetic element, with only the key set is passed in.
     * <p/>
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
            if (element != null) {
                ((CacheEntryRemovedListener<? super K, ? super V>) cacheListener)
                        .entryRemoved(
                            new JCacheEntryEventAdapter<K, V>(fromEhcache(cache), element)
                            );
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an element has been put into the cache. The
     * {@link net.sf.ehcache.Cache#put(net.sf.ehcache.Element)} method
     * will block until this method returns.
     * <p/>
     * Implementers may wish to have access to the Element's fields, including value, so the
     * element is provided. Implementers should be careful not to modify the element. The
     * effect of any modifications is undefined.
     */
    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        if (createdListener) {
            if (element != null) {
                ((CacheEntryCreatedListener<K, V>) cacheListener)
                        .entryCreated(
                                new JCacheEntryEventAdapter<K, V>(fromEhcache(cache), element)
                        );
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an element has been put into the cache and the element already
     * existed in the cache. This is thus an update.
     * <p/>
     * The {@link net.sf.ehcache.Cache#put(net.sf.ehcache.Element)} method
     * will block until this method returns.
     * <p/>
     * Implementers may wish to have access to the Element's fields, including value, so the
     * element is provided. Implementers should be careful not to modify the element. The
     * effect of any modifications is undefined.
     */
    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        if (updatedListener) {
            if (element != null) {
                ((CacheEntryCreatedListener<K, V>) cacheListener)
                        .entryCreated(
                            new JCacheEntryEventAdapter<K, V>(fromEhcache(cache), element)
                            );
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an element is <i>found</i> to be expired. The
     * {@link net.sf.ehcache.Cache#remove(Object)} method will block until this method returns.
     * <p/>
     * As the {@link net.sf.ehcache.Element} has been expired, only what was the key of the element is known.
     * <p/>
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
            if (element != null) {
                ((CacheEntryExpiredListener<K, V>) cacheListener)
                        .entryExpired(
                                new JCacheEntryEventAdapter<K, V>(fromEhcache(cache), element)
                        );
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Called immediately after an element is evicted from the cache. Evicted in this sense
     * means evicted from one store and not moved to another, so that it exists nowhere in the
     * local cache.
     * <p/>
     * In a sense the Element has been <i>removed</i> from the cache, but it is different,
     * thus the separate notification.
     */
    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        if (expiredListener) {
            if (element != null) {
                ((CacheEntryExpiredListener<K, V>) cacheListener).entryExpired(
                        new JCacheEntryEventAdapter<K, V>(fromEhcache(cache), element)
                );
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
     * <p/>
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
    
    /**
     * <p>fromEhcache.</p>
     *
     * @param ehcache a {@link net.sf.ehcache.Ehcache} object.
     * @return a {@link net.sf.ehcache.jcache.JCache} object.
     */
    protected JCache<K,V> fromEhcache(Ehcache ehcache) {
        if (ehcache instanceof JCacheEhcacheDecorator) {
            return (JCache<K,V>)((JCacheEhcacheDecorator) ehcache).getJcache();
        }
        else {
            return null;
        }
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
}
