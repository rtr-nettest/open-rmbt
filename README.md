Open-RMBT
===============

*Open-RMBT* is an open source, multi-threaded bandwidth test written in Java and
C, consisting of:

- command line client
- Java Applet client
- Android client
- control Servlet based on Restlet
- map Servlet based on Restlet
- statistics Servlet based on Restlet
- test server (written in C)
- qos test server

*Open-RMBT* is released under the [Apache License, Version 2.0]. It was developed
by [alladin-IT GmbH] and financed by the
[Austrian Regulatory Authority for Broadcasting and Telecommunications (RTR-GmbH)] [RTR-GmbH].

 [alladin-IT GmbH]: http://alladin.at/
 [RTR]: https://www.rtr.at/
 [Apache License, Version 2.0]: http://www.apache.org/licenses/LICENSE-2.0

The following Eclipse projects are distributed in this release:

- **RMBTSharedCode** - common libraries and classes
- **RMBTUtil** - common libraries and classes
- **RMBTControlServer** - Servlet acting as control server for the clients
- **RMBTMapServer** - Servlet acting as map server
- **RMBTStatisticServer** - Servlet acting as statistics server
- **RMBTServer** - speed test server
- **RMBTQoSServer** - qos test server
- **RMBTClient** - client code used by *RMBTAndroid*, the command line client and the Applet
- **RMBTAndroid** - Android App


Dependencies
---------------

The following third party libraries are required dependencies:

### Google Play Services ###

- see <http://developer.android.com/google/play-services/setup.html>.
- copy "extras/google/google_play_services/libproject" as "google-play-services_lib" into the source distribution of *Open-RMBT*.


### Android Support Library ###

- see <http://developer.android.com/tools/extras/support-library.html>
- copy as "RMBTAndroid/libs/android-support-v13.jar"

### Guava ###

- Apache 2.0. License
- available at <https://code.google.com/p/guava-libraries/>
- copy as "RMBTSharedCode/lib/guava-18.0.jar"


### dnsjava ###

- BSD License
- available at <http://www.xbill.org/dnsjava/>
- copy as "RMBTSharedCode/lib/dnsjava-2.1.4.jar"


### PostgreSQL JDBC Driver ###

- BSD License
- available at <http://jdbc.postgresql.org/>
- copy as "RMBTSharedCode/lib/postgresql-9.2-1002.jdbc4.jar"


### JSON in Java ###

- MIT License (+ "The Software shall be used for Good, not Evil.")
- available at <http://www.json.org/java/index.html>
- copy as "RMBTSharedCode/lib/org.json.jar" and "RMBTClient/lib/org.json.jar"


### Simple Logging Facade for Java (SLF4J) ###

- MIT License
- available at <http://www.slf4j.org/>
- copy as "RMBTAndroid/libs/slf4j-android-1.5.8.jar"


### JOpt Simple ###

- MIT License
- available at <http://pholser.github.com/jopt-simple/>
- copy as "RMBTClient/lib/jopt-simple-3.2.jar"


### Apache Commons ###

- Apache 2.0 License
- available at <http://commons.apache.org/>
- copy as:
 - "RMBTClient/lib/commons-logging-1.1.1.jar"
 - "RMBTClient/lib/org.apache.httpclient.jar"
 - "RMBTClient/lib/org.apache.httpcore.jar"
 - "RMBTControlServer/WebContent/WEB-INF/lib/commons-csv-1.0.jar"
 - "RMBTControlServer/WebContent/WEB-INF/lib/commons-io-2.4.jar"


### Restlet Framework ###

- Version: 2.1
- Licenses:
  - Apache 2.0
  - LGPL license version 3.0
  - LGPL license version 2.1 
  - CDDL license version 1.0 or
  - EPL license version 1.0
- available at <http://restlet.org/>
- copy as:
  - "RMBTControlServer/WebContent/WEB-INF/lib/org.restlet.jar"
  - "RMBTControlServer/WebContent/WEB-INF/lib/org.restlet.ext.json.jar"
  - "RMBTControlServer/WebContent/WEB-INF/lib/org.restlet.ext.servlet.jar"
  - "RMBTMapServer/WebContent/WEB-INF/lib/org.restlet.jar"
  - "RMBTMapServer/WebContent/WEB-INF/lib/org.restlet.ext.json.jar"
  - "RMBTMapServer/WebContent/WEB-INF/lib/org.restlet.ext.servlet.jar"

### PostGIS/ODBC ###

- Version: 2.1
- Licenses:
  - GPL license version 2.0 (for PostGIS)
  - LGPL license version 2.1 (for PostGIS/JDBC)
- available at <http://postgis.net/>
- copy as:
  - "RMBTMapServer/WebContent/WEB-INF/lib/postgis.jar"
