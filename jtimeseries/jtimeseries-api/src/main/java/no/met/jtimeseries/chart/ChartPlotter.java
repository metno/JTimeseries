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
package no.met.jtimeseries.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Paint;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import no.met.halo.common.LogUtils;

import no.met.jtimeseries.data.dataset.ArrowDataset;
import no.met.jtimeseries.data.dataset.CloudDataset;
import no.met.jtimeseries.data.item.SymbolValueItem;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.SymbolPhenomenon;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ExtendedDateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYArrowRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.WindDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

public class ChartPlotter {

    private static final Logger logger = Logger.getLogger(ChartPlotter.class
            .getName());

    public static final Color DOMAIN_GRID_LINE_COLOR = new Color(230, 230, 230);

    public static final Color RANGE_GRID_LINE_COLOR = new Color(230, 230, 230);

    public static final float LOWER_PLOT_MARGIN = 0.04f;

    // weight (relative size) of plots in combined plot mode
    private static final int mainPlotWeight = 10;
    private static final int windPlotWeight = 1;
    private static final int weatherSymbolPlotWeight = 1;
    private static final int cloudPlotWeight = 1;

    protected ResourceBundle messages;
    private Locale locale;

    private boolean addedDomainMarkers = false;
    // The width of the chart
    private int width;
    // The height of the chart
    private int height;
    // The plot object
    private XYPlot plot;
    // The index of a object when plot the object in the plot
    private int plotIndex;
    // The rang axis index of a object when plot the object in the plot
    private int rangeAxisIndex;
    // Plot object for wind arrows
    private XYPlot windPlot = null;
    // Plot object for weather symbols (when plotted on top of diagram)
    private XYPlot weatherSymbolPlot = null;
    // Plot object for cloud symbols (when plotted on top of diagram)
    private XYPlot cloudPlot = null;

    public ChartPlotter(String language) {
        plotIndex = 0;
        rangeAxisIndex = 0;
        plot = new XYPlot();

        locale = new Locale(language);
        messages = ResourceBundle.getBundle("messages", locale);
    }

    public boolean isAddedDomainMarkers() {
        return addedDomainMarkers;
    }

