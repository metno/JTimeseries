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
package no.met.jtimeseries;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class LocationForecastValidator implements Validator {
    private final Schema schema;
    
    public LocationForecastValidator(Schema schema) {
        this.schema = schema;
    }
    
    @Override
    public void validate(Source source) throws ValidationException {
        javax.xml.validation.Validator validator = schema.newValidator();
        validator.setErrorHandler(new LocationForecastValidator.LocationForecastErrorHandler());
        try {
            validator.validate(source);
        } catch (SAXException ex) {
            Logger.getLogger(LocationForecastValidator.class.getName()).log(Level.WARNING, 
                "Error while validating location forecast schema", ex);
            throw new ValidationException(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(LocationForecastValidator.class.getName()).log(Level.WARNING, 
                "Error while getting location forecast schema", ex);
            throw new ValidationException(ex.getMessage());
        }
    }
    
    private static class LocationForecastErrorHandler implements ErrorHandler {

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
    
    }
    
}
