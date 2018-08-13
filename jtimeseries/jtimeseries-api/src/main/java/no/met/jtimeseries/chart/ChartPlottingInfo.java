/*******************************************************************************
 *   Copyright (C) 2016 MET Norway
 *   Contact information:
 *   Norwegian Meteorological Institute
 *   Henrik Mohns Plass 1
 *   0313 OSLO
 *   NORWAY
 *
 *   This file is part of jTimeseries
 *   jTimeseries is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *   jTimeseries is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *   You should have received a copy of the GNU General Public License
 *   along with jTimeseries; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/
package no.met.jtimeseries.chart;

public class ChartPlottingInfo {
    private final int width;
    private final int height;
    private final double longitude;
    private final double latitude;
    private final double altitude;
    private final boolean showWaterTemperature;
    private final boolean showAirTemperature;
    private final boolean showDewpointTemperature;
    private final boolean showPressure;
    private final boolean showWaveDirection;
    private final boolean showWaveHeight;
    private final boolean showCurrentDirection;
    private final boolean showCurrentSpeed;
    private final boolean showWindDirection;
    private final boolean showWindSpeed;
    private final boolean showWindSymbol;
    private final boolean showCloudSymbol;
    private final boolean showWeatherSymbol;
    private final boolean showPrecipitation;
    private final boolean showAccumulatedPrecipitation;
    private final String language;
    private String windSpeedUnit = "ms";
    private String timezone = "UTC";
    
  
    private ChartPlottingInfo(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.altitude = builder.altitude;
        this.showAirTemperature = builder.showAirTemperature;
        this.showWaterTemperature = builder.showWaterTemperature;
        this.showDewpointTemperature = builder.showDewpointTemperature;
        this.showPressure = builder.showPressure;
        this.showWaveDirection = builder.showWaveDirection;
        this.showWaveHeight = builder.showWaveHeight;
        this.showCurrentDirection = builder.showCurrentDirection;
        this.showCurrentSpeed = builder.showCurrentSpeed;
        this.showWindDirection = builder.showWindDirection;
        this.showWindSpeed = builder.showWindSpeed;
        this.showPrecipitation = builder.showPrecipitation;
        this.showAccumulatedPrecipitation = builder.showAccumulatedPrecipitation;
        this.showCloudSymbol = builder.showCloudSymbol;
        this.showWindSymbol = builder.showWindSymbol;
        this.showWeatherSymbol = builder.showWeatherSymbol;        
        this.timezone = builder.timezone;
        this.language = builder.language;
        this.windSpeedUnit = builder.windSpeedUnit;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public String getTimezone() {
        return timezone;
    }     

    public String getLanguage() {
        return language;
    }
    
    public String getWindSpeedUnit() {
        return windSpeedUnit;
    }

    public boolean isShowWaterTemperature() {
        return showWaterTemperature;
    }

    public boolean isShowAirTemperature() {
        return showAirTemperature;
    }
    
    public boolean isShowDewpointTemperature() {
        return showDewpointTemperature;
    }

    public boolean isShowPressure() {
        return showPressure;
    }    
    
    public boolean isShowWaveDirection() {
        return showWaveDirection;
    }

    public boolean isShowWaveHeight() {
        return showWaveHeight;
    }
    
    public boolean isShowCurrentDirection() {
        return showCurrentDirection;
    }

    public boolean isShowCurrentSpeed() {
        return showCurrentSpeed;
    }
    
    public boolean isShowWindDirection() {
        return showWindDirection;
    }

    public boolean isShowWindSpeed() {
        return showWindSpeed;
    }

    public boolean isShowWindSymbol() {
        return showWindSymbol;
    }

    public boolean isShowCloudSymbol() {
        return showCloudSymbol;
    }

    public boolean isShowWeatherSymbol() {
        return showWeatherSymbol;
    }

    public boolean isShowPrecipitation() {
        return showPrecipitation;
    }

    public boolean isShowAccumulatedPrecipitation() {
        return showAccumulatedPrecipitation;
    }


    public static class Builder {
        private int width;
        private int height;
        private double longitude;
        private double latitude;
        private double altitude;
        private boolean showWaterTemperature;
        private boolean showAirTemperature;
        private boolean showDewpointTemperature;
        private boolean showPressure;
        private boolean showWaveDirection;
        private boolean showWaveHeight;
        private boolean showCurrentDirection;
        private boolean showCurrentSpeed;
        private boolean showWindDirection;
        private boolean showWindSpeed;
        private boolean showWindSymbol;
        private boolean showCloudSymbol;
        private boolean showWeatherSymbol;
        private boolean showPrecipitation;
        private boolean showAccumulatedPrecipitation;
        private String timezone;
        private String language;
        private String windSpeedUnit;
        
        private Builder() {
        }
        
        public Builder(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
        
        public Builder width(int width) {
            this.width = width;
            return this;
        }
        
        public Builder height(int height) {
            this.height = height;
            return this;
        }
        
        public Builder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }
        
        public Builder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }
        
        public Builder altitude(double altitude) {
            this.altitude = altitude;
            return this;
        }
        
        public Builder showWaterTemperature(boolean swt) {
            this.showWaterTemperature = swt;
            return this;
        }
        
        public Builder showAirTemperature(boolean sat) {
            this.showAirTemperature = sat;
            return this;
        }
        
        public Builder showPressure(boolean pressure) {
            this.showPressure = pressure;
            return this;
        }

        public Builder showWaveDirection(boolean waveDirection){
            this.showWaveDirection = waveDirection;
            return this;
        }
        
        public Builder showWaveHeight(boolean waveHeight){
            this.showWaveHeight = waveHeight;
            return this;
        }
        
        public Builder showCurrentDirection(boolean currentDirection){
            this.showCurrentDirection = currentDirection;
            return this;
        }
        
        public Builder showCurrentSpeed(boolean currentSpeed){
            this.showCurrentSpeed = currentSpeed;
            return this;
        }
        
        public Builder showWindDirection(boolean windDirection){
            this.showWindDirection = windDirection;
            return this;
        }
        
        public Builder showWindSpeed(boolean windSpeed){
            this.showWindSpeed = windSpeed;
            return this;
        }
        
        public Builder showWindSymbol(boolean windSymbol){
            this.showWindSymbol = windSymbol;
            return this;
        }
        
        public Builder showWeatherSymbol(boolean weatherSymbol){
            this.showWeatherSymbol = weatherSymbol;
            return this;
        }
        
        public Builder showCloudSymbol(boolean cloudSymbol){
            this.showCloudSymbol = cloudSymbol;
            return this;
        } 
        
        public Builder showPrecipitation(boolean precipitation){
            this.showPrecipitation = precipitation;
            return this;
        }

        public Builder showAccumulatedPrecipitation(boolean accumulatedPrecipitation){
            this.showAccumulatedPrecipitation = accumulatedPrecipitation;
            return this;
        }

        public Builder windSpeedUnit(String windSpeedUnit) {
            this.windSpeedUnit=windSpeedUnit;
            return this;
        }
        
        public Builder showDewpointTemperature(boolean dewpointTemperature) {
            this.showDewpointTemperature=dewpointTemperature;
            return this;
        }
        
        public Builder timezone(String timezone){
            this.timezone=timezone;
            return this;
        }
        
        public Builder language(String language){
            this.language = language;
            return this;
        }
        
        public ChartPlottingInfo build() {
            return new ChartPlottingInfo(this);
        }

    }

    @Override
    public String toString() {
        return "ChartPlottingInfo{" + "width=" + width + ", height=" + height + ", longitude=" + longitude 
                + ", latitude=" + latitude + ", altitude=" + altitude + ", showWaterTemperature=" 
                + showWaterTemperature + ", showAirTemperature=" + showAirTemperature + ", showPressure=" 
                + showPressure + ", showWaveDirection=" + showWaveDirection + ", showWaveHeight=" + showWaveHeight
                + ", showWindSymbol=" + showWindSymbol + ", showCloudSymbol=" + showCloudSymbol 
                + ", showWeatherSymbol=" + showWeatherSymbol + ", showPrecipitation=" + showPrecipitation
                + ", showAccumulatedPrecipitation=" + showAccumulatedPrecipitation
                + ", showCurrentDirection=" + showCurrentDirection + ", showCurrentSpeed=" + showCurrentSpeed 
                + ", showWindDirection=" + showWindDirection + ", showWindSpeed=" + showWindSpeed 
                + ", showDewpointTemperature=" + showDewpointTemperature 
                + ", windSpeedUnit=" + windSpeedUnit + ", language=" + language + ", timezone=" + timezone + '}';
    }
}
