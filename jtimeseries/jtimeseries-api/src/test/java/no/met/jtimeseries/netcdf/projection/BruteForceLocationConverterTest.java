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

public class BruteForceLocationConverterTest {

	private int getYSize(double[][] data) {
		return data.length;
	}
	
	private int getXSize(double[][] data) {
		return data[0].length;
	}
	
	private BruteForceLocationConverter getConverter(double[][] longitudes, double[][] latitudes) {
		int xSize = getXSize(longitudes);
		int ySize = getYSize(latitudes);
		
		assertEquals(xSize, getXSize(longitudes));
		assertEquals(ySize, getYSize(latitudes));

		int fullSize = xSize * ySize;
		
		int[] shape = new int[1];
		shape[0] = fullSize;
		Array lon = new ArrayDouble(shape);
		Array lat = new ArrayDouble(shape);
		
		for ( int y = 0; y < ySize; y ++ ) {
			for ( int x = 0; x < xSize; x ++ ) {
				int index = (y * xSize) + x;
				lon.setDouble(index, longitudes[y][x]);
				lat.setDouble(index, latitudes[y][x]);
			}
		}		
		return new BruteForceLocationConverter(xSize, ySize, lon, lat);
	}

	@Test
	public void test() {
		double[][] longitudes = {
				{1,2,3},
				{1,2,3},
				{1,2,3}
		};
		double[][] latitudes = {
				{5,5,5},
				{4,4,4},
				{3,3,3}
		};
		
		BruteForceLocationConverter converter = getConverter(longitudes, latitudes);
		GridLocation location = converter.convert(2, 4);
		
		assertEquals(new GridLocation(1,1), location);
	}
	
	@Test
	public void test2() {
		double[][] longitudes = {
				{1,2,3,4},
				{1,2,3,4},
				{1,2,3,4}
		};
		double[][] latitudes = {
				{5,5,5,5},
				{4,4,4,4},
				{3,3,3,3}
		};
		
		BruteForceLocationConverter converter = getConverter(longitudes, latitudes);
		GridLocation location = converter.convert(2, 3);
		
		assertEquals(new GridLocation(1,2), location);
	}

}
