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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import net.sf.json.JSONObject;
import no.met.jtimeseries.Jsonizer;
import no.met.jtimeseries.TimeSeriesEnabler;
import no.met.jtimeseries.chart.CardinalSpline;
import no.met.jtimeseries.chart.TimeBase;
import no.met.jtimeseries.chart.Utility;
import no.met.jtimeseries.data.item.AbstractValueItem;
import no.met.jtimeseries.data.item.NumberValueItem;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Class to represent phenomenon that are described by floating point
 * numbers.
 */
public class NumberPhenomenon extends AbstractPhenomenon implements Jsonizer, Cloneable, 
        TimeSeriesEnabler, Iterable<NumberValueItem> {
        
    protected List<NumberValueItem> items;
    
    public NumberPhenomenon() {               
        items = new ArrayList<NumberValueItem>();
    }
    
    public NumberPhenomenon(String name, String unit) {
        this.name = name;
        this.unit = unit;
        items = new ArrayList<NumberValueItem>();
    }

    
    public void addValue(Date time, Double value) {
        this.items.add(new NumberValueItem(time, value));
    }
    
    public void addValue(Date fromTime, Date toTime, Double value){
    	this.items.add(new NumberValueItem(fromTime, toTime, value));
    }
    
    @Override
    public void cutOlderThan(Date d) {
    	List<NumberValueItem> newItems = new ArrayList<NumberValueItem>();
    	for ( NumberValueItem item : items )
    		if ( ! item.getTimeTo().after(d) )
    			newItems.add(item);
    	items = newItems;
    }

    /**
     * Gets value of the specified time d
     * Returns null if no value is found.
     */
    public Double getValueByTime(Date d) {
    	
    	for( NumberValueItem item : items ){
    		if( item.getTimeFrom().equals(d)){
    			return item.getValue();
    		}
    	}
    	
        //if no value is return then calculate the value
        return calculateValueByTime(d);
    }
    
    /**
     * Calculatet the value of the time according to the value 
     * at the previous time and the value at the next time
     * @param d
     * @return
     */
    private Double calculateValueByTime(Date d){
   	
    	NumberValueItem nextItem = null;
    	NumberValueItem preItem = null;
    	
    	for( int i = 0; i < items.size(); i++ ){
    		NumberValueItem item = items.get(i);
    		
    		if( item.getTimeFrom().after(d) ){
    			nextItem = item;
    			if( i > 0 ) {
    				preItem = items.get(i-1);
    			}
    		}
    	}
    	
    	if( nextItem != null && preItem != null ){
    		double valuePre = preItem.getValue();
    		double valueNext = nextItem.getValue();
    		double slope = (nextItem.getTimeFrom().getTime() - preItem.getTimeFrom().getTime()) / (valueNext - valuePre);
    		return (d.getTime() - preItem.getTimeFrom().getTime())/slope + valuePre;
    	}
    	return null;
    	
    }

    public List<Date> getTime() {
    	
    	List<Date> times = new ArrayList<Date>();
    	for( NumberValueItem item : items){
    		times.add(item.getTimeFrom());
    	}
    	
        return times;
    }
    
    @Override
    public List<Date> getTimes() {
        return getTime();
    }    

    public List<Double> getValue() {
    	
    	List<Double> values = new ArrayList<Double>();
    	for( NumberValueItem item : items ){
    		values.add(item.getValue());
    	}
    	
        return values;
    }

   
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public double getMaxValue(){
    	
    	if( items.isEmpty()){
    		return 0;
    	}
    	
    	double ret = -Double.MAX_VALUE;
    	for ( NumberValueItem d : items )
    		if ( d.getValue() > ret )
    			ret = d.getValue();
    	
    	return ret; 
    }
    
    public double getMinValue(){
    	
    	if( items.isEmpty()){
    		return 0;
    	}
    	
    	double ret = Double.MAX_VALUE;
    	for ( NumberValueItem d : items )
    		if ( d.getValue() < ret )
    			ret = d.getValue();

    	return ret;
    }

    public void clear() {
    	items.clear();
    }

    @Override
    public JSONObject toJSON() {
        SimpleDateFormat sdf = new SimpleDateFormat(Utility.DATE_FORMAT); 
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));        
        Map<String, Double> jsonMap = new HashMap<>(items.size());
        for (NumberValueItem nvi : items) {              
            calendar.setTime(nvi.getTimeFrom());
            jsonMap.put(sdf.format(calendar.getTime()), nvi.getValue());
        }
        jsonMap = new TreeMap<>(jsonMap);        
        JSONObject json = JSONObject.fromObject(jsonMap);
        return json;
    }

   
    /**
     * Do cadinal spline
     * @param tension
     */
    public void doSpline(double tension) {
    	
    	if( items.isEmpty() ){
    		return;
    	}
    	
    	NumberValueItem firstItem = items.get(0);
    	if( !firstItem.getTimeFrom().equals(firstItem.getTimeTo())){
    		String msg = "Spline calculatations are not supported on phenomenon where a single value";
    		msg += " have different from and to time since the spline calculations cannot deal with the toTime.";
    		throw new UnsupportedOperationException(msg);
    	}
    	
        // add cardinal control points before rendering with standard
		// spline
		CardinalSpline cs = new CardinalSpline(this.items, tension);
		this.items = cs.cardinalSpline();
    }
    
    /**
     * Do cadinal spline first, then do standard spline
     * @param tension
     * @param precision
     */
    public void doHybridSpline(double tension, int precision) {
        doSpline(tension);
        if (precision <= 0) {
            throw new IllegalArgumentException("Requires precision > 0.");
        }
        if (this.items.size()<=2) {
            throw new IllegalArgumentException("NumberPhenomenon size should be larger than 2.");
        }
        List<NumberValueItem> splinedItems=new ArrayList<>();
        splinedItems.add(this.items.get(0));
        int np = this.items.size(); // number of points
        double[] d = new double[np]; // Newton form coefficients
        double[] x = new double[np]; // x-coordinates of nodes
        double y;
        double t;
        double oldy = 0;
        double oldt = 0;

        double[] a = new double[np];
        double t1;
        double t2;
        double[] h = new double[np];

        for (int i = 0; i < np; i++) {
            //ControlPoint cpi = this.points.get(i);
            x[i] = this.items.get(i).getTimeFrom().getTime();
            d[i] = this.items.get(i).getValue();
        }

        for (int i = 1; i <= np - 1; i++) {
            h[i] = x[i] - x[i - 1];
        }
        double[] sub = new double[np - 1];
        double[] diag = new double[np - 1];
        double[] sup = new double[np - 1];

        for (int i = 1; i <= np - 2; i++) {
            diag[i] = (h[i] + h[i + 1]) / 3;
            sup[i] = h[i + 1] / 6;
            sub[i] = h[i] / 6;
            a[i] = (d[i + 1] - d[i]) / h[i + 1]
                       - (d[i] - d[i - 1]) / h[i];
        }
        solveTridiag(sub, diag, sup, a, np - 2);

        // note that a[0]=a[np-1]=0
        // draw
        oldt = x[0];
        oldy = d[0];
        int k=0;
        for (int i = 1; i <= np - 1; i++) {
            // loop over intervals between nodes
            for (int j = 1; j <= precision; j++) {
                t1 = (h[i] * j) / precision;
                t2 = h[i] - t1;
                y = ((-a[i - 1] / 6 * (t2 + h[i]) * t1 + d[i - 1])
                        * t2 + (-a[i] / 6 * (t1 + h[i]) * t2
                        + d[i]) * t1) / h[i];
                t = (long)(x[i - 1] + t1);
                splinedItems.add(new NumberValueItem(new Date((long)t),y));
                oldt = t;
                oldy = y;
            }
        }
        this.items=splinedItems;
    }
    
    /**
     * This method is directly copied from Jfreechart XYSplineRenderer class.
     *
     * @param sub
     * @param diag
     * @param sup
     * @param b
     * @param n
     */
    private void solveTridiag(double[] sub, double[] diag, double[] sup,
            double[] b, int n) {
        int i;
        for (i = 2; i <= n; i++) {
            sub[i] = sub[i] / diag[i - 1];
            diag[i] = diag[i] - sub[i] * sup[i - 1];
            b[i] = b[i] - sub[i] * b[i - 1];
        }
        b[n] = b[n] / diag[n];
        for (i = n - 1; i >= 1; i--) {
            b[i] = (b[i] - sup[i] * b[i + 1]) / diag[i];
        }
    }
    
    public void addThresholdPoints(double threshold) {
    	
    	if( items.isEmpty() ){
    		return;
    	}
    	
    	NumberValueItem firstItem = items.get(0);
    	if( !firstItem.getTimeFrom().equals(firstItem.getTimeTo())){
    		String msg = "Spline calculatations are not supported on phenomenon where a single value";
    		msg += " have different from and to time since the spline calculations cannot deal with the toTime.";
    		throw new UnsupportedOperationException(msg);
    	}    	
    	
        // add threshold point into the dataset
    	this.items = Utility.addThresholdItems(items, threshold);

    }
    
    @Override
    public TimeSeriesCollection getTimeSeriesWithThreshold(String title, TimeBase timeBase, double threshold) {
        
        if (title == null) {
             throw new IllegalArgumentException("Timeseries name can not be null");
         }
         
         if (timeBase == null) {
             throw new IllegalArgumentException("TimeBase can not be null");
         }
         
         if (timeBase == TimeBase.AUTO) {
            timeBase = Utility.autoTimeBaseFromItems(this.items);
        }
         
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
		TimeSeries series = new TimeSeries(title);

		// To make the odd series to be above threshold series and even series
		// to be below threshold series
		// add a empty series if the first point is below threshold
		if (this.items.size() > 0 && this.items.get(0).getValue() < threshold) {
			//series = new TimeSeries(title);
			timeSeriesCollection.addSeries(series);
		}

		for (int i = 0; i < this.items.size();) {

			series = new TimeSeries(title);

			// insert last threshold point
			if (i != 0) {
				NumberValueItem item = items.get(i - 1);
				series.add(Utility.getPeriod(timeBase, item.getTimeFrom()), item.getValue());
			}

			if (this.items.size() > i && this.items.get(i).getValue() >= threshold) {
				while ((i < this.items.size())
						&& this.items.get(i).getValue() >= threshold) {
					NumberValueItem item = items.get(i);
					series.add(Utility.getPeriod(timeBase, item.getTimeFrom()), item.getValue());
					i++;
				}
			} else {
				while ((i < this.items.size())
						&& this.items.get(i).getValue() <= threshold) {
					NumberValueItem item = items.get(i);
					series.add(Utility.getPeriod(timeBase, item.getTimeFrom()), item.getValue());
					i++;
				}

			}
			timeSeriesCollection.addSeries(series);

		}	
		return timeSeriesCollection;

	}
    
    @Override
    public TimeSeriesCollection getTimeSeries(String title, TimeBase timeBase) {
        if (title == null) {
             throw new IllegalArgumentException("Timeseries name can not be null");
         }
         
         if (timeBase == null) {
             throw new IllegalArgumentException("TimeBase can not be null");
         }
         
         TimeSeriesCollection  seriesCollection = new TimeSeriesCollection();
         //List<Date> times = this.timeForBestResolution();
         
         if (timeBase == TimeBase.AUTO) {
            timeBase = Utility.autoTimeBaseFromItems(this.items);
         }
         
         //List<Double> values = this.bestResolutionValues();
         TimeSeries timeSeries = new TimeSeries(title);
         for( NumberValueItem item : items){
        	 timeSeries.add(Utility.getPeriod(timeBase, item.getTimeFrom()), item.getValue());
         }
         seriesCollection.addSeries(timeSeries);         
         
         return seriesCollection;   
    }  
    
    @Override
    public Iterator<NumberValueItem> iterator() {
        return new Iterator<NumberValueItem>() {
            private Iterator<NumberValueItem> itemsIt = items.iterator();
            
            @Override
            public boolean hasNext() {
                return itemsIt.hasNext();
            }

            @Override
            public NumberValueItem next() {
                return itemsIt.next();
            }

            @Override
            public void remove() {
            	itemsIt.remove();
            }
        };
    }

	@Override
	public List<? extends AbstractValueItem> getItems() {		
		return items;
	}

	/**
	 * Scaling up or down the data list with specified rate
	 * @param s The rate to scale data
	 * @return A new data after scaling
	 */
    public NumberPhenomenon scaling(double s) {
        if (items!=null && !items.isEmpty()){
            for (int i=0;i<items.size();i++){
                items.set(i, new NumberValueItem(items.get(i).getTimeFrom(),items.get(i).getTimeTo(),items.get(i).getValue()*s));
            }
        }
        return this;
    }        
    
    /**
     * Transform the data with specified value
     * @param v The value to add to the data
     * @return A new data after transform
     */
    public NumberPhenomenon transform(double v) {
        if (items!=null && !items.isEmpty()){
            for (int i=0;i<items.size();i++){
                items.set(i, new NumberValueItem(items.get(i).getTimeFrom(),items.get(i).getTimeTo(),items.get(i).getValue()+v));
            }
        }
        return this;
    }
    
    public static List<NumberValueItem> cloneList(List<NumberValueItem> items) {
        if (items==null)
            return null;
        List<NumberValueItem> clonedItems=new ArrayList<NumberValueItem>(items.size());
        for (NumberValueItem i:items){
            clonedItems.add((NumberValueItem)i.clone());
        }
        return clonedItems;
    }
    
    /**
     * set negative values to 0
     */
    public void removeNegativeValues() {
        if( items.isEmpty() ){
            return;
        }
        for (NumberValueItem item:items) {
            if (item.getValue()<0)
                item.setValue(0);
        }
    }
    
    @Override
    public NumberPhenomenon clone() {
        try {
            NumberPhenomenon cloned=(NumberPhenomenon)super.clone();
            cloned.items= NumberPhenomenon.cloneList(cloned.items);
            return cloned;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }
    
    public void printItems() {
        if (items==null)
            return;
        for (NumberValueItem item:items) {
            System.out.println(item.toString());
        }
    }
}
