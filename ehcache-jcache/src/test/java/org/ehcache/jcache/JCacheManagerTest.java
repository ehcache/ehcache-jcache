package org.ehcache.jcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.Test;

import javax.cache.configuration.MutableConfiguration;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
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

  @Test
  public void testCreateCacheThrowsOnMissingCacheLoaderFactory() {
    JCacheManager jCacheManager = new JCacheManager(null, CacheManager.getInstance(), null, null);
    final MutableConfiguration<Object, Object> configuration = new MutableConfiguration<Object, Object>().setReadThrough(true);
    try {
      jCacheManager.createCache("foo", configuration);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage().contains("Factory<CacheLoader>"), is(true));
    }
  }

  @Test
  public void testCreateCacheThrowsOnMissingCacheWriterFactory() {
    JCacheManager jCacheManager = new JCacheManager(null, CacheManager.getInstance(), null, null);
    final MutableConfiguration<Object, Object> configuration = new MutableConfiguration<Object, Object>().setWriteThrough(true);
    try {
      jCacheManager.createCache("foo", configuration);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage().contains("Factory<CacheWriter>"), is(true));
    }
  }

}