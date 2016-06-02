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
package no.met.jtimeseries.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;

import no.met.phenomenen.NumberPhenomenon;

import org.junit.Test;

public class LocationForecastDataModelTest {


    @Test
    public void testTimeToAndFromEmptyModel() {

        DataModel model = new GenericDataModel();

        assertNull(model.getTimeFrom());
        assertNull(model.getTimeTo());

    }

    @Test
    public void testTimeToAndFromSingleValueModel(){

        DataModel model = singleValueModel();

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2012, 3, 3);
        Date timeFrom = cal.getTime();
        Date timeTo = cal.getTime();

        assertEquals(timeFrom, model.getTimeFrom());
        assertEquals(timeTo, model.getTimeTo());

    }

    private DataModel singleValueModel(){

        GenericDataModel m = new GenericDataModel();
        Calendar cal = Calendar.getInstance();
        cal.clear();

        NumberPhenomenon gp = new NumberPhenomenon();
        
        cal.set(2012, 3, 3);
        gp.addValue(cal.getTime(), 10.0);
        m.addPhenomenen("temp", gp);

        return m;
    }

    @Test
    public void testTimeToAndFromPartialModel(){

        DataModel model = partialModel();

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2012, 3, 3, 11, 0);
        Date start = cal.getTime();
        cal.set(2012, 3, 3, 14, 0);
        Date end = cal.getTime();

        assertEquals(start, model.getTimeFrom());
        assertEquals(end, model.getTimeTo());

    }

    private DataModel partialModel() {

        GenericDataModel m = new GenericDataModel();
        Calendar cal = Calendar.getInstance();
        cal.clear();

        NumberPhenomenon temp = new NumberPhenomenon();
        cal.set(2012, 3, 3, 12, 0);
        temp.addValue(cal.getTime(), 10.0);
        cal.set(2012, 3, 3, 13, 0);
        temp.addValue(cal.getTime(), 10.0);        
        m.addPhenomenen("temp", temp);
        
        
        NumberPhenomenon precip = new NumberPhenomenon();
        cal.set(2012, 3, 3, 12, 0);
        precip.addValue(cal.getTime(), cal.getTime(), 10.0);
        cal.set(2012, 3, 3, 14, 0);
        precip.addValue(cal.getTime(), cal.getTime(), 10.0);
        m.addPhenomenen("percipitation", precip);

        NumberPhenomenon cloudiness = new NumberPhenomenon();
        cal.set(2012, 3, 3, 11, 0);
        cloudiness.addValue(cal.getTime(), 10.0);
        cal.set(2012, 3, 3, 13, 0);
        cloudiness.addValue(cal.getTime(), 20.0);
        m.addPhenomenen("cloudiness", cloudiness);

        return m;


    }

}
