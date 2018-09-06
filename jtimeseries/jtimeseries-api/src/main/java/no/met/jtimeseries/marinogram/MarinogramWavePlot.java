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
package no.met.jtimeseries.marinogram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import no.met.jtimeseries.chart.ChartPlotter;
import no.met.jtimeseries.chart.PlotStyle;
import no.met.jtimeseries.chart.SplineStyle;
import no.met.jtimeseries.chart.TimeBase;
import no.met.jtimeseries.chart.Utility;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.filter.InListFromDateFilter;
import no.met.phenomenen.weatherapi.PhenomenonName;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;

public class MarinogramWavePlot extends MarinogramPlot {

	private boolean showWaveDirection;
	private boolean showWaveHeight;

	public MarinogramWavePlot(int width, int height, String timezone, String language) {
		super(width, height, timezone, language);
	}

	public boolean isShowWaveDirection() {
		return showWaveDirection;
	}

	public void setShowWaveDirection(boolean showWaveDirection) {
		this.showWaveDirection = showWaveDirection;
	}

	public boolean isShowWaveHeight() {
		return showWaveHeight;
	}

	public void setShowWaveHeight(boolean showWaveHeight) {
		this.showWaveHeight = showWaveHeight;
	}

	@Override
	public void addPlot(MarinogramPlot plot) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removePlot(MarinogramPlot plot) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public XYPlot getPlot() throws ParseException {
		return createPlot(timezone, showWaveDirection,
				showWaveHeight);
	}

	private XYPlot createPlot(TimeZone timezone, boolean plotWaveDirection,
			boolean plotWaveHeight) {
		ChartPlotter plotter = new ChartPlotter(language);
		// default setting
		plotter.setHeight(this.getHeight());
		plotter.setWidth(this.getWidth());
		plotter.setPlotDefaultProperties("", "");
		Color waveHeightColor = new Color(0, 105, 161);
		Color waveDirectionColor = new Color(0, 105, 161);
		// plot style
		PlotStyle.Builder waveStyleBuilder = new PlotStyle.Builder("Wave");
		PlotStyle plotStyle;
		NumberPhenomenon waveDirection = getOceanForecastDataModel()
				.getPhenomenen(PhenomenonName.WaveDirection.toString(), NumberPhenomenon.class);
		NumberPhenomenon waveHeight = getOceanForecastDataModel()
                .getPhenomenen(PhenomenonName.WaveHeight.toString(), NumberPhenomenon.class);
        if (waveHeight == null || waveDirection == null) {
            return  null;
        }
        
        double tick=(waveHeight.getMaxValue()-waveHeight.getMinValue())/2;
        tick=Math.ceil(tick);
        double lowBound = Math.floor(waveHeight.getMinValue()/(tick))*(tick);
        //The minimum scale is 0
        lowBound=lowBound<0?0:lowBound;
        lowBound=lowBound-tick/2;
        double upperBound = lowBound+tick*4;

		// reference the range axis
		NumberAxis leftNumberAxis = new NumberAxis();
		leftNumberAxis.setLabel(messages.getString("parameter.wave") + " (m)");
		leftNumberAxis.setLabelPaint(waveHeightColor);
        leftNumberAxis.setTickLabelPaint(waveHeightColor);
		leftNumberAxis.setLowerBound(lowBound);
		leftNumberAxis.setUpperBound(upperBound);
		leftNumberAxis.setTickUnit(new NumberTickUnit(tick));

		NumberAxis rightNumberAxis = new NumberAxis();
		rightNumberAxis.setLabelPaint(waveHeightColor);
        rightNumberAxis.setTickLabelPaint(waveHeightColor);
		rightNumberAxis.setLowerBound(lowBound);
		rightNumberAxis.setUpperBound(upperBound);
		rightNumberAxis.setTickUnit(new NumberTickUnit(tick));

		List<Date> shortTermTime = this.getShortTermTime(waveDirection.getTime().get(0));

		//set thte plot wave height color to be transparent if show wave height is false
		if (!plotWaveHeight) {
			waveHeightColor = new Color(0, 0, 0, 0);
		}

		// plot style
		plotStyle = waveStyleBuilder.spline(SplineStyle.HYBRID)
				.stroke(new BasicStroke(2.0f)).seriesColor(waveHeightColor)
				.numberAxis(leftNumberAxis).nonNegative(true).build();

		//Draw the wave height even if plotWaveHeight is false (but with transparent in such a case)
		//for the purpose to keep the same background grid and tick label on the y-axis 
		//no matter the wave height is shown or not
		plotter.addLineChart(TimeBase.SECOND,waveHeight, plotStyle);

		plotter.getPlot().setRangeAxis(1, rightNumberAxis);
		plotter.getPlot().setOutlineVisible(true);

		// first set domain date format and then add hour based domain grid
		// lines
		// TODO: wrap this inside the addHourBasedDomainGridLines for
		// simplicity
        
		Date minDate = shortTermTime.get(0);
		Date maxDate = shortTermTime.get(shortTermTime.size() - 1);
		plotter.setDomainRange(minDate, maxDate);
		plotter.setDomainDateFormat(timezone, "HH");
		// set domain range after (must) plot all the data
		plotter.addHourBasedDomainGridLines();
		// invisible domain axis
		plotter.getPlot().getDomainAxis().setTickLabelsVisible(false);
		// add markers
        plotter.addDomainMarkers(shortTermTime, timezone, locale);

		if (plotWaveDirection) {
			List<Date> symbolTimes = Utility.filterMinimumHourInterval(
					waveDirection.getTime(), 2,1);
			InListFromDateFilter symbolTimesFilter = new InListFromDateFilter(symbolTimes);
			waveDirection.filter(symbolTimesFilter);
			waveHeight = null;
			if (plotWaveHeight) {
				waveHeight = getOceanForecastDataModel()
                        .getPhenomenen(PhenomenonName.WaveHeight.toString(), NumberPhenomenon.class);
				waveHeight.filter(symbolTimesFilter);
			}

			plotStyle = waveStyleBuilder.seriesColor(waveDirectionColor).build();
			plotter.addArrowDirectionPlot(waveDirection, waveHeight, 0.1, plotStyle);
		}
		plotter.getPlot().setRangeZeroBaselineVisible(false);
        
		return plotter.getPlot();

	}
}
