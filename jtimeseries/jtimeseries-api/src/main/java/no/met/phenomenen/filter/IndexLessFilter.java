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
package no.met.phenomenen.filter;

import no.met.jtimeseries.data.item.AbstractValueItem;

/**
 * Item filter that removes all items with a index less than specified. In other
 * words it removes the N first elements.
 * 
 */
public class IndexLessFilter implements ItemFilter {

    private final int index;

    private int seenItems = 0;

    /**
     * @param index
     *            Removes items less than the index.
     */
    public IndexLessFilter(int index) {
        this.index = index;
    }

    @Override
    public boolean removeItem(AbstractValueItem nextItem) {

        if (seenItems < index) {
            seenItems++;
            return true;
        }

        seenItems++;
        return false;
    }

}
