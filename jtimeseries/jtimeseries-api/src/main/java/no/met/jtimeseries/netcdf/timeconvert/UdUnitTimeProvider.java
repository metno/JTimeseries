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
package no.met.jtimeseries.netcdf.timeconvert;

import java.util.Date;

import ucar.nc2.units.DateUnit;


class UdUnitTimeProvider implements TimeProvider {

	private DateUnit units;
	
	public UdUnitTimeProvider(String units) throws InvalidUnitSpecificationException {

		try {
			this.units = new DateUnit(units);
		}
		catch ( Exception e ) {
			throw new InvalidUnitSpecificationException(units);
		}
	}
	
	@Override
	public Date getDate(long value) {

		return units.makeDate((double) value);
	}

}
