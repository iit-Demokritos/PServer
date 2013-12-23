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
/*
 * PersServer.java
 *
 * A general-puprose Personalization Server.
 *
 * Manages user profiles and preferences, in the frame of an
 * application. Helps the application to tailor itself to
 * its users.
 *
 * Extends YServer, a simple but expandable web server. YServer
 * supports multiple users, requests for file resources under a
 * public directory root, GET and POST HTTP methods, MIME types,
 * and loading application settings from an initialization file.
 *
 * The code starts with the web server part and the specialization
 * to PersServer follows. This specialization is done simply
 * by overloading two functions of the YServer (and by a lots of
 * other code).
 *
 * An extended documentation of the PersServer can be found by
 * running the server and connecting through a web browser to:
 *    http://persserver:port/
 * The port can be set at the server.ini initialization file.
 *
 * Yannis Stavrakas, 2001
 */
///////////////////////////////////////////////////////
//
//              FUTURE ENHANCEMENTS
//
// * add 'from' and 'to' date query parameters to 'calcdecay' request
// * add absolute time as option in 'calcdecay'
// * change every occurence of 'decaydata' to 'logdata'
// * make pers server modes different classes
// * separate in different files various components
//
//            * Also to web server *
//
// * provide a menu to change application settings of
//   .ini file while server is running
// * investigate using the Java HTTP classes for the
//   web server instead of plain sockets
//
///////////////////////////////////////////////////////
package pserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.sql.*;

import pserver.data.DBAccess;
import pserver.data.PServerClientsDBAccess;
import pserver.data.PShashMap;
import pserver.userInterface.*;
import pserver.logic.*;
import pserver.utilities.*;
import pserver.pservlets.*;

//===================================================================
// PersServer
//
// Main class surrogate, implemented as a subclass of 'WebServer'.
// Used mainly to convert name from 'WebServer' to 'PersServer', and
// to add a few member variables.
//===================================================================
/**
 *
 * Main class surrogate, implemented as a subclass
 * {@link pserver.WebServer WebServer.}
 *
 */
public class PersServer extends WebServer {
    //database JDBC connection parameters\

    /**
     *
     */
    public static final String QuitCommand = "quit";
    /**
     *
     */
    public String dbDriver;         //JDBC driver
    /**
     *
     */
    public String dbUrl;            //JDBC connection string
    /**
     *
     */
    public String dbUser;           //user alias
    /**
     *
     */
    public String dbPass;           //user password       
    //database product name
    /**
     *
     */
    public String dbType;    //eg. "ACCESS", "MySQL", etc.
    private boolean stop = false;    //variable for stop checking
    private static final String PBEANS_FILE_NAME = "./pbeans.ini";//the file that contains psrvlet name
//    public HashMap<String, PService> pservlets; //the pservlets
    /**
     *
     */
    public PShashMap<String, PService> pservlets;
    /**
     *
     */
    public static PersServer pObj;        //the single WebServer object
    /**
     *
     */
    public static PBeansLoader pbeansLoadader;

