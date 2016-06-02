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
public class MarinogramWindPlot extends MarinogramPlot {

    private boolean showWindDirection;
    private boolean showWindSpeed;

    public MarinogramWindPlot(int width, int height, String timezone, String language) {
        super(width, height, timezone, language);
    }

    public boolean isShowWindDirection() {
        return showWindDirection;
    }

    public void setShowWindDirection(boolean showWindDirection) {
        this.showWindDirection = showWindDirection;
    }

    public boolean isShowWindSpeed() {
        return showWindSpeed;
    }

    public void setShowWindSpeed(boolean showWindSpeed) {
        this.showWindSpeed = showWindSpeed;
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
        return createPlot(timezone, showWindDirection, showWindSpeed);
    }

    private XYPlot createPlot(TimeZone timezone, boolean plotWindDirection, boolean plotWindSpeed) {
        ChartPlotter plotter = new ChartPlotter();
        // default setting
        plotter.setHeight(this.getHeight());
        plotter.setWidth(this.getWidth());
        plotter.setPlotDefaultProperties("", "");
        Color windSpeedColor = new Color(0, 0, 0);
        Color windDirectionColor = new Color(0, 0, 0);
        // plot style
        PlotStyle.Builder currentStyleBuilder = new PlotStyle.Builder("Wind");
        PlotStyle plotStyle;
        NumberPhenomenon windDirection = getLocationForecastDataModel().getPhenomenen(
                PhenomenonName.WindDirectionDegree.toString(), NumberPhenomenon.class);
        NumberPhenomenon windSpeed = getLocationForecastDataModel().getPhenomenen(
                PhenomenonName.WindSpeedMPS.toString(), NumberPhenomenon.class);
        if (windSpeed == null || windDirection == null) {
            return null;
        }
        
        double tick=(windSpeed.getMaxValue()-windSpeed.getMinValue())/3;
        tick=Math.ceil(tick);
        double lowBound = Math.floor(windSpeed.getMinValue()/(tick))*(tick);
        //The minimum scale is 0
        lowBound=lowBound<0?0:lowBound;
        lowBound=lowBound-tick/2;
        double upperBound = lowBound+tick*6;

        // reference the range axis
        NumberAxis leftNumberAxis = new NumberAxis();
        leftNumberAxis.setLabel(messages.getString("parameter.wind") + " (m/s)");
        leftNumberAxis.setLabelPaint(windSpeedColor);
        leftNumberAxis.setTickLabelPaint(windSpeedColor);
        //int tickUnit = (int) Math.ceil((upperBound - lowBound) / 6);
        leftNumberAxis.setTickUnit(new NumberTickUnit(tick));
        leftNumberAxis.setLowerBound(lowBound);
        leftNumberAxis.setUpperBound(upperBound);

        NumberAxis rightNumberAxis = new NumberAxis();
        rightNumberAxis.setLabel(messages.getString("label.knots"));
        rightNumberAxis.setLabelPaint(windSpeedColor);
        rightNumberAxis.setTickLabelPaint(windSpeedColor);
        lowBound = lowBound / KNOT;
        upperBound = upperBound / KNOT;
        rightNumberAxis.setLowerBound(lowBound);
        rightNumberAxis.setUpperBound(upperBound);
        rightNumberAxis.setTickUnit(new NumberTickUnit(tick / KNOT));
        NumberFormat formatter = new DecimalFormat("#0.0");
        rightNumberAxis.setNumberFormatOverride(formatter);

        List<Date> shortTermTimeList = this.getShortTermTime(windDirection.getTime().get(0));

        // set thte plot current speed color to be transparent if show current
        // speed is false
        if (!plotWindSpeed) {
            windSpeedColor = new Color(0, 0, 0, 0);
        }

        // plot style
        plotStyle = currentStyleBuilder.spline(SplineStyle.HYBRID).stroke(new BasicStroke(2.0f))
                .seriesColor(windSpeedColor).numberAxis(leftNumberAxis).nonNegative(true).build();

        // Draw the current direction even if plotCurrentSpeed is false (but
        // with transparent in such a case)
        // for the purpose to keep the same background grid and tick label on
        // the y-axis
        // no matter the wave height is shown or not
        plotter.addLineChart(TimeBase.SECOND, windSpeed, plotStyle);

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

        if (plotWindDirection) {
            List<Date> symbolTimes = Utility.filterMinimumHourInterval(windDirection.getTime(), 2, 1);
            InListFromDateFilter symbolTimesFilter = new InListFromDateFilter(symbolTimes);
            windDirection.filter(symbolTimesFilter);
            windSpeed = null;
            if (plotWindSpeed) {
                windSpeed = getLocationForecastDataModel().getPhenomenen(PhenomenonName.WindSpeedMPS.toString(),
                        NumberPhenomenon.class);
                windSpeed.filter(symbolTimesFilter);
                windSpeed = windSpeed.scaling(1 / KNOT);
            }

            plotStyle = currentStyleBuilder.seriesColor(windDirectionColor).build();
            // when plot wind direction, the arrow should be rotated 180 degree
            windDirection = windDirection.transform(180);
            plotter.addArrowDirectionPlot(windDirection, windSpeed, 2, plotStyle);
            // transform back after plot
            windDirection = windDirection.transform(180);
        }
        plotter.getPlot().setRangeZeroBaselineVisible(false);

        return plotter.getPlot();

    }
}
