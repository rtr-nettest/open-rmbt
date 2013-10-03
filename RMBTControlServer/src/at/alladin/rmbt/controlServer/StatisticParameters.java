package at.alladin.rmbt.controlServer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class StatisticParameters implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final String lang;
    private final float quantile;
    private final int months;
    private final int maxDevices;
    private final String type;
    private final String networkTypeGroup;
    
    public StatisticParameters(String defaultLang, String params)
    {
        String _lang = defaultLang;
        float _quantile = 0.8f;
        int _months = 2;
        int _maxDevices = 50;
        String _type = "mobile";
        String _networkTypeGroup = null;
        
        if (params != null && !params.isEmpty())
            // try parse the string to a JSON object
            try
            {
                final JSONObject request = new JSONObject(params);
                _lang = request.optString("language" , _lang);
                
                final double __quantile = request.optDouble("quantile", Double.NaN);
                if (__quantile >= 0 && __quantile <= 1)
                    _quantile = (float) __quantile;
                
                final int __months = request.optInt("months", 0);
                if (__months > 0)
                    _months = __months;
                
                final int __maxDevices = request.optInt("max_devices", 0);
                if (__maxDevices > 0)
                    _maxDevices = __maxDevices;
                
                final String __type = request.optString("type", null);
                if (__type != null)
                    _type = __type;
                
                final String __networkTypeGroup = request.optString("network_type_group", null);
                if (__networkTypeGroup != null && ! __networkTypeGroup.equalsIgnoreCase("all"))
                    _networkTypeGroup = __networkTypeGroup;
            }
            catch (final JSONException e)
            {
            }
        lang = _lang;
        quantile = _quantile;
        months = _months;
        maxDevices = _maxDevices;
        type = _type;
        networkTypeGroup = _networkTypeGroup;
    }

    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

    public String getLang()
    {
        return lang;
    }

    public float getQuantile()
    {
        return quantile;
    }

    public int getMonths()
    {
        return months;
    }

    public int getMaxDevices()
    {
        return maxDevices;
    }

    public String getType()
    {
        return type;
    }

    public String getNetworkTypeGroup()
    {
        return networkTypeGroup;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + maxDevices;
        result = prime * result + months;
        result = prime * result + ((networkTypeGroup == null) ? 0 : networkTypeGroup.hashCode());
        result = prime * result + Float.floatToIntBits(quantile);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatisticParameters other = (StatisticParameters) obj;
        if (lang == null)
        {
            if (other.lang != null)
                return false;
        }
        else if (!lang.equals(other.lang))
            return false;
        if (maxDevices != other.maxDevices)
            return false;
        if (months != other.months)
            return false;
        if (networkTypeGroup == null)
        {
            if (other.networkTypeGroup != null)
                return false;
        }
        else if (!networkTypeGroup.equals(other.networkTypeGroup))
            return false;
        if (Float.floatToIntBits(quantile) != Float.floatToIntBits(other.quantile))
            return false;
        if (type == null)
        {
            if (other.type != null)
                return false;
        }
        else if (!type.equals(other.type))
            return false;
        return true;
    }
    
    
}
