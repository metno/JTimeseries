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

import java.net.URL;
import java.util.Date;

import org.jfree.chart.JFreeChart;

import no.met.jtimeseries.chart.ChartPlottingInfo;
import no.met.jtimeseries.chart.TimePeriod;
import no.met.jtimeseries.chart.Utility;
import no.met.jtimeseries.data.item.AbstractValueItem;
import no.met.jtimeseries.data.item.NumberValueItem;
import no.met.jtimeseries.data.model.GenericDataModel;
import no.met.phenomenen.NumberPhenomenon;

public class CreateChart {

	private static void show(JFreeChart chart) {
        ChartFrame frame = new ChartFrame(chart, new java.awt.Dimension(800, 300));
        frame.pack();
        frame.setVisible(true);
	}

	static private JFreeChart getShortTermChart(GenericDataModel model, ChartPlottingInfo cpi) {
		MeteogramWrapper wrapper = new MeteogramWrapper("no");

		NumberPhenomenon temperature = model.getNumberPhenomenon("AirTemperature"); // need a point-in-time variable here
		Date from = temperature.getStartTime();
		Date to = Utility.getDateWithAddedHours(from, 48);

		TimePeriod timePeriod = new TimePeriod(from, to).adapt(3);

		// Many elements will refuse to render properly if they contain more data than necessary
		model.cutOlderThan(to);
		return wrapper.createShortTermMeteogram(model, timePeriod, cpi);
	}

	static private JFreeChart getLongTermChart(GenericDataModel model, ChartPlottingInfo cpi) {
		MeteogramWrapper wrapper = new MeteogramWrapper("no");

		NumberPhenomenon temperature = model.getNumberPhenomenon("AirTemperature"); // need a point-in-time variable here
		Date from = temperature.getStartTime();
		Date to = Utility.getDateWithAddedHours(from, 228);

		TimePeriod timePeriod = new TimePeriod(from, to).adapt(6);

		return wrapper.createLongTermMeteogram(model, timePeriod, cpi);
	}


	public static void main(String[] args) throws Exception {

		URL resource = CreateChart.class.getClassLoader().getResource("locationforecast/forecast.xml");
		
		GenericDataModel model = MeteogramWrapper.getModel(resource.toString(), null);
		
		ChartPlottingInfo cpi = new ChartPlottingInfo.Builder(0, 0).altitude(0).width(800).height(300)
                .showAirTemperature(true).showDewpointTemperature(true).showPressure(true).timezone("UTC").showCloudSymbol(true)
                .showWeatherSymbol(true).showWindSymbol(true).showPrecipitation(true).showAccumulatedPrecipitation(true).showWindSpeed(true)
                .showWindDirection(true).windSpeedUnit("knop").build();

		//JFreeChart chart = getShortTermChart(model, cpi);
		JFreeChart chart = getLongTermChart(model, cpi);
		show(chart);
	}

}
