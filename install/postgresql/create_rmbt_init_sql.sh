#!/bin/bash

# dz
# 
# Create rmbt_init.sql - database initialization
# GIT location: /install/postgresql/rmbt_init.sql


# set -x

export LANG=C
DATE=`date '+%Y-%m-%d_%H-%M-%S'`
OUTPUT=rmbt_init.sql
# List of tables to be dumped
declare -a TABLES=("as2provider" "client_type" "mcc2country" "mccmnc2name" "mccmnc2provider" "network_type" "provider" \
  "qos_test_desc" "qos_test_objective" "qos_test_type_desc")

# Check if file exists
test -r $OUTPUT                    && echo "$OUTPUT exists, aborting." &&  exit 0
# Create header with timestamp
echo "-- $DATE $OUTPUT" > $OUTPUT  
echo "-- " > $OUTPUT  
# Dump tables

# Loop through all tables
for i in "${TABLES[@]}"
do
   # Update on progress
   echo "Dumping table $i..."
   echo "-- $DATE $OUTPUT table $i" > $i.sql
   pg_dump rmbt --table=$i --column-inserts >> $i.sql
   cat $i.sql >> $OUTPUT
done

# Include dummy tables



# List of tables to be included from templates
declare -a TABLES=("device_map" "news" "settings" "test_server")

# Include templates

# Loop through all template tables
for i in "${TABLES[@]}"
do
   # Update on progress
   echo "Including template table $i..."
   echo "-- $DATE $OUTPUT template table $i" >> $OUTPUT
   cat $i-template.sql >> $OUTPUT
done

test -r $OUTPUT                     && echo "$OUTPUT created." && exit 1
