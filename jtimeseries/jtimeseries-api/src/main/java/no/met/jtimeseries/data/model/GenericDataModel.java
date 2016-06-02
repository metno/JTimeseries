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
package no.met.jtimeseries.data.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.json.JSONObject;
import no.met.jtimeseries.Jsonizer;
import no.met.phenomenen.AbstractPhenomenon;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.SymbolPhenomenon;
import no.met.phenomenen.TextPhenomenon;

/**
 * A class to hold the different observational phenomena 
 */
public class GenericDataModel extends DataModel implements Jsonizer {

	
	//String is used as key because in case of NETCDF files name of phenomenon might not known in advance. 
    //If it is known in advance then AbstractPhenomenon.Names enum can be used
    Map<String, AbstractPhenomenon> phenomena = new TreeMap<String, AbstractPhenomenon>();
        
    public GenericDataModel() {
        super();
    }
    
    @Override
    public Date getTimeFrom() {
    	
    	Date timeFrom = null;
    	for( Map.Entry<String, AbstractPhenomenon> entry : phenomena.entrySet() ){
    		Date d = entry.getValue().getStartTime();
    		if( timeFrom == null || d.compareTo(timeFrom) < 0 ){
    			timeFrom = d;
    		}
    	}
    	return timeFrom;

    }
    
    @Override
    public Date getTimeTo() {
    	
    	Date timeTo = null;
    	for( Map.Entry<String, AbstractPhenomenon> entry : phenomena.entrySet() ){
    		Date d = entry.getValue().getEndTime();
    		if( timeTo == null || d.compareTo(timeTo) > 0 ){
    			timeTo = d;
    		}
    	}
    	return timeTo;    	
    }
    
    /**
     * Remove all data older that the given parameter
     * @param The maximum age of data to still exist in object after invocation of this method
     */
    public void cutOlderThan(Date d) {
    	for ( AbstractPhenomenon p : phenomena.values() )
    		p.cutOlderThan(d);
    }
    
    /**
     * Add a particular phenomenon to the model
     * @param key
     * @param vbp 
     */
    public void addPhenomenen(String key, AbstractPhenomenon vbp) {
        if (!phenomena.containsKey(key)) {
            phenomena.put(key, vbp);
        } else {
            throw  new IllegalArgumentException("Key "+key+ " already exist.");
        }
    }
    
    public boolean isExist(String key) {
    	
    	if(phenomena.containsKey(key)){
    		return true;
    	}
    	
        return false;
    }
    
    /**
     * Get a list of all contained phenomena
     * @return
     */
    public Set<String> getPhenomena() {
    	return phenomena.keySet();
    }
    
    
    /**
     * Get a particular phenomenon from the model
     * @param <T> type of phenomenon class
     * @param key
     * @param type phenomenon class
     * @return 
     */
    public<T extends AbstractPhenomenon> T getPhenomenen(String key, Class<T> type) {  
        if (type == null) {
            throw new NullPointerException("Type can  not be null");
        }
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Empty key is not allowed");
        }
        if (phenomena.containsKey(key)) {           
            return type.cast(phenomena.get(key));
        }
        return null;
    }    
   
    public NumberPhenomenon getNumberPhenomenon(String key){
    	return getPhenomenen(key, NumberPhenomenon.class);
    }
    
    /**
     * Get a particular phenomenon from the model
     * @param <T> type of phenomenon class
     * @param key
     * @param type phenomenon class
     * @return 
     */
    public TextPhenomenon getTextPhenomenon(String key) {
    	return getPhenomenen(key, TextPhenomenon.class);
    }      
    
    public SymbolPhenomenon getSymbolPhenomenon(String key) {  
    	return getPhenomenen(key, SymbolPhenomenon.class);
    }      
	
	@Override
	public JSONObject toJSON() {
        JSONObject jObject = new JSONObject();
        for ( Map.Entry<String, AbstractPhenomenon> entry : phenomena.entrySet() )
        	jObject.accumulate(entry.getKey(), entry.getValue().toJSON());

        return jObject;
	}
    
}
