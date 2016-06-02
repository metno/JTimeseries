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
package no.met.jtimeseries.chart;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Paint;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import no.met.jtimeseries.data.dataset.ArrowDataset;
import no.met.jtimeseries.data.dataset.CloudDataset;
import no.met.jtimeseries.data.dataset.DefaultArrowDataset;
import no.met.jtimeseries.data.dataset.DefaultCloudDataset;
import no.met.jtimeseries.data.dataset.DefaultImageDataset;
import no.met.jtimeseries.data.dataset.ImageDataset;
import no.met.jtimeseries.data.item.AbstractValueItem;
import no.met.jtimeseries.data.item.NumberValueItem;
import no.met.phenomenen.NumberPhenomenon;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.Year;
import org.jfree.data.xy.DefaultWindDataset;
import org.jfree.data.xy.WindDataset;

public class Utility {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Calculate the time at the threshold value
     * 
     * @param time1
     *            The first time string
     * @param value1
     *            The first value
     * @param time2
     *            The second time String
     * @param value2
     *            The second value
     * @param value3
     *            The threshold value
     * @return The time at the threshold
     */
    public static Date timeOfThreshold(Date time1, double value1, Date time2, double value2, double value3) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        long date3 = 0;
        long date1 = time1.getTime();
        long date2 = time2.getTime();

