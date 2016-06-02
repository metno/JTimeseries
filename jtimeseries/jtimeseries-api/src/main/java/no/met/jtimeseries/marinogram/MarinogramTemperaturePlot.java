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
import java.awt.Font;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import no.met.jtimeseries.chart.ChartPlotter;
import no.met.jtimeseries.chart.PlotStyle;
import no.met.jtimeseries.chart.SplineStyle;
import no.met.jtimeseries.chart.TimeBase;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.weatherapi.PhenomenonName;

import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

/**
 * A class for creating marinogram aTemperature plot.
 */
public class MarinogramTemperaturePlot extends MarinogramPlot {

    private XYPlot plot;
    // private ChartPlotter plotter;
    private boolean showSeaTemp = true;
    private boolean showAirTemp = true;
    private boolean showDewTemp = true;

    public MarinogramTemperaturePlot(int width, int height, String timezone, String language) {
        super(width, height, timezone, language);
        // plotter = new ChartPlotter();
    }

    public boolean isShowAirTemp() {
        return showAirTemp;
    }

    public void setShowAirTemp(boolean showAirTemp) {
        this.showAirTemp = showAirTemp;
    }

    public boolean isShowSeaTemp() {
        return showSeaTemp;
    }

    public void setShowSeaTemp(boolean showSeaTemp) {
        this.showSeaTemp = showSeaTemp;
    }

    public boolean isShowDewTemp() {
        return showDewTemp;
    }

    public void setShowDewTemp(boolean showDewTemp) {
        this.showDewTemp = showDewTemp;
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
        if (plot == null) {
            plot = createPlot(timezone, showAirTemp, showSeaTemp, showDewTemp);
        }
        return plot;
    }

    private XYPlot createPlot(TimeZone timezone, boolean plotAirTemp, boolean plotWaterTemp, boolean plotDewpointTemp)
            throws ParseException {
        Date startTime = null;
        NumberPhenomenon aTemperature = null;
        NumberPhenomenon wTemperature = null;
        NumberPhenomenon dTemperature = null;
        // default setting
        ChartPlotter plotter = new ChartPlotter();
        plotter.setHeight(this.getHeight());
        plotter.setWidth(this.getWidth());
        plotter.setPlotDefaultProperties("", "");

        double minValue=100;
        double maxValue=-100;
        int plotIndex=0;
        if (plotAirTemp) {
            aTemperature = getLocationForecastDataModel().getPhenomenen(PhenomenonName.AirTemperature.toString(),
                    NumberPhenomenon.class);
            minValue=aTemperature.getMinValue()<minValue?aTemperature.getMinValue():minValue;
            maxValue=aTemperature.getMaxValue()>maxValue?aTemperature.getMaxValue():maxValue;
            startTime = aTemperature.getTime().get(0);
            plotTemperature(plotter, aTemperature, new BasicStroke(2.0f), Color.RED, messages.getString("label.air"), true);
            plotter.getPlot().getRenderer(plotIndex).setSeriesVisibleInLegend(0,true);
            plotter.getPlot().getRenderer(plotIndex).setSeriesVisibleInLegend(1,true);
            plotIndex++;
        }

        if (plotWaterTemp) {
            wTemperature = getOceanForecastDataModel().getPhenomenen(PhenomenonName.seaTemperature.toString(),
                    NumberPhenomenon.class);
            // only plot water temperature if it is availbe for this location
            if (wTemperature != null) {
                minValue=wTemperature.getMinValue()<minValue?wTemperature.getMinValue():minValue;
                maxValue=wTemperature.getMaxValue()>maxValue?wTemperature.getMaxValue():maxValue;
                startTime = wTemperature.getTime().get(0);
                BasicStroke dottedStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
                        new float[] { 2.0f, 6.0f }, 0.0f);
                plotTemperature(plotter, wTemperature, dottedStroke, Color.RED, messages.getString("label.water"), true);
                plotter.getPlot().getRenderer(plotIndex++).setSeriesVisibleInLegend(0,true);
            }
        }
        
