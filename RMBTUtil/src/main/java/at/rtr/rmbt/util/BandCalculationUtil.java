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

package at.rtr.rmbt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BandCalculationUtil {

    /**
     *
     * @param earfcn Frequency to check
     * @return LTEBand object for matched band, or NULL if invalid earfcn is passed
     */
    public static FrequencyInformation<LTEBand> getBandFromEarfcn(int earfcn){
        if(earfcn > 0  && earfcn < 18000) { //DL
            for (LTEBand band: lteBands.values()) { //Loop through all lteBands
                if(band.containsDLChannel(earfcn)) //If the band contains the earfcn then return it
                    return new FrequencyInformation<>(earfcn, band);
            }
        } else if (earfcn >=18000 && earfcn <= 65535){ //UL
            for (LTEBand band: lteBands.values()) { //Loop through all lteBands
                if(band.containsULChannel(earfcn)) //If the band contains the earfcn then return it
                    return new FrequencyInformation<>(earfcn, band);
            }
        }

        //Invalid input
        return null;
    }

    /**
     *
     * @param uarfcn Frequency to check
     * @return UMTSBand object for matched band, or NULL if invalid earfcn is passed
     */
    public static FrequencyInformation<UMTSBand> getBandFromUarfcn(int uarfcn){
        //we can't differentiate between UL and DL with umts?
        for (UMTSBand umtsBand : umtsBands.values()) { //Loop through all lteBands
            if (umtsBand.containsChannel(uarfcn)) //If the band contains the earfcn then return it
                return new FrequencyInformation<>(uarfcn, umtsBand);
        }

        //Invalid input
        return null;
    }

    /**
     *
     * @param arfcn Frequency to check
     * @return GSMBand object for matched band, or NULL if invalid earfcn is passed
     */
    public static FrequencyInformation<GSMBand> getBandFromArfcn(int arfcn){
        //we can't differentiate between UL and DL with umts?
        for (GSMBand gsmBand : gsmBands) { //Loop through all lteBands
            if (gsmBand.containsChannel(arfcn)) //If the band contains the earfcn then return it
                return new FrequencyInformation<>(arfcn, gsmBand);
        }

        //Invalid input
        return null;
    }

    public static class FrequencyInformation<U extends Band> {
        private int earfcn;
        private Band band;

        public FrequencyInformation(int earfcn, U band) {
            this.earfcn = earfcn;
            this.band = band;
        }

        public double getFrequencyDL() {
            return this.band.getFrequencyDL(earfcn);
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
    private static HashMap<Integer, LTEBand> lteBands = new HashMap<Integer, LTEBand>() {{
        put(1, new LTEBand(1, 1920, 1980, 2110, 2170, 18000, 18599, 18000, "2100 MHz"));
        put(2, new LTEBand(2, 1850, 1910, 1930, 1990, 18600, 19199, 18000, "PCS A-F blocks 1,9 GHz"));
        put(3, new LTEBand(3, 1710, 1785, 1805, 1880, 19200, 19949, 18000, "1800 MHz"));
        put(4, new LTEBand(4, 1710, 1755, 2110, 2155, 19950, 20399, 18000, "AWS-1 1.7+2.1 GHz"));
        put(5, new LTEBand(5, 824, 849, 869, 894, 20400, 20649, 18000, "850MHz (was CDMA)"));
        put(6, new LTEBand(6, 830, 840, 875, 885, 20650, 20749, 18000, "850MHz subset (was CDMA)"));
        put(7, new LTEBand(7, 2500, 2570, 2620, 2690, 20750, 21449, 18000, "2600 MHz"));
        put(8, new LTEBand(8, 880, 915, 925, 960, 21450, 21799, 18000, "900 MHz"));
        put(9, new LTEBand(9, 1749.9, 1784.9, 1844.9, 1879.9, 21800, 22149, 18000, "DCS1800 subset"));
        put(10, new LTEBand(10, 1710, 1770, 2110, 2170, 22150, 22749, 18000, "Extended AWS/AWS-2/AWS-3"));
        put(11, new LTEBand(11, 1427.9, 1447.9, 1475.9, 1495.9, 22750, 22949, 18000, "1.5 GHz lower"));
        put(12, new LTEBand(12, 699, 716, 729, 746, 23010, 23179, 18000, "700 MHz lower A(BC) blocks"));
        put(13, new LTEBand(13, 777, 787, 746, 756, 23180, 23279, 18000, "700 MHz upper C block"));
        put(14, new LTEBand(14, 788, 798, 758, 768, 23280, 23379, 18000, "700 MHz upper D block"));
        put(17, new LTEBand(17, 704, 716, 734, 746, 23730, 23849, 18000, "700 MHz lower BC blocks"));
        put(18, new LTEBand(18, 815, 830, 860, 875, 23850, 23999, 18000, "800 MHz lower"));
        put(19, new LTEBand(19, 830, 845, 875, 890, 24000, 24149, 18000, "800 MHz upper"));
        put(20, new LTEBand(20, 832, 862, 791, 821, 24150, 24449, 18000, "800 MHz"));
        put(21, new LTEBand(21, 1447.9, 1462.9, 1495.9, 1510.9, 24450, 24599, 18000, "1.5 GHz upper"));
        put(22, new LTEBand(22, 3410, 3490, 3510, 3590, 24600, 25399, 18000, "3.5 GHz"));
        put(23, new LTEBand(23, 2000, 2020, 2180, 2200, 25500, 25699, 18000, "2 GHz S-Band"));
        put(24, new LTEBand(24, 1626.5, 1660.5, 1525, 1559, 25700, 26039, 18000, "1.6 GHz L-Band"));
        put(25, new LTEBand(25, 1850, 1915, 1930, 1995, 26040, 26689, 18000, "PCS A-G blocks 1900"));
        put(26, new LTEBand(26, 814, 849, 859, 894, 26690, 27039, 18000, "ESMR+ 850 (was: iDEN)"));
        put(27, new LTEBand(27, 807, 824, 852, 869, 27040, 27209, 18000, "800 MHz SMR (was iDEN)"));
        put(28, new LTEBand(28, 703, 748, 758, 803, 27210, 27659, 18000, "700 MHz"));
        put(29, new LTEBand(29, 0, 0, 717, 728, 0, 0, -9660, "700 lower DE blocks (suppl. DL)"));
        put(30, new LTEBand(30, 2305, 2315, 2350, 2360, 27660, 27759, 17890, "2.3GHz WCS"));
        put(31, new LTEBand(31, 452.5, 457.5, 462.5, 467.5, 27760, 27809, 17890, "IMT 450 MHz"));
        put(32, new LTEBand(32, 0, 0, 1452, 1496, 0, 0, -9920, "1.5 GHz L-Band (suppl. DL)"));
        put(33, new LTEBand(33, 1900, 1920, 1900, 1920, 36000, 36199, 0, "2 GHz TDD lower"));
        put(34, new LTEBand(34, 2010, 2025, 2010, 2025, 36200, 36349, 0, "2 GHz TDD upper"));
        put(35, new LTEBand(35, 1850, 1910, 1850, 1910, 36350, 36949, 0, "1,9 GHz TDD lower"));
        put(36, new LTEBand(36, 1930, 1990, 1930, 1990, 36950, 37549, 0, "1.9 GHz TDD upper"));
        put(37, new LTEBand(37, 1910, 1930, 1910, 1930, 37550, 37749, 0, "PCS TDD"));
        put(38, new LTEBand(38, 2570, 2620, 2570, 2620, 37750, 38249, 0, "2600 MHz TDD"));
        put(39, new LTEBand(39, 1880, 1920, 1880, 1920, 38250, 38649, 0, "IMT 1.9 GHz TDD (was TD-SCDMA)"));
        put(40, new LTEBand(40, 2300, 2400, 2300, 2400, 38650, 39649, 0, "2300 MHz"));
        put(41, new LTEBand(41, 2496, 2690, 2496, 2690, 39650, 41589, 0, "Expanded TDD 2.6 GHz"));
        put(42, new LTEBand(42, 3400, 3600, 3400, 3600, 41590, 43589, 0, "3,4-3,6 GHz"));
        put(43, new LTEBand(43, 3600, 3800, 3600, 3800, 43590, 45589, 0, "3.6-3,8 GHz"));
        put(44, new LTEBand(44, 703, 803, 703, 803, 45590, 46589, 0, "700 MHz APT TDD"));
        put(45, new LTEBand(45, 1447, 1467, 1447, 1467, 46590, 46789, 0, "1500 MHZ"));
        put(46, new LTEBand(46, 5150, 5925, 5150, 5925, 46790, 54539, 0, "TD Unlicensed"));
        put(47, new LTEBand(47, 5855, 5925, 5855, 5925, 54540, 55239, 0, "Vehicle to Everything (V2X) TDD"));
        put(65, new LTEBand(65, 1920, 2010, 2110, 2200, 131072, 131971, 65536, "Extended IMT 2100"));
        put(66, new LTEBand(66, 1710, 1780, 2110, 2200, 131972, 132671, 65536, "AWS-3"));
        put(67, new LTEBand(67, 0, 0, 738, 758, 0, 0, -67336, "700 EU (Suppl. DL)"));
        put(68, new LTEBand(68, 698, 728, 753, 783, 132672, 132971, 65136, "700 ME"));
        put(69, new LTEBand(69, 0, 0, 2570, 2620, 0, 0, -67836, "IMT-E FDD CA"));
        put(70, new LTEBand(70, 1695, 1710, 1995, 2020, 132972, 133121, 64636, "AWS-4"));
        put(0, new LTEBand(0, 0, 0, 0, 0, 0, 0, 0, null));

    }};

    /**
     * Static list of all UMTS Bands
     * taken from the 3GPP TS 25.101 standard
     * ftp://ftp.3gpp.org/Specs/latest/Rel-14/25_series/25101-e00.zip
     * latest update 2016-06
     *
     * order by priority (if one uarfcn is contained in multiple bands)
     */
    private static HashMap<Integer, UMTSBand> umtsBands = new HashMap<Integer, UMTSBand>() {{
        put(1, new UMTSBand(1, 1922.4, 1977.6, 2112.4, 2167.6, 9612, 9888, -950, "2100 MHz"));
        put(2, new UMTSBand(2, 1852.4, 1907.6, 1932.4, 1987.6, 9262, 9538, -400, "1900 MHz PCS"));
        put(3, new UMTSBand(3, 1712.4, 1782.6, 1807.4, 1877.6, 937, 1288, -225, "1800 MHz DCS"));
        put(4, new UMTSBand(4, 1712.4, 1752.6, 2112.4, 2152.6, 1312, 1513, -225, "AWS-1"));
        put(5, new UMTSBand(5, 826.4, 846.6, 871.4, 891.6, 4132, 4233, -225, "850 MHz"));
        put(6, new UMTSBand(6, 832.4, 837.6, 877.4, 882.6, 4162, 4188, -225, "850 MHz Japan"));
        put(7, new UMTSBand(7, 2502.4, 2567.6, 2622.4, 2687.6, 2012, 2338, -225, "2600 MHz"));
        put(8, new UMTSBand(8, 882.4, 912.6, 927.4, 957.6, 2712, 2863, -225, "900 MHz"));
        put(9, new UMTSBand(9, 1752.4, 1782.4, 1847.4, 1877.4, 8762, 8912, -475, "1800 MHz Japan"));
        put(10, new UMTSBand(10, 1712.4, 1767.6, 2112.4, 2167.6, 2887, 3163, -225, "AWS-1+"));
        put(11, new UMTSBand(11, 1430.4, 1445.4, 1478.4, 1493.4, 3487, 3562, -225, "1500 MHz Lower"));
        put(12, new UMTSBand(12, 701.4, 713.6, 731.4, 743.6, 3617, 3678, -225, "700 MHz US a"));
        put(13, new UMTSBand(13, 779.4, 784.6, 748.4, 753.6, 3792, 3818, -225, "700 MHz US c"));
        put(14, new UMTSBand(14, 790.4, 795.6, 760.4, 765.6, 3892, 3918, -225, "700 MHz US PS"));
        put(19, new UMTSBand(19, 832.4, 842.6, 877.4, 887.6, 312, 363, -400, "800 MHz Japan"));
        put(20, new UMTSBand(20, 834.4, 859.6, 793.4, 818.6, 4287, 4413, -225, "800 MHz EU DD"));
        put(21, new UMTSBand(21, 1450.4, 1460.4, 1498.4, 1508.4, 462, 512, -400, "1500 MHz Upper"));
        put(22, new UMTSBand(22, 3412.4, 3487.6, 3512.4, 3587.6, 4437, 4813, -225, "3500 MHz"));
        put(25, new UMTSBand(25, 1852.4, 1912.6, 1932.4, 1992.6, 4887, 5188, -225, "1900+ MHz"));
        put(26, new UMTSBand(26, 816.4, 846.6, 861.4, 891.6, 5537, 5688, -225, "850+ MHz"));
    }};

    /**
     * Static list of all GSM Bands
     * taken from the 3GPP TS 45.005 standard
     * http://www.3gpp.org/ftp/Specs/archive/45_series/45.005/45005-e10.zip
     * latest update 2017-06
     *
     * order by priority (if one uarfcn is contained in multiple bands)
     */
    private static List<GSMBand> gsmBands = new ArrayList<GSMBand>() {{
        add(new GSMBand(5, 824.2, 848.8, 869.2, 893.8, 128, 251, 0, "GSM 850"));
        add(new GSMBand(8, 890.2, 914.8, 935.2, 959.8, 1, 124, 0, "GSM 900"));
        add(new GSMBand(8, 890, 914.8, 935, 959.8, 0, 124, 0, "GSM 900"));
        add(new GSMBand(8, 880.2, 889.8, 925.2, 934.8, 975, 1023, 0, "GSM 900"));
        add(new GSMBand(8, 890, 914.8, 935, 959.8, 0, 124, 0, "GSM 900"));
        add(new GSMBand(8, 876.2, 889.8, 921.2, 934.8, 955, 1023, 0, "GSM 900"));
        add(new GSMBand(3, 1710.2, 1784.8, 1805.2, 1879.8, 512, 885, 0, "GSM 1800"));
        add(new GSMBand(2, 1850.2, 1909.8, 1930.2, 1989.8, 512, 810, 0, "GSM 1900"));
    }};

    private static HashMap<Integer, WifiBand> wifiBands = new HashMap<Integer, WifiBand>() {{
        put(2412, new WifiBand(2412, 1, "2.4 GHz"));
        put(2417, new WifiBand(2417, 2, "2.4 GHz"));
        put(2422, new WifiBand(2422, 3, "2.4 GHz"));
        put(2427, new WifiBand(2427, 4, "2.4 GHz"));
        put(2432, new WifiBand(2432, 5, "2.4 GHz"));
        put(2437, new WifiBand(2437, 6, "2.4 GHz"));
        put(2442, new WifiBand(2442, 7, "2.4 GHz"));
        put(2447, new WifiBand(2447, 8, "2.4 GHz"));
        put(2452, new WifiBand(2452, 9, "2.4 GHz"));
        put(2457, new WifiBand(2457, 10, "2.4 GHz"));
        put(2462, new WifiBand(2462, 11, "2.4 GHz"));
        put(2467, new WifiBand(2467, 12, "2.4 GHz"));
        put(2472, new WifiBand(2472, 13, "2.4 GHz"));
        put(2484, new WifiBand(2484, 14, "2.4 GHz"));
        put(5160, new WifiBand(5160, 32, "5 GHz"));
        put(5170, new WifiBand(5170, 34, "5 GHz"));
        put(5180, new WifiBand(5180, 36, "5 GHz"));
        put(5190, new WifiBand(5190, 38, "5 GHz"));
        put(5200, new WifiBand(5200, 40, "5 GHz"));
        put(5210, new WifiBand(5210, 42, "5 GHz"));
        put(5220, new WifiBand(5220, 44, "5 GHz"));
        put(5230, new WifiBand(5230, 46, "5 GHz"));
        put(5240, new WifiBand(5240, 48, "5 GHz"));
        put(5250, new WifiBand(5250, 50, "5 GHz"));
        put(5260, new WifiBand(5260, 52, "5 GHz"));
        put(5270, new WifiBand(5270, 54, "5 GHz"));
        put(5280, new WifiBand(5280, 56, "5 GHz"));
        put(5290, new WifiBand(5290, 58, "5 GHz"));
        put(5300, new WifiBand(5300, 60, "5 GHz"));
        put(5310, new WifiBand(5310, 62, "5 GHz"));
        put(5320, new WifiBand(5320, 64, "5 GHz"));
        put(5340, new WifiBand(5340, 68, "5 GHz"));
        put(5480, new WifiBand(5480, 96, "5 GHz"));
        put(5500, new WifiBand(5500, 100, "5 GHz"));
        put(5510, new WifiBand(5510, 102, "5 GHz"));
        put(5520, new WifiBand(5520, 104, "5 GHz"));
        put(5530, new WifiBand(5530, 106, "5 GHz"));
        put(5540, new WifiBand(5540, 108, "5 GHz"));
        put(5550, new WifiBand(5550, 110, "5 GHz"));
        put(5560, new WifiBand(5560, 112, "5 GHz"));
        put(5570, new WifiBand(5570, 114, "5 GHz"));
        put(5580, new WifiBand(5580, 116, "5 GHz"));
        put(5590, new WifiBand(5590, 118, "5 GHz"));
        put(5600, new WifiBand(5600, 120, "5 GHz"));
        put(5610, new WifiBand(5610, 122, "5 GHz"));
        put(5620, new WifiBand(5620, 124, "5 GHz"));
        put(5630, new WifiBand(5630, 126, "5 GHz"));
        put(5640, new WifiBand(5640, 128, "5 GHz"));
        put(5660, new WifiBand(5660, 132, "5 GHz"));
        put(5670, new WifiBand(5670, 134, "5 GHz"));
        put(5680, new WifiBand(5680, 136, "5 GHz"));
        put(5690, new WifiBand(5690, 138, "5 GHz"));
        put(5700, new WifiBand(5700, 140, "5 GHz"));
        put(5710, new WifiBand(5710, 142, "5 GHz"));
        put(5720, new WifiBand(5720, 144, "5 GHz"));
        put(5745, new WifiBand(5745, 149, "5 GHz"));
        put(5755, new WifiBand(5755, 151, "5 GHz"));
        put(5765, new WifiBand(5765, 153, "5 GHz"));
        put(5775, new WifiBand(5775, 155, "5 GHz"));
        put(5785, new WifiBand(5785, 157, "5 GHz"));
        put(5795, new WifiBand(5795, 159, "5 GHz"));
        put(5805, new WifiBand(5805, 161, "5 GHz"));
        put(5825, new WifiBand(5825, 165, "5 GHz"));
        put(5845, new WifiBand(5845, 169, "5 GHz"));
        put(5865, new WifiBand(5865, 173, "5 GHz"));
        put(4915, new WifiBand(4915, 183, "5 GHz"));
        put(4920, new WifiBand(4920, 184, "5 GHz"));
        put(4925, new WifiBand(4925, 185, "5 GHz"));
        put(4935, new WifiBand(4935, 187, "5 GHz"));
        put(4940, new WifiBand(4940, 188, "5 GHz"));
        put(4945, new WifiBand(4945, 189, "5 GHz"));
        put(4960, new WifiBand(4960, 192, "5 GHz"));
        put(4980, new WifiBand(4980, 196, "5 GHz"));
        put(58320, new WifiBand(58320, 1, "60 GHz"));
        put(60480, new WifiBand(60480, 2, "60 GHz"));
        put(62640, new WifiBand(62640, 3, "60 GHz"));
        put(64800, new WifiBand(64800, 4, "60 GHz"));
        put(66960, new WifiBand(66960, 5, "60 GHz"));
        put(69120, new WifiBand(69120, 6, "60 GHz"));
    }};

    public static class WifiBand {
        private String informal_name; //e.g. 2.4 GHz; 5 GHz; 60 GHz
        private int channel_number;
        private int frequency;

        public WifiBand(int frequency,  int channelNumber, String informalName) {
            this.frequency = frequency;
            this.channel_number = channelNumber;
            this.informal_name = informalName;
        }

        public String getInformalName() {
            return informal_name;
        }

        public int getChannelNumber() {
            return channel_number;
        }

        public int getFrequency() {
            return frequency;
        }
    }

    public static WifiBand getBandFromWifiFrequency(int frequency) {
        if (wifiBands.containsKey(frequency)) {
            return wifiBands.get(frequency);
        }
        return null;
    }

    public static abstract class Band {
        private int band;

        private final double upload_frequency_lower_bound;
        private final double upload_frequency_upper_bound;
        private final double download_frequency_lower_bound;
        private final double download_frequency_upper_bound;
        private final double upload_channel_lower_bound;
        private final double upload_channel_upper_bound;
        private final double channel_offset; //difference between (upload_channel_lower_bound - download_channel_lower_bound)
        private final String informal_name;

        protected Band(int band, double upload_frequency_lower_bound, double upload_frequency_upper_bound, double download_frequency_lower_bound, double download_frequency_upper_bound, double upload_channel_lower_bound, double upload_channel_upper_bound, double channel_offset, String informal_name) {
            this.band = band;
            this.upload_frequency_lower_bound = upload_frequency_lower_bound;
            this.upload_frequency_upper_bound = upload_frequency_upper_bound;
            this.download_frequency_lower_bound = download_frequency_lower_bound;
            this.download_frequency_upper_bound = download_frequency_upper_bound;
            this.upload_channel_lower_bound = upload_channel_lower_bound;
            this.upload_channel_upper_bound = upload_channel_upper_bound;
            this.channel_offset = channel_offset;
            this.informal_name = informal_name;
        }

        public abstract double getStep();

        /** Checks whether a upload frequency is contained in band object
         *
         * @param channel Frequency to check
         * @return True if the upload frequency is contained in this band, else false
         */
        public boolean containsChannel (double channel) {
            return containsDLChannel(channel) || containsULChannel(channel);
        }

        public boolean containsDLChannel(double channel) {
            return channel > (upload_channel_lower_bound - channel_offset) && channel < (upload_channel_upper_bound - channel_offset);
        }

        public boolean containsULChannel(double channel) {
            return channel > upload_channel_lower_bound && channel < upload_channel_upper_bound;
        }

        public double getFrequencyDL(double channel) {
            double channelOffset = (!containsDLChannel(channel))?0:this.channel_offset;
            double frequency = this.download_frequency_lower_bound + getStep() * (channel - (this.upload_channel_lower_bound - channelOffset));
            frequency = (double) Math.round(frequency * 1000) / 1000;
            return frequency;
        }

        public double getFrequencyUL(double channel) {
            double channelOffset = (!containsULChannel(channel))?0:-this.channel_offset;
            double frequency = this.download_frequency_lower_bound + getStep() * (channel - (this.upload_channel_lower_bound - channelOffset));
            frequency = (double) Math.round(frequency * 1000) / 1000;
            return frequency;
        }


        public int getBand() {
            return band;
        }

        public String getInformalName() {
            return informal_name;
        }
    }

    public static class LTEBand extends Band {

        protected LTEBand(int band, double upload_frequency_lower_bound, double upload_frequency_upper_bound, double download_frequency_lower_bound, double download_frequency_upper_bound, double upload_channel_lower_bound, double upload_channel_upper_bound, double channel_offset, String informal_name) {
            super(band, upload_frequency_lower_bound, upload_frequency_upper_bound, download_frequency_lower_bound, download_frequency_upper_bound, upload_channel_lower_bound, upload_channel_upper_bound, channel_offset, informal_name);
        }

        @Override
        public double getStep() {
            return 0.1;
        }
    }

    public static class UMTSBand extends Band {

        public UMTSBand(int band, double upload_frequency_lower_bound, double upload_frequency_upper_bound, double download_frequency_lower_bound, double download_frequency_upper_bound, double upload_channel_lower_bound, double upload_channel_upper_bound, double channel_offset, String informal_name) {
            super(band, upload_frequency_lower_bound, upload_frequency_upper_bound, download_frequency_lower_bound, download_frequency_upper_bound, upload_channel_lower_bound, upload_channel_upper_bound, channel_offset, informal_name);
        }

        @Override
        public double getStep() {
            return 0.2;
        }
    }

    public static class GSMBand extends Band {

        protected GSMBand(int band, double upload_frequency_lower_bound, double upload_frequency_upper_bound, double download_frequency_lower_bound, double download_frequency_upper_bound, double upload_channel_lower_bound, double upload_channel_upper_bound, double channel_offset, String informal_name) {
            super(band, upload_frequency_lower_bound, upload_frequency_upper_bound, download_frequency_lower_bound, download_frequency_upper_bound, upload_channel_lower_bound, upload_channel_upper_bound, channel_offset, informal_name);
        }

        @Override
        public double getStep() {
            return 0.2;
        }
    }
}
