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

import java.awt.Font;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

public class ErrorPlot {
	public static XYPlot getMarinogramErrorPlot(){
		XYItemRenderer renderer = new StandardXYItemRenderer();
        NumberAxis rangeAxis = new NumberAxis();
        XYPlot plot = new XYPlot(null, null, rangeAxis, renderer);
        XYTextAnnotation annotation = new XYTextAnnotation("Hello!", 0.5,  0.5);
        annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
        plot.addAnnotation(annotation);
        return plot;
	}
	
	
}
