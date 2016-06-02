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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;
import no.met.halo.common.LogUtils;
import static no.met.jtimeseries.Constant.JTIMESERIES_ENV;

import no.met.jtimeseries.Location;

public class LocationForecastAddressFactory {

    private static final Logger logger = Logger.getLogger(LocationForecastAddressFactory.class.getSimpleName());
    
    public static URL getURL(Location location) {
    	
        URL url = null;

        StringBuilder sBuffer = new StringBuilder("lat=");
        try {
            sBuffer.append(location.getLatitude());
            sBuffer.append(";lon=");
            sBuffer.append(location.getLongitude());
            if (location.getAltitude() != -10000) {
                sBuffer.append(";msl=");
                sBuffer.append((int) location.getAltitude());
            }
            
            // read datasource information from configuration file
            no.met.halo.common.ConfigUtils cfg = new no.met.halo.common.ConfigUtils("/config/jtimeseries.properties",
                    JTIMESERIES_ENV);            
            String scheme = cfg.getRequired("datasource.scheme");
            String server = cfg.getRequired("datasource.server");
            String port = cfg.getRequired("datasource.port");
            String path = cfg.getRequired("datasource.meteogram.path");
            
            URI uri = new URI(scheme, server+":"+port, path, sBuffer.toString(), null);
            url = uri.toURL();
            
        } catch (MalformedURLException ex) {
            LogUtils.logException(logger, ex.getMessage(), ex);
        } catch (URISyntaxException ex) {
            LogUtils.logException(logger, ex.getMessage(), ex);
        }

        return url;    	
    	    	
    }
}
