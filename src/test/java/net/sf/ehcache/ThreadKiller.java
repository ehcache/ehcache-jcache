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

package net.sf.ehcache;

import java.io.IOException;
import java.io.Serializable;


/**
 * A class which throws a RuntimeException when serialized or deserialized. This
 * should be enough to kill threads that do not have special handling.
 *
 * @author Greg Luck
 * @version $Id: ThreadKiller.java 796 2008-10-09 02:39:03Z gregluck $
 */
public class ThreadKiller extends Object implements Serializable {

    private static final long serialVersionUID = 293659559026635155L;

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        throw new Error("Thread killer strikes");

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        throw new Error("Thread killer strikes");
    }

}
