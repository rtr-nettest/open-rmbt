Open-RMBT
=========

> *Open-RMBT* is an open source, multi-threaded bandwidth measurement system.

It consists of the following components:
* Web site
* JavaScript client
* Android client +
* iOS client 
* Measurement server
* QoS measurement server (in this repository)
* Control server 
* Statistics server +
* Map server (in this repository)

+) These components are available in separate repositories. This repository still contains outdated and incompatible versions of these components. They shall not be used for production purposes. Plese contact us if you are uncertain about the compatiblity of repositories.

*Open-RMBT* is released under the [Apache License, Version 2.0](LICENSE). It was developed
by the [Austrian Regulatory Authority for Broadcasting and Telecommunications (RTR-GmbH)](https://www.rtr.at/).


Related material
----------------

* [RMBT specification](https://www.netztest.at/doc/)
* [RTR-NetTest/rmbt-server](https://github.com/rtr-nettest/rmbt-server) - Test Server for conducting measurements based on the RMBT protocol
* [RTR-NetTest/rmbtws](https://github.com/rtr-nettest/rmbtws) - JavaScript client for conducting RMBT-based speed measurements
* [RTR-NetTest/open-rmbt-control](https://github.com/rtr-nettest/open-rmbt-control) - Control server
* [RTR-NetTest/open-rmbt-statistics](https://github.com/rtr-nettest/open-rmbt-statistics) - Statistics server
* [RTR-NetTest/open-rmbt-ios](https://github.com/rtr-nettest/open-rmbt-ios) - iOS app
* [RTR-NetTest/open-rmbt-android](https://github.com/rtr-nettest/open-rmbt-android) - Android app
* [RTR-NetTest/rtr-nettest/open-rmbt-website](https://github.com/rtr-nettest/open-rmbt-website) - Web site


System requirements
-------------------

* 1-3 servers
* Everything can be installed on a single server 
* The test server (RMBT-Websocket) should run on a physical machine
* Base system Debian 11 or newer (or similar) 
* At least a single static public IPv4 address (IPv6 support recommended, more addresses allow to run more services on port 443)

  *NOTE: other Linux distributions can also be used, but commands and package names may be different*


Installation 
--------------

### For each server:

1. Setup IP/DNS/hostname
2. firewall (e.g. iptables)
3. Install git
4. Install and configure sshd 
5. Install and configure ntp
6. dpkg-reconfigure locales (database requires en_US.UTF-8)
7. dpkg-reconfigure tzdata
8. Install and configure letsencrypt

### Database Server

1. Install:
    * postgresql (version 13 and higher)
    * postgresql-common
    * postgresql-contrib
    * postgis
    * postgresql-13-postgis-3
    * *for quantile extension; Install:*
      * devscripts
      * sudo
      * postgresql-server-dev-all (or ..-13)
      * pgxnclient
      * Run:
        ` pgxn install quantile`

2. Run:

    ```bash
    su - postgres
    createuser -lSRD rmbt     # (set db pass)
    createuser -lSRDP rmbt_control     # (set db pass)
    createuser -LSRD rmbt_group_control
    createuser -LSRD rmbt_group_read_only
    echo 'GRANT rmbt_group_read_only TO rmbt_group_control;' | psql
    echo 'GRANT rmbt_group_control TO rmbt_control;' | psql
    createdb -O rmbt rmbt
 
    # (additional users might be needed for replication and nagios)
    
    # if not using postgis 3, set the correct version
    #> sed -i "s/postgis-3/postgis-X\.Y/g rmbt.sql"
    cat rmbt.sql | psql rmbt -1
    cat rmbt_init.sql | psql rmbt -1
    ```
    (optional: add additional open databases, eg. Corine)

3. Edit table "test_server"

   You need to add the test server key to the test_server table.
   
4. Optimise postgres settings
   
    Check the values of 
    * shared_buffers
    * work_mem
    * max_worker_processes
    * max_parallel_workers_per_gather
    * max_parallel_workers
    
### MapServer

1. Install:
  * Apache Tomcat 9 or higher
  * nginx (optional, highly recommended)
  * openjdk-11-jre (do not use a higher version)
  * libservlet3.1-java

2. Edit `/etc/tomcat9/context.xml` (substitute parts with `[]`), add to `<Context>`:


    ```xml
    <Context>
    <!-- [...] -->
    <Resource 
       name="jdbc/rmbtro" 
       auth="Container"
       type="javax.sql.DataSource"
       maxActive="200" maxIdle="10" maxWait="10000"
       url="jdbc:postgresql://[db host]/rmbt"
       driverClassName="org.postgresql.Driver"
       username="rmbt" password="[read only pass]"
       description="DB RO Connection" />
    <!-- [...] -->
    </Context>
     
    ```
3. Build the server:
    
    The map server can be built with gradle:
    ```bash
    ./gradlew :RMBTMapServer:war 
    ```
    The war file is located in `RMBTMapServer/build/lib`.

4. Copy `RMBTMapServer.war` to `/var/lib/tomcat9/webapps/`
    
5. Add the package `libpostgresql-jdbc-java` from [Postgresql JDBC](https://jdbc.postgresql.org/) and restart tomcat9.

6. Optimize tomcat settings

    Check the values in /etc/default/tomcat9
    * JAVA_OPTS -Xmms MEM -Xmx MEM

Get in Touch
------------

* [RTR-Netztest](https://www.netztest.at) on the web
