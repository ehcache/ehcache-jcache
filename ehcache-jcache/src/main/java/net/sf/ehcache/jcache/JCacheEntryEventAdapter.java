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

import javax.cache.event.CacheEntryEvent;

/**
 * Adapt an ehcache event to a JSR107 event
 *
 * @author Ryan Gardner
 */
public class JCacheEntryEventAdapter<K, V> extends CacheEntryEvent<K, V> {
    private Element element;

    public JCacheEntryEventAdapter(JCache<K, V> source, Element element) {
        super(source);
        this.element = element;
    }

    @Override
    public K getKey() {
        return (K) element.getKey();
    }

    @Override
    public V getValue() {
        return (V) element.getValue();
    }
}
