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

# inport open data from bev

# open data site (all annual releases)
# https://www.data.gv.at/katalog/dataset/verwaltungsgrenzen-vgd-stichtagsdaten-grundstucksgenau
# latest release
# VGD-Oesterreich_gst_01202020.zip
URL=https://nextcloud.bev.gv.at/nextcloud/index.php/s/5zXAdcpT5w7SM7R/download

DB=bev_vgd


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
SHP=`basename Oesterreich_BEV_VGD*.shp .shp`


# import as table statistik_austria_gem (takes some time)
# -I create geo index
# -s data is in "MGI / Austria Lambert" => code 31287
# -d drop table if it exists
shp2pgsql -I -s 31287 -d $SHP.shp $DB | psql rmbt > /dev/null


sql=$(cat <<EOF
BEGIN;
ALTER TABLE bev_vgd ADD COLUMN kg_nr_int INTEGER CONSTRAINT bev_vgd_kg_nr_int UNIQUE;
UPDATE bev_vgd SET kg_nr_int = kg_nr::INTEGER;
CREATE INDEX IF NOT EXISTS bev_vgd_kg_nr_int_gix ON bev_vgd USING btree(kg_nr_int);
CREATE INDEX IF NOT EXISTS bev_vgd_gkz_idx ON bev_vgd USING btree (gkz);
CREATE INDEX IF NOT EXISTS bev_vgd_kg_nr_idx ON bev_vgd USING btree (kg_nr);
ALTER TABLE bev_vgd OWNER TO rmbt;
GRANT SELECT ON TABLE bev_vgd TO rmbt_group_read_only;
ALTER TABLE bev_vgd ADD COLUMN bbox geometry;
UPDATE bev_vgd SET bbox=ST_EXPAND(geom,0.01);
CREATE INDEX IF NOT EXISTS bev_vgd_bbox_gix ON bev_vgd USING gist (bbox);
ANALYZE bev_vgd;
COMMIT;
VACUUM bev_vgd;

EOF
)
echo -e $sql|psql rmbt

echo done
