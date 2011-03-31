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

import net.sf.ehcache.Element;
import net.sf.jsr107cache.CacheEntry;

/**
 * An implementation of CacheEntry.
 *
 * A CacheEntry is metadata about an entry in the cache. It does not include the value.
 *                   
 * @author Greg Luck
 * @version $Id: JCacheEntry.java 744 2008-08-16 20:10:49Z gregluck $
 */
public class JCacheEntry implements CacheEntry {
    private Element element;

    /**
     * Constructor
     *
     * @param element an element from Ehcache
     */
    public JCacheEntry(Element element) {
        this.element = element;
    }

    /**
     * Returns the key corresponding to this entry.
     *
     * @return the key corresponding to this entry.
     * @throws IllegalStateException implementations may, but are not
     *                               required to, throw this exception if the entry has been
     *                               removed from the backing map
     */
    public Object getKey() throws IllegalStateException {
        if (element != null) {
            return element.getObjectKey();
        } else {
            return null;
        }
    }

    /**
     * Returns the value corresponding to this entry.  If the mapping
     * has been removed from the backing map (by the iterator's
     * <tt>remove</tt> operation), the results of this call are undefined.
     *
     * @return the value corresponding to this entry.
     * @throws IllegalStateException implementations may, but are not
     *                               required to, throw this exception if the entry has been
     *                               removed from the backing map
     */
    public Object getValue() throws IllegalStateException {
        if (element != null) {
            return element.getObjectValue();
        } else {
            return null;
        }
    }

    /**
     * Replaces the value corresponding to this entry with the specified
     * value (optional operation).  (Writes through to the map.)  The
     * behavior of this call is undefined if the mapping has already been
     * removed from the map (by the iterator's <tt>remove</tt> operation).
     *
     * @param value new value to be stored in this entry.
     * @return old value corresponding to the entry.
     * @throws UnsupportedOperationException if the <tt>put</tt> operation
     *                                       is not supported by the backing map.
     */
    public Object setValue(Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Ehcache does not support modification of Elements. They are immutable.");
    }

    /**
     * This implementation does not have a notion of cost. Accordingly, 0 is always returned.
     *
     * @return 0
     */
    public long getCost() {
        return 0;
    }

    /**
     * Gets the creationTime attribute of the ElementAttributes object.
     *
     * @return The creationTime value
     */
    public long getCreationTime() {
        if (element != null) {
            return element.getCreationTime();
        } else {
            return 0;
        }
    }

    /**
     * Returns the expiration time based on time to live. If this element also has a time to idle setting, the expiry
     * time will vary depending on whether the element is accessed.
     *
     * @return the time to expiration
     */
    public long getExpirationTime() {
        if (element != null) {
            return element.getExpirationTime();
        } else {
            return 0;
        }
    }

    /**
     * Gets the hit count on this element. A new element has a hit count of 0.
     * @return the number of hits this Element has had. If the Element was updated, the hit count is reset to 0.
     */
    public int getHits() {
        if (element != null) {
            return (int) element.getHitCount();
        } else {
            return 0;
        }
    }

    /**
     * Gets the last access time.
     * Access means a get. So a newly created {@link Element}
     * will have a last access time equal to its create time.
     */
    public long getLastAccessTime() {
        if (element != null) {
            return element.getLastAccessTime();
        } else {
            return 0;
        }
    }

    /**
     * Updated means there was an existing Element that was replaced with a new one, for the same key. If an update
     * occurs, then the lastUpdateTime will be non-zero.
     * @return 0 if never updated, otherwise the time the update occurred.
     */
    public long getLastUpdateTime() {
        if (element != null) {
            return element.getLastUpdateTime();
        } else {
            return 0;
        }
    }

    /**
     * Returns the version of the Element. This is based on timestamps.
     * @return 0 if the first version, otherwise the timestamp when the version was created.
     */
    public long getVersion() {
        if (element != null) {
            return element.getVersion();
        } else {
            return 0;
        }

    }

    /**
     * An Element is 'valid' if it is unexpired.
     * @return true if unexpired, otherwise false.
     */
    public boolean isValid() {
        if (element != null) {
            return !element.isExpired();
        } else {
            return false;
        }
    }
}

