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

import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

public abstract class RendererFactory {

    public static XYItemRenderer createRenderer(String splineStyle) {
        // render the line with spline
        if (splineStyle.equalsIgnoreCase(SplineStyle.STANDARD)) {
            return new XYSplineRenderer();
        } else if (splineStyle.equalsIgnoreCase(SplineStyle.CARDINAL)) {
            return new XYCardinalSplineRenderer();
        } else {
            // render the line without spline
            StandardXYItemRenderer render = new StandardXYItemRenderer();
            render.setDrawSeriesLineAsPath(true);
            return render;
        }
    }
}
