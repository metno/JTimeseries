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

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import no.met.jtimeseries.chart.XYSplineRenderer;
import no.met.jtimeseries.data.item.NumberValueItem;
import no.met.phenomenen.NumberPhenomenon;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;



/**
 * Creates simple plots, consisting of xy spline plots only
 */
public class SimplePlotProvider implements PlotProvider {

	private static final Color[] colors = {Color.red, Color.blue, Color.green, Color.orange, Color.yellow, Color.pink};

//	@Override
//	public XYPlot getPlot(List<NumberPhenomenon> dataList) {
//		
//		XYPlot plot = new XYPlot();
//		
//		plot.setDomainAxis(getDomainAxis(dataList));
//
//		int idx = 0;
//		for ( ValueAxis valueAxis : getRangeAxis(dataList)) {
//			System.out.println(valueAxis.getLowerBound() + " - " + valueAxis.getUpperBound());
//			plot.setRangeAxis(idx ++, valueAxis);
//		}
//
//		for ( int i = 0; i < dataList.size(); i ++ ) 
//			addTimeSeries(plot, dataList.get(i), i, colors[i % colors.length]);
//
//		return plot;
//	}
	
	@Override
	public XYPlot getPlot(List<NumberPhenomenon> dataList) {
		
		XYPlot plot = new XYPlot();
		
		plot.setDomainAxis(getDomainAxis(dataList));
		
		Map<String, Integer> axes = createRangeAxes(dataList, plot);

		for ( int i = 0; i < dataList.size(); i ++ ) {
			NumberPhenomenon phenomenon = dataList.get(i);
			//addTimeSeries(plot, phenomenon, i, colors[i % colors.length]);

			plot.setDataset(i, getTimeSeries(phenomenon));
			plot.setRenderer(i, new XYSplineRenderer());
			plot.getRenderer(i).setSeriesPaint(0, colors[i % colors.length]);

			plot.mapDatasetToRangeAxis(i, axes.get(phenomenon.getPhenomenonUnit()));
		}
		
		return plot;
	}


	
	/**
	 * Create a domain axis for the data.
	 * 
	 * @param dataList List to create axis from
	 * @return An axis for plotting, with time elements
	 */
	private DateAxis getDomainAxis(List<NumberPhenomenon> dataList) {
		DateAxis domainAxis = new DateAxis();
		domainAxis.setRange(dataList.get(0).getStartTime(), dataList.get(0).getEndTime());
		return domainAxis;
	}
	
	
	/**
	 * Create range axes for all values to be used. 
	 * 
	 * @param dataList List to create axes from
	 * @param plot The plot render data on 
	 * 
	 * @return A map pointing from unit name to axis index
	 */
	private Map<String, Integer> createRangeAxes(List<NumberPhenomenon> dataList, XYPlot plot)
	{
		HashMap<String, Integer> ret = new HashMap<String, Integer>();

		int i = 0;
		for ( ValueAxis va : getRangeAxis(dataList) ) {
			String unit = va.getLabel();
			plot.setRangeAxis(i, va);
			ret.put(unit, i ++);
		}
		
		return ret;
	}
	
	
	/**
	 * Get the list of all range axes to be used. This method will collate 
	 * axes with the same units.
	 * 
	 * @param dataList  List to create axis from
	 * @return An axis for plotting, with value elements
	 */
	private List<ValueAxis> getRangeAxis(List<NumberPhenomenon> dataList) {

		class AxisInfo {
			private final String unit;
			private double low = Double.MAX_VALUE;
			private double high = -Double.MAX_VALUE;
			
			public AxisInfo(String unit) {
				this.unit = unit;
			}
			
			public void add(NumberPhenomenon p) {
				if ( p.getMinValue() < low )
					low = p.getMinValue();
				if ( p.getMaxValue() > high )
					high = p.getMaxValue();
			}
			
			private double border() {
				if ( unit.equals("%") )
					return 0;
				return (high - low) / 10.0;
			}
			
			public String getUnit() {
				return unit;
			}
			
			public NumberAxis getAxis() {
				NumberAxis rangeAxis = new NumberAxis(getUnit());
				rangeAxis.setRange(getLowValue(), getHighValue());
				return rangeAxis;
			}
			
			private double getLowValue() {
				if ( unit.equals("%") )
					return 0;
				else if ( low == 0 )
					return 0;
				return low - border();
			}
			
			private double getHighValue() {
				if ( unit.equals("%") )
					return 100;
				return high + border();
			}
		}
		
		Vector<AxisInfo> axisInfoList = new Vector<AxisInfo>();
		
		for ( NumberPhenomenon p : dataList ) {
			
			AxisInfo axisInfo = null;
			for ( AxisInfo ai : axisInfoList ) 
				if ( ai.getUnit().equals(p.getPhenomenonUnit()) ) {
					axisInfo = ai;
					break;
				}
			if ( axisInfo == null ) {
				axisInfo = new AxisInfo(p.getPhenomenonUnit());
				axisInfoList.add(axisInfo);
			}
			axisInfo.add(p);
		}

		Vector<ValueAxis> ret = new Vector<ValueAxis>();
		for ( AxisInfo axisInfo : axisInfoList ) {
			ret.add(axisInfo.getAxis());
		}
		
		return ret;
	}
	

	/**
	 * Add a single {@link GenericNumberPhenomenon} with the given color to the plot.
	 */
	private void addTimeSeries(XYPlot plot, NumberPhenomenon toAdd, int dataSetCount, Color color) {
		
		plot.setDataset(dataSetCount, getTimeSeries(toAdd));
		plot.setRenderer(dataSetCount, new XYSplineRenderer());
		plot.getRenderer(dataSetCount).setSeriesPaint(0, color);
	}
	
	
	/**
	 * Convert {@link GenericNumberPhenomenon} to a {@link TimeSeriesCollection} 
	 * @param data
	 * @return
	 */
	private TimeSeriesCollection getTimeSeries(NumberPhenomenon data) {
		TimeSeries timeSeries = new TimeSeries(data.getPhenomenonName());
		for ( NumberValueItem item : data )
			timeSeries.add(new Minute(item.getTimeFrom()), item.getValue());
		TimeSeriesCollection dataset = new TimeSeriesCollection(timeSeries);
		return dataset;
	}
}
