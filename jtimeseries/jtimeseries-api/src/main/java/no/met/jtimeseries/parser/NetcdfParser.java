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
package no.met.jtimeseries.parser;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import no.met.jtimeseries.Location;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.jtimeseries.netcdf.NetcdfFileExtractor;
import no.met.jtimeseries.netcdf.ParameterReference;

public class NetcdfParser implements Parser {

	private Location location;
	private Iterable<String> parameters;
	private ParameterReference parameterRefersTo;

	private class NamedDataModel {
		private GenericDataModel model;
		private String title;
		
		public NamedDataModel(GenericDataModel model, String title) {
			this.model = model;
			this.title = title;
		}
		
		public GenericDataModel getModel() {
			return model;
		}
		
		public String getTitle() {
			return title;
		}
	}
	
	private HashMap<String, NamedDataModel> parsed = new HashMap<String, NamedDataModel>();
	
	public NetcdfParser(Location location, Iterable<String> parameters, ParameterReference parameterRefersTo) {
		this.location = location;
		this.parameters = parameters;
		this.parameterRefersTo = parameterRefersTo;
	}

	private String stripRanges(String variable) {
		String[] elements = variable.split("\\[");
		return elements[0];
	}
	
	private int getIndex(String indexSpecifier, NetcdfFileExtractor extractor) throws ParseException 
	{
		try {
			return Integer.parseInt(indexSpecifier);
		}
		catch ( NumberFormatException e ) {
			Set<String> cfRole = extractor.getVariablesWithCfRole("timeseries_id");
			if ( cfRole.size() != 1 )
				throw new ParseException("Unable to understand index element: " + indexSpecifier, 0);
				
			String role = cfRole.iterator().next();
			
			List<String> names = extractor.getStringValues(role);
			for ( int index = 0; index < names.size(); index ++ )
				if ( names.get(index).equals(indexSpecifier) )
					return index;
			
			throw new ParseException("Unable to find index element: " + indexSpecifier, 0);
		}
	}
	
	private Range[] getRanges(String variable, NetcdfFileExtractor extractor) throws ParseException {
		String[] elements = variable.split("\\[");
		if ( elements.length <= 1 )
			return null;
		
		Range[] ret = new Range[elements.length -1];
		for ( int i = 1; i < elements.length; i ++ )
		{
			String[] subElement = elements[i].split("\\]");
			if ( subElement.length != 1 )
				throw new ParseException("Invalid variable specification: " + elements[i], 0);

			int idx = getIndex(subElement[0], extractor);
			try {
				ret[i -1] = new Range(idx, idx);
			}
			catch ( InvalidRangeException e ) {
				throw new ParseException(e.getMessage(), 0);
			}
		}
		
		return ret;
	}
	
//	private Map<String, Range[]> getWantedParameters(NetcdfFileExtractor extractor) throws ParseException {
//		
//		HashMap<String, Range[]> wantedParameters = new HashMap<String, Range[]>();
//
//		if ( parameterRefersTo == ParameterReference.VARIABLE_NAME )
//			for ( String p : parameters )
//				wantedParameters.put(stripRanges(p), getRanges(p));
//		else {
//			for ( String p : parameters ) {
//				for ( String var : extractor.getVariablesWithStandardName(p) )
//					wantedParameters.put(stripRanges(var), getRanges(var));
//			}
//		}
//		
//		return wantedParameters;
//	}
	
	private Set<String> getWantedParameters(NetcdfFileExtractor extractor) {
		HashSet<String> wantedParameters = new HashSet<String>();
		if ( parameterRefersTo == ParameterReference.VARIABLE_NAME )
			for ( String p : parameters )
				wantedParameters.add(p);
		else {
			for ( String p : parameters ) {
				Set<String> variables = extractor.getVariablesWithStandardName(p);
				wantedParameters.addAll(variables);
			}
		}
		return wantedParameters;
	}
	
	@Override
	public GenericDataModel parse(String resource) throws ParseException, IOException {
		return lookupCacheOrParse(resource).getModel();
	}

	public String getTitle(String resource) throws ParseException, IOException {
		return lookupCacheOrParse(resource).getTitle();		
	}

	private NamedDataModel lookupCacheOrParse(String resource) throws ParseException, IOException {
		NamedDataModel model = parsed.get(resource);
		if ( model == null ) {
			model = doParse(resource);
			parsed.put(resource, model);
		}	
		return model;
	}
	
	private NamedDataModel doParse(String resource) throws ParseException, IOException {
		
		NetcdfFileExtractor extractor = new NetcdfFileExtractor(resource);
		
//		LAG MAP AV resource->extractor.getName(variable)
//		FINN EN MÅTE å videreformidle
		
		GenericDataModel model = new GenericDataModel();
		String title = null;
		
		try {
			if ( parameters != null ) {
	
				Set<String> wantedParameters = getWantedParameters(extractor);
	
				for ( String wantedParameter : wantedParameters ) {
					String variableName = stripRanges(wantedParameter);
					if ( extractor.hasVariable(variableName) ) {
						Range[] ranges = getRanges(wantedParameter, extractor);
						model.addPhenomenen(wantedParameter, extractor.getPhenomenon(variableName, location, ranges));
						if ( title == null )
							title = extractor.getName(variableName);
					}
				}
			}
			else {
				for ( String variable : extractor.getVariables() ) {
					model.addPhenomenen(variable, extractor.getPhenomenon(variable, location, null));
					if ( title == null )
						title = extractor.getName(variable);
				}
			}
		}
		catch ( InvalidRangeException e ) {
			throw new ParseException(e.getMessage(), 0);
		}

		return new NamedDataModel(model, title);
	}
}
