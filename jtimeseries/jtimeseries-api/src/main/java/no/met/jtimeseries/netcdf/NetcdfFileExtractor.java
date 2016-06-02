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
package no.met.jtimeseries.netcdf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;
import no.met.halo.common.LogUtils;

import no.met.jtimeseries.Location;
import no.met.jtimeseries.netcdf.projection.GridLocation;
import no.met.jtimeseries.netcdf.projection.LocationConverter;
import no.met.jtimeseries.netcdf.projection.LocationConverterFactory;
import no.met.jtimeseries.netcdf.timeconvert.InvalidUnitSpecificationException;
import no.met.jtimeseries.netcdf.timeconvert.TimeProvider;
import no.met.jtimeseries.netcdf.timeconvert.TimeProviderFactory;
import no.met.phenomenen.NumberPhenomenon;

import org.jfree.util.Log;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class NetcdfFileExtractor {

	private static final Logger logger = Logger.getLogger(NetcdfFileExtractor.class.getName());
	private NetcdfFile ncFile;
	
	private HashSet<String> variables = new HashSet<String>();
	private List<Date> validTimes = null;

	public NetcdfFileExtractor(URL resource) throws IOException {
		ncFile = NetcdfDataset.openDataset(resource.toString());
		process();		
	}
	
	public NetcdfFileExtractor(String resource) throws FileNotFoundException {
		try {
			ncFile = NetcdfDataset.openDataset(resource);
		} catch (IOException e) {
			throw new FileNotFoundException("Unable to locate "+resource);
		}
		process();
	}
	
	private void process() {
		HashSet<String> dimensions = new HashSet<String>(); 
		for ( Dimension d : ncFile.getDimensions() )
			dimensions.add(d.getName());

		for ( Variable v : ncFile.getVariables() ) {
			// skip dimension variables, and variables without dimensions
			if ( ! dimensions.contains(v.getName()) && ! v.getDimensions().isEmpty() ) {  
				variables.add(v.getName());
			}
		}
	}
	
	public Set<String> getVariables() {
		return variables;
	}

	public Set<String> getVariablesWithStandardName(String standardName) {
		Set<String> ret = new TreeSet<String>();
		for ( Variable v : ncFile.getVariables() ) {
			Attribute currentStandardName = v.findAttribute("standard_name");
			if ( currentStandardName != null && currentStandardName.getStringValue().equals(standardName) )
				ret.add(v.getName());
		}
		return ret;
	}
	
	public Set<String> getVariablesWithCfRole(String cfRole) {
		Set<String> ret = new TreeSet<String>();
		for ( Variable v : ncFile.getVariables() ) {
			Attribute currentStandardName = v.findAttribute("cf_role");
			if ( currentStandardName != null && currentStandardName.getStringValue().equals(cfRole) )
				ret.add(v.getName());
		}
		return ret;	
	}
	
	public boolean hasVariable(String name) {
		return variables.contains(name);
	}

	public Variable getTimeDimension() {
		for ( Dimension d : ncFile.getDimensions() ) {
			Variable t = ncFile.findVariable(d.getName());
			Attribute axis = t.findAttribute("axis");
			if ( axis == null ) {
				if ( t.getName().equals("time") ) {
					return t;
				}						
			}
			else if (axis.isString() && axis.getStringValue().equals("T") ) {
				return t;
			}
		}
		return null; // not found
	}
	
	public List<Date> getValidTimes() throws ParseException, IOException {
		if ( validTimes == null ) {
			Variable time = getTimeDimension();
			if ( time == null )
				throw new ParseException("Unable to find time dimension in data", 0);
			
			validTimes = new Vector<Date>();
			Array data = time.read();
			if ( data.getSize() <= 1 )
				throw new ParseException("Time dimension has size " + data.getSize(), 0);
			TimeProvider timeProvider;
			try {
				timeProvider = TimeProviderFactory.getTimeProvider(time.getUnitsString());
			}
			catch ( InvalidUnitSpecificationException e ) {
				// TODO: better handling of this
				System.err.println(e.getMessage());
				timeProvider = TimeProviderFactory.getDefaultTimeProvider();
			}
			for ( int i = 0; i < data.getSize(); i ++ ) {
				long val = data.getLong(i);
				validTimes.add(timeProvider.getDate(val));
			}
		}
		return validTimes;
	}

	private GridLocation getGridLocation(Location location) throws ParseException, IOException {
		LocationConverter converter = LocationConverterFactory.get(ncFile);
		GridLocation gridLocation = converter.convert(location.getLongitude(), location.getLatitude());
		return gridLocation;
	}
	
	private List<Range> getRange(Variable variable, Range[] ranges) throws ParseException, IOException {
		Vector<Range> ret = new Vector<Range>();
		ret.add(new Range(getValidTimes().size()));
		if ( ranges != null )
			for ( int i = 0; i < ranges.length; i ++ )
				ret.add(ranges[i]);

		return ret;
	}
	
	private List<Range> getRange(Variable variable, GridLocation gridLocation, Range[] ranges) throws ParseException, IOException {

		Vector<Range> ret = new Vector<Range>();
		try {
			ret.add(new Range(getValidTimes().size()));

			if ( ranges != null )
				for ( int i = 0; i < ranges.length; i ++ )
					ret.add(ranges[i]);

			if ( gridLocation != null && variable.getDimensions().size() > ret.size() ) {
				int x = (int) Math.round(gridLocation.getX());
				int y = (int) Math.round(gridLocation.getY());
				
				ret.add(new Range(y, y, 1));
				ret.add(new Range(x, x, 1));
			}
		}
		catch ( InvalidRangeException e ) {
            LogUtils.logException(logger, "Range error for variable: " + variable.getName(), e);
		} 
		
		return ret;
	}

	List<Double> getValues(String variable, Location location, Range[] ranges) throws IOException, ParseException, InvalidRangeException {
		Variable v = ncFile.findVariable(variable);
		if ( v == null )
			return null;
		
		Array data = null;
		if ( location == null) {
			List<Range> rangeList = getRange(v, ranges);
			if ( v.getRanges().size() != rangeList.size() ) {
				
				String signature = variable;
				for ( Range r : v.getRanges() )
					signature += "[" + r.getName() + "]";
				
				String wanted = "";
				for ( Range r : rangeList )
					wanted += "[" + r.getName() + "]";
				
				throw new ParseException("Unable to handle extra implicit dimensions in variable. (" + signature + ") vs " + wanted, 0);
			}
			data = v.read(rangeList);
		}
		else {
			GridLocation gridLocation = getGridLocation(location);
			if ( gridLocation != null ) {
				List<Range> r = getRange(v, gridLocation, ranges);
				data = v.read(r);
			}
			else 
				return null;
		}

		Vector<Double> ret = new Vector<Double>();
		for ( int i = 0; i < data.getSize(); i ++ ) {
			double val = data.getDouble(i);
			ret.add(new Double(val));
		}
		return ret;
	}
	
	public List<String> getStringValues(String variable) {
		Vector<String> ret = new Vector<String>();
		
		Variable v = ncFile.findVariable(variable);
		if ( v == null )
			return null;

		List<Range> ranges = v.getRanges();
		if ( ranges.size() != 2 )
			return null;
		Range placeIndexes = ranges.get(0);
		
		Range.Iterator iter = placeIndexes.getIterator();
		while (iter.hasNext()) {
			try {
				int index = iter.next();
				Vector<Range> readRange = new Vector<Range>();
				readRange.add(new Range(index, index));
				readRange.add(ranges.get(1));
				
				Array data = v.read(readRange);
				String newData = new String(data.getDataAsByteBuffer().array()).trim();
				ret.add(newData);
			}
			catch ( Exception e ) {
				System.err.println(e.getMessage());
			}
			
		}
		
		
//		Array data = null;
//		try {
//			if ( location == null)
//				data = v.read(getRange(v, ranges));
//			else {
//				GridLocation gridLocation = getGridLocation(location);
//				if ( gridLocation != null ) {
//					List<Range> r = getRange(v, gridLocation, ranges);
//					data = v.read(r);
//				}
//				else 
//					return null;
//			}
//		}
//		catch ( InvalidRangeException e )
//		{
//			throw new ParseException(e.getMessage(), 0);
//		}
//
//		Vector<Double> ret = new Vector<Double>();
//		for ( int i = 0; i < data.getSize(); i ++ ) {
//			double val = data.getDouble(i);
//			ret.add(new Double(val));
//		}
//		return ret;		
		
//		ret.add("Aavatsmarkbreen");
//		ret.add("Chydeniusbreen");
//		ret.add("Comfortlessbreen");
//		ret.add("Etonbreen");
//		ret.add("Fridtjofbreen");
//		ret.add("Hansbreen");
//		ret.add("Hayesbreen");
//		ret.add("Kongsvegen");
//		ret.add("Kuhrbreen");
//		ret.add("Monacobreen");
//		ret.add("Raudfjordbreen");
//		ret.add("Uversbreen");
//		ret.add("Veteranbreen");
		
		return ret;
	}

	private String getAttribute(String variable, String attribute) {
		Variable v = ncFile.findVariable(variable);
		if ( v == null )
			return null;
		
		Attribute a = v.findAttribute(attribute);
		if ( a == null )
			return null;
		
		return a.getStringValue();
	}
	
	public String getName(String variable) {
		String ret = getAttribute(variable, "long_name");
		if ( ret == null )
			ret = getAttribute(variable, "standard_name");
		if ( ret == null )
			ret = variable;
		return ret;		
	}
	
	private String getName(String variable, Range[] ranges) {
		if ( ranges == null )
			return getName(variable);
		else {
			if ( ranges.length == 1 ) {
				Set<String> cfRole = getVariablesWithCfRole("timeseries_id");
				if ( cfRole.size() == 1 ) {
					String role = cfRole.iterator().next();
					List<String> names = getStringValues(role);
					
					String ret = names.get(ranges[0].first());
					return ret;
				}
			}

			String ret = variable;
			for ( int i = 0; i < ranges.length; i ++ )
				ret += '[' + Integer.toString(ranges[i].first()) + ']';
			return ret;
		}
	}
	
	String getUnit(String variable) {
		return getAttribute(variable, "units");
	}
	
	public NumberPhenomenon getPhenomenon(String variable, Location location, Range[] ranges) throws ParseException, IOException, InvalidRangeException {

		List<Date> times = getValidTimes();
		List<Double> values = getValues(variable, location, ranges);
				
		if ( values == null ) {
			Log.warn("Requested data outside grid");
			values = Collections.nCopies(times.size(), Double.NaN);
		}
		
		if ( times.size() != values.size() )
			throw new IllegalArgumentException("date/varaiable size mismatch");
		
		NumberPhenomenon ret = new NumberPhenomenon(getName(variable, ranges), getUnit(variable));
		for ( int i = 0; i < times.size(); i ++ )
			ret.addValue(times.get(i), values.get(i));
		
		return ret;
	}
}
