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
import net.sf.ehcache.constructs.EhcacheDecoratorAdapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Simple decorator that retains a reference to the JCache adpater.
 * <p/>
 * This makes is possible to retrieve the JSR107 cache from a reference on just the ehcache
 * (for instance, to support adapting EHCache events to JSR107 events)
 *
 * @param <K> the type of keys used in the JCache that this ehcache decorator wraps
 * @param <V> the type of values used in the JCache that this ehcache decorator wraps
 * @author Ryan Gardner
 * @since 1.4.0-beta1
 */
public class JCacheEhcacheDecorator<K, V> extends EhcacheDecoratorAdapter {
    private JCache<K, V> jcache;

    /**
     * <p>Constructor for JCacheEhcacheDecorator.</p>
     *
     * @param underlyingCache a {@link net.sf.ehcache.Ehcache} object.
     * @since 1.4.0-beta1
     */
    public JCacheEhcacheDecorator(Ehcache underlyingCache) {
        super(underlyingCache);
    }

    /**
     * <p>Setter for the field {@code jcache}.</p>
     *
     * @param jcache a {@link JCache} object.
     * @since 1.4.0-beta1
     */
    public void setJcache(JCache<K, V> jcache) {
        this.jcache = jcache;
    }

    /**
     * <p>Constructor for JCacheEhcacheDecorator.</p>
     *
     * @param underlyingCache a {@link net.sf.ehcache.Ehcache} object.
     * @param jcache a {@link net.sf.ehcache.jcache.JCache} object.
     * @since 1.4.0-beta1
     */
    public JCacheEhcacheDecorator(Ehcache underlyingCache, JCache<K, V> jcache) {
        super(underlyingCache);
        this.jcache = jcache;
    }

    /** {@inheritDoc} */
    @Override
    public void put(Element element) throws IllegalArgumentException, IllegalStateException, CacheException {
        Element newElement = element;
        if (this.jcache.getConfiguration().isStoreByValue()) {
            K key = cloneKeyValue((K) element.getKey());
            newElement = duplicateElementWithNewKey(element, key);
        }
        super.put(newElement);
    }

    /**
     * <p>duplicateElementWithNewKey.</p>
     *
     * @param element a {@link net.sf.ehcache.Element} object.
     * @param newKey a {@link java.lang.Object} object.
     * @return a {@link net.sf.ehcache.Element} object.
     * @since 1.4.0-beta1
     */
    protected Element duplicateElementWithNewKey(final Element element, final Object newKey) {
        return new Element(newKey, element.getValue(), element.getVersion(),
                element.getCreationTime(), element.getLastAccessTime(), element.getHitCount(), element.usesCacheDefaultLifespan(),
                element.getTimeToLive(), element.getTimeToIdle(), element.getLastUpdateTime());
    }


    private K cloneKeyValue(K key) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);

            oos.writeObject(key);
            oos.flush();
            ByteArrayInputStream bin =
                    new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);

            return (K) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new CacheException("Unable to clone Key", e);
        } catch (IOException e) {
            throw new CacheException("Unable to clone key", e);
        }
    }

    /**
     * <p>Getter for the field {@code jcache}.</p>
     *
     * @return a {@link JCache} object.
     * @since 1.4.0-beta1
     */
    public JCache<K, V> getJcache() {
        return jcache;
    }
}
