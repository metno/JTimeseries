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
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

/**
 * Class to plot current in Marinogram
 * 
 */
public class MarinogramCurrentPlot extends MarinogramPlot {

	private boolean showCurrentDirection;
	private boolean showCurrentSpeed;

	public MarinogramCurrentPlot(int width, int height, String timezone, String language) {
		super(width, height, timezone, language);
	}

	public boolean isShowCurrentDirection() {
		return showCurrentDirection;
	}

	public void setShowCurrentDirection(boolean showCurrentDirection) {
		this.showCurrentDirection = showCurrentDirection;
	}

	public boolean isShowCurrentSpeed() {
		return showCurrentSpeed;
	}

	public void setShowCurrentSpeed(boolean showCurrentSpeed) {
		this.showCurrentSpeed = showCurrentSpeed;
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
		return createPlot(timezone, showCurrentDirection,
				showCurrentSpeed);
	}

	private XYPlot createPlot(TimeZone timezone, boolean plotCurrentDirection,
			boolean plotCurrentSpeed) {
		ChartPlotter plotter = new ChartPlotter(language);
		// default setting
		plotter.setHeight(this.getHeight());
		plotter.setWidth(this.getWidth());
		plotter.setPlotDefaultProperties("", "");
		Color currentSpeedColor = new Color(142, 25, 131);
		Color currentDirectionColor = new Color(142, 25, 131);
		// plot style
		PlotStyle.Builder currentStyleBuilder = new PlotStyle.Builder("Current");
		PlotStyle plotStyle;
		NumberPhenomenon currentDirection = getOceanForecastDataModel()
				.getPhenomenen(PhenomenonName.CurrentDirection.toString(), NumberPhenomenon.class);
		NumberPhenomenon currentSpeed = getOceanForecastDataModel()
                .getPhenomenen(PhenomenonName.CurrentSpeed.toString(), NumberPhenomenon.class);
        if (currentSpeed == null || currentDirection == null) {
            return  null;
        }
        currentSpeed=currentSpeed.scaling(100);
        double tick=(currentSpeed.getMaxValue()-currentSpeed.getMinValue())/2;
        tick=Math.ceil(tick/10)*10;
		double lowBound = Math.floor(currentSpeed.getMinValue()/(tick))*(tick);
		//The minimum scale is 0
		lowBound=lowBound<0?0:lowBound;
		lowBound=lowBound-tick/2;
		double upperBound = lowBound+tick*4;

		// reference the range axis
		NumberAxis leftNumberAxis = new NumberAxis();
		leftNumberAxis.setLabel(messages.getString("parameter.current") + " (cm/s)");
		leftNumberAxis.setLabelPaint(currentSpeedColor);
        leftNumberAxis.setTickLabelPaint(currentSpeedColor);
		leftNumberAxis.setLowerBound(lowBound);
		leftNumberAxis.setUpperBound(upperBound);
		leftNumberAxis.setTickUnit(new NumberTickUnit(tick));
		

		NumberAxis rightNumberAxis = new NumberAxis();
		rightNumberAxis.setLabel(messages.getString("label.knots"));
		rightNumberAxis.setLabelPaint(currentSpeedColor);
        rightNumberAxis.setTickLabelPaint(currentSpeedColor);
        lowBound=lowBound/100.0/KNOT;
        upperBound=upperBound/100.0/KNOT;
		rightNumberAxis.setLowerBound(lowBound);
		rightNumberAxis.setUpperBound(upperBound);
		rightNumberAxis.setTickUnit(new NumberTickUnit(tick/100.0/KNOT));
		NumberFormat formatter = new DecimalFormat("#0.00");
		rightNumberAxis.setNumberFormatOverride(formatter);
		
		List<Date> shortTermTimeList = this.getShortTermTime(currentDirection.getTime().get(0));

		//set thte plot current speed color to be transparent if show current speed is false
		if (!plotCurrentSpeed) {
			currentSpeedColor = new Color(0, 0, 0, 0);
		}

		// plot style
		BasicStroke dottedStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 2.0f,
                new float[] { 2.0f, 6.0f }, 0.0f);
		plotStyle = currentStyleBuilder.spline(SplineStyle.HYBRID)
				.stroke(dottedStroke).seriesColor(currentSpeedColor)
				.numberAxis(leftNumberAxis).nonNegative(true).build();

		//Draw the current direction even if plotCurrentSpeed is false (but with transparent in such a case)
		//for the purpose to keep the same background grid and tick label on the y-axis 
		//no matter the wave height is shown or not
		plotter.addLineChart(TimeBase.SECOND,currentSpeed, plotStyle);
		
		plotter.getPlot().setRangeAxis(1, rightNumberAxis);
		plotter.getPlot().setOutlineVisible(true);

		Date minDate = shortTermTimeList.get(0);
		Date maxDate = shortTermTimeList.get(shortTermTimeList.size() - 1);
		plotter.setDomainRange(minDate, maxDate);
		plotter.setDomainDateFormat(timezone, "HH");
		// set domain range after (must) plot all the data
		plotter.addHourBasedDomainGridLines();
		// invisible domain axis
		plotter.getPlot().getDomainAxis().setTickLabelsVisible(false);
		// add markers
        plotter.addDomainMarkers(shortTermTimeList, timezone, locale);
        
		if (plotCurrentDirection) {
			List<Date> symbolTimes = Utility.filterMinimumHourInterval(
					currentDirection.getTime(), 2,1);
			InListFromDateFilter symbolTimesFilter = new InListFromDateFilter(symbolTimes);
			currentDirection.filter(symbolTimesFilter);
			currentSpeed = null;
			if (plotCurrentSpeed) {
				currentSpeed = getOceanForecastDataModel()
                        .getPhenomenen(PhenomenonName.CurrentSpeed.toString(), NumberPhenomenon.class);
				currentSpeed.filter(symbolTimesFilter);
				currentSpeed=currentSpeed.scaling(1/100.0/KNOT);
			}

			plotStyle = currentStyleBuilder.seriesColor(currentDirectionColor).build();
			plotter.addArrowDirectionPlot(currentDirection, currentSpeed, 2, plotStyle);
		}
		plotter.getPlot().setRangeZeroBaselineVisible(false);
		
		return plotter.getPlot();

	}
}