    public void setAddedDomainMarkers(boolean addedDomainMarkers) {
        this.addedDomainMarkers = addedDomainMarkers;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public XYPlot getPlot() {
        return plot;
    }

    public void setPlot(XYPlot plot) {
        this.plot = plot;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getPlotIndex() {
        return plotIndex;
    }

    public void setPlotIndex(int plotIndex) {
        this.plotIndex = plotIndex;
    }

    public int getRangeAxisIndex() {
        return rangeAxisIndex;
    }

    public void setRangeAxisIndex(int rangeAxisIndex) {
        this.rangeAxisIndex = rangeAxisIndex;
    }

    /**
     * Set the range of the domain axis.
     *
     * @param minDate The minimum date value for the domain axis.
     * @param maxDate The maximum date value for the domain axis.
     */
    public void setDomainRange(Date minDate, Date maxDate) {

        ValueAxis domainAxis = plot.getDomainAxis();
        if (!(domainAxis instanceof DateAxis)) {
            throw new IllegalStateException("Domain axis was not a DateAxis");
        }

        ExtendedDateAxis dateAxis = (ExtendedDateAxis) domainAxis;
        dateAxis.setMinimumDate(minDate);
        dateAxis.setMaximumDate(maxDate);

        // if this was not set the first tick would not be calculated correctly for the long
        // term meteogram, but it would be one hour of.
        dateAxis.setStartDate(minDate);
    }

    /**
     * Set the timezone and format of x-axis
     *
     * @param timezone The timezone
     * @param format The time format
     */
    public void setDomainDateFormat(TimeZone timezone, String format) {
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(timezone);
        dateAxis.setDateFormatOverride(dateFormat);
    }

    /**
     * Create a overlaind chart
     *
     * @param chartTitle The title of the chart
     * @return The JfreeChart object
     */
    public JFreeChart createOverlaidChart(String chartTitle) {
        logger.info("Creating chart " + chartTitle);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        // return a new chart containing the overlaid plot...

        Plot chartPlot = createOverlaidPlot();
        /*
		 * // make combined plot if sub-plots are defined if (windPlot != null
		 * || weatherSymbolPlot != null || cloudPlot != null) { StackedXYPlot
		 * combiPlot = new StackedXYPlot(plot.getDomainAxis()); if (cloudPlot !=
		 * null) { combiPlot.add(cloudPlot, cloudPlotWeight); } if
		 * (weatherSymbolPlot != null) { combiPlot.add(weatherSymbolPlot,
		 * weatherSymbolPlotWeight); } combiPlot.add(plot, mainPlotWeight); if
		 * (windPlot != null) { combiPlot.add(windPlot, windPlotWeight); }
		 * combiPlot.setGap(0.0f); chartPlot = combiPlot; } else { chartPlot =
		 * plot; }
         */

        JFreeChart chart = new JFreeChart(chartTitle,
                JFreeChart.DEFAULT_TITLE_FONT, chartPlot, true);
        chart.setBorderVisible(false);
        Paint paint = new GradientPaint(0, 0, Color.WHITE, getWidth(), 0,
                Color.WHITE);
        chart.setBackgroundPaint(paint);

        return chart;
    }

    public Plot createOverlaidPlot() {
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        // return a new chart containing the overlaid plot...

        Plot chartPlot = null;
        // make combined plot if sub-plots are defined
        if (windPlot != null || weatherSymbolPlot != null || cloudPlot != null) {
            CombinedDomainXYPlot combiPlot = new CombinedDomainXYPlot(
                    plot.getDomainAxis());
            if (cloudPlot != null) {
                combiPlot.add(cloudPlot, cloudPlotWeight);
            }
            if (weatherSymbolPlot != null) {
                combiPlot.add(weatherSymbolPlot, weatherSymbolPlotWeight);
            }
            combiPlot.add(plot, mainPlotWeight);
            if (windPlot != null) {
                combiPlot.add(windPlot, windPlotWeight);
            }
            combiPlot.setGap(0.0f);
            chartPlot = combiPlot;
        } else {
            chartPlot = plot;
        }

        return chartPlot;
    }

    /**
     * Set the default properties of the plot
     *
     * @param xAxisName The title of x-axis
     * @param yAxisName The title of y-axis
     */
    public void setPlotDefaultProperties(String xAxisName, String yAxisName) {
        ExtendedDateAxis axis = new ExtendedDateAxis(xAxisName);

        plot.setDomainAxis(axis);

        plot.setRangeAxis(new NumberAxis(yAxisName));
        plot.setBackgroundPaint(Color.white);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeCrosshairVisible(false);

        // remove plot outlines
        plot.setOutlineVisible(false);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(RANGE_GRID_LINE_COLOR);
        plot.setRangeGridlineStroke(new BasicStroke(1));

    }

    /**
     * Add a line chart in the plot
     *
     * @param timeBase The base time of the data: second, minute, hour, year or auto
     * @param phenomenon Phenomenon to plot.
     * @param plotStyle The style preference for the plot
     *
     */
    public void addLineChart(TimeBase timeBase, NumberPhenomenon ph, PlotStyle plotStyle) {
        NumberPhenomenon phenomenon = ph.clone();
        if (phenomenon.getItems().size() > 0) {
            XYItemRenderer renderer = RendererFactory.createRenderer(SplineStyle.STANDARD);
            // if using hybrid spline
            if (plotStyle.getSplineStyle().equalsIgnoreCase(SplineStyle.HYBRID)) {
                phenomenon.doHybridSpline(0.5d, 4);
                renderer = RendererFactory.createRenderer(SplineStyle.NONE);
            }

            if (plotStyle.getNonNegative()) {
                phenomenon.removeNegativeValues();
            }

            XYDataset dataset = phenomenon.getTimeSeries(plotStyle.getTitle(), timeBase);

            renderer.setSeriesPaint(0, plotStyle.getSeriesColor());
            renderer.setSeriesStroke(0, plotStyle.getStroke());

            // hidden the legend by default
            renderer.setSeriesVisibleInLegend(0, false);

            plot.setDataset(plotIndex, dataset);
            plot.setRenderer(plotIndex, renderer);

            // render the range axis
            NumberAxis numberAxis;
            // null check for number axis
            if (plotStyle.getNumberAxis() == null) {
                numberAxis = new NumberAxis(plotStyle.getTitle());
                numberAxis.setAutoRangeIncludesZero(false);
                numberAxis.setLabelPaint(plotStyle.getLabelColor());
                numberAxis.setTickLabelPaint(plotStyle.getLabelColor());

                // ugly calculation
                double max = phenomenon.getMaxValue();
                double min = phenomenon.getMinValue();
                // increase and decrease max, min respectiivly to get the
                // difference pf atleast 50
                while ((max - min) <= plotStyle.getDifference()) {
                    max++;
                    min--;
                }
                int tUnit = (int) ((max - min) / plotStyle.getTotalTicks());
                if (tUnit <= 1) {
                    tUnit = 2;
                }
                int[] range = calculateAxisMaxMin(phenomenon, tUnit,
                        plotStyle.getTotalTicks());

                NumberTickUnit ntu = new NumberTickUnit(tUnit);
                numberAxis.setTickUnit(ntu);
                numberAxis.setRangeWithMargins(range[1], range[0]);
            } else {
                numberAxis = plotStyle.getNumberAxis();
            }
            plot.setRangeAxis(rangeAxisIndex, numberAxis);

            Date minDate = phenomenon.getStartTime();
            Date maxDate = phenomenon.getEndTime();

            DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
            domainAxis.setRange(minDate, maxDate);

            plot.mapDatasetToRangeAxis(plotIndex, rangeAxisIndex);

            plotIndex++;
            rangeAxisIndex++;
        }
    }

    public void addThresholdLineChart(TimeBase timeBase, NumberPhenomenon ph, PlotStyle plotStyle) {
        NumberPhenomenon phenomenon = ph.clone();
        if (phenomenon.getItems().size() > 0) {
            XYItemRenderer renderer = RendererFactory.createRenderer(SplineStyle.STANDARD);
            // if using hybrid
            if (plotStyle.getSplineStyle()
                    .equalsIgnoreCase(SplineStyle.HYBRID)) {
                phenomenon.doHybridSpline(0.5d, 4);
                renderer = RendererFactory.createRenderer(SplineStyle.NONE);
            }

            if (plotStyle.getNonNegative()) {
                phenomenon.removeNegativeValues();
            }

            // add threshold point into the dataset
            phenomenon.addThresholdPoints(plotStyle.getThreshold());
            // create threshold data set with specified threshold value
            XYDataset dataset = phenomenon.getTimeSeriesWithThreshold(plotStyle.getTitle(), timeBase,
                    plotStyle.getThreshold());

            // render even series lines with color1 and odd series lines with
            // color2
            for (int i = 0; i < dataset.getSeriesCount(); i++) {
                if (i % 2 == 0) {
                    renderer.setSeriesPaint(i, plotStyle.getPlusDegreeColor());
                } else {
                    renderer.setSeriesPaint(i, plotStyle.getMinusDegreeColor());
                }
                renderer.setSeriesStroke(i, plotStyle.getStroke());
                // hidden legend
                renderer.setSeriesVisibleInLegend(i, false);
            }

            plot.setDataset(plotIndex, dataset);
            plot.setRenderer(plotIndex, renderer);

            NumberAxis numberAxis;
            // null check for number axis
            if (plotStyle.getNumberAxis() == null) {
                numberAxis = new NumberAxis(plotStyle.getTitle());
                numberAxis.setAutoRangeIncludesZero(false);
                numberAxis.setLabelPaint(plotStyle.getLabelColor());
                numberAxis.setTickLabelPaint(plotStyle.getLabelColor());

                // ugly calculation
                double max = phenomenon.getMaxValue();
                double min = phenomenon.getMinValue();
                int tUnit = getTemperatureTicksUnit(max, min);
                int[] range = calculateAxisMaxMin(phenomenon, tUnit,
                        plotStyle.getTotalTicks());

                NumberTickUnit ntu = new NumberTickUnit(tUnit);
                numberAxis.setTickUnit(ntu);
                numberAxis.setLowerMargin(LOWER_PLOT_MARGIN);
                numberAxis.setRangeWithMargins(range[1], range[0]);
            } else {
                numberAxis = plotStyle.getNumberAxis();
            }

            plot.setRangeAxis(rangeAxisIndex, numberAxis);

            Date minDate = phenomenon.getStartTime();
            Date maxDate = phenomenon.getEndTime();

            DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
            domainAxis.setRange(minDate, maxDate);

            plot.mapDatasetToRangeAxis(plotIndex, rangeAxisIndex);

            plotIndex++;
            rangeAxisIndex++;
        }
    }

    /**
     * Calculate the tick unit used for the temperature axis. The tick unit for the temperature axis will depend on the
     * difference between the max and min value of the temperature values.
     *
     * @param maxValue The maximum temperature value.
     * @param minValue The minimum tempereature value.
     * @return The value between each tick on the range axis.
     */
    public int getTemperatureTicksUnit(double maxValue, double minValue) {
        int unit = 0;
        double diff = maxValue - minValue;
        if (diff < 8) {
            unit = 1;
        } else if (diff < 16) {
            unit = 2;
        } else {
            unit = 5;
        }
        return unit;
    }

    /**
     * Calculate the max and min values to use for the range axis based on the dataset values.
     *
     * @param values The range values that should be plotted.
     * @param tickUnit The value between each tick in the axis.
     * @param tTicks The total number of ticks that you want on the axis
     * @return An int array with two values. The first is the max value, the second is the min value.
     */
    public int[] calculateAxisMaxMin(NumberPhenomenon phenomenon, int tickUnit,
            int tTicks) {

        int[] range = new int[2];

        double maxValue = phenomenon.getMaxValue();
        double minValue = phenomenon.getMinValue();

        int rangeMax = (int) maxValue;
        int rangeMin = (int) minValue;
        int numTicks = (int) (maxValue - minValue) / tickUnit;
        numTicks++;

        boolean addToMax = true;
        while (numTicks < tTicks) {
            if (addToMax) {
                rangeMax += tickUnit;
                addToMax = false;
            } else {
                rangeMin -= tickUnit;
                addToMax = true;
            }
            numTicks++;
        }
        range[0] = rangeMax;
        range[1] = rangeMin;

        return range;
    }

    /**
     * Add normal bars in to the chart
     *
     * @param dataset The dataset to visualise
     * @param title
     * @param color
     * @param margin Define the space between two bars
     */
    public void addBarChart(XYDataset dataset, String title, Color color,
            double margin, double maxValue) {

        if (dataset.getItemCount(0) > 0) {
            XYBarRenderer renderer = new XYBarRenderer(margin);
            renderer.setSeriesPaint(0, color);
            renderer.setShadowVisible(false);
            renderer.setBaseItemLabelsVisible(false);
            renderer.setBarPainter(new StandardXYBarPainter());
            renderer.setSeriesVisibleInLegend(0, false);
            renderer.setDrawBarOutline(true);
            plot.mapDatasetToRangeAxis(plotIndex, rangeAxisIndex);
            plot.setDataset(plotIndex, dataset);
            plot.setRenderer(plotIndex, renderer);

            if (!title.equals("")) {
                // if title is not null then show the legend and label of the
                // bar
                NumberAxis numberAxis = new NumberAxis(title);
                numberAxis.setLowerMargin(LOWER_PLOT_MARGIN);
                double maxRange = calculateRangeMax(maxValue);
                numberAxis.setRangeWithMargins(new Range(0, maxRange), true, true);
                numberAxis.setLabelPaint(color);
                numberAxis.setTickLabelPaint(color);
                plot.setRangeAxis(rangeAxisIndex, numberAxis);

            }

            plotIndex++;
            rangeAxisIndex++;
        }
    }

    /**
     * @param maxValue The maximum value in the dataset.
     * @return The max value to use in the range of the bars
     */
    private double calculateRangeMax(double maxValue) {

        double maxRange = 10;

        // we double the range as long as max precipitation is higher than max range
        while (maxValue > maxRange) {
            maxRange = maxRange * 2;
        }
        return maxRange;
    }

    /**
     * Add max min bar. Mainly used to show precipitation that has max and min values
     *
     * @param timeBase
     * @param title
     * @param maxMinTimeSeriesEnabler
     * @param maxColor
     * @param minColor
     */
    public void addMaxMinPercipitationBars(TimeBase timeBase, String title,
            NumberPhenomenon max, NumberPhenomenon min, Color maxColor, Color minColor, double maxRange) {

        XYDataset maxDataset = max.getTimeSeries(title, timeBase);
        XYDataset minDataset = min.getTimeSeries(title, timeBase);
        if (maxDataset.getSeriesCount() > 0 && minDataset.getSeriesCount() > 0
                && maxDataset.getItemCount(0) > 0 && minDataset.getItemCount(0) > 0) {
            double margin = 0.2;
            double maxPrecipitation = Math.max(max.getMaxValue(), maxRange);

            addBarChart(minDataset, "min", minColor, margin, maxPrecipitation);
            showBarValuesOnBottom(plotIndex - 1, 1D);

            rangeAxisIndex--;

            addBarChart(maxDataset, "max", maxColor, margin, maxPrecipitation);
            showBarValuesOnTop(plotIndex - 1, 6D);

            plot.getRangeAxis(getRangeAxisIndex() - 1).setVisible(false);

            final Marker marker = new ValueMarker(0);
            marker.setPaint(Color.GRAY);
            marker.setStroke(new BasicStroke(1));
            plot.addRangeMarker(getRangeAxisIndex() - 1, marker,
                    Layer.BACKGROUND);
        }
    }

    public void addPercipitationBars(TimeBase timeBase, String title, NumberPhenomenon phenomenon, Color color, double maxRange) {
        XYDataset dataSet = phenomenon.getTimeSeries(title, timeBase);

        if (dataSet.getSeriesCount() > 0) {
            double margin = 0.1;
            double maxPrecipitation = Math.max(phenomenon.getMaxValue(), maxRange);
            addBarChart(dataSet, "value", color, margin, maxPrecipitation);
            showBarValuesOnTop(plotIndex - 1, 6D);

            plot.getRangeAxis(getRangeAxisIndex() - 1).setVisible(false);

            final Marker marker = new ValueMarker(0);
            marker.setPaint(Color.GRAY);
            marker.setStroke(new BasicStroke(1));
            plot.addRangeMarker(getRangeAxisIndex() - 1, marker,
                    Layer.BACKGROUND);
        }
    }

    public void addAccumulatedPrecipitationBars(TimeBase timeBase, String title, NumberPhenomenon phenomenon, Color color){
        XYDataset dataSet = phenomenon.getTimeSeries(title, timeBase);
        if (dataSet.getSeriesCount() > 0) {
            double margin = 0.1;
            addBarChart(dataSet, messages.getString("label.accumulatedPrecipitation"), color, margin, phenomenon.getMaxValue());
            showBarValuesOnTop(plotIndex - 1, 6D);
        }

    }

    /**
     * Show bar values at the top of the bar
     *
     * @param plotIndex
     * @param offSet
     */
    private void showBarValuesOnTop(int plotIndex, double offSet) {
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(plotIndex);
        renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER));
        renderer.setItemLabelAnchorOffset(offSet);
        // renderer.setItemLabelFont(new Font("arial",Font.BOLD,9));
        renderer.setBaseItemLabelFont(new Font("arial", Font.BOLD, 8));
        renderer.setBaseItemLabelsVisible(true);
    }

