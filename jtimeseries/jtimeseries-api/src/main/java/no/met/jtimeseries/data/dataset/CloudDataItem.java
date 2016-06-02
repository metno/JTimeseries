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
package no.met.jtimeseries.data.dataset;

import java.io.Serializable;

/**
 * The basic data type in CloudDataset
 *
 */
public class CloudDataItem implements Comparable<CloudDataItem>, Serializable{

    private static final long serialVersionUID = -460495167179655930L;
    private Number fog;
	private Number highClouds;
	private Number mediumClouds;
	private Number lowClouds;
	private Number x;
	
	/**
	 * Constructor of CloudDataItem
	 * @param x Long value of time
	 * @param fog Value of fog
	 * @param highClouds Value of high clouds
	 * @param mediumClouds Value of medium clouds
	 * @param lowClouds Value of low clouds
	 */
	public CloudDataItem(Number x, Number fog, Number highClouds,
			Number mediumClouds, Number lowClouds) {
	    this.x=x;
		this.fog=fog;
		this.highClouds=highClouds;
		this.mediumClouds=mediumClouds;
		this.lowClouds=lowClouds;
	}

	public Number getFog() {
		return fog;
	}

	public Number getHighClouds() {
		return highClouds;
	}

	public Number getMediumClouds() {
		return mediumClouds;
	}

	public Number getLowClouds() {
		return lowClouds;
	}

	public Number getX() {
		return x;
	}
	
	/**
     * Checks this instance for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CloudDataItem)) {
            return false;
        }
        CloudDataItem that = (CloudDataItem) obj;
        if (!this.x.equals(that.x)) {
            return false;
        }
        if (!this.fog.equals(that.fog)) {
            return false;
        }
        if (!this.highClouds.equals(that.highClouds)) {
            return false;
        }
        if (!this.mediumClouds.equals(that.mediumClouds)) {
            return false;
        }
        if (!this.lowClouds.equals(that.lowClouds)) {
            return false;
        }
        return true;
    }
    
    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param object  the object to compare to.
     *
     * @return A negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */
    public int compareTo(CloudDataItem object) {
        if (object instanceof CloudDataItem) {
            CloudDataItem item = (CloudDataItem) object;
            if (this.x.doubleValue() > item.x.doubleValue()) {
                return 1;
            }
            else if (this.x.equals(item.x)) {
                return 0;
            }
            else {
                return -1;
            }
        }
        else {
            throw new ClassCastException("CloudDataItem.compareTo(error)");
        }
    }
}
