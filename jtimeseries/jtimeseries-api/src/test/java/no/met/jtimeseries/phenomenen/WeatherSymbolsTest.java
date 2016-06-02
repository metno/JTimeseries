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
package no.met.jtimeseries.phenomenen;


public class WeatherSymbolsTest {
/*
    @Test
    public void testGetTimeList() {
        final int nSymbols = 10;
        WeatherSymbols w = getTestSymbols(2, nSymbols);
        List<Date> times = getTestTimes(2, nSymbols);
        List<Date> symbolTimes = w.getTimeList();
        assertTrue(symbolTimes.size()==nSymbols);
        assertTrue(symbolTimes.containsAll(times));
    }

    @Test
    public void testFilterByValidHours() {
        final int nSymbols = 10;
        WeatherSymbols w = getTestSymbols(1, nSymbols);
        WeatherSymbols w2 = getTestSymbols(2, nSymbols);
        WeatherSymbols w3 = getTestSymbols(3, nSymbols);
        WeatherSymbols w6 = getTestSymbols(6, nSymbols);

        w.addAllSymbols(w2.getSymbols());
        w.addAllSymbols(w3.getSymbols());
        w.addAllSymbols(w6.getSymbols());
        
        w.filterByValidHours(2);
        assertTrue(w.getSymbols().size() == 10);
        w.filterByValidHours(1);
        assertTrue(w.getSymbols().size() == 0);
    }

    @Test
    public void testFilterByTimeList() {
        final int nSymbols = 10;
        WeatherSymbols w = getTestSymbols(2, nSymbols);
        final int nKeep = 7;
        List<Date> times = getTestTimes(2, nKeep);
        // adding time not in symbols
        times.add(new Date(0));
        w.filterByTimeList(times);
        assertTrue(w.getSymbols().size() == nKeep);
    }

    @Test
    public void testGetBestValidHour() {
        final int nSymbols = 10;
        WeatherSymbols w = getTestSymbols(1, nSymbols);
        WeatherSymbols w2 = getTestSymbols(2, nSymbols);
        WeatherSymbols w3 = getTestSymbols(3, nSymbols);
        WeatherSymbols w6 = getTestSymbols(6, nSymbols);

        w.addAllSymbols(w2.getSymbols());
        w.addAllSymbols(w3.getSymbols());
        w.addAllSymbols(w6.getSymbols());
        
        assertTrue(w.getBestValidHour(1, 10) == 1);
        assertTrue(w.getBestValidHour(2, 10) == 2);
        assertTrue(w.getBestValidHour(4, 6) == 6);
        
        w.clear();
        w.addAllSymbols(w3.getSymbols());
        assertTrue(w.getBestValidHour(1, 10) == 3);
        assertTrue(w.getBestValidHour(4, 6) == 6);
        w.addAllSymbols(w6.getSymbols());
        assertTrue(w.getBestValidHour(1, 10) == 3);
        
    }
    
    private static List<Date> getTestTimes(int validHours, int n) {
        ArrayList<Date> l = new ArrayList<Date>();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2000, 1, 1, 0, 0);
        for (int i=0; i<n; i++) {
            l.add( new Date(cal.getTimeInMillis()) );
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }
        return l;
    }
    
    private static WeatherSymbols getTestSymbols(int validHours, int n) {
        WeatherSymbols w = new WeatherSymbols();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2000, 1, 1, 0, 0);
        for (int i=0; i<n; i++) {
            Date from = new Date(cal.getTimeInMillis());
            Date to = new Date(cal.getTimeInMillis() + (validHours * 3600000));
            w.addWeatherSymbol(from, to, 1);
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }
        return w;
    }
*/
}