    /**
     * Show bar value at the bottom of the bar
     *
     * @param plotIndex
     * @param offSet
     */
    private void showBarValuesOnBottom(int plotIndex, double offSet) {
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(plotIndex);
        renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER));
        renderer.setItemLabelAnchorOffset(offSet);

        // renderer.setItemLabelFont(new Font("arial",Font.BOLD,9));
        renderer.setBaseItemLabelFont(new Font("arial", Font.BOLD, 8));
        renderer.setBaseItemLabelsVisible(true);

    }

    /**
     * Add wind arrows to the bottom of the diagram (like yr)
     */
    public void addWindPlot(NumberPhenomenon direction, NumberPhenomenon speed, double yPosition) {
        NumberAxis na = new NumberAxis("");
        na.setVisible(false);
        XYWindArrowRenderer windRenderer = new XYWindArrowRenderer();
        windRenderer.setSeriesVisibleInLegend(0, false);

        WindDataset dataset = Utility.toChartWindDataset(direction, speed);
        windPlot = new XYPlot(dataset, plot.getDomainAxis(0), na, windRenderer);
        windPlot.setRangeGridlinesVisible(false);
        windPlot.setOutlineVisible(true);
        windPlot.setDomainGridlinesVisible(false);
        //addTimeValueSeparators(direction.getTime(), windPlot);
    }

    /**
     * Add cloud to the top of the diagram (like yr)
     */
    public void addCloudPlot(NumberPhenomenon fog, NumberPhenomenon highClouds,
            NumberPhenomenon mediumClouds, NumberPhenomenon lowClouds,
            double yPosition, int numDomainSteps) {
        NumberAxis na = new NumberAxis("");
        na.setVisible(false);
        XYCloudSymbolRenderer cloudRenderer = new XYCloudSymbolRenderer(numDomainSteps);
        cloudRenderer.setSeriesVisibleInLegend(0, false);

        CloudDataset dataset = Utility.toChartCloudDataset(fog, highClouds,
                mediumClouds, lowClouds);
        cloudPlot = new XYPlot(dataset, plot.getDomainAxis(0), na,
                cloudRenderer);
        cloudPlot.setRangeGridlinesVisible(false);
        cloudPlot.setOutlineVisible(false);
        cloudPlot.setDomainGridlinesVisible(false);
    }

    /**
     * Set the lower bound and upper bound value of y-axis
     *
     * @param rangeAxisIndex The index of range axis
     * @param lowerBound The lower bound value
     * @param upperBound The upper bound value
     */
    public void formalizeRangeAxis(int rangeAxisIndex, double lowerBound,
            double upperBound) {
        plot.getRangeAxis(rangeAxisIndex).setUpperBound(upperBound);
        plot.getRangeAxis(rangeAxisIndex).setLowerBound(lowerBound);
    }

    /**
     * Set lower and upper bound of the y-axis and include margins at the top and bottom.
     *
     * @param rangeAxisIndex The index of range axis
     * @param lowerBound The lower bound value
     * @param upperBound The upper bound value
     */
    public void formalizeRangeAxisWithMargins(int rangeAxisIndex,
            double lowerBound, double upperBound) {
        plot.getRangeAxis(rangeAxisIndex).setLowerMargin(LOWER_PLOT_MARGIN);
        plot.getRangeAxis(rangeAxisIndex).setUpperBound(upperBound);
        plot.getRangeAxis(rangeAxisIndex).setLowerBound(lowerBound);

    }

    /**
     * Hidden the series legend
     *
     * @param plotIndex
     * @param rangeAxisIndex
     */
    public void hiddenSeriesLegend(int plotIndex, int rangeAxisIndex) {
        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis(rangeAxisIndex);
        numberAxis.setVisible(false);
        XYItemRenderer renderer = plot.getRenderer(plotIndex);
        renderer.setSeriesVisibleInLegend(0, false);
    }

    public int rangeAxisIndexAdd(int i) {
        return rangeAxisIndex += i;
    }

    public int plotIndexAdd(int i) {
        return plotIndex += i;
    }

    private Map<Long, String> getDomainMarkersWithLabel(List<Date> time,
            TimeZone timeZone, Locale locale) {
        Map<Long, String> markers = new HashMap<Long, String>();
        for (Date date : time) {
            SimpleDateFormat dateFormat;
            //long term and short time have different time format marker
            if (Utility.hourDifference(time.get(0), time.get(time.size() - 1)) < 50) {
                // short term
                dateFormat = new SimpleDateFormat("EEEE d. MMM.", locale);
            } else {
                // long term
                dateFormat = new SimpleDateFormat("EEE. d. MMM.", locale);
            }
            dateFormat.setTimeZone(timeZone);
            if (isDayMarker(date, timeZone)) {
                SimpleDateFormat utcFormat = new SimpleDateFormat(
                        "dd MMM yyyy HH:mm:ss z");
                utcFormat.setTimeZone(timeZone);
                try {
                    Date dateInUTC = utcFormat.parse(utcFormat.format(date));
                    markers.put(dateInUTC.getTime(), dateFormat.format(date));
                } catch (ParseException ex) {
                    LogUtils.logException(logger, ex.getMessage(), ex);
                }

            }
        }
        return markers;
    }

    // will fail if there are no values at 00:00
    private boolean isDayMarker(Date d, TimeZone timeZone) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.setTimeZone(timeZone);
        return (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0);
    }

    public void addDomainMarkers(List<Date> time, TimeZone timeZone, Locale locale) {
        Date start = time.get(0);
        Date stop = time.get(time.size() - 1);
        addDomainMarkers(start, stop, timeZone, locale);
    }

    public void addDomainMarkers(Date start, Date stop, TimeZone timeZone, Locale locale) {
        if (!addedDomainMarkers) {
            //make a one hour by one hour time list
            //The reason is that not all the data are hour by hour
            //domainMarkers will not mark labels when no date at clock 00:00

            List<Date> time = getTimeHourByHour(start, stop);
            // set the markers
            Map<Long, String> domainMarkers = getDomainMarkersWithLabel(time,
                    timeZone, locale);
            for (Map.Entry<Long, String> entry : domainMarkers.entrySet()) {
                Long timeInMilli = entry.getKey();
                String label = entry.getValue();
                final Marker originalEnd = new ValueMarker(timeInMilli);
                originalEnd.setPaint(Color.DARK_GRAY);
                originalEnd.setStroke(new BasicStroke(1.0f));
                originalEnd.setLabel(label);//Arial Hebrew, SansSerif
                originalEnd.setLabelFont(new Font("Arial", Font.PLAIN, 14));
                originalEnd.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
                originalEnd.setLabelTextAnchor(TextAnchor.TOP_LEFT);
                plot.addDomainMarker(originalEnd, Layer.BACKGROUND);
                setAddedDomainMarkers(true);
            }

        }
    }

    private List<Date> getTimeHourByHour(Date currentTime, Date endTime) {
        List<Date> newTime = new ArrayList<Date>();
        Calendar cal = Calendar.getInstance();
        while (currentTime.getTime() <= endTime.getTime()) {
            newTime.add(currentTime);
            cal.setTime(currentTime);
            cal.add(Calendar.HOUR_OF_DAY, 1);
            currentTime = new Date(cal.getTimeInMillis());
        }
        return newTime;
    }

    /**
     * Add domain grid lines for hour based meteograms.
     *
     * This cannot be done automatically by JFreechart since we want to only label every other hour but want a domain
     * grid line for every hour.
     */
    public void addHourBasedDomainGridLines() {
        addDomainGridLinesToPlot(1, this.plot);
    }

    /**
     * Add domain grid lines for hour based meteograms.
     *
     * This cannot be done automatically by JFreechart since we want to only label every other hour but want a domain
     * grid line for every hour.
     *
     * @param interval The number of hours between each tick.
     */
    public void addDomainGridLines(int interval) {
        addDomainGridLinesToPlot(interval, this.plot);
    }

    private void addTimeValueSeparators(List<Date> values, XYPlot plot) {
        if (values.size() < 2) {
            return;
        }
        Calendar splitTime = Calendar.getInstance();
        for (int i = 0; i < values.size(); i++) {
            Date current = values.get(i);
            splitTime.setTime(current);
            splitTime.add(Calendar.HOUR_OF_DAY, -1);
            paintDomainGridLine(splitTime, plot);
        }
    }

    private void addDomainGridLinesToPlot(int hourInterval, XYPlot plot) {
        DateAxis axis = (DateAxis) plot.getDomainAxis();

        Calendar currDate = Calendar.getInstance();
        currDate.setTime(axis.getMinimumDate());

        Calendar maxDate = Calendar.getInstance();
        maxDate.setTime(axis.getMaximumDate());

        // we do not paint the first domain grid lines since it conflicts with
        // the axis line
        boolean first = true;
        while (currDate.compareTo(maxDate) < 0) {
            if (first) {
                first = false;
            } else {
                paintDomainGridLine(currDate, plot);
            }
            currDate.add(Calendar.HOUR, hourInterval);
        }
    }

    private void paintDomainGridLine(Calendar date, XYPlot plot) {

        long millis = date.getTimeInMillis();
        final Marker originalEnd = new ValueMarker(millis);
        originalEnd.setPaint(DOMAIN_GRID_LINE_COLOR);
        originalEnd.setStroke(new BasicStroke(1));
        plot.addDomainMarker(originalEnd, Layer.BACKGROUND);
    }

    /**
     * Adds the specified weather symbols to the chart. Symbols are excluded if necessary to fit in the chart. Specify
     * phenomenon to follow value curve. Plot on top of chart if phenomenon is null.
     *
     * @return
     */
    public void addWeatherSymbol(SymbolPhenomenon symbols,
            NumberPhenomenon phenomenon) {

        boolean followPhenomenon = (phenomenon != null);
        NumberAxis na;
        if (!followPhenomenon) {
            na = new NumberAxis("");
            na.setVisible(false);
            weatherSymbolPlot = new XYPlot();
            weatherSymbolPlot.setDomainAxis(plot.getDomainAxis(0));
            weatherSymbolPlot.setRangeAxis(na);
            weatherSymbolPlot.setRangeGridlinesVisible(false);
            weatherSymbolPlot.setOutlineVisible(false);
            weatherSymbolPlot.setDomainGridlinesVisible(false);
        }

        XYImageAnnotation imageannotation;

        int imageSize = getWeatherSymbolImageSize();

        for (SymbolValueItem symbol : symbols) {
            Image image = Symbols.getSymbolImage(symbol.getValue());
            image = image.getScaledInstance(imageSize, imageSize,
                    Image.SCALE_SMOOTH);
            if (followPhenomenon) { // plot over the phenomenon curve
                Double val = phenomenon.getValueByTime(symbol.getTimeFrom());
                if (val != null) {
                    double padding = 0.08; // space between curve and symbol in
                    // phenomenon units
                    imageannotation = new XYImageAnnotation(symbol.getTimeFrom()
                            .getTime(), val.doubleValue()
                            + padding
                            * (plot.getRangeAxis().getUpperBound() - plot
                            .getRangeAxis().getLowerBound()), image,
                            RectangleAnchor.CENTER);
                    plot.addAnnotation(imageannotation);
                }
            } else { // plot symbols on top in separate weatherSymbolPlot
                imageannotation = new XYImageAnnotation(symbol.getTimeFrom()
                        .getTime(), 0.5, image);
                weatherSymbolPlot.addAnnotation(imageannotation);
            }
        }
    }

    /**
     * Add arrow symobl to the chart. Specify height to follow value curve. Plot in the middle of chart if height is
     * null.
     *
     * @param direction
     * @param time
     * @param height
     */
    /**
     * Add arrow symobl to the chart. Specify height to follow value curve. Plot in the middle of chart if height is
     * null.
     *
     * @param direction The direction of arrow
     * @param height The followed height
     * @param offset The offset value to follow the height, value is between [-1,1]
     */
    public void addArrowDirectionPlot(NumberPhenomenon direction,
            NumberPhenomenon height, double offset, PlotStyle plotStyle) {
        XYArrowRenderer arrowRender = new XYArrowRenderer();
        arrowRender.setSeriesVisibleInLegend(0, false);
        arrowRender.setSeriesPaint(0, plotStyle.getSeriesColor());

        ArrowDataset dataset = Utility.toChartArrowDataset(direction,
                height, offset);
        XYPlot arrowPlot = new XYPlot(dataset, plot.getDomainAxis(0), plotStyle.getNumberAxis(),
                arrowRender);
        arrowPlot.setRangeGridlinesVisible(false);
        arrowPlot.setOutlineVisible(false);

        plot.mapDatasetToRangeAxis(plotIndex, rangeAxisIndex);
        plot.setDataset(plotIndex, dataset);
        plot.setRenderer(plotIndex, arrowRender);

        plotIndex++;
        rangeAxisIndex++;
    }

    /**
     * Calculate size of weather symbols based on plot height. Does not work for high and narrow diagrams.
     */
    private int getWeatherSymbolImageSize() {
        int originalImageSize = 38; // in pixels
        int combinedPlotWeight = mainPlotWeight + windPlotWeight
                + weatherSymbolPlotWeight + cloudPlotWeight;
        int imageSize = height / combinedPlotWeight;
        if (imageSize > originalImageSize) { // do not enlarge raster graphics
            return originalImageSize;
        }
        return imageSize;
    }

    public void setDomainAxis(ValueAxis axis) {
        plot.setDomainAxis(axis);
    }

    public void addZeroMarker(double value) {
        ValueMarker mark = new ValueMarker(value, Color.BLACK, new BasicStroke(
                2), Color.BLACK, null, 0.5f);
        this.plot.addRangeMarker(mark);

    }

    public void setRangeAxis(NumberAxis na) {
        plot.setRangeAxis(na);
        rangeAxisIndex++;
    }

    public static XYTextAnnotation createTextAnnotation(String label, double x, double y, TextAnchor textAnchor, Color color) {
        final XYTextAnnotation textAnnotation = new XYTextAnnotation(label, x, y + 0.1d);
        textAnnotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        textAnnotation.setPaint(color);
        return textAnnotation;
    }

    /**
     * Set the bound of axis
     *
     * @param numberAxis The range axis
     * @param maxValue The maximum value
     * @param minValue The minimum value
     * @param span The number of lines between the maximum value and the minimum value
     * @param gridLines The number of background lines
     */
    public static void setAxisBound(NumberAxis numberAxis, double maxValue, double minValue, int span, int gridLines) {
        double tick = (maxValue - minValue) / span;
        tick = Math.ceil(tick);
        double lowBound = Math.floor(minValue / (tick)) * (tick);
        double upperBound = Math.ceil(maxValue / (tick)) * (tick);
        int emptyLines = (int) ((lowBound + gridLines * tick - upperBound) / tick);
        lowBound = lowBound - (emptyLines / 2) * tick;
        lowBound = lowBound == minValue ? lowBound - 1 : lowBound;
        upperBound = lowBound + tick * gridLines;

        numberAxis.setTickUnit(new NumberTickUnit(tick));
        numberAxis.setLowerBound(lowBound);
        numberAxis.setUpperBound(upperBound);
    }
}
