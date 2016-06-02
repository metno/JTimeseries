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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;

/**
 * Create svg files from a JFreechart
 */
public class SvgChartSaver extends AbstractChartSaver {

	public SvgChartSaver() {
		super(".svg");
	}
	
	@Override
	public void save(File file, JFreeChart chart, int width, int height)
			throws IOException {
		
        if (file == null || chart == null)
            throw new IllegalArgumentException("Null 'file' or 'chart' argument.");
        //get the genric dom imp
        DOMImplementation dom = GenericDOMImplementation.getDOMImplementation();
        //create document
        org.w3c.dom.Document document = dom.createDocument(null, "svg", null);
        //create svg 2d graphics
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        svgGenerator.getGeneratorContext().setPrecision(6);
        svgGenerator.setSVGCanvasSize(new Dimension(width, height));
        //render chart with svg 2D graphics        
        chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, width, height));
        Element svgRoot = svgGenerator.getRoot();
        /// set SVG Canvas size (For auto resizing)
        svgRoot.setAttributeNS(null, SVGGraphics2D.SVG_VIEW_BOX_ATTRIBUTE, String.format("0 0 %d %d", width, height));
        svgRoot.setAttributeNS(null, "preserveAspectRatio", "xMidYMid meet");
        svgRoot.removeAttribute("width");
        svgRoot.removeAttribute("height");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        svgGenerator.stream(svgRoot, writer, false, true);
    }
}
