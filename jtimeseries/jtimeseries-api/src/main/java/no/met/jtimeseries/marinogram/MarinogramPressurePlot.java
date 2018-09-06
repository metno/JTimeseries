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
import no.met.phenomenen.weatherapi.PhenomenonName;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;

public class MarinogramPressurePlot extends MarinogramPlot {

    private boolean showPressure;

    public MarinogramPressurePlot(int width, int height, String timezone, String language) {
        super(width, height,timezone,language);
    }

    public boolean isShowPressure() {
        return showPressure;
    }

    public void setShowPressure(boolean showPressure) {
        this.showPressure = showPressure;
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
        return createPlot(timezone, showPressure);
    }

    private XYPlot createPlot(TimeZone timezone, boolean plotPressure) {
        ChartPlotter plotter = null;
        if (plotPressure) {
            plotter = new ChartPlotter(language);
            // default setting
            plotter.setHeight(this.getHeight());
            plotter.setWidth(this.getWidth());
            plotter.setPlotDefaultProperties("", "");
            NumberPhenomenon pressure = getLocationForecastDataModel()
                    .getPhenomenen(PhenomenonName.Pressure.toString(), NumberPhenomenon.class);
            List<Date> shortTermTime = pressure.getTime();
            Color pressureColor = new Color(11, 164, 42);
            
            PlotStyle.Builder pressureStyleBuilder = new PlotStyle.Builder(messages.getString("parameter.pressure") + " (hPa)");
            PlotStyle plotStyle = pressureStyleBuilder.spline(SplineStyle.HYBRID).ticks(4).difference(50.0d).
                    seriesColor(pressureColor).labelColor(pressureColor).build();
            plotter.addLineChart(TimeBase.SECOND, pressure, plotStyle);
            //plotter.formalizeRangeAxisWithMargins(plotter.getRangeAxisIndex() - 1, 950, 1050);
            
            double tick=(pressure.getMaxValue()-pressure.getMinValue())/2;
            tick=Math.ceil(tick/10)*10;
            double lowBound = Math.floor(pressure.getMinValue()/(tick))*(tick);
            lowBound=lowBound-tick/2;
            double upperBound = lowBound+tick*4;
            
            //replicate the range axis 
            NumberAxis referenceAxis = (NumberAxis) plotter.getPlot().getRangeAxis();
            referenceAxis.setTickUnit(new NumberTickUnit(tick));
            referenceAxis.setLowerBound(lowBound);
            referenceAxis.setUpperBound(upperBound);
            NumberAxis numberAxis = new NumberAxis();
            numberAxis.setLabelPaint(pressureColor);
            numberAxis.setTickLabelPaint(referenceAxis.getTickLabelPaint());
            //numberAxis.setLowerMargin(ChartPlotter.LOWER_PLOT_MARGIN);
            numberAxis.setRange(referenceAxis.getLowerBound(), referenceAxis.getUpperBound());
            numberAxis.setTickUnit(referenceAxis.getTickUnit());
            //numberAxis.setRangeWithMargins(950, 1050);
            plotter.getPlot().setRangeAxis(1, numberAxis);
            
            //first set domain date format and then add hour based domain grid lines
            //TODO: wrap this inside the addHourBasedDomainGridLines for simplicity
            Date minDate = shortTermTime.get(0);
            Date maxDate = shortTermTime.get(shortTermTime.size() >= 48 ? 48 : shortTermTime.size() - 1);
            plotter.setDomainRange(minDate, maxDate);
            plotter.setDomainDateFormat(timezone, "HH");
            plotter.getPlot().setOutlineVisible(true);
            //set domain range after (must) plot all the data
            plotter.addHourBasedDomainGridLines();
            //invisible domain axis
            plotter.getPlot().getDomainAxis().setTickLabelsVisible(false);
            // add markers
            plotter.addDomainMarkers(shortTermTime, timezone, locale);

        }


        return plotter.getPlot();

    }
}
