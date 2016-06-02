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

import no.met.jtimeseries.chart.TimePeriod;
import no.met.jtimeseries.chart.Utility;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.weatherapi.PhenomenonName;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * A class to parse the data from api.met.no/weather-api/oceanforecast
 */

public class OceanForecastParseScheme extends ParseScheme {
    private static final Logger logger = Logger.getLogger(OceanForecastParseScheme.class.getName());

    private DateFormat dateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
    private Date timeFrom; 
    private GenericDataModel model;
    private TimePeriod forecastPeriod;
    
    
//    public OceanForecastParseScheme(GenericDataModel ofdm) {
//        this.model = ofdm;
//        // to prevent problems when converting between daylight savings hours
//        // and not
//        // we use UTC which does not have this problem.
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//    }
    
    public OceanForecastParseScheme(TimePeriod timePeriod) {
        
        if (model == null)
            model = new GenericDataModel();
        forecastPeriod = timePeriod;
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
        
    public GenericDataModel getOceanForecastDataModel() {
        return model;
    }

    public void setOceanForecastDataModel(GenericDataModel oceanForecastDataModel) {
        this.model = oceanForecastDataModel;
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

    
    public void xmlTreeWalk(Document document) throws ParseException {
        xmlTreeWalk(document.getRootElement());
    }

    public void xmlTreeWalk(Element element) throws ParseException {
        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node node = element.node(i);
            if (node instanceof Element) {
                if (((Element) node).getName().equals("begin")) {
                    timeFrom = dateFormat.parse(node.getText());
                }
                // read phenomena if it is inside forecast-period
                else if (forecastPeriod.inside(timeFrom))
                    readPhenomenen((Element) node);
                xmlTreeWalk((Element) node);
            }
        }
    }

    // parse everthing in one go
    private void readPhenomenen(Element node) throws ParseException{
        String name = node.getName();
                
        if (name.equalsIgnoreCase("begin")) {
            timeFrom = dateFormat.parse(node.getText());
        } else if (name.equalsIgnoreCase("seaTemperature")) {
            
            if (!model.isExist(PhenomenonName.seaTemperature.toString()))  {
                NumberPhenomenon temperature = new NumberPhenomenon(); 
                temperature.setUnit(node.attributeValue("uom"));
                temperature.addValue(timeFrom, Double.parseDouble(node.getText()));
                model.addPhenomenen(PhenomenonName.seaTemperature.toString(), temperature);                
            }  else {
                model.getPhenomenen(PhenomenonName.seaTemperature.toString(), 
                        NumberPhenomenon.class).addValue(timeFrom, Double.parseDouble(node.getText()));
            }
            
        } else if (name.equalsIgnoreCase("meanTotalWaveDirection")) {
            if (!model.isExist(PhenomenonName.WaveDirection.toString()))  {
            	NumberPhenomenon waveDirection = new NumberPhenomenon(); 
                waveDirection.setUnit(node.attributeValue("uom"));
                waveDirection.addValue(timeFrom, Double.parseDouble(node.getText()));
                model.addPhenomenen(PhenomenonName.WaveDirection.toString(), waveDirection);                
            }  else {
                model.getPhenomenen(PhenomenonName.WaveDirection.toString(),
                        NumberPhenomenon.class).addValue(timeFrom, Double.parseDouble(node.getText()));
            }            
        } else if (name.equalsIgnoreCase("significantTotalWaveHeight")) {
            if (!model.isExist(PhenomenonName.WaveHeight.toString()))  {
            	NumberPhenomenon waveHeight = new NumberPhenomenon();                 
                waveHeight.setUnit(node.attributeValue("uom"));
                waveHeight.addValue(timeFrom, Double.parseDouble(node.getText()));
                model.addPhenomenen(PhenomenonName.WaveHeight.toString(), waveHeight);                
            }  else {
                model.getPhenomenen(PhenomenonName.WaveHeight.toString(), NumberPhenomenon.class)
                        .addValue(timeFrom, Double.parseDouble(node.getText()));
            }
           
        } else if (name.equalsIgnoreCase("seaCurrentDirection")) {
            if (!model.isExist(PhenomenonName.CurrentDirection.toString()))  {
                NumberPhenomenon currentDirection = new NumberPhenomenon(); 
                currentDirection.setUnit(node.attributeValue("uom"));
                currentDirection.addValue(timeFrom, Double.parseDouble(node.getText()));
                model.addPhenomenen(PhenomenonName.CurrentDirection.toString(), currentDirection);                
            }  else {
                model.getPhenomenen(PhenomenonName.CurrentDirection.toString(),
                        NumberPhenomenon.class).addValue(timeFrom, Double.parseDouble(node.getText()));
            }            
        } else if (name.equalsIgnoreCase("seaCurrentSpeed")) {
            if (!model.isExist(PhenomenonName.CurrentSpeed.toString()))  {
                NumberPhenomenon currentSpeed = new NumberPhenomenon(); 
                currentSpeed.setUnit(node.attributeValue("uom"));
                currentSpeed.addValue(timeFrom, Double.parseDouble(node.getText()));
                model.addPhenomenen(PhenomenonName.CurrentSpeed.toString(), currentSpeed);                
            }  else {
                model.getPhenomenen(PhenomenonName.CurrentSpeed.toString(),
                        NumberPhenomenon.class).addValue(timeFrom, Double.parseDouble(node.getText()));
            }            
        } 
    }

    @Override
    public GenericDataModel getModel() {
        return  model;
    }
    
    public void setModel(GenericDataModel model) {
        this.model = model;
    }
}
