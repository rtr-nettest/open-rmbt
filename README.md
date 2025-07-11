Open-RMBT
=========

> *Open-RMBT* is an open source, multi-threaded bandwidth measurement system.

This is the original, mostly historic repository for RTR-Netztest (RTR-NetTest, aka Open-RMBT). Current code can be found in separate repositories as detailed below.

Open-RMBT consists of the following components available in different repositories:
* Web site +
* JavaScript client
* Android client +
* iOS client +
* Measurement server +
* QoS measurement server (in this repository)
* Control server +
* Statistics server +
* Map server +
* Desktop app +

+) These components are available in separate repositories. This repository still contains outdated and incompatible versions of these components. They shall not be used for production purposes. Plese contact us if you are uncertain about the compatiblity of components or repositories.

*Open-RMBT* is released under the [Apache License, Version 2.0](LICENSE). It was developed
by the [Austrian Regulatory Authority for Broadcasting and Telecommunications (RTR-GmbH)](https://www.rtr.at/).


Related material
----------------

* [RMBT specification](https://www.netztest.at/doc/)
* [RTR-NetTest/rmbt-server](https://github.com/rtr-nettest/rmbt-server) - Test Server for conducting measurements based on the RMBT protocol
* [RTR-NetTest/rmbtws](https://github.com/rtr-nettest/rmbtws) - JavaScript client for conducting RMBT-based speed measurements
* [RTR-NetTest/open-rmbt-control](https://github.com/rtr-nettest/open-rmbt-control) - Control server
* [RTR-NetTest/open-rmbt-statistics](https://github.com/rtr-nettest/open-rmbt-statistics) - Statistics server
* [RTR-NetTest/open-rmbt-map](https://github.com/rtr-nettest/open-rmbt-map) - Map server
* [RTR-NetTest/open-rmbt-ios](https://github.com/rtr-nettest/open-rmbt-ios) - iOS app
* [RTR-NetTest/open-rmbt-android](https://github.com/rtr-nettest/open-rmbt-android) - Android app
* [RTR-NetTest/open-rmbt-website](https://github.com/rtr-nettest/open-rmbt-website) - Web site
* [RTR-NetTest/open-rmbt-desktop](https://github.com/rtr-nettest/open-rmbt-desktop) - Desktop app



System requirements
-------------------

* 1-3 servers
* Everything can be installed on a single server 
* The test server (RMBT-Websocket) should run on a physical machine
* Base system Debian 12 or newer (or similar) 
* At least a single static public IPv4 address (IPv6 support recommended, more addresses allow to run more services on port 443)

  *NOTE: other Linux distributions can also be used, but commands and package names may be different*


Installation 
--------------

### For each server:

1. Setup IP/DNS/hostname
2. Firewall (e.g. iptables)
3. Install git
4. Install and configure sshd 
5. Install and configure ntp
6. dpkg-reconfigure locales (database requires en_US.UTF-8)
7. dpkg-reconfigure tzdata
8. Install and configure letsencrypt

### Database Server

1. Install:
    * postgresql-16
    * postgresql-contrib
    * postgresql-16-cron
    * postgis
    * postgresql-16-postgis-3


2. Configure pg_cron, add in
   /etc/postgresql/13/main/postgresql.conf
   ```
   # -- extension pg_cron
   # add to postgresql.conf

   # required to load pg_cron background worker on start-up
   shared_preload_libraries = 'pg_cron'
   # optionally, specify the database in which the pg_cron background worker should
   cron.database_name = 'rmbt'

   # Schedule jobs via background workers instead of localhost connections
   cron.use_background_workers = on
   ```

3. Run:

    ```bash
    # Restart database to enable pg_cron
    systemctl restart postgresql
    su - postgres
    # Create database users
    createuser -lSRDP rmbt     # (set password)
    createuser -lSRDP rmbt_control     # (set password)
    createuser -LSRD rmbt_group_control
    createuser -LSRD rmbt_group_read_only
    createuser -LSRD rmbt_group_read_only
    # The following two users are within schema, but not required
    # for basic functionality
    createuser -LSRD kibana # (for export)
    createuser -LSRD nagios # (for monitoring) 
    # Additional users might be required for replication
    echo 'GRANT rmbt_group_read_only TO rmbt_group_control;' | psql
    echo 'GRANT rmbt_group_control TO rmbt_control;' | psql

    # Create database
    createdb -O rmbt rmbt 
 
    # import database scheme    
    cat rmbt.sql | psql rmbt -1
    # import basic configuration (modifiy according to your needs)
    cat rmbt_init.sql | psql rmbt -1
    ```
    (optional: add additional 3rd party open data, eg. Corine)

4. Edit table "test_server"

   You need to add the test server key to the test_server table.
   
5. Edit `/etc/postgresql/13/main/postgresql.conf` to allow remote access for the ControlServer and other servers 
   (change `listen_addresses`) and configure `/etc/postgresql/13/main/pg_hba.conf` accordingly

6. Optimise postgres settings
   
    Check the values of 
    * shared_buffers (approx. 25% of available memory)
    * work_mem (approx25% of mem/max_connections)
    * effective_cache_size (approx. 50% of mem)
    * maintenance_work_mem (approx. 5% of mem)
    * max_worker_processes (as number of CPUs)
    * max_parallel_workers_per_gather (as number of CPUs)
    * max_parallel_workers (as number of CPUs)
    
     
    ```

Get in Touch
------------

* [RTR-Netztest](https://www.netztest.at) on the web
