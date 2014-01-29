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

    private final Element element;
    private final Class<K> keyType;
    private final Class<V> valueType;

    public JCacheEntry(final Element e, final Class<K> keyType, final Class<V> valueType) {
        this.element = e;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public K getKey() {
        if (element != null) {
            return keyType.cast(element.getObjectKey());
        } else {
            return null;
        }
    }

    @Override
    public V getValue() {
        if (element != null) {
            return valueType.cast(element.getObjectValue());
        } else {
            return null;
        }
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        if(clazz.isAssignableFrom(getClass())) {
            return clazz.cast(this);
        }
        if(clazz.isAssignableFrom(Element.class)) {
            return clazz.cast(element);
        }
        throw new IllegalArgumentException();
    }
}

