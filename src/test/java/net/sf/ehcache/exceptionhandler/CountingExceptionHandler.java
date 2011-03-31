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

import net.sf.ehcache.Ehcache;

import java.util.ArrayList;
import java.util.List;

/**
 * A test handler, used to test the Exception handling mechanism
 *
 * @author <a href="mailto:gluck@gregluck.com">Greg Luck</a>
 * @version $Id: CountingExceptionHandler.java 796 2008-10-09 02:39:03Z gregluck $
 */
public class CountingExceptionHandler implements CacheExceptionHandler {

    /**
     * the list of handled exceptions, static so you can get them without a reference
     */
    public static final List HANDLED_EXCEPTIONS = new ArrayList();

    /**
     * Called if an Exception occurs in a Cache method. This method is not called
     * if an <code>Error</code> occurs.
     *
     * @param ehcache   the cache in which the Exception occurred
     * @param key       the key used in the operation, or null if the operation does not use a key
     * @param exception the exception caught
     */
    public void onException(Ehcache ehcache, Object key, Exception exception) {

        HandledException handledException = new HandledException(ehcache, key, exception);
        HANDLED_EXCEPTIONS.add(handledException);
    }

    /**
     * Clear counter
     */
    public static void resetCounters() {
        HANDLED_EXCEPTIONS.clear();
    }

    /**
     * A value object for each exception handled
     */
    public static class HandledException {
        private final Ehcache ehcache;
        private final Object key;
        private final Exception exception;

        /**
         * Constructor
         *
         * @param ehcache
         * @param key
         * @param exception
         */
        public HandledException(Ehcache ehcache, Object key, Exception exception) {
            this.ehcache = ehcache;
            this.key = key;
            this.exception = exception;
        }

        /**
         * @return
         */
        public Object getKey() {
            return key;
        }

        /**
         * @return underlying exception
         */
        public Exception getException() {
            return exception;
        }
    }


}
