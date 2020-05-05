package at.rtr.rmbt.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Extern on 20.06.2017.
 */
public class LteBandCalculationUtilTest {
    @Test
    public void getBandFromEarfcn() throws Exception {
        BandCalculationUtil.FrequencyInformation band = BandCalculationUtil.getBandFromEarfcn(2850);
        assertEquals(7, band.getBand());
        assertEquals(2630.0, band.getFrequencyDL(), 0);

        band = BandCalculationUtil.getBandFromNrarfcn(636000);
        assertEquals(78, band.getBand());
        assertEquals(3540.0, band.getFrequencyDL(), 0);

        band = BandCalculationUtil.getBandFromNrarfcn(2016749);
        assertEquals(258, band.getBand());
        assertEquals(24255.0, band.getFrequencyDL(), 0);

        band = BandCalculationUtil.getBandFromNrarfcn(390000);
        assertEquals(1, band.getBand());
        assertEquals(1950.0, band.getFrequencyDL(), 0);

        band = BandCalculationUtil.getBandFromEarfcn(1814);
        assertEquals(3, band.getBand());
        assertEquals(1866.4, band.getFrequencyDL(), 0);

        band = BandCalculationUtil.getBandFromEarfcn(3350);
        assertEquals(7, band.getBand());
        assertEquals(2680.0, band.getFrequencyDL(), 0);

        band = BandCalculationUtil.getBandFromUarfcn(9690);
        assertEquals(1, band.getBand());
        assertEquals(2128, band.getFrequencyDL(), 0);

        band = BandCalculationUtil.getBandFromUarfcn(3650);
        assertEquals(12, band.getBand());
        assertEquals(738.0, band.getFrequencyDL(), 0);

        band = BandCalculationUtil.getBandFromArfcn(130);
        assertEquals(5, band.getBand());
        assertEquals(869.6, band.getFrequencyDL(), 0);

        band = BandCalculationUtil.getBandFromArfcn(980);
        assertEquals(8, band.getBand());
        assertEquals(926.2, band.getFrequencyDL(), 0);



    }

}