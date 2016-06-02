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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Set;
import java.util.Vector;

import no.met.jtimeseries.ChartFrame;
import no.met.jtimeseries.Location;

import org.jfree.chart.JFreeChart;

import ucar.ma2.InvalidRangeException;


public class NetcdfMeteogramWrapper {

	public static final int DEFAULT_HEIGHT = 300;
	public static final int DEFAULT_WIDTH = 750;

	public static JFreeChart getChart(String resource, Location location, ParameterReference parameterRefersTo) throws FileNotFoundException, IOException, ParseException, InvalidRangeException {
		return getChart(resource, location, null, parameterRefersTo, null);
	}

	public static JFreeChart getChart(String resource, Location location, Iterable<String> parameters, ParameterReference parameterRefersTo, String header) 
			throws FileNotFoundException, IOException, ParseException, InvalidRangeException {
		NetcdfChartProvider plotter = new NetcdfChartProvider(resource, location, parameters, parameterRefersTo, header);
		
		JFreeChart jchart = plotter.getChart();

		return jchart;
	}

	public static void getData(PrintStream out, String resource, Location location, Iterable<String> parameters, ParameterReference parameterRefersTo) throws FileNotFoundException, IOException, ParseException, InvalidRangeException {
		getData(out, resource, location, parameters, parameterRefersTo, null);
	}
	
	
	public static void getData(PrintStream out, String resource, Location location, Iterable<String> parameters, ParameterReference parameterRefersTo, String header) throws FileNotFoundException, IOException, ParseException, InvalidRangeException {
		NetcdfChartProvider plotter = new NetcdfChartProvider(resource, location, parameters, parameterRefersTo, header);
		plotter.getCsv(out);
	}

	
	private static void showChart(JFreeChart jchart) {
		ChartFrame frame = new ChartFrame(jchart, new java.awt.Dimension(
				DEFAULT_WIDTH, DEFAULT_HEIGHT));
		frame.pack();
		frame.setVisible(true);		
	}
	
	private static void scenario1() throws Exception {
		String url = "http://dev-vm188.met.no/thredds/dodsC/cryoclim/indicators/myfile.nc";
		Vector<String> parameters = new Vector<String>();
		parameters.add("gsl[Comfortlessbreen]");
		parameters.add("gsl[Kuhrbreen]");
		String header = "Some header";
		showChart(getChart(url, null, parameters, ParameterReference.VARIABLE_NAME, header));
	}
	private static void scenario2() throws Exception {
		String url = "http://dev-vm188.met.no/thredds/dodsC/cryoclim/indicators/myfile.nc";
		Vector<String> parameters = new Vector<String>();
		parameters.add("gsl[1]");
		parameters.add("gsl[2]");
		String header = "AUTO";
		showChart(getChart(url, null, parameters, ParameterReference.VARIABLE_NAME, header));
	}
	private static void scenario3() throws Exception {
		String url = "http://thredds.met.no/thredds/dodsC/cryoclim/met.no/osisaf-nh-agg.dods?mean_ice_extent,ice_conc_avg[0:1:371][10][10],xc[10],yc[10],time";
		showChart(getChart(url, null, null, ParameterReference.VARIABLE_NAME, null));
	}
	private static void scenario4() throws Exception {
		String url = "http://thredds.met.no/thredds/dodsC/cryoclim/met.no/osisaf-nh-agg";
		Vector<String> parameters = new Vector<String>();
		parameters.add("numdays");
		Location location = new Location(11, 69);
		showChart(getChart(url, location, parameters, ParameterReference.VARIABLE_NAME, null));
	}
	private static void scenario5() throws Exception {
		String url = "http://thredds.met.no/thredds/dodsC/cryoclim/met.no/osisaf-nh-agg";
		Vector<String> parameters = new Vector<String>();
		parameters.add("mean_ice_extent");
		Location location = null;
		showChart(getChart(url, location, parameters, ParameterReference.VARIABLE_NAME, null));
	}

	private static void scenario6() throws Exception {
		showChart(getChart(
				"http://thredds.met.no/thredds/fileServer/cryoclim/met.no/sie-mar/sie-mar_osisaf_monthly_mean_sie_mar.nc",
				null,
				ParameterReference.VARIABLE_NAME));
	}

	private static void scenario7() throws Exception {
		showChart(getChart(
				"http://thredds.met.no/thredds/fileServer/cryoclim/met.no/sie-sep/sie-sep_osisaf_monthly_mean_sie_sep.nc",
				null,
				ParameterReference.VARIABLE_NAME));
	}
	
	
	
	public static void main(String[] args) throws Exception {

//		scenario1();
		scenario2();
//		//scenario3(); // does not work (should it?)
//		scenario4();
//		scenario5();
//		scenario6();
//		scenario7();
	}
}
