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
package no.met.jtimeseries.netcdf.projection;

import static org.junit.Assert.*;

import org.junit.Test;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;



public class LonLatGridLocationConverterTest {

	private Array getArray(double[] values) {
		int[] shape = new int[1];
		shape[0] = values.length;
		Array a = new ArrayDouble(shape);
		for ( int i = 0; i < values.length; i ++ )
			a.setDouble(i, values[i]);
		return a;
	}
	
	private LonLatGridLocationConverter getConverter(double[] latitudes, double[] longitudes) {
		return new LonLatGridLocationConverter(getArray(latitudes), getArray(longitudes));
	}
	
	@Test
	public void testFindIndex() {

		double[] lon = {0,10,20,30,40};
		double[] lat = {0,1,2,3,4};
		LonLatGridLocationConverter converter = getConverter(lon, lat);
		GridLocation loc = converter.convert(23, 3);
		
		assertNotNull(loc);
		assertEquals(2.3, loc.getX(), 0.01);
		assertEquals(3, loc.getY(), 0.01);
	}
	
	@Test
	public void testOutOfRange1() {

		double[] lon = {0,10,20,30,40};
		double[] lat = {0,1,2,3,4};
		LonLatGridLocationConverter converter = getConverter(lon, lat);
		GridLocation loc = converter.convert(41, 3);
		
		assertNull(loc);
	}

	@Test
	public void testOutOfRange2() {

		double[] lon = {0,10,20,30,40};
		double[] lat = {0,1,2,3,4};
		LonLatGridLocationConverter converter = getConverter(lon, lat);
		GridLocation loc = converter.convert(-1, 3);
		
		assertNull(loc);
	}

	
	@Test
	public void negativeProgressionArray() {
		double[] lon = {1,3,5,7,9};
		double[] lat = {3,2,1,0,-1,-2};
		LonLatGridLocationConverter converter = getConverter(lon, lat);
		GridLocation loc = converter.convert(7, -1);
		
		assertNotNull(loc);
		assertEquals(3, loc.getX(), 0.01);
		assertEquals(4, loc.getY(), 0.01);
	}
}
