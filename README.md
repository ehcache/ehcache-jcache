Ehcache-JCache
==============

About
-----

*ehcache-jcache* is a full implementation of the API and SPI from JSR-107 (aka JCache). It provides a wrapper around an Ehcache cache
that allows allows you to use Ehcache as the caching provider using only JSR-107 APIs.

More detailed information about how to use this is found under the [ehcache-jcache](https://github.com/Terracotta-OSS/ehcache-jcache/tree/master/ehcache-jcache)
module

Modules
--------------------
* [ehcache-jcache](https://github.com/Terracotta-OSS/ehcache-jcache/tree/master/ehcache-jcache)
  This contains the ehcache-jcache implementation
* [jcache-tck-runner](https://github.com/Terracotta-OSS/ehcache-jcache/tree/master/jcache-tck-runner)
  This runs the JSR107 TCK suite against the ehcache-jcache implementation to verify compliance with the spec.


Build
--------------------
* Just run the following command line (provided you have maven 3 already installed) :

    mvn clean install


* You may want to disable the run-tck profile (if you don't have the TCK in your local maven repository since it's not published to a maven repository) :

    mvn clean install -P -run-tck

* Current build status: [![Build Status](https://ehcache.ci.cloudbees.com/buildStatus/icon?job=ehcache-jcache)](https://ehcache.ci.cloudbees.com/job/ehcache-jcache/)

[![Build Status](http://www.cloudbees.com/sites/default/files/Button-Powered-by-CB.png)](http://www.cloudbees.com/)

Development
--------

Active development of the ehcache-jcache module follows changes to the spec. There will be no attempt to maintain backwards
compatibility between release versions; the focus of each release will be compliance with the latest JSR107 spec.

Release
--------

Following releases of the JSR107 spec APIs, an updated release milestone will be released and the latest stable release code will sit on the master
branch of the ehcache-jcache github repository.

Issue tracker
-------------

Please log issues to: <https://github.com/Terracotta-OSS/ehcache-jcache/issues>


License
-------

This software is provided under an Apache 2 open source license, read the `LICENSE.txt` file for details.


Contributors
------------

This free, open source software was made possible by Terracotta, Inc.. See the `CONTRIBUTORS.markdown` file for details.


Copyright
---------

Copyright (c) Terracotta

Using it
========

Maven
-----

Releases are available from Maven Central.

Snapshots are available from the Sonatype OSS snapshot repository.
In order to access the snapshots, you need to add the following repository to your pom.xml:
```xml
<repository>
    <id>sonatype-nexus-snapshots</id>
    <name>Sonatype Nexus Snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
