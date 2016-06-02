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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * A class to represent location forecast api metadata
 */
@XmlRootElement(name = "metadata")
public class ApiMetadata {
    private int altitude;
    private List<ApiModel> models;

    @XmlElement
    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    @XmlElement(name = "model")
    public List<ApiModel> getModels() {
        return models;
    }

    public void setModels(List<ApiModel> models) {
        this.models = models;
    }
    
    public void addModel(ApiModel model) {
        if (models == null)
            models = new ArrayList<>(2);
        models.add(model);
    }
    
    public static class ApiModel {
        private String name;
        //use string for simplicity, conversion will be performed at consumer side
        private String from;
        private String to;
        private String nextRun;
        private  String termin;
        private String runended;

        @XmlAttribute
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlAttribute        
        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        @XmlAttribute
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }       

        @XmlAttribute(name = "nextrun")
        public String getNextRun() {
            return nextRun;
        }

        public void setNextRun(String nextRun) {
            this.nextRun = nextRun;
        }

        @XmlAttribute(name = "termin")
        public String getTermin() {
            return termin;
        }

        public void setTermin(String termin) {
            this.termin = termin;
        }

        @XmlAttribute(name = "runended")
        public String getRunended() {
            return runended;
        }

        public void setRunended(String runended) {
            this.runended = runended;
        }
        
    }
}
