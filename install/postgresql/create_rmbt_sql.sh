#!/bin/bash

# dz
# 
# Create rmbt.sql - database schema for RTR-Netztest
# GIT location: /install/postgresql/rmbt.sql


# set -x

export LANG=C
DATE=`date '+%Y-%m-%d_%H-%M-%S'`
OUTPUT=rmbt.sql

# Check if file exists
test -r $OUTPUT                    && echo "$OUTPUT exists, aborting." &&  exit 0
# Create header with timestamp
echo "-- $DATE $OUTPUT" > $OUTPUT  
# Dump schema
pg_dump -s rmbt >> $OUTPUT
# Remove ^M (DOS-linebreaks)
sed -ie "s///" $OUTPUT
test -r $OUTPUT                     && echo "$OUTPUT created." && exit 1