    /**
     * This is the main method of the application.
     *
     * @param args Command-line arguments
     */
    static public void main(String[] args) {
        pObj = new PersServer();    //application object is a PersServer now
        WebServer.obj = pObj;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            do {
                line = br.readLine().toLowerCase();
            } while (line.equals(QuitCommand) == false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PersServer.terminate(true);

    }

    /**
     * Initializer method.
     */
    protected PersServer() {
        start();
    }

    /**
     * This method starts the PersServer
     */
    public void start() {
        //initialize variables
        initVars();
        //initialize variables from application settings
        //(check 'Preferences.setDef()' for their default values
        // and the 'prefFile' text file for their actual values)
        pref = new Preferences(prefFile, prefHeader);
        loadSettings();
        //prepare log file
        flog = new FileLog(logPath, maxFLSize);
        //setup UI
        win = new ControlWin(appCapt);
        //load MIME data
        mime = new Properties();
        loadMime();
        srv = new WServer(port, maxReq, sslOn);
        //set if msgs appear on screen and/or log file,
        //and how detailed messages that appear on screen are
        setMessageMode();  //after WServer creation, to allow initial lines to appear anyway
        //report log mode anyway (irrespective of log mode)
        win.log.forceReport("Https set to: " + (sslOn ? "on" : "off"));
        win.log.forceReport("Messages log mode set to: " + logMode);
        win.log.forceReport("");  //en empty line for aesthetics
        flog.forceWriteln("Messages log mode set to: " + logMode);
        //load the pservlets
        loadPservlets();
        //check if database connection is OK, and get DB type
        dbType = getDBType();
        //if DB failure, report it (irrespectively of log mode) and quit
        if (dbType == null) {
            System.out.println("Database connection failed");
            flog.forceWriteln("Database connection failed");
            terminate(false);  //quit application if cannot connect to DB
        }
        //if DB connection OK, report it (irrespectively of log mode) and continue
        win.log.forceReport("Connected to DB: " + dbType);
        win.log.forceReport("");  //en empty line for aesthetics
        flog.forceWriteln("Connected to DB: " + dbType);
        srv.start();
    }
    //loafs pservlets into a hachmap to access them via the defined name found from the servlet.ini file

    /*
     * Load the pservlets
     *
     */
    private void loadPservlets() {
        win.log.forceReport("");  //en empty line for aesthetics
        win.log.forceReport("Loading PBeans");
        pbeansLoadader = new PBeansLoader(PBEANS_FILE_NAME, prefHeader);
        pservlets = pbeansLoadader.getPServlets();
        if (pservlets == null) {
            win.log.forceReport("There was error(s) while loading PBeans");
            terminate(false);
        }
        win.log.forceReport("");  //en empty line for aesthetics
    }

    /**
     * This method Stops the PersServer
     */
    private void stop() {
        stop = true;
    }

    //base class overridden methods
    @Override
    protected void initVars() {
        //initialize all
        super.initVars();
        //override variable values as needed
        appName = "PServer";
        appVers = "1.2";
        appCapt = " " + appName + " ver. " + appVers;
        prefHeader = appName + " settings";
    }

    /**
     *
     */
    @Override
    protected void loadSettings() {
        //load all settings of super class
        super.loadSettings();
        //load additional settings of this class
        dbDriver = pref.getPref("database_driver");
        dbUrl = new StringBuilder().append("jdbc:").append(pref.getPref("database_url")).toString();
        dbUrl += dbUrl.contains("?") ? "&" : "?";
        dbUrl += "useUnicode=true&characterEncoding=utf-8";
        dbUser = pref.getPref("database_user");
        dbPass = pref.getPref("database_pass");
        //allowAnonymous=(pref.getPref("anonymous")).equals("off")? false:true;        
    }

    /**
     * Method to get the database product name
     *
     * @return A String with the database product name
     */
    private String getDBType() {
        //get database product name:
        //-null if connection fails
        //-'MySQL' if database is MySQL
        //-'ACCESS' if database is MS-Access
        //- ... etc.
        DBAccess dbAccess = null;
        try {
            Class.forName(dbDriver);
//            dbAccess = new DBAccess(dbUrl, dbUser, dbPass);
            dbAccess = DBAccess.getInstance(dbUrl, dbUser, dbPass);
            //conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            dbAccess.connect();
            try {
                PServerClientsDBAccess.initialize(dbAccess);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (ClassNotFoundException e) {  //connection failed, return null
            System.out.println("\n" + e + "\n");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.out.println("\n" + e + "\n");
            e.printStackTrace();
            return null;
        }
        //get DB metadata
        String db;
        try {
            DatabaseMetaData dbMeta = dbAccess.getMetadata();
            db = dbMeta.getDatabaseProductName();
        } catch (SQLException e) {
            db = null;
            e.printStackTrace();
        }
        //disconnect anyway
        try {
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        //'db' can still be null
        return db;
    }

    /**
     * Creates a new
     * {@link pserver.data.DBAccess#DBAccess(java.lang.String, java.lang.String, java.lang.String) DBAccess}
     * object
     *
     * @return A
     * {@link pserver.data.DBAccess#DBAccess(java.lang.String, java.lang.String, java.lang.String) DBAccess}
     * object.
     */
    public DBAccess getNewDBAccess() {
        return DBAccess.getInstance(dbUrl, dbUser, dbPass);
    }
}
