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

import java.net.URL;
import java.util.HashSet;
import java.util.Vector;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import no.met.jtimeseries.Location;

import org.junit.Test;


public class NetcdfMeteogramWrapperTest {

	@Test
	public void accessRemoteFile() throws Exception {
		// It is silly to assume that a remote server is up when we run unit tests
		/*
		NetcdfMeteogramWrapper.getChart(
				"http://thredds.met.no/thredds/fileServer/cryoclim/met.no/sie-mar/sie-mar_osisaf_monthly_mean_sie_mar.nc", 
				null);
		*/
	}
	
	@Test
	public void accessLocalFile() throws Exception {
		URL resource = getClass().getClassLoader().getResource("netcdf/missing_units.nc");
		NetcdfMeteogramWrapper.getChart(resource.toString(), null, ParameterReference.VARIABLE_NAME);
	}

	@Test
	public void requestMissingParameter() throws Exception {
		URL resource = getClass().getClassLoader().getResource("netcdf/missing_units.nc");
		Vector<String> parameters = new Vector<String>();
		parameters.add("nonoexistant");
		try {
			NetcdfMeteogramWrapper.getChart(resource.toString(), null, parameters, ParameterReference.VARIABLE_NAME, null);
			org.junit.Assert.fail("Expected Exception");
		}
		catch(ParseException e) {
		}
	}

	@Test
	public void requestTimeParameter() throws Exception {
		URL resource = getClass().getClassLoader().getResource("netcdf/missing_units.nc");
		Vector<String> parameters = new Vector<String>();
		parameters.add("time");
		parameters.add("geopotential_height");
		NetcdfMeteogramWrapper.getChart(resource.toString(), null, parameters, ParameterReference.VARIABLE_NAME, null);
	}

	
	@Test
	public void requestMissingFile() throws Exception {
		try {
			NetcdfMeteogramWrapper.getChart("/no_such_file.nc", null, ParameterReference.VARIABLE_NAME);
			org.junit.Assert.fail("Expected Exception");
		}
		catch(FileNotFoundException e) {
		}
	}
	
	@Test
	public void readFileWithCfRole() throws Exception {
		URL resource = getClass().getClassLoader().getResource("netcdf/cf_role.nc");
		try {
			NetcdfMeteogramWrapper.getChart(resource.toString(), null, ParameterReference.VARIABLE_NAME);
		}
		catch(ParseException e) {
		}
	}

	@Test
	public void extractNamesFromFileWithCfRole() throws Exception {
		URL resource = getClass().getClassLoader().getResource("netcdf/cf_role.nc");
		List<String> parameters = new Vector<String>();
		parameters.add("gsl[Kongsvegen]");
		NetcdfMeteogramWrapper.getChart(resource.toString(), null, parameters, ParameterReference.VARIABLE_NAME, null);
	}
	
}
