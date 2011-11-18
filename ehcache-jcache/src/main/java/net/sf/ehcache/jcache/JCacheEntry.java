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

import net.sf.ehcache.Element;

import javax.cache.Cache;

/**
 * An implementation of CacheEntry.
 * <p/>
 * A CacheEntry is metadata about an entry in the cache. It does not include the value.
 *
 * @param <K> the type of keys used by this JCacheEntry
 * @param <V> the type of values that are loaded by this JCacheEntry
 * @author Greg Luck, Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheEntry<K, V> implements Cache.Entry<K, V> {
    private Element element;

    /**
     * Constructor
     *
     * Create a new JCacheEntry for this key / value pair
     *
     * @param key a K object.
     * @param value a V object.
     */
    public JCacheEntry(K key, V value) {
        this.element = new Element(key, value);
    }
    
    /**
     * Constructor
     *
     * @param element an element from Ehcache
     */
    public JCacheEntry(Element element) {
        this.element = element;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the key corresponding to this entry.
     */
    @Override
    public K getKey() {
        if (element != null) {
            return (K) element.getObjectKey();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Returns the value stored in the cache when this entry was created.
     */
    @Override
    public V getValue() {
        if (element != null) {
            return (V) element.getValue();
        } else {
            return null;
        }
    }
}

