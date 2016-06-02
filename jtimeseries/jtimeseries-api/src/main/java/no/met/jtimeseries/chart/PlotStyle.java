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
import java.awt.Stroke;
import org.jfree.chart.axis.NumberAxis;

/**
 * A class to define the style for a specific plot
 */
public class PlotStyle {
    private String title;
    private String splineStyle;
    private double threshold;
    private int totalTicks;
    private double difference;
    private Color plusDegreeColor;
    private Color minusDegreeColor;
    private Color seriesColor;
    private Color labelColor;
    private Stroke stroke;
    private NumberAxis numberAxis;
    //some curve cannot has negative values such as wind curve,
    //so all the values that are below 0 will be 0 if nonNegative is true
    private boolean nonNegative;

    private PlotStyle() {
    }

    private PlotStyle(Builder builder) {
        this.title = builder.title;
        this.threshold = builder.threshold;
        this.difference = builder.difference;
        this.totalTicks = builder.totalTicks;
        this.splineStyle = builder.splineStyle;
        this.plusDegreeColor = builder.plusDegreeColor;
        this.minusDegreeColor = builder.minusDegreeColor;
        this.seriesColor = builder.seriesColor;
        this.labelColor = builder.labelColor;
        this.stroke = builder.stroke;
        this.numberAxis = builder.numberAxis;
        this.nonNegative = builder.nonNegative;
    }

    /**
     * A class to build the PlotStyle
     */
    public static class Builder {
        private String title;
        private String splineStyle;
        private double threshold = 0.0d;
        private double difference = 0.0d;
        // Default is 11
        private int totalTicks = 11;

        private Color plusDegreeColor = new Color(240, 28, 28);
        private Color minusDegreeColor = new Color(2, 128, 217);
        private Color seriesColor = Color.BLACK;
        private Color labelColor = Color.BLACK;
        private Stroke stroke = new BasicStroke(2.0f);
        private boolean nonNegative = false;

        private NumberAxis numberAxis;

        private Builder() {
        }

        public Builder(String title) {
            this.title = title;
        }

        public Builder spline(String splineStyle) {
            this.splineStyle = splineStyle;
            return this;
        }

        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder difference(double diff) {
            this.difference = diff;
            return this;
        }

        public Builder plusDegreeColor(Color plusDegreeColor) {
            this.plusDegreeColor = plusDegreeColor;
            return this;
        }

        public Builder minusDegreeColor(Color minusDegreeColor) {
            this.minusDegreeColor = minusDegreeColor;
            return this;
        }

        public Builder seriesColor(Color seriesColor) {
            this.seriesColor = seriesColor;
            return this;
        }

        public Builder labelColor(Color labelColor) {
            this.labelColor = labelColor;
            return this;
        }

        public Builder stroke(Stroke stroke) {
            this.stroke = stroke;
            return this;
        }

        public Builder numberAxis(NumberAxis numberAxis) {
            this.numberAxis = numberAxis;
            return this;
        }

        public Builder ticks(int ticks) {
            this.totalTicks = ticks;
            return this;
        }

        public Builder nonNegative(boolean nonNegative) {
            this.nonNegative = nonNegative;
            return this;
        }
        
        public PlotStyle build() {
            return new PlotStyle(this);
        }

    }

    public Color getMinusDegreeColor() {
        return minusDegreeColor;
    }

    public Color getPlusDegreeColor() {
        return plusDegreeColor;
    }

    public Color getSeriesColor() {
        return seriesColor;
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public String getSplineStyle() {
        return splineStyle;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getTitle() {
        return title;
    }

    public NumberAxis getNumberAxis() {
        return numberAxis;
    }

    public int getTotalTicks() {
        return totalTicks;
    }

    public double getDifference() {
        return difference;
    }
    
    public boolean getNonNegative() {
        return nonNegative;
    }
}
