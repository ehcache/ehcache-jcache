Ehcache-JCache
==============

About
-----

*ehcache-jcache* is a full implementation of the API and SPI from from JSR107 (aka JCache). It provides a wrapper around an Ehcache cache
that allows allows you to use Ehcache as the caching provider using only JSR107 APIs.

More detailed information about how to use this is found under the [ehcache-jcache](https://github.com/jsr107/ehcache-jcache/tree/master/ehcache-jcache) 
module

Modules
--------------------
* [ehcache-jcache](https://github.com/jsr107/ehcache-jcache/tree/master/ehcache-jcache)
  This contains the ehcache-jcache implementation
* [jcache-tck-runner](https://github.com/jsr107/ehcache-jcache/tree/master/ehcache-jcache/jcache-tck-runner/)
  This runs the JSR107 TCK suite against the ehcache-jcache implementation to verify compliance with the spec.


Build
--------------------
* Just run the following command line (provided you have maven 3 already installed) :

    mvn clean install


* You may want to disable the run-tck profile (if you don't have it in your lcal maven repository) since it's not published to a maven repository :

    mvn clean install -P -run-tck


IRC
---

We will be using the `#jsr107` channel on Freenode for chat.


Issue tracker
-------------

Please log issues to: <https://github.com/jsr107/ehcache-jcache/issues>


License
-------

This software is provided under an Apache 2 open source license, read the `LICENSE.txt` file for details.


Contributors
------------

This free, open source software was made possible by Terracotta, Inc.. See the `CONTRIBUTORS.markdown` file for details.


Copyright
---------

Copyright (c) Terracotta
