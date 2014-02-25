Ehcache-JCache
==============

About
-----

*ehcache-jcache* is a full implementation of the API and SPI from from JSR107 (aka JCache). 
It provides a wrapper around an Ehcache cache that allows allows you to use Ehcache as the 
caching provider using only JSR107 APIs.

Getting Started
---------------
### Including in your project ###
To include this in your project, you need to include:

```xml
<dependency>
   <groupId>org.ehcache</groupId>
   <artifactId>jcache</artifactId>
   <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Configuring a JCache ###

There are two ways to configure a JCache.

1. Programatically using a CacheBuilder
2. Declaratively using an ehcache-*.xml file

### Configuring a JCache Programatically ###

The `JCacheCacheManager` is responsible for creating a JCache that delegates the storage and retrieval of cache
elements to an underlying ehcache.

The `CacheManager` can be created manually, or you can use the `Caching` singleton entrypoint to retrieve it.

```java
    Cache foo = Caching.getCacheManager().createCacheBuilder("foo").build();
````

You can set additional parameters on the cache as well. For instance, to create a new cache that will have entries
expire 10 minutes after they are created (or last modified) that stores cache values as references:

```java
    Cache blarg = Caching.getCacheManager().createCacheBuilder("blarg")
            .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new Duration(TimeUnit.MINUTES, 10))
            .setStoreByValue(false)
            .build();
```

Currently only the configuration parameters specified in the JSR107 spec are exposed via the builder interface.
You can also configure caches declartively in ehcache's well-known xml cache configuration format.

When you create a named cache manager, the jcache-ehcache provider will look in the classpath for a file named
"`ehcache-NAME.xml`" (where NAME is the name of the cache manager you are creating).

If you have a file named `ehcache-jcache-example.xml`, for instance, then when you call:

```java
    Cache boo = Caching.getCacheManager("jcache-example").getCache("boo");
```

The cache will be configured based on the parameters set in the `ehcache-jcache-example.xml` file in the classpath.
In that xml file additional parameters (such as the size of the cache) can be configured.

**Note: The defaultCache entry in an xml configuration file is not used for caches created by the JCacheManager**

As part of the specification of JSR107, every cache created programatically via ```getCacheManager().getCache()``` 
uses the same default settings regardless of the underlying caching provider. This implementation honors that part of the specification which means that you will 
need to explicitly define the entries in the cache config file. 

Using with JSR107 annotations
-------------
The reference implementation of the JSR107 annotations can be used with any JSR107 caching provider.
There are annotation implementations provided for both CDI and Spring that will work with ehcache-jcache.
For more information on annotations, see <https://github.com/jsr107/RI/tree/master/cache-annotations-ri>

If you want to use annotations with this (or any other JSR107 provider) you need to also include:

**For spring annotations** _(compatible with Spring 3.0.6+)_:

```xml
<dependency>
   <groupId>org.jsr107.ri</groupId>
   <artifactId>cache-annotations-ri-spring</artifactId>
   <version>0.4</version>
</dependency>
```

**For CDI annotations:**

```xml
<dependency>
   <groupId>org.jsr107.ri</groupId>
   <artifactId>cache-annotations-ri-cdi</artifactId>
   <version>0.4</version>
</dependency>
```

Copyright
---------

Copyright (c) Terracotta