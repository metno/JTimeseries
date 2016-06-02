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
package no.met.jtimeseries.meteogram;

import no.met.jtimeseries.chart.ChartPlottingInfo;
import no.met.jtimeseries.marinogram.MarinogramWrapper;

/**
 * This class is to generate a time series chart for forecast of ocean location.
 * This chart is supposed to be like forecast chart at yr.no as much as possible
 *
 *
 */
public class Marinogram extends AbstractChart{
    
    private ChartPlottingInfo cpi;
    
    public Marinogram(ChartPlottingInfo chartPlotingInfo) {
        this.cpi = chartPlotingInfo;   
        this.width=this.cpi.getWidth();
    }    

    @Override
    public void drawChart() {
        //Location location = new Location(cpi.getLongitude(), cpi.getLatitude());
        MarinogramWrapper  mw = new MarinogramWrapper(cpi.getWidth(), cpi.getLanguage());
        chart = mw.createMarinogram(cpi);
        chart.removeLegend();
        this.height=mw.getTotalPlotHeight()==0?this.width/2:mw.getTotalPlotHeight();
    }
}
