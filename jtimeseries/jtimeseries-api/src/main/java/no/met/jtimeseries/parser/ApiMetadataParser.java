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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import no.met.halo.common.LogUtils;
import no.met.jtimeseries.ApiMetadata;
import no.met.jtimeseries.Location;

/**
 *
 * A class to parse metadata from location forecast api
 */
public final class ApiMetadataParser {
    private static final List<String> acceptedElements;

    static {
        acceptedElements = new ArrayList<>(2);
        acceptedElements.add("location");
        acceptedElements.add("model");
    }
    
    private ApiMetadataParser() {
    }
    
    /**
     * get metadata from location forecast api
     * @param longitude
     * @param latitude
     * @return metadata
     */
    public static ApiMetadata getMetadata(double longitude, double latitude) {
        URL url = LocationForecastAddressFactory.getURL(new Location(longitude, latitude));
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        ApiMetadata metadata = null;
        try (InputStream is = url.openStream()) {              
            XMLEventReader xmler = xmlif.createXMLEventReader(is); 
            XMLEventReader xmlfer = xmlif.createFilteredReader(xmler, new EventFilter() {

                @Override
                public boolean accept(XMLEvent event) {
                    if (event.isStartElement()) {
                        StartElement startElement = event.asStartElement();
                        String name = startElement.getName().getLocalPart();                        
                        if (!acceptedElements.contains(name)) {
                            return false;
                        }
                    }            
                    if (event.isEndElement()) {
                        EndElement endElement = event.asEndElement();
                        String name = endElement.getName().getLocalPart();                        
                        if (!acceptedElements.contains(name)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
            while (xmlfer.hasNext()) {
                XMLEvent event = xmlfer.nextEvent();                
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    switch (startElement.getName().getLocalPart()) {
                        case "model":                               
                            if (metadata == null) {
                                metadata = new ApiMetadata();
                            }    
                            ApiMetadata.ApiModel model = new ApiMetadata.ApiModel();
                            model.setName(startElement.getAttributeByName(new QName("name")).getValue());
                            model.setFrom(startElement.getAttributeByName(new QName("from")).getValue());
                            model.setTo(startElement.getAttributeByName(new QName("to")).getValue());
                            model.setNextRun(startElement.getAttributeByName(new QName("nextrun")).getValue());
                            model.setTermin(startElement.getAttributeByName(new QName("termin")).getValue());
                            model.setRunended(startElement.getAttributeByName(new QName("runended")).getValue());
                            metadata.addModel(model);
                           
                            break;  
                        case "location":                            
                            if (metadata == null) {
                                metadata = new ApiMetadata();
                            }    
                            metadata.setAltitude(Integer.parseInt(startElement.
                                    getAttributeByName(new QName("altitude")).getValue()));
                           
                            break;                        
                    }                    
                }
            }     
                
        }  catch (IOException | XMLStreamException ex) {            
            LogUtils.logException(Logger.getLogger(ApiMetadataParser.class.getName()), "Failed to parse product from" +url.toString(), ex);            
        }
        return metadata;
    }
}
