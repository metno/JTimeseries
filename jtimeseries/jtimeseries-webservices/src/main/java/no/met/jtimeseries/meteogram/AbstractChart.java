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
package no.met.jtimeseries.meteogram;

import java.util.TimeZone;
import java.util.logging.Logger;
import no.met.jtimeseries.MeteogramWrapper;

import org.jfree.chart.JFreeChart;

/**
 * This is an abstract class for generating chart from data
 *
 *
 */
public abstract class AbstractChart {

    JFreeChart chart;
    //The width of the chart
    protected int width = MeteogramWrapper.DEFAULT_WIDTH;
    //The height of the chart
    protected int height = MeteogramWrapper.DEFAULT_HEIGHT;
    //The timezone of the chart
    protected TimeZone timezone = TimeZone.getTimeZone("UTC");

    static final Logger logger = Logger.getLogger(AbstractChart.class.getName());

    public abstract void drawChart();

    /**
     * Get the value of a parameters.
     * @param record The String of parameter in url command, like "temperature=true"
     * @return The value after =
     */
    static String getValueOf(String record) {
        return record.substring(record.indexOf("=") + 1);
    }
}
