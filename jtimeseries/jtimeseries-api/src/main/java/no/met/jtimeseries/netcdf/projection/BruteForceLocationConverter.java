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

import java.io.IOException;
import java.text.ParseException;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;

/**
 * Does a simple linear search through lat/lon variables to find the closest 
 * matching index
 */
public class BruteForceLocationConverter implements LocationConverter {

	private int xSize;
	private int ySize;
	private Array longitudes = null;
	private Array latitudes = null;

	
	public BruteForceLocationConverter(NetcdfFile source) throws IOException, ParseException {
		
		// fill in this
		
	}
	
	// for testing purposes
	public BruteForceLocationConverter(int xSize, int ySize, Array longitudes, Array latitudes) {
		this.xSize = xSize;
		this.ySize = ySize;
		this.longitudes = longitudes;
		this.latitudes = latitudes;
	}
	
	
	@Override
	public GridLocation convert(double longitude, double latitude) {
		
		GridLocation ret = new GridLocation(-1, -1);
		
		double smallestDistance = Double.MAX_VALUE;
		
		for ( int y = 0; y < ySize; y ++ ) {
			for ( int x = 0; x < xSize; x ++ ) {
				int index = (y * xSize) + x;
				double relativeLongitude = longitude - longitudes.getDouble(index);
				double relativeLatitude  = latitude - latitudes.getDouble(index);
				double distance = Math.sqrt((relativeLongitude * relativeLongitude) + (relativeLatitude * relativeLatitude));
				if ( distance < smallestDistance ) {
					smallestDistance = distance;
					ret.setX(x);
					ret.setY(y);
				}
			}
		}
		// should never happen:
		if ( ret.getX() == -1 )
			return null;
		
		return ret;
	}

}
