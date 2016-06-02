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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;
import no.met.jtimeseries.data.item.AbstractValueItem;
import no.met.phenomenen.filter.ItemFilter;

/**
 * Base class for all phenomenons.
 */
public abstract class AbstractPhenomenon {
    
    protected String name;
    protected String unit; 
    
    public abstract List<Date> getTimes();
    
    public abstract List<? extends AbstractValueItem> getItems();
    
    /**
     * @param index The 0 based index of the item.
     * @return The item at specified index. Null if it does not exist
     */
    public AbstractValueItem getItem(int index){
        return getItems().get(index);
    }    
    
	/**
	 * @return The name of the phenomenon or if the phenomenon does not have a
	 *         name it returns the name of the implementing class.
	 */
    public String getPhenomenonName() {
        if (this.name != null) {
            return this.name;
        }
        return this.getClass().getSimpleName();
    }

    /**
     * @return The unit of the phenomenon if it has a unit. Otherwise it return
     * and empty string.
     */
    public String getPhenomenonUnit() {
        if (this.unit != null) {
            return this.unit;
        }
        return "";
    }
	
    /**
     * Get time of first value
     */
    public Date getStartTime(){
        Date first = null;
        List<Date> times = getTimes();
        for (Date date : times) {
            if (first == null || date.before(first))
                first = date;
        }
        return first;       
    }

    /**
     * Get time of last value
     */
    public Date getEndTime() {

        Date last = null;
        List<Date> times = getTimes();
        for (Date date : times) {
            if (last == null || date.after(last))
                last = date;
        }
        return last;        
        
    }

    public Date getLastToTime() {
        List<? extends AbstractValueItem> items = getItems();
        
        Date last = null;
        for(AbstractValueItem item : items ){
            if (last == null || item.getTimeTo().after(last))
                last = item.getTimeTo();
            
        }
        return last;
    }
    
    /**
     * Remove all data older that the given parameter
     * @param The maximum age of data to still exist in object after invocation of this method
     */
    public abstract void cutOlderThan(Date d);
    
    /**
     * Remove items based on filter object.
     * @param filter The filter used to decide if items should be removed.
     */
    public void filter(ItemFilter filter){
        
        List<? extends AbstractValueItem> items = getItems();
        Iterator<? extends AbstractValueItem> it = items.iterator();
        while(it.hasNext()){
            
            AbstractValueItem item = it.next();
            if(filter.removeItem(item)){
                it.remove();
            }
        }              
    }  
	
    public abstract JSONObject toJSON();
}
