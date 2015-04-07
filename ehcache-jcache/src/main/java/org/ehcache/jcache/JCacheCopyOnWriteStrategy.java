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
import net.sf.ehcache.Element;
import net.sf.ehcache.store.compound.ReadWriteCopyStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

/**
 * This class provides a copy strategy that is compatible with JSR107's requirement that
 * storeByValue caches will store the keys by value as well as the values.
 * <br />
 * Once ehcache handles these concerns natively, this class will be either deleted or
 * deprecated.
 * <br />
 * (in other words, you should not use this class directly)
 * <br />
 * (some code was copied from  net.sf.ehcache.store.compound.ReadWriteSerializationCopyStrategy which was written by
 *
 * @author Alex Snaps
 * @author Ludovic Orban
 *         )
 * @author Ryan Gardner
 * @since 0.4
 */
class JCacheCopyOnWriteStrategy implements ReadWriteCopyStrategy<Element> {

    /**
     * {@inheritDoc}
     *
     * Deep copies some object and returns an internal storage-ready copy
     */
    @Override
    public Element copyForWrite(final Element value, final ClassLoader classLoader) {
        if (value == null) {
            return null;
        } else {
            Object elementValue = value.getObjectValue();
            Object elementKey = value.getObjectKey();

            Object newKey = toObject(toByteArray(elementKey), classLoader);
            Object serializedValue = toObject(toByteArray(elementValue), classLoader);

            return duplicateElementWithNewValue(value, newKey, serializedValue);
        }
    }

    byte[] toByteArray(Object elementValue) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(bout);
            oos.writeObject(elementValue);
        } catch (Exception e) {
            throw new CacheException("When configured copyOnRead or copyOnWrite, a Store will only accept Serializable values", e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {
                //
            }
        }
        return bout.toByteArray();
    }


    /**
     * {@inheritDoc}
     *
     * Reconstruct an object from its storage-ready copy.
     */
    @Override
    public Element copyForRead(final Element storedValue, final ClassLoader classLoader) {
        if (storedValue == null) {
            return null;
        } else {
            Object newKey = toObject(toByteArray(storedValue.getObjectKey()), classLoader);
            Object deserializedValue = toObject(toByteArray(storedValue.getObjectValue()), classLoader);
            return duplicateElementWithNewValue(storedValue, newKey, deserializedValue);
        }
    }

    Object toObject(byte[] bytes, ClassLoader loader) {
        if (bytes == null) {
            return null;
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;
        Object deserializedElement;
        try {
            ois = new PreferredClassLoaderObjectInputSteam(bin, loader);
            deserializedElement = ois.readObject();
        } catch (Exception e) {
            throw new CacheException("When configured copyOnRead or copyOnWrite, a Store will only accept Serializable values", e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (Exception e) {
                //
            }
        }
        return deserializedElement;
    }

    /**
     * Make a duplicate of an element but using the specified value
     *
     * @param element  the element to duplicate
     * @param newValue the new element's value
     * @return the duplicated element
     * @see net.sf.ehcache.store.compound.ReadWriteSerializationCopyStrategy#duplicateElementWithNewValue(net.sf.ehcache.Element, Object)
     * @since 1.4.0-beta1
     */
    Element duplicateElementWithNewValue(final Element element, final Object newKey, final Object newValue) {
        return new Element(newKey, newValue, element.getVersion(),
                element.getCreationTime(), element.getLastAccessTime(), element.getHitCount(), element.usesCacheDefaultLifespan(),
                element.getTimeToLive(), element.getTimeToIdle(), element.getLastUpdateTime());
    }

    /**
     * This class provides a way to satisfy the requirements of JSR107 in being able to specify a classloader to deserialize
     * objects with
     */
    private static class PreferredClassLoaderObjectInputSteam extends ObjectInputStream {
        private ClassLoader classLoader;

        /**
         * Constructor
         *
         * @param in the input stream
         * @throws IOException if the constructor of ObjectInputStream throws an exception
         */
        public PreferredClassLoaderObjectInputSteam(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {

            return Class.forName(desc.getName(), false, classLoader);

        }

    }

}
