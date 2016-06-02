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
import java.util.TimeZone;

/**
 * Helper class to define a period of time. Joda time library could be used instead.
 */
public class TimePeriod {
    
    private Date start;
    private Date end;
    
    public TimePeriod(Date start, Date end) {
        this.start = start;
        this.end = end;
    }
    
    public TimePeriod(Date start, int numHours) {
    	this.start = start;
    	this.end = Utility.getDateWithAddedHours(start, numHours);
    }
    
    /**
     * Get a new TimePeriod, with added hours at both start and end 
     * @param hours Number of hours to add
     * @return The modified TimePeriod
     */
    public TimePeriod adjust(int hours) {
    	return new TimePeriod(Utility.getDateWithAddedHours(start, hours), Utility.getDateWithAddedHours(end, hours));
    }
    
    /**
     * Get the most similar TimePeriod where timePeriod's (start % snapTo == 0).
     * @param snapTo Hour to snap to
     * @return The modified TimePeriod
     */
    public TimePeriod adapt(int snapTo) {
    	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	cal.setTime(getStart());
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);

    	int offset = (snapTo - (cal.get(Calendar.HOUR) % snapTo));
    	cal.add(Calendar.HOUR, offset);

    	Date start = cal.getTime();

    	cal.setTime(getEnd());
    	cal.add(Calendar.HOUR, offset);
    	Date end = cal.getTime();
    	
    	return new TimePeriod(start, end);
    }

    
    public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	/**
     * Returns true if date is between or equal to current start/end time.
     */
    public boolean inside(Date date) {
    	if (date == null)
            return false;
    	
//    	if (start==null){
//    		start=date;
//    		Calendar cal = Calendar.getInstance();
//    		cal.setTime(start);
//            cal.add(Calendar.HOUR_OF_DAY, hours);
//            end = new Date(cal.getTimeInMillis());
//    	}

        if (isDefined()) {
            return ((start.getTime() <= date.getTime()) && 
                    (date.getTime() <= end.getTime()));
        }
        return false;
    }
    
    /**
     * Returns true if both start and end is defined
     */
    public boolean isDefined() {
        return (start != null && end!=null);
    }
    
    @Override
    public String toString() {
    	return start.toString() + " - " + end.toString();
    }
}
