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
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.met.forecasts.LocationForecast;
import no.met.halo.common.LogUtils;
import no.met.jtimeseries.ApiMetadata;
import no.met.jtimeseries.Location;
import no.met.jtimeseries.chart.Utility;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

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

    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(dateFormat);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        return mapper;
    }

    /**
     * get metadata from location forecast api
     * @param longitude
     * @param latitude
     * @return metadata
     */
    public static ApiMetadata getMetadata(double longitude, double latitude) {
        URL url = LocationForecastAddressFactory.getURL(new Location(longitude, latitude));
        ApiMetadata metadata = new ApiMetadata();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url.toString());
            // add request headers
            request.addHeader(HttpHeaders.USER_AGENT, "halo.met.no");

            try (CloseableHttpResponse response = httpClient.execute(request);) {
                HttpEntity entity = response.getEntity();
                ObjectMapper mapper = createDefaultObjectMapper();
                LocationForecast locationForecast = mapper.readValue(entity.getContent(), LocationForecast.class);
                LocationForecast.Meta meta = locationForecast.getProperties().getMeta();

                ApiMetadata.ApiModel model = new ApiMetadata.ApiModel();
                //hard code to avoid update halo at the moment
                model.setName("EPS");
                //blindly set all run, termin values to updateAt since locationforecast 2 only has updatedAt, this will make halo happy
                model.setNextRun(meta.getUpdatedAt());
                model.setNextRun(meta.getUpdatedAt());
                model.setTermin(meta.getUpdatedAt());
                model.setRunended(meta.getUpdatedAt());
                metadata.setAltitude((int)locationForecast.getGeometry().getCoordinates().get(2));
                metadata.addModel(model);

            }
        } catch (IOException ex) {
            LogUtils.logException(Logger.getLogger(ApiMetadataParser.class.getName()), "Failed to parse product from" +url.toString(), ex);
        }
        return metadata;
    }
}
