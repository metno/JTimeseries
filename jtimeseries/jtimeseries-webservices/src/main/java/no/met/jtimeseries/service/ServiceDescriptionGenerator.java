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
package no.met.jtimeseries.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * Automatically generate web service documentation by reading the annotations
 * for a class.
 */
public class ServiceDescriptionGenerator {

    private static final Logger logger = Logger.getLogger(ServiceDescriptionGenerator.class.getName());

    /**
     * Create a description of the web service offered by a class in XML. The
     * documentation is created by looking at the web service annotations.
     *
     * @param c
     *            The class to generate documentation for.
     * @return A XML document object.
     */
    public static Document getXMLServiceDescription(Class<? extends Object> c) {

        List<Method> serviceMethods = getServiceMethods(c);

        DocumentFactory df = new DocumentFactory();
        Document xmlDoc = df.createDocument();

        Element rootElement = df.createElement("services");
        xmlDoc.add(rootElement);
        for (Method m : serviceMethods) {

            Element service = rootElement.addElement("service");
            MethodInfo mi = getMethodInfo(m);
            service.addAttribute("path", mi.path);

            if (mi.produces != null) {
                service.addAttribute("returmMimeType", StringUtils.join(mi.produces, ','));
            }

            if (mi.description != null) {
                service.addAttribute("description", mi.description);
            }

            List<ParameterInfo> params = getParameters(m);
            for (ParameterInfo pi : params) {
                Element param = service.addElement("parameter");
                param.addAttribute("name", pi.name);
                if (pi.defaultValue != null) {
                    param.addAttribute("defaultValue", pi.defaultValue);
                }
            }
        }

        return xmlDoc;
    }

    private static MethodInfo getMethodInfo(Method m) {

        MethodInfo mi = new MethodInfo();

        Path path = m.getAnnotation(Path.class);
        mi.path = path.value();

        Produces produces = m.getAnnotation(Produces.class);
        if (produces != null) {
            mi.produces = produces.value();
        }

        ServiceDescription description = m.getAnnotation(ServiceDescription.class);
        if (description != null) {
            mi.description = description.value();
        }

        return mi;

    }

    /**
     * @param c The class to search for methods in.
     * @return A list of method that is used to offer web services in the class.
     */
    private static List<Method> getServiceMethods(Class<? extends Object> c) {

        List<Method> serviceMethods = new ArrayList<Method>();

        for (Method m : c.getMethods()) {

            logger.info(m.getName());
            if (m.isAnnotationPresent(Path.class)) {
                serviceMethods.add(m);
            }
        }

        return serviceMethods;
    }

    /**
     * @param method The method to get information about.
     * @return Information about all the parameters to web service call to the method.
     */
    private static List<ParameterInfo> getParameters(Method method) {

        List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (Annotation[] annotationForParam : paramAnnotations) {

            ParameterInfo pi = new ParameterInfo();
            for (Annotation a : annotationForParam) {
                if (a instanceof QueryParam) {
                    pi.name = ((QueryParam) a).value();
                } else if (a instanceof DefaultValue) {
                    pi.defaultValue = ((DefaultValue) a).value();
                }
            }

            if (pi.name != null) {
                parameters.add(pi);
            }
        }

        return parameters;
    }

    private static class ParameterInfo {

        String name;
        String defaultValue;

    }

    private static class MethodInfo {

        String path;
        String produces[];
        String description;

    }

}
