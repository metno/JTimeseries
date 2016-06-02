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

import java.util.Calendar;
import java.util.Date;

import org.jfree.data.time.RegularTimePeriod;

/**
 * A flexible time period that is used for time periods that are multiples of a
 * whole hour.
 *
 */
public class FlexibleHour extends RegularTimePeriod {

    private final Date date;
    private final int numHours;

    public FlexibleHour(Date date, int numHours) {
        this.date = date;
        this.numHours = numHours;
    }

    @Override
    public int compareTo(Object o1) {

        if (o1 instanceof FlexibleHour) {
            return this.date.compareTo(((FlexibleHour) o1).date);
        } else {
            // consider time periods to be ordered after general objects
            return 1;
        }
    }

    @Override
    public long getFirstMillisecond() {
        return date.getTime();
    }

    @Override
    public long getFirstMillisecond(Calendar cal) {

        cal.setTime(date);
        return cal.getTimeInMillis();
    }

    @Override
    public long getLastMillisecond() {

        Calendar cal = Calendar.getInstance();
        return getLastMillisecond(cal);

    }

    @Override
    public long getLastMillisecond(Calendar cal) {
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, numHours);
        return cal.getTimeInMillis();

    }

    @Override
    public long getSerialIndex() {
        return date.getTime();
    }

    @Override
    public RegularTimePeriod next() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, numHours);

        return new FlexibleHour(cal.getTime(), numHours);
    }

    @Override
    public void peg(Calendar arg0) {
        // This method did not seem relevant to implement as we do not store the
        // milliseconds, but rather re-calculate them every time.
        // This method also seems to violate the immutability of the class that
        // is required according to the documentation.
    }

    @Override
    public RegularTimePeriod previous() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, -numHours);
        return new FlexibleHour(cal.getTime(), numHours);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += date.hashCode() * 37;
        return result;
    }

    @Override
    public boolean equals(Object o){

        if( !(o instanceof FlexibleHour)){
            return false;
        }
        FlexibleHour fh = (FlexibleHour) o;

        if( this.numHours != fh.numHours) {
            return false;
        }

        return this.date.equals(fh.date);

    }

}