        if (plotDewpointTemp) {
            dTemperature = getLocationForecastDataModel().getPhenomenen(PhenomenonName.dewPointTemperature.toString(),
                    NumberPhenomenon.class);
            minValue=dTemperature.getMinValue()<minValue?dTemperature.getMinValue():minValue;
            maxValue=dTemperature.getMaxValue()>maxValue?dTemperature.getMaxValue():maxValue;
            startTime = dTemperature.getTime().get(0);
            plotTemperature(plotter, dTemperature, new BasicStroke(2.0f), Color.ORANGE, messages.getString("label.dewpoint"), false);
            plotter.getPlot().getRenderer(plotIndex).setSeriesVisibleInLegend(0,true);
        }

        double tick=(maxValue-minValue)/3.5;
        tick=Math.ceil(tick);
        double lowBound = Math.floor(minValue/(tick))*(tick);
        lowBound=lowBound-tick/2;
        double upperBound = lowBound+tick*7;
        
        // set range axis
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setLabelPaint(Color.RED);
        numberAxis.setTickLabelPaint(Color.RED);
        numberAxis.setLabel(messages.getString("parameter.temperature") + " (\u00B0 C)");
        numberAxis.setTickUnit(new NumberTickUnit(tick));
        numberAxis.setLowerBound(lowBound);
        numberAxis.setUpperBound(upperBound);
        
        //Set left axis and right axis
        plotter.getPlot().setRangeAxis(0, numberAxis);
        plotter.getPlot().setRangeAxis(1, numberAxis);
        //Set the third axis and hide the third axis
        if (plotAirTemp && plotWaterTemp && plotDewpointTemp) {
            NumberAxis numberAxis2 = new NumberAxis();
            numberAxis2.setTickUnit(new NumberTickUnit(tick));
            numberAxis2.setLowerBound(lowBound);
            numberAxis2.setUpperBound(upperBound);
            plotter.getPlot().setRangeAxis(2, numberAxis2);
            plotter.getPlot().getRangeAxis(2).setVisible(false);
        }
        
        //Show legend at the top right position of the plot
        LegendTitle lt = new LegendTitle(plotter.getPlot());
        lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
        lt.setBackgroundPaint(new Color(255, 255, 255, 100));
        lt.setFrame(new BlockBorder(Color.white));
        lt.setPosition(RectangleEdge.TOP);
        XYTitleAnnotation ta = new XYTitleAnnotation(0.99, 0.95, lt,RectangleAnchor.TOP_RIGHT);
        plotter.getPlot().addAnnotation(ta);

        // set domain range after (must) plot all the data
        plotter.addHourBasedDomainGridLines();
        // add markers
        plotter.addDomainMarkers(getShortTermTime(startTime), timezone, locale);
        Date minDate = getShortTermTime(startTime).get(0);
        Date maxDate = getShortTermTime(startTime).get(getShortTermTime(startTime).size() - 1);
        plotter.setDomainRange(minDate, maxDate);
        plotter.setDomainDateFormat(timezone, "HH");
        plotter.getPlot().setOutlineVisible(true);
        // invisible the domain i.e, x axis
        plotter.getPlot().getDomainAxis().setTickLabelsVisible(false);

        return plotter.getPlot();

    }

    private void plotTemperature(ChartPlotter plotter, NumberPhenomenon temperature, BasicStroke stroke, Color color, String label,
            boolean isThresholdLine) {
        // number axis to be used for wind speed plot
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setLabelPaint(color);
        numberAxis.setTickLabelPaint(color);
        numberAxis.setLabel(messages.getString("parameter.temperature") + " (\u00B0 C)");
        PlotStyle plotStyle = new PlotStyle.Builder(label).ticks(6).stroke(stroke).seriesColor(color)
                .plusDegreeColor(color).labelColor(new Color(240, 28, 28)).spline(SplineStyle.HYBRID).numberAxis(numberAxis).build();
        
        if (isThresholdLine)
            plotter.addThresholdLineChart(TimeBase.SECOND, temperature, plotStyle);
        else
            plotter.addLineChart(TimeBase.SECOND, temperature, plotStyle);
    }
}