        double slope = (date2 - date1) / (value2 - value1);
        double bias = date2 - slope * value2;
        date3 = new Double(slope * value3 + bias).longValue();
        return new Date(date3);
    }

    /**
     * The the hour of a time.
     * 
     * @param time
     *            The time string. Such as "2012-03-20T14:00:00Z"
     * @return The hour. Such as 14 for the above case
     */
    public static int getHourOfDay(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getHourOfDayUTC(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(time);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * @param orig
     *            The orginal date to add hours to
     * @param hours
     *            The number of hours to add.
     * @return A new date object with the specified number of hours added to it.
     */
    public static Date getDateWithAddedHours(Date orig, int hours) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(orig);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();

    }

    /**
     * Add value points that corresponding values are threshold values into the
     * value list
     * 
     * @param value
     *            The value list
     * @param Threshold
     *            The threshold value
     * @return New value list that contains threshold values
     */
    public static List<Double> addValueOfThreshold(List<Double> value, double threshold) {
        List<Double> valueWithThreshold = new ArrayList<Double>();

        if (value.size() >= 2) {
            double value1, value2;
            int index = 0;
            valueWithThreshold.add(index++, value.get(0));

            for (int i = 1; i < value.size(); i++) {
                value1 = value.get(i - 1);
                value2 = value.get(i);
                // if the threshold value is between two points,
                // then add a threshold point between the two points
                if (value1 < threshold && value2 > threshold || value1 > threshold && value2 < threshold) {
                    valueWithThreshold.add(index++, new Double(threshold));
                }
                valueWithThreshold.add(index++, value2);
            }
        }
        return valueWithThreshold;
    }

    /**
     * Add time points that the corresponding values are threshold values into
     * the time series
     * 
     * @param time
     *            Time List
     * @param value
     *            Value List
     * @param threshold
     *            The threshold value
     * @return New time list that contains threshold values
     */
    public static List<Date> addTimeOfThreshold(List<Date> time, List<Double> value, double threshold) {
        List<Date> timeWithThreshold = new ArrayList<Date>();
        if (value.size() >= 2) {
            double value1, value2;
            Date time1, time2;
            int index = 0;
            timeWithThreshold.add(index++, time.get(0));

            for (int i = 1; i < value.size(); i++) {
                value1 = value.get(i - 1);
                value2 = value.get(i);
                time1 = time.get(i - 1);
                time2 = time.get(i);
                // if the threshold value is between two points,
                // then add a threshold point between the two points
                if (value1 < threshold && value2 > threshold || value1 > threshold && value2 < threshold) {
                    timeWithThreshold.add(index++, timeOfThreshold(time1, value1, time2, value2, threshold));
                }
                timeWithThreshold.add(index++, time.get(i));
            }
        }
        return timeWithThreshold;
    }

    /**
     * Add time points that the corresponding values are threshold values into
     * the time series
     * 
     * @param time
     *            Time List
     * @param value
     *            Value List
     * @param threshold
     *            The threshold value
     * @return New time list that contains threshold values
     */
    public static List<NumberValueItem> addThresholdItems(List<NumberValueItem> items, double threshold) {

        List<NumberValueItem> itemsWithThreshold = new ArrayList<NumberValueItem>();
        if (items.size() >= 2) {
            double value1, value2;
            Date time1, time2;
            itemsWithThreshold.add(items.get(0));

            for (int i = 1; i < items.size(); i++) {
                value1 = items.get(i - 1).getValue();
                value2 = items.get(i).getValue();
                time1 = items.get(i - 1).getTimeFrom();
                time2 = items.get(i).getTimeFrom();

                if (value1 < threshold && value2 > threshold || value1 > threshold && value2 < threshold) {

                    Date timeOfThreshold = timeOfThreshold(time1, value1, time2, value2, threshold);
                    itemsWithThreshold.add(new NumberValueItem(timeOfThreshold, threshold));
                }
                itemsWithThreshold.add(items.get(i));
            }
        }

        return itemsWithThreshold;
    }

    /**
     * Calculate the hour difference between two time
     * 
     * @param timeFrom
     *            The start time
     * @param timeTo
     *            The end time
     * @return The hour difference
     */
    public static int hourDifference(Date timeFrom, Date timeTo) {
        double diff = (timeTo.getTime() - timeFrom.getTime()) / 1000 / 60 / 60;
        return new Double(diff).intValue();
    }

    /**
     * Removes time values closer than the specified interval (ensures that
     * dates are minHourInterval apart or more).
     * 
     * @param orig
     *            Original date list
     * @param minHourInterval
     *            The minimum interval hours
     * @param startPoint
     *            Set the start point
     * @return The filtered date list
     */
    public static List<Date> filterMinimumHourInterval(List<Date> orig, int minHourInterval, int startPoint) {
        if (orig.size() < 2) // nothing to filter
            return orig;
        ArrayList<Date> filtered = new ArrayList<Date>();
        filtered.add(new Date(orig.get(startPoint).getTime()));
        long hourDiff;
        if (orig.size() >= 2) {
            Date prevDate = new Date(orig.get(startPoint).getTime());
            for (int i = startPoint + 1; i < orig.size();) {
                hourDiff = ((orig.get(i).getTime() - prevDate.getTime()) / 3600000);
                // if hourdiff >= minHourInterval then add it directly
                if (hourDiff >= minHourInterval) {
                    filtered.add(new Date(orig.get(i).getTime()));
                    prevDate = new Date(orig.get(i).getTime());
                    i++;
                } else {
                    while (hourDiff < minHourInterval && i < orig.size() - 1) {
                        i++;
                        hourDiff = ((orig.get(i).getTime() - prevDate.getTime()) / 3600000);
                    }
                    filtered.add(new Date(orig.get(i).getTime()));
                    prevDate = new Date(orig.get(i).getTime());
                    i++;
                }
            }
        }
        filtered.remove(filtered.size() - 1);
        return filtered;
    }

    /**
     * Create a JFreeChart WindDataset from WindDirection and WindSpeed
     */
    public static WindDataset toChartWindDataset(NumberPhenomenon direction, NumberPhenomenon speed) {
        List<Date> timeList = direction.getTime();
        List<Double> degreeList = direction.getValue();
        List<Double> speedList = speed.getValue();

        Object[][] timeSeries = new Object[timeList.size()][];
        for (int i = 0; i < timeList.size(); i++) {
            timeSeries[i] = new Object[] { new Date(timeList.get(i).getTime()), new Double(degreeList.get(i)),
                    new Double(speedList.get(i)) };
        }
        Object[][][] dataSetArray = { timeSeries };
        return new DefaultWindDataset(dataSetArray);
    }

    public static ArrowDataset toChartArrowDataset(NumberPhenomenon direction, NumberPhenomenon position, double offset) {
        List<Date> timeList = direction.getTime();
        List<Double> degreeList = direction.getValue();
        List<Double> positionList = new ArrayList<Double>();
        if (offset < -1 || offset > 1) {
            offset = 0.1;
        }
        for (int i = 0; i < degreeList.size(); i++) {
            positionList.add(null);
        }

        if (position != null)
            positionList = position.getValue();

        Object[][] timeSeries = new Object[timeList.size()][];
        for (int i = 0; i < timeList.size(); i++) {
            timeSeries[i] = new Object[] { new Date(timeList.get(i).getTime()), new Double(degreeList.get(i)),
                    positionList.get(i) == null ? null : new Double(positionList.get(i)), new Double(offset) };
        }
        Object[][][] dataSetArray = { timeSeries };
        return new DefaultArrowDataset(dataSetArray);
    }

    public static ImageDataset toChartImageDataset(List<Date> time, List<Image> images, List<Double> position,
            double offset) {

        List<Double> positionList = new ArrayList<Double>();
        if (offset < -1 || offset > 1) {
            offset = 0.1;
        }
        if (position == null)
            for (int i = 0; i < images.size(); i++) {
                positionList.add(null);
            }
        else
            positionList = position;

        Object[][] timeSeries = new Object[time.size()][];
        for (int i = 0; i < time.size(); i++) {
            timeSeries[i] = new Object[] { new Date(time.get(i).getTime()), images.get(i),
                    positionList.get(i) == null ? null : new Double(positionList.get(i)), new Double(offset) };
        }
        Object[][][] dataSetArray = { timeSeries };
        return new DefaultImageDataset(dataSetArray);
    }

    /**
     * Create a JFreeChart CloudDataset from Cloud data
     */
    public static CloudDataset toChartCloudDataset(NumberPhenomenon fog, NumberPhenomenon highClouds,
            NumberPhenomenon mediumClouds, NumberPhenomenon lowClouds) {

        // assume all phenomenon has same size. Should be ok for data from
        // api.met.no
        int numItems = fog.getTimes().size();
        Object[][] timeSeries = new Object[fog.getTimes().size()][];
        for (int i = 0; i < numItems; i++) {
            timeSeries[i] = new Object[] { fog.getItem(i).getTimeFrom(), ((NumberValueItem) fog.getItem(i)).getValue(),
                    ((NumberValueItem) highClouds.getItem(i)).getValue(),
                    ((NumberValueItem) mediumClouds.getItem(i)).getValue(),
                    ((NumberValueItem) lowClouds.getItem(i)).getValue() };
        }

        Object[][][] dataSetArray = { timeSeries };
        return new DefaultCloudDataset(dataSetArray);

    }

    /**
     * Return regulartimeperiod for create dataset
     * 
     * @param timeBase
     * @param time
     * @return
     */
    public static RegularTimePeriod getPeriod(TimeBase timeBase, Date date) {
        RegularTimePeriod period = null;

        if (timeBase == TimeBase.YEAR)
            period = new Year(date);
        else if (timeBase == TimeBase.MONTH)
            period = new Month(date);
        else if (timeBase == TimeBase.DAY)
            period = new Day(date);
        else if (timeBase == TimeBase.HOUR)
            period = new Hour(date);
        else if (timeBase == TimeBase.MINUTE)
            period = new Minute(date);
        else if (timeBase == TimeBase.SECOND)
            period = new Second(date);
        else if (timeBase == TimeBase.HOUR_3)
            return new FlexibleHour(date, 3);
        else if (timeBase == TimeBase.HOUR_6)
            return new FlexibleHour(date, 6);
        else if (timeBase == TimeBase.HOUR_12)
            return new FlexibleHour(date, 12);
        else if (timeBase == TimeBase.HOUR_24)
            return new FlexibleHour(date, 24);

        return period;
    }

    /**
     * Set the timebase automatically according to the time difference between
     * the first two time
     * 
     * @param xAxis
     *            time axis
     * @return the time base
     */
    public static TimeBase autoTimeBase(List<Date> xAxis) {
        Date time1 = xAxis.get(0);
        Date time2 = xAxis.get(1);

        return calculateTimeBase(time1, time2);
    }

    /**
     * Set the timebase automatically according to the time difference between
     * the first two time
     * 
     * @param xAxis
     *            time axis
     * @return the time base
     */
    public static TimeBase autoTimeBaseFromItems(List<? extends AbstractValueItem> items) {

        // need at least two items for the calculation
        if (items.size() < 2) {
            return null;
        }

        Date time1 = items.get(0).getTimeFrom();
        Date time2 = items.get(1).getTimeFrom();

        return calculateTimeBase(time1, time2);

    }

    /**
     * Create a plot with an error message in case of error.
     * 
     * @param width
     *            The width of the plot
     * @return The JFreeChart error plot
     */
    public static JFreeChart createErrorChart(int width) {
        XYPlot plot = new XYPlot();
        plot.setBackgroundPaint(null);
        plot.setBackgroundImage(Symbols.getImage("/error.png"));
        JFreeChart jchart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        jchart.setBorderVisible(false);
        Paint paint = new GradientPaint(0, 0, Color.WHITE, width, 0, Color.WHITE);
        jchart.setBackgroundPaint(paint);

        return jchart;
    }

    private static TimeBase calculateTimeBase(Date time1, Date time2) {
        long diff = time2.getTime() - time1.getTime();
        // if the diff is larger than one year
        if ((diff / 365 / 24 / 60 / 60 / 1000) >= 1)
            return TimeBase.YEAR;
        // if the diff is larger than one month
        else if ((diff / 28 / 24 / 60 / 60 / 1000) >= 1)
            return TimeBase.MONTH;
        // if the diff is larger than one day
        else if ((diff / 24 / 60 / 60 / 1000) >= 1)
            return TimeBase.DAY;
        // if the diff is larger than one hour
        else if ((diff / 60 / 60 / 1000) >= 1)
            return TimeBase.HOUR;
        // if the diff is larger than one minute
        else if ((diff / 60 / 1000) >= 1)
            return TimeBase.MINUTE;
        // if the diff is larger than one second
        else if (diff >= 1000)
            return TimeBase.SECOND;
        return null;
    }
}
