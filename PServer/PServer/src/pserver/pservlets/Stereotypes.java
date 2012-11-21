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
import pserver.data.VectorMap;
import pserver.logic.PSReqWorker;

/**
 *
 * @author alexm
 */
public class Stereotypes implements pserver.pservlets.PService {

    @Override
    public String getMimeType() {
        return pserver.pservlets.PService.xml;
    }

    @Override
    public void init(String[] params) throws Exception {
    }

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

    //--------------------------------------------------------------------------------------------
    //STER_MODE commands
    //In Stereotype Mode, the server offers support for stereotypes.
    //Stereotypes are categories of users with specific characteristics.
    //Each stereotype has a profile that is defined by means of (feature,
    //value) tupples. Features can be entities relevant to specific
    //applications, while values give an estimation about the stereotype
    //relevence to corresponding features. Each stereotype may have
    //its own different features. Features can be organized in a
    //tree or graph based manner, for easily managing conceptual
    //hierarchies. This organization is encoded in the name of every
    //feature as a path expression, and is setup by applications.
    //Users can be assigned stereotypes, together with a degree of
    //relevence, showing how relevent is a stereotype to a user.
    //A user may be assigned several stereotypes (not the same twice).
    //The DB structure: stereotypes (st_stereotype) with key 'st_stereotype',
    //stereotype_profiles (sp_stereotype, sp_feature, sp_value, sp_numvalue)
    //with key 'sp_stereotype' and 'sp_feature', stereotype_users
    //(su_user, su_stereotype, su_degree) with key 'su_user' and
    //'su_stereotype'. If a field in 'stereotypes' is deleted, the deletion
    //is cascaded to 'stereotype_profiles' and 'stereotype_users', because
    //of referential integrity constraints. The field 'sp_numvalue' is
    //"invisible": it is not part of the results of 'select' queries,
    //and contains the numeric equivalent of the string value in field
    //'sp_value', or NULL if the string cannot be converted to numeric.
    //This duplicate field is used mainly to allow for two types of value
    //comparisons: string and numeric. Note that the primary data type for
    //values is always string, as it is more general, and that the numeric
    //version always correspond to the string version. Also note that
    //values intented to be numeric must use '.' for the decimal part when
    //given as strings. If ',' is used, the string will not be successfully
    //converted to numeric, and its numeric equivalent will be NULL. The
    //field 'su_degree' is numeric (double), and when its values are
    //exchanged as strings they follow the rules described above. This
    //field also contain NULLs for values that could not be converted to
    //numeric.    
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

    //-addusr
    //template: ster?com=addusr&usr=<usr>&<str_1>=<deg_1>&<str_2>=...
    //          Order of query params is not important. User name
    //          must not be empty string.
    //descript: assigns stereotypes to a user with an associated degree.
    //          Degree is numeric (double), and expresses relevence.
    //          For the 'degree' parameters in query string that cannot
    //          be converted to numeric, NULLs will be inserted in DB.
    //          The stereotypes must already exist in the DB. If a
    //          stereotype in the query string does not already exists
    //          in the DB, or if a (usr, str) pair already exists, then
    //          code 401 (request error) will be returned. If the error
    //          code 401 is returned then no records have been inserted
    //          in the DB.
    //example : ster?com=addusr&usr=kostas&visitor=0.78&expert=9
    //          ster?com=addusr&usr=034&visitor=&expert=
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : no response body exists.
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

