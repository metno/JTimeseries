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

/**
 * A class that creates data points to generate cardinal splines
 * The algorithm implemented here is original designed by
 * Dr. Murtaza Khan, which can be found at
 * http://www.mathworks.com/matlabcentral
 * /fileexchange/7078-cardinal-spline-catmull
 * -rom-spline/content/cardinalspline/TestEvaluateCardinal2D.m
 *
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.met.jtimeseries.data.item.NumberValueItem;

public class CardinalSpline {

    private List<ControlPoint> splinePoints;

    public CardinalSpline(List<NumberValueItem> items) {
        this(items, 0.5);
    }

    public CardinalSpline(List<NumberValueItem> items, double tension) {
        generateSplinePoints(items, tension);
    }

    /**
     * Create control points to generate spline. This method will be called in
     * constructor. So after create a new object of CardinalSpline, all the
     * control points for cardinal spline will be created cardinalSplineX and
     * cardinalSplineY can return the values of these points
     *
     * @param time
     * @param value
     * @param tension
     * @return
     */
    private List<ControlPoint> generateSplinePoints(List<NumberValueItem> items, double tension) {
        List<ControlPoint> points = new ArrayList<ControlPoint>();
        splinePoints = new ArrayList<ControlPoint>();
        for( NumberValueItem item : items ){
        	points.add(new ControlPoint(item.getTimeFrom().getTime(), item.getValue()));
        }
        // construct spline
        // number of intervals (i.e. parametric curve would be
        // evaluted n+1 times)
        int minimumN = 1;
        int maximumN = 4;
        // add two more points at the top and the end for drawing the curve
        // between the first two and last two points
        points.add(0, points.get(0));
        points.add(points.get(points.size() - 1));

        List<ControlPoint> newPoints;
        double smallDistance = Math.pow(
                Math.pow(points.get(3).x
                        - points.get(0).x, 2)
                        + Math.pow(points.get(3).y
                                - points.get(0).y, 2), 0.5);
        double currentDistance;
        double currentN;
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
                if (!splinePoints.contains(newPoints.get(j)))
                    splinePoints.add(newPoints.get(j));
            }
        }
        return splinePoints;
    }

    /**
     *
     * @return A list of time string of the cardinal spline control points
     */
    public List<Date> cardinalSplineX() {
        List<Date> values = new ArrayList<Date>();
        for (int i = 0; i < splinePoints.size(); i++) {
            values.add(new Date((long) splinePoints.get(i).x));
        }
        return values;
    }

    /**
     *
     * @return A list of double values of the cardinal spline control points
     */
    public List<Double> cardinalSplineY() {
        List<Double> values = new ArrayList<Double>();
        for (int i = 0; i < splinePoints.size(); i++) {
            values.add(splinePoints.get(i).y);
        }
        return values;
    }
    
    public List<NumberValueItem> cardinalSpline() {
    	
    	List<NumberValueItem> items = new ArrayList<NumberValueItem>();
    	for( ControlPoint point : splinePoints ){
    		items.add(new NumberValueItem(new Date((long)point.x), point.y));
    	}
    	return items;
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
            ControlPoint p2, ControlPoint p3, double t, double u) {
        double s = (1 - t) / 2.0;
        double[][] cardinalMatrix = new double[][] { { -s, 2 - s, s - 2, s },
                { 2 * s, s - 3, 3 - (2 * s), -s }, { -s, 0, s, 0 },
                { 0, 1, 0, 0 } };
        double[][] ghx = new double[][] { { p0.x }, { p1.x }, { p2.x },
                { p3.x } };
        double[][] ghy = new double[][] { { p0.y }, { p1.y }, { p2.y },
                { p3.y } };
        double[][] uMatrix = new double[][] { { Math.pow(u, 3), Math.pow(u, 2),
                u, 1 } };
        double newPointx = matrixMultiply(
                matrixMultiply(uMatrix, cardinalMatrix), ghx)[0][0];
        double newPointy = matrixMultiply(
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
    private List<ControlPoint> evaluateCardinal2DAtNplusOneValues(
            ControlPoint p0, ControlPoint p1, ControlPoint p2, ControlPoint p3,
            double t, int n) {
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
    private static double[][] matrixMultiply(double[][] m1, double[][] m2) {
        double[][] result = null;
        if (m1 != null && m2 != null && m1[0].length == m2.length) {
            double sum;
            result = new double[m1.length][m2[0].length];
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
     * Represents a control point. Each control point is a point on a curve. A
     * curve consists a lot of lines which connect control points one by one
     * from left to right in order
     */
    class ControlPoint {

        /** The x-coordinate. */
        public double x;

        /** The y-coordinate. */
        public double y;

        /**
         * Creates a new control point.
         *
         * @param x
         *            the x-coordinate.
         * @param y
         *            the y-coordinate.
         */
        public ControlPoint(double x, double y) {
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
            if (this.y != that.y) {
                return false;
            }
            return true;
        }

        /**
         * Implement hashcode for object when the object implements equals
         * Hashcode will return different values for different objects
         *
         * @return
         */
        public int hashcode() {
            int result = 17;
            result = 31 * result + (int) Double.doubleToLongBits(x);
            result = 31 * result + (int) Double.doubleToLongBits(y);
            return result;
        }
    }
}
