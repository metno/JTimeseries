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
package no.met.jtimeseries.netcdf;

import static org.junit.Assert.*;

import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.Vector;

import no.met.jtimeseries.data.item.NumberValueItem;
import no.met.phenomenen.NumberPhenomenon;

import org.junit.Test;

public class NetcdfChartProviderTest {

	@Test
	public void test() throws Exception {
		URL resource = getClass().getClassLoader().getResource("netcdf/cf_role.nc");
		List<String> parameters = new Vector<String>();
		parameters.add("gsl[Kongsvegen]");

		NetcdfChartProvider provider = new NetcdfChartProvider(resource.toString(), null, parameters, ParameterReference.VARIABLE_NAME, null);

		Vector<NumberPhenomenon> phenomena = provider.getWantedPhenomena(parameters);
		
		assertEquals(1, phenomena.size());
		NumberPhenomenon phenomenon = phenomena.get(0);

		NumberValueItem first = (NumberValueItem) phenomenon.getItem(0);
		assertEquals(336, first.getValue(), 0.00001);
		
		NumberValueItem last = (NumberValueItem) phenomenon.getItem(12);
		assertEquals(495, last.getValue(), 0.00001);
	}

	@Test
	public void testDates() throws Exception {
		URL resource = getClass().getClassLoader().getResource("netcdf/even_simpler.nc");
		List<String> parameters = new Vector<String>();
		parameters.add("air_potential_temperature[5][5]");
		NetcdfChartProvider provider = new NetcdfChartProvider(resource.toString(), null, parameters, ParameterReference.VARIABLE_NAME, null);

		provider.getWantedPhenomena(null);

		NumberPhenomenon phenomenon = provider.getWantedPhenomenon("air_potential_temperature[5][5]");
		assertNotNull(phenomenon);
		
		assertEquals(new Date(1308398400L * 1000), phenomenon.getStartTime());
		assertEquals(new Date(1308409202L * 1000), phenomenon.getEndTime());
	}

}
