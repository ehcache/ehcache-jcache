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

package net.sf.ehcache.loader;


import net.sf.ehcache.Ehcache;
import net.sf.ehcache.jcache.loader.JCacheLoader;
import net.sf.ehcache.jcache.loader.JCacheLoaderFactory;

import java.util.Map;
import java.util.Properties;

/**
 * A factory for creating counting cache loaders. This can be hooked up to the JCacheFactory by
 * specifying "cacheLoaderFactoryClassName" in the environment
 *
 * @author Greg Luck
 * @version $Id: CountingCacheLoaderFactory.java 804 2008-10-11 05:42:22Z gregluck $
 */
public class CountingCacheLoaderFactory extends JCacheLoaderFactory {


    /**
     * Creates a CacheLoader. This method is called from {@link net.sf.ehcache.jcache.JCacheFactory}
     *
     * @param environment the same environment passed into {@link net.sf.ehcache.jcache.JCacheFactory}. This factory can
     *                    extract any properties it needs from the environment.
     * @return a constructed CacheLoader
     */
    public JCacheLoader createCacheLoader(Map environment) {
        return new CountingCacheLoader();
    }

    /**
     * Creates a CacheLoader using the Ehcache configuration mechanism at the time the associated cache is created.
     *
     * @param properties implementation specific properties. These are configured as comma
     *                   separated name value pairs in ehcache.xml
     * @return a constructed CacheLoader
     */
    public JCacheLoader createCacheLoader(Ehcache cache, Properties properties) {
        return new CountingCacheLoader();
    }
}
