/** *****************************************************************************
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
 ****************************************************************************** */
package no.met.jtimeseries;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Logger;

import no.met.jtimeseries.chart.ChartPlotter;
import no.met.jtimeseries.chart.ChartPlottingInfo;
import no.met.jtimeseries.chart.PlotStyle;
import no.met.jtimeseries.chart.SplineStyle;
import no.met.jtimeseries.chart.TimeBase;
import no.met.jtimeseries.chart.TimePeriod;
import no.met.jtimeseries.chart.Utility;
import no.met.jtimeseries.data.item.AbstractValueItem;
import no.met.jtimeseries.data.item.NumberValueItem;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.jtimeseries.marinogram.MarinogramPlot;
import no.met.jtimeseries.marinogram.MarinogramWrapper;
import no.met.jtimeseries.parser.ForecastParser;
import no.met.jtimeseries.parser.LocationForecastAddressFactory;
import no.met.jtimeseries.parser.LocationForecastParseScheme;
import no.met.phenomenen.AbstractPhenomenon;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.SymbolPhenomenon;
import no.met.phenomenen.filter.AfterDateFilter;
import no.met.phenomenen.filter.BeforeDateFilter;
import no.met.phenomenen.filter.EveryNthItemFilter;
import no.met.phenomenen.filter.InListFromDateFilter;
import no.met.phenomenen.filter.IndexLessFilter;
import no.met.phenomenen.filter.LessOrEqualNumberFilter;
import no.met.phenomenen.filter.OverlappingTimeFilter;
import no.met.phenomenen.weatherapi.PhenomenonName;
import no.met.halo.common.LogUtils;

import org.apache.commons.lang.time.DateUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.ui.Layer;
import org.jfree.ui.TextAnchor;

/**
 * A wrapper class for creating meteogram
 */
public class MeteogramWrapper {

    private static final Logger logger = Logger.getLogger(MeteogramWrapper.class.getSimpleName());
    // values according to current halo (full width) size
    public static final int DEFAULT_HEIGHT = 300;
    public static final int DEFAULT_WIDTH = 750;
    public static final int SHORT_TERM_HOURS = 48;
    public static final int LONG_TERM_HOURS = 228;
    public static final int BACKGROUND_LINES = 10;

    private ResourceBundle messages;

    private Locale locale;

    public MeteogramWrapper(String language) {

        locale = new Locale(language);
        messages = ResourceBundle.getBundle("messages", locale);

    }

    public static GenericDataModel getModel(String resource, TimePeriod timePeriod) throws ParseException, IOException {

        GenericDataModel model = new GenericDataModel();
        LocationForecastParseScheme locationForecastParser = new LocationForecastParseScheme(timePeriod);
        locationForecastParser.setModel(model);

        ForecastParser forecastParser = new ForecastParser(locationForecastParser, resource);
        return forecastParser.populateModelWithData();
    }

    public static GenericDataModel getModel(Reader xmlReader) throws ParseException, IOException, DocumentException {

        GenericDataModel model = new GenericDataModel();
        LocationForecastParseScheme locationForecastParser = new LocationForecastParseScheme();
        locationForecastParser.setModel(model);

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(xmlReader);

        locationForecastParser.parse(document);

        return model;
    }

    public static GenericDataModel getModel(Location location, TimePeriod timePeriod) throws ParseException, IOException {
        return getModel(LocationForecastAddressFactory.getURL(location).toString(), timePeriod);
    }

    public JFreeChart createMeteogram(ChartPlottingInfo cpi, int numHours) {

        // if all paramerter are false then do not parse just create a plot
        if (!cpi.isShowAirTemperature() && !cpi.isShowPressure() && !cpi.isShowPrecipitation()
                && !cpi.isShowAccumulatedPrecipitation() && !cpi.isShowWindSymbol() && !cpi.isShowCloudSymbol()
                && !cpi.isShowWeatherSymbol() && !cpi.isShowWindDirection() && !cpi.isShowWindSpeed()
                && !cpi.isShowDewpointTemperature()) {
            return MarinogramWrapper.createEmptyChart(cpi);
        }

        if (numHours == LONG_TERM_HOURS) {
            return createLongTermMeteogram(cpi, numHours);
        } else {
            return createShortTermMeteogram(cpi, numHours);
        }
    }

    /**
     * Get the nearest full hour where UTC hour % snapTo == 0
     *
     * @param date Date to adapt
     * @param snapTo Hour to snap to
     * @return
     */
    private static Date adapt(Date date, int snapTo) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int offset = (snapTo - (cal.get(Calendar.HOUR) % snapTo));
        cal.add(Calendar.HOUR, offset);

