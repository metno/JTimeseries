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
 * Filter all values that do not match the specified list of dates.
 */
public class FromDateFilter implements ItemFilter {

    private Set<Date> excludeDates;

    /**
     * @param excludeList
     *            All items where the from date is in this list will be
     *            removed.
     */
    public FromDateFilter(List<Date> excludeList) {
        excludeDates = new HashSet<Date>(excludeList);
    }

    @Override
    public boolean removeItem(AbstractValueItem nextItem) {

        if (excludeDates.contains(nextItem.getTimeFrom())) {
            return true;
        }

        return false;
    }

}
