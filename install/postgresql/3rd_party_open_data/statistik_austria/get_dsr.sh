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

# import DauerSiedlungsraum (permanent settlement area) from Statistik Austria

# 
# https://www.data.gv.at/katalog/dataset/stat_dauersiedlungsraum1ee4b/resource/4903006f-2405-4a98-b5fe-4c5b9ae2472a
#
# latest release as of 2016-02-22 
URL=http://data.statistik.gv.at/data/OGDEXT_DSR_1_STATISTIK_AUSTRIA_20111031.zip

DB=dsr


mkdir ~/open
cd ~/open

rm $DB.zip
rm $SHP.*


wget $URL -O $DB.zip
# -o overwrite without prompting
unzip -o *.zip

rm *.zip

# File name might change (contains date)
# e.g Oesterreich_BEV_VGD_LAM_040121
SHP=`basename STATISTIK_AUSTRIA_DSR*.shp .shp`


# import as table statistik_austria_gem (takes some time)
# -I create geo index
# -s data is in "MGI / Austria Lambert" => code 31287
# -d drop table if it exists
# -W encoding LATIN1
shp2pgsql -I -s 31287 -d $SHP.shp $DB | psql rmbt > /dev/null


sql=$(cat <<EOF
BEGIN;
ALTER TABLE dsr OWNER TO rmbt;
GRANT SELECT ON TABLE dsr TO rmbt_group_read_only;
ANALYZE dsr;
COMMIT;
VACUUM dsr;

EOF
)
echo -e $sql|psql rmbt

echo done
