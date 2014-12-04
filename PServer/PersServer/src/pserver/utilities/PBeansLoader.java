/*
 * Copyright 2013 IIT , NCSR Demokritos - http://www.iit.demokritos.gr,
 *                            SciFY NPO - http://www.scify.org
 *
 * This product is part of the PServer Free Software.
 * For more information about PServer visit http://www.pserver-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If this code or its output is used, extended, re-engineered, integrated,
 * or embedded to any extent in another software or hardware, there MUST be
 * an explicit attribution to this work in the resulting source code,
 * the packaging (where such packaging exists), or user interface
 * (where such an interface exists).
 *
 * The attribution must be of the form
 * "Powered by PServer, IIT NCSR Demokritos , SciFY"
 */

//===================================================================
// Preferences
//
// this class loads the pservlets with their names that are installed in pserver
//
//===================================================================
package pserver.utilities;

import java.io.*;
import java.util.*;

import pserver.*;
import pserver.algorithms.graphs.GraphClustering;
import pserver.algorithms.metrics.VectorMetric;
import pserver.data.PShashMap;
import pserver.data.PSproperties;
import pserver.pservlets.*;

/**
 *
 * @author scify
 */
public class PBeansLoader {

    private String file;  //the full pathname of file
    private String header;  //header in file
    //program options that persist
    private PSproperties pref = new PSproperties();  //user defined values
//    private HashMap<String, PService> pservlets = null;
    private PShashMap<String, PService> pservlets = null;
    private HashMap<String, VectorMetric> vMetrics = null;
    private HashMap<String, GraphClustering> gClustering = null;

    /**
     *
     * @param file
     * @param header
     */
    public PBeansLoader(String file, String header) {
        this.file = (new File(file)).getAbsolutePath();  //convert to absolute path
        this.header = header;
        pref.clear();
        load();
        Enumeration e = pref.propertyNames();
//      pservlets = new HashMap<String, PService>();
        pservlets = new PShashMap<String, PService>();
        vMetrics = new HashMap<String, VectorMetric>();
        gClustering = new HashMap<String, GraphClustering>();

        while (e.hasMoreElements()) {

            String pBeanName = (String) e.nextElement();               //e.g ps
            String paramName = (String) pref.getProperty(pBeanName); //e.g pserver.algorithms.metrics.CosineVectorMetric();
            int pos = ((String) pref.getProperty(pBeanName)).indexOf('(');
            String className;                                          //e.g pserver.algorithms.metrics.PearsonCorrelationMetric
            if (pos != -1) {
                className = paramName.substring(0, pos);
            } else {
                className = paramName;
            }
            //WebServer.win.log.forceReport( "" + pServletName + " == " + className );
            try {
                Class serviceClass = Class.forName(className);
                Class[] interfaces = serviceClass.getInterfaces();
                try {
                    for (int j = 0; j < interfaces.length; j++) {
                        //System.out.println( " class is " + interfaces[j].getName() );
                        if (interfaces[j].getName().endsWith("pserver.pservlets.PService")) { //loads the pservlet beans
                            PService service = (PService) serviceClass.newInstance();
                            if (pos != -1) {
                                StringTokenizer params = new StringTokenizer(paramName.substring(pos + 1, paramName.length() - 1), ",");
                                String[] parameters = new String[params.countTokens()];
                                int i = 0;
                                while (params.hasMoreTokens()) {
                                    parameters[i] = params.nextToken();
                                    i++;
                                }
                                service.init(parameters);
                            } else {
                                service.init(null);
                            }
                            WebServer.win.log.forceReport("loaded PServlet: " + className + " with name " + pBeanName);
                            pservlets.put(pBeanName.toLowerCase(), service);
                        } else if (interfaces[j].getName().endsWith("pserver.algorithms.metrics.VectorMetric")) { //loads the metric beans
                            VectorMetric metric = (VectorMetric) serviceClass.newInstance();
                            WebServer.win.log.forceReport("loaded Vector Metric: " + className + " with name " + pBeanName);
                            vMetrics.put(pBeanName.toLowerCase(), metric);
                        } else if (interfaces[j].getName().endsWith("pserver.algorithms.graphs.GraphClustering")) { //loads the metric beans
                            GraphClustering clustering = (GraphClustering) serviceClass.newInstance();
                            WebServer.win.log.forceReport("loaded Graph Clustering algorithm: " + className + " with name " + pBeanName);
                            gClustering.put(pBeanName.toLowerCase(), clustering);
                        }
                    }
                } catch (Exception ex) {
                    WebServer.win.log.debug(ex.toString());
                    pservlets = null;
                    return;
                }
            } catch (ClassNotFoundException ex) {
                WebServer.win.log.debug(ex.toString());
                pservlets = null;
                return;
            }
        }
    }

    /**
     *
     * @return
     */
    public PShashMap<String, PService> getPServlets() {
        return this.pservlets;
    }

    /**
     *
     * @param name
     * @return
     */
    public String getPref(String name) {
        return pref.getProperty(name);  //null if not there
    }

    /**
     *
     * @return
     */
    public String[] getProperties() {
        Enumeration e = pref.propertyNames();
        Vector<String> elements = new Vector<String>();
        while (e.hasMoreElements()) {
            elements.addElement((String) e.nextElement());
        }
        return (String[]) elements.toArray(new String[0]);
    }

    //load and save
    /**
     *
     */
    public void load() {
        try {
            FileInputStream in = new FileInputStream(file);
            pref.load(in);
            String[] properties = getProperties();
        } catch (Exception e) {
            WebServer.win.log.forceReport("Configuration file had bad entries, a new one will be created");
        }
    }

    /**
     * @return the vMetrics
     */
    public HashMap<String, VectorMetric> getVMetrics() {
        return vMetrics;
    }
    

    /**
     * @return the gClustering
     */
    public HashMap<String, GraphClustering> getGClustering() {
        return gClustering;
    }
}
