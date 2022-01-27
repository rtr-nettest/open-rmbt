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


# F1/16

# https://www.a1.net/versorgunsdaten-gemaess-auflagen
# as zip
F1_A1TA=https://cdn11.a1.net/m/resources/media/zip/2100-Final-20211130-versorgt.zip

# 
F1_TMA=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/speed_21q4.csv

# https://www.drei.at/de/info/netzabdeckung/versorgungsdaten-700-mhz-1500-mhz-2100-mhz.html
F1_H3A=https://www.drei.at/media/common/info/netzabdeckung/h3a-versorgung-rohdaten-700-1500-2100.csv


# F7/16 (3,5 GHz data)

# https://www.a1.net/versorgunsdaten-gemaess-auflagen
(was: https://www.a1.net/5g-netzabdeckung-karte)
# 
URL_A1TA=https://cdn11.a1.net/m/resources/media/excel/5GNR3500-Final-20211130-versorgt.csv
# https://cdn11.a1.net/m/resources/media/excel/5GNR3500-Final-20210630-versorgt.csv
# https://cdn11.a1.net/m/resources/media/excel/5GNR3500-20210331-versorgt.csv

# https://www.magenta.at/unternehmen/rechtliches/versorgungsdaten
URL_TMA=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/speed_5g_21q4.csv
# URL_TMA_NDL=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/Rohdaten_SPEEDMAP_DL_AVG.csv
# URL_TMA_MDL=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/Rohdaten_SPEEDMAP_DL_MAX.csv
# URL_TMA_NUL=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/Rohdaten_SPEEDMAP_UL_AVG.csv
# URL_TMA_MUL=https://www.magenta.at/content/dam/magenta_at/csv/versorgungsdaten/Rohdaten_SPEEDMAP_UL_MAX.csv

# https://www.drei.at/de/info/netzabdeckung/versorgungsdaten-35-ghz.html
URL_H3A=https://www.drei.at/media/common/info/netzabdeckung/h3a-versorgung-rohdaten.csv

# https://www.liwest.at/5g-fwa
URL_LIWEST=https://www.liwest.at/fileadmin/user_upload/5g/rtr_f716.CSV

# https://citycom-austria.com/festnetz-versorgungsnetz/
URL_HGRAZ=https://raw.githubusercontent.com/GrazNewRadio/Versorgungskarte/main/GrazNewRadio_Versorgungskarte.csv


# https://www.salzburg-ag.at/internet-tv-telefon/fuer-privat/internet/cablelink-air/netzabdeckung-air.html
URL_SBGAG=https://www.salzburg-ag.at/content/dam/web18/dokumente/cablelink/internet/RohdatenSalzburgAG3_5GHz.csv

# https://www.massresponse.com/versorgungsdaten3-5ghz/
URL_MASS=https://www.massresponse.com/versorgungsdaten3-5ghz/OpenDataRasterdatenMASS.csv


# process files

mkdir -p ~/open
cd ~/open

rm -rf *.csv
rm -rf *.zip
rm -rf x1


# F1/16

wget $F1_A1TA -O F1_A1TA.zip
# unzip file
unzip -p F1_A1TA.zip  > F1_A1TA.csv
rm F1_A1TA.zip
# header: operator;reference;license;rfc_date;raster;dl_normal;ul_normal;dl_max;ul_max
# data: <S>A1TA<T>;"F1/16<T>;<S>CCBY4.0<T>;2021-11-30;100mN26419E47453;19335600;8701020;138600000;62370000

# data: 
# different quotes, to be removed
sed -i "s/\x93//g" F1_A1TA.csv
sed -i "s/\x94//g" F1_A1TA.csv
sed -i "s/\x22//g" F1_A1TA.csv


wget $F1_TMA -O F1_TMA.csv
# header: "OPERATOR","REFERENCE","LICENSE","RFC_DATE","RASTER","DL_NORMAL","UL_NORMAL","DL_MAX","UL_MAX"
# data: "TMA","F1/16","CCBY4.0","2021-12-20","100mN28890E46950",22315359,5605760,56791399,14057599
# quotes to be removed
sed -i "s/\"//g" F1_TMA.csv
# delimiter "," (should be ";")
sed -i "s/,/;/g" F1_TMA.csv


wget $F1_H3A -O F1_H3A.csv
# header: <FEFF>OPERATOR;REFERENCE;LICENSE;RFC_DATE;RASTER;DL_NORMAL;DL_MAX;UL_NORMAL;UL_MAX
# data: H3A;F7/16;CCBY4.0;2021-12-27;100mN26636E45278;540000;1350000;60000;150000
# wrong procedure identifier - should be F1/16 (not F7/16)
sed -i "s/F7\/16/F1\/16/g" F1_H3A.csv
# wrong order of data fields - workaround at db-import



# F7/16


# download files
wget $URL_A1TA -O A1TA.csv
# header: operator;reference;license;rfc_date;raster;dl_normal;ul_normal;dl_max;ul_max
# data:  <S<A1TA<T<;"F7/16<T<;<S<CCBY4.0<T>;2021-11-30;100mN28428E47592;461902200;82481876;475760000;84956463
# remove all variants of typographic quotes
sed -i "s/\x93//g" A1TA.csv
sed -i "s/\x94//g" A1TA.csv
sed -i "s/\x22//g" A1TA.csv
#


wget $URL_TMA -O TMA.csv
# header "OPERATOR","REFERENCE","LICENSE","RFC_DATE","RASTER","DL_NORMAL","UL_NORMAL","DL_MAX","UL_MAX"
# data: "TMA","F7/16","CCBY4.0","2021-12-22","100mN28896E46893",77004805,2150000,81920138,2150000
# quotes to be removed
sed -i "s/\"//g" TMA.csv
# delimiter "," (should be ";")
sed -i "s/,/;/g" TMA.csv

#
wget $URL_H3A -O H3A.csv
# header: <FEFF>OPERATOR;REFERENCE;LICENSE;RFC_DATE;RASTER;DL_NORMAL;DL_MAX;UL_NORMAL;UL_MAX
# data: H3A;F7/16;CCBY4.0;2021-12-21;100mN27808E45282;13452000;33633600;1200000;3003000
# wrong order of data fields - workaround at db-import

#
wget $URL_LIWEST -O LIWEST.csv
# header: <FEFF>operator;reference;license;rfc_date;raster;dl_normal;ul_normal;dl_max;ul_max
# data: LIWEST;F7/16;CCBY4.0;2020-11-29;100mN27838E46196;85000000;8000000;100000000;10000000
#

wget $URL_HGRAZ -O HGRAZ.csv
# header: operator;reference;license;rfc_date;raster;dl_normal;ul_normal;dl_max;ul_max
# data: HGRAZ;F7/16;CCBY4.0;2021-10-18;100mN27367E46010;0;0;0;0
# remove empty data rows
sed -i '/\;0\;0\;0\;0/d' a.csv
#

wget $URL_SBGAG -O SBGAG.csv
# header: <FEFF>operator;reference;lincense;rfc-date;rasterid;dl_normal;ul_normal;dl_max;ul_max
# data: SBG;F7/16;CCBY4.0;20.10.2021;100mN27094E46042;12000000;1200000;20000000;2000000
#

wget $URL_MASS -O MASS.csv
# header: operator;reference;license;rfc_date;raster;dl_normal;ul_normal;dl_max;ul_max
# data: MASS;F7/16;CCBY4.0;2021-11-01;100mN28508E48099;40000000;20000000;200000000;40000000
#

# import CSV

# note different order of fields on H3A data

sql=$(cat <<EOF
BEGIN;

TRUNCATE cov_mno;

COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max)
FROM '/var/lib/postgresql/open/F1_A1TA.csv'
DELIMITER ';'
CSV HEADER; 

COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max)
FROM '/var/lib/postgresql/open/F1_TMA.csv'
DELIMITER ';'
CSV HEADER; 


COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,dl_max,ul_normal,ul_max)
FROM '/var/lib/postgresql/open/F1_H3A.csv'
DELIMITER ';'
CSV HEADER; 

COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max)
FROM '/var/lib/postgresql/open/A1TA.csv'
DELIMITER ';'
CSV HEADER; 

COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max)
FROM '/var/lib/postgresql/open/TMA.csv'
DELIMITER ';'
CSV HEADER; 


COPY cov_mno(operator,reference,license,rfc_date,raster,dl_normal,dl_max,ul_normal,ul_max)
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
