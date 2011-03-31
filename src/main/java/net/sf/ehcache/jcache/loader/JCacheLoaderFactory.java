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

package net.sf.ehcache.jcache.loader;

import net.sf.ehcache.loader.CacheLoaderFactory;
import net.sf.ehcache.Ehcache;

import java.util.Map;
import java.util.Properties;


/**
 * An abstract factory for creating cache loaders. Implementers should provide their own
 * concrete factory extending this factory.
 * <p/>
 * There is one factory method for JSR107 Cache Loaders and one for Ehcache ones. The Ehcache
 * loader is a sub interface of the JSR107 Cache Loader.
 * <p/>
 * Note that both the JCache and Ehcache APIs also allow the CacheLoader to be set programmatically.
 * @author Greg Luck
 * @version $Id: CacheLoaderFactory.java 804 2008-10-11 05:42:22Z gregluck $
 */
public abstract class JCacheLoaderFactory extends CacheLoaderFactory {

    /**
     * Creates a CacheLoader using the JSR107 creational mechanism.
     * This method is called from {@link net.sf.ehcache.jcache.JCacheFactory}
     *
     * @param environment the same environment passed into {@link net.sf.ehcache.jcache.JCacheFactory}.
     * This factory can extract any properties it needs from the environment.
     * @return a constructed CacheLoader
     */
    public abstract JCacheLoader createCacheLoader(Map environment);

    /**
     * Creates a CacheLoader using the Ehcache configuration mechanism at the time the associated cache
     *  is created.
     * @param cache a reference to the owning cache
     * @param properties implementation specific properties configured as delimiter
     *  separated name value pairs in ehcache.xml
     * @return a constructed CacheLoader
     */
    public abstract JCacheLoader createCacheLoader(Ehcache cache, Properties properties);

}
