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
import no.met.jtimeseries.data.item.TextValueItem;

/**
 * Class used for phenomenon that are described by text. This could for instance
 * be wind speed or wind direction.
 */
public class TextPhenomenon extends AbstractPhenomenon implements Jsonizer, Iterable<TextValueItem> {

	private List<TextValueItem> items;
	
	public TextPhenomenon(){
		items = new ArrayList<TextValueItem>();
	}
	
    public void addValue(Date time, String value) {   	
    	this.items.add(new TextValueItem(time, value));
    }	
    
    public void addValue(Date timeFrom, Date timeTo, String value) {   	
    	this.items.add(new TextValueItem(timeFrom, timeTo, value));
    }	    
	
    @Override
    public void cutOlderThan(Date d) {
    	List<TextValueItem> newItems = new ArrayList<TextValueItem>();
    	for ( TextValueItem item : items )
    		if ( ! item.getTimeTo().after(d) )
    			newItems.add(item);
    	items = newItems;
    }
    
    @Override
    public List<Date> getTimes() {
    	
    	List<Date> times = new ArrayList<Date>();
    	for(TextValueItem item : items ){
    		times.add(item.getTimeFrom());
    	}
    	return times;
    }
    
    @Override
    public List<? extends AbstractValueItem> getItems(){
    	return items;
    }

    public List<String> getValue() {
    	
    	List<String> values = new ArrayList<String>();
    	for( TextValueItem item : items ){
    		values.add(item.getValue());
    	}
    	
        return values;
    }

  
    public void setUnit(String unit) {
        this.unit = unit;
    }    

    public void clear() {
    	items.clear();
    }    
    
    @Override
    public JSONObject toJSON() {
        JSONObject json = JSONObject.fromObject(items);
        return json;
    }

    @Override
    public Iterator<TextValueItem> iterator() {
        
    	return new Iterator<TextValueItem>() {
            
            private Iterator<TextValueItem> itemsIt  = items.iterator();
            @Override
            public boolean hasNext() {
                return itemsIt.hasNext();
            }

            @Override
            public TextValueItem next() {
                return itemsIt.next();
            }

            @Override
            public void remove() {
                itemsIt.remove();
            }
        };
    }       
    
}
