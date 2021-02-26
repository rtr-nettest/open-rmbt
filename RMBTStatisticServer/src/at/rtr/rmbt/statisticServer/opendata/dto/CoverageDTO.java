package at.rtr.rmbt.statisticServer.opendata.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.GeoJsonObject;

public class CoverageDTO {
    private String operator;
    private String raster;
    private Integer downloadKbitMax;
    private Integer uploadKbitMax;
    private Integer downloadKbitNormal;
    private Integer uploadKbitNormal;
    private String technology;
    private String lastUpdated;
    private GeoJsonObject rasterGeoJson;


    public GeoJsonObject getRasterGeoJson() {
        return rasterGeoJson;
    }

    public void setRasterGeoJson(GeoJsonObject rasterGeoJson) {
        this.rasterGeoJson = rasterGeoJson;
    }

    public void setGeoJson(String json) {
        ObjectMapper om = new ObjectMapper();
        try {
            GeoJsonObject geoJsonObject = om.readValue(json, GeoJsonObject.class);
            this.rasterGeoJson = geoJsonObject;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRaster() {
        return raster;
    }

    public void setRaster(String raster) {
        this.raster = raster;
    }

    public Integer getDownloadKbitMax() {
        return downloadKbitMax;
    }

    public void setDownloadKbitMax(Integer downloadKbitMax) {
        this.downloadKbitMax = downloadKbitMax;
    }

    public Integer getUploadKbitMax() {
        return uploadKbitMax;
    }

    public void setUploadKbitMax(Integer uploadKbitMax) {
        this.uploadKbitMax = uploadKbitMax;
    }

    public Integer getDownloadKbitNormal() {
        return downloadKbitNormal;
    }

    public void setDownloadKbitNormal(Integer downloadKbitNormal) {
        this.downloadKbitNormal = downloadKbitNormal;
    }

    public Integer getUploadKbitNormal() {
        return uploadKbitNormal;
    }

    public void setUploadKbitNormal(Integer uploadKbitNormal) {
        this.uploadKbitNormal = uploadKbitNormal;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public static class Raster {
        private String type;
        private int[][][] coordinates;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int[][][] getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(int[][][] coordinates) {
            this.coordinates = coordinates;
        }
    }
}
