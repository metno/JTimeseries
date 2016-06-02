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
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.WindDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

/**
 * A custom renderer for displaying wind arrows
 */

public class XYWindArrowRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable,
        Serializable {

    private static final long serialVersionUID = 3857515445536001879L;
    // Height of the arrow in pixels
    private int arrowHeight = 16;
    // Width of the arrow line and feather lines in pixels
    private int arrowWidth = 1;
    // width and length of the arrow head in pixels
    private int arrowHeadSize = 5;
    // width of the feather lines in pixels
    private int featherWidth = 6;
    // Space between each feather line
    private int featherPadding = 5;
    // Use (yr-style) arrow or standard circle head
    private boolean useArrowHead = true;
    // to avoid recalculation of arrow size on each plot
    private int cachedPlotHeight = -1;
    private double zeroWindLimit=0.2;

    private RenderingHints renderHints;

    /**
     * Creates a new renderer.
     */
    public XYWindArrowRenderer() {
        super();
        renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private void resizeArrowByPlotHeight(int plotHeight) {
        if (plotHeight == cachedPlotHeight) // values are same as last time
            return;
        arrowHeight = (int) (plotHeight * 0.6);
        arrowWidth = 1 + (arrowHeight / 20);
        arrowHeadSize = 4 + arrowWidth;
        featherWidth = 2 + (4 * arrowWidth);
        cachedPlotHeight = plotHeight;
    }

    /**
     * Draws the visual representation of a single wind arrow.
     */
    @Override
    public void drawItem(Graphics2D g2d, XYItemRendererState state, Rectangle2D plotArea, PlotRenderingInfo info,
            XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
            CrosshairState crosshairState, int pass) {
        resizeArrowByPlotHeight((int) plotArea.getHeight());
        // Needs a new graphics object to use translate() and rotate()
        Graphics2D g2 = (Graphics2D) g2d.create();
        g2.setRenderingHints(renderHints);
        RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
        double middleY = plotArea.getCenterY();

        WindDataset windData = (WindDataset) dataset;

        Number x = windData.getX(series, item);
        Number windDir = windData.getWindDirection(series, item);
        Number wforce = windData.getWindForce(series, item);

        double middleX = domainAxis.valueToJava2D(x.doubleValue(), plotArea, domainAxisLocation);

        g2.translate((int) middleX, (int) middleY);
        g2.setColor(Color.BLACK);

        if (wforce.doubleValue() <= zeroWindLimit) {
            drawCircle(g2);
        } else {
            g2.rotate(Math.toRadians(windDir.doubleValue() - 180));
            drawArrow(g2, wforce.doubleValue());

            if (useArrowHead) {
                g2.fill(getPolygonHead(arrowHeadSize, arrowHeight));
            } else {
                g2.draw(getCircleHead(arrowHeadSize, arrowHeight));
            }
        }
    }

    private void drawCircle(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING , RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawOval(-arrowHeight/2, -arrowHeight/2, arrowHeight, arrowHeight);
    }

    private void drawArrow(Graphics2D g, double speed) {
        int startX = -(arrowWidth / 2);
        int startY = -(arrowHeight / 2);
        // draw main arrow line
        g.fill(new Rectangle2D.Double(startX, startY, arrowWidth, arrowHeight));

        // drawing speed feathers (representing 25 ms each)
        int featherOffset = 0;
        int featherHeight = arrowHeight / featherPadding;
        int flagFeathers = (int) speed / 25;
        int[] flagFeatherX = { 1, -featherWidth, 1 };
        for (int i = 0; i < flagFeathers; i++) {
            if (i > 0)
                featherOffset += 1; // more space if multiple flags
            int yValue = (arrowHeight / 2) - (featherOffset * featherHeight);
            int flagSize = (int) (featherHeight * 1.5f);
            int[] flagFeatherY = { yValue, (yValue - flagSize / 2), yValue - flagSize };
            g.fill(new Polygon(flagFeatherX, flagFeatherY, 3));
            featherOffset += 1;
        }
        // Add space between flag-feather and next
        if (flagFeathers > 0)
            featherOffset++;
        double remainingSpeed = speed - (flagFeathers * 25);

        // draw full feathers (representing 5 ms each)
        int fullFeathers = (int) remainingSpeed / 5;
        g.setStroke(new BasicStroke(arrowWidth));
        for (int i = 0; i < fullFeathers; i++) {
            int yValue = (arrowHeight / 2) - (featherOffset * featherHeight);
            yValue -= (arrowWidth / 2); // allign with start of arrow
            int[] x = { 1, -featherWidth };
            int[] y = { yValue, yValue };
            g.draw(new Polygon(x, y, 2));
            featherOffset += 1;
        }
        remainingSpeed = remainingSpeed - (fullFeathers * 5);

        // draw half feathers (representing 2.5 ms each)
        int halfFeathers = (int) (remainingSpeed / 2.5);
        // never draw half-feathers at the start
        if (featherOffset == 0)
            featherOffset = 1;
        for (int i = 0; i < halfFeathers; i++) {
            int yValue = (arrowHeight / 2) - (featherOffset * featherHeight);
            yValue -= (arrowWidth / 2); // allign with start of arrow
            int[] x = { 1, (-featherWidth / 2) };
            int[] y = { yValue, yValue };
            g.draw(new Polygon(x, y, 2));
            featherOffset += 1;
        }

        g.setStroke(new BasicStroke(1));
    }

    private Polygon getPolygonHead(int size, int lineHeight) {
        int half = (size / 2);
        int yStart = -(lineHeight / 2);
        int[] x = { -half, half, 0 };
        int[] y = { yStart, yStart, yStart - size };
        return new Polygon(x, y, 3);
    }

    private Ellipse2D.Double getCircleHead(int size, int lineHeight) {
        int x = -(size / 2);
        int y = -((lineHeight / 2) + size);
        return new Ellipse2D.Double(x, y, size, size);
    }

    /**
     * Returns a clone of the renderer.
     * 
     * @return A clone.
     * @throws CloneNotSupportedException
     *             if the renderer cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}