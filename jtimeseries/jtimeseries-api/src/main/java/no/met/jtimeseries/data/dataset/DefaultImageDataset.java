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
package no.met.jtimeseries.data.dataset;

import java.awt.Image;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.util.PublicCloneable;

/**
 * A default implementation of the {@link ImageDataset} interface that stores data
 * values in arrays of double primitives.
 */
public class DefaultImageDataset extends AbstractXYDataset implements
		ImageDataset, PublicCloneable {

	private static final long serialVersionUID = -3087521573838002842L;

	/** The keys for the series. */
    private List<String> seriesKeys;

    /** Storage for the series data. */
    private List<List<ImageDataItem>> allSeriesData;

    /**
     * Constructs a new, empty, dataset.  Since there are currently no methods
     * to add data to an existing dataset, you should probably use a different
     * constructor.
     */
	public DefaultImageDataset() {
		this.seriesKeys = new java.util.ArrayList<String>();
        this.allSeriesData = new java.util.ArrayList<List<ImageDataItem>>();
	}

	/**
     * Constructs a dataset based on the specified data array.
     *
     * @param data  the data (<code>null</code> not permitted).
     *
     * @throws NullPointerException if <code>data</code> is <code>null</code>.
     */
	public DefaultImageDataset(Object[][][] data) {
		this(seriesNameListFromDataArray(data), data);
	}

	/**
     * Constructs a dataset based on the specified data array.
     *
     * @param seriesNames  the names of the series (<code>null</code> not
     *     permitted).
     * @param data  the image data.
     *
     * @throws NullPointerException if <code>seriesNames</code> is
     *     <code>null</code>.
     */
    public DefaultImageDataset(String[] seriesNames, Object[][][] data) {
        this(Arrays.asList(seriesNames), data);
    }
    
    /**
     * Constructs a dataset based on the specified data array.  The array
     * can contain multiple series, each series can contain multiple items,
     * and each item is as follows:
     * <ul>
     * <li><code>data[series][item][0]</code> - the date (either a
     *   <code>Date</code> or a <code>Number</code> that is the milliseconds
     *   since 1-Jan-1970);</li>
     * <li><code>data[series][item][1]</code> - the percentage value of direction</li>
     * </ul>
     *
     * @param seriesKeys  the names of the series (<code>null</code> not
     *     permitted).
     * @param data  the image dataset (<code>null</code> not permitted).
     *
     * @throws IllegalArgumentException if <code>seriesKeys</code> is
     *     <code>null</code>.
     * @throws IllegalArgumentException if the number of series keys does not
     *     match the number of series in the array.
     * @throws NullPointerException if <code>data</code> is <code>null</code>.
     */
	public DefaultImageDataset(List<String> seriesKeys, Object[][][] data) {
    	if (seriesKeys == null) {
            throw new IllegalArgumentException("Null 'seriesKeys' argument.");
        }
        if (seriesKeys.size() != data.length) {
            throw new IllegalArgumentException("The number of series keys does "
                    + "not match the number of series in the data array.");
        }
        this.seriesKeys = seriesKeys;
        int seriesCount = data.length;
        this.allSeriesData = new java.util.ArrayList<List<ImageDataItem>>(seriesCount);

        for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
            List<ImageDataItem> oneSeriesData = new java.util.ArrayList<ImageDataItem>();
            int maxItemCount = data[seriesIndex].length;
            for (int itemIndex = 0; itemIndex < maxItemCount; itemIndex++) {
                Object xObject = data[seriesIndex][itemIndex][0];
                if (xObject != null) {
                    Number xNumber;
                    if (xObject instanceof Number) {
                        xNumber = (Number) xObject;
                    }
                    else {
                        if (xObject instanceof Date) {
                            Date xDate = (Date) xObject;
                            xNumber = new Long(xDate.getTime());
                        }
                        else {
                            xNumber = new Integer(0);
                        }
                    }
                    Image image = (Image) data[seriesIndex][itemIndex][1];
                    Number position = (Number) data[seriesIndex][itemIndex][2];
                    Number offset = (Number) data[seriesIndex][itemIndex][3];
                    oneSeriesData.add(new ImageDataItem(xNumber, image,position,offset));
                }
            }
            Collections.sort(oneSeriesData);
            this.allSeriesData.add(seriesIndex, oneSeriesData);
        }
    }
    
    /**
     * Utility method for automatically generating series names.
     *
     * @param data  the image data (<code>null</code> not permitted).
     *
     * @return An array of <i>Series N</i> with N = { 1 .. data.length }.
     *
     * @throws NullPointerException if <code>data</code> is <code>null</code>.
     */
    public static List<String> seriesNameListFromDataArray(Object[][] data) {

        int seriesCount = data.length;
        List<String> seriesNameList = new java.util.ArrayList<String>(seriesCount);
        for (int i = 0; i < seriesCount; i++) {
            seriesNameList.add("Series " + (i + 1));
        }
        return seriesNameList;
    }
	
    /**
     * Returns the number of series in the dataset.
     *
     * @return The series count.
     */
    public int getSeriesCount() {
        return this.allSeriesData.size();
    }
    
    /**
     * Returns the number of items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The item count.
     */
    public int getItemCount(int series) {
        if (series < 0 || series >= getSeriesCount()) {
            throw new IllegalArgumentException("Invalid series index: "
                    + series);
        }
        List<ImageDataItem> oneSeriesData = (List<ImageDataItem>) this.allSeriesData.get(series);
        return oneSeriesData.size();
    }
    
    /**
     * Returns the key for a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The series key.
     */
    public Comparable<String> getSeriesKey(int series) {
        if (series < 0 || series >= getSeriesCount()) {
            throw new IllegalArgumentException("Invalid series index: "
                    + series);
        }
        return (Comparable<String>) this.seriesKeys.get(series);
    }
    
    /**
     * Returns the x-value for one item within a series.  This should represent
     * a point in time, encoded as milliseconds in the same way as
     * java.util.Date.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The x-value for the item within the series.
     */
    public Number getX(int series, int item) {
        List<ImageDataItem> oneSeriesData = (List<ImageDataItem>) this.allSeriesData.get(series);
        ImageDataItem windItem = (ImageDataItem) oneSeriesData.get(item);
        return windItem.getX();
    }
    
    /**
     * Returns the y-value for one item within a series.  This maps to the
     * {@link #getFog(int, int)} method and is implemented because
     * <code>ImageDataset</code> is an extension of {@link ImageDataset}.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The y-value for the item within the series.
     */
    public Number getY(int series, int item) {
        return this.getPosition(series, item);
    }
    
    /**
     * Returns the direction for one item within a series. 
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The direction for the item within the series.
     */
	@Override
	public Image getImage(int series, int item) {
		List<ImageDataItem> oneSeriesData = (List<ImageDataItem>) this.allSeriesData.get(series);
		ImageDataItem imageItem = (ImageDataItem) oneSeriesData.get(item);
        return imageItem.getImage();
	}

	/**
     * Returns the position for one item within a series. 
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The position for the item within the series.
     */
	@Override
	public Number getPosition(int series, int item) {
		List<ImageDataItem> oneSeriesData = (List<ImageDataItem>) this.allSeriesData.get(series);
		ImageDataItem imageItem = (ImageDataItem) oneSeriesData.get(item);
        return imageItem.getPosition();
	}

	/**
     * Returns the offset for one item within a series. 
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The offset for the item within the series.
     */
	@Override
	public Number getOffset(int series, int item) {
		List<ImageDataItem> oneSeriesData = (List<ImageDataItem>) this.allSeriesData.get(series);
		ImageDataItem imageItem = (ImageDataItem) oneSeriesData.get(item);
        return imageItem.getOffset();
	}
	
	/**
     * Checks this <code>ImageDataset</code> for equality with an arbitrary
     * object.  This method returns <code>true</code> if and only if:
     * <ul>
     *   <li><code>obj</code> is not <code>null</code>;</li>
     *   <li><code>obj</code> is an instance of
     *       <code>DefaultImageDataset</code>;</li>
     *   <li>both datasets have the same number of series containing identical
     *       values.</li>
     * <ul>
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DefaultImageDataset)) {
            return false;
        }
        DefaultImageDataset that = (DefaultImageDataset) obj;
        if (!this.seriesKeys.equals(that.seriesKeys)) {
            return false;
        }
        if (!this.allSeriesData.equals(that.allSeriesData)) {
            return false;
        }
        return true;
    }


}
