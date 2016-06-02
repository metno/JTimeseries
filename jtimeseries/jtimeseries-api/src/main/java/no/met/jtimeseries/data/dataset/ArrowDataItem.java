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
 * The basic data type in ArrowDataset
 *
 */
public class ArrowDataItem implements Comparable<ArrowDataItem>, Serializable{

	private static final long serialVersionUID = 3326665266382313697L;
	private Number direction;
	private Number x;
	private Number position;
	private Number offset;
	
	/**
	 * 
	 * @param x Long value of time
	 * @param direction The degree of arrow
	 */
	public ArrowDataItem(Number x, Number direction, Number position, Number offset) {
	    this.x=x;
		this.direction=direction;
		this.position=position;
		this.offset=offset;
	}

	public Number getDirection() {
		return direction;
	}

	public Number getX() {
		return x;
	}
	
	public Number getPosition() {
		return position;
	}

	public Number getOffset() {
		return offset;
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
        if (!(obj instanceof ArrowDataItem)) {
            return false;
        }
        ArrowDataItem that = (ArrowDataItem) obj;
        if (!this.x.equals(that.x)) {
            return false;
        }
        if (!this.direction.equals(that.direction)) {
            return false;
        }
        if (!this.position.equals(that.position)) {
            return false;
        }
        if (!this.offset.equals(that.offset)) {
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
    public int compareTo(ArrowDataItem object) {
        if (object instanceof ArrowDataItem) {
            ArrowDataItem item = (ArrowDataItem) object;
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
            throw new ClassCastException("ArrowDataItem.compareTo(error)");
        }
    }
}
