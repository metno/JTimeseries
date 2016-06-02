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
package org.jfree.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.reflect.FieldUtils;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleInsets;

/**
 * An extension of {@link CombinedDomainXYPlot} that contains multiple subplots that have their own domain axis.

 */
public class StackedXYPlot extends CombinedDomainXYPlot {
    private static final long serialVersionUID = 3860487632916676808L;   

    public StackedXYPlot() {
        this(new NumberAxis());
    }
    
    /**
     * Creates a new stacked plot that allows having own domain axis among multiple subplots
     * subplots.
     *
     * @param domainAxis  the domain axis.
     */
    public StackedXYPlot(ValueAxis domainAxis) {
        super(domainAxis); 
    }

    /**
     * Returns type of plot.
     *
     * @return The type of plot.
     */
    @Override
    public String getPlotType() {
        return "Stacked_XYPlot";
    }
    
    /**
     * Adds a subplot with the specified weight 
     * @param subplot  the subplot (<code>null</code> not permitted).
     * @param weight  the weight (must be >= 1).
     */
    @Override
    public void add(XYPlot subplot, int weight) {

        Objects.requireNonNull(subplot, "subplot must not be null");
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be >= 1.");
        }        
        subplot.setParent(this);
        subplot.setWeight(weight);
        subplot.addChangeListener(this);
        subplot.setRangeZeroBaselineVisible(false);
        subplot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0), false);     
        
        List subplots = Collections.EMPTY_LIST;
        try {
            subplots = (List) FieldUtils.readField(this, "subplots", true);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StackedXYPlot.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        subplots.add(subplot);

        ValueAxis axis = getDomainAxis();
        if (axis != null) {
            axis.configure();
        }
        fireChangeEvent();
    }     

    /**
     * Draws the plot.
     * @param graphics2d the graphics device.
     * @param plotArea the plot plotArea (in Java2D space).
     * @param anchor an anchor point in Java2D space (<code>null</code>
                permitted).
     * @param parentState the state from the parent plot
                     (<code>null</code> permitted).
     * @param plotRenderingInfo chart drawing information (<code>null</code>
              permitted).
     */
    @Override
    public void draw(Graphics2D graphics2d,
                     Rectangle2D plotArea,
                     Point2D anchor,
                     PlotState parentState,
                     PlotRenderingInfo plotRenderingInfo) {
        
        if (plotRenderingInfo != null) {
            plotRenderingInfo.setPlotArea(plotArea);
        }
        
        RectangleInsets insets = getInsets();
        insets.trim(plotArea);

        setFixedRangeAxisSpaceForSubplots(null);
        //calculateAxisSpace will also calculate sub-plot plotArea
        AxisSpace space = calculateAxisSpace(graphics2d, plotArea);        
        Rectangle2D dataArea = space.shrink(plotArea, null);
        Rectangle2D[] calculatedSubPlotAreas = null;
        //get subplotsAreas from parent class
        try {
            calculatedSubPlotAreas = (Rectangle2D[]) FieldUtils.readField(this, "subplotAreas", true);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StackedXYPlot.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        setFixedRangeAxisSpaceForSubplots(space);   
          
        // draw all the subplots         
        for (int i = 0; i < getSubplots().size(); i++) {
            XYPlot plot = (XYPlot) getSubplots().get(i);             
            PlotRenderingInfo subplotInfo = null;
            if (plotRenderingInfo != null) {
                subplotInfo = new PlotRenderingInfo(plotRenderingInfo.getOwner());
                plotRenderingInfo.addSubplotInfo(subplotInfo);
            }
            plot.draw(graphics2d, calculatedSubPlotAreas[i], anchor, parentState, subplotInfo);
        }

        if (plotRenderingInfo != null) {
            plotRenderingInfo.setDataArea(dataArea);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); 
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); 
    }    
}
