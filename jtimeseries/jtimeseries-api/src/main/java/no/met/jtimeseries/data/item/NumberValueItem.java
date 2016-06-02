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
package no.met.jtimeseries.data.item;

import java.util.Date;

/**
 * A class to represent single value item such as temperature etc.
 */
public class NumberValueItem extends AbstractValueItem implements Cloneable {

    private double value;

    /**
     * Create a single valued item where the timeFrom and timeTo is the same.
     * 
     * @param timeFrom
     * @param value
     */
    public NumberValueItem(Date timeFrom, double value) {
        super(timeFrom);
        this.value = value;
    }

    public NumberValueItem(Date timeFrom, Date timeTo, double value) {
        super(timeFrom, timeTo);
        this.value = value;
    }

    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value=value;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {

        }
        return null;
    }
    
    @Override
    public String toString() {
        return this.getTimeFrom()+"-"+this.getTimeTo()+" "+this.value;
    }
}