        return cal.getTime();
    }

    public JFreeChart createShortTermMeteogram(ChartPlottingInfo cpi, int numHours) {
        return createShortTermMeteogram(cpi, new TimePeriod(new Date(), numHours));
    }

    public JFreeChart createShortTermMeteogram(ChartPlottingInfo cpi, TimePeriod timePeriod) {

        int snapTo = 3;

        TimePeriod periodToUse = timePeriod.adapt(snapTo);

        try {
            GenericDataModel model = getModel(new Location(cpi.getLongitude(), cpi.getLatitude()), periodToUse);

            return createShortTermMeteogram(model, periodToUse, cpi);
        } catch (Exception exception) {
            LogUtils.logException(logger, exception.getMessage(), exception);
            return Utility.createErrorChart(cpi.getWidth());
        }
    }

    /**
     * Generate meteogram chart with selected parameters
     *
     * @throws ParseException if data parsing fails
     */
    public JFreeChart createShortTermMeteogram(GenericDataModel model, TimePeriod timePeriod, ChartPlottingInfo cpi) {

        JFreeChart jchart = null;

        ChartPlotter plotter = new ChartPlotter();
        // default setting
        plotter.setHeight(cpi.getHeight());
        plotter.setWidth(cpi.getWidth());
        plotter.setPlotDefaultProperties("", "");
        Date origEndTime = model.getTimeTo();
        // build accumulated precipitation model
        generateShortTermAccumulatedPrecipitationModel(model, origEndTime);

        if (cpi.isShowAirTemperature()) {
            plotTemperature(model, plotter);
        }
        if (cpi.isShowDewpointTemperature()) {
            plotDewPointTemperature(model, plotter);
        }

        //reset the label and range when both temperature show
        if (cpi.isShowAirTemperature() && cpi.isShowDewpointTemperature()) {
            resetBoundForTemperature(model, plotter);
        }

        if (cpi.isShowPressure()) {
            plotPressure(model, plotter);
        }

        SymbolPhenomenon weatherSymbols = model.getSymbolPhenomenon(PhenomenonName.WeatherSymbols
                .nameWithResolution(1));
        if (weatherSymbols == null) {
            weatherSymbols = model.getSymbolPhenomenon(PhenomenonName.WeatherSymbols.nameWithResolution(3));
            weatherSymbols.filter(new IndexLessFilter(1));
        } else {

            weatherSymbols.filter(new IndexLessFilter(1));

            // remove every second item since we only want to display every
            // second hour
            weatherSymbols.filter(new EveryNthItemFilter(2));
        }
        // we need at least three hours at the end to display a weather
        // symbol correctly so remove any item
        // that is closed than three hours to the end of the meteogram.
        Date symbolEndTime = Utility.getDateWithAddedHours(origEndTime, -3);
        weatherSymbols.filter(new AfterDateFilter(symbolEndTime));

        List<Date> symbolTimes = weatherSymbols.getTimes();

        if (cpi.isShowWeatherSymbol()) {

            if (cpi.isShowAirTemperature()) {
                NumberPhenomenon temperature = model.getNumberPhenomenon(PhenomenonName.AirTemperature.toString());
                plotter.addWeatherSymbol(weatherSymbols, temperature);
            } else {
                plotter.addWeatherSymbol(weatherSymbols, null);
            }
        }

        if (cpi.isShowWindSpeed()) {
            plotWindSpeedDirection(model, plotter, cpi.isShowWindDirection(), cpi.getWindSpeedUnit(), symbolTimes);
        }

        if (cpi.isShowWindSymbol()) {
            filterWindSymbols(model, symbolTimes);
            plotWindSymbols(model, plotter);
        }

        if (cpi.isShowPrecipitation()) {
            filterShortTermPercipiation(model);

            double maxValue = 0;
            if(cpi.isShowAccumulatedPrecipitation()){
                int timeResolution = 1;
                NumberPhenomenon pcModel = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(1));
                if (pcModel == null){
                    timeResolution = 3;
                }
                maxValue = getAccumulatedPrecipitaionMaxValue(model, timeResolution);
            }

            plotShortTermPercipitation(model, plotter, maxValue);
        }

        if (cpi.isShowAccumulatedPrecipitation()) {

            //filterShortTermAccumulatedPrecipitaiton(model);
            plotShortTermAccumulatedPrecipitation(model, plotter);

        }

        if (cpi.isShowCloudSymbol()) {
            filterCloudSymbols(model, symbolTimes);
            plotCloudSymbols(model, plotter);
        }

        plotDomainRangeAndMarkers(model, plotter, cpi, 1, timePeriod);

        // create the chart
        jchart = plotter.createOverlaidChart("");
        return jchart;

    }

    public JFreeChart createLongTermMeteogram(ChartPlottingInfo cpi, int numHours) {
        return createLongTermMeteogram(cpi, new TimePeriod(new Date(), numHours));
    }

    public JFreeChart createLongTermMeteogram(ChartPlottingInfo cpi, TimePeriod timePeriod) {

        int snapTo = 6;

        TimePeriod periodToUse = timePeriod.adapt(snapTo);

        // If we don't modify end time, the chart will be wrong!
        periodToUse = new TimePeriod(periodToUse.getStart(),
                Utility.getDateWithAddedHours(periodToUse.getEnd(), snapTo * -2));

        try {
            GenericDataModel model = getModel(new Location(cpi.getLongitude(), cpi.getLatitude()), periodToUse);
            return createLongTermMeteogram(model, periodToUse, cpi);
        } catch (Exception exception) {
            LogUtils.logException(logger, exception.getMessage(), exception);
            return Utility.createErrorChart(cpi.getWidth());
        }
    }

    public JFreeChart createLongTermMeteogram(GenericDataModel model, TimePeriod timePeriod, ChartPlottingInfo cpi) {

        ChartPlotter plotter = new ChartPlotter();
        // default setting
        plotter.setHeight(cpi.getHeight());
        plotter.setWidth(cpi.getWidth());
        plotter.setPlotDefaultProperties("", "");
        NumberPhenomenon temperature = model.getPhenomenen(PhenomenonName.AirTemperature.toString(),
                NumberPhenomenon.class);
        Date startTime = calculateStartTimeForLongTerm(temperature);
        Date origEndTime = model.getTimeTo();
        generateLongTermAccumulatedPrecipitationModel(model, startTime, origEndTime, 6);

        if (cpi.isShowAirTemperature() || cpi.isShowDewpointTemperature()) {
            filterLongTermTemperature(model, startTime);
        }

        if (cpi.isShowAirTemperature()) {
            plotTemperature(model, plotter);
        }

        if (cpi.isShowDewpointTemperature()) {
            plotDewPointTemperature(model, plotter);
        }

        //reset the label and range when both temperature show
        if (cpi.isShowAirTemperature() && cpi.isShowDewpointTemperature()) {
            resetBoundForTemperature(model, plotter);
        }

        if (cpi.isShowPressure()) {
            filterLongTermPressure(model, startTime);
            plotPressure(model, plotter);
        }

        // we start by filtering the weather symbols since they are used to
        // determine where the cloud and
        // wind symbols are plotted as well.
        List<Date> symbolTimes;
        SymbolPhenomenon weatherSymbols = model.getSymbolPhenomenon(PhenomenonName.WeatherSymbols
                .nameWithResolution(6));

        // for the symbol to display correctly it has to be at least three
        // hours
        // from the start of the meteogram left hand side.
        Date symbolStartTime = Utility.getDateWithAddedHours(startTime, 3);
        weatherSymbols.filter(new BeforeDateFilter(symbolStartTime));
        weatherSymbols.filter(new OverlappingTimeFilter());
        symbolTimes = weatherSymbols.getTimes();

        if (cpi.isShowWeatherSymbol()) {

            if (cpi.isShowAirTemperature()) {
                plotter.addWeatherSymbol(weatherSymbols, temperature);
            } else {
                plotter.addWeatherSymbol(weatherSymbols, null);
            }
        }

        if (cpi.isShowWindSpeed()) {
            filterLongTermWindSpeed(model, startTime);
            plotWindSpeedDirection(model, plotter, cpi.isShowWindDirection(), cpi.getWindSpeedUnit(), symbolTimes);
        }

        if (cpi.isShowWindSymbol()) {
            filterWindSymbols(model, symbolTimes);
            plotWindSymbols(model, plotter);
        }

        if (cpi.isShowPrecipitation()) {

            filterLongTermPercipiation(model, startTime);

            // If we have a model containing both precipitation and accumulative precipitation
            // we want the precipitation model to be based on the accumulative max value
            double maxValue = 0;
            if(cpi.isShowAccumulatedPrecipitation()) {
                maxValue = getAccumulatedPrecipitaionMaxValue(model, 6);
            }
            plotLongTermPrecipitation(model, plotter, maxValue);

        }
        if (cpi.isShowAccumulatedPrecipitation()){
            filterLongTermAccumulativePrecipitaion(model, startTime, 6);
            plotLongTermAccumulatedPrecipitation(model, plotter, 6);
        }

        if (cpi.isShowCloudSymbol()) {

            filterCloudSymbols(model, symbolTimes);
            plotCloudSymbols(model, plotter);
        }

        plotDomainRangeAndMarkers(model, plotter, cpi, 6, timePeriod);

        // create the chart
        return plotter.createOverlaidChart("");

    }

    private Double getAccumulatedPrecipitaionMaxValue(GenericDataModel model, int timeResolution){
        NumberPhenomenon pc = model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(timeResolution));
        return pc.getMaxValue();


    }

    /**
     * Reset bound when both air temperature and dew point temperature are shown
     *
     * @param model
     * @param plotter
     */
    private void resetBoundForTemperature(GenericDataModel model, ChartPlotter plotter) {
        NumberPhenomenon temperature = model.getNumberPhenomenon(PhenomenonName.AirTemperature.toString());
        NumberPhenomenon dewtemperature = model.getNumberPhenomenon(PhenomenonName.dewPointTemperature.toString());
        double minValue = temperature.getMinValue() <= dewtemperature.getMinValue() ? temperature.getMinValue() : dewtemperature.getMinValue();
        double maxValue = temperature.getMaxValue() >= dewtemperature.getMaxValue() ? temperature.getMaxValue() : dewtemperature.getMaxValue();

        NumberAxis numberAxis1 = (NumberAxis) plotter.getPlot().getRangeAxis(plotter.getPlotIndex() - 2);
        numberAxis1.setLabel(messages.getString("parameter.temperature"));
        ChartPlotter.setAxisBound(numberAxis1, maxValue, minValue, 8, BACKGROUND_LINES);

        NumberAxis numberAxis2 = (NumberAxis) plotter.getPlot().getRangeAxis(plotter.getPlotIndex() - 1);
        numberAxis2.setLabel(messages.getString("parameter.temperature"));
        numberAxis2.setUpperBound(numberAxis1.getUpperBound());
        numberAxis2.setLowerBound(numberAxis1.getLowerBound());
        numberAxis2.setTickUnit(numberAxis1.getTickUnit());
        numberAxis2.setVisible(false);

        //Add labels on the curves
        NumberValueItem airItem = (NumberValueItem) temperature.getItems().get(0);
        NumberValueItem dewpointItem = (NumberValueItem) dewtemperature.getItems().get(0);

        plotter.getPlot().getRenderer().addAnnotation(ChartPlotter.
                createTextAnnotation(messages.getString("label.air"), temperature.getStartTime().getTime(),
                        airItem.getValue() + 0.1d, TextAnchor.BOTTOM_LEFT, Color.RED), Layer.BACKGROUND);
        plotter.getPlot().getRenderer().addAnnotation(ChartPlotter.
                createTextAnnotation(messages.getString("label.dewpoint"), dewtemperature.getStartTime().getTime(),
                        dewpointItem.getValue() + 0.1d, TextAnchor.BOTTOM_LEFT, Color.ORANGE), Layer.BACKGROUND);
    }

    private void plotDomainRangeAndMarkers(GenericDataModel model, ChartPlotter plotter, ChartPlottingInfo cpi,
            int interval, TimePeriod timePeriod) {

        NumberPhenomenon temperature = model.getNumberPhenomenon(PhenomenonName.AirTemperature.toString());
        List<Date> shortTermTime = temperature.getTime();

        // set domain range after (must) plot all the data
        plotter.addDomainGridLines(interval);
        plotter.setDomainRange(timePeriod.getStart(), timePeriod.getEnd());
        plotter.setDomainDateFormat(TimeZone.getTimeZone(cpi.getTimezone()), "HH");

        // add markers
        plotter.addDomainMarkers(timePeriod.getStart(), timePeriod.getEnd(), TimeZone.getTimeZone(cpi.getTimezone()), locale);

    }

    // filter the values for percipitation before plotting short term meteogram
    private void filterShortTermPercipiation(GenericDataModel model) {

        if (hasMinMaxPrecipitation(model, 1)) {

            NumberPhenomenon pcMin = model.getNumberPhenomenon(PhenomenonName.PrecipitationMin.nameWithResolution(1));
            NumberPhenomenon pcMax = model.getNumberPhenomenon(PhenomenonName.PrecipitationMax.nameWithResolution(1));

            // remove all values where max is less or equal to zero.
            pcMax.filter(new LessOrEqualNumberFilter(0.0));

            // since if max is 0 then min is also 0, then we remove all that are
            // not in max
            pcMin.filter(new InListFromDateFilter(pcMax.getTimes()));
        } else {
            NumberPhenomenon pc = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(3));
            pc.filter(new LessOrEqualNumberFilter(0.0));
        }

    }

    /**
     * Creates a short term accumulated precipitation model based on precipitation data time series.
     * @param model
     * @param endTime
     */
    private void generateShortTermAccumulatedPrecipitationModel(GenericDataModel model, Date endTime){
        int timeResolution = 1;
        if(model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(1)) == null
                && model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(3)) == null){
            filterShortTermPercipiation(model);
            NumberPhenomenon pcModel;
            if (hasMinMaxPrecipitation(model, 1)) {
                pcModel = model.getNumberPhenomenon(PhenomenonName.PrecipitationMax.nameWithResolution(1));
            }else{
                pcModel = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(1));
                if (pcModel == null) {// does not have 1 hour precipitation, using 3 hours (locationforecast <= 1.9)
                    pcModel = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(3));
                    timeResolution = 3;
                }
            }
            generateAccumulatedPrecipitation(model, pcModel, timeResolution, endTime);
        }
    }

    private void generateLongTermAccumulatedPrecipitationModel(GenericDataModel model, Date startTime, Date endTime, int TimeResolution){
        filterLongTermPercipiation(model, startTime);
        NumberPhenomenon pcModel = model.getNumberPhenomenon(PhenomenonName.PrecipitationMax.nameWithResolution(TimeResolution));
        if (pcModel == null){
            pcModel = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(TimeResolution));
        }
        generateAccumulatedPrecipitation(model, pcModel, TimeResolution, endTime);
    }

    private void filterShortTermAccumulatedPrecipitaiton(GenericDataModel model){

        if (hasMinMaxPrecipitation(model, 1)) {
            NumberPhenomenon pcMax = model.getNumberPhenomenon(PhenomenonName.PrecipitationMax.nameWithResolution(1));
            pcMax.filter(new LessOrEqualNumberFilter(0.0));

        }else{
            NumberPhenomenon pc = model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(3));
            pc.filter(new LessOrEqualNumberFilter(0.0));
        }
    }

    private void plotShortTermAccumulatedPrecipitation(GenericDataModel model, ChartPlotter plotter){

        Color accumulatedPrecipitationColor = new Color(114, 232, 93, 180);

        TimeBase precipitationTimeBase = TimeBase.HOUR;
        NumberPhenomenon pc = model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(1));
        if (pc == null) {// does not have 1 hour precipitation, using 3 hours (locationforecast <= 1.9)
            precipitationTimeBase = TimeBase.HOUR_3;
            pc = model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(3));
        }
        if (!pc.getItems().isEmpty()) {
            pc.filter(new LessOrEqualNumberFilter(0.0)); // avoid plotting empty bars with 0 numbers
            if (!pc.getItems().isEmpty()) { // filter did not remove all values
                plotter.addAccumulatedPrecipitationBars(precipitationTimeBase, "accumulated precipitation", pc, accumulatedPrecipitationColor, pc.getMaxValue());
            }
        }
    }

    private void plotShortTermPercipitation(GenericDataModel model, ChartPlotter plotter, double accumulatedMaxValue) {

        Color maxPercipitationColor = new Color(160, 218, 232, 180);
        Color minPercipitationColor = new Color(104, 207, 232, 180);

        if (hasMinMaxPrecipitation(model, 1)) {
            NumberPhenomenon pcMin = model.getNumberPhenomenon(PhenomenonName.PrecipitationMin.nameWithResolution(1));
            NumberPhenomenon pcMax = model.getNumberPhenomenon(PhenomenonName.PrecipitationMax.nameWithResolution(1));
            if (!pcMin.getItems().isEmpty() || !pcMax.getItems().isEmpty()) {
                if(accumulatedMaxValue != 0){
                    plotter.addMaxMinPercipitationBars(TimeBase.HOUR, "precipitation", pcMax, pcMin, maxPercipitationColor,
                            minPercipitationColor, accumulatedMaxValue);
                }else{
                    plotter.addMaxMinPercipitationBars(TimeBase.HOUR, "precipitation", pcMax, pcMin, maxPercipitationColor,
                            minPercipitationColor, pcMax.getMaxValue());
                }

            }
        } else {
            TimeBase precipitationTimeBase = TimeBase.HOUR;
            NumberPhenomenon pc = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(1));
            if (pc == null) {// does not have 1 hour precipitation, using 3 hours (locationforecast <= 1.9)
                precipitationTimeBase = TimeBase.HOUR_3;
                pc = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(3));
            }
            if (!pc.getItems().isEmpty()) {
                pc.filter(new LessOrEqualNumberFilter(0.0)); // avoid plotting empty bars with 0 numbers
                if (!pc.getItems().isEmpty()) { // filter did not remove all values
                    if(accumulatedMaxValue != 0){
                        plotter.addPercipitationBars(precipitationTimeBase, "precipitation", pc, maxPercipitationColor, accumulatedMaxValue);

                    }else{
                        plotter.addPercipitationBars(precipitationTimeBase, "precipitation", pc, maxPercipitationColor, pc.getMaxValue());
                    }
                }
            }
        }

    }

    // filter the values for percipitation before plotting short term meteogram
    private void filterLongTermPercipiation(GenericDataModel model, Date startTime) {

        if (hasMinMaxPrecipitation(model, 6)) {

            NumberPhenomenon pcMin = model.getNumberPhenomenon(PhenomenonName.PrecipitationMin.nameWithResolution(6));
            NumberPhenomenon pcMax = model.getNumberPhenomenon(PhenomenonName.PrecipitationMax.nameWithResolution(6));

            pcMin.filter(new BeforeDateFilter(startTime));
            pcMax.filter(new BeforeDateFilter(startTime));

            pcMin.filter(new OverlappingTimeFilter());
            pcMax.filter(new OverlappingTimeFilter());

            // store the original end time before we filtering items we do not
            // want.
            Date originalEndTime = pcMin.getLastToTime();

            // remove all values where max is less or equal to zero.
            pcMax.filter(new LessOrEqualNumberFilter(0.0));

            // since if max is 0 then min is also 0, then we remove all that are
            // not in max
            pcMin.filter(new InListFromDateFilter(pcMax.getTimes()));

            NumberPhenomenon pc6 = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(6));
            pc6.filter(new OverlappingTimeFilter());
            pc6.filter(new BeforeDateFilter(originalEndTime));
            pc6.filter(new LessOrEqualNumberFilter(0.0));
        } else {
            NumberPhenomenon pc6 = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(6));
            pc6.filter(new BeforeDateFilter(startTime));
            pc6.filter(new LessOrEqualNumberFilter(0.0));
            pc6.filter(new OverlappingTimeFilter());
        }

    }

    private void plotLongTermPrecipitation(GenericDataModel model, ChartPlotter plotter, double maxValue) {

        Color maxPercipitationColor = new Color(160, 218, 232, 180);
        Color minPercipitationColor = new Color(104, 207, 232, 180);

        if (hasMinMaxPrecipitation(model, 6)) {

            NumberPhenomenon pcMin = model.getNumberPhenomenon(PhenomenonName.PrecipitationMin.nameWithResolution(6));
            NumberPhenomenon pcMax = model.getNumberPhenomenon(PhenomenonName.PrecipitationMax.nameWithResolution(6));
            NumberPhenomenon pc6 = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(6));

            if (!pcMin.getItems().isEmpty() || !pcMax.getItems().isEmpty()) {
                plotter.addMaxMinPercipitationBars(TimeBase.HOUR_6, "precipitation", pcMax, pcMin,
                        maxPercipitationColor, minPercipitationColor, maxOf(pcMax, pc6));
            }

            // the max/min precipitation does not cover the entire long term
            // range so need to supplement with
            // normal precipitation values.
            if (!pc6.getItems().isEmpty()) {
                if(maxValue != 0){
                    plotter.addPercipitationBars(TimeBase.HOUR_6, "precipitation", pc6, maxPercipitationColor, maxValue);
                }else{
                    plotter.addPercipitationBars(TimeBase.HOUR_6, "precipitation", pc6, maxPercipitationColor, pc6.getMaxValue());
                }
            }
        } else {

            NumberPhenomenon pc6 = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(6));
            if (!pc6.getItems().isEmpty()) {
                if(maxValue != 0){
                    plotter.addPercipitationBars(TimeBase.HOUR_6, "precipitation", pc6, maxPercipitationColor, maxValue);
                }else{
                    plotter.addPercipitationBars(TimeBase.HOUR_6, "precipitation", pc6, maxPercipitationColor, pc6.getMaxValue());
                }
            }
        }

    }

    /**
     * Generates the accumulated precipitation model based on a given precipitation model and time resolution.
     * @param model
     * @param precipitation
     * @param timeResolution
     * @param endTime
     */
    private void generateAccumulatedPrecipitation(GenericDataModel model, NumberPhenomenon precipitation, int timeResolution, Date endTime){
        NumberPhenomenon accumulatedPrecipitation = new NumberPhenomenon();
        Date startTime = precipitation.getStartTime();
        Date prevTime = startTime;

        while (startTime.before(endTime)){
            if (prevTime != startTime){
                double currentPrecipitation = precipitation.getValueByTime(startTime) != null ? precipitation.getValueByTime(startTime).doubleValue() : 0;
                if (currentPrecipitation > 0){
                    model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(timeResolution)).addValue(startTime, endTime, precipitation.getValueByTime(startTime).doubleValue() + accumulatedPrecipitation.getValueByTime(prevTime).doubleValue());
                }else{
                    model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(timeResolution)).addValue(startTime, endTime, accumulatedPrecipitation.getValueByTime(prevTime).doubleValue());
                }

            }else{
                accumulatedPrecipitation.addValue(startTime, endTime, precipitation.getValueByTime(startTime).doubleValue());
                model.addPhenomenen(PhenomenonName.AccumulativePrecipitation.nameWithResolution(timeResolution), accumulatedPrecipitation);
            }
            prevTime = startTime;
            startTime = DateUtils.addHours(startTime, timeResolution);
        }
    }

    private void plotLongTermAccumulatedPrecipitation(GenericDataModel model, ChartPlotter plotter, int timeResolution){

        Color accumulatedPrecipitationColor = new Color(114, 232, 93, 180);
        NumberPhenomenon accumulatedPrecipitation = model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(timeResolution));

        if (!accumulatedPrecipitation.getItems().isEmpty()) {
            plotter.addAccumulatedPrecipitationBars(TimeBase.HOUR_6, "accumulated precipitation", accumulatedPrecipitation, accumulatedPrecipitationColor, accumulatedPrecipitation.getMaxValue());
        }

    }

    private void filterLongTermAccumulativePrecipitaion(GenericDataModel model, Date startTime, int timeResolution){
        NumberPhenomenon pc6 = model.getNumberPhenomenon(PhenomenonName.AccumulativePrecipitation.nameWithResolution(timeResolution));
        pc6.filter(new BeforeDateFilter(startTime));
        pc6.filter(new LessOrEqualNumberFilter(0.0));
    }

    private double maxOf(NumberPhenomenon np1, NumberPhenomenon np2) {
        return Math.max(np1.getMaxValue(), np2.getMaxValue());
    }

    private boolean hasMinMaxPrecipitation(GenericDataModel model, int timeResolution) {
        return model.isExist(PhenomenonName.PrecipitationMin.nameWithResolution(timeResolution))
                && model.isExist(PhenomenonName.PrecipitationMax.nameWithResolution(timeResolution));
    }

    /**
     * @param phenomenon
     * @return The start time for the long term meteogram.
     */
    private Date calculateStartTimeForLongTerm(AbstractPhenomenon phenomenon) {

        List<Date> times = phenomenon.getTimes();
        Date startTime = null;
        for (Date time : times) {
            if (Utility.getHourOfDayUTC(time) % 6 == 0) {
                startTime = time;
                break;
            }
        }
        return startTime;
    }

    private void plotPressure(GenericDataModel model, ChartPlotter plotter) {

        NumberPhenomenon pressure = model.getNumberPhenomenon(PhenomenonName.Pressure.toString());
        Color pressureColor = new Color(11, 164, 42);
        // number axis to be used for pressure plot
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setLabelPaint(pressureColor);
        numberAxis.setTickLabelPaint(pressureColor);
        numberAxis.setLabel(messages.getString("parameter.pressure") + " (hPa)");
        double lowBound = 950;
        double upperBound = 1050;
        numberAxis.setLowerBound(lowBound);
        numberAxis.setUpperBound(upperBound);
        double tickUnit = (upperBound - lowBound) / BACKGROUND_LINES;
        numberAxis.setTickUnit(new NumberTickUnit(tickUnit));

        PlotStyle plotStyle = new PlotStyle.Builder("Pressure (hPa)").seriesColor(pressureColor)
                .plusDegreeColor(pressureColor).spline(SplineStyle.HYBRID).stroke(new BasicStroke(1.3f))
                .numberAxis(numberAxis).build();
        plotter.addThresholdLineChart(TimeBase.SECOND, pressure, plotStyle);
    }

    private void plotWindSpeedDirection(GenericDataModel model, ChartPlotter plotter, boolean showWindDirection,
            String unit, List<Date> symbolTimes) {

        // plot wind speed
        NumberPhenomenon windSpeed = model.getNumberPhenomenon(PhenomenonName.WindSpeedMPS.toString()).clone();
        Color windSpeedColor = new Color(0, 0, 0);
        // number axis to be used for wind speed plot
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setLabelPaint(windSpeedColor);
        numberAxis.setTickLabelPaint(windSpeedColor);
        if (unit.equalsIgnoreCase("ms")) {
            numberAxis.setLabel(messages.getString("parameter.wind") + " (m/s)");
        } else {
            windSpeed.scaling(1 / MarinogramPlot.KNOT);
            numberAxis.setLabel(messages.getString("parameter.wind") + " (" + messages.getString("label.knots") + ")");
            NumberFormat formatter = new DecimalFormat("#0.0");
            numberAxis.setNumberFormatOverride(formatter);
        }
        double maxValue = windSpeed.getMaxValue();
        double minValue = windSpeed.getMinValue();

        ChartPlotter.setAxisBound(numberAxis, maxValue, minValue, 8, BACKGROUND_LINES);

        PlotStyle plotStyle = new PlotStyle.Builder("Wind").seriesColor(windSpeedColor).plusDegreeColor(windSpeedColor)
                .spline(SplineStyle.HYBRID).stroke(new BasicStroke(2.0f)).numberAxis(numberAxis).nonNegative(true)
                .build();
        plotter.addLineChart(TimeBase.SECOND, windSpeed, plotStyle);

        // plot wind direction
        if (showWindDirection) {
            NumberPhenomenon windDirection = model.getNumberPhenomenon(PhenomenonName.WindDirectionDegree.toString())
                    .clone();

            InListFromDateFilter symbolTimesFilter = new InListFromDateFilter(symbolTimes);
            windDirection.filter(symbolTimesFilter);
            windSpeed.filter(symbolTimesFilter);

            // when plot wind direction, the arrow should be rotated 180 degree
            windDirection = windDirection.transform(180);
            NumberAxis numberAxisDirection = null;
            try {
                numberAxisDirection = (NumberAxis) numberAxis.clone();
            } catch (CloneNotSupportedException e) {
            }
            numberAxisDirection.setVisible(false);
            plotter.getPlot().setRangeAxis(plotter.getPlotIndex(), numberAxisDirection);
            plotter.addArrowDirectionPlot(windDirection, windSpeed, 0.08, plotStyle);
            // transform back after plot
            windDirection = windDirection.transform(180);
        }
    }

    private void plotTemperature(GenericDataModel model, ChartPlotter plotter) {

        NumberPhenomenon temperature = model.getNumberPhenomenon(PhenomenonName.AirTemperature.toString());
        Color temperatureColor = Color.RED;
        // number axis to be used for wind speed plot
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setLabelPaint(temperatureColor);
        numberAxis.setTickLabelPaint(temperatureColor);
        numberAxis.setLabel(messages.getString("parameter.airTemperature") + " (\u00B0 C)");
        double maxValue = temperature.getMaxValue();
        double minValue = temperature.getMinValue();

        ChartPlotter.setAxisBound(numberAxis, maxValue, minValue, 8, BACKGROUND_LINES);

        PlotStyle plotStyle = new PlotStyle.Builder("AirTemperature").seriesColor(temperatureColor)
                .plusDegreeColor(temperatureColor).spline(SplineStyle.HYBRID).stroke(new BasicStroke(2.0f))
                .numberAxis(numberAxis).build();

        plotter.addThresholdLineChart(TimeBase.SECOND, temperature, plotStyle);

    }

    private void plotDewPointTemperature(GenericDataModel model, ChartPlotter plotter) {

        NumberPhenomenon temperature = model.getNumberPhenomenon(PhenomenonName.dewPointTemperature.toString());
        if (temperature == null) {
            throw new NullPointerException("Missing parameter [" + messages.getString("parameter.dewPointTemperature") + "]");
        }
        Color temperatureColor = Color.ORANGE;
        // number axis to be used for wind speed plot
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setLabelPaint(temperatureColor);
        numberAxis.setTickLabelPaint(temperatureColor);
        numberAxis.setLabel(messages.getString("parameter.dewPointTemperature") + " (\u00B0 C)");
        double maxValue = temperature.getMaxValue();
        double minValue = temperature.getMinValue();

        ChartPlotter.setAxisBound(numberAxis, maxValue, minValue, 8, BACKGROUND_LINES);

        PlotStyle plotStyle = new PlotStyle.Builder("Dew point").seriesColor(temperatureColor)
                .plusDegreeColor(temperatureColor).spline(SplineStyle.HYBRID).stroke(new BasicStroke(2.0f))
                .numberAxis(numberAxis).build();

        plotter.addLineChart(TimeBase.SECOND, temperature, plotStyle);

    }

    private void filterLongTermTemperature(GenericDataModel model, Date startTime) {
        NumberPhenomenon temperature = model.getNumberPhenomenon(PhenomenonName.AirTemperature.toString());
        temperature.filter(new BeforeDateFilter(startTime));
        NumberPhenomenon dewpointTemperature = model.getNumberPhenomenon(PhenomenonName.dewPointTemperature.toString());
        if (dewpointTemperature != null) {
            dewpointTemperature.filter(new BeforeDateFilter(startTime));
        }
    }

    private void filterLongTermPressure(GenericDataModel model, Date startTime) {
        NumberPhenomenon pressure = model.getNumberPhenomenon(PhenomenonName.Pressure.toString());
        pressure.filter(new BeforeDateFilter(startTime));
    }

    private void filterLongTermWindSpeed(GenericDataModel model, Date startTime) {
        NumberPhenomenon windSpeed = model.getNumberPhenomenon(PhenomenonName.WindSpeedMPS.toString());
        windSpeed.filter(new BeforeDateFilter(startTime));
        NumberPhenomenon windDirection = model.getNumberPhenomenon(PhenomenonName.WindDirectionDegree.toString());
        windDirection.filter(new BeforeDateFilter(startTime));
    }

    /**
     * Adding Wind-symbols to the plot
     *
     * @param model datamodel to fetch the wind data from
     * @param plotter ChartPlotter to add the symbols on
     */
    private void plotWindSymbols(GenericDataModel model, ChartPlotter plotter) {
        NumberPhenomenon windDirection = model.getNumberPhenomenon(PhenomenonName.WindDirectionDegree.toString());
        NumberPhenomenon windSpeed = model.getNumberPhenomenon(PhenomenonName.WindSpeedMPS.toString());
        double vAlignMiddle = 0.5;

        plotter.addWindPlot(windDirection, windSpeed, vAlignMiddle);
    }

    private void filterWindSymbols(GenericDataModel model, List<Date> symbolTimes) {
        NumberPhenomenon windDirection = model.getNumberPhenomenon(PhenomenonName.WindDirectionDegree.toString());
        NumberPhenomenon windSpeed = model.getNumberPhenomenon(PhenomenonName.WindSpeedMPS.toString());

        InListFromDateFilter symbolTimesFilter = new InListFromDateFilter(symbolTimes);
        windDirection.filter(symbolTimesFilter);
        windSpeed.filter(symbolTimesFilter);

    }

    /**
     * Filter cloud data before plotting
     *
     * @param model datamodel to fetch the wind data from
     * @param symbolTimes used to filter symbols if not null
     */
    private void filterCloudSymbols(GenericDataModel model, List<Date> symbolTimes) {
        NumberPhenomenon highClouds = model.getNumberPhenomenon(PhenomenonName.HighCloud.toString());
        NumberPhenomenon mediumClouds = model.getNumberPhenomenon(PhenomenonName.MediumCloud.toString());
        NumberPhenomenon lowClouds = model.getNumberPhenomenon(PhenomenonName.LowCloud.toString());
        NumberPhenomenon fog = model.getNumberPhenomenon(PhenomenonName.Fog.toString());

        InListFromDateFilter symbolTimesFilter = new InListFromDateFilter(symbolTimes);
        fog.filter(symbolTimesFilter);
        highClouds.filter(symbolTimesFilter);
        mediumClouds.filter(symbolTimesFilter);
        lowClouds.filter(symbolTimesFilter);

    }

    private void plotCloudSymbols(GenericDataModel model, ChartPlotter plotter) {
        NumberPhenomenon highClouds = model.getNumberPhenomenon(PhenomenonName.HighCloud.toString());
        NumberPhenomenon mediumClouds = model.getNumberPhenomenon(PhenomenonName.MediumCloud.toString());
        NumberPhenomenon lowClouds = model.getNumberPhenomenon(PhenomenonName.LowCloud.toString());
        NumberPhenomenon fog = model.getNumberPhenomenon(PhenomenonName.Fog.toString());

        int numTimeSteps = model.getNumberPhenomenon(PhenomenonName.AirTemperature.toString()).getTimes().size();

        double vAlignMiddle = 0.5;
        plotter.addCloudPlot(fog, highClouds, mediumClouds, lowClouds, vAlignMiddle, numTimeSteps);

    }

    public static void main(String[] args) {
        JFreeChart jchart;
        try {
            ChartPlottingInfo cpi = new ChartPlottingInfo.Builder(10.48, 59.88).altitude(0).width(800).height(300)
                    .showAirTemperature(true).showDewpointTemperature(true).showPressure(true).timezone("UTC").showCloudSymbol(true)
                    .showWeatherSymbol(true).showWindSymbol(true).showPrecipitation(true).showWindSpeed(true)
                    .showWindDirection(true).windSpeedUnit("knop").build();

            MeteogramWrapper wrapper = new MeteogramWrapper("en");

            jchart = wrapper.createMeteogram(cpi, SHORT_TERM_HOURS);

            ChartFrame frame = new ChartFrame(jchart, new java.awt.Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
            frame.pack();
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Done!");
    }
}
