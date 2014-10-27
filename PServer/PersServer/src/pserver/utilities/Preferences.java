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
// A general-puprose class
//
// Keeps persistent options of the application.
// Default values for options (used in case preferences file is
// not created yet) can be set in method setDef().
//===================================================================
package pserver.utilities;

import java.io.*;
import java.util.*;

import pserver.*;

/**
 *
 * @author scify
 * @author Nick Zorbas
 */
public class Preferences {

    private String file;  //the full pathname of file
    private String header;  //header in file
    //program options that persist
    private Properties defPref = new Properties();      //default values
    private Properties pref = new Properties();  //user defined values

    /**
     *
     * @param file
     * @param header
     */
    public Preferences(String file, String header) {
        this.file = (new File(file)).getAbsolutePath();  //convert to absolute path
        this.header = header;
        setDef();
        pref.clear();
        load();
    }
    //set default values

    private void setDef() {
        defPref.clear();
        //default values of WebServer class variables
        defPref.put("log_mode", "on");
        defPref.put("debug_mode", "on");
        defPref.put("main_html_dir", "./public");
        defPref.put("max_requests", "50");
        defPref.put("max_log_size", "50000000");
        defPref.put("port", "1111");
        defPref.put("ssl", "off");
        defPref.put("database_driver", "com.mysql.jdbc.Driver");
        defPref.put("database_url", "mysql://localhost/pserver?rewriteBatchedStatements=true");
        defPref.put("database_user", "pserver");
        defPref.put("database_pass", "pserver");
        defPref.put("read_request_timeout", "5000000");
        defPref.put("def_html_file", "index.html");
        defPref.put("log_file_path", "./server.log");
        defPref.put("thread_num", "5");
    }

    //restore defaults
//    public void restoreDef() {
//        pref.clear();  //defPref becomes active
//    }
    //get and set
    /**
     *
     * @param name
     * @param val
     */
    public void setPref(String name, String val) {
        pref.put(name, val);
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
        ArrayList<Object> elements = new ArrayList<Object>();
        while (e.hasMoreElements()) {
            elements.add(e.nextElement());
        }
        return (String[]) elements.toArray(new String[0]);
    }

    /**
     *
     * @return
     */
    public String[] getDefaultProperties() {
        Enumeration e = defPref.propertyNames();
        ArrayList<Object> elements = new ArrayList<Object>();
        while (e.hasMoreElements()) {
            elements.add(e.nextElement());
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
            String[] defProperties = getDefaultProperties();
            if (properties.length != defProperties.length) {
                throw new Exception();
            }
            for (int i = 0; i < properties.length; i++) {
                boolean found = false;
                for (int j = 0; i < defProperties.length; j++) {
                    if (properties[i].equals(defProperties[j])) {
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
//            WebServer.win.log.forceReport("Configuration file had bad entries, a new one will be created");
            //the first time, preferences file will not exist
            pref = defPref;
            store();
        }
    }

    /**
     *
     */
    public void store() {
        try {
            FileOutputStream out = new FileOutputStream(file);
            pref.store(out, header);
        } catch (IOException e) {
            WebServer.win.log.forceReport("Unable to store user preferences in " + file + ": " + e);
        }
    }
    //it does the same thing as the previous but it does not show a message dialog

    /**
     *
     * @return
     */
    public boolean silentStore() {
        try {
            FileOutputStream out = new FileOutputStream(file);
            pref.store(out, header);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
