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

import no.met.jtimeseries.data.item.AbstractValueItem;

/**
 * Filter all value items that has a from date before a specific time.
 */
public class BeforeDateFilter implements ItemFilter {

    private final Date beforeDate;
    
    /**
     * @param beforeDate All items before this date will be removed by this filter.
     */
    public BeforeDateFilter(Date beforeDate){
        this.beforeDate = beforeDate;
    }
    
    
    @Override
    public boolean removeItem(AbstractValueItem nextItem) {

        if( nextItem.getTimeFrom().before(beforeDate) ){
            return true;
        }
        
        return false;
    }

}
