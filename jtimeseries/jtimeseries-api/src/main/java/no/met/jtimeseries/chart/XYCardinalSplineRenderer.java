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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * A renderer that connects data points with cardinal splines and/or draws
 * shapes at each data point. This renderer is designed for use with the
 * {@link XYPlot} class. The algorithm implemented here is original designed by
 * Dr. Murtaza Khan, which can be found at
 * http://www.mathworks.com/matlabcentral
 * /fileexchange/7078-cardinal-spline-catmull
 * -rom-spline/content/cardinalspline/TestEvaluateCardinal2D.m
 *
 */
public class XYCardinalSplineRenderer extends XYLineAndShapeRenderer {

    /**
     *
     */
    private static final long serialVersionUID = 3155076358227506874L;

    /**
     * To collect data points for later splining.
     */
    private Vector<ControlPoint> points;

    /**
     * Resolution of splines (value between [0,1])
     */
    private float tension;

    /**
     * Creates a new instance with the 'tension' attribute defaulting to 0.5.
     */
    public XYCardinalSplineRenderer() {
        this(0.5f);
    }

    /**
     * Creates a new renderer with the specified tension.
     *
     * @param tension
     *            the tension of the spline.
     */
    public XYCardinalSplineRenderer(float tension) {
        super();
        if (tension < 0 || tension > 1) {
            throw new IllegalArgumentException(
                    "Requires tension between [0,1].");
        }
        this.tension = tension;
    }

    /**
     * Get the resolution of splines.
     *
     * @return The tension of the splines.
     *
     * @see #setTension(double)
     */
    public float getTension() {
        return this.tension;
    }

    /**
     * Set the resolution of splines and sends a {@link RendererChangeEvent} to
     * all registered listeners.
     *
     * @param p
     *            number of line segments between points (must be > 0).
     *
     * @see #getPrecision()
     */
    public void setTension(float tension) {
        if (tension < 0 || tension > 1) {
            throw new IllegalArgumentException(
                    "Requires tension between [0,1].");
        }
        this.tension = tension;
        fireChangeEvent();
    }

