/** *****************************************************************************
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
 ****************************************************************************** */
package no.met.jtimeseries.service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.header.ContentDisposition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.FileNameMap;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import no.met.halo.common.ConfigUtils;

import no.met.halo.common.LogUtils;
import no.met.jtimeseries.ApiMetadata;
import static no.met.jtimeseries.Constant.JTIMESERIES_ENV;
import no.met.jtimeseries.Location;
import no.met.jtimeseries.LocationForecastValidator;
import no.met.jtimeseries.MeteogramWrapper;
import no.met.jtimeseries.ValidationException;
import no.met.jtimeseries.Validator;
import no.met.jtimeseries.chart.ChartPlottingInfo;
import no.met.jtimeseries.chart.TimePeriod;
import no.met.jtimeseries.chart.Utility;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.jtimeseries.meteogram.AbstractChart;
import no.met.jtimeseries.meteogram.AbstractChartSaver;
import no.met.jtimeseries.meteogram.Marinogram;
import no.met.jtimeseries.meteogram.Meteogram;
import no.met.jtimeseries.meteogram.PngChartSaver;
import no.met.jtimeseries.meteogram.SvgChartSaver;
import no.met.jtimeseries.netcdf.NetcdfMeteogramWrapper;
import no.met.jtimeseries.netcdf.ParameterReference;

import org.dom4j.Document;
import org.jfree.chart.JFreeChart;

import no.met.jtimeseries.parser.ApiMetadataParser;
import no.met.jtimeseries.parser.LocationForecastAddressFactory;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.weatherapi.PhenomenonName;
import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The servlet of Jtimeseries chart. This is the thrid version.
 *
 */
@Path("/")
public class TimeSeriesService {

    private static final Logger logger = Logger.getLogger(TimeSeriesService.class.getName());

    /**
     * A listing of all allowable ways to save a chart to file for serving
     */
    private static Map<String, AbstractChartSaver> chartSavers = createChartSavers();

    /**
     * Create the list of ways to save a file
     */
    private static Map<String, AbstractChartSaver> createChartSavers() {
        Map<String, AbstractChartSaver> ret = new TreeMap<String, AbstractChartSaver>();

        // All entries here must be lowercase!
        ret.put("png", new PngChartSaver());
        ret.put("svg", new SvgChartSaver());
        return ret;
    }

    @GET
    @Path("")
    @Produces("application/xml")
    @ServiceDescription("Return the list of services with parameters.")
    public Response capabilities() {
        Document d = ServiceDescriptionGenerator.getXMLServiceDescription(this.getClass());
        return Response.ok(d.asXML()).build();
    }

    @GET
    @Path("metadata")
    @Produces("application/xml")
    @ServiceDescription("Return the metadata.")
    public ApiMetadata getApiMetadata(
            @QueryParam("latitude") @DefaultValue("0") double latitude,
            @QueryParam("longitude") @DefaultValue("0") double longitude) {
        ApiMetadata metadata = ApiMetadataParser.getMetadata(longitude, latitude);
        return metadata;
    }

