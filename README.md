Open-RMBT
=========

> *Open-RMBT* is an open source, multi-threaded bandwidth test written in Java and
C, consisting of:

> * command line client
> * Java Applet client
> * Android client
> * control Servlet based on Restlet
> * map Servlet based on Restlet
> * statistics Servlet based on Restlet
> * qos test server

*Open-RMBT* is released under the [Apache License, Version 2.0](LICENSE). It was developed
by the [Austrian Regulatory Authority for Broadcasting and Telecommunications (RTR-GmbH)](https://www.rtr.at/).

The following projects are distributed in this release:

- **RMBTSharedCode** - common libraries and classes
- **RMBTUtil** - common libraries and classes
- **RMBTControlServer** - Servlet acting as control server for the clients
- **RMBTMapServer** - Servlet acting as map server
- **RMBTStatisticServer** - Servlet acting as statistics server
- **RMBTQoSServer** - qos test server
- **RMBTClient** - client code used by *RMBTAndroid*, the command line client and the Applet
- **RMBTAndroid** - Android App


Related materials
-----------------

* [RMBT specification](https://www.netztest.at/doc/)
* [RTR-Netztest/rmbt-server](https://github.com/rtr-nettest/rmbt-server) - Test Server for conducting measurements based on the RMBT protocol
* [RTR-Netztest/rmbtws](https://github.com/rtr-nettest/rmbtws) - JavaScript client for conducting RMBT-based speed measurements
* [RTR-Netztest/open-rmbt-ios](https://github.com/rtr-nettest/open-rmbt-ios) - iOS app


System requirements
-------------------

* 1-3 servers
* Everything can be installed on a single server
* The test servers (RMBT and Websocket) should run on a physical machine
* Base system Debian 9 or newer (or similar) 
* At least one static IPv4 address (IPv6 support recommended, more addresses allow to run more services on port 443)

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
    * postgresql (version 10 and higher)
    * postgresql-common
    * postgresql-contrib
    * postgis
    * postgresql-10-postgis-2.4
    * *for quantile extension; Install:*
      * devscripts
      * sudo
      * postgresql-server-dev-all (or ..-10)
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
    
    # if not using postgis 2.4, set the correct version
    #> sed -i "s/postgis-2\.4/postgis-X.Y/g rmbt.sql"
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
    
### Control-,  Map- and StatisticServer

1. Install:
  * Apache Tomcat 8 or higher
  * nginx (optional, highly recommended)
  * openjdk-8-jre (do not use a higher version)
  * libservlet3.1-java
  * geoip-database

2. Edit `/etc/tomcat7/context.xml` (substitute parts with `[]`), add to `<Context>`:

   For control server:
    ```xml
    <Context>
    <Resource 
       name="jdbc/rmbt" 
       auth="Container"
       type="javax.sql.DataSource"
       maxActive="200" maxIdle="10" maxWait="10000"
       url="jdbc:postgresql://[db host]/rmbt"
       driverClassName="org.postgresql.Driver"
       username="rmbt_control" password="[db r/w pass]"
       description="DB RW Connection" />
    <Parameter name="RMBT_SECRETKEY" value="[rmbt qos secret key]" override="false" />
    </Context>
    ```
    For statistic/map servers:
    
    ```xml
    <Context>
    <Resource 
       name="jdbc/rmbtro" 
       auth="Container"
       type="javax.sql.DataSource"
       maxActive="200" maxIdle="10" maxWait="10000"
       url="jdbc:postgresql://[db host]/rmbt"
       driverClassName="org.postgresql.Driver"
       username="rmbt" password="[read only pass]"
       description="DB RO Connection" />
    </Context>
     
    ```
3. Build the servers
    
    The servers can be built with gradle:
    ```bash
    ./gradlew :RMBTControlServer:war :RMBTMapServer:war :RMBTStatisticServer:war
    ```
    The war files are then located in `RMBT[Control/Map/Statistic]Server/build/lib`.

4. Copy `RMBTControlServer.war`, `RMBTMapServer.war` and/or `RMBTStatisticServer.war` to `/var/lib/tomcat7/webapps/`

    In case the Java-Postgres connector is missing:
    Add the package `libpostgresql-jdbc-java`
    manually add the jar to the `lib` folder (create folder if it does not exist)
    
    `cp /var/lib/tomcat7/webapps/RMBTControlServer/WEB-INF/lib/postgresql-XXX.jdbcYY.jar /var/lib/postgresql/netztest/RMBTSharedCode/lib/postgresql-XXX.jdbcYY.jar` 


5. Run `service tomcat8 restart`

6. Optimize tomcat settings

    Check the values in /etc/default/tomcat8
    * JAVA_OPTS -Xmms MEM -Xmx MEM

Get in Touch
------------

* [RTR-Netztest](https://www.netztest.at) on the web