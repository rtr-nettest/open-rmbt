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

# Import open data communities from Statistik Austria

# Open data site:
# http://data.statistik.gv.at/web/meta.jsp?dataset=OGDEXT_GEM_1
# current release
URL=http://data.statistik.gv.at/data/OGDEXT_GEM_1_STATISTIK_AUSTRIA_20190101.zip
# 2018: http://data.statistik.gv.at/data/OGDEXT_GEM_1_STATISTIK_AUSTRIA_20180101.zip

DB=statistik_austria_gem

rm STATISTIK_AUSTRIA_GEM*
wget $URL
unzip *.zip
rm *.zip
test -r ogd && rm ogd
# projection: EPSG:31287
shp2pgsql -s 31287 *.shp > statistik_austria_gem.sql
sed -i "s/statistik_austria_gem_20190101polygon/statistik_austria_gem/g" statistik_austria_gem.sql
rm STATISTIK_AUSTRIA_GEM*
# import data
psql rmbt < statistik_austria_gem.sql
sql=$(cat <<EOF
BEGIN;
ALTER TABLE statistik_austria_gem OWNER TO rmbt;
CREATE INDEX IF NOT EXISTS statistik_austria_gem_gix ON statistik_austria_gem USING gist (geom);
GRANT SELECT ON TABLE statistik_austria_gem TO rmbt_group_read_only;
ANALYZE statistik_austria_gem;
VACUUM statistik_austria_gem;
COMMIT;
EOF
)
echo -e $sql|psql rmbt

ls -l  statistik_austria_gem.sql
