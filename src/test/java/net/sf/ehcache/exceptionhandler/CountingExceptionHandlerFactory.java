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

package net.sf.ehcache.exceptionhandler;

import java.util.Properties;

/**
 * @author <a href="mailto:gluck@gregluck.com">Greg Luck</a>
 * @version $Id: CountingExceptionHandlerFactory.java 744 2008-08-16 20:10:49Z gregluck $
 */
public class CountingExceptionHandlerFactory extends CacheExceptionHandlerFactory {


    /**
     * Create an <code>CacheExceptionHandler</code>
     *
     * @param properties implementation specific properties. These are configured as comma
     *                   separated name value pairs in ehcache.xml
     * @return a constructed CacheExceptionHandler
     */
    public CacheExceptionHandler createExceptionHandler(Properties properties) {
        return new CountingExceptionHandler();
    }
}
