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
package org.jfree.chart.renderer.xy;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import no.met.jtimeseries.data.dataset.ArrowDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.PublicCloneable;

/**
 * A custom renderer for displaying cloud symbols
 */

public class XYArrowRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable,
        Serializable {

    private static final long serialVersionUID = -4474500826149210560L;
    private RenderingHints renderHints;

    /**
     * Creates a new renderer.
     */
    public XYArrowRenderer() {
        super();      
        renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
    
    /**
     * Draws the visual representation of a single symbol.
     */
    @Override
    public void drawItem(Graphics2D g2d, XYItemRendererState state, Rectangle2D plotArea, PlotRenderingInfo info,
            XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
            CrosshairState crosshairState, int pass) {
        // Needs a new graphics object to use translate() and rotate()
        Graphics2D g2 = (Graphics2D) g2d.create();
        g2.setRenderingHints(renderHints);
        double middleY = plotArea.getCenterY();
        double maxY=plotArea.getMaxY();
        double minY=plotArea.getMinY();
        double upperBound=rangeAxis.getUpperBound();
        double lowerBound=rangeAxis.getLowerBound();
        
        ArrowDataset arrowData = (ArrowDataset) dataset;
        int nItems = arrowData.getItemCount(series);
        Number x = arrowData.getX(series, item);
        Number direction = arrowData.getDirection(series, item);
        Number position = arrowData.getPosition(series, item);
        
        Number offset = arrowData.getOffset(series, item);
        
        double middleX = domainAxis.valueToJava2D(x.doubleValue(), plotArea, plot.getDomainAxisEdge());
        //if no logical position then just show arrow in the middle
        if (position==null)
        	g2.translate((int)middleX,(int)middleY); // make x=0, y=0 the middle of the symbol
        else
        	g2.translate(middleX,relatedPosition(minY,maxY,lowerBound,upperBound,position.doubleValue(),offset.doubleValue())); 
        g2.rotate(Math.toRadians(direction.doubleValue()));
        Paint itemPaint = getItemPaint(series, item);
        g2.setPaint(itemPaint);
        g2.setStroke(new BasicStroke());
        
        double arrowWidth = 1.2;
        double arrowHeight = (plotArea.getWidth() / (nItems)) /2.0;
        
        drawArrow(g2,arrowWidth,arrowHeight);
    }
    
    /**
     * Get physical position on the chart
     * @param minY Physical min value of Y
     * @param maxY Physical max value of Y
     * @param lowerBound Logical min value of Y
     * @param upperBound Logical max value of Y
     * @param position Logical position of Y
     * @param offset Offset the to physical position
     * @return The Physical y position
     */
    private double relatedPosition(double minY, double maxY, double lowerBound, double upperBound, double position, double offset) {
		return (position-lowerBound)/(upperBound-lowerBound)*(minY-maxY)+maxY+(minY-maxY)*offset;
	}

	private void drawArrow(Graphics2D g,double arrowWidth, double arrowHeight) {
        int startX = - (int)(arrowWidth/2)-1;
        int startY = -(int)(arrowHeight/2);
        //draw main line
        g.fill(new Rectangle2D.Double(startX, startY, arrowWidth, arrowHeight));
        //draw arrow
        g.fill(new Polygon(new int[]{startX,startX-3,startX+4,startX+1},new int[]{startY,startY+5,startY+5,startY},4));
        g.setStroke(new BasicStroke(1));
    }
    
    /**
     * Returns a clone of the renderer.
     * @return A clone.
     * @throws CloneNotSupportedException
     *             if the renderer cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    

}