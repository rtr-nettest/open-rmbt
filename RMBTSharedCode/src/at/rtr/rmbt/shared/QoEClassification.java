package at.rtr.rmbt.shared;

import java.util.ArrayList;
import java.util.List;

public final class QoEClassification {
    public enum Category {STREAMING_AUDIO_STREAMING, VIDEO_SD, VIDEO_HD,
        VIDEO_UHD, GAMING, GAMING_CLOUD, GAMING_STREAMING,
        GAMING_DOWNLOAD, VOIP, VIDEO_TELEPHONY, VIDEO_CONFERENCING,
        MESSAGING, WEB, CLOUD}

    public static List<Classification> classify(long pingNs, long downKbps, long upKbps) {
        ArrayList<Classification> ret = new ArrayList<>();

        for (Category cat : Category.values()) {
            if (Math.random() < 0.95) {
                ret.add(new Classification((int) (Math.random()*5), (float) Math.random(), cat));
            }
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
}
