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
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

class LonLatGridLocationConverter implements LocationConverter {

	private Array x = null;
	private Array y = null;

	public LonLatGridLocationConverter(NetcdfFile source) throws IOException, ParseException {
		for ( Dimension dimension : source.getDimensions() ) {
			Variable var = source.findVariable(dimension.getName());
			if ( var != null ) {
				Attribute axis = var.findAttribute("axis");
				if ( axis != null && axis.isString() ) {
					String value = axis.getStringValue();
					if ( value.equals("X") ) {
						if ( x != null )
							throw new ParseException("Many x dimensions in document", 0);
						x = var.read();
					}
					else if ( value.equals("Y") ) {
						if ( y != null )
							throw new ParseException("Many y dimensions in document", 0);
						y = var.read();
					}
				}
			}
		}
		if ( x == null )
			throw new ParseException("Unable to find x dimension in data", 0);
		if ( y == null )
			throw new ParseException("Unable to find y dimension in data", 0);
	}

	/**
	 * For testing
	 */
	LonLatGridLocationConverter(Array x, Array y) {
		this.x = x;
		this.y = y;
	}
	
	private double nearestIndex(Array a, double wantedValue) {

		boolean risingValues = a.getDouble(0) < a.getDouble(1);
		
		if ( risingValues ) {
			int low = 0;
			while ( low +1 < a.getSize() && a.getDouble(low +1) < wantedValue )
				low ++;
			
			double lowValue = a.getDouble(low);
			if ( low == a.getSize() -1 && lowValue != wantedValue )
				return -1;
			
			double highValue = a.getDouble(low +1);
			
			if ( lowValue > wantedValue )
				return -1;
			if (highValue < wantedValue )
				return -1;
			
			double distanceToLow = wantedValue - lowValue;
			double distanceToHigh = highValue - wantedValue;
	
			double decimal = (1/(distanceToLow + distanceToHigh)) * distanceToLow;
			return low + decimal;
		}
		else {
			int low = (int) (a.getSize() -1);
			while ( low > 0 && a.getDouble(low -1) < wantedValue )
				low --;
			
			double lowValue = a.getDouble(low);
			if ( low == 0 && lowValue != wantedValue )
				return -1;
			
			double highValue = a.getDouble(low -1);
			
			if ( lowValue > wantedValue )
				return -1;
			if (highValue < wantedValue )
				return -1;
			
			double distanceToLow = wantedValue - lowValue;
			double distanceToHigh = highValue - wantedValue;
	
			double decimal = (1/(distanceToLow + distanceToHigh)) * distanceToLow;
			return low - decimal;
		}
	}

	
	@Override
	public GridLocation convert(double longitude, double latitude) {

		double xIndex = nearestIndex(x, longitude);
		double yIndex = nearestIndex(y, latitude);

		if ( xIndex < 0 || yIndex < 0 )
			return null;
		
		return new GridLocation(xIndex, yIndex);
	}
}
