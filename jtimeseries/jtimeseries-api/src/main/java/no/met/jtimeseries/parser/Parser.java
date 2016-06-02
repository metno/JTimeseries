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

import no.met.jtimeseries.data.model.GenericDataModel;

/**
 * The most basic interface to parsing any kind of file
 * @author vegardb
 *
 */
public interface Parser {

	/**
	 * Open and read the document at the given location 
	 * 
	 * @param resource location of the document to read
	 * 
	 * @return the extracted data for the document
	 * 
	 * @throws IOException on errors when opening or downloading the document
	 * @throws ParseException if unable to parse the obtained document 
	 */
	public GenericDataModel parse(String resource) throws ParseException, IOException;
}
