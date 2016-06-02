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
package no.met.jtimeseries.netcdf.plot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import no.met.phenomenen.NumberPhenomenon;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;

public class MultiPlotProvider implements PlotProvider {

	// This exists in addition to dataForProviders, in order to maintain the 
	// ordering of charts 
	private List<PlotProvider> subProviders = new Vector<PlotProvider>();
	
	// Mapping provider->parameter names 
	private HashMap<PlotProvider, List<String>> dataForProviders = new HashMap<PlotProvider, List<String>>();
	
	
	public void addPlotProvider(PlotProvider provider) {
		subProviders.add(provider);
		dataForProviders.put(provider, null);
	}
	
	
	public void addPlotProvider(PlotProvider provider, List<String> parameters) {
		subProviders.add(provider);
		dataForProviders.put(provider, parameters);
	}
	
	private List<NumberPhenomenon> extractNumberPhenomena(List<String> wantedVariables, List<NumberPhenomenon> dataList) {
		
		List<NumberPhenomenon> ret = new Vector<NumberPhenomenon>();
		for ( String variable : wantedVariables )
			for ( NumberPhenomenon phenomenen : dataList )
				if ( variable.equals(phenomenen.getPhenomenonName()) ) {
					ret.add(phenomenen);
					break;
				}
		
		// note: this will quietly ignore missing data
		
		return ret;
	}
	
	
	public XYPlot getPlot(List<NumberPhenomenon> dataList)
	{
		DateAxis domainAxis = new DateAxis("T", TimeZone.getTimeZone("UTC"), Locale.getDefault());
		domainAxis.setRange(dataList.get(0).getStartTime(), dataList.get(0).getEndTime());
		
		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(domainAxis);

		for ( PlotProvider provider : subProviders ) {
			
			List<String> variables = dataForProviders.get(provider);
			if ( variables == null)
				plot.add(provider.getPlot(dataList));
			else 
				plot.add(provider.getPlot(extractNumberPhenomena(variables, dataList)));
		}
		
		return plot;
	}

}
