package at.rtr.rmbt.android.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

/**
 * Created by Extern on 19.06.2017.
 */
@TargetApi(value = Build.VERSION_CODES.LOLLIPOP_MR1)
public class SubscriptionInfoHelper {
    private final SubscriptionManager subscriptionManager;

    public SubscriptionInfoHelper(Context context) {
        subscriptionManager = (SubscriptionManager) context
                .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

    }

    /**
     * Get information about the SIM that is currently used for data
     * @return info or NULL if there is no active sim or the active sim cannot be detected
     */
    public ActiveDataSubscriptionInfo getActiveDataSubscriptionInfo() {
        SubscriptionInfo activeSubscription;
        ActiveDataSubscriptionInfo info = new ActiveDataSubscriptionInfo();
        if (subscriptionManager.getActiveSubscriptionInfoCount() > 1) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                int dataSubscriptionId = subscriptionManager.getDefaultDataSubscriptionId();
                activeSubscription = subscriptionManager.getActiveSubscriptionInfo(dataSubscriptionId);
            }
            else {
                activeSubscription = null;
            }
        }
        else if (subscriptionManager.getActiveSubscriptionInfoCount() == 1) {
            activeSubscription = subscriptionManager.getActiveSubscriptionInfoList().get(0);
        }
        else {
            activeSubscription = null;
        }

        if (activeSubscription != null) {
            //fill info from this
            info.setCountry(activeSubscription.getCountryIso());
            String simOperator = activeSubscription.getMcc() + "-" + String.format("%02d", activeSubscription.getMnc());
            info.setSimOperator(String.valueOf(simOperator));
            info.setSimOperatorName(activeSubscription.getCarrierName().toString());
            info.setDisplayName(activeSubscription.getDisplayName().toString());
            info.setSimCount(subscriptionManager.getActiveSubscriptionInfoCount());
            return info;
        }

        return null;
    }

    public int getActiveSimCount() {
        return subscriptionManager.getActiveSubscriptionInfoCount();
    }

    public static class ActiveDataSubscriptionInfo {
        private String country;
        private String simOperator;
        private String simOperatorName;
        private String displayName;
        private int simCount;


        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getSimOperator() {
            return simOperator;
        }

        public void setSimOperator(String simOperator) {
            this.simOperator = simOperator;
        }

        public String getSimOperatorName() {
            return simOperatorName;
        }

        public void setSimOperatorName(String simOperatorName) {
            this.simOperatorName = simOperatorName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public int getSimCount() {
            return simCount;
        }

        public void setSimCount(int simCount) {
            this.simCount = simCount;
        }
    }
}
