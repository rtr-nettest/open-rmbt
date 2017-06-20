package at.alladin.rmbt.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Extern on 20.06.2017.
 */
public class LteBandCalculationUtilTest {
    @Test
    public void getBandFromEarfcn() throws Exception {
        LteBandCalculationUtil.LTEFrequencyInformation band = LteBandCalculationUtil.getBandFromEarfcn(2850);
        assertEquals(7, band.getBand());
        assertEquals(2630.0, band.getFrequencyDL(), 0);

        band = LteBandCalculationUtil.getBandFromEarfcn(1814);
        assertEquals(3, band.getBand());
        assertEquals(1866.4, band.getFrequencyDL(), 0);

        band = LteBandCalculationUtil.getBandFromEarfcn(3350);
        assertEquals(7, band.getBand());
        assertEquals(2680.0, band.getFrequencyDL(), 0);
    }

}