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

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;

/**
 * Adapt an ehcache event to a JSR107 event
 *
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
class JCacheEntryEventAdapter<K, V> extends CacheEntryEvent<K, V> {
    private final Element element;
    private final Class<K> keyType;
    private final Class<V> valueType;

    /**
     * <p>Constructor for JCacheEntryEventAdapter.</p>
     *
     * @param source a {@link net.sf.ehcache.jcache.JCache} object.
     * @param element a {@link net.sf.ehcache.Element} object.
     * @param eventType type
     */
    public JCacheEntryEventAdapter(JCache<K, V> source, Element element, final EventType eventType) {
        super(source, eventType);
        this.element = element;
        final CompleteConfiguration<K, V> cfg = source.getConfiguration(CompleteConfiguration.class);
        this.keyType = cfg.getKeyType();
        this.valueType = cfg.getValueType();
    }

    /** {@inheritDoc} */
    @Override
    public K getKey() {
        return keyType.cast(element.getObjectKey());
    }

    /** {@inheritDoc} */
    @Override
    public V getValue() {
        return valueType.cast(element.getObjectValue());
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        if(clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        } else if(clazz.isAssignableFrom(Element.class)) {
            return clazz.cast(element);
        }
        return null;
    }

    /**
     * Returns the value of the cache entry with the event
     *
     * @return the value
     */
    @Override
    public V getOldValue() {
        if (isOldValueAvailable()) {
            //todo not available in this version of Ehcache return oldValue;
            return null;
        } else {
            throw new UnsupportedOperationException("The old value is not available for key " + getKey());
        }
    }

    /**
     * Whether the old value is available
     *
     * @return true if the old value is populated
     */
    @Override
    public boolean isOldValueAvailable() {
        return false; //todo not available in this version of Ehcache
    }
}
