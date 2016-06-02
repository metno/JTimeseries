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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package no.met.jtimeseries;

/**
 *
 * @author sarfraza
 */
public class Location {

    private double longitude;
    private double latitude;
    private double altitude=-10000;

    private String polygon = "10 60.5, 10 61, 10.5 61, 10.5 60.5, 10 60.5";
    private String polygonId;

    /**
     * 
     * @param lon Longitude value as double
     * @param lat Latitude value as double
     */
    public Location(double lon, double lat) {
        longitude = lon;
        latitude = lat;
    }

    public String getPolygonId() {
        return polygonId;
    }

    public void setPolygonId(String polygonId) {
        this.polygonId = polygonId;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPolygon() {
        return polygon;
    }

    public void setPolygon(String polygon) {
        this.polygon = polygon;
    }

    @Override
    public String toString() {
    	return "POINT(" + getLongitude() + " " + getLatitude() + ")";
    }
}
