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
// PSReqWorker
//
// Personalization Server Request Worker 'PSReqWorker' class is a
// subclass of 'ReqWorker'. It extends the functionality of a basic
// web server by overriding two superclass methods, and implements
// a special Pers. Server protocol.
//
// Two main modes are offered: Personal ,Stereotypes and Communities. Each mode
// supports a number of operations that can be performed by issuing
// suitable HTTP requests to the Personalization Server. The
// modes are indepe=dent from each other. They share the same
// database, however they are supported by a separate set of DB
// tables.
//
// The syntax of those special requests are as follows:
//     http://server:port/<mode_id>?<query_string>
// For Personal mode the mode ID is "pers",for Stereotypes
// mode the mode ID is "ster" and for Communities the mode ID
// is "commu". The query string identifies the
// operation and its parameters, and is described separetely for
// each operation. Reserved words in the query string are case
// independent.
//
// All names in the query string must conform to the syntax defined
// on URLs, that is, they must not contain spaces, +, =, &, ?, etc.
// In addition, to avoid confusion with XML syntax, the characters
// < and > are also not allowed.
//
// The answer is formatted as XML. The XML answer must have a single
// element called <result>, encompassing a number of <row> elements,
// each containing the data elements corresponding to the specific
// request. By having such a standard format it becomes easier for
// the applications to parse all XML answers in a uniform way.
//
// For error handling, a number of HTTP error codes are used to
// denote success, client error (wrong request), or server error.
//===================================================================
package pserver.logic;

import java.net.*;
import java.sql.*;
import java.util.*;
//import javax.swing.*;
import java.security.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import pserver.*;
import pserver.data.DBAccess;
import pserver.pservlets.*;

/**
 *
 * @author scify
 * @author Nick Zorbas <nickzorb@gmail.con>
 */
public class PSReqWorker extends ReqWorker {
    //modes of response specify process of responding.
    //declare additional (regarding base class) response modes
    //static public final int ADMIN_MODE = 2;  //personal mode

    /**
     *
     */
    static public final int SERVICE_MODE = 2;  //personal mode
    //static public final int PERS_MODE = 3;  //personal mode
    //static public final int STER_MODE = 4;  //stereotype mode
    //static public final int COMMU_MODE = 5;  //communities mode
    //variables relevant to response body
    private StringBuffer response = null;  //the body of the response
    private String format = null;          //format, in form of file extension
    //database JDBC connection parameters
    private String url;         //JDBC string specifying the data source
    private String user;
    private String pass;
    //private int perm;           //permissions for client
    private String clientName;  //the name of the client that did the request
    //private String administrator_name;//login name for administrator
    //private String administrator_pass;//login password for administrator    
    //initializers
    HashMap<String, String> clientsHash = new HashMap<String, String>();

    /**
     * Initializer method for the Request.
     *
     * @param sock Socket for particular client request.
     */
    public PSReqWorker(Socket sock) {
        super(sock);
        url = ((PersServer) PersServer.pObj).dbUrl;  //casting to subclass necessary
        user = ((PersServer) PersServer.pObj).dbUser;
        pass = ((PersServer) PersServer.pObj).dbPass;
        //allowAnonymous=((PersServer)PersServer.obj).allowAnonymous;
        //administrator_name = ( ( PersServer ) PersServer.pObj ).administrator_name;
        //administrator_pass = ( ( PersServer ) PersServer.pObj ).administrator_pass;
        //db = ((PersServer)PersServer.obj).dbType;
    }

    //overriden base class methods
    /**
     *
     */
    @Override
    public void switchRespMode() {
        //overriden method, extends modes of operation (respMode).
        //initiates a sequence of methods in which the actions
        //appropriate to client request are performed and the
        //body of the response (if any) is decided.
        //if an error occurs, the 'respCode' can be set accordingly

        //TODO: Check if contains resURI
        if (PersServer.pObj.pservlets.containsKey(resURI.toLowerCase().substring(1))) {
            respMode = SERVICE_MODE;
            System.out.println(PersServer.pObj.pservlets.get(resURI.toLowerCase().substring(1)).toString() + "%%%%%%%%%%%%%%%%%%%%%%%");
            analyzeServiceMode(PersServer.pObj.pservlets.get(resURI.toLowerCase().substring(1)));
            return;
        }
        //if the following line is removed, the server
        //will not return requested files in FILE_MODE.
        //the response will consist only of http header
        super.switchRespMode();  //mode set by base class
    }

    /**
     *
     */
    @Override
    public void switchRespBody() {
        //overriden method, "plugs-in" response body (if any), length,
        //and MIME type, depending on the mode of operation (respMode).
        //this method is called only if no error has occured ('respCode'
        //is 'NORMAL', which corresponds to HTTP '200 OK').
        switch (respMode) {
            case SERVICE_MODE:
                if (response != null) {
                    rbString = response.substring(0);
                    //rbLength = rbString.length();
                    try {
                        //rbLength = rbString.length();
                        rbLength = rbString.getBytes("UTF-8").length;
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                    }
                }
                //else response variables (rbString etc.) remain null
                break;
            //else delegate to base class
            default:
                super.switchRespBody();
                break;
        }
    }

    /**
     * Method for manipulate the pservices
     *
     * @param servlet a {@link pserver.pservlets.PService PService} interface.
     */
    private void analyzeServiceMode(PService servlet) {
        //Connection conn = connDB(url, user, pass);
//        DBAccess dbAccess = new DBAccess(url, user, pass);
        DBAccess dbAccess = DBAccess.getInstance(url, user, pass);
        if (resURI.substring(1).endsWith(".xml") || resURI.substring(1).endsWith(".json")) {
            try {

                servlet.init(initParam);
            } catch (Exception ex) {
                Logger.getLogger(PSReqWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        mimeType = servlet.getMimeType();
        response = new StringBuffer();
        respCode = servlet.service(queryParam, response, dbAccess);
    }

//    ====================================================
//    NOT USED
//    private Connection connDB
//    private void disconnDB
//    ====================================================
    /**
     * Database connection method
     *
     * @param DBUrl JDBC connection string
     * @param DBUser user alias
     * @param DBPass user password
     * @return A {@link java.sql.Connection Connection} interface.
     */
    private Connection connDB(String DBUrl, String DBUser, String DBPass) {
        //connect to database, return null if unable
        Connection conn = null;
        try {
            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            conn = DriverManager.getConnection(DBUrl, DBUser, DBPass);
            //} catch(ClassNotFoundException e) {  //no connection established, stop
            //  WebServer.win.log.error(port + "-Problem connecting to DB: " + e);
            //return null;
        } catch (SQLException e) {
            WebServer.win.log.error("-Problem connecting to DB: " + e);
            return null;
        }
        return conn;
    }

    /**
     * Database disconnect method
     *
     * @param conn A {@link java.sql.Connection Connection} interface.
     */
    private void disconnDB(Connection conn) {
        //disconnect from database
        try {
            conn.close();
        } catch (SQLException e) {
            WebServer.win.log.error("-Problem disconnecting from DB: " + e);
        }
    }
}
