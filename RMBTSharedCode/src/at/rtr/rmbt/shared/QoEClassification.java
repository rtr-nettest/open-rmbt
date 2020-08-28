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

                //formula
                //a_1 := threshold between yellow / red
                //a_3 := threshold between dark green / green
                //c= sqrt(a_3/a_1)
                //a_0 := a1 / c ==> 0
                //a_2 := a1 * c ==> threshold yellow / green
                //a_4 := a3 * c ==> 1
                //a_n := a_(n-1) * c


                //percent := (log(x) - log(a_0))/(log(a_4)-log/a_0)
                double a1 = threshold[threshold.length-1];
                double a3 = threshold[0];
                double c = Math.sqrt(a3/a1);
                double a0 = a1 / c;
                double a2 = a1 * c;
                double a4 = a3 * c;

                assignedQuality = (float) ((Math.log(value) - Math.log(a0)) / (Math.log(a4) - Math.log(a0)));

                if (!inverse) {
                    //down, up
                    assignedClass = value >= a3 ? 4 : value >= a2 ? 3 : value >= a1 ? 2 : 1;

                } else {
                    assignedClass = value <= a3 ? 4 : value <= a2 ? 3 : value <= a1 ? 2 : 1;
                }
                assignedQuality = Math.max(0, Math.min(1, assignedQuality));

                //System.out.printf("%s / %s: a0: %d, a1: %d, a2: %d, a3: %d, a4: %d; val %d --> %.2f (class %d)%n",
                 //       classifier.category, entry.getKey(), (long) a0, (long) a1, (long) a2, (long) a3, (long) a4, value, assignedQuality, assignedClass);

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
