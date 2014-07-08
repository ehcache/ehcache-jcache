package org.ehcache.jcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JCacheManagerTest {

  @Test
  public void testUnwrapReturnsCacheManagerType() {
    JCacheManager jCacheManager = new JCacheManager(null, CacheManager.getInstance(), null, null);
    final CacheManager unwrap = jCacheManager.unwrap(CacheManager.class);
    assertThat(unwrap, notNullValue());
    assertThat(unwrap, instanceOf(CacheManager.class));
  }

  @Test
  public void testUnwrapThrowsOnUnsupportedType() {
    JCacheManager jCacheManager = new JCacheManager(null, CacheManager.getInstance(), null, null);
    try {
      jCacheManager.unwrap(Cache.class);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

}