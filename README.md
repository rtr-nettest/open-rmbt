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

*Open-RMBT* is released under the [Apache License, Version 2.0]. It was developed
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


System requirements
-------------------

* 1-4 physical or virtual servers
* Everything can also be installed on a single server
* The test servers (RMBT and Websocket) should run on a physical machine
* Base system Debian 8 or newer (or similar) 
* At least on static IPv4 address (IPv6 support recommended, more addresses allow to run more services on port 443)

  *NOTE: (other Linux distributions can also be used, but commands and package names may need to be changed)*


Installation 
--------------

### For each server:

1. Setup IP/DNS/hostname
2. firewall (e.g. iptables)
3. Install/Setup sshd 
4. Install/Setup ntpd
5. dpkg-reconfigure locales
6. dpkg-reconfigure tzdata

### Database Server

1. Install:
    * postgresql
    * postgresql-common
    * postgresql-contrib
    * postgis
    * postgresql-9.6-postgis-2.3
    * *for quantile extension; if not found in distribution. Install:*
      * devscripts
      * sudo
      * postgresql-server-dev-all (or ..-9.1)
      * pgxnclient
      * Run:
        ` pgxn install quantile`

2. Run:

    ```bash
    su - postgres
    createuser -lSRD rmbt     (add -P if you want to set a password)
    createuser -lSRDP rmbt_control     (set db pass)
    createuser -LSRD rmbt_group_control
    createuser -LSRD rmbt_group_read_only
    createuser -LSRD rmbt_web_admin
    echo 'GRANT rmbt_group_read_only TO rmbt_group_control;' | psql
    echo 'GRANT rmbt_group_control TO rmbt_control;' | psql
    createdb -O rmbt rmbt
    
    # if not using postgis 2.1, set the correct version
    #> sed -i "s/postgis-2\.1/postgis-2.3/g rmbt.sql"
    
    cat rmbt.sql | psql rmbt -1
    cat rmbt_init.sql | psql rmbt -1
    cat rmbt_qos_init.sql | psql rmbt -1
    ```
    

3. Edit table "test_server"

   You need to add the key found in secret.h to the test_server table.

### Control and Mapserver

1. Install:
  * Apache Tomcat 7 or higher
  * nginx (optional)
  * openjdk-7-jre or higher
  * openjdk-7-jdk (NOT openjdk-6...!) or higher
  * libservlet3.1-java

2. Edit `/etc/tomcat7/context.xml` (substitute parts with `[]`), add to `<Context>`:

    ```xml
    <Parameter name="RMBT_SECRETKEY" value="[rmbt secret key]" override="false"/>
    <Resource 
       name="jdbc/rmbt" 
       auth="Container"
       type="javax.sql.DataSource"
       maxActive="75" maxIdle="10" maxWait="10000"
       url="jdbc:postgresql://[db host]/rmbt"
       driverClassName="org.postgresql.Driver"
       username="[db user]" password="[db pass]"
       description="DB Connection" />
    
    <Parameter name="RMBT_MAP_HOST" value="[map host]" override="false"/>
    <Parameter name="RMBT_MAP_PORT" value="[map port]" override="false"/>
    <Parameter name="RMBT_MAP_SSL" value="[map server ssl ? true : false]" override="false"/>
    ```

3. Build the servers
    
    The servers can be built with gradle:
    ```bash
    ./gradlew :RMBTControlServer:war :RMBTMapServer:war :RMBTStatisticServer:war
    ```
    The war files are then located in `RMBT[Control/Map/Statistic]Server/build/lib`.

4. Copy `RMBTControlServer.war`, `RMBTMapServer.war` and/or `RMBTStatisticServer.war` to `/var/lib/tomcat7/webapps/`

    In case the Java-Postgres connector is missing and not provided 
    by the package `libpostgresql-jdbc-java` (problem with Debian8) 
    manually add the jar to the `lib` folder (create folder if it does not exist)
    
    `cp /var/lib/tomcat7/webapps/RMBTControlServer/WEB-INF/lib/postgresql-9.4-1201.jdbc41.jar /var/lib/postgresql/netztest/RMBTSharedCode/lib/postgresql-9.4-1201.jdbc41.jar` 


5. Run `service tomcat7 restart`

Get in Touch
------------

* [RTR-Netztest](https://www.netztest.at) on the web