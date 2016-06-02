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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.util.PublicCloneable;

/**
 * A default implementation of the {@link CloudDataset} interface that stores data
 * values in arrays of double primitives.
 */
public class DefaultCloudDataset extends AbstractXYDataset implements
		CloudDataset, PublicCloneable {

	private static final long serialVersionUID = 5188633158742348257L;

	/** The keys for the series. */
    private List<String> seriesKeys;

    /** Storage for the series data. */
    private List<List<CloudDataItem>> allSeriesData;

    /**
     * Constructs a new, empty, dataset.  Since there are currently no methods
     * to add data to an existing dataset, you should probably use a different
     * constructor.
     */
	public DefaultCloudDataset() {
		this.seriesKeys = new java.util.ArrayList<String>();
        this.allSeriesData = new java.util.ArrayList<List<CloudDataItem>>();
	}

	/**
     * Constructs a dataset based on the specified data array.
     *
     * @param data  the data (<code>null</code> not permitted).
     *
     * @throws NullPointerException if <code>data</code> is <code>null</code>.
     */
	public DefaultCloudDataset(Object[][][] data) {
		this(seriesNameListFromDataArray(data), data);
	}

	/**
     * Constructs a dataset based on the specified data array.
     *
     * @param seriesNames  the names of the series (<code>null</code> not
     *     permitted).
     * @param data  the cloud data.
     *
     * @throws NullPointerException if <code>seriesNames</code> is
     *     <code>null</code>.
     */
    public DefaultCloudDataset(String[] seriesNames, Object[][][] data) {
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
     * <li><code>data[series][item][1]</code> - the percentage value of fog</li>
     * <li><code>data[series][item][2]</code> - the percentage value of high clouds</li>
     *   * <li><code>data[series][item][3]</code> - the percentage value of medium clouds</li>
     * <li><code>data[series][item][4]</code> - the percentage value of low clouds</li>
     * </ul>
     *
     * @param seriesKeys  the names of the series (<code>null</code> not
     *     permitted).
     * @param data  the cloud dataset (<code>null</code> not permitted).
     *
     * @throws IllegalArgumentException if <code>seriesKeys</code> is
     *     <code>null</code>.
     * @throws IllegalArgumentException if the number of series keys does not
     *     match the number of series in the array.
     * @throws NullPointerException if <code>data</code> is <code>null</code>.
     */
	public DefaultCloudDataset(List<String> seriesKeys, Object[][][] data) {
    	if (seriesKeys == null) {
            throw new IllegalArgumentException("Null 'seriesKeys' argument.");
        }
        if (seriesKeys.size() != data.length) {
            throw new IllegalArgumentException("The number of series keys does "
                    + "not match the number of series in the data array.");
        }
        this.seriesKeys = seriesKeys;
        int seriesCount = data.length;
        this.allSeriesData = new java.util.ArrayList<List<CloudDataItem>>(seriesCount);

        for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
            List<CloudDataItem> oneSeriesData = new java.util.ArrayList<CloudDataItem>();
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
                    Number fog = (Number) data[seriesIndex][itemIndex][1];
                    Number highClouds = (Number) data[seriesIndex][itemIndex][2];
                    Number mediumClouds = (Number) data[seriesIndex][itemIndex][3];
                    Number lowClouds = (Number) data[seriesIndex][itemIndex][4];
                    oneSeriesData.add(new CloudDataItem(xNumber, fog, highClouds,
                    		mediumClouds,lowClouds));
                }
            }
            Collections.sort(oneSeriesData);
            this.allSeriesData.add(seriesIndex, oneSeriesData);
        }
    }
    
    /**
     * Utility method for automatically generating series names.
     *
     * @param data  the cloud data (<code>null</code> not permitted).
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
        List<CloudDataItem> oneSeriesData = (List<CloudDataItem>) this.allSeriesData.get(series);
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
        List<CloudDataItem> oneSeriesData = (List<CloudDataItem>) this.allSeriesData.get(series);
        CloudDataItem windItem = (CloudDataItem) oneSeriesData.get(item);
        return windItem.getX();
    }
    
    /**
     * Returns the y-value for one item within a series.  This maps to the
     * {@link #getFog(int, int)} method and is implemented because
     * <code>CloudDataset</code> is an extension of {@link CloudDataset}.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The y-value for the item within the series.
     */
    public Number getY(int series, int item) {
        return getFog(series, item);
    }
    
    /**
     * Returns the fog for one item within a series. 
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The fog for the item within the series.
     */
	@Override
	public Number getFog(int series, int item) {
		List<CloudDataItem> oneSeriesData = (List<CloudDataItem>) this.allSeriesData.get(series);
        CloudDataItem cloudItem = (CloudDataItem) oneSeriesData.get(item);
        return cloudItem.getFog();
	}

	/**
     * Returns the highclouds for one item within a series. 
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The highclouds for the item within the series.
     */
	@Override
	public Number getHighClouds(int series, int item) {
		List<CloudDataItem> oneSeriesData = (List<CloudDataItem>) this.allSeriesData.get(series);
        CloudDataItem cloudItem = (CloudDataItem) oneSeriesData.get(item);
        return cloudItem.getHighClouds();
	}

	/**
     * Returns the mediumClouds for one item within a series. 
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The mediumClouds for the item within the series.
     */
	@Override
	public Number getMediumClouds(int series, int item) {
		List<CloudDataItem> oneSeriesData = (List<CloudDataItem>) this.allSeriesData.get(series);
        CloudDataItem cloudItem = (CloudDataItem) oneSeriesData.get(item);
        return cloudItem.getMediumClouds();
	}

	/**
     * Returns the lowClouds for one item within a series. 
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The lowClouds for the item within the series.
     */
	@Override
	public Number getLowClouds(int series, int item) {
		List<CloudDataItem> oneSeriesData = (List<CloudDataItem>) this.allSeriesData.get(series);
        CloudDataItem cloudItem = (CloudDataItem) oneSeriesData.get(item);
        return cloudItem.getLowClouds();
	}
	
	/**
     * Checks this <code>CloudDataset</code> for equality with an arbitrary
     * object.  This method returns <code>true</code> if and only if:
     * <ul>
     *   <li><code>obj</code> is not <code>null</code>;</li>
     *   <li><code>obj</code> is an instance of
     *       <code>DefaultCloudDataset</code>;</li>
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
        if (!(obj instanceof DefaultCloudDataset)) {
            return false;
        }
        DefaultCloudDataset that = (DefaultCloudDataset) obj;
        if (!this.seriesKeys.equals(that.seriesKeys)) {
            return false;
        }
        if (!this.allSeriesData.equals(that.allSeriesData)) {
            return false;
        }
        return true;
    }
}
