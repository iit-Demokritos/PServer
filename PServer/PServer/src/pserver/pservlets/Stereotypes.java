/* 
 * Copyright 2011 NCSR "Demokritos"
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");   
 * you may not use this file except in compliance with the License.   
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package pserver.pservlets;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import pserver.WebServer;
import pserver.data.DBAccess;
import pserver.data.PServerResultSet;
import pserver.data.PStereotypesDBAccess;
import pserver.data.VectorMap;
import pserver.logic.PSReqWorker;

/**
 * Contains all necessary methods for the management of Stereotypes mode of
 * PServer.
 */
public class Stereotypes implements pserver.pservlets.PService {

    /**
     * Returns the mime type.
     *
     * @return Returns the XML mime type from Interface {@link PService}.
     */
    @Override
    public String getMimeType() {
        return pserver.pservlets.PService.xml;
    }

    /**
     * Overridden method of init from {@link PService} Does nothing here.
     *
     * @param params An array of strings containing the parameters
     * @throws Exception Default Exception is thrown.
     */
    @Override
    public void init(String[] params) throws Exception {
    }

    /**
     * Creates a service for Stereotypes' mode when a command is sent to
     * PServer. The command is identified from its name and proper methods for
     * the management of this command are called. A response code is produced
     * depending on results.
     *
     * @param parameters The parameters needed for this service.
     * @param response The response string that is created.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    @Override
    public int service(VectorMap parameters, StringBuffer response, DBAccess dbAccess) {
        int respCode;
        VectorMap queryParam;

        StringBuffer respBody = response;
        queryParam = parameters;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        if (clntIdx == -1) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Parameter clnt does not exist");
            return respCode;  //no point in proceeding
        }
        String clientName = (String) queryParam.getVal(clntIdx);
        clientName = clientName.substring(0, clientName.indexOf('|'));
        queryParam.updateVal(clientName, clntIdx);

        //commands of STER_MODE here!
        //find 'com' query param (case independent)
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //if 'com' param not present, request is invalid
        if (comIdx == -1) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command does not exist");
            return respCode;//no point in proceeding
        }
        //recognize command encoded in request
        String com = (String) queryParam.getVal(comIdx);
        if (com.equalsIgnoreCase("addstr")) {       //add stereotype
            respCode = comSterAddStr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("setstr") || com.equalsIgnoreCase("setstrftr")) {  //update stereotype features
            respCode = comSterSetStr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("lststr")) {  //list all stereotypes
            respCode = comSterLstStr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getstr")) {  //get feature values for a stereotype
            respCode = comSterGetStr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("sqlstr")) {  //specify conditions and select stereotypes
            respCode = comSterSqlStr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("remstr")) {  //remove stereotype(s)
            respCode = comSterRemStr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("addusr")) {  //assign user to stereotype(s)
            respCode = comSterAddUsr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("setdeg")) {  //update assignment degree
            respCode = comSterSetDeg(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("incdeg")) {  //increment assignment degree
            respCode = comSterIncDeg(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getusr")) {  //get assigned stereotypes for a user
            respCode = comSterGetUsr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("sqlusr")) {  //specify conditions and select assignments
            respCode = comSterSqlUsr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("remusr")) {  //remove user assignments to stereotypes
            respCode = comSterRemUsr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("mkster")) {  //remove user assignments to stereotypes
            respCode = comSterMake(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("update")) {  //remove user assignments to stereotypes
            respCode = comSterUpdate(queryParam, respBody, dbAccess);
        } else {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command not recognized");
        }
        return respCode;
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, adds an attribute to database with the parameters
     * specified and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterAddStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);//transaction guarantees integrity
            //the new (feature, def value) pairs must be inserted, and
            //the user attributes must be expanded with the new features
            //-start transaction body
            if (!sterExistsStr(queryParam, dbAccess)) {
                success = success && execSterAddStr(queryParam, dbAccess);
            } else {
                respCode = PSReqWorker.DUBLICATE_ERROR;  //client request data inconsistent?                
                dbAccess.disconnect();
                WebServer.win.log.error("-DB Stereotype already exists: ");
                return respCode;
            }
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
     * Adds a stereotype with the parameters specified.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean sterExistsStr(VectorMap queryParam, DBAccess dbAccess) {
        //returns true if stereotype in 'str' query parameter
        //already exists in the DB. Returns false otherwise.
        //request properties
        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
        if (strIdx == -1) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        String stereot = (String) queryParam.getVal(strIdx);
        //execute request
        boolean exists = false;  //true if user exists in DB
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get specified stereotype record
            query = "select st_stereotype from stereotypes where st_stereotype='" + stereot + "' AND FK_psclient='" + clientName + "'";
            PServerResultSet rs = dbAccess.executeQuery(query);
            while (rs.next()) {
                rowsAffected += 1;
            }  //one or none row expected
            exists = (rowsAffected > 0) ? true : false;
            //close resultset and statement
            rs.close();
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Stereotype rows: " + rowsAffected);
        return success && exists;  //'success' expected true here
    }

    /**
     * Method referring to execution part of process.
     *
     * Adds a stereotype with the parameters specified.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterAddStr(VectorMap queryParam, DBAccess dbAccess) {
        //request properties
        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
        if (strIdx == -1) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        String stereot = (String) queryParam.getVal(strIdx);
        int ruleIdx = queryParam.qpIndexOfKeyNoCase("rule");
        String rule;
        if (ruleIdx == -1) {
            rule = null;
        } else {
            rule = (String) queryParam.getVal(ruleIdx);
        }
        
       
        //values in 'queryParam' can be empty string,
        //check if stereotype in 'str' is legal
        if (!DBAccess.legalStrName(stereot)) {
            return false;
        }
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert new stereotype 
            if (ruleIdx == -1) {
                query = "insert into stereotypes (st_stereotype,FK_psclient) values ('" + stereot + "','" + clientName + "')";
                rowsAffected = dbAccess.executeUpdate(query);
            } else {
                String sqlRule = getSQLFromStereotypeRule(rule);
                query = "insert into stereotypes (st_stereotype,st_rule, FK_psclient) values ( ?,?,'" + clientName + "')";
                PreparedStatement prep = dbAccess.getConnection().prepareStatement(query);
                prep.setString(1, stereot);
                prep.setString(2, sqlRule.toString());
                rowsAffected = prep.executeUpdate();
                prep.close();
                addUsersToStereotype(dbAccess, clientName, stereot, sqlRule);
                updateStereotype(dbAccess, clientName, stereot);
            }
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        return success;
    }

    private String getSQLFromStereotypeRule(String rule) {
        StringBuilder sqlRule = new StringBuilder();
        //sqlRule.append("SELECT * FROM user_attributes Where FK_psclient='").append(clientName).append("' AND ");
        String[] tokens = rule.split("\\|");
        int idx = 0;
        for (String token : tokens) {
            if (idx % 2 == 0) {
                //sqlRule.append("AND ");
                while (token.startsWith("(")) {
                    token = token.substring(1);
                    sqlRule.append("(");
                }
                int endParenthesisCounter = 0;
                while (token.endsWith(")")) {
                    token = token.substring(0, token.length() - 1);
                    endParenthesisCounter++;
                }

                String first = null;
                String operator = null;
                String second = null;

                if (token.contains("<>")) {
                    first = DBAccess.UATTR_TABLE_FIELD_ATTRIBUTE + "='" + token.substring(0, token.indexOf("<>")) + "' AND " + DBAccess.UATTR_TABLE_FIELD_VALUE;
                    operator = "<>";
                    second = "'" + token.substring(token.indexOf(">") + 1) + "'";
                } else if (token.contains(":")) {
                    first = DBAccess.UATTR_TABLE_FIELD_ATTRIBUTE + "='" + token.substring(0, token.indexOf(":")) + "' AND " + DBAccess.UATTR_TABLE_FIELD_VALUE;
                    operator = "=";
                    second = "'" + token.substring(token.indexOf(":") + 1) + "'";
                }

                sqlRule.append("(");
                sqlRule.append(first);
                sqlRule.append(operator);
                sqlRule.append(second);
                sqlRule.append(")");

                for (int i = 0; i < endParenthesisCounter; i++) {
                    sqlRule.append(")");
                }
            } else {
                sqlRule.append(" ").append(token.toUpperCase()).append(" ");
            }
            idx++;
        }
        return sqlRule.toString();
    }

    private void addUsersToStereotype(DBAccess dbAccess, String clientName, String stereot, String rule) throws SQLException {
        String insUsrSql = "INSERT INTO " + DBAccess.STEREOTYPE_USERS_TABLE
                + "(" + dbAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE
                + "," + dbAccess.STEREOTYPE_USERS_TABLE_FIELD_USER
                + "," + dbAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE
                + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ('" + stereot + "',?,1,'" + clientName + "')";
        PreparedStatement prep = dbAccess.getConnection().prepareStatement(insUsrSql);
        String sql = "SELECT " + DBAccess.UATTR_TABLE_FIELD_USER + " FROM " + DBAccess.UATTR_TABLE + " WHERE FK_psclient='" + clientName + "' AND " + rule + " GROUP BY " + DBAccess.UATTR_TABLE_FIELD_USER;
        PServerResultSet rs = dbAccess.executeQuery(sql);
        while (rs.next()) {
            String user = rs.getRs().getString(1);
            prep.setString(1, user);
            prep.addBatch();
        }
        rs.close();
        prep.executeBatch();
        prep.close();
    }

    private void updateStereotype(DBAccess dbAccess, String clientName, String stereotype) throws SQLException {
        String sql = "DELETE FROM " + DBAccess.STERETYPE_PROFILES_TABLE + " WHERE " + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        dbAccess.executeUpdate(sql);
        String subSelect = "SELECT '" + stereotype + "',up_feature,sum(up_value),'" + clientName + "' FROM " + DBAccess.UPROFILE_TABLE
                + " WHERE up_user IN (SELECT su_user FROM stereotype_users WHERE su_stereotype = '" + stereotype + "' AND FK_psclient='" + clientName + "' ) GROUP BY up_feature";

        sql = "INSERT INTO " + DBAccess.STERETYPE_PROFILES_TABLE
                + "(" + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_STEREOTYPE
                + "," + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_FEATURE
                + "," + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_NUMVALUE
                + "," + DBAccess.FIELD_PSCLIENT + ") " + subSelect + "";
        dbAccess.executeUpdate(sql);
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, adds a user to a specific stereotype in database
     * with the parameters specified and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterAddUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);//transaction guarantees integrity
            success = execSterAddUsr(queryParam, dbAccess);
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
     * Inserts a user in table stereotype_users in database with the parameters
     * specified.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterAddUsr(VectorMap queryParam, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        if (usrIdx == -1) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
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
            //insert each (user, stereotype, degree) in a new row in 'stereotype_users'.
            //Note that the specified stereotypes must already exist in 'stereotypes'
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != usrIdx && i != clntIdx) {  //'com' and 'usr' query parameters excluded
                    String stereot = (String) queryParam.getKey(i);
                    if (stereotypeExists(dbAccess, stereot, clientName) == false) {
                        WebServer.win.log.debug("-Stereotype " + stereot + " does not exists");
                        continue;
                    }
                    if (stereotypeHasUser(dbAccess, stereot, user, clientName)) {
                        WebServer.win.log.debug("-Stereotype " + stereot + " already has the user " + user);
                        continue;
                    }
                    String degree = (String) queryParam.getVal(i);
                    String numDegree = DBAccess.strToNumStr(degree);  //numeric version of degree                    
                    updateStereotypeWithUser(dbAccess, clientName, stereot, user, Float.parseFloat(numDegree));
                    query = "insert into stereotype_users " + "(su_user, su_stereotype, su_degree, FK_psclient) values ('" + user + "', '" + stereot + "', " + numDegree + ",'" + clientName + "')";
                    rowsAffected += dbAccess.executeUpdate(query);
                }
            }
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        return success;
    }

    private boolean stereotypeExists(DBAccess dbAccess, String stereot, String clientName) throws SQLException {
        String sql = "SELECT * FROM " + DBAccess.STEREOTYPE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE + "='" + stereot + "'";
        PServerResultSet rs = dbAccess.executeQuery(sql);
        boolean ret = false;
        if (rs.next()) {
            ret = true;
        }
        rs.close();
        return ret;
    }

    private boolean stereotypeHasUser(DBAccess dbAccess, String stereotype, String user, String clientName) throws SQLException {
        String sql = "SELECT * FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE "
                + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
        PServerResultSet rs = dbAccess.executeQuery(sql);
        boolean ret = false;
        if (rs.next()) {
            ret = true;
        }
        rs.close();
        return ret;
    }

    private void updateStereotypeWithUser(DBAccess dbAccess, String clientName, String stereotype, String user, float degree) throws SQLException {
        String subSelect = "SELECT '" + stereotype + "',up_feature, 0,'" + clientName + "' FROM " + DBAccess.UPROFILE_TABLE
                + " WHERE up_user ='" + user + "' AND FK_psclient='" + clientName + "'";

        String sql = "INSERT IGNORE INTO " + DBAccess.STERETYPE_PROFILES_TABLE
                + "(" + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_STEREOTYPE
                + "," + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_FEATURE
                + "," + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_NUMVALUE
                + "," + DBAccess.FIELD_PSCLIENT + ") " + subSelect + "";
        dbAccess.executeUpdate(sql);

        sql = "UPDATE " + DBAccess.STERETYPE_PROFILES_TABLE + "," + DBAccess.UPROFILE_TABLE
                + " SET " + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_NUMVALUE + "=" + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_NUMVALUE + "+"
                + degree + "*" + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE
                + " WHERE " + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND "
                + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "= " + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_FEATURE;
        dbAccess.executeUpdate(sql);
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, gets the features and their values of specified
     * stereotypes fro database with the parameters specified and returns the
     * response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterGetStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
            //execute the command
            boolean success;
            success = execSterGetStr(queryParam, respBody, dbAccess);
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
     * Gets the features and their values from database with the parameters
     * specified.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterGetStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        if (qpSize < 3 || qpSize > 6) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
        int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
        int numIdx = queryParam.qpIndexOfKeyNoCase("num");
        int srtIdx = queryParam.qpIndexOfKeyNoCase("srt");
        int cmpIdx = queryParam.qpIndexOfKeyNoCase("cmp");
        if (strIdx == -1 || ftrIdx == -1) {
            return false;
        }  //must exist
        String stereot = (String) queryParam.getVal(strIdx);
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
        //comparison style decides on which field to perform SQL order by.
        //Since both fields contain the same values as strings and as doubles,
        //this actually decides whether to treat values as strings or doubles.
        //That is actually the whole point of having same values in two fields.
        String comparField = comparStyle.equals("s") ? "sp_value" : "sp_numvalue";
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching records
            //if (db.compareTo("ACCESS") == 0) {  //database type is MS-Access
            query = "select sp_feature, sp_value from stereotype_profiles where sp_stereotype='" + stereot + "' and FK_psclient='" + clientName + "' and sp_feature in " + "(select sp_feature from stereotype_profiles where sp_feature" + ftrCondition + " and FK_psclient='" + clientName + "' ) order by " + comparField + srtCondition + ", sp_feature";
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body
            respBody.append(DBAccess.xmlHeader("/resp_xsl/singlestereot_profile.xsl"));
            respBody.append("<result>\n");
            //select first rows as specified by query parameter 'num'
            while (rowsAffected < limit && rs.next()) {
                String featureVal = rs.getRs().getString("sp_feature");  //cannot be null
                String valueVal = rs.getRs().getString("sp_value");
                if (rs.getRs().wasNull()) {
                    valueVal = "";
                }
                respBody.append("<row><ftr>").append(featureVal).append("</ftr><val>").append(valueVal).append("</val></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset and statement
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
     * Connects to database, gets the names of stereotypes and their degrees
     * from database with the parameters specified and returns the response
     * code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterGetUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
            //execute the command
            boolean success;
            success = execSterGetUsr(queryParam, respBody, dbAccess);
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
     * Gets stereotypes names and degree from database with the parameters
     * specified.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterGetUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        if (qpSize < 3 || qpSize > 5) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
        int numIdx = queryParam.qpIndexOfKeyNoCase("num");
        int srtIdx = queryParam.qpIndexOfKeyNoCase("srt");
        if (usrIdx == -1 || strIdx == -1) {
            return false;
        }  //must exist
        String user = (String) queryParam.getVal(usrIdx);
        String stereot = (String) queryParam.getVal(strIdx);
        //if optional query params absent, use defaults
        String numberOfRes = (numIdx == -1) ? "*" : (String) queryParam.getVal(numIdx);
        String sortOrder = (srtIdx == -1) ? "desc" : (String) queryParam.getVal(srtIdx);
        //check if upper limit of result number can be obtained
        int limit = DBAccess.numPatternCondition(numberOfRes);
        if (limit == -1) {
            return false;
        }
        String strCondition = (stereot.equals("*")) ? "" : "su_stereotype='" + stereot + "' and ";
        String srtCondition = DBAccess.srtPatternCondition(sortOrder);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching records
            if (strCondition.equals("") == false) {
                query = "select su_stereotype, su_degree from stereotype_users where " + strCondition + " su_user='" + user + "' and FK_psclient='" + clientName + "'  order by su_degree" + srtCondition + ", su_stereotype";
            } else {
                query = "select su_stereotype, su_degree from stereotype_users where su_user='" + user + "' and FK_psclient='" + clientName + "'  order by su_degree" + srtCondition + ", su_stereotype";
            }
//            System.out.println( "=======================================" );
//            System.out.println( query );
//            System.out.println( "=======================================" );
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/stereot_singleuser.xsl"));
            respBody.append("<result>\n");
            //select first rows as specified by query parameter 'num'
            while (rowsAffected < limit && rs.next()) {
                String stereotVal = rs.getRs().getString("su_stereotype");  //cannot be null
                String degreeVal = (new Double(rs.getRs().getDouble("su_degree"))).toString();
                if (rs.getRs().wasNull()) {
                    degreeVal = "";
                }
                respBody.append("<row><str>" + stereotVal
                        + "</str><deg>" + degreeVal
                        + "</deg></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset and statement
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
     * Connects to database, increases the degree of a stereotype for a specific
     * user from database with the parameters specified and returns the response
     * code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterIncDeg(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);//transaction guarantees integrity
            success = execSterIncDeg(queryParam, respBody, dbAccess);
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
     * Increases the degree value of a stereotype for a specific user from
     * database with the parameters specified.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterIncDeg(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            //increment degrees of stereotypes for a user            
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != usrIdx && i != clntIdx) {  //'com' and 'usr' query parameters excluded
                    //get current parameter pair
                    String stereot = (String) queryParam.getKey(i);
                    if (stereotypeExists(dbAccess, stereot, clientName) == false) {
                        WebServer.win.log.debug("-Stereotype " + stereot + " does not exists");
                        continue;
                    }
                    if (stereotypeHasUser(dbAccess, stereot, user, clientName) == false) {
                        WebServer.win.log.debug("-Stereotype " + stereot + " already does not have the user " + user);
                        continue;
                    }
                    String step = (String) queryParam.getVal(i);
                    Float numStep = DBAccess.strToNum(step);  //is it numeric?
                    if (numStep != null) {  //if null, 'step' not numeric, misspelled request
                        //get degree for current user, stereotype record
                        query = "select su_degree from stereotype_users where su_user='" + user + "' and su_stereotype='" + stereot + "' and FK_psclient='" + clientName + "' ";
                        PServerResultSet rs = dbAccess.executeQuery(query);
                        boolean recFound = rs.next();  //expect one row or none
                        Double degree = recFound ? new Double(rs.getRs().getDouble("su_degree")) : 0;
                        rs.close();  //in any case
                        //update current user, stereotype record
                        double newNumDegree = degree.doubleValue() + numStep.doubleValue();
                        String newDegree = DBAccess.formatDouble(new Double(newNumDegree));
                        query = "UPDATE stereotype_users set su_degree=" + newDegree + " where su_user='" + user + "' and su_stereotype='" + stereot + "' and FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                        updateStereotypeForDegree(dbAccess, stereot, user, clientName, numStep);

                    } else {
                        success = false;
                    }  //misspelled request, abort and rollback
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

    private void updateStereotypeForDegree(DBAccess dbAccess, String stereotype, String user, String clientName, float degree) throws SQLException {
        String sql = "UPDATE " + DBAccess.STERETYPE_PROFILES_TABLE + "," + DBAccess.UPROFILE_TABLE
                + " SET " + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_NUMVALUE + "=" + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_NUMVALUE + "+"
                + degree + "*" + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE
                + " WHERE " + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND "
                + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "= " + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_FEATURE;
        dbAccess.executeUpdate(sql);
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, increases the value of a stereotype from database
     * with the parameters specified and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterIncVal(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execSterIncVal(queryParam, respBody, dbAccess);
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
     * Increases the value of a stereotype for a specific user from database
     * with the parameters specified.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterIncVal(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
        if (strIdx == -1) {
            return false;
        }
        String stereot = (String) queryParam.getVal(strIdx);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //increment numeric values of features in stereotype
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != strIdx && i != clntIdx) {  //'com' and 'str' query parameters excluded
                    //get current parameter pair
                    String feature = (String) queryParam.getKey(i);
                    String step = (String) queryParam.getVal(i);
                    Float numStep = DBAccess.strToNum(step);  //is it numeric?
                    if (numStep != null) {  //if null, 'step' not numeric, misspelled request
                        //get value for current stereotype, feature record
                        query = "select sp_value from stereotype_profiles where sp_stereotype='" + stereot + "' and sp_feature ='" + feature + "' and FK_psclient='" + clientName + "' ";
                        PServerResultSet rs = dbAccess.executeQuery(query);
                        boolean recFound = rs.next();  //expect one row or none
                        String value = recFound ? rs.getRs().getString("sp_value") : null;
                        rs.close();  //in any case
                        Float numValue = DBAccess.strToNum(value);  //is it numeric?
                        if (numValue != null) {  //if null, 'value' does not exist or not numeric
                            //update current stereotype, feature record
                            double newNumValue = numValue.doubleValue() + numStep.doubleValue();
                            String newValue = DBAccess.formatDouble(new Double(newNumValue));
                            query = "UPDATE stereotype_profiles set sp_value='" + newValue + "', sp_numvalue=" + newValue + " where sp_stereotype='" + stereot + "' and sp_feature='" + feature + "' and FK_psclient='" + clientName + "' ";
                            int tmpRows = dbAccess.executeUpdate(query);
                            if (tmpRows == 0) {
                                query = "INSERT stereotype_profiles ( sp_stereotype, sp_feature, sp_value, sp_numvalue, FK_psclient ) SELECT '" + stereot + "', uf_feature, uf_defvalue, uf_numdefvalue, FK_psclient FROM up_features WHERE uf_feature ='" + feature + "' AND FK_psclient='" + clientName + "'";
                                dbAccess.executeUpdate(query);
                                query = "UPDATE stereotype_profiles SET sp_value='" + newValue + "', sp_numvalue=" + newValue + " where sp_stereotype='" + stereot + "' and sp_feature='" + feature + "' and FK_psclient='" + clientName + "' ";
                                tmpRows = dbAccess.executeUpdate(query);
                            }
                            rowsAffected += tmpRows;
                        }
                        //else if numValue == null
                        //ignore current stereotype, feature record and continue with next
                    } //else if numStep == null
                    else {
                        success = false;
                    }  //misspelled request, abort and rollback
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
     * Connects to database, gets stereotypes from database with the parameters
     * specified and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterLstStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
            //execute the command
            boolean success;
            success = execSterLstStr(queryParam, respBody, dbAccess);
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
     * Creates a list of stereotypes retrieved from database with the parameters
     * specified.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterLstStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        if (qpSize < 2 || qpSize > 3) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
        int modIdx = queryParam.qpIndexOfKeyNoCase("mod");
        if (strIdx == -1) {
            return false;
        }  //must exist
        String stereot = (String) queryParam.getVal(strIdx);
        String mode = (modIdx == -1) ? "*" : (String) queryParam.getVal(modIdx);  //default '*'
        //interpret patterns
        String strCondition;
        if (stereot.equals("*")) {
            strCondition = "";
        } else {
            strCondition = " where st_stereotype='" + stereot + "'";
        }
        String link = strCondition.equals("") ? " where" : " and";
        String modCondition;
        if (mode.equals("p")) {
            modCondition = link + " st_stereotype not in "
                    + "(select distinct sp_stereotype from stereotype_profiles WHERE FK_psclient='" + clientName + "')";
        } else if (mode.equals("u")) {
            modCondition = link + " st_stereotype not in "
                    + "(select distinct su_stereotype from stereotype_users WHERE FK_psclient='" + clientName + "')";
        } else {
            modCondition = "";
        }  //case of '*'
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching records
            if ((strCondition + modCondition).equals("") == true) {
                query = "select st_stereotype," + DBAccess.STEREOTYPE_TABLE_FIELD_RULE + " from stereotypes where FK_psclient='" + clientName + "'";
            } else {
                query = "select st_stereotype," + DBAccess.STEREOTYPE_TABLE_FIELD_RULE + " from stereotypes" + strCondition + modCondition + " AND FK_psclient='" + clientName + "'";
            }
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/stereotypes.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String stereotVal = rs.getRs().getString("st_stereotype");  //cannot be null
                String rule = rs.getRs().getString(DBAccess.STEREOTYPE_TABLE_FIELD_RULE);  //cannot be null
                respBody.append("<row><str>" + stereotVal + "</str><rule>" + rule + "</rule></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset and statement
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
     * Connects to database, removes an attribute for a specific stereotype from
     * database with the parameters specified and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterRemStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execSterRemStr(queryParam, respBody, dbAccess);
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
     * Removes an attribute for a specific stereotype from database with the
     * parameters specified.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterRemStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int lkeIdx = queryParam.qpIndexOfKeyNoCase("lke");
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete specified stereotypes            
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != lkeIdx && i != clntIdx) {  //'com', 'lke' query parameters excluded
                    String key = (String) queryParam.getKey(i);
                    String stereot = (String) queryParam.getVal(i);
                    if (key.equalsIgnoreCase("str")) {
                        query = "delete from stereotype_attributes where sp_stereotype='" + stereot + "' and FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from stereotype_profiles where sp_stereotype='" + stereot + "' and FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                        query = "delete from stereotypes where st_stereotype='" + stereot + "' and FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate(query);
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if (!success) {
                    break;
                }  //discontinue loop, rollback
            }
            if (lkeIdx != -1) {  //delete stereotypes matching the pattern
                String stereotPattern = (String) queryParam.getVal(lkeIdx);

                query = "delete from stereotype_attributes where sp_stereotype like '" + stereotPattern + "%' and FK_psclient='" + clientName + "' ";
                rowsAffected += dbAccess.executeUpdate(query);
                query = "delete from stereotype_profiles where sp_stereotype like '" + stereotPattern + "%' and FK_psclient='" + clientName + "' ";
                rowsAffected += dbAccess.executeUpdate(query);
                query = "delete from stereotypes where st_stereotype like '" + stereotPattern + "%' and FK_psclient='" + clientName + "' ";
                rowsAffected += dbAccess.executeUpdate(query);
            }
            
          
            if (qpSize == 2) {  //no 'str' and 'lke' query parameters specified
                //delete rows of all stereotypes
               
                query = "delete from stereotype_profiles WHERE FK_psclient='" + clientName + "' ";
                rowsAffected = dbAccess.executeUpdate(query);
                query = "delete from stereotypes WHERE FK_psclient='" + clientName + "' ";
                rowsAffected = dbAccess.executeUpdate(query);
            }
            //format response body
            //response will be used only in case of success            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>").append(rowsAffected).append("</num_of_rows></row>\n");
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
     * Connects to database, removes all data stored in database for a
     * stereotype with the parameters specified and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterRemUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            dbAccess.setAutoCommit(false);
            boolean success = true;
            success = execSterRemUsr(queryParam, respBody, dbAccess);
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
     * Removes all data for a specific stereotype from database with the
     * parameters specified.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterRemUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            //delete rows of matching stereotypes for specified users
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != clntIdx) {  //'com' query parameter excluded
                    String user = (String) queryParam.getKey(i);
                    String stereot = (String) queryParam.getVal(i);
                    if (stereotypeExists(dbAccess, stereot, clientName) == false) {
                        WebServer.win.log.debug("-Stereotype " + stereot + " does not exists");
                        continue;
                    }
                    if (stereotypeHasUser(dbAccess, stereot, user, clientName) == false) {
                        WebServer.win.log.debug("-Stereotype " + stereot + " already does not have the user " + user);
                        continue;
                    }
                    PStereotypesDBAccess sdbAccess = new PStereotypesDBAccess(dbAccess);
                    rowsAffected += sdbAccess.removeUserFromStereotype(user, stereot, clientName);
                }
            }
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
     * Connects to database, removes all records with stereotypes matching the
     * stereotype pattern, for the specified user and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private float getUserDegree(DBAccess dbAccess, String stereotype, String user, String clientName) throws SQLException {
        String sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE "
                + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "' AND "
                + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        PServerResultSet rs = dbAccess.executeQuery(sql);
        rs.next();
        float val = rs.getRs().getFloat(1);
        rs.close();
        return val;
    }

    /**
     * Method referring to execution part of process.
     *
     * Removes all records with stereotypes matching the stereotype pattern, for
     * the specified user.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int updateStereotypeWithUpdatededUser(DBAccess dbAccess, String clientName, String stereotype, String user, float degree) throws SQLException {
        String sql = "UPDATE " + DBAccess.STERETYPE_PROFILES_TABLE + "," + DBAccess.UPROFILE_TABLE
                + " SET " + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_NUMVALUE + "=" + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_NUMVALUE + "+"
                + degree + "*" + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE
                + " WHERE " + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND "
                + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "= " + DBAccess.STERETYPE_PROFILES_TABLE + "." + DBAccess.STERETYPE_PROFILES_TABLE_FIELD_FEATURE;
        return dbAccess.executeUpdate(sql);
    }

    /**
     * Method referring to command part of process.
     *
     * Connects to database, changes the degree of specific stereotypes and
     * returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterSetDeg(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execSterSetDeg(queryParam, respBody, dbAccess);
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
     * Changes the degree of specific stereotypes.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterSetDeg(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            //update degrees of specified stereotypes assigned to a user            
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != usrIdx && i != clntIdx) {  //'com' and 'usr' query parameters excluded                    
                    String stereot = (String) queryParam.getKey(i);
                    if (stereotypeExists(dbAccess, stereot, clientName) == false) {
                        WebServer.win.log.debug("-Stereotype " + stereot + " does not exists");
                        continue;
                    }
                    if (stereotypeHasUser(dbAccess, stereot, user, clientName) == false) {
                        WebServer.win.log.debug("-Stereotype " + stereot + " already does not have the user " + user);
                        continue;
                    }
                    float oldDegree = getUserDegree(dbAccess, stereot, user, clientName);
                    String newDegree = (String) queryParam.getVal(i);
                    float difDegree = oldDegree - Float.parseFloat(newDegree);
                    String numNewDegree = DBAccess.strToNumStr(newDegree);  //numeric version of degree
                    query = "UPDATE stereotype_users set su_degree=" + numNewDegree + " where su_user='" + user + "' and su_stereotype='" + stereot + "' and FK_psclient='" + clientName + "' ";
                    rowsAffected += dbAccess.executeUpdate(query);
                    rowsAffected += updateStereotypeWithUpdatededUser(dbAccess, clientName, stereot, user, difDegree);
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
     * Connects to database, changes the value of specific features of
     * stereotypes and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterSetStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execSterSetStr(queryParam, respBody, dbAccess);
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
     * Changes the value of specific features of stereotypes.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterSetStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
        if (strIdx == -1) {
            return false;
        }
        String stereot = (String) queryParam.getVal(strIdx);
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //update values of matching stereotype features
            for (int i = 0; i < qpSize; i++) {
                if (i != comIdx && i != strIdx && i != clntIdx) {  //'com' and 'str' query parameters excluded
                    String newValue = (String) queryParam.getVal(i);
                    String ftrCondition = DBAccess.ftrPatternCondition((String) queryParam.getKey(i));
                    String numNewValue = DBAccess.strToNumStr(newValue);  //numeric version of value
                    query = "UPDATE stereotype_profiles set sp_value='" + newValue + "', sp_numvalue=" + numNewValue + " where sp_stereotype='" + stereot + "' and sp_feature" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                    int tmpRows = dbAccess.executeUpdate(query);
                    if (tmpRows == 0) {
                        query = "INSERT stereotype_profiles ( sp_stereotype, sp_feature, sp_value, sp_numvalue, FK_psclient ) SELECT '" + stereot + "', uf_feature, uf_defvalue, uf_numdefvalue, FK_psclient FROM up_features WHERE uf_feature " + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        dbAccess.executeUpdate(query);
                        query = "UPDATE stereotype_profiles set sp_value='" + newValue + "', sp_numvalue=" + numNewValue + " where sp_stereotype='" + stereot + "' and sp_feature" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        tmpRows = dbAccess.executeUpdate(query);
                    }
                    rowsAffected += tmpRows;
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
     * Connects to database, gets the features of stereotypes with specific
     * condition parameters and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterSqlStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
            //execute the command
            boolean success;
            success = execSterSqlStr(queryParam, respBody, dbAccess);
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
     * Gets the features of stereotypes with specific condition parameters
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterSqlStr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            //get matching stereotype profiles            
            query = "select sp_stereotype, sp_feature, sp_value from stereotype_profiles" + whrCondition;
            //System.out.println("=====================================");
            //System.out.println( query );
            //System.out.println("=====================================");
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body            
            respBody.append(DBAccess.xmlHeader("/resp_xsl/stereot_profiles.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String stereotVal = rs.getRs().getString("sp_stereotype");  //cannot be null
                String featureVal = rs.getRs().getString("sp_feature");     //cannot be null
                String valueVal = rs.getRs().getString("sp_value");
                if (rs.getRs().wasNull()) {
                    valueVal = "";
                }
                respBody.append("<row><str>" + stereotVal
                        + "</str><ftr>" + featureVal
                        + "</ftr><val>" + valueVal
                        + "</val></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset and statement
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
     * Connects to database, gets the users belonging on stereotypes with
     * specific condition parameters and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterSqlUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
            //execute the command
            boolean success;
            success = execSterSqlUsr(queryParam, respBody, dbAccess);
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
     * Gets the users belonging on stereotypes with specific condition
     * parameters.
     *
     * @param queryParam The parameters of the query.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execSterSqlUsr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            //get matching stereotype - user records
            query = "select su_user, su_stereotype, su_degree from stereotype_users" + whrCondition;
            PServerResultSet rs = dbAccess.executeQuery(query);
            //format response body
            respBody.append(DBAccess.xmlHeader("/resp_xsl/stereot_users.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String userVal = rs.getRs().getString("su_user");           //cannot be null
                String stereotVal = rs.getRs().getString("su_stereotype");  //cannot be null
                String degreeVal = (new Double(rs.getRs().getDouble("su_degree"))).toString();
                if (rs.getRs().wasNull()) {
                    degreeVal = "";
                }
                respBody.append("<row><usr>" + userVal
                        + "</usr><str>" + stereotVal
                        + "</str><deg>" + degreeVal
                        + "</deg></row>\n");
                rowsAffected += 1;  //number of result rows
            }
            respBody.append("</result>");
            //close resultset
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
     * Connects to database, creates stereotypes with
     * specific condition parameters and returns the response code.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comSterMake(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
            //execute the command
            boolean success;
            success = makeStereotypes(queryParam, respBody, dbAccess);
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

    private boolean makeStereotypes(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        return false;
        //try {
            /*
         boolean success = true;
         int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
         String clientName = (String) queryParam.getVal(clntIdx);

         Statement stmt = dbAccess.getConnection().createStatement();
         String sql;
         sql = "SELECT * FROM " + DBAccess.ATTRIBUTES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
         ResultSet rs = stmt.executeQuery(sql);
         LinkedList<String> attributes = new LinkedList<String>();
         while (rs.next()) {
         attributes.add(rs.getString(1));
         }
         rs.close();

         sql = "SELECT " + DBAccess.USER_TABLE_FIELD_USER + " FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' LIMIT ? OFFSET ?";
         PreparedStatement userStmt = dbAccess.getConnection().prepareStatement(sql);
         sql = "SELECT * FROM " + DBAccess.UATTR_TABLE + " WHERE " + DBAccess.UATTR_TABLE_FIELD_USER + "=? AND " + DBAccess.UATTR_TABLE_FIELD_ATTRIBUTE + " = ? AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
         PreparedStatement getUserAttrsStmt = dbAccess.getConnection().prepareStatement(sql);
         sql = "INSERT DELAYED INTO " + DBAccess.STEREOTYPE_TABLE + " VALUES ( ?,'" + clientName + "')";
         PreparedStatement insertStereotype = dbAccess.getConnection().prepareStatement(sql);
         sql = "INSERT DELAYED INTO " + DBAccess.STEREOTYPE_ATTIBUTE_TABLE + " VALUES ( ?,?,?,'" + clientName + "')";
         PreparedStatement insertStereotypeAttrs = dbAccess.getConnection().prepareStatement(sql);
         int i = 0;
         int userSize = 1000;
         int offset = 0;
         Set<String> stereotypes = new TreeSet<String>();
         do {
         i = 0;
         userStmt.setInt(1, userSize);
         userStmt.setInt(2, offset);
         rs = userStmt.executeQuery();
         while (rs.next()) {
         StringBuffer sterName = new StringBuffer();
         String user = rs.getString(1);
         getUserAttrsStmt.setString(1, user);
         for (String attr : attributes) {
         getUserAttrsStmt.setString(2, attr);
         ResultSet retAttrs = getUserAttrsStmt.executeQuery();
         if (retAttrs.next() == false) {
         sterName.append("NULL_");
         } else {
         String val = retAttrs.getString(3);
         sterName.append(val + "_");
         }
         retAttrs.close();
         }
         stereotypes.add(sterName.toString());
         i++;
         }
         rs.close();
         offset += userSize;
         } while (i == userSize);
         for (String ster : stereotypes) {
         insertStereotype.clearParameters();
         insertStereotype.setString(1, ster);
         insertStereotype.addBatch();
         String[] vals = ster.split("_");
         i = 0;
         for (String attr : attributes) {
         insertStereotypeAttrs.clearParameters();
         insertStereotypeAttrs.setString(1, ster);
         insertStereotypeAttrs.setString(2, attr);
         insertStereotypeAttrs.setString(3, vals[i]);
         insertStereotypeAttrs.addBatch();
         i++;
         }
         }
         insertStereotype.executeBatch();
         insertStereotypeAttrs.executeBatch();
         userStmt.close();
         getUserAttrsStmt.close();
         insertStereotype.close();
         insertStereotypeAttrs.close();

         return success;
         } catch (SQLException ex) {
         WebServer.win.log.error(ex.toString());
         ex.printStackTrace();
         return false;
         }*/
    }

    private int comSterUpdate(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
            //execute the command
            boolean success = true;
            //success = updateStereotypes(queryParam, respBody, dbAccess);
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

    private void updateStereotypeWithRemovedUser(DBAccess dbAccess, String clientName, String stereot, String user, int qpSize) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
