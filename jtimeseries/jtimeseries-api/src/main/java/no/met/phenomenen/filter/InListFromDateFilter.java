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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.met.jtimeseries.data.item.AbstractValueItem;

/**
 * Filters item by removing all items not having a from data found in an include
 * list.
 */
public class InListFromDateFilter implements ItemFilter {

    private Set<Date> includeListSet;

    /**
     * @param includeList
     *            All items where the from date is not in this list will be
     *            removed.
     */
    public InListFromDateFilter(List<Date> includeList) {
        includeListSet = new HashSet<Date>(includeList);
    }

    @Override
    public boolean removeItem(AbstractValueItem nextItem) {

        if (!includeListSet.contains(nextItem.getTimeFrom())) {
            return true;
        }

        return false;
    }

}
