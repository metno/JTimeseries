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
package no.met.jtimeseries.marinogram;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import no.met.jtimeseries.data.model.GenericDataModel;

import org.jfree.chart.plot.XYPlot;

/**
 * An abstract Marinogram plot
 */
public abstract class MarinogramPlot {
	private int width;
	private int height;
	private String description;
	protected TimeZone timezone;

	protected Locale locale;
	
	protected String language;
	
	protected ResourceBundle messages;
	
	protected List<Date> shortTermTime=null;
	//The rate to change unit from m/s to knop
	public static final double KNOT=0.514444;
	
	private GenericDataModel locationForecastDataModel;
	private GenericDataModel oceanForecastDataModel;

	public MarinogramPlot(int width, int height, String timezone, String language) {
        this(width, language);
	    this.height = height;
		this.timezone = TimeZone.getTimeZone(timezone);
	}

	public MarinogramPlot(int width, String language) {
		this.width = width;
		this.language = language;
		locale = new Locale(language);
	    messages = ResourceBundle.getBundle("messages", locale);
	}

	public GenericDataModel getLocationForecastDataModel() {
		return locationForecastDataModel;
	}

	public void setLocationForecastDataModel(GenericDataModel locationForecastDataModel) {
		this.locationForecastDataModel = locationForecastDataModel;
	}

	public GenericDataModel getOceanForecastDataModel() {
		return oceanForecastDataModel;
	}

	public void setOceanForecastDataModel(GenericDataModel oceanForecastDataModel) {
		this.oceanForecastDataModel = oceanForecastDataModel;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Add a plot to the marinogram
	 * 
	 * @param plot
	 */
	public abstract void addPlot(MarinogramPlot plot);

	/**
	 * Remove plot from a marinogram
	 * 
	 * @param plot
	 */
	public abstract void removePlot(MarinogramPlot plot);

	/**
	 * Get the plot
	 * 
	 * @return plot
	 * @throws ParseException
	 */
	public abstract XYPlot getPlot() throws ParseException;

	@Override
	public int hashCode() {
		return this.getDescription().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MarinogramPlot other = (MarinogramPlot) obj;
		if ((this.description == null) ? (other.description != null)
				: !this.description.equals(other.description)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return (this.getClass().getSimpleName()) + "[" + this.getDescription()
				+ ", width= " + this.getWidth() + "]";
	}
	
	public List<Date> getShortTermTime(Date startTime){
		if (startTime==null)
			return null;
		if (shortTermTime!=null)
			return shortTermTime;
		shortTermTime=new ArrayList<Date>();
		shortTermTime.add(startTime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(startTime);
		for (int i=0;i<48;i++){
			cal.add(Calendar.HOUR_OF_DAY, 1);
			shortTermTime.add(new Date(cal.getTimeInMillis()));
		}
		return shortTermTime;
	}

}
