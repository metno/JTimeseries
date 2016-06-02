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
package no.met.jtimeseries.data.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import no.met.jtimeseries.chart.TimePeriod;
import no.met.jtimeseries.chart.Utility;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.jtimeseries.parser.LocationForecastParseScheme;
import no.met.phenomenen.NumberPhenomenon;
import no.met.phenomenen.TextPhenomenon;
import no.met.phenomenen.weatherapi.PhenomenonName;

import org.junit.Test;
//import no.met.jtimeseries.data.model.LocationForecastDataModel;

public class XMLLocationForecastDataParserTest {

	private static DateFormat dateFormat = new SimpleDateFormat(
			Utility.DATE_FORMAT);

	static {
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Test
	public void testInsideHirlamWithPercipitation() throws ParseException {

		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.clear();
		cal.set(2012, 3, 10, 11, 0);
		Date from = cal.getTime();
		cal.set(2012, 3, 12, 11, 0);
		Date to = cal.getTime();

		TimePeriod timePeriod = new TimePeriod(d("2012-04-10T11:00:00Z"), 48);
		LocationForecastParseScheme scheme = new LocationForecastParseScheme(timePeriod);

		GenericDataModel model = new GenericDataModel();
		scheme.setModel(model);

		URL filepath = this.getClass().getResource(
				"/locationforecast/inside-hirlam-with-precipitation.xml");
		scheme.parseFromFile(filepath.toString());

		assertEquals(from, model.getTimeFrom());
		assertEquals(to, model.getTimeTo());

		List<Date> expectedDates = Arrays.asList(d("2012-04-10T11:00:00Z"),
				d("2012-04-10T12:00:00Z"), d("2012-04-10T13:00:00Z"),
				d("2012-04-10T14:00:00Z"), d("2012-04-10T15:00:00Z"),
				d("2012-04-10T16:00:00Z"), d("2012-04-10T17:00:00Z"),
				d("2012-04-10T18:00:00Z"), d("2012-04-10T19:00:00Z"),
				d("2012-04-10T20:00:00Z"), d("2012-04-10T21:00:00Z"),
				d("2012-04-10T22:00:00Z"), d("2012-04-10T23:00:00Z"),
				d("2012-04-11T00:00:00Z"), d("2012-04-11T01:00:00Z"),
				d("2012-04-11T02:00:00Z"), d("2012-04-11T03:00:00Z"),
				d("2012-04-11T04:00:00Z"), d("2012-04-11T05:00:00Z"),
				d("2012-04-11T06:00:00Z"), d("2012-04-11T07:00:00Z"),
				d("2012-04-11T08:00:00Z"), d("2012-04-11T09:00:00Z"),
				d("2012-04-11T10:00:00Z"), d("2012-04-11T11:00:00Z"),
				d("2012-04-11T12:00:00Z"), d("2012-04-11T13:00:00Z"),
				d("2012-04-11T14:00:00Z"), d("2012-04-11T15:00:00Z"),
				d("2012-04-11T16:00:00Z"), d("2012-04-11T17:00:00Z"),
				d("2012-04-11T18:00:00Z"), d("2012-04-11T19:00:00Z"),
				d("2012-04-11T20:00:00Z"), d("2012-04-11T21:00:00Z"),
				d("2012-04-11T22:00:00Z"), d("2012-04-11T23:00:00Z"),
				d("2012-04-12T00:00:00Z"), d("2012-04-12T01:00:00Z"),
				d("2012-04-12T02:00:00Z"), d("2012-04-12T03:00:00Z"),
				d("2012-04-12T04:00:00Z"), d("2012-04-12T05:00:00Z"),
				d("2012-04-12T06:00:00Z"), d("2012-04-12T07:00:00Z"),
				d("2012-04-12T08:00:00Z"), d("2012-04-12T09:00:00Z"),
				d("2012-04-12T10:00:00Z"), d("2012-04-12T11:00:00Z"));

		NumberPhenomenon temp = model.getPhenomenen(
				PhenomenonName.AirTemperature.toString(),
				NumberPhenomenon.class);
		assertEquals(expectedDates, temp.getTime());

		assertEquals(new Double(4.8), temp.getValue().get(0));

		assertEquals(
				new Double(989.5),
				model.getPhenomenen(
						PhenomenonName.Pressure.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(100),
				model.getPhenomenen(
						PhenomenonName.Cloudiness.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(100),
				model.getPhenomenen(
						PhenomenonName.LowCloud.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(100),
				model.getPhenomenen(
						PhenomenonName.MediumCloud.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(81.2),
				model.getPhenomenen(
						PhenomenonName.HighCloud.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(0),
				model.getPhenomenen(
						PhenomenonName.Fog.toString(),
						NumberPhenomenon.class).getValue().get(0));

		NumberPhenomenon windDirectionDegree = model.getPhenomenen(
				PhenomenonName.WindDirectionDegree.toString(),
				NumberPhenomenon.class);
		assertEquals(new Double(145.7), windDirectionDegree.getValue().get(0));

		TextPhenomenon windDirectionName = model.getTextPhenomenon(
				PhenomenonName.WindDirectionName.toString());
		assertEquals("SE", windDirectionName.getValue().get(0));

		NumberPhenomenon windSpeedMps = model.getPhenomenen(
				PhenomenonName.WindSpeedMPS.toString(),
				NumberPhenomenon.class);
		assertEquals(new Double(6.2), windSpeedMps.getValue().get(0));

		NumberPhenomenon windSpeedBeaufort = model.getPhenomenen(
				PhenomenonName.WindSpeedBeaufort.toString(),
				NumberPhenomenon.class);
		assertEquals(new Double(4), windSpeedBeaufort.getValue().get(0));

		TextPhenomenon windSpeedName = model.getTextPhenomenon(
				PhenomenonName.WindSpeedName.toString());
		assertEquals("Laber bris", windSpeedName.getValue().get(0));

		NumberPhenomenon precipitationMin = model.getNumberPhenomenon(PhenomenonName.PrecipitationMin.nameWithResolution(1));
		List<Double> minPrecip = Arrays.asList(0.3, 0.4, 0.8, 1.3, 1.2, 0.1,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.4, 0.4, 0.4, 0.4, 0.5, 0.5,
				0.5, 0.4, 0.3, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		assertEquals(minPrecip, precipitationMin.getValue());

		NumberPhenomenon precipitationMax = model.getNumberPhenomenon(PhenomenonName.PrecipitationMax
						.nameWithResolution(1));
		List<Double> maxPrecip = Arrays.asList(1.4, 1.7, 3.0, 2.6, 2.7, 1.0,
				0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.3, 1.2, 1.2, 1.2, 1.0, 1.0,
				1.0, 0.8, 0.7, 0.5, 0.4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		assertEquals(maxPrecip, precipitationMax.getValue());

		NumberPhenomenon precipitation = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(1));
		List<Double> precip = Arrays.asList(0.6, 0.8, 2.2, 2.1, 2.0, 0.4, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 1.3, 1.2, 1.2, 1.2, 0.5, 0.5, 0.5,
				0.4, 0.3, 0.2, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0);
		assertEquals(precip, precipitation.getValue());
	}

	@Test
	public void testOutsideHirlamWithPercipitation() throws ParseException {

		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.clear();
		cal.set(2012, 03, 12, 12, 0);
		Date to = cal.getTime();

		
		TimePeriod timePeriod = new TimePeriod(d("2012-04-10T12:00:00Z"), 48);
		LocationForecastParseScheme scheme = new LocationForecastParseScheme(timePeriod);

		GenericDataModel model = new GenericDataModel();
		scheme.setModel(model);

		URL filepath = this.getClass().getResource(
				"/locationforecast/outside-hirlam-with-precipitation.xml");
		scheme.parseFromFile(filepath.toString());

		cal.set(2012, 03, 10, 12, 0);
		Date expectedFrom = cal.getTime();
		assertEquals(expectedFrom, model.getTimeFrom());
		assertEquals(to, model.getTimeTo());

		List<Date> expectedDates = Arrays.asList(d("2012-04-10T12:00:00Z"),
				d("2012-04-10T15:00:00Z"), d("2012-04-10T18:00:00Z"),
				d("2012-04-10T21:00:00Z"), d("2012-04-11T00:00:00Z"),
				d("2012-04-11T03:00:00Z"), d("2012-04-11T06:00:00Z"),
				d("2012-04-11T09:00:00Z"), d("2012-04-11T12:00:00Z"),
				d("2012-04-11T15:00:00Z"), d("2012-04-11T18:00:00Z"),
				d("2012-04-11T21:00:00Z"), d("2012-04-12T00:00:00Z"),
				d("2012-04-12T03:00:00Z"), d("2012-04-12T06:00:00Z"),
				d("2012-04-12T09:00:00Z"), d("2012-04-12T12:00:00Z"));

		NumberPhenomenon temp = model.getPhenomenen(
				PhenomenonName.AirTemperature.toString(),
				NumberPhenomenon.class);
		assertEquals(expectedDates, temp.getTime());

		assertEquals(new Double(10.7), temp.getValue().get(0));

		assertEquals(
				new Double(1011.8),
				model.getPhenomenen(
						PhenomenonName.Pressure.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(58.6),
				model.getPhenomenen(
						PhenomenonName.Cloudiness.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(0),
				model.getPhenomenen(
						PhenomenonName.LowCloud.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(0),
				model.getPhenomenen(
						PhenomenonName.MediumCloud.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(58.6),
				model.getPhenomenen(
						PhenomenonName.HighCloud.toString(),
						NumberPhenomenon.class).getValue().get(0));
		assertEquals(
				new Double(0),
				model.getPhenomenen(
						PhenomenonName.Fog.toString(),
						NumberPhenomenon.class).getValue().get(0));

		NumberPhenomenon windDirectionDegree = model.getPhenomenen(
				PhenomenonName.WindDirectionDegree.toString(),
				NumberPhenomenon.class);
		assertEquals(new Double(133.6), windDirectionDegree.getValue().get(0));

		TextPhenomenon windDirectionName = model.getTextPhenomenon(
				PhenomenonName.WindDirectionName.toString());
		assertEquals("SE", windDirectionName.getValue().get(0));

		NumberPhenomenon windSpeedMps = model.getPhenomenen(
				PhenomenonName.WindSpeedMPS.toString(),
				NumberPhenomenon.class);
		assertEquals(new Double(3.1), windSpeedMps.getValue().get(0));
		NumberPhenomenon windSpeedBeaufort = model.getPhenomenen(
				PhenomenonName.WindSpeedBeaufort.toString(),
				NumberPhenomenon.class);
		assertEquals(new Double(2), windSpeedBeaufort.getValue().get(0));

		TextPhenomenon windSpeedName = model.getTextPhenomenon(
				PhenomenonName.WindSpeedName.toString());
		assertEquals("Svak vind", windSpeedName.getValue().get(0));

		NumberPhenomenon precip = model.getNumberPhenomenon(PhenomenonName.Precipitation.nameWithResolution(3));
		List<Double> precipVals = Arrays.asList(0.0, 0.0, 0.0, 1.5, 2.6, 1.8, 4.0, 4.6, 3.9,
				3.5, 1.9, 0.2, 0.2, 0.0, 0.0, 0.0);
		assertEquals(precipVals, precip.getValue());

		NumberPhenomenon minPrecip = model.getNumberPhenomenon(PhenomenonName.PrecipitationMin.nameWithResolution(3));
		assertNull(minPrecip);

		NumberPhenomenon maxPrecip = model.getNumberPhenomenon(PhenomenonName.PrecipitationMax.nameWithResolution(3));
		assertNull(maxPrecip);
	}

	private Date d(String date) throws ParseException {
		return dateFormat.parse(date);
	}

}
