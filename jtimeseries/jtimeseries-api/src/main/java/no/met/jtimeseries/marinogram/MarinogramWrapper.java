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
import java.awt.GradientPaint;
import java.awt.Paint;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import no.met.halo.common.LogUtils;
import no.met.jtimeseries.ChartFrame;
import no.met.jtimeseries.Location;
import no.met.jtimeseries.MeteogramWrapper;
import no.met.jtimeseries.chart.ChartPlotter;
import no.met.jtimeseries.chart.ChartPlottingInfo;
import no.met.jtimeseries.chart.Symbols;
import no.met.jtimeseries.chart.TimePeriod;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.jtimeseries.parser.*;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ExtendedDateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.StackedXYPlot;
import org.jfree.chart.plot.XYPlot;

public class MarinogramWrapper extends MarinogramPlot {

    private static final Logger logger = Logger.getLogger(MarinogramWrapper.class.getSimpleName());
    public static final int DEFAULT_HEIGHT = 150;
    public static final int DEFAULT_WIDTH = 800;
    private int totalPlotHeight;
    private List<MarinogramPlot> plots;
    private StackedXYPlot combiPlot;
    private static String language;

    public MarinogramWrapper(int width, int height, String timezone, String language) {
        super(width, height, timezone, language);
        totalPlotHeight = 0;
        MarinogramWrapper.language = language;
        this.plots = new ArrayList<MarinogramPlot>();
    }

    public MarinogramWrapper(int width, String language) {
        super(width, language);
        totalPlotHeight = 0;
        MarinogramWrapper.language = language;
        this.plots = new ArrayList<MarinogramPlot>();
    }

    public int getTotalPlotHeight() {
        return totalPlotHeight;
    }

    public void setTotalPlotHeight(int totalPlotHeight) {
        this.totalPlotHeight = totalPlotHeight;
    }

    @Override
    public void addPlot(MarinogramPlot plot) {
        this.plots.add(plot);
    }

    @Override
    public void removePlot(MarinogramPlot plot) {
        this.plots.remove(plot);
    }

    @Override
    public XYPlot getPlot() throws ParseException {
        //extra check to avoid add plots on multiple getPlot calls
        if (combiPlot != null) {
            return combiPlot;
        }
        for (int i = plots.size() - 1; i >= 0; i--) {
            MarinogramPlot marinogramPlot = plots.get(i);
            XYPlot plot = marinogramPlot.getPlot();

            if (combiPlot == null) {
                // create a stacked plot with the domain axis of the first plot
                combiPlot = new StackedXYPlot(plot.getDomainAxis());
                combiPlot.setGap(0.0d);
            }
            if (plot != null) {
                ExtendedDateAxis xAxis = (ExtendedDateAxis) plot.getDomainAxis();
                xAxis.setAxislineExtended(true);

                if (i == 0) {
                    xAxis.setTickLabelsVisible(true);
                }
                combiPlot.add(plot, marinogramPlot.getHeight());
            }
        }

        return combiPlot;
    }

    private static Date getStartDate() {

    	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	cal.setTime(new Date());
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);

    	int startHour = 3;
    	int offset = (startHour - (cal.get(Calendar.HOUR) % startHour));
    	cal.add(Calendar.HOUR, offset);

    	System.out.println(cal.getTime() + " - " + offset);

