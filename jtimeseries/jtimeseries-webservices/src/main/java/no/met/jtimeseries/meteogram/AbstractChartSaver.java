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

import java.io.File;
import java.io.IOException;

import org.jfree.chart.JFreeChart;

/**
 * Create a file with a representation of the given char
 */
public abstract class AbstractChartSaver {

	private static String FILE_PREFIX = "TimeSeriesChart-";
	private String defaultFileSuffix;
	
	public AbstractChartSaver(String fileSuffix) {
		this.defaultFileSuffix = fileSuffix;
	}
	
	
	/**
	 * Create a temporary file, and save the chart to that file
	 * @param chart the diagram to save
	 * @return a temporary file containing the saved image
	 * @throws IOException
	 */
	public File save(AbstractChart chart) throws IOException {
		return save(chart.chart, chart.width, chart.height);
	}
	
	/**
	 * Create a temporary file, and save the chart to that file
	 * 
	 * @param chart the diagram to save
	 * @param width generated image size, horizontally
	 * @param height generated image size, vertically
	 * @return a temporary file containing the saved image
	 * @throws IOException
	 */
    public File save(JFreeChart chart, int width, int height) throws IOException
    {
    	File toSave = getTempFile();
    	save(toSave, chart, width, height);
    	return toSave;
    }
	
	/**
	 * Save the given chart to a file
	 * 
	 * @param out The file to save data to
	 * @param chart the diagram to save
	 * @param width generated image size, horizontally
	 * @param height generated image size, vertically
	 * @throws IOException
	 */
    abstract public void save(File out, JFreeChart chart, int width, int height) throws IOException;
    
    
    /**
     * Create a temporary file
     */
    private File getTempFile() throws IOException {
        String tempDirName = System.getProperty("java.io.tmpdir");
        if (tempDirName == null) {
            throw new RuntimeException("Temporary directory system property "
                        + "(java.io.tmpdir) is null.");
        }
        // create a temporary directory if it doesn't exist
        File tempDir = new File(tempDirName);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        File tempFile = File.createTempFile(FILE_PREFIX, defaultFileSuffix, new File(System.getProperty("java.io.tmpdir")));
        return tempFile;
    }

}
