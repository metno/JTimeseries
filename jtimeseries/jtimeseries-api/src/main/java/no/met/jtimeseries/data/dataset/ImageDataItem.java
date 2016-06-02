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

import java.awt.Image;
import java.io.Serializable;

/**
 * The basic data type in ImageDataset
 *
 */
public class ImageDataItem implements Comparable<ImageDataItem>, Serializable{

	private static final long serialVersionUID = 3326665266382313697L;
	private Image image;
	private Number x;
	private Number position;
	private Number offset;
	
	/**
	 * 
	 * @param x Long value of time
	 * @param direction The degree of arrow
	 */
	public ImageDataItem(Number x, Image image, Number position, Number offset) {
	    this.x=x;
		this.image=image;
		this.position=position;
		this.offset=offset;
	}

	public Image getImage() {
		return image;
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
        if (!(obj instanceof ImageDataItem)) {
            return false;
        }
        ImageDataItem that = (ImageDataItem) obj;
        if (!this.x.equals(that.x)) {
            return false;
        }
        if (!this.image.equals(that.image)) {
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
    public int compareTo(ImageDataItem object) {
        if (object instanceof ImageDataItem) {
            ImageDataItem item = (ImageDataItem) object;
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
