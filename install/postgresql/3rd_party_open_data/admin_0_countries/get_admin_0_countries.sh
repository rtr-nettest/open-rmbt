#!/bin/bash
#/*******************************************************************************
# * Copyright 2022 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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

# import country boarders from https://www.naturalearthdata.com

# 
# https://www.naturalearthdata.com/downloads/10m-cultural-vectors/
#
# latest release 5.1.1, with boundary lakes
URL=https://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/cultural/ne_10m_admin_0_countries_lakes.zip

# name of postgres table
TABLE=admin_0_countries

# create tmp dir (remains after finalization)
mkdir -p ~/open/$TABLE
cd ~/open/$TABLE && rm *

wget $URL -O $TABLE.zip
unzip -o $TABLE.zip
# rm $TABLE.zip
SHP=$(ls *.shp)

# import as table statistik_austria_gem (takes some time)
# -I create geo index
# -s data is WGS84 => EPSG4326
# -d drop table if it exists
# -W encoding - eg. LATIN1 (not used, is in UTF8)
shp2pgsql -I -s 4326 -d $SHP $TABLE  | psql rmbt > /dev/null

# final fixup (owner, permissions etc.)
sql=$(cat <<EOF
BEGIN;
ALTER TABLE $TABLE OWNER TO rmbt;
GRANT SELECT ON TABLE $TABLE TO rmbt_group_read_only;
ANALYZE $TABLE;
COMMIT;

EOF
)
echo -e $sql|psql rmbt

echo done

#
