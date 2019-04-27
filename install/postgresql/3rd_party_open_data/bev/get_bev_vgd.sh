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

# open data site:
# https://www.data.gv.at/katalog/dataset/verwaltungsgrenzen-vgd-stichtagsdaten-grundstucksgenau/resource/acb1b6b7-504d-4bd3-856a-89afb9657381
# current release
URL=http://www.bev.gv.at/pls/portal/docs/PAGE/BEV_PORTAL_CONTENT_ALLGEMEIN/0200_PRODUKTE/UNENTGELTLICHE_PRODUKTE_DES_BEV/VGD-Oesterreich_gst.zip

rm Aktualitaetsstand.txt Oesterreich*
wget $URL
unzip *.zip
rm *.zip
test -r bev_vgd.sql && rm bev_vgd.sql
shp2pgsql -W LATIN1 -s 31287 *.shp > bev_vgd.sql

# rename database
sed -i "s/oesterreich_bev_vgd_lam/bev_vgd/g" bev_vgd.sql

# import data
psql rmbt < bev_vgd.sql
sql=$(cat <<EOF
BEGIN;
ALTER TABLE bev_vgd ADD COLUMN kg_nr_int INTEGER CONSTRAINT bev_vgd_kg_nr_int UNIQUE;
UPDATE bev_vgd SET kg_nr_int = kg_nr::INTEGER;
CREATE INDEX IF NOT EXISTS bev_vgd_kg_nr_int_gix ON bev_vgd USING btree(kg_nr_int);
CREATE INDEX IF NOT EXISTS bev_vgd_gkz_idx ON bev_vgd USING btree (gkz);
CREATE INDEX IF NOT EXISTS bev_vgd_kg_nr_idx ON bev_vgd USING btree (kg_nr);
ALTER TABLE bev_vgd OWNER TO rmbt;
GRANT SELECT ON TABLE bev_vgd TO rmbt_group_read_only;
ANALYZE bev_vgd;
VACUUM bev_vgd;
COMMIT;

EOF
)
echo -e $sql|psql rmbt

ls -l bev_vgd.sql
