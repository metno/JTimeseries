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
package no.met.phenomenen;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;
import no.met.jtimeseries.Jsonizer;
import no.met.jtimeseries.data.item.AbstractValueItem;
import no.met.jtimeseries.data.item.NumberValueItem;
import no.met.jtimeseries.data.item.SymbolValueItem;

/**
 * Phenomenon where the should be plotted as some type of symbol. It is the
 * Responsibility of the plotter to find the correct symbols to use for each
 * value.
 *
 */
public class SymbolPhenomenon extends AbstractPhenomenon implements Iterable<SymbolValueItem>, Jsonizer {

	protected List<SymbolValueItem> values;
	
	public SymbolPhenomenon(){
		values = new ArrayList<SymbolValueItem>();
	}

		
	public void addValue(Date timeFrom, int value){
		values.add(new SymbolValueItem(timeFrom, timeFrom, value));
	}    
    
	public void addValue(Date timeFrom, Date timeTo, int value){
		values.add(new SymbolValueItem(timeFrom, timeTo, value));
	}
	
    @Override
    public void cutOlderThan(Date d) {
    	List<SymbolValueItem> newItems = new ArrayList<SymbolValueItem>();
    	for ( SymbolValueItem item : values )
    		if ( ! item.getTimeTo().after(d) )
    			newItems.add(item);
    	values = newItems;
    }


	@Override
	public List<Date> getTimes() {
		List<Date> times = new ArrayList<Date>();
		for(SymbolValueItem item : values ){
			times.add(item.getTimeFrom());
		}
		return times;
	}	
	
    @Override
    public List<? extends AbstractValueItem> getItems(){
    	return values;
    }
    
    @Override
    public Iterator<SymbolValueItem> iterator() {
        
    	return new Iterator<SymbolValueItem>() {
            
            private Iterator<SymbolValueItem> valuesIt  = values.iterator();
            @Override
            public boolean hasNext() {
                return valuesIt.hasNext();
            }

            @Override
            public SymbolValueItem next() {
                return valuesIt.next();
            }

            @Override
            public void remove() {
                valuesIt.remove();
            }
        };
    }    
    	

	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}



}
