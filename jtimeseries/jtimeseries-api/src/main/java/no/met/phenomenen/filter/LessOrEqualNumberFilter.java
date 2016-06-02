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
import no.met.jtimeseries.data.item.NumberValueItem;

/**
 * Item filter that is used to remove items that are less or equal to a specific
 * value.
 * 
 * Using this filter on anything but a NumberValueItem will lead to a runtime exception.
 */
public class LessOrEqualNumberFilter implements ItemFilter {

    private final double threshold;
    
    /**
     * @param threshold All items that have a value less or equals to this will be removed.
     */
    public LessOrEqualNumberFilter(double threshold){
        this.threshold = threshold;
    }
    
    @Override
    public boolean removeItem(AbstractValueItem nextItem) {

        if( !(nextItem instanceof NumberValueItem) ){
            throw new IllegalArgumentException( nextItem + " is not a NumberValueItem" );
        }
        
        NumberValueItem i = (NumberValueItem) nextItem;
        
        if( i.getValue() <= threshold ){
            return true;
        }
        
        return false;
    }

}
