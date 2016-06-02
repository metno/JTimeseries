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
package org.jfree.chart.axis;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.jfree.ui.RectangleEdge;

public class ExtendedDateAxis extends DateAxis {
    private static final long serialVersionUID = -6565560444386257799L;   
    
    private boolean axislineExtended;
    
    // the date for the first tick
    private Date startDate;

    public ExtendedDateAxis() {
    }

    public ExtendedDateAxis(String label) {
        super(label);
    }

    public ExtendedDateAxis(String label, TimeZone zone) {
        super(label, zone);
    }

    public ExtendedDateAxis(String label, TimeZone zone, Locale locale) {
        super(label, zone, locale);
    }

    public boolean isAxislineExtended() {
        return axislineExtended;
    }

    public void setAxislineExtended(boolean axislineExtended) {
        this.axislineExtended = axislineExtended;
    }
    
    public void setStartDate(Date startDate){
        this.startDate = startDate;
    }
    
    @Override
    protected AxisState drawTickMarksAndLabels(Graphics2D g2,
            double cursor, Rectangle2D plotArea, Rectangle2D dataArea,
            RectangleEdge edge) {        
        if (isAxisLineVisible()) {
            //check to draw extended x-axis
            if (isAxislineExtended()) {
                drawAxisLine(g2, cursor, plotArea, edge);
            }
        }    
        return super.drawTickMarksAndLabels(g2, cursor, plotArea, dataArea, edge);
    }
    
    @Override
    protected Date previousStandardDate(Date date, DateTickUnit unit) {
        
        // overriding this function was neccesary for the long term meteogram otherwise the calculated start
        // tick would be one hour too soon for a reason we could not discover.
        return startDate;
    }    
}
