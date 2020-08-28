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
package no.met.jtimeseries.chart;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import no.met.halo.common.LogUtils;



public class Symbols {
    // set the folds of the symbol images

    private static final Logger logger = Logger.getLogger(Symbols.class.getName());
    
    private static String serviceUrl = "http://localhost:8080/jtimeseries-webservices/";
    private static String symbolFilePath = "/images/symbols/";
    private static String windFilePath = "/disk1/project/timeserieschart/source/jtimeseries/jtimeseries-webservices/src/main/webapp/images/wind/";

    public Symbols() {
    }

    /**
     * Get symbol image object for the specified weather symbol
     * 
     * @param int The index (also the name) of the symbol.
     * 
     * @return Symbol image.
     */
    public static Image getSymbolImage(int i) {
        BufferedImage bufferedImage = null;
        StringBuilder imageName = new StringBuilder();
        imageName.append(symbolFilePath);
        imageName.append(String.valueOf(i));
        imageName.append(".png");
        try {
            InputStream in = Symbols.class.getResourceAsStream(imageName.toString());
            bufferedImage = ImageIO.read(in);
        } catch (IOException e) {
            LogUtils.logException(logger, "Failed to read symbol image for i'the image i=" + i, e);
        }

        return bufferedImage;
    }

    public static Image getSymbolImage(String name) {
        BufferedImage bufferedImage = null;
        StringBuilder imageName = new StringBuilder();
        imageName.append(symbolFilePath);
        imageName.append(String.valueOf(name));
        imageName.append(".png");
        try {
            InputStream in = Symbols.class.getResourceAsStream(imageName.toString());
            bufferedImage = ImageIO.read(in);
        } catch (IOException e) {
            LogUtils.logException(logger, "Failed to read symbol image for i'the image i=" + name, e);
        }

        return bufferedImage;
    }

    /**
     * Get symbol image object for the specified wind symbol
     * 
     * @param String
     *            The name of the wind symbol.
     * 
     * @param double The degree of the wind direction.
     * 
     * @return wind image.
     */
    public static Image getWindImage(String title, double degree) {
        BufferedImage bufferedImage = null;
        StringBuilder imageName = new StringBuilder();
        imageName.append(windFilePath);
        imageName.append(title);
        imageName.append(".png");
        try {
            InputStream ins = Symbols.class.getResourceAsStream(imageName.toString());
            bufferedImage = ImageIO.read(ins);
            bufferedImage = rotateImage(bufferedImage, degree);
        } catch (IOException e) {
            LogUtils.logException(logger, "Failed to read wind symbol image: " + title + ", " + degree, e);
        }
        return bufferedImage;
    }

    public static Image getArrowImage(double degree){
    	return getWindImage("Flau vind",degree);
    }
    
    /**
     * Rotate the wind image object with the specified degree
     * 
     * @param BufferedImage
     *            The image object of the wind symbol.
     * 
     * @param double The degree of the wind direction.
     * 
     * @return wind image.
     */
    private static BufferedImage rotateImage(BufferedImage bufferedimage, double degree) {
        int width = bufferedimage.getWidth();
        int height = bufferedimage.getHeight();
        int type = bufferedimage.getColorModel().getTransparency();
        BufferedImage bufferedImage;
        Graphics2D graphics2d;
        (graphics2d = (bufferedImage = new BufferedImage(width, height, type)).createGraphics()).setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2d.rotate(Math.toRadians(degree + 45 + 90), width / 2, height / 2);
        graphics2d.drawImage(bufferedimage, 0, 0, null);
        graphics2d.dispose();
        return bufferedImage;
    }

    /**
     * Get the symbol file path for the specified weather symbol
     * 
     * @param int The index (also the name) of the symbol.
     * 
     * @return file path.
     */
    @SuppressWarnings("unused")
    private static String getSymbolFilePath(int number) {
        return getRealPath() + symbolFilePath + new Integer(number).toString() + ".png";
    }

    /**
     * Get the symbol file path for the specified wind symbol
     * 
     * @param String
     *            The name of the symbol.
     * 
     * @return file path.
     */
    @SuppressWarnings("unused")
    private static String getWindDirectionFilePath(String title) {
        return getRealPath() + windFilePath + title + ".png";
    }

    /**
     * Get the real path of the web application on the disk
     * 
     * @return file path.
     */
    private static String getRealPath() {
        return serviceUrl;
    }
    
    public static Image getImage(String absolutePath) {
        BufferedImage bufferedImage = null;        
        try {
            InputStream in = Symbols.class.getResourceAsStream(absolutePath);
            bufferedImage = ImageIO.read(in);
        } catch (IOException e) {
            LogUtils.logException(logger, "Failed to read image: " + absolutePath, e);
        }

        return bufferedImage;
    }

}
