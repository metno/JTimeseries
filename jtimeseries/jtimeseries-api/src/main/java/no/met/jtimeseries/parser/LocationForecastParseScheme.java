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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;
import no.met.halo.common.LogUtils;
import no.met.jtimeseries.chart.TimePeriod;
import no.met.jtimeseries.chart.Utility;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.SymbolPhenomenon;
import no.met.phenomenen.TextPhenomenon;
import no.met.phenomenen.weatherapi.PhenomenonName;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class LocationForecastParseScheme extends ParseScheme {
    
    private static final Logger logger = Logger.getLogger(LocationForecastParseScheme.class.getSimpleName());

    private DateFormat dateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
    private Date timeFrom;
    private Date timeTo;

    private GenericDataModel model;
    private TimePeriod forecastPeriod;

    
    public LocationForecastParseScheme() {
        model = new GenericDataModel();
    	forecastPeriod = null;
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public LocationForecastParseScheme(Date start, Date end) {
        
        model = new GenericDataModel();
    	forecastPeriod = new TimePeriod(start, end);
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    	
    }
    
    public LocationForecastParseScheme(TimePeriod timePeriod) {
        model = new GenericDataModel();
    	this.forecastPeriod = timePeriod;
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    
    public LocationForecastParseScheme(int hours) {
        
        model = new GenericDataModel();
    	forecastPeriod = new TimePeriod(new Date(), hours);
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public GenericDataModel getModel() {
        return model;
    }

    public void setModel(GenericDataModel model) {
        this.model = model;
    }
    
    /**
     * Parse the XML stream from api.met.no with provided parameters
     *
     * @throws ParseException
     */
    @Override
    public void parse(Document document) throws ParseException {
        //Document document = openDataSource(getURLWithAllParameter().toString());
        xmlTreeWalk(document);
    }

    /**
     * Parse a api.met.no XML document that is stored in a file. This function
     * is meant to be use for testing to prevent data to change while you test.
     *
     * @param filename The file:// url the file you want to parse.
     * @throws ParseException
     */
    public void parseFromFile(String filename) throws ParseException {
        Document document = openDataSource(filename);
        xmlTreeWalk(document);
    }

    public void xmlTreeWalk(Document document) throws ParseException {
        xmlTreeWalk(document.getRootElement());
    }

    private boolean inWantedTimeFrame() {
    	if ( forecastPeriod == null )
    		return true;
    	
    	return forecastPeriod.inside(timeFrom) && forecastPeriod.inside(timeTo);
    }
    
    public void xmlTreeWalk(Element element) throws ParseException {
        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node node = element.node(i);
            if (node instanceof Element) {
                if (((Element) node).getName().equals("time")) {
                    timeFrom = dateFormat.parse(((Element) node).attributeValue("from"));
                    timeTo = dateFormat.parse(((Element) node).attributeValue("to"));                    
                }
                // read phenomena if it is inside forecast-period
                else if (inWantedTimeFrame())
                    readPhenomenen((Element) node);
                xmlTreeWalk((Element) node);
            }
        }
    }

    // parse everthing in one go
    private void readPhenomenen(Element node) throws  ParseException {
        String name = node.getName();
        // use simple try-catch and throws parse exception approach in order to synergize with netcdf parser. 
        //Because it might possible that the MissingParamter error is not valid in case of netcdf files
        try {
            if (name.equalsIgnoreCase("temperature")) {                
                addUnitAwareGenericNumberPhenomenon(PhenomenonName.AirTemperature, "value", node);
            } else if (name.equalsIgnoreCase("dewpointTemperature")) {    //                
                addUnitAwareGenericNumberPhenomenon(PhenomenonName.dewPointTemperature, "value", node);
            } else if (name.equalsIgnoreCase("pressure")) {    //            
                addUnitAwareGenericNumberPhenomenon(PhenomenonName.Pressure, "value", node);
            } else if (name.equalsIgnoreCase("windDirection")) {

                addGenericNumberPhenomenon(PhenomenonName.WindDirectionDegree, "deg", node);
                addGenericTextPhenomenon(PhenomenonName.WindDirectionName, "name", node);

            } else if (name.equalsIgnoreCase("windSpeed")) {

                addGenericNumberPhenomenon(PhenomenonName.WindSpeedMPS, "mps", node);        	
                addGenericNumberPhenomenon(PhenomenonName.WindSpeedBeaufort, "beaufort", node);
                addGenericTextPhenomenon(PhenomenonName.WindSpeedName, "name", node);

            } else if (name.equalsIgnoreCase("precipitation")) {

                if (node.attributeValue("maxvalue") != null) {
                    addMultipleTimeResolutionNumberPhenomenon(PhenomenonName.PrecipitationMax, "maxvalue", node);
                }                
                if (node.attributeValue("minvalue") != null){
                    addMultipleTimeResolutionNumberPhenomenon(PhenomenonName.PrecipitationMin, "minvalue", node);            	
                }

                addMultipleTimeResolutionNumberPhenomenon(PhenomenonName.Precipitation, "value", node);            


            } else if (name.equalsIgnoreCase("symbol")) {

                int timeResolution = Utility.hourDifference(timeFrom, timeTo);
                // until we can remove multiple time resolution phenomenon
                if (!model.isExist(PhenomenonName.WeatherSymbols.nameWithResolution(timeResolution)))  {

                    SymbolPhenomenon weatherSymbols = new SymbolPhenomenon();                 
                    weatherSymbols.addValue(timeFrom, timeTo, Integer.parseInt(node.attributeValue("number")));
                    model.addPhenomenen(PhenomenonName.WeatherSymbols.nameWithResolution(timeResolution), weatherSymbols);                

                }  else {
                    model.getSymbolPhenomenon(PhenomenonName.WeatherSymbols.nameWithResolution(timeResolution)).addValue(timeFrom, timeTo, Integer.parseInt(node.attributeValue("number")));                
                }               


            } else if (name.equalsIgnoreCase("cloudiness")) {
                addGenericNumberPhenomenon(PhenomenonName.Cloudiness, "percent", node);
            } else if (name.equalsIgnoreCase("fog")) {
                addGenericNumberPhenomenon(PhenomenonName.Fog, "percent", node);
            } else if (name.equalsIgnoreCase("lowClouds")) {
                addGenericNumberPhenomenon(PhenomenonName.LowCloud, "percent", node);
            } else if (name.equalsIgnoreCase("mediumClouds")) {
                addGenericNumberPhenomenon(PhenomenonName.MediumCloud, "percent", node);
            } else if (name.equalsIgnoreCase("highClouds")) {
                addGenericNumberPhenomenon(PhenomenonName.HighCloud, "percent", node);
            }
        }
        catch (NullPointerException npe) {
            throw new ParseException("The parameter ["+name+"] is mising data.", 0);
        }

    }
    
    private void addGenericNumberPhenomenon(PhenomenonName phenomenonType, String attributeName, Element node ) {
    	addGenericNumberPhenomenon(phenomenonType.toString(), attributeName, node);
    }
    
    private void addGenericNumberPhenomenon(String phenomenonName, String attributeName, Element node ) {
        if (!model.isExist(phenomenonName))  {
        	NumberPhenomenon phenom = new NumberPhenomenon();           
            phenom.addValue(timeFrom, timeTo, new Double(node.attributeValue(attributeName)).doubleValue());
            model.addPhenomenen(phenomenonName, phenom);                
        }  else {
            model.getPhenomenen(phenomenonName, 
            		NumberPhenomenon.class).addValue(timeFrom, timeTo, new Double(node.attributeValue(attributeName)).doubleValue());
        }            
    }    
    
    private void addUnitAwareGenericNumberPhenomenon(PhenomenonName phenomenonType, String attributeName, Element node ) {
    	addUnitAwareGenericNumberPhenomenon(phenomenonType.toString(), attributeName, node);
    }
    
    private void addUnitAwareGenericNumberPhenomenon(String phenomenonName, String attributeName, Element node ) {
        if (!model.isExist(phenomenonName))  {
        	NumberPhenomenon phenom = new NumberPhenomenon();   
            phenom.setUnit(node.attributeValue("unit"));
            phenom.addValue(timeFrom, new Double(node.attributeValue(attributeName)).doubleValue());
            model.addPhenomenen(phenomenonName, phenom);                
        }  else {
            model.getPhenomenen(phenomenonName, 
            		NumberPhenomenon.class).addValue(timeFrom, new Double(node.attributeValue(attributeName)).doubleValue());
        }            
    }  
    
    public void addGenericTextPhenomenon(PhenomenonName phenomenonName, String attributeName, Element node ) {    	
        if (!model.isExist(phenomenonName.toString()))  {                        
            TextPhenomenon phenom = new TextPhenomenon();          
            phenom.addValue(timeFrom, node.attributeValue(attributeName));
            model.addPhenomenen(phenomenonName.toString(), phenom);                
            
        }  else {
            model.getTextPhenomenon(phenomenonName.toString()).addValue(timeFrom, node.attributeValue(attributeName));                
        }
    }
    
    public void addMultipleTimeResolutionNumberPhenomenon(PhenomenonName phenomenonName, String attributeName, Element node ) {    	
        int timeResolution = Utility.hourDifference(timeFrom, timeTo);       
        // until we can remove multiple time resolution phenomenon
        if (!model.isExist(phenomenonName.nameWithResolution(timeResolution)))  {
            
            NumberPhenomenon phenom = new NumberPhenomenon();          
            phenom.addValue(timeFrom, timeTo, new Double(node.attributeValue(attributeName)));
            model.addPhenomenen(phenomenonName.nameWithResolution(timeResolution), phenom);                
            
        }  else {
            model.getNumberPhenomenon(phenomenonName.nameWithResolution(timeResolution)).addValue(timeFrom, timeTo, new Double(node.attributeValue(attributeName)));                
        }  
    }
    

    // this is takem from xml pars old class
    /**
     * Open the XML stream
     *
     * @param url
     * @return
     */
    
    public Document openDataSource(String url) {
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(url);
        } catch (DocumentException e) {
            LogUtils.logException(logger, "Failed to parse '" + url + "': " + e.getMessage(), e);
        }
        return document;
    }  
}
