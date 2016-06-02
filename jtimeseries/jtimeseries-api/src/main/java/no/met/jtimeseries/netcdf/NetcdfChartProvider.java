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

import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import no.met.jtimeseries.Location;
import no.met.jtimeseries.data.item.NumberValueItem;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.jtimeseries.netcdf.plot.MultiPlotProvider;
import no.met.jtimeseries.netcdf.plot.PlotProvider;
import no.met.jtimeseries.netcdf.plot.SimplePlotProvider;
import no.met.jtimeseries.parser.NetcdfParser;
import no.met.phenomenen.NumberPhenomenon;

import org.jfree.chart.JFreeChart;

import ucar.ma2.InvalidRangeException;


/**
 * Plotter for netcdf files
 * 
 * @author vegardb
 *
 */
public class NetcdfChartProvider {

	private GenericDataModel dataModel;
	private String header;
	
	/**
	 * Create a plotter object
	 * 
	 * @param resource the file name or URL of the wanted resource
	 * @param location longitude/latitude coordinates in the data's grid. If this is null, assume the wanted data is not gridded. 
	 * @throws IOException
	 * @throws ParseException 
	 * @throws InvalidRangeException if any implied index is invalid 
	 */
	public NetcdfChartProvider(String resource, Location location, Iterable<String> parameters, ParameterReference parameterRefersTo, String header) throws IOException, ParseException, InvalidRangeException {
		
		NetcdfParser parser = new NetcdfParser(location, parameters, parameterRefersTo);
		
		dataModel = parser.parse(resource);
		if ( header != null && header.equals("AUTO") )
			this.header = parser.getTitle(resource);
		else
			this.header = header;
	}

	
	/**
	 * Create a chart representing all available data
	 * 
	 * @return A chart with all parameters plotted.
	 * @throws ParseException
	 * @throws IOException
	 */
	public JFreeChart getChart() throws ParseException, IOException {
		return getChart(getVariables());
	}
	
	Vector<NumberPhenomenon> getWantedPhenomena(Iterable<String> variables) throws ParseException {
		if ( variables == null )
			variables = getVariables();

		Vector<NumberPhenomenon> data = new Vector<NumberPhenomenon>();
		for ( String variable : variables )
			data.add(getWantedPhenomenon(variable));
		
		if ( data.isEmpty() )
			throw new ParseException("Unable to find requested parameters", 0);
		
		return data;
	}
	
	NumberPhenomenon getWantedPhenomenon(String variable) throws ParseException {
		NumberPhenomenon phenomenon = dataModel.getPhenomenen(variable, NumberPhenomenon.class);
		if ( phenomenon == null )
			throw new ParseException(variable + ": No such parameter", 0);

		return phenomenon;
	}

	/**
	 * Create a chart with the given variables
	 * 
	 * @param variables Names of wanted variables
	 * @return A chart with all requested parameters plotted
	 * @throws ParseException
	 * @throws IOException
	 */
	public JFreeChart getChart(Iterable<String> variables) throws ParseException, IOException {
		
		return getChart(getWantedPhenomena(variables));
	}
	
	public void getCsv(PrintStream out) throws ParseException, IOException {
		getCsv(out, null);
	}

	
	public void getCsv(PrintStream out, Iterable<String> variables) throws ParseException, IOException {
		
		Vector<NumberPhenomenon> data = getWantedPhenomena(variables);

		// header
		out.print("# Time");
		for ( NumberPhenomenon p : data )
			out.print(",\t" + p.getPhenomenonName() + " (" + p.getPhenomenonUnit() + ")");
		out.println();
		
		TreeMap<Date, Double[]> displayData = new TreeMap<Date, Double[]>();
		for ( int i = 0; i < data.size(); i ++ ) {
			for ( NumberValueItem atom : data.get(i)) {
				Double[] d = displayData.get(atom.getTimeFrom());
				if ( d == null ) {
					d = new Double[data.size()];
					displayData.put(atom.getTimeFrom(), d);
				}
				d[i] = atom.getValue();
			}
		}

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		for ( Entry<Date, Double[]> element : displayData.entrySet() ) {
			out.print(format.format(element.getKey()));
			Double[] d = element.getValue();
			for ( int i = 0; i < d.length; i ++ )
				out.print(",\t" + d[i]);
			out.println();
		}
	}
	
	/**
	 * Get a list of all available parameters
	 * @return Names of all parameters that are available.
	 */
	private Set<String> getVariables() {
		
		return dataModel.getPhenomena();
	}

	
	/**
	 * Convert {@link Phenomenon} list to a {@link JFreeChart}
	 * 
	 * @param dataList The data to convert
	 * @return A complete chart, ready to display
	 */
	private JFreeChart getChart(List<NumberPhenomenon> dataList) {
		
		JFreeChart chart = new JFreeChart(getPlotProvider().getPlot(dataList));
		if ( header != null )
			chart.setTitle(header);
		return chart;
	}
	
	
	private PlotProvider getPlotProvider() {
		MultiPlotProvider plotProvider = new MultiPlotProvider();
		plotProvider.addPlotProvider(new SimplePlotProvider());
		return plotProvider;
	}
}
