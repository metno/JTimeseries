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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * Interface and stub method for downloading and parsing locationforecast-like 
 * data
 */
abstract class ParseScheme implements Parser {
    /**
     * Parse the document 
     * @param document
     * @throws ParseException 
     */
    protected abstract void parse(Document document) throws ParseException;
    /**
     * Get the data model correspond to a scheme
     * @return 
     */
    protected abstract GenericDataModel getModel();
    
    
    @Override
	public GenericDataModel parse(String resource) throws ParseException, IOException {

		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(resource);
			parse(document);
		} catch (DocumentException e) {
			throw new IOException(e);
		}
		return getModel();
	}
}
