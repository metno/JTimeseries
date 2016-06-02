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
package no.met.jtimeseries.netcdf;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import no.met.jtimeseries.Location;

import org.junit.Test;


public class NetcdfFileExtractorTest {

	private URL getResource(String path) {
		return getClass().getClassLoader().getResource(path);
	}
	private NetcdfFileExtractor getExtractor(String path) throws IOException {
		URL url = getResource(path);
		return new NetcdfFileExtractor(url);		
	}
	
	@Test
	public void testOpenNonExistingFile() {
		try {
			new NetcdfFileExtractor("/no/such/file.nc");
			fail("Opening non-existing file did not fail");
		}
		catch (IOException e) 
		{}
	}

	@Test
	public void testTimes() throws IOException, ParseException {
		NetcdfFileExtractor extractor = getExtractor("netcdf/simple.nc");
		
		List<Date> dates = extractor.getValidTimes();
		assertEquals(2, dates.size());

		assertEquals(new Date(1308398400000L), dates.get(0));
		assertEquals(new Date(1308409200000L), dates.get(1));
	}

	@Test 
	public void testUnits() throws IOException, ParseException {
		NetcdfFileExtractor extractor = getExtractor("netcdf/simple.nc");
		
		assertEquals("K", extractor.getUnit("air_potential_temperature"));
		assertEquals("m", extractor.getUnit("geopotential_height"));
		assertEquals("m/s", extractor.getUnit("x_wind"));
	}

	@Test 
	public void testUnitFromMissingParameter() throws IOException, ParseException {
		NetcdfFileExtractor extractor = getExtractor("netcdf/simple.nc");

		assertFalse(extractor.hasVariable("no_such_parameter"));
		assertNull(extractor.getUnit("no_such_parameter"));
	}
	
	@Test
	public void testMissingUnitInSource() throws IOException {
		NetcdfFileExtractor extractor = getExtractor("netcdf/missing_units.nc");

		assertTrue(extractor.hasVariable("geopotential_height"));
		assertNull(extractor.getUnit("geopotential_height"));
	}
	
	@Test
	public void testGetValuesWithNullLocation() throws Exception {
		NetcdfFileExtractor extractor = getExtractor("netcdf/missing_units.nc");

		List<Double> data = extractor.getValues("geopotential_height", null, null);

		assertEquals(2, data.size());
		assertEquals(24234.43, data.get(0).doubleValue(), 0.001);
		assertEquals(24234.6, data.get(1).doubleValue(), 0.001);
	}

	@Test
	public void testGetLocatedValuesOutsideGrid() throws Exception {
		NetcdfFileExtractor extractor = getExtractor("netcdf/simple.nc");
		List<Double> data = extractor.getValues("geopotential_height", new Location(-5, 12), null);
		
		assertNull(data);
	}
	
	@Test
	public void testGetLocatedValues() throws IOException, ParseException {
		
		// hardcoded support for an extra dimension has been removed - this 
		// test should be reinstated once this feature has been implemented 
		// properly 
		
//		NetcdfFileExtractor extractor = getExtractor("netcdf/simple.nc");
//
//		List<Double> data = extractor.getValues("geopotential_height", new Location(6, 62));
//		
//		assertEquals(2, data.size());
//
//		assertEquals(24236.71, data.get(0).doubleValue(), 0.005);
//		assertEquals(24249.56, data.get(1).doubleValue(), 0.005);
	}
}
