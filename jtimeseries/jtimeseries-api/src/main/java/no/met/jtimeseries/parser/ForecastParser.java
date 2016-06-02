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
import java.util.logging.Logger;
import no.met.halo.common.LogUtils;

import no.met.jtimeseries.data.model.GenericDataModel;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * A class to parse the data based on the scheme
 */
public class ForecastParser {
    private static final Logger logger = Logger.getLogger(ForecastParser.class.getSimpleName());
    private Parser parser;
    private String resource;
    
    public ForecastParser(Parser parser, String resource) {
    	this.parser = parser;
    	this.resource = resource;
    }
    
    public Document getDocument() {
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(resource);
        } catch (DocumentException e) {
            LogUtils.logException(logger, "Failed to parse " + resource + ": " + e.getMessage(), e);
        }
        return document;
    }
    
    /**
     * Parse the data based on the scheme and then populate the model
     * @return data model
     * @throws ParseException 
     */
    public GenericDataModel populateModelWithData() throws ParseException, IOException {    	
    	return parser.parse(resource);    	
    }
    
}
