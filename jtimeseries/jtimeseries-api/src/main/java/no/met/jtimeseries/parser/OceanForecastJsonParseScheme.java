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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.met.forecasts.OceanForecast;
import no.met.jtimeseries.MeteogramWrapper;
import no.met.jtimeseries.chart.TimePeriod;
import no.met.jtimeseries.chart.Utility;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.SymbolPhenomenon;
import no.met.phenomenen.weatherapi.PhenomenonName;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.dom4j.Document;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

public class OceanForecastJsonParseScheme extends ParseScheme {

    private static final Logger logger = Logger.getLogger(OceanForecastJsonParseScheme.class.getSimpleName());

    private DateFormat dateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
    private Date timeFrom;
    private Date timeTo;

    private GenericDataModel model;
    private TimePeriod forecastPeriod;


    public OceanForecastJsonParseScheme() {
        model = new GenericDataModel();
        forecastPeriod = null;
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public OceanForecastJsonParseScheme(Date start, Date end) {

        model = new GenericDataModel();
        forecastPeriod = new TimePeriod(start, end);
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    }

    public OceanForecastJsonParseScheme(TimePeriod timePeriod) {
        model = new GenericDataModel();
        this.forecastPeriod = timePeriod;
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public OceanForecastJsonParseScheme(int hours) {

        model = new GenericDataModel();
        forecastPeriod = new TimePeriod(new Date(), hours);
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    protected void parse(Document document) throws ParseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenericDataModel parse(String resource) throws IOException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            //"https://api.met.no/weatherapi/Oceanforecast/2.0/complete?lat=60.10&lon=9.58"
            HttpGet request = new HttpGet(resource);
            // add request headers
            request.addHeader(HttpHeaders.USER_AGENT, "halo.met.no");

            try (CloseableHttpResponse response = httpClient.execute(request);) {
                HttpEntity entity = response.getEntity();
                ObjectMapper mapper = createDefaultObjectMapper();
                OceanForecast OceanForecast = mapper.readValue(entity.getContent(), OceanForecast.class);
                OceanForecast.Meta meta = OceanForecast.getProperties().getMeta();
                List<OceanForecast.TimeSeries> timeSeries = OceanForecast.getProperties().getTimeSeries();
                String unit = null;
                for (OceanForecast.TimeSeries ts : timeSeries) {
                    timeFrom = ts.getTime();
                    if (forecastPeriod.inside(ts.getTime())) {

                        addUnitAwareGenericNumberPhenomenon(PhenomenonName.WaveHeight.toString(),
                                OceanForecast.getProperties().getMeta().getUnits().get("sea_surface_wave_height"),
                                ts, "sea_surface_wave_height");

                        addUnitAwareGenericNumberPhenomenon(PhenomenonName.WaveDirection.toString(),
                                OceanForecast.getProperties().getMeta().getUnits().get("sea_surface_wave_from_direction"),
                                ts, "sea_surface_wave_from_direction");

                        addUnitAwareGenericNumberPhenomenon(PhenomenonName.CurrentSpeed.toString(),
                                OceanForecast.getProperties().getMeta().getUnits().get("sea_water_speed"),
                                ts, "sea_water_speed");

                        addUnitAwareGenericNumberPhenomenon(PhenomenonName.CurrentDirection.toString(),
                                OceanForecast.getProperties().getMeta().getUnits().get("sea_water_to_direction"),
                                ts, "sea_water_to_direction");

                        addUnitAwareGenericNumberPhenomenon(PhenomenonName.seaTemperature.toString(),
                                OceanForecast.getProperties().getMeta().getUnits().get("sea_water_temperature"),
                                ts, "sea_water_temperature");
                    }
                }
            }
        }
        return model;
    }

    private void addSymbol(PhenomenonName phenomenonName, OceanForecast.TimeSeries ts, String propertyName ) {
        int timeResolution = (Utility.hourDifference(forecastPeriod.getStart(), forecastPeriod.getEnd()) <= MeteogramWrapper.SHORT_TERM_HOURS ? 1 : 6);
        // until we can remove multiple time resolution phenomenon
        if (timeResolution == 1 ) {
            timeTo = timeFrom;
        } else {
            timeTo = Utility.getDateWithAddedHours(ts.getTime(), timeResolution);
        }
        OceanForecast.NextXHours nextXHours = (timeResolution == 1) ? ts.getData().getNext1Hours() : ts.getData().getNext6Hours();
        if (nextXHours != null) {
            if (!model.isExist(PhenomenonName.WeatherSymbols.nameWithResolution(timeResolution))) {
                SymbolPhenomenon weatherSymbols = new SymbolPhenomenon();
                weatherSymbols.addValue(timeFrom, timeTo, nextXHours.getSummary().getSymbolCode());
                model.addPhenomenen(PhenomenonName.WeatherSymbols.nameWithResolution(timeResolution), weatherSymbols);

            } else {
                model.getSymbolPhenomenon(PhenomenonName.WeatherSymbols.nameWithResolution(timeResolution)).addValue(
                        timeFrom, timeTo, nextXHours.getSummary().getSymbolCode());
            }
        }
    }
    private void addUnitAwareGenericNumberPhenomenon(String phenomenonName, String unit, OceanForecast.TimeSeries ts,
                                                     String propertyName) {
        if (!model.isExist(phenomenonName))  {
            NumberPhenomenon phenom = new NumberPhenomenon();
            phenom.setUnit(unit);
            phenom.addValue(timeFrom, new Double((Double)ts.getData().getInstant().getDetails().get(propertyName)));
            model.addPhenomenen(phenomenonName, phenom);
        }  else {
            model.getPhenomenen(phenomenonName,
                    NumberPhenomenon.class).addValue(timeFrom,
                    new Double((Double)ts.getData().getInstant().getDetails().get(propertyName)));
        }
    }

    private void addGenericNumberPhenomenon(String phenomenonName, OceanForecast.TimeSeries ts,
                                            String propertyName ) {
        timeTo = timeFrom;
        if (!model.isExist(phenomenonName)) {
            NumberPhenomenon phenom = new NumberPhenomenon();
            phenom.addValue(timeFrom, timeTo, new Double((Double) ts.getData().getInstant().getDetails().get(propertyName)));
            model.addPhenomenen(phenomenonName, phenom);
        } else {
            Number number = ts.getData().getInstant().getDetails().get(propertyName);
            if (number == null) number = 0.0;
            model.getPhenomenen(phenomenonName,
                    NumberPhenomenon.class).addValue(timeFrom, timeTo,
                    new Double((Double) number));
        }

    }

    private ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(dateFormat);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        return mapper;
    }

    @Override
    public GenericDataModel getModel() {
        return model;
    }

    public void setModel(GenericDataModel model) {
        this.model = model;
    }

}