    /**
     * Initialises the renderer.
     * <P>
     * This method will be called before the first item is rendered, giving the
     * renderer an opportunity to initialise any state information it wants to
     * maintain. The renderer can do nothing if it chooses.
     *
     * @param g2
     *            the graphics device.
     * @param dataArea
     *            the area inside the axes.
     * @param plot
     *            the plot.
     * @param data
     *            the data.
     * @param info
     *            an optional info collection object to return data back to the
     *            caller.
     *
     * @return The renderer state.
     */
    @Override
    public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea,
            XYPlot plot, XYDataset data, PlotRenderingInfo info) {

//		State state = (State) super.initialise(g2, dataArea, plot, data, info);
//		state.setProcessVisibleItemsOnly(false);
//		this.points = new Vector<ControlPoint>();
//		setBaseShapesVisible(true);
//		setDrawSeriesLineAsPath(true);
//		return state;

        State state = (State) super.initialise(g2, dataArea, plot, data, info);
        state.setProcessVisibleItemsOnly(false);
        this.points = new Vector<ControlPoint>();
        setDrawSeriesLineAsPath(true);

        setBaseShapesVisible(false);
        this.setBaseShapesFilled(false);
        return state;
    }

    /**
     * Draws the item (first pass). This method draws the lines connecting the
     * items. Instead of drawing separate lines, a GeneralPath is constructed
     * and drawn at the end of the series painting.
     *
     * @param g2
     *            the graphics device.
     * @param state
     *            the renderer state.
     * @param plot
     *            the plot (can be used to obtain standard color information
     *            etc).
     * @param dataset
     *            the dataset.
     * @param pass
     *            the pass.
     * @param series
     *            the series index (zero-based).
     * @param item
     *            the item index (zero-based).
     * @param domainAxis
     *            the domain axis.
     * @param rangeAxis
     *            the range axis.
     * @param dataArea
     *            the area within which the data is being drawn.
     */
    @Override
    protected void drawPrimaryLineAsPath(XYItemRendererState state,
            Graphics2D g2, XYPlot plot, XYDataset dataset, int pass,
            int series, int item, ValueAxis domainAxis, ValueAxis rangeAxis,
            Rectangle2D dataArea) {

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        // get the data points
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        // collect points
        if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
            ControlPoint p = new ControlPoint(
                    plot.getOrientation() == PlotOrientation.HORIZONTAL ? (float) transY1
                            : (float) transX1,
                    plot.getOrientation() == PlotOrientation.HORIZONTAL ? (float) transX1
                            : (float) transY1);
            if (!this.points.contains(p)) {
                this.points.add(p);
            }
        }
        if (item == dataset.getItemCount(series) - 1) {
            State s = (State) state;
            // construct path
            // we need at least two points to draw something
            if (this.points.size() > 1) {
                ControlPoint cp0 = this.points.get(0);
                s.seriesPath.moveTo(cp0.x, cp0.y);
                // we need at least 2 points to spline. Draw simple line
                // for less then 2 points
                if (this.points.size() == 2) {
                    ControlPoint cp1 = this.points.get(1);
                    s.seriesPath.lineTo(cp1.x, cp1.y);

                } else {
                    // construct spline
                    // number of intervals (i.e. parametric curve would be
                    // evaluted n+1 times)
                    // set the minimum resolution
                    int minimumN = 1;
                    int maximumN=4;

                    // add two more points at the top and the end for drawing the curve between the first two and last two points
                    this.points.add(0,this.points.get(0));
                    this.points.add(this.points.get(this.points.size() - 1));

                    // set the minimum distance when using minimumN
                    double smallDistance = Math.pow(
                            Math.pow(points.get(3).x
                                    - points.get(0).x, 2)
                                    + Math.pow(points.get(3).y
                                            - points.get(0).y, 2), 0.5);

                    double currentDistance;
                    double currentN;

                    List<ControlPoint> newPoints;
                    for (int i = 0; i < this.points.size() - 3; i++) {
                        currentDistance = Math
                                .pow(Math.pow(points.get(i + 3).x
                                        - points.get(i).x, 2)
                                        + Math.pow(points.get(i + 3).y
                                                - points.get(i).y, 2), 0.5);
                        currentN = minimumN * currentDistance / smallDistance;
                        currentN=currentN>maximumN?maximumN:currentN;
                        newPoints = evaluateCardinal2DAtNplusOneValues(
                                this.points.get(i),
                                this.points.get(i + 1),
                                this.points.get(i + 2),
                                this.points.get(i + 3), tension,
                                (int)currentN);
                        for (int j = 0; j < newPoints.size(); j++) {
                            s.seriesPath.lineTo(newPoints.get(j).x,newPoints.get(j).y);
                        }
                    }
                }
                s.seriesPath.lineTo(points.get(points.size()-1).x,points.get(points.size()-1).y);
                // draw path
                drawFirstPassShape(g2, pass, series, item, s.seriesPath);

            }

            // reset points vector
            this.points = new Vector<ControlPoint>();
        }
    }

    /**
     * P0,P1,P2,P3 are given four points. Each have x and y values. P1 and P2
     * are endpoints of curve. P0 and P3 are used to calculate the slope of the
     * endpoints (i.e slope of P1 and P2).
     *
     * @param p0
     * @param p1
     * @param p2
     * @param p3
     * @param t
     *            The tension
     * @param u
     *            The parameter at which spline is evaluated
     * @return A control point
     */
    private ControlPoint evaluateCardinal2D(ControlPoint p0, ControlPoint p1,
            ControlPoint p2, ControlPoint p3, float t, float u) {
        float s = (1 - t) / 2.0f;
        float[][] cardinalMatrix = new float[][] { { -s, 2 - s, s - 2, s },
                { 2 * s, s - 3, 3 - (2 * s), -s }, { -s, 0, s, 0 },
                { 0, 1, 0, 0 } };
        float[][] ghx = new float[][] { { p0.x }, { p1.x }, { p2.x },
                { p3.x } };
        float[][] ghy = new float[][] { { p0.y }, { p1.y }, { p2.y },
                { p3.y } };
        float[][] uMatrix = new float[][] { { (float)Math.pow(u, 3), (float)Math.pow(u, 2),
                u, 1 } };
        float newPointx = matrixMultiply(
                matrixMultiply(uMatrix, cardinalMatrix), ghx)[0][0];
        float newPointy = matrixMultiply(
                matrixMultiply(uMatrix, cardinalMatrix), ghy)[0][0];
        ControlPoint contrlPoint = new ControlPoint(newPointx, newPointy);
        return contrlPoint;
    }

    /**
     * Evaluate cardinal spline at N+1 values for given four points and tesion.
     * Uniform parameterization is used. P0,P1,P2 and P3 are given four points.
     *
     * @param p0
     * @param p1
     * @param p2
     * @param p3
     * @param t
     *            Tension
     * @param n
     *            The number of intervals (spline is evaluted at N+1 values).
     * @return A list of control points
     */
    public List<ControlPoint> evaluateCardinal2DAtNplusOneValues(
            ControlPoint p0, ControlPoint p1, ControlPoint p2, ControlPoint p3,
            float t, int n) {
        List<ControlPoint> cpList = new ArrayList<ControlPoint>();
        float u = 0;
        ControlPoint p = evaluateCardinal2D(p0, p1, p2, p3, t, u);
        cpList.add(p);
        // System.out.println(p.x+","+p.y);
        float du = 1.0f / n;
        for (int i = 1; i < n+1; i++) {
            u = i * du;
            p = evaluateCardinal2D(p0, p1, p2, p3, t, u);
            cpList.add(p);
            // System.out.println(p.x+","+p.y);
        }
        return cpList;
    }

    /**
     * Multiple two matrixes
     *
     * @param m1
     *            The first matrix
     * @param m2
     *            The second matrix
     * @return The result of matrix
     */
    private static float[][] matrixMultiply(float[][] m1, float[][] m2) {
        float[][] result = null;
        if (m1 != null && m2 != null && m1[0].length == m2.length) {
            float sum;
            result = new float[m1.length][m2[0].length];
            for (int i = 0; i < m1.length; i++) {
                for (int j = 0; j < m2[0].length; j++) {
                    sum = 0;
                    for (int k = 0; k < m1[0].length; k++) {
                        sum += m1[i][k] * m2[k][j];
                    }
                    result[i][j] = sum;
                }
            }
        }
        return result;
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj
     *            the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYCardinalSplineRenderer)) {
            return false;
        }
        XYCardinalSplineRenderer that = (XYCardinalSplineRenderer) obj;
        if (this.tension != that.tension) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Represents a control point.
     */
    class ControlPoint {

        /** The x-coordinate. */
        public float x;

        /** The y-coordinate. */
        public float y;

        /**
         * Creates a new control point.
         *
         * @param x
         *            the x-coordinate.
         * @param y
         *            the y-coordinate.
         */
        public ControlPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Tests this point for equality with an arbitrary object.
         *
         * @param obj
         *            the object (<code>null</code> permitted.
         *
         * @return A boolean.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ControlPoint)) {
                return false;
            }
            ControlPoint that = (ControlPoint) obj;
            if (this.x != that.x) {
                return false;
            }
            return true;
        }
    }

    public static double round(double num, int precision){
        return Math.pow(0, 2);
    }
}
