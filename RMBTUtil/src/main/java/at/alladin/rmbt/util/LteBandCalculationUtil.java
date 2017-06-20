/*******************************************************************************
 * Copyright 2017 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package at.alladin.rmbt.util;

import java.util.HashMap;

public class LteBandCalculationUtil {

    /**
     *
     * @param earfcn Frequency to check
     * @return LTEBand object for matched band, or NULL if invalid earfcn is passed
     */
    public static LTEFrequencyInformation getBandFromEarfcn(int earfcn){
        if(earfcn > 0  && earfcn < 18000) { //DL
            for (LTEBand band: bands.values()) { //Loop through all bands
                if(band.containsDLEarfcn(earfcn)) //If the band contains the earfcn then return it
                    return new LTEFrequencyInformation(earfcn, band);
            }
        } else if (earfcn >=18000 && earfcn <= 65535){ //UL
            for (LTEBand band: bands.values()) { //Loop through all bands
                if(band.containsULEarfcn(earfcn)) //If the band contains the earfcn then return it
                    return new LTEFrequencyInformation(earfcn, band);
            }
        }

        //Invalid input
        return null;
    }

    public static LTEBand getBand(int band) {
        if (bands.containsKey(band)) {
            return bands.get(band);
        }
        else {
            return null;
        }
    }

    public static class LTEFrequencyInformation {
        private int earfcn;
        private LTEBand band;

        public LTEFrequencyInformation(int earfcn, LTEBand band) {
            this.earfcn = earfcn;
            this.band = band;
        }

        public double getFrequencyDL() {
            return this.band.download_frequency_lower_bound + 0.1 * (earfcn - this.band.download_frequency_offset);
        }

        public int getBand() {
            return this.band.getBand();
        }

        public String getInformalName() {
            return this.band.getInformalName();
        }

        @Override
        public String toString() {
            return "Earfcn: " + earfcn + ", Band: " + band;
        }
    }

    /**
     * Static list of all LTE Bands
     * taken from the 3gpp 36.101 standard
     * http://www.3gpp.org/ftp//Specs/archive/36_series/36.101/
     * latest update e30 (2017-03-28)
     */
    private static HashMap<Integer, LTEBand> bands = new HashMap<Integer, LTEBand>() {{
        put(1, new LTEBand(1, 1920, 1980, 2110, 2170, 18000, 0, "IMT 2.1 GHz"));
        put(2, new LTEBand(2, 1850, 1910, 1930, 1990, 18600, 600, "PCS A-F blocks 1,9 GHz"));
        put(3, new LTEBand(3, 1710, 1785, 1805, 1880, 19200, 1200, "1800 MHz (was GSM-1800/DCS1800)"));
        put(4, new LTEBand(4, 1710, 1755, 2110, 2155, 19950, 1950, "AWS-1 1.7+2.1 GHz"));
        put(5, new LTEBand(5, 824, 849, 869, 894, 20400, 2400, "850MHz (was CDMA)"));
        put(6, new LTEBand(6, 830, 840, 875, 885, 20650, 2650, "850MHz subset (was CDMA)"));
        put(7, new LTEBand(7, 2500, 2570, 2620, 2690, 20750, 2750, "IMT-E 2.6 GHz FDD"));
        put(8, new LTEBand(8, 880, 915, 925, 960, 21450, 3450, "900 MHz (was GSM900)"));
        put(9, new LTEBand(9, 1749.9, 1784.9, 1844.9, 1879.9, 21800, 3800, "DCS1800 subset"));
        put(10, new LTEBand(10, 1710, 1770, 2110, 2170, 22150, 4150, "Extended AWS/AWS-2/AWS-3"));
        put(11, new LTEBand(11, 1427.9, 1447.9, 1475.9, 1495.9, 22750, 4750, "1.5 GHz lower"));
        put(12, new LTEBand(12, 699, 716, 729, 746, 23010, 5010, "700 MHz lower A(BC) blocks"));
        put(13, new LTEBand(13, 777, 787, 746, 756, 23180, 5180, "700 MHz upper C block"));
        put(14, new LTEBand(14, 788, 798, 758, 768, 23280, 5280, "700 MHz upper D block"));
        put(17, new LTEBand(17, 704, 716, 734, 746, 23730, 5730, "700 MHz lower BC blocks"));
        put(18, new LTEBand(18, 815, 830, 860, 875, 23850, 5850, "800 MHz lower"));
        put(19, new LTEBand(19, 830, 845, 875, 890, 24000, 6000, "800 MHz upper"));
        put(20, new LTEBand(20, 832, 862, 791, 821, 24150, 6150, "800 MHz (Digital Dividend)"));
        put(21, new LTEBand(21, 1447.9, 1462.9, 1495.9, 1510.9, 24450, 6450, "1.5 GHz upper"));
        put(22, new LTEBand(22, 3410, 3490, 3510, 3590, 24600, 6600, "3.5 GHz"));
        put(23, new LTEBand(23, 2000, 2020, 2180, 2200, 25500, 7500, "2 GHz S-Band"));
        put(24, new LTEBand(24, 1626.5, 1660.5, 1525, 1559, 25700, 7700, "1.6 GHz L-Band"));
        put(25, new LTEBand(25, 1850, 1915, 1930, 1995, 26040, 8040, "PCS A-G blocks 1900"));
        put(26, new LTEBand(26, 814, 849, 859, 894, 26690, 8690, "ESMR+ 850 (was: iDEN)"));
        put(27, new LTEBand(27, 807, 824, 852, 869, 27040, 9040, "800 MHz SMR (was iDEN)"));
        put(28, new LTEBand(28, 703, 748, 758, 803, 27210, 9210, "700 APT"));
        put(29, new LTEBand(29, 0, 0, 717, 728, 0, 9660, "700 lower DE blocks (suppl. DL)"));
        put(30, new LTEBand(30, 2305, 2315, 2350, 2360, 27660, 9770, "2.3GHz WCS"));
        put(31, new LTEBand(31, 452.5, 457.5, 462.5, 467.5, 27760, 9870, "IMT 450 MHz"));
        put(32, new LTEBand(32, 0, 0, 1452, 1496, 0, 9920, "1.5 GHz L-Band (suppl. DL)"));
        put(33, new LTEBand(33, 1900, 1920, 1900, 1920, 36000, 36000, "2 GHz TDD lower"));
        put(34, new LTEBand(34, 2010, 2025, 2010, 2025, 36200, 36200, "2 GHz TDD upper"));
        put(35, new LTEBand(35, 1850, 1910, 1850, 1910, 36350, 36350, "1,9 GHz TDD lower"));
        put(36, new LTEBand(36, 1930, 1990, 1930, 1990, 36950, 36950, "1.9 GHz TDD upper"));
        put(37, new LTEBand(37, 1910, 1930, 1910, 1930, 37550, 37550, "PCS TDD"));
        put(38, new LTEBand(38, 2570, 2620, 2570, 2620, 37750, 37750, "IMT-E 2.6 GHz TDD"));
        put(39, new LTEBand(39, 1880, 1920, 1880, 1920, 38250, 38250, "IMT 1.9 GHz TDD (was TD-SCDMA)"));
        put(40, new LTEBand(40, 2300, 2400, 2300, 2400, 38650, 38650, "2.3 GHz TDD"));
        put(41, new LTEBand(41, 2496, 2690, 2496, 2690, 39650, 39650, "Expanded TDD 2.6 GHz"));
        put(42, new LTEBand(42, 3400, 3600, 3400, 3600, 41590, 41590, "3,4-3,6 GHz"));
        put(43, new LTEBand(43, 3600, 3800, 3600, 3800, 43590, 43590, "3.6-3,8 GHz"));
        put(44, new LTEBand(44, 703, 803, 703, 803, 45590, 45590, "700 MHz APT TDD"));
        put(45, new LTEBand(45, 1447, 1467, 1447, 1467, 46590, 46590, "TD 1500"));
        put(464, new LTEBand(464, 5150, 5925, 5150, 5925, 46790, 46790, "TD Unlicensed"));
        put(47, new LTEBand(47, 5855, 5925, 5855, 5925, 54540, 54540, "Vehicle to Everything (V2X) TDD"));
        put(48, new LTEBand(48, 3550, 3700, 3550, 3700, 55240, 55240, null));
        put(65, new LTEBand(65, 1920, 2010, 2110, 2200, 131072, 65536, "Extended IMT 2100"));
        put(66, new LTEBand(66, 1710, 1780, 2110, 2200, 131972, 66436, "AWS-3"));
        put(67, new LTEBand(67, 0, 0, 738, 758, 0, 67336, "700 EU (Suppl. DL)"));
        put(68, new LTEBand(68, 698, 728, 753, 783, 132672, 67536, "700 ME"));
        put(69, new LTEBand(69, 0, 0, 2570, 2620, 0, 67836, "IMT-E FDD CA"));
        put(70, new LTEBand(70, 1695, 1710, 1995, 2020, 132972, 68336, "AWS-4"));
        put(0, new LTEBand(0, 0, 0, 0, 0, 0, 0, null));
    }};

    /**
     * LTEBand
     * This work was inspired by https://github.com/Shelnutt2/BandDetection
     * which in case was inspired by
     * This work was inspired by https://github.com/richliu/earfcn2freq
     */
    public static class LTEBand {
        private int band;
        private double upload_frequency_lower_bound;
        private double upload_frequency_upper_bound;
        private double download_frequency_lower_bound;
        private double download_frequency_upper_bound;
        private double upload_frequency_offset;
        private double download_frequency_offset;
        private String informal_name;

        /** Constructor to create LTEBand object
         *
         * @param band Int for Band
         * @param upload_frequency_lower_bound Frequency upload lower bound
         * @param upload_frequency_upper_bound Frequency upload upper bound
         * @param download_frequency_lower_bound Frequency download lower bound
         * @param download_frequency_upper_bound Frequency download upper bound
         * @param upload_frequency_offset Frequency upload offset
         * @param download_frequency_offset Frequency download offset
         */
        LTEBand(int band, double upload_frequency_lower_bound, double upload_frequency_upper_bound, double download_frequency_lower_bound, double download_frequency_upper_bound, double upload_frequency_offset, double download_frequency_offset, String informal_name) {
            this.band = band;
            this.upload_frequency_lower_bound = upload_frequency_lower_bound;
            this.upload_frequency_upper_bound = upload_frequency_upper_bound;
            this.download_frequency_lower_bound = download_frequency_lower_bound;
            this.download_frequency_upper_bound = download_frequency_upper_bound;
            this.upload_frequency_offset = upload_frequency_offset;
            this.download_frequency_offset = download_frequency_offset;
            this.informal_name = informal_name;
        }

            /** Checks whether a upload frequency is contained in band object
             *
             * @param earfcn Frequency to check
             * @return True if the upload frequency is contained in this band, else false
             */
        public boolean containsULEarfcn (double earfcn) {
            return earfcn > this.upload_frequency_offset && earfcn < (this.upload_frequency_offset + (this.upload_frequency_upper_bound - this.upload_frequency_lower_bound) * 10);
        }

        /** Checks whether a download frequency is contained in band object
         *
         * @param earfcn Frequency to check
         * @return True if the download frequency is contained in this band, else false
         */
        public boolean containsDLEarfcn (double earfcn) {
            return earfcn > this.download_frequency_offset && earfcn < (this.download_frequency_offset + (this.download_frequency_upper_bound - this.download_frequency_lower_bound) * 10);
        }


        public int getBand() {
            return band;
        }

        public String getInformalName() {
            return this.informal_name;
        }

        @Override
        public String toString() {
            return "Band " + band;
        }
    }
}
