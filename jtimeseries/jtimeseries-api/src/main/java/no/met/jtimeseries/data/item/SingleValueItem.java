
package no.met.jtimeseries.data.item;

import java.util.Date;

/**
 * A class to represent single value item such as temperature etc. 
 */
public class SingleValueItem {
    
    private final Date time;
	private final double value;
	
	public SingleValueItem(Date time, double value) {
		this.time = time;
		this.value = value;
	}

	public Date getTime() {
		return time;
	}

    public double getValue() {
        return value;
    }	
    
}