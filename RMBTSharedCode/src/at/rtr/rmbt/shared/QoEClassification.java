package at.rtr.rmbt.shared;

import com.google.common.base.Enums;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QoEClassification {
    public enum Category {STREAMING_AUDIO_STREAMING, VIDEO_SD, VIDEO_HD,
        VIDEO_UHD, GAMING, GAMING_CLOUD, GAMING_STREAMING,
        GAMING_DOWNLOAD, VOIP, VIDEO_TELEPHONY, VIDEO_CONFERENCING,
        MESSAGING, WEB, CLOUD}

    private static int CLASSIFICATION_ITEMS = 4;

    public static List<Classification> classify(long pingNs, long downKbps, long upKbps, List<QoEClassificationThresholds> classifiers) {
        ArrayList<Classification> ret = new ArrayList<>();

        for (QoEClassificationThresholds classifier : classifiers) {
            int minClass = CLASSIFICATION_ITEMS; //start with highest class, grade down
            float minQuality = 1f;


            for (Map.Entry<QoEClassificationThresholds.Criteria, Long[]> entry : classifier.thresholds.entrySet()) {
                final Long value;
                switch (entry.getKey()) {
                    case DOWN:
                        value = downKbps;
                        break;
                    case UP:
                        value = upKbps;
                        break;
                    case PING:
                    default:
                        value = pingNs;
                        break;
                }

                Long[] threshold = entry.getValue();

                final boolean inverse = threshold[0] < threshold[1];
                int assignedClass = 1;
                float assignedQuality = 0;

                if (!inverse) {
                    //down, up
                    int c = 0;
                    for (int i = 0; i < threshold.length; i++, c++) {
                        if (value >= threshold[i]) {
                            assignedClass = CLASSIFICATION_ITEMS - c;
                            if (assignedClass > 1 && assignedClass < CLASSIFICATION_ITEMS) {
                                //linear calculation of the value within the given class bounds
                                assignedQuality = (assignedClass - 1) * 0.33f + 0.33f * ((value - threshold[i]) / (float) (threshold[i - 1] - threshold[i]));
                            }
                            //System.out.println("classified " + value + " for " + entry.getKey().name() + " as "+ assignedClass + "/" + assignedQuality);
                            break;
                        }
                    }
                } else {
                    //inverse --> e.g. for ping, where lower value is better
                    int c = 0;
                    for (int i = 0; i < threshold.length; i++, c++) {
                        if (value <= threshold[i]) {
                            assignedClass = CLASSIFICATION_ITEMS - c;
                            if (assignedClass > 1  && assignedClass < CLASSIFICATION_ITEMS) {
                                assignedQuality = (assignedClass - 1) * 0.33f + 0.33f * ((value - threshold[i - 1]) / (float) (threshold[i] - threshold[i - 1]));
                            }
                            //System.out.println("classified " + value + " for " + entry.getKey().name() + " as "+ assignedClass + "/" + assignedQuality);
                            break;
                        }
                    }
                }

                if (assignedClass == CLASSIFICATION_ITEMS) {
                    assignedQuality = 1f;
                }

                if (assignedClass < minClass) {
                    minClass = assignedClass;
                }
                if (assignedQuality < minQuality) {
                    minQuality = assignedQuality;
                }

            }

            ret.add(new Classification(minClass, minQuality, classifier.category));
        }

        return ret;
    }

    public static final class Classification {
        private int classification;
        private float quality;
        private String category;

        public Classification(int classification, float quality, Category category) {
            this.category = category.name().toLowerCase();
            this.quality = quality;
            this.classification = classification;
        }

        public int getClassification() {
            return classification;
        }

        public float getQuality() {
            return quality;
        }

        public String getCategory() {
            return category;
        }
    }

    public static class QoEClassificationThresholds {
        public enum Criteria {PING, DOWN, UP}
        final Category category;

        final Map<Criteria, Long[]> thresholds;

        public QoEClassificationThresholds(Category category, Map<Criteria, Long[]> thresholds) {
            this.category = category;
            this.thresholds = thresholds;
        }

        public QoEClassificationThresholds(Category category, ResultSet rs) throws SQLException {
            this.category = category;

            Map<Criteria, Long[]> thresholds = new HashMap<>();
            for (Criteria cat : Criteria.values()) {
                String columnName = ((cat == Criteria.DOWN) ? "dl" : (cat == Criteria.UP) ? "ul" : "ping");
                Long[] catThresholds = {
                        rs.getLong(columnName + "_4"),
                        rs.getLong(columnName + "_3"),
                        rs.getLong(columnName + "_2")
                };
                thresholds.put(cat,catThresholds);
            }

            this.thresholds = thresholds;
        }
    }

    public static List<QoEClassificationThresholds> getQoEClassificationsFromDb(Connection conn) {
        final String sql = "SELECT * FROM qoe_classification";
        List<QoEClassificationThresholds> ret = new ArrayList<>();

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                //get appropriate category
                String cat = rs.getString("category");
                if (!Enums.getIfPresent(Category.class, cat.toUpperCase()).isPresent()) {
                    System.out.printf("invalid category: " + cat);
                }
                Category category = Category.valueOf(cat.toUpperCase());
                ret.add(new QoEClassificationThresholds(category, rs));
            }

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

}