    //-getstr
    //template: ster?com=getstr&str=<str>&ftr=<ftr_pattern>[&num=
    //          <num_pattern>&srt=<order_pattern>&cmp=<comp_pattern>]
    //          Order of query params is not important. Query params
    //          'num', 'srt', and 'cmp' are optional. If ommited, 'num'
    //          defaults to '*', 'srt' defaults to 'desc', and 'cmp' to 'n'.
    //pattern : for feature, * | name[.*], where name is a path expression.
    //          For num, * | <integer>.
    //          For srt, asc | desc. For A->Z use 'asc', for 10->1 use 'desc'.
    //          For cmp, s | n. Values are compared as strings if cmp==s,
    //          while they are compared as numbers (doubles) if cmp==n.
    //          String values that cannot be converted to doubles are
    //          represented as NULLs in numeric comparison.
    //descript: for the specified stereotype, the features matching the pattern
    //          are found and sorted according to value (based on 'srt' and
    //          'cmp'), and secondarily according to feature name (asc, A->Z).
    //          Then the first <num_pattern> rows are selected (or all, if
    //          <num_pattern> is '*') and an XML answer is formed. If no
    //          feature in DB matches the pattern or if <num_pattern> <=0
    //          or if stereotype does not exist, the result will not have any
    //          'row' elements (200 OK will still be returned).
    //          Note that 'srt' and 'cmp' affect the sorting on value.
    //          Note that in case a number of features matching the pattern
    //          have the same value, some of them may be part of the
    //          results, while others not. This depends on 'num', which
    //          determines in absolute terms the number of result rows.
    //          Which of the features with the same value will be part of
    //          the result depends on the feature name, which is a secondary
    //          field of ordering.
    //example : ster?com=getstr&str=professionals&ftr=interest.*&num=3
    //          ster?com=getstr&str=s106&ftr=page6.*
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/singlestereot_profile.xsl"?>
    //          <result>
    //              <row><ftr>feature</ftr><val>value</val></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is handled
    //          directly by an application and not by a browser, this
    //          reference to xsl can be ignored.
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

