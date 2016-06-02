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

import no.met.jtimeseries.MeteogramWrapper;
import no.met.jtimeseries.chart.ChartPlottingInfo;

/**
 * This class is to generate a time series chart for forecast of land location.
 * This chart is supposed to be like forecast chart at yr.no as much as possible
 *
 *
 */
public class Meteogram extends AbstractChart {
    
    private ChartPlottingInfo cpi;
    
    private int numHours;
    
    public Meteogram(ChartPlottingInfo chartPlotingInfo, int numHours) {
        this.cpi = chartPlotingInfo;   
        this.width=this.cpi.getWidth();
        this.numHours = numHours;
    }    

    @Override
    public void drawChart() {        
        MeteogramWrapper wrapper = new MeteogramWrapper(cpi.getLanguage());
        chart = wrapper.createMeteogram(cpi, numHours);
    }

}