    	return cal.getTime();
    }

    public static GenericDataModel getModel(String resource, TimePeriod timePeriod) throws ParseException, IOException {

        GenericDataModel model = new GenericDataModel();
        OceanForecastJsonParseScheme oceanForecastParser = new OceanForecastJsonParseScheme(timePeriod);
        oceanForecastParser.setModel(model);

        ForecastParser forecastParser = new ForecastParser(oceanForecastParser, resource);
        return forecastParser.populateModelWithData();
    }


    public JFreeChart createMarinogram(ChartPlottingInfo cpi) {
        JFreeChart jchart = null;
        if (!cpi.isShowAirTemperature() && !cpi.isShowWaterTemperature() && !cpi.isShowPressure()
                && !cpi.isShowWaveDirection() && !cpi.isShowWaveHeight()
                && !cpi.isShowCurrentDirection() && !cpi.isShowCurrentSpeed()
                && !cpi.isShowWindDirection() && !cpi.isShowWindSpeed() && !cpi.isShowDewpointTemperature()) {
            jchart = createEmptyChart(cpi);
            return jchart;
        }
        try {
        	TimePeriod timePeriod = new TimePeriod(new Date(), MeteogramWrapper.SHORT_TERM_HOURS).adapt(3);

            // parse oceanforecast data from api.met.no
            Location location = new Location(cpi.getLongitude(),
                    cpi.getLatitude());
            // The curve will not be shown if too many data points when setDrawSeriesLineAsPath(true) in render;
            OceanForecastParseScheme oceanForecastParseScheme = new OceanForecastParseScheme(timePeriod);
            ForecastParser forecastParser = new ForecastParser(
                    oceanForecastParseScheme, OceanForecastAddressFactory.getURL(location).toString());
            // parse locationforcast data from api.met.no
            //MarinogramAPIHandler api = new MarinogramAPIHandler();
            //api.fetchAsJson(OceanForecastAddressFactory.getURL(location));
            GenericDataModel locationForecastDataModel = null;
            GenericDataModel model = null;

            if (cpi.isShowAirTemperature() || cpi.isShowWaterTemperature() || cpi.isShowDewpointTemperature()) {
                MarinogramTemperaturePlot mp = new MarinogramTemperaturePlot(
                        cpi.getWidth(), cpi.getWidth() / 4, cpi.getTimezone(), cpi.getLanguage());
                totalPlotHeight += cpi.getWidth() / 4;
                mp.setDescription("Temperature Plot");
                mp.setShowAirTemp(cpi.isShowAirTemperature());
                mp.setShowSeaTemp(cpi.isShowWaterTemperature());
                mp.setShowDewTemp(cpi.isShowDewpointTemperature());
                locationForecastDataModel = MeteogramWrapper.getModel(location, timePeriod);
                mp.setLocationForecastDataModel(locationForecastDataModel);
                model = this.getModel(OceanForecastAddressFactory.getURL(location).toString(), timePeriod);
                mp.setOceanForecastDataModel(model);
                this.addPlot(mp);

            }

            if (cpi.isShowCurrentDirection() || cpi.isShowCurrentSpeed()) {
                MarinogramCurrentPlot mp = new MarinogramCurrentPlot(cpi.getWidth(),
                        cpi.getWidth() / 7, cpi.getTimezone(), cpi.getLanguage());
                totalPlotHeight += cpi.getWidth() / 7;
                mp.setDescription("Current Plot");
                mp.setShowCurrentSpeed(cpi.isShowCurrentSpeed());
                mp.setShowCurrentDirection(cpi.isShowCurrentDirection());

                // extra check if model has no data
                if (model == null) {
                    model = this.getModel(OceanForecastAddressFactory.getURL(location).toString(), timePeriod);
                }
                mp.setOceanForecastDataModel(model);
                this.addPlot(mp);

            }

            if (cpi.isShowWaveDirection() || cpi.isShowWaveHeight()) {
                MarinogramWavePlot mp = new MarinogramWavePlot(cpi.getWidth(),
                        cpi.getWidth() / 7, cpi.getTimezone(), cpi.getLanguage());
                totalPlotHeight += cpi.getWidth() / 7;
                mp.setDescription("Wave Plot");
                mp.setShowWaveHeight(cpi.isShowWaveHeight());
                mp.setShowWaveDirection(cpi.isShowWaveDirection());

                // extra check if model has no data
                if (model == null) {
                    model = this.getModel(OceanForecastAddressFactory.getURL(location).toString(), timePeriod);
                }
                mp.setOceanForecastDataModel(model);
                this.addPlot(mp);

            }

            if (cpi.isShowPressure()) {
                MarinogramPressurePlot pressurePlot = new MarinogramPressurePlot(
                        cpi.getWidth(), cpi.getWidth() / 7, cpi.getTimezone(), cpi.getLanguage());
                totalPlotHeight += cpi.getWidth() / 7;
                pressurePlot.setShowPressure(cpi.isShowPressure());
                // extra check if locationForecastDataModel has no data
                if (locationForecastDataModel == null) {
                    locationForecastDataModel = MeteogramWrapper.getModel(location, timePeriod);
                }
                pressurePlot.setLocationForecastDataModel(locationForecastDataModel);
                this.addPlot(pressurePlot);
            }

            if (cpi.isShowWindDirection() || cpi.isShowWindSpeed()) {
                MarinogramWindPlot mp = new MarinogramWindPlot(cpi.getWidth(),
                        cpi.getWidth() / 7, cpi.getTimezone(), cpi.getLanguage());
                totalPlotHeight += cpi.getWidth() / 7;
                mp.setDescription("Wind Plot");
                mp.setShowWindSpeed(cpi.isShowWindSpeed());
                mp.setShowWindDirection(cpi.isShowWindDirection());

                // extra check if model has no data
                if (model == null) {
                    model = this.getModel(OceanForecastAddressFactory.getURL(location).toString(), timePeriod);
                }
                locationForecastDataModel = MeteogramWrapper.getModel(location, timePeriod);
                mp.setLocationForecastDataModel(locationForecastDataModel);
                mp.setOceanForecastDataModel(model);
                this.addPlot(mp);

            }

            jchart = createJFreeChart("", this.getPlot(), this.getWidth());

        } catch (Exception e) {
            LogUtils.logException(logger, "Failure during marinogram generation with "+cpi.toString(), e);
            XYPlot plot = new XYPlot();
            plot.setBackgroundPaint(null);
            plot.setBackgroundImage(Symbols.getImage("/error.png"));
            jchart = createJFreeChart("", plot, this.getWidth());
        }
        return jchart;
    }

    private static JFreeChart createJFreeChart(String title, Plot plot, int width) {
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBorderVisible(false);
        Paint paint = new GradientPaint(0, 0, Color.WHITE, width,
                0, Color.WHITE);
        chart.setBackgroundPaint(paint);
        return chart;
    }

    public static JFreeChart createEmptyChart(ChartPlottingInfo cpi) {
        ChartPlotter cp = new ChartPlotter(language);
        cp.setHeight(cpi.getHeight());
        cp.setWidth(cpi.getWidth());
        cp.setPlotDefaultProperties("", "");
        ValueAxis dateAxis=new DateAxis();
        dateAxis.setTickLabelsVisible(false);
        cp.setDomainAxis(dateAxis);
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setRange(0, 10);
        numberAxis.setTickUnit(new NumberTickUnit(1.0d));
        numberAxis.setTickLabelsVisible(false);
        cp.setRangeAxis(numberAxis);
        return createJFreeChart("", cp.getPlot(), cpi.getWidth());
    }

    public static void main(String args[]) {
        MarinogramTemperaturePlot mp = new MarinogramTemperaturePlot(800, 200, "UTC", "en");
        mp.setDescription("Temperature");

        MarinogramTemperaturePlot mp1 = new MarinogramTemperaturePlot(800, 200, "UTC", "en");
        mp1.setDescription("Temperature1");

        MarinogramPlot marinogram = new MarinogramWrapper(900, 200, "UTC", "en");
        marinogram.setDescription("marinogram");

        MarinogramWrapper marinogram1 = new MarinogramWrapper(940, 200, "UTC", "en");
        //
        // 30.95, 71.5
        // 58.9653, 5.7180
        //41.8947&longitude=12.4839
        //41.0138
        //5.04092, 58.89468
        //16.66, 68.56
        //10.72938, 71.50000
        ChartPlottingInfo cpi = new ChartPlottingInfo.Builder(5.32905, 74.39825)
                .width(mp1.getWidth()).showAirTemperature(true)
                .showWaterTemperature(true).showDewpointTemperature(true).showPressure(true)
                .showWaveDirection(true).showWaveHeight(true)
                .showCurrentDirection(true).showCurrentSpeed(true)
                .showWindDirection(true).showWindSpeed(true).timezone("UTC").language("en").build();
        JFreeChart jchart = marinogram1.createMarinogram(cpi);
        jchart.setBorderVisible(false);
        Paint paint = new GradientPaint(0, 0, Color.WHITE,
                marinogram1.getWidth(), 0, Color.WHITE);
        jchart.setBackgroundPaint(paint);
        jchart.removeLegend();
        ChartFrame frame = new ChartFrame(jchart, new java.awt.Dimension(900,
                400));
        frame.pack();
        frame.setVisible(true);
    }
}
