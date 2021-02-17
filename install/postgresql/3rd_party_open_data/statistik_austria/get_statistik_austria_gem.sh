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
URL=https://data.statistik.gv.at/data/OGDEXT_GEM_1_STATISTIK_AUSTRIA_20200101.zip
# 2019: http://data.statistik.gv.at/data/OGDEXT_GEM_1_STATISTIK_AUSTRIA_20190101.zip
# 2018: http://data.statistik.gv.at/data/OGDEXT_GEM_1_STATISTIK_AUSTRIA_20180101.zip
SHP=STATISTIK_AUSTRIA_GEM_20200101Polygon
DB=statistik_austria_gem


mkdir ~/open
cd ~/open


rm $SHP.*
wget $URL
unzip *.zip
rm *.zip


# import as table statistik_austria_gem (takes some time)
# -I create geo index
# -s data is in "MGI / Austria Lambert" => code 31287
# -d drop table if it exists
shp2pgsql -I -s 31287 -d $SHP.shp statistik_austria_gem | psql rmbt > /dev/null

# typical record
# INSERT INTO "statistik_austria_gem" ("id","name",geom) VALUES ('10706','Gattendorf','010600..')

# bbox?

# owner
sql=$(cat <<EOF
BEGIN;
ANALYZE statistik_austria_gem;
ALTER TABLE statistik_austria_gem OWNER TO rmbt;
GRANT SELECT ON TABLE statistik_austria_gem TO rmbt_group_read_only;
ALTER TABLE statistik_austria_gem ADD COLUMN bbox geometry;
UPDATE statistik_austria_gem SET bbox=ST_EXPAND(geom,0.01);
CREATE INDEX IF NOT EXISTS statistik_austria_gem_bbox_gix ON statistik_austria_gem USING gist (bbox);
ANALYZE statistik_austria_gem;
COMMIT;
VACUUM statistik_austria_gem;
EOF
)
echo -e $sql|psql rmbt


echo done
