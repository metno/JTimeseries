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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import no.met.jtimeseries.netcdf.NetcdfFileExtractor;

import org.junit.Test;


public class UdUnitTimeProviderTest {

//	@Test
//	public void convertYears() throws Exception {
//		
//		UdUnitTimeProvider provider = new UdUnitTimeProvider("years since 1900-01-01");
//		
//		assertEquals(new Date(101,0,1), provider.getDate(101));
//	}

	// convertMonths?
	// convertDays?
	
	
// Tests removed due to timezone issues	
//	@Test
//	public void convertSeconds() throws Exception {
//		
//		UdUnitTimeProvider provider = new UdUnitTimeProvider("seconds since 1970-01-01");
//		
//		assertEquals(new Date(112, 4, 7, 13, 0), provider.getDate(1336388400));
//	}
//
//	@Test
//	public void convertSecondsFromOtherTimeThanEpoch() throws Exception {
//		
//		UdUnitTimeProvider provider = new UdUnitTimeProvider("seconds since 1978-01-01");
//		
//		assertEquals(new Date(120, 4, 7, 13, 0), provider.getDate(1336388400));
//	}

	
}
