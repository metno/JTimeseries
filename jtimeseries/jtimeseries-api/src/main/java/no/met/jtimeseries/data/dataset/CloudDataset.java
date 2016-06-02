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

import org.jfree.data.xy.*;
/**
 * Interface for a dataset that supplies fog, highclouds, mediumclouds and lowclouds values
 * observed at various points in time.
 */
public interface CloudDataset extends XYDataset {

    /**
     * Returns the fog value.
     *
     * @param series  the series (in the range <code>0 to
     *     <code>getSeriesCount() - 1).
     * @param item  the item (in the range <code>0 to
     *     <code>getItemCount(series) - 1).
     *
     * @return The percentage of fog value.
     */
    public Number getFog(int series, int item);

    /**
     * Returns the highclouds value
     *
     * @param series  the series (in the range <code>0 to
     *     <code>getSeriesCount() - 1).
     * @param item  the item (in the range <code>0 to
     *     <code>getItemCount(series) - 1).
     *
     * @return The percentage of highclouds value
     */
    public Number getHighClouds(int series, int item);

    /**
     * Returns the percentage of mediumclouds value
     *
     * @param series  the series (in the range <code>0 to
     *     <code>getSeriesCount() - 1).
     * @param item  the item (in the range <code>0 to
     *     <code>getItemCount(series) - 1).
     *
     * @return The mediumclouds value
     */
    public Number getMediumClouds(int series, int item);
    
    /**
     * Returns the lowclouds value
     *
     * @param series  the series (in the range <code>0 to
     *     <code>getSeriesCount() - 1).
     * @param item  the item (in the range <code>0 to
     *     <code>getItemCount(series) - 1).
     *
     * @return The percentage of lowclouds value.
     */
    public Number getLowClouds(int series, int item);
}
