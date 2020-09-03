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
import no.met.forecasts.LocationForecast;
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

public class LocationForecastJsonParseScheme extends ParseScheme {

    private static final Logger logger = Logger.getLogger(LocationForecastJsonParseScheme.class.getSimpleName());

    private DateFormat dateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
    private Date timeFrom;
    private Date timeTo;

    private GenericDataModel model;
    private TimePeriod forecastPeriod;


    public LocationForecastJsonParseScheme() {
        model = new GenericDataModel();
    	forecastPeriod = null;
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public LocationForecastJsonParseScheme(Date start, Date end) {

        model = new GenericDataModel();
    	forecastPeriod = new TimePeriod(start, end);
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    }

    public LocationForecastJsonParseScheme(TimePeriod timePeriod) {
        model = new GenericDataModel();
    	this.forecastPeriod = timePeriod;
        // to prevent problems when converting between daylight savings hours
        // and not
        // we use UTC which does not have this problem.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public LocationForecastJsonParseScheme(int hours) {

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
            //"https://api.met.no/weatherapi/locationforecast/2.0/complete?lat=60.10&lon=9.58"
            HttpGet request = new HttpGet(resource);
            // add request headers
            request.addHeader(HttpHeaders.USER_AGENT, "halo.met.no");

            try (CloseableHttpResponse response = httpClient.execute(request);) {
                HttpEntity entity = response.getEntity();
                ObjectMapper mapper = createDefaultObjectMapper();
                LocationForecast locationForecast = mapper.readValue(entity.getContent(), LocationForecast.class);
                LocationForecast.Meta meta = locationForecast.getProperties().getMeta();
                List<LocationForecast.TimeSeries> timeSeries = locationForecast.getProperties().getTimeSeries();
                String unit = null;
                for (LocationForecast.TimeSeries ts : timeSeries) {
                    timeFrom = ts.getTime();
                    if (forecastPeriod.inside(ts.getTime())) {

                        addUnitAwareGenericNumberPhenomenon(PhenomenonName.AirTemperature.toString(),
                                locationForecast.getProperties().getMeta().getUnits().get("air_temperature"),
                                ts, "air_temperature");

                        addUnitAwareGenericNumberPhenomenon(PhenomenonName.dewPointTemperature.toString(),
                                locationForecast.getProperties().getMeta().getUnits().get("dew_point_temperature"),
                                ts, "dew_point_temperature");

                        addUnitAwareGenericNumberPhenomenon(PhenomenonName.Pressure.toString(),
                                locationForecast.getProperties().getMeta().getUnits().get("air_pressure_at_sea_level"),
                                ts, "air_pressure_at_sea_level");

                        addMultipleTimeResolutionNumberPhenomenon(PhenomenonName.Precipitation,
                                locationForecast.getProperties().getMeta().getUnits().get("precipitation_amount"),
                                ts, "precipitation_amount");

                        unit = meta.getUnits().get("precipitation_amount_max");
                        if (unit != null) {
                            addMultipleTimeResolutionNumberPhenomenon(PhenomenonName.PrecipitationMax,
                                    locationForecast.getProperties().getMeta().getUnits().get("precipitation_amount_max"),
                                    ts, "precipitation_amount_max");
                        }

                        unit = meta.getUnits().get("precipitation_amount_min");
                        if (unit != null) {
                            addMultipleTimeResolutionNumberPhenomenon(PhenomenonName.PrecipitationMin,
                                    locationForecast.getProperties().getMeta().getUnits().get("precipitation_amount_min"),
                                    ts, "precipitation_amount_min");
                        }

                        addGenericNumberPhenomenon(PhenomenonName.Cloudiness.toString(),
                                ts, "cloud_area_fraction");

                        addGenericNumberPhenomenon(PhenomenonName.LowCloud.toString(),
                                ts, "cloud_area_fraction_low");

                        addGenericNumberPhenomenon(PhenomenonName.MediumCloud.toString(),
                                ts, "cloud_area_fraction_medium");

                        addGenericNumberPhenomenon(PhenomenonName.HighCloud.toString(),
                                ts, "cloud_area_fraction_high");

                        addGenericNumberPhenomenon(PhenomenonName.Fog.toString(),
                                ts, "fog_area_fraction");

                        addSymbol(PhenomenonName.WeatherSymbols, ts, "");

                        addGenericNumberPhenomenon(PhenomenonName.WindDirectionDegree.toString(),
                                ts, "wind_from_direction");

                        addGenericNumberPhenomenon(PhenomenonName.WindSpeedMPS.toString(),
                                ts, "wind_speed");
                    }
                }
            }
        }
        return model;
    }

    private void addSymbol(PhenomenonName phenomenonName, LocationForecast.TimeSeries ts, String propertyName ) {
        int timeResolution = (Utility.hourDifference(forecastPeriod.getStart(), forecastPeriod.getEnd()) <= MeteogramWrapper.SHORT_TERM_HOURS ? 1 : 6);
        // until we can remove multiple time resolution phenomenon
        if (timeResolution == 1 ) {
            timeTo = timeFrom;
        } else {
            timeTo = Utility.getDateWithAddedHours(ts.getTime(), timeResolution);
        }
        LocationForecast.NextXHours nextXHours = (timeResolution == 1) ? ts.getData().getNext1Hours() : ts.getData().getNext6Hours();
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

    public void addMultipleTimeResolutionNumberPhenomenon(PhenomenonName phenomenonName, String unit, LocationForecast.TimeSeries ts,
                                                          String propertyName ) {
        //int timeResolution = (Utility.hourDifference(forecastPeriod.getStart(), forecastPeriod.getEnd()) <= MeteogramWrapper.SHORT_TERM_HOURS ? 1 : 6);
        // until we can remove multiple time resolution phenomenon
        LocationForecast.NextXHours next1Hours = ts.getData().getNext1Hours();

        if (next1Hours != null) {
            timeTo = Utility.getDateWithAddedHours(ts.getTime(), 1);
            if (!model.isExist(phenomenonName.nameWithResolution(1))) {
                NumberPhenomenon phenom = new NumberPhenomenon();
                phenom.addValue(timeFrom, timeTo,
                        new Double((Double) next1Hours.getDetails().get(propertyName)));
                model.addPhenomenen(phenomenonName.nameWithResolution(1), phenom);
            } else {
                model.getNumberPhenomenon(phenomenonName.nameWithResolution(1)).addValue(timeFrom, timeTo,
                        new Double((Double) next1Hours.getDetails().get(propertyName)));
            }
        }

        LocationForecast.NextXHours next6Hours = ts.getData().getNext6Hours();
        if (next6Hours != null) {
            timeTo = Utility.getDateWithAddedHours(ts.getTime(), 6);
            if (!model.isExist(phenomenonName.nameWithResolution(6))) {
                NumberPhenomenon phenom = new NumberPhenomenon();
                phenom.addValue(timeFrom, timeTo,
                        new Double((Double) next6Hours.getDetails().get(propertyName)));
                model.addPhenomenen(phenomenonName.nameWithResolution(6), phenom);
            } else {
                model.getNumberPhenomenon(phenomenonName.nameWithResolution(6)).addValue(timeFrom, timeTo,
                        new Double((Double) next6Hours.getDetails().get(propertyName)));
            }
        }
    }

    private void addUnitAwareGenericNumberPhenomenon(String phenomenonName, String unit, LocationForecast.TimeSeries ts,
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

    private void addGenericNumberPhenomenon(String phenomenonName, LocationForecast.TimeSeries ts,
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
