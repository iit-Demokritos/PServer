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

package pserver.pservlets;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import pserver.WebServer;
import pserver.data.DBAccess;
import pserver.data.PServerResultSet;
import pserver.data.PStereotypesDBAccess;
import pserver.data.PUserDBAccess;
import pserver.data.VectorMap;
import pserver.domain.PAttribute;
import pserver.domain.PDecayData;
import pserver.domain.PFeature;
import pserver.domain.PNumData;
import pserver.logic.PSReqWorker;
import pserver.utilities.ClientCredentialsChecker;

/**
 * Contains all necessary methods for the management of Personal mode of
 * PServer.
 * 
 * @author scify
 * @author Nick Zorbas <nickzorb@gmail.con>
 */
public class Personal implements pserver.pservlets.PService {

    /**
     * Returns the mime type.
     *
     * @return Returns the XML mime type from Interface {@link PService}.
     */
    public String getMimeType() {
        return pserver.pservlets.PService.xml;
    }

    /**
     * Overridden method of init from {@link PService} Does nothing here.
     *
     * @param params An array of strings containing the parameters
     * @throws Exception Default Exception is thrown.
     */
    public void init(String[] params) throws Exception {
    }

    /**
     * Creates a service for Communities mode when a command is sent to PServer.
     * The command is identified from its name and proper methods for the
     * management of this command are called. A response code is produced
     * depending on results.
     *
     * @param parameters The parameters needed for this service.
     * @param response The response string that is created.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    public int service(VectorMap parameters, StringBuffer response, DBAccess dbAccess) {
        int respCode;
        VectorMap queryParam;

        StringBuffer respBody = response;
        queryParam = parameters;

        if (!ClientCredentialsChecker.check(dbAccess, queryParam)) {
            return PSReqWorker.REQUEST_ERR;  //no point in proceeding
        }

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        clientName = clientName.substring(0, clientName.indexOf('|'));
        queryParam.updateVal(clientName, clntIdx);

        //commANDs of PERS_MODE here!
        //find 'com' query param (case independent)
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //if 'com' param not present, request is invalid
        if (comIdx == -1) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request commAND does not exist");
            return respCode;  //no point in proceeding
        }
        //recognize commAND encoded in request
        String com = (String) queryParam.getVal(comIdx);
        System.out.println("================  " + com + "  ===========");
        //operations of features
        if (com.equalsIgnoreCase("addftr")) {       //add new feature(s)
            respCode = comPersAddFtr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("addattr")) { //add new attribute(s)
            respCode = comPersAddAttr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("remftr")) {  //remove feature(s)
            respCode = comPersRemFtr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("remAttr")) {  //remove attributes
            respCode = comPersRemAttr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("setdef") || com.equalsIgnoreCase("setftrdef")) {  //update the def value of ftr
            respCode = comPersSetFtrDef(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("setattrdef")) {  //update the def value of ftr
            respCode = comPersSetAttrDef(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getdef") || com.equalsIgnoreCase("getftrdef")) {  //get ftr(s) AND def val(s)
            respCode = comPersGetFtrDef(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getattrdef")) {
            respCode = comPersGetAttrDef(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("setusr")) {  //add AND update user
            respCode = comPersSetUsr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("incval")) {  //increment numeric values         
            respCode = comPersIncVal(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("setattr")) {
            respCode = comPersSetAttr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getusrs")) {  //get feature values for a user
            respCode = comPersGetUsrs(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getusr") || com.equalsIgnoreCase("getusrftr")) {  //get feature values for a user
            respCode = comPersGetUsrFtr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getusrattr")) {  //get feature values for a user
            respCode = comPersGetUsrAttr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("sqlusr") || com.equalsIgnoreCase("sqlftrusr")) {  //specify conditions AND select users
            respCode = comPersSqlUsrFtr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("sqlattrusr")) {
            respCode = comPersSqlUsrAttr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("remusr")) {  //remove user(s)
            respCode = comPersRemUsr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("setdcy")) {  //add AND update decay feature groups
            respCode = comPersSetDcy(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getdrt")) {  //get decay rate for a group
            respCode = comPersGetDrt(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("remdcy")) {  //remove decay feature groups
            respCode = comPersRemDcy(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("addddt")) {  //add new decay data
            respCode = comPersAddDdt(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("sqlddt")) {  //retrieve decay data under conditions
            respCode = comPersSqlDdt(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("remddt")) {  //remove decay data
            respCode = comPersRemDdt(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("caldcy")) {  //calculate decay values for a user
            respCode = comPersCalDcy(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("addndt")) {  //add new numeric data
            respCode = comPersAddNdt(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("sqlndt")) {  //retrieve numeric data under conditions
            respCode = comPersSqlNdt(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("remndt")) {  //remove numeric data
            respCode = comPersRemNdt(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getavg")) {  //calculate average values for a user
            respCode = comPersGetAvg(queryParam, respBody, dbAccess);
        } else {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request commAND not recognized");
        }

        return respCode;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, adds a attribute with its parameters to database
     * and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersAddAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {

        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);//transaction guarantees integrity
            //the new (feature, def value) pairs must be inserted, AND
            //the user attributes must be expANDed with the new features
            //-start transaction body
            success &= execPersAddAttr(queryParam, respBody, dbAccess);
            success &= persExpAndUserAttributes(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Adds a new attribute with the parameters specified.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if insertion of attributes was successful or false if an
     * error occurred.
     */
    private boolean execPersAddAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert each (attribute, def value) in a new row
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                    //'feature' cannot be empty string, 'queryParam' does not allow it                    
                    String attrName = ((String) queryParam.getKey(i));
                    if (DBAccess.legalFtrOrAttrName(attrName) == true) {  //check if name is legal
                        PAttribute attr = new PAttribute();
                        attr.setName(attrName);
                        String defValue = (String) queryParam.getVal(i);
                        attr.setValue(defValue);
                        attr.setDefValue(defValue);

                        rowsAffected += dbAccess.insertNewAttribute(attr, clientName);
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if (!success) {
                    break;
                }
            }
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        //format response body
        //response will be used only in case of success        
        respBody.append("<?xml version=\"1.0\"?>\n");
        respBody.append("<result>\n");
        respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
        respBody.append("</result>");
        return success;
    }

    /**
     * Method referring to command and execution part of process.
     *
     * Connects to database, adds attributes according to a user with the
     * parameters specified to database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean persExpAndUserAttributes(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert new features in user profiles accordingly
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                    //'attribute' cannot be empty string, 'queryParam' does not allow it
                    String attribute = (String) queryParam.getKey(i);
                    String defValue = (String) queryParam.getVal(i);
                    //if (db.compareTo("ACCESS") == 0) {  //database type is MS-Access
                    query = "insert into user_attributes " + "(user, attribute, attribute_value, FK_psclient)" + " select user, '" + attribute + "', '" + defValue + "', '" + clientName + "' from users WHERE FK_psclient='" + clientName + "'";
                    dbAccess.executeUpdate(query);
                }
            }
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
        }
        WebServer.win.log.debug("-Rows inserted in user_attributes: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, adds decay data (timestamp) for the specified user
     * and features to database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersAddDdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //first connect to DB
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success;
            dbAccess.setAutoCommit(false);  //transaction guarantees integrity
            //-start transaction body
            success = execPersAddDdt(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //problem with client request
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Adds new decay data with the parameters specified in database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if insertion of decay data was successful or false if an
     * error occurred.
     */
    private boolean execPersAddDdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        int sidIdx = queryParam.qpIndexOfKeyNoCase("sid");
        if (usrIdx == -1) {
            return false;
        }
        String user = (String) queryParam.getVal(usrIdx);
        int sid;
        if (sidIdx != -1) {
            try {
                sid = Integer.parseInt((String) queryParam.getVal(sidIdx));
            } catch (NumberFormatException e) {
                Logger.getLogger(Personal.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
        } else {
            try {
                sid = dbAccess.getLastSessionId(user, clientName);
            } catch (SQLException ex) {
                Logger.getLogger(Personal.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        //execute request
        boolean success = true;
        int rowsAffected = 0;
        try {
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != usrIdx && i != clntIdx && i != sidIdx) {  //'com' AND 'usr' query parameters excluded
                    //get current parameter pair
                    String feature = (String) queryParam.getKey(i);
                    String strTimestamp = (String) queryParam.getVal(i);
                    Long timestamp = DBAccess.timestampPattern(strTimestamp);
                    if (timestamp != null) {  //if null, 'timestamp' not numeric, misspelled request
                        //insert current (user, feature, timestamp) tuple
                        PDecayData data = new PDecayData(user, feature, timestamp, sid);
                        rowsAffected += dbAccess.insertNewDecayData(data, clientName);
                        //else if timestamp is null
                    } else {
                        success = false;
                    }  //misspelled request, abort AND rollback
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
            //close statement
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        //format response body
        //response will be used only in case of success        
        respBody.append("<?xml version=\"1.0\"?>\n");
        respBody.append("<result>\n");
        respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
        respBody.append("</result>");
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, adds features with the parameters specified to
     * database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersAddFtr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //first connect to DB
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);  //transaction guarantees integrity
            //the new (feature, def value) pairs must be inserted, AND
            //the user profiles must be expANDed with the new features
            //-start transaction body
            success &= execPersAddFtr(queryParam, respBody, dbAccess);
            //success &= persExpANDProfiles( queryParam, respBody, dbAccess );
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.warn("-DB Transaction problem: " + e);
        }
        //disconnect from DB anyway
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Adds new features with the parameters specified in database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if insertion of features was successful or false if an error
     * occurred.
     */
    private boolean execPersAddFtr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert each (feature, def value) in a new row
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                    //'feature' cannot be empty string, 'queryParam' does not allow it
                    String feature = (String) queryParam.getKey(i);
                    if (DBAccess.legalFtrOrAttrName(feature)) {  //check if name is legal
                        String defValue = (String) queryParam.getVal(i);
                        PFeature featureObj = new PFeature(feature, defValue, defValue);
                        rowsAffected += dbAccess.insertNewFeature(featureObj, clientName);
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        //format response body
        //response will be used only in case of success
        respBody.append("<?xml version=\"1.0\"?>\n");
        respBody.append("<result>\n");
        respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
        respBody.append("</result>");
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, adds numeric data (timestamp, session id) for the
     * specified user and features to database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersAddNdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //first connect to DB
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success;
            dbAccess.setAutoCommit(false);  //transaction guarantees integrity
            //-start transaction body
            success = execPersAddNdt(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //problem with client request
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Adds new numeric data with the parameters specified in database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if insertion of numeric data was successful or false if an
     * error occurred.
     */
    private boolean execPersAddNdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        int sidIdx = queryParam.qpIndexOfKeyNoCase("sid");
        if (usrIdx == -1) {
            return false;
        }
        String user = (String) queryParam.getVal(usrIdx);
        int sid;
        if (sidIdx == -1) {
            try {
                sid = dbAccess.getLastSessionId(user, clientName);
            } catch (SQLException ex) {
                Logger.getLogger(Personal.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        } else {
            try {
                sid = Integer.parseInt((String) queryParam.getVal(sidIdx));
            } catch (NumberFormatException e) {
                Logger.getLogger(Personal.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
        }

        int tmsIdx = queryParam.qpIndexOfKeyNoCase("tms");
        String strTimestamp;
        if (tmsIdx == -1) {
            strTimestamp = new String("-");
        } else {
            strTimestamp = (String) queryParam.getVal(tmsIdx);
        }
        Long timestamp = DBAccess.timestampPattern(strTimestamp);
        if (timestamp == null) {
            return false;
        }
        //if null, 'timestamp' not numeric, misspelled request
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert all (user, feature, timestamp, value) tuples
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != usrIdx && i != tmsIdx && i != clntIdx) {  //'com', 'usr', 'tms', 'clnt' query parameters excluded
                    //get current parameter pair
                    String feature = (String) queryParam.getKey(i);
                    String strValue = (String) queryParam.getVal(i);
                    Float value = DBAccess.strToNum(strValue);
                    if (value != null) {  //if null, 'value' not numeric, misspelled request
                        //insert current (user, feature, timestamp, value) tuple
                        PNumData numData = new PNumData(user, feature, value.floatValue(), timestamp, sid);
                        rowsAffected += dbAccess.insertNewNumData(numData, clientName);
                    } //else if value is null
                    else {
                        success = false;
                    }  //misspelled request, abort AND rollback
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
            //close statement
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, calculates the decay data rate for a specific user,
     * feature group and decay rate from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersCalDcy(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersCalDcy(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Calculates the decay data and sorts them by their timestamp of a
     * specified user, feature group and features from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if process of decay data was successful or false if an error
     * occurred.
     */
    private boolean execPersCalDcy(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        if (qpSize < 3 || qpSize > 5) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        int grpIdx = queryParam.qpIndexOfKeyNoCase("grp");
        int drtIdx = queryParam.qpIndexOfKeyNoCase("drt");
        int numIdx = queryParam.qpIndexOfKeyNoCase("num");
        if (usrIdx == -1 || grpIdx == -1) {
            return false;
        }  //must exist
        String clientName = (String) queryParam.getVal(clntIdx);
        String user = (String) queryParam.getVal(usrIdx);
        String group = (String) queryParam.getVal(grpIdx);
        //transform group to a condition that matches features
        String ftrCondition = DBAccess.ftrGroupCondition(group);
        //decay rate query param: decide use of default, convert to numeric, validate
        String rateStr;
        if (drtIdx == -1) {                       //decay rate absent
            rateStr = getDecayRate(dbAccess, group, clientName);  //look it up in DB
            if (rateStr == null) {
                rateStr = "0";
            }   //not in DB, use default value
        } else //decay rate specified in request
        {
            rateStr = (String) queryParam.getVal(drtIdx);
        }
        Float rateDbl = DBAccess.strToNum(rateStr);                        //converts string to Double
        if (rateDbl == null) {
            return false;
        }  //if null, 'rate' not numeric, discontinue request
        double rate_dbl = rateDbl.doubleValue();
        if (rate_dbl < 0 || rate_dbl > 1) {
            return false;
        }                  //discontinue request
        float rate = (float) rate_dbl;
        //'num' query param: decide use of default, convert to numeric, validate
        String numOfResultsStr = (numIdx == -1) ? "*" : (String) queryParam.getVal(numIdx);
        int numOfResults = DBAccess.numPatternCondition(numOfResultsStr);
        if (numOfResults == -1) {
            return false;
        }         //'num' not numeric, discontinue request
        //variables for the calculation of decay values
        VectorMap ftrVisits = null;       //user visiting features: (feature, timestamp)
        VectorMap ftrDecayValues = null;  //(feature, decay value) pairs
        //populate 'ftrVisits' with (feature, timestamp) pairs,
        //for specified user AND feature group, ordered by timestamp desc
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //prepare query
            query = "select dd_feature, dd_timestamp from decay_data WHERE dd_user='" + user + "' AND dd_feature" + ftrCondition + " AND FK_psclient='" + clientName + "' order by dd_timestamp desc";
            //count number of tuples
            PServerResultSet rs = dbAccess.executeQuery(query);
            while (rs.next()) {
                rowsAffected++;
            }
            //rs.close();
            //retrieve matching records AND populate 'ftrVisits'
            ftrVisits = new VectorMap(rowsAffected);
            //rs = dbAccess.executeQuery( query );        //reopen to reposition cursor at start
            rs.getRs().beforeFirst();
            while (rs.next()) {
                String feature = rs.getRs().getString("dd_feature");            //cannot be null
                Long timestamp = new Long(rs.getRs().getLong("dd_timestamp"));  //cannot be null
                ftrVisits.add(feature, timestamp);
            }
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        //if array 'ftrVisits' populated, proceed to
        //calculate decay values AND format response
        rowsAffected = 0;
        if (success) {  //proceed if no problem with query
            //calculate values: result array with (distinct feature, decay value) pairs
            //for the specified user, ftr group, AND rate, ordered by decay value desc.
            //'ftrVisits' should be null here, but 'ftrDecayValues' is checked for null.
            ftrDecayValues = calcDecayValues(ftrVisits, rate);
            if (ftrDecayValues == null) {
                WebServer.win.log.debug("-Problem calculating decay values");
                return false;
            }
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/decay_values.xsl"));
            respBody.append("<result>\n");
            //select first rows as specified by query parameter 'num'
            int i = 0;
            while (i < ftrDecayValues.size() && i < numOfResults) {
                String featureVal = (String) ftrDecayValues.getKey(i);
                Double valueVal = (Double) ftrDecayValues.getVal(i);
                respBody.append("<row><ftr>" + featureVal
                        + "</ftr><decay_val>" + valueVal.toString()
                        + "</decay_val></row>\n");
                i += 1;  //number of result rows
            }
            respBody.append("</result>");
            rowsAffected = i;
        }
        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
        return success;
    }

    /**
     * Returns the decay rate of a specific feature group from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return A string value of decay rate.
     */
    private String getDecayRate(DBAccess dbAccess, String group, String clientName) {
        //checks DB (table decay_groups) for the feature group 'group',
        //AND if there returns its corresponding decay rate, else null.
        String decayRate = null;  //init to null
        String query;
        try {
            //get decay rate of specified feature group
            //Statement stmt = conn.createStatement();
            query = "select dg_rate from decay_groups WHERE dg_group='" + group + "' AND FK_psclient='" + clientName + "'";
            PServerResultSet rs = dbAccess.executeQuery(query);
            if (rs.next()) //one or none 'group' can exist (primary key)
            {
                decayRate = String.valueOf(rs.getRs().getFloat("dg_rate"));
            }  //cannot be null
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Decay rate returned: " + decayRate);  //may be null
        return decayRate;
    }

    /**
     * Returns the calculation of decay values from the sequence of features and
     * timestamps.
     *
     * @param ftrVisits A vector map which has a sequence of features,
     * timestamps
     * @param rate The rate of decay which takes values between 0,1
     * @return The vector map containing the values of pair feature and decay
     * rate.
     */
    private VectorMap calcDecayValues(VectorMap ftrVisits, float rate) {
        //--------------------------------------------------------------------
        //'ftrVisits' is a sequence of (feature, timestamp) tuples ordered
        //by timestamp descenting (timestamp is not used, but it is included
        //for future use). Those represent the selections of a user (most
        //recently selected feature first). 'rate' is a number between (0,1)
        //both inclusive, that gives a measure of how much the interest of the
        //user for a feature decreases as new features are visited (higher
        //rates mean a stronger decrease of interest). The function returns
        //'ftrDecayValues', a sequence of (feature, decay value) pairs. The
        //features are those in 'ftrVisits', but only one occurrence of each
        //distinct feature exists in 'ftrDecayValues'. The decay value is a
        //metric of the interest of the user towards a feature in comparison
        //with all the other features. In case the rate is 0, the decay value
        //gives simply the number of visits of the user to the feature.
        //In case the rate is higher than 0, the decay rate takes into account
        //not only how many times a feature has been visited, but also when
        //it was visited in comparison to other features. Recently visited
        //AND / or frequently visited features receive higher decay values.
        //The 'ftrDecayValues' array is sorted by decay value descenting.
        //The method used for calculating the decay values is explained in
        //the paper "Adaptation to drifting user's interest" by Ivan Koychev
        //AND Ingo Schwab. In short, the decay value of a feature is the sum
        //of the weights for all visit of the feature. For every visit there
        //corresponds a weight which depends on the rate AND on the order
        //of the visit (number of visit in the sequence of all visits of all
        //features). The weight formula is: -((2*rate)/(total-1))*(i-1)+1+rate
        //WHERE total is the total number of visits the user paid to all
        //features AND i is the sequence number of the current visit.
        //We assume that the first value of i in the formula should be 1.
        //--------------------------------------------------------------------
        if (ftrVisits == null) {
            return null;
        }
        int visits = ftrVisits.size();
        VectorMap ftrDecayValues = new VectorMap(visits / 4, visits / 12);  //a mean of 4 visits per feature?
        //for each visit calculate weight(i, rate) AND
        //update (add to) decay value of corresponding feature
        for (int i = 0; i < visits; i++) {
            double weight = -((2 * rate * i) / (visits - 1)) + 1 + rate;  //the weight formula
            String feature = (String) ftrVisits.getKey(i);
            int pos = ftrDecayValues.indexOfKey(feature, 0);
            if (pos == -1) //feature does not exist, insert
            {
                ftrDecayValues.add(feature, new Double(weight));
            } else {          //feature exists, update decay value (add weight)
                Double currValue = (Double) ftrDecayValues.getVal(pos);
                Double newValue = new Double(currValue.doubleValue() + weight);
                ftrDecayValues.updateVal(newValue, pos);
            }
        }
        ftrDecayValues.trimToSize();  //not necessary, just being tidy
        //sort 'ftrDecayValues' by decay value descenting
        // - the SelectSort algorithm is used for sorting
        for (int i = ftrDecayValues.size() - 1; i > 0; i--) {
            //find index of min entry in range (0, i)
            int min = 0;
            for (int j = 1; j <= i; j++) {
                double jValue = ((Double) ftrDecayValues.getVal(j)).doubleValue();
                double minValue = ((Double) ftrDecayValues.getVal(min)).doubleValue();
                if (jValue < minValue) {
                    min = j;
                }
            }
            //swap min entry with entry at i
            Object tmp;
            tmp = ftrDecayValues.getKey(min);
            ftrDecayValues.updateKey(ftrDecayValues.getKey(i), min);
            ftrDecayValues.updateKey(tmp, i);
            tmp = ftrDecayValues.getVal(min);
            ftrDecayValues.updateVal(ftrDecayValues.getVal(i), min);
            ftrDecayValues.updateVal(tmp, i);
        }
        return ftrDecayValues;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the default value for specific attributes and
     * client from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersGetAttrDef(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetAttrDef(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets default values for specified attributes from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting the default values of
     * attributes or false if an error occurred.
     */
    private boolean execPersGetAttrDef(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        if (qpSize != 3) {
            return false;
        }
        int clnt = queryParam.qpIndexOfKeyNoCase("clnt");
        int attr = queryParam.qpIndexOfKeyNoCase("attr");
        if (attr == -1) {
            return false;
        }
        String clientName = (String) queryParam.getVal(clnt);
        String ftrCondition = DBAccess.ftrPatternCondition((String) queryParam.getVal(attr));
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get def values of matching features            
            query = "SELECT attr_name, attr_defvalue FROM attributes" + " WHERE attr_name" + ftrCondition + " AND FK_psclient='" + clientName + "' ORDER BY attr_name";  //ascending
            /*WebServer.win.log.debug("=============================================");
             WebServer.win.log.debug(query);
             WebServer.win.log.debug(clientName);
             WebServer.win.log.debug("=============================================");*/
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body
            respBody.append(DBAccess.xmlHeader("/resp_xsl/up_attributes.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String feature = rs.getRs().getString("attr_name");  //cannot be null
                String defValue = rs.getRs().getString("attr_defvalue");
                if (rs.getRs().wasNull()) {
                    defValue = "";
                }
                respBody.append("<row><attr>" + feature
                        + "</attr><defval>" + defValue
                        + "</defval></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows found: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, calculates the average values of specific features
     * and user from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersGetAvg(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetAvg(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Calculates the average of feature values for a specified user from
     * database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if calculation of the average of feature values was
     * successful or false if an error occurred.
     */
    private boolean execPersGetAvg(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        if (qpSize < 2 || qpSize > 3) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
        if (ftrIdx == -1) {
            return false;
        }  //must exist
        String clientName = (String) queryParam.getVal(clntIdx);
        String feature = (String) queryParam.getVal(ftrIdx);
        String ftrCondition = DBAccess.ftrPatternCondition(feature);
        //optional query params
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        String usrCondition;
        if (usrIdx == -1) {
            usrCondition = "";
        } else {
            String user = (String) queryParam.getVal(usrIdx);
            usrCondition = " AND nd_user = '" + user + "'";
        }
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get average value
            query = "select avg(nd_numvalue) as average from num_data WHERE nd_feature in " + "(select nd_feature from num_data WHERE nd_feature" + ftrCondition + " AND FK_psclient='" + clientName + "' )" + usrCondition + " AND FK_psclient='" + clientName + "'";
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/average_featureval.xsl"));
            respBody.append("<result>\n");
            String averageStr;
            rs.next();
            double averageVal = rs.getRs().getDouble("average");
            if (rs.getRs().wasNull()) {
                averageStr = "";
            } else {
                averageStr = DBAccess.formatDouble(new Double(averageVal));
            }
            respBody.append("<row><avg>" + averageStr + "</avg></row>\n");
            respBody.append("</result>");
            rowsAffected = 1;  //number of result rows
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the decay rates for specified feature groups
     * from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersGetDrt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetDrt(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets the decay rates for specified feature groups from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting decay rates of feature
     * groups or false if an error occurred.
     */
    private boolean execPersGetDrt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");

        String clientName = (String) queryParam.getKey(clntIdx);
        //concatenate all group names in request
        StringBuffer groups = new StringBuffer();
        for (int i = 0; i < qpSize; i++) {
            if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                //append current group name
                String key = (String) queryParam.getKey(i);
                if (key.equalsIgnoreCase("grp")) {
                    if (groups.length() > 0) {
                        groups.append(",");
                    }  //separate with ","
                    groups.append("'");
                    groups.append(queryParam.getVal(i));
                    groups.append("'");
                }
            }
        }
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get decay rates of specified feature groups
            //(or of all groups in DB if no 'grp' query parameters exist in request)            
            if (qpSize == 1) {
                query = "select dg_group, dg_rate from decay_groups WHERE FK_psclient='" + clientName + "' order by dg_rate desc";
            } else {
                query = "select dg_group, dg_rate from decay_groups WHERE dg_group in (" + groups.substring(0) + ") AND FK_psclient='" + clientName + "' order by dg_rate desc";
            }
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body                        
            respBody.append(DBAccess.xmlHeader("/resp_xsl/decay_groups.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String group = rs.getRs().getString("dg_group");  //cannot be null
                Float rate = new Float(rs.getRs().getFloat("dg_rate"));    //ditto
                respBody.append("<row><grp>" + group
                        + "</grp><rate>" + rate.toString()
                        + "</rate></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows found: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the default values for the specified features
     * from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersGetFtrDef(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetFtrDef(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets the default values of specific features with specified condition
     * parameters from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting the default values of
     * features or false if an error occurred.
     */
    private boolean execPersGetFtrDef(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        if (qpSize != 3) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
        if (ftrIdx == -1) {
            return false;
        }
        String clientName = (String) queryParam.getVal(clntIdx);
        String ftrCondition = DBAccess.ftrPatternCondition((String) queryParam.getVal(ftrIdx));
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get def values of matching features            
            query = "SELECT uf_feature, uf_defvalue FROM up_features" + " WHERE uf_feature" + ftrCondition + " AND FK_psclient='" + clientName + "' ORDER BY uf_feature";  //ascending
            //WebServer.win.log.debug("=============================================");
            //WebServer.win.log.debug(query);
            //WebServer.win.log.debug("=============================================");
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/up_features.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String feature = rs.getRs().getString("uf_feature");  //cannot be null
                String defValue = rs.getRs().getString("uf_defvalue");
                if (rs.getRs().wasNull()) {
                    defValue = "";
                }
                respBody.append("<row><ftr>" + feature
                        + "</ftr><defval>" + defValue
                        + "</defval></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows found: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the attributes for a user with specific
     * parameters from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersGetUsrAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetUsrAttr(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets the default values of specific attributes with specified condition
     * parameters from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting the default values of
     * attributes or false if an error occurred.
     */
    private boolean execPersGetUsrAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        int attrIdx = queryParam.qpIndexOfKeyNoCase("attr");
        int numIdx = queryParam.qpIndexOfKeyNoCase("num");
        int srtIdx = queryParam.qpIndexOfKeyNoCase("srt");
        int cmpIdx = queryParam.qpIndexOfKeyNoCase("cmp");
        if (usrIdx == -1 || attrIdx == -1) {
            return false;
        }  //must exist
        String clientName = (String) queryParam.getVal(clntIdx);
        String user = (String) queryParam.getVal(usrIdx);
        String feature = (String) queryParam.getVal(attrIdx);
        //if optional query params absent, use defaults
        String numberOfRes = (numIdx == -1) ? "*" : (String) queryParam.getVal(numIdx);
        String sortOrder = (srtIdx == -1) ? "desc" : (String) queryParam.getVal(srtIdx);
        String comparStyle = (cmpIdx == -1) ? "n" : (String) queryParam.getVal(cmpIdx);
        //check if upper limit of result number can be obtained
        int limit = DBAccess.numPatternCondition(numberOfRes);
        if (limit == -1) {
            return false;
        }
        String attrCondition = DBAccess.ftrPatternCondition(feature);
        String srtCondition = DBAccess.srtPatternCondition(sortOrder);
        //comparison style decides on which field to perform SQL order by.
        //Since both fields contain the same values as strings AND as doubles,
        //this actually decides whether to treat values as strings or doubles.
        //That is actually the whole point of having same values in two fields.
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching records
            if (user.contains("*")) {
                query = "select attribute, attribute_value from user_attributes WHERE user like'" + user.replaceAll("\\*", "%") + "' AND attribute in " + "(select attribute from user_attributes WHERE attribute" + attrCondition + ") AND FK_psclient='" + clientName + "' order by attribute_value " + srtCondition + ", attribute";
            } else {
                query = "select attribute, attribute_value from user_attributes WHERE user='" + user + "' AND attribute in " + "(select attribute from user_attributes WHERE attribute" + attrCondition + ") AND FK_psclient='" + clientName + "' order by  attribute_value " + srtCondition + ", attribute";
            }

//          WebServer.win.log.debug( "=============================================" );
//          WebServer.win.log.debug( query );
//          WebServer.win.log.debug( "=============================================" );

            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/singleuser_attributes.xsl"));
            respBody.append("<result>\n");
            //select first rows as specified by query parameter 'num'
            while (rowsAffected < limit && rs.next()) {
                String featureVal = rs.getRs().getString("attribute");  //cannot be null
                String valueVal = rs.getRs().getString("attribute_value");
                if (rs.getRs().wasNull()) {
                    valueVal = "";
                }
                respBody.append("<row><attr>" + featureVal
                        + "</attr><val>" + valueVal
                        + "</val></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the features for a user with specific
     * parameters from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersGetUsrFtr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetUsrFtr(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets the features order by their value for a specific user and condition
     * parameters from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting the features or false if an
     * error occurred.
     */
    private boolean execPersGetUsrFtr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
        //int attrIdx = queryParam.qpIndexOfKeyNoCase("attr");
        int numIdx = queryParam.qpIndexOfKeyNoCase("num");
        int srtIdx = queryParam.qpIndexOfKeyNoCase("srt");
        int cmpIdx = queryParam.qpIndexOfKeyNoCase("cmp");
        //if (usrIdx == -1 || (ftrIdx == -1 && attrIdx==-1)) return false;  //must exist
        if (usrIdx == -1 || ftrIdx == -1) {
            return false;
        }  //must exist
        String clientName = (String) queryParam.getVal(clntIdx);
        String user = (String) queryParam.getVal(usrIdx);
        String feature = (String) queryParam.getVal(ftrIdx);
        //if optional query params absent, use defaults
        String numberOfRes = (numIdx == -1) ? "*" : (String) queryParam.getVal(numIdx);
        String sortOrder = (srtIdx == -1) ? "desc" : (String) queryParam.getVal(srtIdx);
        String comparStyle = (cmpIdx == -1) ? "n" : (String) queryParam.getVal(cmpIdx);
        //check if upper limit of result number can be obtained
        int limit = DBAccess.numPatternCondition(numberOfRes);
        if (limit == -1) {
            return false;
        }
        String ftrCondition = DBAccess.ftrPatternCondition(feature);
        String srtCondition = DBAccess.srtPatternCondition(sortOrder);
        String usrCondition = DBAccess.ftrPatternCondition(user);
        //comparison style decides on which field to perform SQL order by.
        //Since both fields contain the same values as strings AND as doubles,
        //this actually decides whether to treat values as strings or doubles.
        //That is actually the whole point of having same values in two fields.
        String comparField = comparStyle.equals("s") ? "up_value" : "up_numvalue";
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching records

            String sUserProfile = "SELECT up_feature FROM user_profiles "
                    + "WHERE up_user " + usrCondition + " "
                    + "AND FK_psclient = '" + clientName + "'";
            String query1 = "SELECT uf_feature AS up_feature,"
                    + " uf_numdefvalue AS up_numvalue FROM up_features "
                    + "WHERE uf_feature NOT IN  (" + sUserProfile + ") "
                    + "AND  uf_feature " + ftrCondition + " "
                    + "AND FK_psclient = '" + clientName + "'";
            String query2 = "SELECT up_feature, up_numvalue FROM user_profiles "
                    + "WHERE up_user " + usrCondition + " "
                    + "AND up_feature " + ftrCondition + " "
                    + "AND FK_psclient='" + clientName + "'";

            query = " ( " + query1 + " ) UNION ( " + query2 + " ) order by " + comparField + srtCondition + ";";

//            System.out.println( "\n\n=============================================\n" );
//            System.out.println(query);
//            System.out.println( "\n=============================================\n\n" );

            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/singleuser_profile.xsl"));
            respBody.append("<result>\n");
            //select first rows as specified by query parameter 'num'
            while (rowsAffected < limit && rs.next()) {
                String featureVal = rs.getRs().getString("up_feature");  //cannot be null
                String valueVal = rs.getRs().getString("up_numvalue");
                if (rs.getRs().wasNull()) {
                    valueVal = "";
                }
                respBody.append("<row><ftr>" + featureVal
                        + "</ftr><val>" + valueVal
                        + "</val></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the users with specific conditions as
     * parameters from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersGetUsrs(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetUsrs(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets the users with with specified condition parameters from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting the users or false if an
     * error occurred.
     */
    private boolean execPersGetUsrs(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int whrIdx = queryParam.qpIndexOfKeyNoCase("whr");
        if (whrIdx == -1) {
            return false;
        }  //must exist
        String clientName = (String) queryParam.getVal(clntIdx);
        String WHERE = (String) queryParam.getVal(whrIdx);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching records            
            query = "select user from users WHERE user like'" + WHERE.replaceAll("\\*", "%") + "' AND FK_psclient='" + clientName + "' ";
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/user.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String user = rs.getRs().getString("user");  //cannot be null
                respBody.append("<row><usr>" + user
                        + "</usr></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, changes the values (increasing or decreasing) of
     * features specified, for a specific user from database and returns the
     * response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersIncVal(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execPersIncVal(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Increases the value by x of the features specified, for a specific user
     * from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if increment was successful or false if an error occurred.
     */
    private boolean execPersIncVal(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        int logIdx = queryParam.qpIndexOfKeyNoCase("log");
        if (usrIdx == -1) {
            return false;
        }
        String clientName = (String) queryParam.getVal(clntIdx);
        String user = (String) queryParam.getVal(usrIdx);
        //execute request
        boolean success = true;
        String sqlString;
        int rowsAffected = 0;
        try {
            /*PUserDBAccess pdbAccess = new PUserDBAccess(dbAccess);
             PStereotypesDBAccess sdbAccess = new PStereotypesDBAccess(dbAccess);
             ArrayList<String> stereotypes = pdbAccess.getStereotypesOfUser( user, clientName );
             HashMap<String, Float> degrees = new HashMap<String, Float>();
             for( String ster : stereotypes ) {      
             float degree = sdbAccess.getUserDegree(ster, user, clientName);
             degrees.put(ster, degree);
             sdbAccess.removeUserFromStereotype(user, ster, clientName);
             }*/
            //increment numeric values of features in profile of user
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != usrIdx && i != clntIdx && i != logIdx) {  //'com' AND 'usr' query parameters excluded
                    //get current parameter pair
                    String feature = (String) queryParam.getKey(i);
                    String ftrCondition = DBAccess.ftrPatternCondition(feature);
                    String step = (String) queryParam.getVal(i);
                    Float numStep = DBAccess.strToNum(step);  //is it numeric?
                    if (numStep != null) {  //if null, 'step' not numeric, misspelled request
                        //get value for current user, feature record
                        sqlString = "select up_feature, up_value from user_profiles WHERE up_user='" + user + "' AND up_feature " + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        PServerResultSet rs = dbAccess.executeQuery(sqlString);
                        if (rs.next() == false) {
                            rs.close();
                            sqlString = "insert into user_profiles (up_user, up_feature, up_value, up_numvalue, FK_psclient )" + " select '" + user + "', uf_feature, uf_defvalue, uf_numdefvalue, FK_psclient FROM up_features WHERE uf_feature " + ftrCondition + " AND FK_psclient = '" + clientName + "'";
                            dbAccess.executeUpdate(sqlString);
                            sqlString = "select up_feature, up_value from user_profiles WHERE up_user='" + user + "' AND up_feature " + ftrCondition + " AND FK_psclient='" + clientName + "'";
                            rs = dbAccess.executeQuery(sqlString);
                            if (rs.next() == false) {
                                WebServer.win.log.debug("-Problem updating DB: Feature name does not exists");
                                success = false;
                                break;
                            }
                        }
                        do {
                            //boolean recFound = rs.next();  //expect one row or none
                            //String value = recFound ? rs.getRs().getString( "up_value" ) : null;
                            String sCurFeature = rs.getRs().getString("up_feature");
                            String value = rs.getRs().getString("up_value");
                            Float numValue = DBAccess.strToNum(value);  //is it numeric?
                            float newNumValue = numValue.floatValue() + numStep.floatValue();
                            String newValue = DBAccess.formatDouble(new Double(newNumValue));

                            //if ( numValue != null ) {  //if null, 'value' does not exist or not numeric
                            //update current user, feature record                        
                            sqlString = "UPDATE user_profiles SET up_value='" + newValue + "', up_numvalue=" + newValue + " WHERE up_user='" + user + "' AND FK_psclient='" + clientName + "' AND up_feature='" + sCurFeature + "'";
                            rowsAffected += dbAccess.executeUpdate(sqlString);
                            int sid = dbAccess.getLastSessionId(user, clientName);
                            System.out.println(System.currentTimeMillis());
                            PNumData data = new PNumData(user, feature, newNumValue, System.currentTimeMillis(), sid);
                            rowsAffected += dbAccess.insertNewNumData(data, clientName);
                            rowsAffected += dbAccess.updateStereotypesFromUserAction(user, feature, numStep.floatValue(), clientName);
                        } while (rs.next());
                        rs.close();
                        //ignore current user, feature record AND continue with next
                    } //else if numStep == null
                    else {
                        success = false;
                    }  //misspelled request, abort AND rollback
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
            /*for( String ster : stereotypes ) {      
             float degree = degrees.get(ster);
             sdbAccess.addUserToStereotype(user, ster, degree, clientName);                
             }*/
            //format response body
            //response will be used only in case of success            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>").append(rowsAffected).append("</num_of_rows></row>\n");
            respBody.append("</result>");
            //close statement
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem updating DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, removes all attributes specified from database and
     * returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersRemAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execPersRemAttr(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    private boolean execPersRemAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //execute request
        String clientName = (String) queryParam.getVal(clntIdx);
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows of matching features
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                    String key = (String) queryParam.getKey(i);
                    if (key.equalsIgnoreCase("attr")) {
                        String ftrCondition = DBAccess.ftrPatternCondition((String) queryParam.getVal(i));
                        query = "delete from user_attributes WHERE attribute" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from attributes WHERE attr_name" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate(query);
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
            //close statement
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows deleted: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to execution part of process.
     *
     * Removes attributes from tables user_attributes and attributes in
     * database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting the default values of
     * features or false if an error occurred.
     */
    private int comPersRemDcy(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execPersRemDcy(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Removes attributes from tables user_attributes and attributes in
     * database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting the default values of
     * features or false if an error occurred.
     */
    private boolean execPersRemDcy(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //execute request
        String clientName = (String) queryParam.getVal(clntIdx);
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows of specified feature groups
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                    String key = (String) queryParam.getKey(i);
                    String group = (String) queryParam.getVal(i);
                    if (key.equalsIgnoreCase("grp")) {
                        query = "delete from decay_groups WHERE dg_group='" + group + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
            if (qpSize == 1) {  //no 'grp' query parameters specified
                //delete rows of all groups
                query = "delete from decay_groups";
                rowsAffected = dbAccess.executeUpdate(query);
            }
            //format response body
            //response will be used only in case of success            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
            //close statement
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows deleted: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, removes specified decay groups from database and
     * returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersRemDdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execPersRemDdt(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Removes decay groups under condition parameters from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if removal was successful or false if an error occurred.
     */
    private boolean execPersRemDdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int whrIdx = queryParam.qpIndexOfKeyNoCase("whr");
        if (whrIdx == -1) {
            return false;
        }
        String clientName = (String) queryParam.getVal(clntIdx);
        String whrCondition = DBAccess.whrPatternCondition((String) queryParam.getVal(whrIdx), clientName);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows specified in 'whr' query parameter
            if (whrCondition.equals("")) //'whr=*', no conditions, all records in table
            {
                query = "delete from decay_data WHERE FK_psclient='" + clientName + "' ";
            } else {
                query = "delete from decay_data" + whrCondition + " AND FK_psclient='" + clientName + "' ";
            }
            rowsAffected += dbAccess.executeUpdate(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows deleted: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, removes all features specified from database and
     * returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersRemFtr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execPersRemFtr(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Removes decay data under condition parameters from database from
     * database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if removal of decay data was successful or false if an error
     * occurred.
     */
    private boolean execPersRemFtr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //execute request
        String clientName = (String) queryParam.getVal(clntIdx);
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows of matching features
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != clntIdx) {  //'com' and 'clnt' query parameters excluded
                    String key = (String) queryParam.getKey(i);
                    if (key.equalsIgnoreCase("ftr")) {
                        String ftrCondition = DBAccess.ftrPatternCondition((String) queryParam.getVal(i));
                        query = "delete from user_profiles WHERE up_feature" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from up_features WHERE uf_feature" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + " " + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from " + DBAccess.COMMUNITY_PROFILES_TABLE + " WHERE " + DBAccess.COMMUNITY_PROFILES_TABLE_FIELD_FEATURE + " " + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate(query);
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows deleted: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, removes all numeric data with specified parameters
     * from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersRemNdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execPersRemNdt(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Removes specified numeric data under condition parameters from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if removal of features was successful or false if an error
     * occurred.
     */
    private boolean execPersRemNdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int whrIdx = queryParam.qpIndexOfKeyNoCase("whr");
        if (whrIdx == -1) {
            return false;
        }
        String clientName = (String) queryParam.getVal(clntIdx);
        String whrCondition = DBAccess.whrPatternCondition((String) queryParam.getVal(whrIdx), clientName);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows specified in 'whr' query parameter
            /*if ( whrCondition.equals( "" ) ) //'whr=*', no conditions, all records in table
             {
             query = "delete from num_data WHERE FK_psclient='" + clientName + "' ";
             } else {
             query = "delete from num_data " + whrCondition + " AND FK_psclient='" + clientName + "' ";
             }*/
            query = "delete from num_data " + whrCondition;

            rowsAffected += dbAccess.executeUpdate(query);
            //format response body
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows deleted: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, removes all data from all tables related to
     * specified users from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersRemUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execPersRemUsr(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Removes all data from all tables referring to a specific user from
     * database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if removal of user data was successful or false if an error
     * occurred.
     */
    private boolean execPersRemUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //execute request
        String clientName = (String) queryParam.getVal(clntIdx);
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows of specified users
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                    String key = (String) queryParam.getKey(i);
                    String user = (String) queryParam.getVal(i);
                    if (key.equalsIgnoreCase("usr")) {
                        dbAccess.removeUserFromStereotypes(user, clientName);
                        query = "delete from num_data WHERE nd_user like '" + user.replaceAll("\\*", "%") + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from decay_data WHERE dd_user like '" + user.replaceAll("\\*", "%") + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from user_sessions WHERE FK_user like '" + user.replaceAll("\\*", "%") + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from stereotype_users WHERE su_user like '" + user.replaceAll("\\*", "%") + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from user_attributes WHERE user like '" + user.replaceAll("\\*", "%") + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from user_profiles WHERE up_user like '" + user.replaceAll("\\*", "%") + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from users WHERE user like '" + user.replaceAll("\\*", "%") + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
            if (qpSize == 1) {  //no 'usr' query parameters specified
                //delete rows of all users
                query = "delete from user_profiles";
                rowsAffected = dbAccess.executeUpdate(query);
                query = "delete from users";
                rowsAffected = dbAccess.executeUpdate(query);
            }
            //format response body
            //response will be used only in case of success                       
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
            e.printStackTrace();
        }
        WebServer.win.log.debug("-Num of rows deleted: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, changes the default values for specified attributes
     * with specific conditions from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersSetAttrDef(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execPersSetAttrDef(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Sets the default value for specified attributes from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if update of default value of features was successful or
     * false if an error occurred.
     */
    private boolean execPersSetAttrDef(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //execute request
        String clientName = (String) queryParam.getVal(clntIdx);
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //update def values of matching features
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx) {  //'com' query parameter excluded
                    String newDefValue = (String) queryParam.getVal(i);
                    String ftrCondition = DBAccess.ftrPatternCondition((String) queryParam.getKey(i));
                    String numNewDefValue = DBAccess.strToNumStr(newDefValue);  //numeric version of def value
                    query = "UPDATE attributes set attr_defvalue='" + newDefValue + "'" + " WHERE attr_name" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                    rowsAffected += dbAccess.executeUpdate(query);
                }
            }
            //format response body
            //response will be used only in case of success
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem updating DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, changes the values with new of specified user and
     * attributes from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersSetAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execPersSetAttr(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Sets new values to attributes specified from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if change of values of attributes was successful or false if
     * an error occurred.
     */
    private boolean execPersSetAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        if (usrIdx == -1) {
            return false;
        }
        String clientName = (String) queryParam.getVal(clntIdx);
        String user = (String) queryParam.getVal(usrIdx);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //increment numeric values of features in profile of user
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != usrIdx && i != clntIdx) {  //'com' AND 'usr' query parameters excluded
                    //get current parameter pair
                    String attribute = (String) queryParam.getKey(i);
                    String sAttrCondition = DBAccess.ftrPatternCondition(attribute);
                    String newVal = (String) queryParam.getVal(i);
                    //get value for current user, feature record
                    //update current user, attribute record
                    query = "UPDATE user_attributes set attribute_value ='" + newVal + "' WHERE user = '" + user + "' AND FK_psclient='" + clientName + "' AND attribute " + sAttrCondition;
                    //System.out.println("============================="+query);
                    rowsAffected += dbAccess.executeUpdate(query);
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem updating DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, changes the values of decay data of specified decay
     * groups from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersSetDcy(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = success && execPersSetDcy(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Changes the value of decay rate for specified feature groups from
     * database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if change of decay rate was successful or false if an error
     * occurred.
     */
    private boolean execPersSetDcy(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        String clientName = (String) queryParam.getVal(clntIdx);
        //for all (group, rate) pairs, execute request
        for (int i = 0; i < qpSize; i++) {
            if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                //get current (group, rate) pair
                String group = (String) queryParam.getKey(i);
                String rate = (String) queryParam.getVal(i);
                //check pair validity
                if (!DBAccess.legalFtrOrAttrName(group)) //ftr group AND ftr name validity is similar
                {
                    return false;
                }     //discontinue request AND rollback
                Float numRateDbl = DBAccess.strToNum(rate);  //converts string to Double
                if (numRateDbl == null) //if null, 'rate' not numeric, misspelled request
                {
                    return false;
                }     //discontinue request AND rollback
                double numRate_dbl = numRateDbl.doubleValue();
                if (numRate_dbl < 0 || numRate_dbl > 1) {
                    return false;
                }     //discontinue request AND rollback
                Float numRate = new Float(numRate_dbl);  //'rate' defined float in DB
                //if new decay feature group, insert (group, rate) in DB,
                //else update rate of existing group to new rate
                boolean success;
                if (!persExistsDecay(dbAccess, group, clientName)) {
                    success = persInsertDecay(dbAccess, group, numRate, clientName);
                } else {
                    success = persSetRate(dbAccess, group, numRate, clientName);
                }
                //if not success discontinue loop AND rollback
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if specified decay groups exists in database and returns true or
     * false depending on the result.
     *
     * @param dbAccess The database manager.
     * @param group The name of decay group.
     * @param clientName The name of client.
     * @return True if specified decay group exists.
     */
    private boolean persExistsDecay(DBAccess dbAccess, String group, String clientName) {
        //returns true if decay feature group already
        //exists in the DB. Returns false otherwise.
        boolean exists = false;  //true if group exists in DB
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get specified decay feature group record
            query = "select dg_group from decay_groups WHERE dg_group='" + group + "' AND FK_psclient='" + clientName + "'";
            PServerResultSet rs = dbAccess.executeQuery(query);
            while (rs.next()) {
                rowsAffected += 1;
            }  //number of rows in result should be 0 or 1
            exists = (rowsAffected > 0) ? true : false;
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Decay feature group exists: " + rowsAffected);
        return success && exists;  //'success' expected true here
    }

    /**
     * Inserts new decay group in database.
     *
     * @param dbAccess The database manager.
     * @param group The name of decay group.
     * @param rate The new value of decay group that will be changed.
     * @param clientName The name of client.
     * @return True if insert was successful or false if failed.
     */
    private boolean persInsertDecay(DBAccess dbAccess, String group, Float rate, String clientName) {
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert the (feature group, decay rate) pair
            query = "insert into decay_groups (dg_group, dg_rate, FK_psclient) values ('" + group + "', " + rate.toString() + ",'" + clientName + "')";
            rowsAffected = dbAccess.executeUpdate(query);
            //close statement
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        return success;
    }

    /**
     * Updates the value of decay group rate for the specified decay group.
     *
     * @param dbAccess The database manager.
     * @param group The name of decay group.
     * @param rate The new value of decay group that will be changed.
     * @param clientName The name of client.
     * @return True if change was successful or false if failed.
     */
    private boolean persSetRate(DBAccess dbAccess, String group, Float rate, String clientName) {
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //update decay rate of specified feature group
            query = "UPDATE decay_groups set dg_rate=" + rate.toString() + " WHERE dg_group='" + group + "' AND FK_psclient='" + clientName + "'";
            rowsAffected += dbAccess.executeUpdate(query);
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem updating DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, changes the default values of specified features
     * from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersSetFtrDef(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = success && execPersSetFtrDef(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Changes the default values of specified features from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if change of default value of features was successful or
     * false if an error occurred.
     */
    private boolean execPersSetFtrDef(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //execute request
        String clientName = (String) queryParam.getVal(clntIdx);
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //update def values of matching features
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                    String newDefValue = (String) queryParam.getVal(i);
                    String ftrCondition = DBAccess.ftrPatternCondition((String) queryParam.getKey(i));
                    String numNewDefValue = DBAccess.strToNumStr(newDefValue);  //numeric version of def value
                    query = "UPDATE up_features set uf_defvalue='" + newDefValue + "', uf_numdefvalue=" + numNewDefValue + " WHERE uf_feature" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                    rowsAffected += dbAccess.executeUpdate(query);
                }
            }
            //format response body
            //response will be used only in case of success
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem updating DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, changes the values of specified features for a
     * specific user from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersSetUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //if new user, initialize user profile for all features,
            //AND then update the values of matching features
            //-start transaction body
            if (!persExistsUsr(queryParam, dbAccess)) {
                success = success && execPersAddUsr(queryParam, dbAccess);
            }
            success = success && execPersSetUsr(queryParam, respBody, dbAccess);
            //-end transaction body
            if (success) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Checks the existence of a specific user in database.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return True if user exists in database or false if not.
     */
    private boolean persExistsUsr(VectorMap queryParam, DBAccess dbAccess) {
        //returns true if user in 'usr' query parameter
        //already exists in the DB. Returns false otherwise.
        //request properties
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        if (usrIdx == -1) {
            return false;
        }
        String user = (String) queryParam.getVal(usrIdx);
        //execute request
        boolean exists = false;  //true if user exists in DB
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get specified user records
            query = "select user from users WHERE user='" + user + "' AND FK_psclient='" + clientName + "'";
            PServerResultSet rs = dbAccess.executeQuery(query);
            while (rs.next()) {
                rowsAffected += 1;
            }  //count number of rows in result
            exists = (rowsAffected > 0) ? true : false;
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of user rows: " + rowsAffected);
        return success && exists;  //'success' expected true here
    }

    /**
     * Method referring to execution part of process.
     *
     * Inserts a specific user and its attributes in database.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return True if process was successful or false if an error occurred.
     */
    private boolean execPersAddUsr(VectorMap queryParam, DBAccess dbAccess) {
        //request properties
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        if (usrIdx == -1) {
            return false;
        }
        String user = (String) queryParam.getVal(usrIdx);
        //values in 'queryParam' can be empty string,
        //user should not be empty string, check it
        if (!DBAccess.legalUsrName(user)) {
            return false;
        }
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert all features in profile of new user, with def value
            query = "INSERT INTO users (user,FK_psclient,decay_factor) VALUES('" + user + "','" + clientName + "','0')";
            rowsAffected += dbAccess.executeUpdate(query);
            //WebServer.win.log.debug("=============================================");
            //WebServer.win.log.debug(query);
            //WebServer.win.log.debug("=============================================");
            //query = "insert into user_profiles (up_user, up_feature, up_value, up_numvalue, FK_psclient )" + " select '" + user + "', uf_feature, uf_defvalue, uf_numdefvalue, FK_psclient FROM up_features WHERE FK_psclient = '" + clientName + "'";
            //WebServer.win.log.debug("=============================================");
            //WebServer.win.log.debug(query);
            //WebServer.win.log.debug("=============================================");
            //rowsAffected = dbAccess.executeUpdate( query );
            query = "insert into user_attributes (user, attribute, attribute_value, FK_psclient )" + " select '" + user + "', attr_name, attr_defvalue, FK_psclient FROM attributes WHERE FK_psclient = '" + clientName + "'";
            //WebServer.win.log.debug("=============================================");
            //WebServer.win.log.debug(query);
            //WebServer.win.log.debug("=============================================");
            rowsAffected += dbAccess.executeUpdate(query);
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to execution part of process.
     *
     * Updates the attributes values of a user, inserts into user_profiles table
     * the specified features and updates the values for the features inserted
     * previously in the table.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if process was successful or false if an error occurred.
     */
    private boolean execPersSetUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        if (usrIdx == -1) {
            return false;
        }
        String user = (String) queryParam.getVal(usrIdx);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            PUserDBAccess pdbAccess = new PUserDBAccess(dbAccess);
            PStereotypesDBAccess sdbAccess = new PStereotypesDBAccess(dbAccess);
            //update values of matching features in profile of user
            ArrayList<String> stereotypes = pdbAccess.getStereotypesOfUser(user, clientName);
            HashMap<String, Float> degrees = new HashMap<String, Float>();
            for (String ster : stereotypes) {
                float degree = sdbAccess.getUserDegree(ster, user, clientName);
                degrees.put(ster, degree);
                sdbAccess.removeUserFromStereotype(user, ster, clientName);
            }
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != usrIdx && i != clntIdx) {  //'com' AND 'usr' query parameters excluded
                    String parameter = (String) queryParam.getKey(i);
                    if (parameter.startsWith("attr_")) {
                        String attribute = parameter.substring(5);
                        String ftrCondition = DBAccess.ftrPatternCondition(attribute);
                        String value = (String) queryParam.getVal(i);
                        query = "UPDATE user_attributes set attribute_value='" + value + "' WHERE user='" + user + "' AND attribute " + ftrCondition + " AND FK_psclient = '" + clientName + "'";
                        //System.out.println("============================="+query);
                        rowsAffected += dbAccess.executeUpdate(query);
                    } else {
                        String feature;
                        if (parameter.startsWith("ftr_")) {
                            feature = parameter.substring(4);
                        } else {
                            feature = parameter;
                        }
                        String ftrCondition = DBAccess.ftrPatternCondition(feature);
                        String value = (String) queryParam.getVal(i);
                        String numValue = DBAccess.strToNumStr(value);  //numeric version of value
                        query = "insert ignore into user_profiles (up_user, up_feature, up_value, up_numvalue, FK_psclient )" + " select '" + user + "', uf_feature, uf_defvalue, uf_numdefvalue, FK_psclient FROM up_features WHERE uf_feature = '" + feature + "' AND FK_psclient = '" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "UPDATE user_profiles set up_value='" + value + "', up_numvalue=" + numValue + " WHERE up_user='" + user + "' AND up_feature" + ftrCondition + " AND FK_psclient = '" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate(query);
                    }

                }
            }
            for (String ster : stereotypes) {
                float degree = degrees.get(ster);
                sdbAccess.addUserToStereotype(user, ster, degree, clientName);
            }
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem updating DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the decay data as specified by the condition
     * parameters from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersSqlDdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersSqlDdt(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets decay data under condition parameters from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting decay data from database or
     * false if an error occurred.
     */
    private boolean execPersSqlDdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int whrIdx = queryParam.qpIndexOfKeyNoCase("whr");
        if (whrIdx == -1) {
            return false;
        }
        String whrCondition = DBAccess.whrPatternCondition((String) queryParam.getVal(whrIdx), clientName);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching decay data
            //query = "select dd_user, dd_feature, dd_timestamp from decay_data" + whrCondition + " AND FK_psclient='" + clientName + "' ";
            query = "select dd_user, dd_feature, dd_timestamp from decay_data" + whrCondition;
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/decay_data.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String userVal = rs.getRs().getString("dd_user");          //cannot be null
                String featureVal = rs.getRs().getString("dd_feature");    //cannot be null
                long timestampVal = rs.getRs().getLong("dd_timestamp");    //cannot be null
                respBody.append("<row><usr>" + userVal
                        + "</usr><ftr>" + featureVal
                        + "</ftr><timestamp>" + timestampVal
                        + "</timestamp></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows found: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the numeric data as specified by the condition
     * parameters from database and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersSqlNdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersSqlNdt(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets numeric data under condition parameters from database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting numeric data from database
     * or false if an error occurred.
     */
    private boolean execPersSqlNdt(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int whrIdx = queryParam.qpIndexOfKeyNoCase("whr");
        if (whrIdx == -1) {
            return false;
        }
        String whrCondition = DBAccess.whrPatternCondition((String) queryParam.getVal(whrIdx), clientName);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching numeric data            
            //query = "select nd_user, nd_feature, nd_timestamp, nd_numvalue from num_data" + whrCondition + " AND FK_psclient='" + clientName + "' ";
            query = "select nd_user, nd_feature, nd_timestamp, nd_value from num_data" + whrCondition;
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/num_data.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String userVal = rs.getRs().getString("nd_user");          //cannot be null
                String featureVal = rs.getRs().getString("nd_feature");    //cannot be null
                long timestampVal = rs.getRs().getLong("nd_timestamp");    //cannot be null
                String numvalueStr;
                double numvalueVal = rs.getRs().getDouble("nd_value");
                if (rs.getRs().wasNull()) {
                    numvalueStr = "";
                } else {
                    numvalueStr = DBAccess.formatDouble(new Double(numvalueVal));
                }
                respBody.append("<row><usr>" + userVal
                        + "</usr><ftr>" + featureVal
                        + "</ftr><timestamp>" + timestampVal
                        + "</timestamp><numvalue>" + numvalueStr
                        + "</numvalue></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows found: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the attributes from table user_profiles as
     * specified by the condition parameters from database and returns the
     * response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersSqlUsrAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersSqlUsrAttr(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets data from table user_profiles under condition parameters from
     * database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting the data from database or
     * false if an error occurred.
     */
    private boolean execPersSqlUsrAttr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int whrIdx = queryParam.qpIndexOfKeyNoCase("whr");
        if (whrIdx == -1) {
            return false;
        }
        String whrCondition = DBAccess.whrPatternCondition((String) queryParam.getVal(whrIdx), clientName);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching user profiles            
            query = "select user, attribute, attribute_value from user_attributes" + whrCondition;
            //System.out.println(""+query);
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body
            respBody.append(DBAccess.xmlHeader("/resp_xsl/user_attributes.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String userVal = rs.getRs().getString("user");        //cannot be null
                String featureVal = rs.getRs().getString("attribute");  //cannot be null
                String valueVal = rs.getRs().getString("attribute_value");
                if (rs.getRs().wasNull()) {
                    valueVal = "";
                }
                respBody.append("<row><usr>" + userVal
                        + "</usr><attr>" + featureVal
                        + "</attr><val>" + valueVal
                        + "</val></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows found: " + rowsAffected);
        return success;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the features from table user_profiles as
     * specified by the condition parameters from database and returns the
     * response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comPersSqlUsrFtr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersSqlUsrFtr(queryParam, respBody, dbAccess);
            //check success
            if (!success) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug("-Possible error in client request");
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Gets data from table user_profiles under condition parameters from
     * database.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return True if there was no problem getting the data from database or
     * false if an error occurred.
     */
    private boolean execPersSqlUsrFtr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int whrIdx = queryParam.qpIndexOfKeyNoCase("whr");
        if (whrIdx == -1) {
            return false;
        }
        String whrCondition = DBAccess.whrPatternCondition((String) queryParam.getVal(whrIdx), clientName);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching user profiles
            query = "select up_user, up_feature, up_value from user_profiles" + whrCondition;
            //System.out.println(""+query);
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/user_profiles.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String userVal = rs.getRs().getString("up_user");        //cannot be null
                String featureVal = rs.getRs().getString("up_feature");  //cannot be null
                String valueVal = rs.getRs().getString("up_value");
                if (rs.getRs().wasNull()) {
                    valueVal = "";
                }
                respBody.append("<row><usr>" + userVal
                        + "</usr><ftr>" + featureVal
                        + "</ftr><val>" + valueVal
                        + "</val></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset AND statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows found: " + rowsAffected);
        return success;
    }
    /*private String getClientName ( VectorMap queryParam ) {
     int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
     if ( clntIdx == -1 ) {
     return null;
     }
     //client attibutes demactrate with the "|" character
     String userANDPass = ( String ) queryParam.getVal( clntIdx );
     StringTokenizer tokenizer = new StringTokenizer( userANDPass, "|" );
     String client = tokenizer.nextToken();//first comes the client name
     return client;
     }*/
}
