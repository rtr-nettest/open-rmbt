package at.rtr.rmbt.client;

import at.rtr.rmbt.shared.QoEClassification;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class QualityOfExperienceTest {

    @Test
    public void testQoE() {
        Map<QoEClassification.QoEClassificationThresholds.Criteria, Long[]> thresholds = new HashMap<>();
        thresholds.put(QoEClassification.QoEClassificationThresholds.Criteria.DOWN, new Long[]{8000L,4000L,2000L});
        thresholds.put(QoEClassification.QoEClassificationThresholds.Criteria.UP, new Long[]{60000L,30000L,10000L});
        thresholds.put(QoEClassification.QoEClassificationThresholds.Criteria.PING, new Long[]{5000000L,10000000L,50000000L});

        List<QoEClassification.Classification> classification = QoEClassification.classify((long) (10 * 1e6), (long) (10 * 1e3), (long) (10 * 1e3), Arrays.asList(
                new QoEClassification.QoEClassificationThresholds(QoEClassification.Category.GAMING_CLOUD, thresholds)
        ));

        assertEquals(classification.get(0).getClassification(),2);
        assertEquals(classification.get(0).getQuality(),0.25,0.01);
    }
}