    //-getusr
    //template: ster?com=getusr&usr=<usr>&str=<str_pattern>[&num=
    //          <num_pattern>&srt=<order_pattern>]
    //          Order of query params is not important. Query params 'num',
    //          and 'srt' are optional. If ommited, 'num' defaults to '*',
    //          and 'srt' defaults to 'desc'.
    //pattern : for stereotypes, * | name.
    //          For num, * | <integer>.
    //          For srt, asc | desc. For 10->1 use 'desc'.
    //descript: for the specified user, the stereotypes matching the pattern
    //          are found and sorted according to degree (based on 'srt'),
    //          and secondarily according to stereotype name (asc, A->Z).
    //          Then the first <num_pattern> rows are selected (or all, if
    //          <num_pattern> is '*') and an XML answer is formed. If no
    //          stereotype in DB matches the pattern or if <num_pattern> <=0
    //          or if the user does not exist, the result will not have any
    //          'row' elements (200 OK will still be returned).
    //          Note that 'srt' affects the sorting on degree. Sorting is
    //          primarily based on numeric values (doubles) of field
    //          'su_degree', which may also contain NULLs in some records.
    //          Note that in case a number of stereotypes matching the pattern
    //          have the same degree, some of them may be part of the
    //          results, while others not. This depends on 'num', which
    //          determines in absolute terms the number of result rows.
    //          Which of the stereotypes with the same value will be part of
    //          the result depends on the stereotype name, which is a
    //          secondary field of ordering.
    //example : ster?com=getusr&usr=eddie&str=*&num=3
    //          ster?com=getusr&usr=w18&str=visitor
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/stereot_singleuser.xsl"?>
    //          <result>
    //              <row><str>stereotype</str><deg>degree</deg></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is handled
    //          directly by an application and not by a browser, this
    //          reference to xsl can be ignored
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
                query = "select su_stereotype, su_degree from stereotype_users where " + strCondition + "and su_user='" + user + "' and FK_psclient='" + clientName + "'  order by su_degree" + srtCondition + ", su_stereotype";
            } else {
                query = "select su_stereotype, su_degree from stereotype_users where su_user='" + user + "' and FK_psclient='" + clientName + "'  order by su_degree" + srtCondition + ", su_stereotype";
            }
            //System.out.println( "=======================================" );
            //System.out.println( query );
            //System.out.println( "=======================================" );
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

    //-incdeg   
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

    //-incval
    //template: ster?com=incval&str=<str>&<ftr_1>=<step_1>&...
    //          Order of query params is not important: updates of feature
    //          values are performed in the order they appear in the request,
    //          however the changes are of accummulative nature, so the final
    //          result is the same.
    //descript: for the specified stereotype, the value for each specified
    //          feature is increased by x (decreased if x is negative), where
    //          x is the step corresponding to that feature. Rows with string
    //          values that cannot be converted to numeric, are not affected.
    //          If no matches are found, or if all matches have values that
    //          cannot be converted to numeric, no records will be updated
    //          (200 OK will still be returned). If any <step_i> parameter
    //          cannot be converted to numeric, 401 is returned. If the error
    //          code 401 is returned then no updates have taken place in the DB.
    //example : ster?com=incval&str=visitor&freq.*=-0.1&interests.books=0.3
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is handled
    //          directly by an application and not by a browser, this
    //          reference to xsl can be ignored.
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

    //-lststr
    //template: ster?com=lststr&str=<str_pattern>[&mod=<mod_pattern>]
    //          Order of query params is not important. Query parameter
    //          'mod' is optional, if omitted defaults to '*'.
    //pattern : for stereotype, * | name
    //          For mode, * | p | u
    //descript: lists the stereotypes in the DB. From an original set of
    //          the stereotypes that match the stereotype pattern (that
    //          is, all stereotypes if str pattern is '*', a single
    //          stereotype if <name> exists in table 'stereotypes', or
    //          none if <name> does not exist or table empty) a subset
    //          is selected and listed as follows: (a) if the mode pattern
    //          is '*', all stereotypes in the original set are returned
    //          without additional filtering, (b) if the mode pattern is
    //          'p', only the stereotypes of the original set that do not
    //          have any associated feature (do not exist in table
    //          'stereotype_profiles') are returned, and (c) if the mode
    //          pattern is 'u' only the stereotypes of the original set
    //          that have not been assigned to any user (do not exist in
    //          table 'stereotype_users') are returned. Result is ordered
    //          by stereotype name. If the final list is empty, a result
    //          without any row elements is returned in the XML answer.
    //example : ster?com=lststr&str=*
    //          ster?com=lststr&str=professionals&mod=*
    //          ster?com=lststr&str=*&mod=p
    //          ster?com=lststr&str=visitor&mod=u
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/stereotypes.xsl"?>
    //          <result>
    //              <row><str>stereotype</str></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is handled
    //          directly by an application and not by a browser, this
    //          reference to xsl can be ignored.
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

    //-remstr
    //template: ster?com=remstr[&str=<str_1>&str=...][&lke=<str_like_pattern>]
    //          Order of query params is not important. The 'str'
    //          and 'lke' query parameters are optional.
    //pattern : an SQL 'like' pattern matching any stereotype that
    //          starts with it.
    //descript: removes the stereotypes specified by 'str' and / or
    //          'lke' query parameters. If no 'str' and 'lke' query
    //          parameters exist, all stereotype records will be
    //          deleted. Referential integrity constraints will cause
    //          records of tables where those stereotypes are foreign
    //          keys to be removed as well. This means that deleted
    //          stereotypes will cease to have any profiles (features),
    //          and that user references to deleted stereotypes will
    //          also be lost. Can be used to initialize the Stereotype
    //          Mode DB, by deleting all records from all tables. In
    //          order to delete the profile (features) of a stereotype
    //          without affecting the user references to that stereotype,
    //          the 'remftr' command can be used. If the specified
    //          stereotypes are not found, no records will be deleted
    //          (200 OK will still be returned). If the error code 401
    //          is returned then no record has been deleted.
    //example : ster?com=remstr&str=visitor&str=experienced
    //          ster?com=remstr&str=visitor&lke=exper
    //          ster?com=remstr   (deletes all records from all tables)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is handled
    //          directly by an application and not by a browser, this
    //          reference to xsl can be ignored.
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
            if (qpSize == 1) {  //no 'str' and 'lke' query parameters specified
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

    //-remusr
    //template: ster?com=remusr[&<usr_1>=<str_pattern_1>&<usr_2>=...]
    //          Order of query params is not important. The user
    //          query parameters are optional.
    //pattern : * | name
    //descript: removes all records with stereotypes matching the
    //          stereotype pattern, for the specified user: either
    //          all stereotypes if pattern is '*', or a single
    //          stereotype, for the corresponding user. This is
    //          repeated for all parameters in the query string.
    //          The same user can de specified more than once in a
    //          query string. If no user query parameters exist, all
    //          the records in the table will be deleted (all user
    //          references to stereotypes). If no (user, stereotype)
    //          in DB matches the patterns, no record will be deleted
    //          (200 OK will still be returned). If the error code 401
    //          is returned then no records have been deleted.
    //example : ster?com=remusr&john=*&george=visitor&george=expert
    //          ster?com=remusr     (deletes all records in table)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is handled
    //          directly by an application and not by a browser, this
    //          reference to xsl can be ignored.
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
                    rowsAffected += dbAccess.removeUserFromStereotype(user, stereot, clientName);                                        
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

    private float getUserDegree(DBAccess dbAccess, String stereotype, String user, String clientName) throws SQLException {
        String sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE + " FROM "+ DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " 
                + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " 
                + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "' AND " 
                + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'" ;
        PServerResultSet rs = dbAccess.executeQuery(sql);
        rs.next();
        float val = rs.getRs().getFloat(1);
        rs.close();
        return val;
    }
    
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
    
    //-setdeg    
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
                    rowsAffected +=updateStereotypeWithUpdatededUser(dbAccess, clientName, stereot, user, difDegree);
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

    //-setstrftr or setstr
    //template: ster?com=setstr&str=<str>&<ftr_pattern_1>=<new_val_1>&...
    //          Order of query params is important: position of 'com'
    //          and 'str' is not important, however updates of values
    //          are performed in the order they appear in the request.
    //pattern : * | name[.*], where name is a path expression
    //descript: updates the values for the specified stereotype of all
    //          features matching the feature pattern(s) to the new
    //          value(s). If the stereotype does not exist, or if no
    //          feature in DB matches a pattern, no value will be
    //          updated (200 OK will still be returned). If the error
    //          code 401 is returned then none of the features matching
    //          the request pattern(s) has been updated to the new value(s).
    //example : ster?com=setstr&str=expert&special.*=0&special.comput=1
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is handled
    //          directly by an application and not by a browser, this
    //          reference to xsl can be ignored.
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

    //-sqlstr
    //template: ster?com=sqlstr&whr=<where_pattern>
    //          Order of query params is not important.
    //pattern : * | <SQL part following WHERE>. The '*' means all.
    //          A special syntax must be used: ':' for = and '|' for <space>.
    //          This is because spaces and '=' are replaced in WWW requests.
    //          Note that string values must be enclosed in single quotes.
    //          Note that there exist two choices for comparisons on values:
    //          string comparisons for field 'sp_value', and numeric (double)
    //          comparisons for field 'sp_numvalue'. String values that cannot
    //          be converted to doubles are represented as NULLs in 'sp_numvalue'.
    //descript: returns part of the table 'stereotype_profiles' as specified
    //          by the condition in the 'whr' query parameter. If no
    //          row in DB satisfies the conditions, the result will
    //          not have any 'row' elements (200 OK will still be returned).
    //example : ster?com=sqlstr&whr=sp_stereotype:'visitor'|and|sp_numvalue<:2|order|by|sp_feature
    //          ster?com=sqlstr&whr=*
    //          ster?com=sqlstr&whr=isnull(sp_numvalue)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/stereot_profiles.xsl"?>
    //          <result>
    //              <row><str>stereotype</str><ftr>feature</ftr><val>value</val></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is handled
    //          directly by an application and not by a browser, this
    //          reference to xsl can be ignored.
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

    //-sqlusr
    //template: ster?com=sqlusr&whr=<where_pattern>
    //          Order of query params is not important.
    //pattern : * | <SQL part following WHERE>. The '*' means all.
    //          A special syntax must be used: ':' for = and '|' for <space>.
    //          This is because spaces and '=' are replaced in WWW requests.
    //          Note that string values must be enclosed in single quotes.
    //descript: returns part of the table 'stereotype_users' as specified
    //          by the condition in the 'whr' query parameter. If no
    //          row in DB satisfies the conditions, the result will
    //          not have any 'row' elements (200 OK will still be returned).
    //example : ster?com=sqlusr&whr=su_stereotype:'visitor'|and|su_degree<:2
    //          ster?com=sqlusr&whr=*
    //          ster?com=sqlusr&whr=isnull(su_degree)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/stereot_users.xsl"?>
    //          <result>
    //              <row><usr>user</usr><str>stereotype</str><deg>degree</deg></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is handled
    //          directly by an application and not by a browser, this
    //          reference to xsl can be ignored.    
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
