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


import net.sf.ehcache.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A timer service used to check performance of tests.
 * <p/>
 * To enable this to work for different machines the following is done:
 * <ul>
 * <li>SimpleLog is used for logging with a known logging level controlled by <code>simplelog.properties</code>
 * which is copied to the test classpath. This removes logging as a source of differences.
 * Messages are sent to stderr which also makes it easy to see messages on remote continuous integration
 * machines.
 * <li>A speedAdjustmentFactor is used to equalize machines. It is supplied as a the System Property
 * 'net.sf.ehcache.speedAdjustmentFactor=n', where n is the number of times the machine is slower
 * than the reference machine e.g. 1.1. This factor is then used to adjust "elapsedTime"
 * as returned by this class. Elapsed Time is therefore not true time, but notional time equalized with the reference
 * machine. If you get performance tests failing add this property.
 * </ul>
 *
 * @author Greg Luck
 * @version $Id: StopWatch.java 796 2008-10-09 02:39:03Z gregluck $
 *          A stop watch that can be useful for instrumenting for performance
 */
public class StopWatch {

    private static final Logger LOG = LoggerFactory.getLogger(StopWatch.class);


    private static final String SUFFIX = "ms";


    /**
     * An attempt to adjust performance tests to different machines.
     */
    private static float speedAdjustmentFactor = 1;


    /**
     * Used for performance benchmarking
     */
    private long timeStamp = System.currentTimeMillis();


    /**
     * Get the speed adjustment factor
     */
    public static float getSpeedAdjustmentFactor() {
        return speedAdjustmentFactor;
    }


    static {

        String speedAdjustmentFactorString =
                PropertyUtil.extractAndLogProperty("net.sf.ehcache.speedAdjustmentFactor", System.getProperties());

        if (speedAdjustmentFactorString != null) {
            try {
                speedAdjustmentFactor = Float.parseFloat(speedAdjustmentFactorString);
            } catch (NumberFormatException e) {
                LOG.debug("Consider setting system property 'net.sf.ehcache.speedAdjustmentFactor=n' " +
                        "where n is the number of times your machine is slower than the reference machine, " +
                        "which is currently a dual G5 PowerMac. e.g. 1.2, which then enables elasped time " +
                        "measurement to be adjusted accordingly.");
            }
            LOG.debug("Using speedAjustmentFactor of " + speedAdjustmentFactor);

        } else {
            LOG.debug("Consider setting system property 'net.sf.ehcache.speedAdjustmentFactor=n' " +
                    "where n is the number of times your machine is slower than the reference machine, " +
                    "which is currently a dual G5 PowerMac. e.g. 1.2, which then enables elasped time " +
                    "measurement to be adjusted accordingly.");
        }

        StopWatch stopWatch = new StopWatch();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            //
        }
        LOG.debug("100 measures as " + stopWatch.getElapsedTime());


    }

//    static {
//
//        float referenceTime = 2050;
//        CacheManager singletonManager = CacheManager.create(AbstractCacheTest.TEST_CONFIG_DIR + "ehcache-big.xml");
//
//        String[] names = singletonManager.getCacheNames();
//        for (int i = 0; i < names.length; i++) {
//            String name = names[i];
//            Ehcache cache = singletonManager.getCache(name);
//            for (int j = 0; i < 100; i++) {
//                cache.put(new Element(new Integer(j), "value"));
//            }
//        }
//        long start = System.currentTimeMillis();
//        for (int repeats = 0; repeats < 5000; repeats++) {
//            for (int i = 0; i < names.length; i++) {
//                String name = names[i];
//                Ehcache cache = singletonManager.getCache(name);
//                for (int j = 0; i < 100; i++) {
//                    Element element = cache.get(name + j);
//                    if ((element == null)) {
//                        cache.put(new Element(new Integer(j), "value"));
//                    }
//                }
//            }
//        }
//        long elapsedTime = System.currentTimeMillis() - start;
//
//        LOG.error("It took this machine: " + elapsedTime + " to perform a time trial compared with the reference time of "
//                + referenceTime + "ms");
//
//        speedAdjustmentFactor = elapsedTime / referenceTime;
//
//        LOG.severe("Elapsed stopwatch times will be adjusted divided by " + speedAdjustmentFactor);
//    }


    /**
     * Gets the time elapsed between now and for the first time, the creation
     * time of the class, and after that, between each call to this method
     * <p/>
     * Note this method returns notional time elapsed. See class description
     */
    public long getElapsedTime() {
        long now = System.currentTimeMillis();
        long elapsed = (long) ((now - timeStamp) / speedAdjustmentFactor);
        timeStamp = now;
        return elapsed;
    }

    /**
     * @return formatted elapsed Time
     */
    public String getElapsedTimeString() {
        return String.valueOf(getElapsedTime()) + SUFFIX;
    }

}


