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
 * A renderer that connects data points with natural cubic splines and/or draws
 * shapes at each data point. This renderer is designed for use with the
 * {@link XYPlot} class.
 *
 * @since 1.0.7
 */
public class XYHybridSplineRenderer extends XYLineAndShapeRenderer {

    private static final long serialVersionUID = 5068282099813736765L;

    /**
     * To collect data points for later splining.
     */
    private Vector<ControlPoint> points;

    /**
     * Resolution of splines (number of line segments between points)
     */
    private int precision;

    /**
     * Arc of cardinal splines
     */
    private float tension;

    /**
     * Creates a new instance with the 'precision' attribute defaulting to 2.
     */
    public XYHybridSplineRenderer() {
        this(3, 0.5f);
    }

    /**
     * Creates a new renderer with the specified precision.
     *
     * @param precision
     *            the number of points between data items.
     */
    public XYHybridSplineRenderer(int precision, float tension) {
        super();
        if (precision <= 0) {
            throw new IllegalArgumentException("Requires precision > 0.");
        }
        this.precision = precision;
        if (tension < 0 || tension > 1) {
            throw new IllegalArgumentException("Requires 1 > tension > 0.");
        }
        this.tension = tension;
    }

    /**
     * Get the resolution of splines.
     *
     * @return Number of line segments between points.
     *
     * @see #setPrecision(int)
     */
    public int getPrecision() {
        return this.precision;
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
    public void setPrecision(int p) {
        if (p <= 0) {
            throw new IllegalArgumentException("Requires p > 0.");
        }
        this.precision = p;
        fireChangeEvent();
    }

    /**
     * Get the tension of cardinal splines.
     *
     * @return The value of tension.
     *
     * @see #setPrecision(int)
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
    public void setTension(float t) {
        if (t < 0 || t > 1) {
            throw new IllegalArgumentException("Requires 1 > tension > 0.");
        }
        this.tension = t;
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
            if (this.points.size() > 1) {
                // we need at least two points to draw something
                ControlPoint cp0 = this.points.get(0);
                s.seriesPath.moveTo(cp0.x, cp0.y);
                if (this.points.size() == 2) {
                    // we need at least 3 points to spline. Draw simple line
                    // for two points
                    ControlPoint cp1 = this.points.get(1);
                    s.seriesPath.lineTo(cp1.x, cp1.y);
                } else {
                    // construct spline

                    // add some cardinal spline control points
                    this.points = addCardinalSplinePoints(this.points, tension);

                    // applying standard spline
                    int np = this.points.size(); // number of points
                    float[] d = new float[np]; // Newton form coefficients
                    float[] x = new float[np]; // x-coordinates of nodes
                    float y;
                    float t;
                    float oldy = 0;
                    float oldt = 0;

                    float[] a = new float[np];
                    float t1;
                    float t2;
                    float[] h = new float[np];

                    for (int i = 0; i < np; i++) {
                        ControlPoint cpi = this.points.get(i);
                        x[i] = cpi.x;
                        d[i] = cpi.y;
                    }

                    for (int i = 1; i <= np - 1; i++) {
                        h[i] = x[i] - x[i - 1];
                    }
                    float[] sub = new float[np - 1];
                    float[] diag = new float[np - 1];
                    float[] sup = new float[np - 1];

                    for (int i = 1; i <= np - 2; i++) {
                        diag[i] = (h[i] + h[i + 1]) / 3;
                        sup[i] = h[i + 1] / 6;
                        sub[i] = h[i] / 6;
                        a[i] = (d[i + 1] - d[i]) / h[i + 1] - (d[i] - d[i - 1])
                                / h[i];
                    }
                    solveTridiag(sub, diag, sup, a, np - 2);

                    // note that a[0]=a[np-1]=0
                    // draw
                    oldt = x[0];
                    oldy = d[0];
                    s.seriesPath.moveTo(oldt, oldy);
                    for (int i = 1; i <= np - 1; i++) {
                        // loop over intervals between nodes
                        for (int j = 1; j <= this.precision; j++) {
                            t1 = (h[i] * j) / this.precision;
                            t2 = h[i] - t1;
                            y = ((-a[i - 1] / 6 * (t2 + h[i]) * t1 + d[i - 1])
                                    * t2 + (-a[i] / 6 * (t1 + h[i]) * t2 + d[i])
                                    * t1)
                                    / h[i];
                            t = x[i - 1] + t1;
                            s.seriesPath.lineTo(t, y);
                            oldt = t;
                            oldy = y;
                        }
                    }
                }
                // draw path
                drawFirstPassShape(g2, pass, series, item, s.seriesPath);
            }

            // reset points vector
            this.points = new Vector<ControlPoint>();
        }
    }

    /**
     * Add cardinal spline control points in points vector
     * @param points The points vector
     * @param tension The tension value to construct cardinal spline
     * @return Cardinal spline control points
     */
    private Vector<ControlPoint> addCardinalSplinePoints(
            Vector<ControlPoint> points, float tension) {
        Vector<ControlPoint> cardinalPoints = new Vector<ControlPoint>();
        // construct spline

        // set the minimum resolution
        int minimumN = 1;
        // set the maximum resolution
        int maximumN = 4;

        // add two more points at the top and the end for drawing the curve
        // between the first two and last two points
        points.add(0, points.get(0));
        points.add(points.get(points.size() - 1));

        // set the minimum distance when using minimumN
        double smallDistance = Math.pow(
                Math.pow(points.get(3).x
                        - points.get(0).x, 2)
                        + Math.pow(points.get(3).y
                                - points.get(0).y, 2), 0.5);

        double currentDistance;
        // number of intervals (i.e. parametric curve would be
        // evaluted currentN times)
        double currentN;

        List<ControlPoint> newPoints;
        for (int i = 0; i < points.size() - 3; i++) {
            currentDistance = Math
                    .pow(Math.pow(points.get(i + 3).x
                            - points.get(i).x, 2)
                            + Math.pow(points.get(i + 3).y
                                    - points.get(i).y, 2), 0.5);
            currentN = minimumN * currentDistance / smallDistance;
            currentN = currentN > maximumN ? maximumN : currentN;
            newPoints = evaluateCardinal2DAtNplusOneValues(
                    points.get(i),
                    points.get(i + 1),
                    points.get(i + 2),
                    points.get(i + 3), tension, (int) currentN);
            for (int j = 0; j < newPoints.size(); j++) {
                if (!cardinalPoints.contains(newPoints.get(j)))
                    cardinalPoints.add(new ControlPoint(newPoints.get(j).x,
                            newPoints.get(j).y));
            }
        }
        // change a small value of the last point in points and add it to control points
        // for the purpose of spline the line between last two points
        cardinalPoints.add(new ControlPoint(points.get(points.size()-1).x+0.01f,
                points.get(points.size()-1).y+0.01f));
        return cardinalPoints;
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
        float[][] ghx = new float[][] { { p0.x }, { p1.x }, { p2.x }, { p3.x } };
        float[][] ghy = new float[][] { { p0.y }, { p1.y }, { p2.y }, { p3.y } };
        float[][] uMatrix = new float[][] { { (float) Math.pow(u, 3),
                (float) Math.pow(u, 2), u, 1 } };
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
        float du = 1.0f / n;
        for (int i = 1; i < n + 1; i++) {
            u = i * du;
            p = evaluateCardinal2D(p0, p1, p2, p3, t, u);
            cpList.add(p);
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
     * Document me!
     *
     * @param sub
     * @param diag
     * @param sup
     * @param b
     * @param n
     */
    private void solveTridiag(float[] sub, float[] diag, float[] sup,
            float[] b, int n) {
        /*
         * solve linear system with tridiagonal n by n matrix a using Gaussian
         * elimination *without* pivoting where a(i,i-1) = sub[i] for 2<=i<=n
         * a(i,i) = diag[i] for 1<=i<=n a(i,i+1) = sup[i] for 1<=i<=n-1 (the
         * values sub[1], sup[n] are ignored) right hand side vector b[1:n] is
         * overwritten with solution NOTE: 1...n is used in all arrays, 0 is
         * unused
         */
        int i;
        /* factorization and forward substitution */
        for (i = 2; i <= n; i++) {
            sub[i] = sub[i] / diag[i - 1];
            diag[i] = diag[i] - sub[i] * sup[i - 1];
            b[i] = b[i] - sub[i] * b[i - 1];
        }
        b[n] = b[n] / diag[n];
        for (i = n - 1; i >= 1; i--) {
            b[i] = (b[i] - sup[i] * b[i + 1]) / diag[i];
        }
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
        if (!(obj instanceof XYSplineRenderer)) {
            return false;
        }
        XYHybridSplineRenderer that = (XYHybridSplineRenderer) obj;
        if (this.precision != that.precision) {
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
            /* && y == ((ControlPoint) obj).y */;
            return true;
        }

    }
}
