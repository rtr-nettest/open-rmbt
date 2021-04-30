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

# Import open data mobile network/5G coverage information

# Open data

# Format: operator;reference;license;rfc_date;raster;dl_normal;ul_normal;dl_max;ul_max

#Definition of columns:
#operator: String
#- “A1TA” for A1 Telekom Austria AG
#- “TMA”: for T-Mobile Austria GmbH
#- “H3A”: for Hutchison Drei Austria GmbH
#-“LIWEST”: for  LIWEST Kabelmedien GmbH
#- “SBG”: for  Salzburg AG für Energie, Verkehr und Telekommunikation
#- “HGRAZ”: for  Holding Graz - Kommunale Dienstleistungen GmbH
#- “MASS”: for MASS Response Service GmbH
#reference: String
#- “F1/16” for data according to TKK decision F1/16 (mobile)
#- “F7/16” for data according to TKK decision F7/16 (3,5 GHz)
#license: String
#- “CCBY4.0”
#rfc_date: String
#- Date of simulation according to RFC 3339 (day only, no time)
#  Example: “2020-12-26”
#raster: String
#- 100m x 100m raster according to ETRS-LAEA
#  Example: “100mN27285E48011”
#dl_normal: Integer
# normaly available downlink speed in Bit/s (no decimals); zero if no coverage
#ul_normal: Integer
#- normaly available uplink speed in Bit/s (no decimals); zero if no coverage
#dl_max: Integer
#- estimated maximum downlink speed in Bit/s (no decimals); zero if no coverage
#ul_max: Integer
#- estimated maximum uplink speed in Bit/s (no decimals); zero if no coverage


# https://www.a1.net/5g-netzabdeckung-karte
# 
URL_A1TA=https://cdn11.a1.net/m/resources/media/excel/5GNR3500-20210331-versorgt.csv

# https://www.magenta.at/3_5Ghz
# URL_TMA=####
URL_TMA_NDL=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/Rohdaten_SPEEDMAP_DL_AVG.csv
URL_TMA_MDL=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/Rohdaten_SPEEDMAP_DL_MAX.csv
URL_TMA_NUL=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/Rohdaten_SPEEDMAP_UL_AVG.csv
URL_TMA_MUL=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/Rohdaten_SPEEDMAP_UL_MAX.csv

# https://www.drei.at/de/info/netzabdeckung/versorgungsdaten-35-ghz.html
URL_H3A=https://www.drei.at/media/common/info/netzabdeckung/h3a-versorgung-rohdaten.csv

# https://www.liwest.at/5g-fwa
URL_LIWEST=https://www.liwest.at/fileadmin/user_upload/5g/rtr_f716.CSV

#URL_HGRAZ=

# https://www.salzburg-ag.at/internet-tv-telefon/fuer-privat/internet/cablelink-air/netzabdeckung-air.html
URL_SBGAG=https://www.salzburg-ag.at/content/dam/web18/dokumente/cablelink/internet/RohdatenSalzburgAG3_5GHz.csv

# https://www.massresponse.com/versorgungsdaten3-5ghz/
URL_MASS=https://www.massresponse.com/versorgungsdaten3-5ghz/OpenDataRasterdatenMASS.csv

mkdir ~/open
cd ~/open

rm -rf *.csv
rm -rf xl/sharedStrings.xml

# download files
wget $URL_A1TA -O A1TA.csv
# remove all variants of typographic quotes
sed -i "s/\x93//g" A1TA.csv
sed -i "s/\x94//g" A1TA.csv
sed -i "s/\x22//g" A1TA.csv
#
wget $URL_TMA_NDL -O TMA_NDL.csv
sed -i "s/,/./g" TMA_NDL.csv
#
wget $URL_TMA_MDL -O TMA_MDL.csv
sed -i "s/,/./g" TMA_MDL.csv
#
wget $URL_TMA_NUL -O TMA_NUL.csv
sed -i "s/,/./g" TMA_NUL.csv
#
wget $URL_TMA_MUL -O TMA_MUL.csv
sed -i "s/,/./g" TMA_MUL.csv
#
wget $URL_H3A -O H3A.csv
#
wget $URL_LIWEST -O LIWEST.csv
#
wget $URL_SBGAG -O SBGAG.csv
#
wget $URL_MASS -O MASS.csv
# MASS: fix invalid strings
# ;4,00E+07;20000000;2,00E+08;40000000
sed -i "s/4,00E+07/40000000/g" MASS.csv
sed -i "s/2,00E+08/200000000/g" MASS.csv


# import CSV

sql=$(cat <<EOF
BEGIN;

CREATE TEMPORARY TABLE tmp_tma (uid serial,
"rfc_date" varchar (50),
"raster" varchar (50),
"dl_normal" double precision,
"ul_normal" double precision,
"dl_max" double precision,
"ul_max" double precision);

ALTER TABLE tmp_tma ADD PRIMARY KEY (uid);
CREATE INDEX tmp_tma_raster_idx
    ON tmp_tma(raster);

COPY tmp_tma(raster,dl_normal,rfc_date)
FROM '/var/lib/postgresql/open/TMA_NDL.csv'
DELIMITER ';'
CSV HEADER;

COPY tmp_tma(raster,dl_max,rfc_date)
FROM '/var/lib/postgresql/open/TMA_MDL.csv'
DELIMITER ';'
CSV HEADER;

COPY tmp_tma(raster,ul_normal,rfc_date)
FROM '/var/lib/postgresql/open/TMA_NUL.csv'
DELIMITER ';'
CSV HEADER;

COPY tmp_tma(raster,ul_max,rfc_date)
FROM '/var/lib/postgresql/open/TMA_MUL.csv'
DELIMITER ';'
CSV HEADER;


TRUNCATE cov_mno;


COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max)
FROM '/var/lib/postgresql/open/A1TA.csv'
DELIMITER ';'
CSV HEADER; 


INSERT INTO cov_mno (raster,dl_normal,ul_normal,dl_max,ul_max,
operator,reference,license,rfc_date)
SELECT tmp_tma.raster,
  round(max(dl_normal)*1000000)::BIGINT dl_normal,
  round(max(ul_normal)*1000000)::BIGINT ul_normal,
  round(max(dl_max)*1000000)::BIGINT dl_max,
  round(max(ul_max)*1000000)::BIGINT ul_max,
  'TMA','F7/16','CCBY4.0',tmp_tma.rfc_date
FROM tmp_tma GROUP BY tmp_tma.raster,tmp_tma.rfc_date;

COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max)
FROM '/var/lib/postgresql/open/H3A.csv'
DELIMITER ';'
CSV HEADER; 

COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max)
FROM '/var/lib/postgresql/open/LIWEST.csv'
DELIMITER ';'
CSV HEADER;

COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max)
FROM '/var/lib/postgresql/open/SBGAG.csv'
DELIMITER ';'
CSV HEADER;

COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max)
FROM '/var/lib/postgresql/open/MASS.csv'
DELIMITER ';'
CSV HEADER;

ANALYSE cov_mno;
SELECT COUNT(*) FROM cov_mno;

ALTER TABLE cov_mno_fn OWNER TO rmbt;
GRANT SELECT ON TABLE cov_mno_fn TO rmbt_group_read_only;

COMMIT;
VACUUM cov_mno;
EOF
)
echo -e $sql|psql rmbt

# add to git
# todo


echo done
