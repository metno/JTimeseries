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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import no.met.jtimeseries.data.dataset.CloudDataset;

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

public class XYCloudSymbolRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable,
        Serializable {

    private static final long serialVersionUID = -4474500826149210560L;
    private RenderingHints renderHints;

    private int numTimeSteps;
    
    /**
     * Creates a new renderer.
     * @param numTimeSteps The number of total timesteps in the domain range. Used in the calculation of symbol width
     * @param numTimeStepsPerCloud The number of timesteps a cloudsymbol covers. Used in calculation of symbol width
     */
    public XYCloudSymbolRenderer(int numTimeSteps) {
        super();      
        renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        this.numTimeSteps = numTimeSteps;
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
        CloudDataset cloudData = (CloudDataset) dataset;
        Number x = cloudData.getX(series, item);
        double middleX = domainAxis.valueToJava2D(x.doubleValue(), plotArea, plot.getDomainAxisEdge());
        g2.translate((int)middleX,(int)middleY); // make x=0, y=0 the middle of the symbol
        g2.setStroke(new BasicStroke());
        double height = plotArea.getHeight() - 2;

        // we set the width to be 20 which is the same as the weather symbols 
        double width = calculateWidth(plotArea.getWidth());
        double startX = -(width/2);
        double startY[] = {-(height/2), -(height/4), 0, (height/4)};
        double values[] = { (cloudData.getHighClouds(series, item).doubleValue() / 100.0 ),
                (cloudData.getMediumClouds(series, item).doubleValue() / 100.0 ),
                (cloudData.getLowClouds(series, item).doubleValue() / 100.0 ),
                (cloudData.getFog(series, item).doubleValue() / 100.0 ) };
        
        for (int i=0; i<values.length; i++) { // for each cloud type
            g2.setColor(new Color(96,96,96));
            g2.fill(new Rectangle2D.Double(startX, startY[i], (width*values[i]), (height/4-1))); // plot could
            g2.setColor(new Color(97,204,247));
            g2.fill(new Rectangle2D.Double(startX+(width*values[i]), startY[i], (width*(1-values[i])), (height/4-1))); // plot sky
        }
    }
    
    private double calculateWidth(double plotAreaWidth){

        double tickWidth = plotAreaWidth / numTimeSteps;

        // cap the cloud width at 20 px
        if( tickWidth > 20 ){
            tickWidth = 20;
        }
        
        return tickWidth;
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