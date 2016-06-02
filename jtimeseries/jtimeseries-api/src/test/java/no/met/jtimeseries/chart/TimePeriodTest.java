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

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class TimePeriodTest {

    @Test
    public void testInside() {
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 1, 1, 0, 0, 0); 
        Date d1 = new Date(cal.getTimeInMillis()); // 2000-01-01 00:00
        cal.set(2000, 1, 1, 1, 0, 0);
        Date d2 = new Date(cal.getTimeInMillis()); // 2000-01-01 01:00
        TimePeriod period = new TimePeriod(d1, d2);
        cal.set(2000, 1, 1, 0, 30, 0);
        Date d3 = new Date(cal.getTimeInMillis()); // 2000-01-01 00:30
        cal.set(2000, 1, 1, 1, 30, 0);
        Date d4 = new Date(cal.getTimeInMillis()); // 2000-01-01 01:30
        cal.set(1999, 1, 1, 1, 30, 0);
        Date d5 = new Date(cal.getTimeInMillis()); // 1999-01-01 01:30
        Date d6 = new Date(d1.getTime() - 1);
        Date d7 = new Date(d1.getTime() + 1);
        Date d8 = new Date(d2.getTime() - 1);
        Date d9 = new Date(d2.getTime() + 1);

        assertTrue(period.inside(d1));
        assertTrue(period.inside(d2));
        assertTrue(period.inside(d3));
        assertFalse(period.inside(d4));
        assertFalse(period.inside(d5));
        assertFalse(period.inside(d6));
        assertTrue(period.inside(d7));
        assertTrue(period.inside(d8));
        assertFalse(period.inside(d9));
        
    }

    @Test
    public void testIsDefined() {
        TimePeriod undef1 = new TimePeriod(null, new Date());
        assertFalse(undef1.isDefined());
        TimePeriod undef2 = new TimePeriod(new Date(), null);
        assertFalse(undef2.isDefined());
        TimePeriod def1 = new TimePeriod(new Date(), new Date());
        assertTrue(def1.isDefined());
    }

}
