#!/bin/bash
#/*******************************************************************************
# * Copyright 2019 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
# * 
# * Licensed under the Apache License, Version 2.0 (the "License");
# * you may not use this file except in compliance with the License.
# * You may obtain a copy of the License at
# * 
# *   http://www.apache.org/licenses/LICENSE-2.0
# * 
# * Unless required by applicable law or agreed to in writing, software
# * distributed under the License is distributed on an "AS IS" BASIS,
# * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# * See the License for the specific language governing permissions and
# * limitations under the License.
# ******************************************************************************/

set -x
export LANG=C

# Import open data fixed network coverage information

# Open data site:
# https://www.data.gv.at/katalog/dataset/breitbandatlas
# current release
URL=https://www.data.gv.at/katalog/dataset/588b9fdc-d2dd-4628-b186-f7b974065d40/resource/3eb11c2d-9567-4a07-abb1-ca06d7d27a55/download/festnetz_2020q1_20201016.zip


mkdir ~/open
cd ~/open

rm *.csv

wget $URL
unzip *.zip
rm *.zip

# Note file name contains date, might change in the future
ls -l *.csv
mv *.csv import.csv

# import CSV

sql=$(cat <<EOF
BEGIN;
TRUNCATE TABLE cov_bb_fixed;
COPY cov_bb_fixed(raster,operator,technology,dl_max_mbit,ul_max_mbit,date)
FROM '/var/lib/postgresql/open/import.csv'
DELIMITER ','
CSV HEADER; 
ANALYSE cov_bb_fixed;
COMMIT;
VACUUM cov_bb_fixed;
EOF
)
echo -e $sql|psql rmbt

rm import.csv

echo done