    @GET
    @Path("forecast/precipitation")
    @Produces("application/json")
    @ServiceDescription("Return short term precipitation.")
    public Response getPrecipitation(
            @QueryParam("latitude") @DefaultValue("0") double latitude,
            @QueryParam("longitude") @DefaultValue("0") double longitude,
            @QueryParam("resolution") @DefaultValue("1") int resolution,
            @QueryParam("term") @DefaultValue("long") String term) {
        Location location = new Location(longitude, latitude);
        try {
            int hh = ("short".equalsIgnoreCase(term))
                    ? MeteogramWrapper.SHORT_TERM_HOURS : MeteogramWrapper.LONG_TERM_HOURS;

            TimePeriod timePeriod = new TimePeriod(new Date(), hh);

            GenericDataModel model = MeteogramWrapper.getModel(location, timePeriod);
            NumberPhenomenon precipitation = model.
                    getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(resolution));
            return Response.ok(precipitation.toJSON()).build();
        } catch (ParseException ex) {
            LogUtils.logException(logger, "Failed to parse data from " + LocationForecastAddressFactory
                    .getURL(location).toString(), ex);
        } catch (IOException ex) {
            LogUtils.logException(logger, "Failed to parse data from " + LocationForecastAddressFactory
                    .getURL(location).toString(), ex);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @POST
    @Path("forecast/archived/precipitation")
    @Produces("application/json")
    @ServiceDescription("Return archived precipitation.")
    public Response getArchivedPrecipitation(
            @QueryParam("latitude") @DefaultValue("0") double latitude,
            @QueryParam("longitude") @DefaultValue("0") double longitude,
            @QueryParam("resolution") @DefaultValue("1") int resolution,
            @QueryParam("term") @DefaultValue("long") String term, String forecast) {
        if (forecast == null || forecast.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            Schema schema = getLocationForecastSchema();
            Validator validator = new LocationForecastValidator(schema);
            validator.validate(new SAXSource(new InputSource(new StringReader(forecast))));

            //posted data is ok.
            int hh = ("short".equalsIgnoreCase(term))
                    ? MeteogramWrapper.SHORT_TERM_HOURS : MeteogramWrapper.LONG_TERM_HOURS;
            GenericDataModel model = MeteogramWrapper.getModel(new StringReader(forecast));
            NumberPhenomenon precipitation = model.
                    getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(resolution));
            Date from = precipitation.getStartTime();
            Date to = Utility.getDateWithAddedHours(from, hh);
            model.cutOlderThan(to);
            return Response.ok(precipitation.toJSON()).build();
        } catch (ParseException ex) {
            LogUtils.logException(logger, "Failed to parse provided data", ex);
        } catch (IOException ex) {
            LogUtils.logException(logger, "Failed to parse provided data", ex);
        } catch (DocumentException ex) {
            LogUtils.logException(logger, "Failed to parse provided data", ex);
        } catch (SAXException ex) {
            Logger.getLogger(TimeSeriesService.class.getName()).log(Level.WARNING, "Invalid schema from api.met.no", ex);
        } catch (ValidationException ex) {
            Logger.getLogger(TimeSeriesService.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @GET
    @Path("meteogram/provided")
    @Produces({"image/svg+xml", "image/png"})
    @ServiceDescription("Generate a meteogram for a provided locationforecast file.")
    public Response createImage(
            @QueryParam("latitude") @DefaultValue("0") double latitude,
            @QueryParam("longitude") @DefaultValue("0") double longitude,
            @QueryParam("width") @DefaultValue("750") int width,
            @QueryParam("height") @DefaultValue("300") int height,
            @QueryParam("temperature") @DefaultValue("true") boolean showTemperature,
            @QueryParam("dewpointTemperature") @DefaultValue("true") boolean showDewpointTemperature,
            @QueryParam("pressure") @DefaultValue("true") boolean showPressure,
            @QueryParam("percipitation") @DefaultValue("true") boolean showPercipitation,
            @QueryParam("windSymbol") @DefaultValue("true") boolean showWindSymbol,
            @QueryParam("weatherSymbol") @DefaultValue("true") boolean showWeatherSymbol,
            @QueryParam("cloudSymbol") @DefaultValue("true") boolean showCloudSymbol,
            @QueryParam("windDirection") @DefaultValue("false") boolean showWindDirection,
            @QueryParam("windSpeed") @DefaultValue("false") boolean showWindSpeed,
            @QueryParam("windSpeedUnit") @DefaultValue("ms") String windSpeedUnit,
            @QueryParam("format") @DefaultValue("png") String format,
            @QueryParam("term") @DefaultValue("short") String term,
            @QueryParam("timezone") @DefaultValue("UTC") String timezone,
            @QueryParam("language") @DefaultValue("en") String language,
            @QueryParam("time") @DefaultValue("") String time) {

        ChartPlottingInfo cpi = new ChartPlottingInfo.Builder(0, 0).altitude(0).width(width)
                .height(height).showAirTemperature(showTemperature).showPressure(showPressure).timezone(timezone)
                .showCloudSymbol(showCloudSymbol).showWeatherSymbol(showWeatherSymbol).showWindSymbol(showWindSymbol)
                .showPrecipitation(showPercipitation).showWindDirection(showWindDirection).showWindSpeed(showWindSpeed)
                .windSpeedUnit(windSpeedUnit).showDewpointTemperature(showDewpointTemperature).language(language).build();

        AbstractChartSaver saver = chartSavers.get(format.toLowerCase());
        if (saver == null) {
            return Response.status(422).build();
        }

        try {
            Client client = Client.create(new DefaultClientConfig());
            String locationForecastUrl = getArchivedLocationForecastUrl(latitude, longitude, time);
            Logger.getLogger(TimeSeriesService.class.getName()).log(Level.INFO, "Fetching locationforecast XML from " + locationForecastUrl);
            WebResource webResource = client.resource(locationForecastUrl);
            ClientResponse response = webResource.type(MediaType.APPLICATION_XML).get(ClientResponse.class);
            String forecast = IOUtils.toString(response.getEntityInputStream());

            //first validate
            Schema schema = getLocationForecastSchema();
            Validator validator = new LocationForecastValidator(schema);
            validator.validate(new SAXSource(new InputSource(new StringReader(forecast))));

            // forecast must become a resource, or getModel must handle xml strings
            GenericDataModel model = MeteogramWrapper.getModel(new StringReader(forecast));
            MeteogramWrapper wrapper = new MeteogramWrapper(language);
            NumberPhenomenon temperature = model.getNumberPhenomenon("AirTemperature"); // need a point-in-time variable here
            Date from = temperature.getStartTime();

            JFreeChart chart = null;

            if (term.equals("short")) {
                Date to = Utility.getDateWithAddedHours(from, 48);
                TimePeriod timePeriod = new TimePeriod(from, to).adapt(3);
                // Many elements will refuse to render properly if they contain more data than necessary
                model.cutOlderThan(to);
                chart = wrapper.createShortTermMeteogram(model, timePeriod, cpi);
            } else if (term.equals("long")) {
                Date to = Utility.getDateWithAddedHours(from, 228);
                TimePeriod timePeriod = new TimePeriod(from, to).adapt(6);
                chart = wrapper.createLongTermMeteogram(model, timePeriod, cpi);
            } else {
                throw new Exception("term must be either \"short\" or \"long\"");
            }

            if (chart != null) {
                File chartFile = saver.save(chart, width, height);
                return serveFile(chartFile);
            }
        } catch (Exception e) {
            Logger.getLogger(TimeSeriesService.class.getName()).log(Level.WARNING, e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Should never happen:
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @GET
    @Path("meteogram")
    @Produces({"image/svg+xml", "image/png"})
    @ServiceDescription("Generate a meteogram at the specified latitude and longitude.")
    public Response createImage(
            @QueryParam("latitude") @DefaultValue("0") double latitude,
            @QueryParam("longitude") @DefaultValue("0") double longitude,
            @QueryParam("altitude") @DefaultValue("0") int altitude,
            @QueryParam("width") @DefaultValue("750") int width,
            @QueryParam("height") @DefaultValue("300") int height,
            @QueryParam("temperature") @DefaultValue("true") boolean showTemperature,
            @QueryParam("dewpointTemperature") @DefaultValue("true") boolean showDewpointTemperature,
            @QueryParam("pressure") @DefaultValue("true") boolean showPressure,
            @QueryParam("percipitation") @DefaultValue("true") boolean showPercipitation,
            @QueryParam("windSymbol") @DefaultValue("true") boolean showWindSymbol,
            @QueryParam("weatherSymbol") @DefaultValue("true") boolean showWeatherSymbol,
            @QueryParam("cloudSymbol") @DefaultValue("true") boolean showCloudSymbol,
            @QueryParam("windDirection") @DefaultValue("false") boolean showWindDirection,
            @QueryParam("windSpeed") @DefaultValue("false") boolean showWindSpeed,
            @QueryParam("windSpeedUnit") @DefaultValue("ms") String windSpeedUnit,
            @QueryParam("format") @DefaultValue("png") String format,
            @QueryParam("term") @DefaultValue("short") String term,
            @QueryParam("timezone") @DefaultValue("UTC") String timezone,
            @QueryParam("language") @DefaultValue("en") String language) {

        AbstractChartSaver saver = chartSavers.get(format.toLowerCase());
        if (saver == null) {
            return Response.status(422).build();
        }

        ChartPlottingInfo cpi = new ChartPlottingInfo.Builder(longitude, latitude).altitude(altitude).width(width)
                .height(height).showAirTemperature(showTemperature).showPressure(showPressure).timezone(timezone)
                .showCloudSymbol(showCloudSymbol).showWeatherSymbol(showWeatherSymbol).showWindSymbol(showWindSymbol)
                .showPrecipitation(showPercipitation).showWindDirection(showWindDirection).showWindSpeed(showWindSpeed)
                .windSpeedUnit(windSpeedUnit).showDewpointTemperature(showDewpointTemperature).language(language).build();
        AbstractChart meteogram;
        if ("short".equalsIgnoreCase(term)) {
            meteogram = new Meteogram(cpi, MeteogramWrapper.SHORT_TERM_HOURS);
        } else if ("long".equalsIgnoreCase(term)) {
            meteogram = new Meteogram(cpi, MeteogramWrapper.LONG_TERM_HOURS);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            meteogram.drawChart();
            File chartFile = saver.save(meteogram);
            return serveFile(chartFile);
        } catch (Exception e) {
            LogUtils.logException(logger, "Failed to create meteogram: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("marinogram")
    @Produces({"image/svg+xml", "image/png"})
    @ServiceDescription("Generate a marinogram for a specified latitude and longitude.")
    public Response createMarinogram(
            @QueryParam("latitude") @DefaultValue("0") double latitude,
            @QueryParam("longitude") @DefaultValue("0") double longitude,
            @QueryParam("width") @DefaultValue("800") int width,
            @QueryParam("waterTemperature") @DefaultValue("true") boolean showWaterTemperature,
            @QueryParam("airTemperature") @DefaultValue("true") boolean showAirTemperature,
            @QueryParam("dewpointTemperature") @DefaultValue("true") boolean showDewpointTemperature,
            @QueryParam("pressure") @DefaultValue("true") boolean showPressure,
            @QueryParam("waveHeight") @DefaultValue("true") boolean showWaveHeight,
            @QueryParam("waveDirection") @DefaultValue("true") boolean showWaveDirection,
            @QueryParam("currentDirection") @DefaultValue("true") boolean showCurrentDirection,
            @QueryParam("currentSpeed") @DefaultValue("true") boolean showCurrentSpeed,
            @QueryParam("windDirection") @DefaultValue("true") boolean showWindDirection,
            @QueryParam("windSpeed") @DefaultValue("true") boolean showWindSpeed,
            @QueryParam("format") @DefaultValue("png") String format,
            @QueryParam("timezone") @DefaultValue("UTC") String timezone,
            @QueryParam("language") @DefaultValue("en") String language) {

        AbstractChartSaver saver = chartSavers.get(format.toLowerCase());
        if (saver == null) {
            return Response.status(422).build();
        }

        ChartPlottingInfo cpi = new ChartPlottingInfo.Builder(longitude, latitude).width(width)
                .showAirTemperature(showAirTemperature).showWaterTemperature(showWaterTemperature)
                .showPressure(showPressure).showWaveHeight(showWaveHeight).showWaveDirection(showWaveDirection)
                .showCurrentDirection(showCurrentDirection).showCurrentSpeed(showCurrentSpeed)
                .showWindDirection(showWindDirection).showWindSpeed(showWindSpeed).showDewpointTemperature(showDewpointTemperature)
                .timezone(timezone).language(language).build();
        AbstractChart marinogram = new Marinogram(cpi);
        try {
            marinogram.drawChart();
            File chartFile = saver.save(marinogram);
            return serveFile(chartFile);
        } catch (Exception e) {
            LogUtils.logException(logger, "Failed to create marinogram: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
    @GET
    @Path("thredds/diagram")
    @Produces("image/png")
    @ServiceDescription("Visualize data from a thredds server.")
    public Response createThreddsDiagram(
    		@QueryParam("url") String url,
    		@QueryParam("parameters") String parameterList,
    		@QueryParam("variables") String variableList,
    		@QueryParam("with_header") @DefaultValue("false") boolean withHeader,
    		@QueryParam("header") String header,
            @QueryParam("width") @DefaultValue("750") int width,
            @QueryParam("height") @DefaultValue("300") int height
    		) {

    	System.out.println(url);

    	Location location = null;

    	if ( parameterList != null && variableList != null ) {
    		logger.severe("Uanble to handle both <parameters> and <variables> in request.");
			return Response.status(Response.Status.BAD_REQUEST).build();
    	}

    	ParameterReference parameterReference = ParameterReference.STANDARD_NAME;
    	if ( parameterList == null ) {
    		parameterList = variableList;
    		parameterReference = ParameterReference.VARIABLE_NAME;
    	}

    	List<String> parameters = null;
    	if ( parameterList != null ) {
    		parameters = new Vector<String>();
    		String[] param = parameterList.split(",");
    		for ( int i = 0; i < param.length; i ++ ) {
    			System.out.println(param[i]);
    			parameters.add(param[i]);
    		}
    	}

    	if ( withHeader )
    		if ( header == null)
    			header = "AUTO";

    	try {
			JFreeChart chart = NetcdfMeteogramWrapper.getChart(url, location, parameters, parameterReference, header);
			File ret = new PngChartSaver().save(chart, width, height);
			return serveFile(ret);
		}
    	catch ( FileNotFoundException e ) {
    		LogUtils.logException(logger, "Unable to make sense of data: " + e.getMessage(), e);
			return Response.status(502).build();
		}
    	catch ( ParseException e ) {
    		LogUtils.logException(logger, "Unable to make sense of request or data: " + e.getMessage(), e);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
    	catch (Exception e) {
    	    LogUtils.logException(logger, "Failed to create timeseries diagram from thredds data: " + e.getMessage(), e);
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
    	}
    }


    @GET
    @Path("thredds/csv")
    @Produces("text/plain")
    @ServiceDescription("Get csv formatted data from a thredds server.")
    public Response createThreddsCsv(
    		@QueryParam("url") String url,
    		@QueryParam("parameters") String parameterList,
    		@QueryParam("variables") String variableList,
    		@QueryParam("header") String header
    		) {

    	Location location = null;

    	if ( parameterList != null && variableList != null ) {
    		logger.severe("Uanble to handle both <parameters> and <variables> in request.");
			return Response.status(Response.Status.BAD_REQUEST).build();
    	}

    	ParameterReference parameterReference = ParameterReference.STANDARD_NAME;
    	if ( parameterList == null ) {
    		parameterList = variableList;
    		parameterReference = ParameterReference.VARIABLE_NAME;
    	}

    	List<String> parameters = null;
    	if ( parameterList != null ) {
    		parameters = new Vector<String>();
    		String[] param = parameterList.split(",");
    		for ( int i = 0; i < param.length; i ++ ) {
    			System.out.println(param[i]);
    			parameters.add(param[i]);
    		}
    	}

    	try {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(s);
			NetcdfMeteogramWrapper.getData(out, url, location, parameters, parameterReference, header);
			return Response.ok(s.toString()).build();
		}
    	catch ( FileNotFoundException e ) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
    	catch ( ParseException e ) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
    	catch (Exception e) {
    	    LogUtils.logException(logger, "Failed to create CSV from thredds data: " + e.getMessage(), e);
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
    	}
    }
     */

    /**
     * Create the Response object for serving a file.
     *
     * @param f The file to serve
     * @return A Response object that will serve the file with the correct headers set.
     */
    private Response serveFile(File f) {

        ContentDisposition cd = ContentDisposition.type("inline").fileName(f.getName()).build();

        // TODO this way of getting the mime type could be slow. Should be
        // tested.
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mt = fileNameMap.getContentTypeFor(f.getAbsolutePath());

        CacheControl cc = new CacheControl();
        cc.setMustRevalidate(true);
        Map<String, String> cacheExtension = cc.getCacheExtension();
        cacheExtension.put("post-check", "0");
        cacheExtension.put("pre-check", "0");

        // to be able to clean up the temporary files generated by the service
        // we read all the bytes in the file into memory and this might not be efficient enough.
        // TODO We need to look into this when we do performance testing.
        byte[] fileBytes = new byte[(int) f.length()];
        try {
            new FileInputStream(f).read(fileBytes);
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(e, 500);
        } catch (IOException e) {
            throw new WebApplicationException(e, 500);
        }
        f.delete();

        String contentType = mt;
        return Response.ok(fileBytes, mt).header("Content-Disposition", cd).header("Content-Type", contentType)
                .cacheControl(cc).build();

    }

    /**
     * Get location forecast schema url
     *
     * @return location forecast schema url or null
     */
    private URL getLocationForecastSchemaUrl() {
        try {
            ConfigUtils cfg = new no.met.halo.common.ConfigUtils("/config/jtimeseries.properties", JTIMESERIES_ENV);
            String scheme = cfg.getRequired("datasource.scheme");
            String server = cfg.getRequired("datasource.server");
            String port = cfg.getRequired("datasource.port");
            String path = cfg.getRequired("datasource.meteogram.path");

            URI uri = new URI(scheme, server + ":" + port, path + "schema", null, null);
            return uri.toURL();
        } catch (URISyntaxException ex) {
            Logger.getLogger(TimeSeriesService.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(TimeSeriesService.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
        return null;
    }

    private Schema getLocationForecastSchema() throws SAXException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL locationForecastSchemaUrl = getLocationForecastSchemaUrl();
        Schema schema = sf.newSchema(locationForecastSchemaUrl);
        return schema;

    }

    private String getArchivedLocationForecastUrl(double lat, double lon, String time) {
        try {
            ConfigUtils cfg = new no.met.halo.common.ConfigUtils("/config/jtimeseries.properties", JTIMESERIES_ENV);
            String scheme = cfg.getRequired("archive.service.scheme");
            String server = cfg.getRequired("archive.service.endpoint");
            String port = cfg.getRequired("archive.service.port");
            String path = cfg.getRequired("archive.service.path");
            StringBuilder parms = new StringBuilder();
            parms.append("latitude=");
            parms.append(lat);
            parms.append("&longitude=");
            parms.append(lon);
            parms.append("&time=");
            parms.append(time);
            URI uri = new URI(scheme, server + ":" + port, path, parms.toString(), null);
            return uri.toString();
        } catch (URISyntaxException ex) {
            Logger.getLogger(TimeSeriesService.class.getName()).log(Level.WARNING,
                    "URI syntex error when creating archive url", ex);
        }
        return "";
    }

}
