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

import pserver.utilities.PServerCommand;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pserver.WebServer;
import pserver.data.DBAccess;
import pserver.data.PServerResultSet;
import pserver.data.PStereotypesDBAccess;
import pserver.data.VectorMap;
import pserver.logic.PSReqWorker;
import pserver.utilities.ClientCredentialsChecker;

/**
 * This class contains all methods required for the management of Stereotypes
 * mode of PServer. Extends {@link PService}
 *
 * @author scify
 * @author Nick Zorbas <nickzorb@gmail.com>
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 * @since 1.2
 */
public class Stereotypes implements PService {

    /**
     * A
     * <code>HashMap</code> containing all available stereotype commands
     *
     * @see HashMap
     */
    private HashMap<String, PServerCommand> commands;

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
     * Overridden method of init from {@link PService}. Inserts all available
     * commands into a <code>HashMap</code> maping their names to a wrapped
     * function.
     *
     * @param params An array of strings containing the parameters
     * @throws Exception Default Exception is thrown.
     */
    //TODO check extreme
    @Override
    public void init(String[] params) throws Exception {
        commands = new HashMap<String, PServerCommand>();
        commands.put("addstr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return addStereotype(queryParam, respBody, dbAccess);
            }
        });
        commands.put("liststr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return listStereotypes(queryParam, respBody, dbAccess);
            }
        });
        commands.put("getstrusr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return getStereotypesUsers(queryParam, respBody, dbAccess);
            }
        });
        commands.put("getstrftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return getStereotypesFeatures(queryParam, respBody, dbAccess);
            }
        });
        commands.put("rmkstr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return remakeStereotype(queryParam, respBody, dbAccess);
            }
        });
        commands.put("remstr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return removeStereotype(queryParam, respBody, dbAccess);
            }
        });
        commands.put("addusr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return addUser(queryParam, respBody, dbAccess);
            }
        });
        commands.put("getusrstrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return getUsersStereotypes(queryParam, respBody, dbAccess);
            }
        });
        commands.put("incdgr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return increaseUsersDegree(queryParam, respBody, dbAccess);
            }
        });
        commands.put("setdgr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return setUsersDegree(queryParam, respBody, dbAccess);
            }
        });
        commands.put("remusr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return removeUser(queryParam, respBody, dbAccess);
            }
        });
        commands.put("updusrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return updateUsers(queryParam, respBody, dbAccess);
            }
        });
        commands.put("chkusrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                int resp = checkUsers(queryParam, respBody, dbAccess);
                return resp >= 0 ? PSReqWorker.NORMAL : resp;
            }
        });
        commands.put("findusrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                int resp = findUsers(queryParam, respBody, dbAccess);
                return resp >= 0 ? PSReqWorker.NORMAL : resp;
            }
        });
        commands.put("addftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return addFeatures(queryParam, respBody, dbAccess);
            }
        });
        commands.put("remftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return removeFeatures(queryParam, respBody, dbAccess);
            }
        });
        commands.put("incftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return increaseFeaturesValue(queryParam, respBody, dbAccess);
            }
        });
        commands.put("setftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return setFeaturesValue(queryParam, respBody, dbAccess);
            }
        });
        commands.put("updftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return updateFeatures(queryParam, respBody, dbAccess);
            }
        });
    }

    /**
     * Begins execution of a stereotype mode request. Returns a result code
     * depending on the result {@link pserver.logic.ReqWorker}.
     *
     * <p>
     * Servicing a request might remove certain parameters so servicing one
     * request with multiple services may result in unpredictable behaviour.</p>
     *
     * @param parameters The parameters of this request.
     * @param response A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int The response code {@link pserver.logic.ReqWorker}.
     */
    @Override
    public int service(VectorMap parameters, StringBuffer response, DBAccess dbAccess) {
        HashMap<String, ArrayList<String>> queryParam;
        queryParam = getParameters(parameters);
        StringBuffer respBody = response;
        if (!ClientCredentialsChecker.check(dbAccess, parameters)) {
            return PSReqWorker.REQUEST_ERR;
        }
        ArrayList<String> clientName = queryParam.get("clnt");
        clientName.add(0, clientName.get(0).split("[|]")[0]);

        return execute(queryParam, respBody, dbAccess);
    }

    /**
     * Selects the appropriate command for the request and begins it's execution
     * while handling certain exceptions.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int The response code {@link ReqWorker}.
     */
    private int execute(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.SERVER_ERR;
        String com = getParameter("com", queryParam);
        queryParam.remove("com");
        //if 'com' param not present, or not recognized request is invalid
        if (com == null) {
            WebServer.win.log.error("-Request command does not exist");
            return PSReqWorker.REQUEST_ERR;
        } else if (!commands.containsKey(com)) {
            WebServer.win.log.error("-Request command not recognized");
            return PSReqWorker.REQUEST_ERR;
        }
        //execute the command
        try {
            //first connect to DB
            dbAccess.connect();
            dbAccess.setAutoCommit(false); //transaction guarantees integrity
            //-start transaction body
            respCode = commands.get(com).runCommand(queryParam, respBody, dbAccess);
            //-end transaction body
            if (respCode == PSReqWorker.NORMAL) {
                dbAccess.commit();
            }
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        } finally {
            try {
                //rollback if not successful
                if (respCode != PSReqWorker.NORMAL) {
                    dbAccess.rollback();
                    WebServer.win.log.warn("-DB rolled back, data not saved");
                }
                //disconnect from DB anyway
                dbAccess.disconnect();
            } catch (SQLException e) {
                respCode = PSReqWorker.SERVER_ERR;
                WebServer.win.log.error("-DB Transaction problem: " + e);
            }
        }
        return respCode;
    }

    /**
     * Executes an addstr request by adding the stereotype and then adding to it
     * all appropriate users based on the stereotype's rule, if one was
     * provided. Finally it calculates the stereotype's profile based on it's
     * users.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int addStereotype(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        String rule = getParameter("rule", queryParam);
        //check if stereotype in 'str' is legal
        if (stereot == null) {
            WebServer.win.log.error("-Request missing str parameter");
            return PSReqWorker.REQUEST_ERR;
        } else if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        //execute request
        String query;
        int rowsAffected = 0;
        try {
            String table = DBAccess.STEREOTYPE_TABLE;
            //insert new stereotype 
            if (rule == null) {
                String[] columns = {DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, DBAccess.FIELD_PSCLIENT};
                String[] values = {stereot, clientName};
                rowsAffected = dbAccess.executeUpdate(DBAccess.buildInsertStatement(table, columns, values).toString());
            } else {
                String[] columns = {DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, DBAccess.STEREOTYPE_TABLE_FIELD_RULE, DBAccess.FIELD_PSCLIENT};
                String[] values = {stereot, rule, clientName};
                rowsAffected = dbAccess.executeUpdate(DBAccess.buildInsertStatement(table, columns, values).toString());
                rowsAffected += updateUsers(queryParam, respBody, dbAccess);
                rowsAffected += updateFeatures(queryParam, respBody, dbAccess);
            }
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        //build response
        StringBuilder temp = new StringBuilder("<num_of_rows>");
        temp.append(rowsAffected).append("</num_of_rows>");
        String[] resp = {temp.toString()};
        buildResponse(null, respBody, resp, dbAccess);
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes a liststr request by adding to the response buffer all
     * stereotypes matching parameter "str" with their respective rules. Also
     * uses parameter mod to only show stereotypes without users (if "mod" maps
     * to "u") or stereotypes without features (if "mod" maps to "p").
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int listStereotypes(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        String mode = getParameter("mod", queryParam);
        if (stereot == null) {
            WebServer.win.log.error("-Request missing str parameter");
            return PSReqWorker.REQUEST_ERR;
        } else if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        StringBuilder strCondition = buildWhereClause(
                DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, stereot);
        StringBuilder clntCondition = buildWhereClause(
                DBAccess.FIELD_PSCLIENT, clientName);

        //if a mode is specified add the corresponding check
        if (mode != null) {
            if (mode.equals("p") || mode.equals("u")) {
                String column;
                String table;
                if (mode.equals("p")) {
                    column = DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE;
                    table = DBAccess.STEREOTYPE_PROFILES_TABLE;
                } else {
                    column = DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE;
                    table = DBAccess.STEREOTYPE_USERS_TABLE;
                }
                //build condition
                strCondition.append(" AND ");
                strCondition.append(DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE);
                strCondition.append(" NOT IN (SELECT DISTINCT ");
                strCondition.append(column).append(" FROM ").append(table);
                strCondition.append(" WHERE ").append(DBAccess.FIELD_PSCLIENT);
                strCondition.append("='").append(clientName).append("')");
            } else {
                WebServer.win.log.error("-Invalid mode");
                return PSReqWorker.REQUEST_ERR;
            }
        }
        //execute request
        String query;
        int rowsAffected = 0;
        try {
            //get matching records
            String[] columns = {
                DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE,
                DBAccess.STEREOTYPE_TABLE_FIELD_RULE
            };
            String table = DBAccess.STEREOTYPE_TABLE;
            String[] where = {strCondition.toString(), clntCondition.toString()};
            PServerResultSet rs = dbAccess.executeQuery(
                    DBAccess.buildSelectStatement(table, columns, where).toString());
            ArrayList<String> response = new ArrayList<String>();
            //format response body        
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                String strVal = rs.getRs().getString(columns[0]);
                String rule = rs.getRs().getString(columns[1]);
                row.append("<str>").append(xml_encode(strVal)).append("</str><rule>");
                row.append(xml_encode(rule)).append("</rule>");
                rowsAffected += 1;  //number of result rows
                response.add(row.toString());
            }
            //close resultset and statement
            rs.close();
            String[] resp = new String[response.size()];
            for (int i = 0; i < response.size(); i++) {
                resp[i] = response.get(i);
            }
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem executing query: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes a getstrusr request by adding to the response buffer all
     * appropriate tuples (stereotype, user, degree) for stereotypes matching
     * parameter "str" and users matching parameter "usr".
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int getStereotypesUsers(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //Gather conditions
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        String users = getParameter("usr", queryParam);
        if (stereot == null) {
            WebServer.win.log.error("-Request missing str parameter");
            return PSReqWorker.REQUEST_ERR;
        } else if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        StringBuilder strCondition = buildWhereClause(
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE, stereot);
        StringBuilder clntCondition = buildWhereClause(
                DBAccess.FIELD_PSCLIENT, clientName);
        StringBuilder usrCondition = buildWhereClause(
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER, users);
        //execute request
        int rowsAffected = 0;
        String query;
        try {
            //get matching records
            String[] columns = {
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE,
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER,
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE
            };
            String table = DBAccess.STEREOTYPE_USERS_TABLE;
            String[] where;
            if (usrCondition != null) {
                where = new String[3];
                where[2] = usrCondition.toString();
            } else {
                where = new String[2];
            }
            where[0] = strCondition.toString();
            where[1] = clntCondition.toString();
            PServerResultSet rs = dbAccess.executeQuery(
                    DBAccess.buildSelectStatement(table, columns, where).toString());
            //format response body            
            ArrayList<String> response = new ArrayList<String>();
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                String stereotype = rs.getRs().getString(columns[0]);
                String strVal = rs.getRs().getString(columns[1]);
                String rule = rs.getRs().getString(columns[2]);
                row.append("<str>").append(xml_encode(stereotype)).append("</str>");
                row.append("<usr>").append(xml_encode(strVal)).append("</usr><dgr>");
                row.append(xml_encode(rule)).append("</dgr>");
                rowsAffected += 1;
                response.add(row.toString());
            }
            //close resultset and statement
            rs.close();
            String[] resp = response.toArray(new String[response.size()]);
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem executing query: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes a getstrftr request by adding to the response buffer all
     * appropriate tuples (stereotype, feature, degree) for stereotypes matching
     * parameter "str" and features matching parameter "ftr".
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int getStereotypesFeatures(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        String features = getParameter("ftr", queryParam);
        String order = getParameter("srt", queryParam);
        if (stereot == null) {
            WebServer.win.log.error("-Request missing str parameter");
            return PSReqWorker.REQUEST_ERR;
        } else if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }

        StringBuilder strCondition = buildWhereClause(
                DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE, stereot);
        StringBuilder clntCondition = buildWhereClause(
                DBAccess.FIELD_PSCLIENT, clientName);
        StringBuilder ftrCondition = buildWhereClause(
                DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE, features);

        if (order != null && order.equalsIgnoreCase("asc")) {
            order = " ORDER BY " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_VALUE + " ASC";
        } else {
            order = " ORDER BY " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_VALUE + " DESC";
        }
        //execute request
        int rowsAffected = 0;
        String query;
        try {
            //get matching records
            String[] columns = {
                DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE,
                DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE,
                DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_VALUE
            };
            String table = DBAccess.STEREOTYPE_PROFILES_TABLE;
            String[] where;
            if (ftrCondition != null) {
                where = new String[3];
                where[2] = ftrCondition.toString();
            } else {
                where = new String[2];
            }
            where[0] = strCondition.toString();
            where[1] = clntCondition.toString();
            query = DBAccess.buildSelectStatement(table, columns, where).append(order).toString();
            PServerResultSet rs = dbAccess.executeQuery(query);

            ArrayList<String> response = new ArrayList<String>();
            //format response body         
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                String str = rs.getRs().getString(columns[0]);
                String strVal = rs.getRs().getString(columns[1]);
                String rule = rs.getRs().getString(columns[2]);
                row.append("<str>").append(xml_encode(str)).append("</str>");
                row.append("<ftr>").append(xml_encode(strVal)).append("</ftr><value>");
                row.append(xml_encode(rule)).append("</value>");
                rowsAffected += 1;
                response.add(row.toString());
            }
            //close resultset and statement
            rs.close();
            String[] resp = response.toArray(new String[response.size()]);
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem executing query: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes a rmkstr request by executing an updusrs request and an updftrs
     * request.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int remakeStereotype(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        if (stereot == null) {
            WebServer.win.log.error("-Request missing str parameter");
            return PSReqWorker.REQUEST_ERR;
        } else if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        StringBuilder strCondition = buildWhereClause(
                DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, stereot);
        StringBuilder clntCondition = buildWhereClause(
                DBAccess.FIELD_PSCLIENT, clientName);
        String query;
        int rowsAffected = 0;
        try {
            //get matching records
            String[] columns = {
                DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE,
                DBAccess.STEREOTYPE_TABLE_FIELD_RULE
            };
            String table = DBAccess.STEREOTYPE_TABLE;
            String[] where = {strCondition.toString(), clntCondition.toString()};
            PServerResultSet rs = dbAccess.executeQuery(
                    DBAccess.buildSelectStatement(table, columns, where).toString());
            while (rs.next()) {
                HashMap<String, ArrayList<String>> tempqueryParam = (HashMap<String, ArrayList<String>>) queryParam.clone();
                ArrayList<String> str = new ArrayList<String>();
                str.add(rs.getRs().getString(columns[0]));
                tempqueryParam.put("str", str);
                int respCode = updateUsers(tempqueryParam, respBody, dbAccess);
                if (respCode == PSReqWorker.NORMAL) {
                    rowsAffected += respCode;
                    respCode = updateFeatures(tempqueryParam, respBody, dbAccess);
                    rowsAffected += respCode >= 0 ? respCode : 0;
                }
            }
            //close resultset and statement
            rs.close();
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem executing query: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        return rowsAffected;
    }

    /**
     * Executes a remstr request by removing any and all references to all
     * stereotypes matching parameter "str" or all stereotypes if parameter
     * "str" is null.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int removeStereotype(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        ArrayList<String> stereot = queryParam.get("str");
        if (stereot != null) {
            for (String str : stereot) {
                if (!DBAccess.legalStrName(str)) {
                    WebServer.win.log.error("-Invalid stereotype name " + str);
                    return PSReqWorker.REQUEST_ERR;
                }
            }
        }
        StringBuilder clntCondition = buildWhereClause(
                DBAccess.FIELD_PSCLIENT, clientName);
        //execute request
        int success = PSReqWorker.NORMAL;
        String query;
        int rowsAffected = 0;

        String[] tables = {
            DBAccess.STEREOTYPE_TABLE,
            DBAccess.STEREOTYPE_PROFILES_TABLE,
            DBAccess.STEREOTYPE_STATISTICS_TABLE,
            DBAccess.STEREOTYPE_USERS_TABLE
        };

        String[] stereotype_columns = {
            DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE,
            DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE,
            DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE,
            DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE
        };

        try {
            //delete all stereotypes
            if (stereot == null) {  //no 'str' query parameters specified
                for (String table : tables) {
                    rowsAffected += dbAccess.executeUpdate(DBAccess.buildDeleteStatement(table, clntCondition.toString()).toString());
                }
            } else {
                //delete specified stereotypes
                for (String str : stereot) {
                    for (int i = 0; i < tables.length; i++) {
                        StringBuilder strCondition = buildWhereClause(
                                stereotype_columns[i], str);
                        String[] where = {strCondition.toString(), clntCondition.toString()};
                        rowsAffected += dbAccess.executeUpdate(DBAccess.buildDeleteStatement(tables[i], where).toString());
                    }
                }
            }
            //format response body
            //response will be used only in case of success
            StringBuilder temp = new StringBuilder("<num_of_rows>");
            temp.append(rowsAffected).append("</num_of_rows>");
            String[] resp = {temp.toString()};
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            success = PSReqWorker.SERVER_ERR;
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows deleted: " + rowsAffected);
        return success;
    }

    /**
     * Executes an addusr request by adding a user to all stereotypes provided
     * with the degree provided. Also updates stereotype profiles by adding the
     * new user's features.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int addUser(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String userName = getParameter("usr", queryParam);
        String clientName = queryParam.get("clnt").get(0);
        queryParam.remove("usr");
        queryParam.remove("clnt");

        if (userName == null) {
            WebServer.win.log.error("-Missing user name");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalUsrName(userName)) {
            WebServer.win.log.error("-Invalid user name");
            return PSReqWorker.REQUEST_ERR;
        }
        //execute request
        String query;
        int rowsAffected = 0;
        try {
            //insert each (user, stereotype, degree) in a new row in 'stereotype_users'.
            //Note that the specified stereotypes must already exist in 'stereotypes'
            for (String stereot : queryParam.keySet()) {
                if (!stereotypeExists(dbAccess, stereot, clientName)) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " does not exists");
                    continue;
                }
                if (stereotypeHasUser(dbAccess, stereot, userName, clientName)) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " already has the user " + userName);
                    continue;
                }
                String degree = getParameter(stereot, queryParam);
                String numDegree = DBAccess.strToNumStr(degree);  //numeric version of degree                    
                updateStereotypeWithUser(dbAccess, clientName, stereot, userName, Float.parseFloat(numDegree));
                String table = DBAccess.STEREOTYPE_USERS_TABLE;
                String[] columns = {DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER,
                    DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE,
                    DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE,
                    DBAccess.FIELD_PSCLIENT};
                String[] values = {userName, stereot, numDegree, clientName};
                query = DBAccess.buildInsertStatement(table, columns, values).toString();
                rowsAffected += dbAccess.executeUpdate(query);
            }
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        //build response
        StringBuilder temp = new StringBuilder("<num_of_rows>");
        temp.append(rowsAffected).append("</num_of_rows>");
        String[] resp = {temp.toString()};
        buildResponse(null, respBody, resp, dbAccess);
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Updates a stereotype's profile by adding a new users features to the
     * current stereotype's features. This is called as part of the addusr
     * request;
     *
     * @param clientName the client performing the request.
     * @param stereotype the stereotype being updated.
     * @param user the user that was added.
     * @param degree the degree this user has with this stereotype.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int updateStereotypeWithUser(DBAccess dbAccess, String clientName, String stereotype, String user, float degree) throws SQLException {
        int res = 0;
        String table = DBAccess.UPROFILE_TABLE;
        StringBuilder clntCondition = buildWhereClause(
                DBAccess.FIELD_PSCLIENT, clientName);
        StringBuilder usrCondition = buildWhereClause(
                DBAccess.UPROFILE_TABLE_FIELD_USER, user);
        String[] columns = {"'" + stereotype + "'",
            DBAccess.UPROFILE_TABLE_FIELD_FEATURE, "0", "'" + clientName + "'"};
        String[] where = {usrCondition.toString(), clntCondition.toString()};
        String subSelect = DBAccess.buildSelectStatement(table, columns, where).toString();

        table = DBAccess.STEREOTYPE_PROFILES_TABLE;
        columns = new String[4];
        columns[0] = DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE;
        columns[1] = DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE;
        columns[2] = DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE;
        columns[3] = DBAccess.FIELD_PSCLIENT;
        String[] values = {subSelect};
        StringBuilder query = DBAccess.buildInsertStatement(table, columns, values);
        //subSelect will be put inside VALUES ('') by the buildInsertStatement
        //so we have to remove that
        int indx = query.indexOf("VALUES");
        query.delete(indx, query.length());
        query.append(subSelect);
        query.insert(6, " IGNORE");
        res += dbAccess.executeUpdate(query.toString());
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(DBAccess.STEREOTYPE_PROFILES_TABLE).append(" s, ");
        sql.append(DBAccess.UPROFILE_TABLE).append(" u SET s.");
        sql.append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE);
        sql.append("=s.");
        sql.append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE);
        sql.append("+").append(degree).append("*u.");
        sql.append(DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE);
        sql.append(" WHERE u.").append(DBAccess.UPROFILE_TABLE_FIELD_USER);
        sql.append("='").append(user).append("' AND s.");
        sql.append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE);
        sql.append("='").append(stereotype).append("' AND u.");
        sql.append(DBAccess.FIELD_PSCLIENT).append("='").append(clientName);
        sql.append("' AND s.").append(DBAccess.FIELD_PSCLIENT).append("='");
        sql.append(clientName).append("' AND s.");
        sql.append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE);
        sql.append("=u.").append(DBAccess.UPROFILE_TABLE_FIELD_FEATURE);
        res += dbAccess.executeUpdate(sql.toString());
        return res;
    }

    /**
     * Checks wether a stereotype exists.
     *
     * @param stereot the stereotype in question
     * @param clientName the client performing the request
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private boolean stereotypeExists(DBAccess dbAccess, String stereot, String clientName) throws SQLException {
        String table = DBAccess.STEREOTYPE_TABLE;
        StringBuilder strCondition = buildWhereClause(
                DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, stereot);
        StringBuilder clntCondition = buildWhereClause(
                DBAccess.FIELD_PSCLIENT, clientName);
        String[] where = {strCondition.toString(), clntCondition.toString()};
        PServerResultSet rs = dbAccess.executeQuery(
                DBAccess.buildSelectStatement(table, where).toString());
        boolean res = false;
        if (rs.next()) {
            res = true;
        }
        rs.close();
        return res;
    }

    /**
     * Checks wether a user belongs to a stereotype.
     *
     * @param stereot the stereotype in question
     * @param user the user in question
     * @param clientName the client performing the request
     * @param dbAccess The database manager.
     * @throws SQLException
     * @return int the response code {@link ReqWorker}.
     */
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

    /**
     * Executes a getusrstrs request by listing all tuples (user, stereotype,
     * degree) for users matching parameter "str" and stereotypes matching
     * parameter "str" if it exists or "*" otherwise, and appends the response
     * to the response buffer.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int getUsersStereotypes(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String user = getParameter("usr", queryParam);
        String stereot = getParameter("str", queryParam);
        String numberOfRes = getParameter("num", queryParam);
        stereot = stereot == null ? "*" : stereot;
        numberOfRes = numberOfRes == null ? "*" : numberOfRes;
        String sortOrder = getParameter("srt", queryParam);
        if (user == null) {
            WebServer.win.log.error("-Missing user name");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalUsrName(user)) {
            WebServer.win.log.error("-Invalid user name");
            return PSReqWorker.REQUEST_ERR;
        }
        //check if upper limit of result number can be obtained
        int limit = DBAccess.numPatternCondition(numberOfRes);
        if (limit == -1) {
            WebServer.win.log.error("-Invalid num parameter");
            return PSReqWorker.REQUEST_ERR;
        }
        String strCondition = buildWhereClause(
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE,
                stereot).toString();
        String srtCondition = DBAccess.srtPatternCondition(sortOrder);
        String clntCondition = buildWhereClause(
                DBAccess.FIELD_PSCLIENT, clientName).toString();
        String usrCondition = buildWhereClause(
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER, user).toString();
        //execute request
        StringBuilder query;
        int rowsAffected = 0;
        try {
            //get matching records
            String table = DBAccess.STEREOTYPE_USERS_TABLE;
            String[] columns = {
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER,
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE,
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE
            };
            String[] where = {usrCondition, clntCondition};
            query = DBAccess.buildSelectStatement(table, columns, where);
            query.append(" order by ");
            query.append(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE);
            query.append(srtCondition).append(", ").append(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE);
            PServerResultSet rs = dbAccess.executeQuery(query.toString());
            //format response body
            //select first rows as specified by query parameter 'num'
            ArrayList<String> response = new ArrayList<String>();
            //format response body            
            while (rowsAffected < limit && rs.next()) {
                StringBuilder row = new StringBuilder();
                String userVal = rs.getRs().getString(columns[0]);
                String stereotVal = rs.getRs().getString(columns[1]);
                String degreeVal = rs.getRs().getString(columns[2]);
                if (rs.getRs().wasNull()) {
                    degreeVal = "";
                }
                row.append("<usr>").append(xml_encode(userVal));
                row.append("</usr><str>").append(xml_encode(stereotVal));
                row.append("</str><deg>").append(xml_encode(degreeVal));
                row.append("</deg>");
                rowsAffected += 1;  //number of result rows
                response.add(row.toString());
            }
            String[] resp = response.toArray(new String[response.size()]);
            buildResponse(null, respBody, resp, dbAccess);
            rs.close();
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem executing query: " + e);
        }
        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes an incdeg request by increasing the degree of all users matching
     * parameter "usr" to all stereotypes given by the given degrees.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int increaseUsersDegree(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = getParameter("clnt", queryParam);
        String user = getParameter("usr", queryParam);
        queryParam.remove("clnt");
        queryParam.remove("usr");
        if (user == null) {
            WebServer.win.log.error("-Missing user name");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalUsrName(user)) {
            WebServer.win.log.error("-Invalid user name");
            return PSReqWorker.REQUEST_ERR;
        }
        String clntCondition = buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString();
        String usrCondition = buildWhereClause(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER, user).toString();
        int rowsAffected = 0;
        try {
            //increment degrees of stereotypes for a user            
            for (String stereot : queryParam.keySet()) {
                if (!stereotypeExists(dbAccess, stereot, clientName)) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " does not exist");
                    continue;
                }
                if (!stereotypeHasUser(dbAccess, stereot, user, clientName)) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " does not have the user " + user);
                    continue;
                }
                String step = queryParam.get(stereot).get(0);
                Float numStep = DBAccess.strToNum(step);  //is it numeric?
                if (numStep != null) {  //if null, 'step' not numeric, misspelled request
                    //get degree for current user, stereotype record
                    String strCondition = buildWhereClause(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE, stereot).toString();
                    String table = DBAccess.STEREOTYPE_USERS_TABLE;
                    String[] columns = {DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE};
                    String[] where = {usrCondition, strCondition, clntCondition};
                    StringBuilder query = DBAccess.buildSelectStatement(table, columns, where);
                    PServerResultSet rs = dbAccess.executeQuery(query.toString());
                    boolean recFound = rs.next();  //expect one row or none
                    Double degree = recFound ? new Double(rs.getRs().getDouble("su_degree")) : 0;
                    rs.close();  //in any case
                    //update current user, stereotype record
                    double newNumDegree = degree.doubleValue() + numStep.doubleValue();
                    String newDegree = DBAccess.formatDouble(new Double(newNumDegree));
                    columns = new String[1];
                    columns[0] = DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE;
                    String[] values = {newDegree.toString()};
                    query = DBAccess.buildUpdateStatement(table, columns, values, where);
                    rowsAffected += dbAccess.executeUpdate(query.toString());
                } else {
                    WebServer.win.log.debug("-Malformed stereotype degree");
                    return PSReqWorker.REQUEST_ERR;
                }  //misspelled request, abort and rollback
            }
            //format response body
            //response will be used only in case of success            
            StringBuilder temp = new StringBuilder("<num_of_rows>");
            temp.append(rowsAffected).append("</num_of_rows>");
            String[] resp = {temp.toString()};
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem updating DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes a setdeg request by setting the degree of all users matching
     * parameter "usr" to all stereotypes given to the given degrees.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int setUsersDegree(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String user = getParameter("usr", queryParam);
        queryParam.remove("clnt");
        queryParam.remove("usr");
        if (user == null) {
            WebServer.win.log.error("-Missing user name");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalUsrName(user)) {
            WebServer.win.log.error("-Invalid user name");
            return PSReqWorker.REQUEST_ERR;
        }
        String clntCondition = buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString();
        String usrCondition = buildWhereClause(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER, user).toString();
        int rowsAffected = 0;
        try {
            //increment degrees of stereotypes for a user            
            for (String stereot : queryParam.keySet()) {
                if (!stereotypeExists(dbAccess, stereot, clientName)) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " does not exists");
                    continue;
                }
                if (!stereotypeHasUser(dbAccess, stereot, user, clientName)) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " already does not have the user " + user);
                    continue;
                }
                String step = queryParam.get(stereot).get(0);
                Float numStep = DBAccess.strToNum(step);  //is it numeric?
                if (numStep != null) {  //if null, 'step' not numeric, misspelled request
                    //get degree for current user, stereotype record
                    String strCondition = buildWhereClause(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE, stereot).toString();
                    String table = DBAccess.STEREOTYPE_USERS_TABLE;
                    String[] columns = {DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE};
                    String[] where = {usrCondition, strCondition, clntCondition};
                    //update current user, stereotype record
                    double newNumDegree = numStep.doubleValue();
                    String newDegree = DBAccess.formatDouble(new Double(newNumDegree));
                    String[] values = {newDegree.toString()};
                    StringBuilder query = DBAccess.buildUpdateStatement(table, columns, values, where);
                    rowsAffected += dbAccess.executeUpdate(query.toString());

                } else {
                    WebServer.win.log.debug("-Malformed stereotype degree");
                    return PSReqWorker.REQUEST_ERR;
                }
            }
            //format response body    
            StringBuilder temp = new StringBuilder("<num_of_rows>");
            temp.append(rowsAffected).append("</num_of_rows>");
            String[] resp = {temp.toString()};
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem updating DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes a remusr request by deleting all references matching the tuples
     * (user, stereotype) provided. This does not affect the stereotypes'
     * profiles.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int removeUser(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        queryParam.remove("clnt");
        //execute request
        String query;
        int rowsAffected = 0;
        try {
            //delete rows of matching stereotypes for specified users
            for (String user : queryParam.keySet()) {
                String stereot = queryParam.get(user).get(0);
                if (!stereotypeExists(dbAccess, stereot, clientName)) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " does not exists");
                    continue;
                }
                if (!stereotypeHasUser(dbAccess, stereot, user, clientName)) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " already does not have the user " + user);
                    continue;
                }
                //TODO decide, is this how we want to do it?
                PStereotypesDBAccess sdbAccess = new PStereotypesDBAccess(dbAccess);
                rowsAffected += sdbAccess.removeUserFromStereotype(user, stereot, clientName);
            }
            StringBuilder temp = new StringBuilder("<num_of_rows>");
            temp.append(rowsAffected).append("</num_of_rows>");
            String[] resp = {temp.toString()};
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        WebServer.win.log.debug("-Num of rows deleted: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes an updusrs request by initiating a chkusrs and a fndusrs
     * request. This does not affect the stereotypes profiles.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int updateUsers(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int rowsRemoved = checkUsers(queryParam, respBody, dbAccess);
        int rowsAdded = -1;
        if (rowsRemoved >= 0) {
            rowsAdded = findUsers(queryParam, respBody, dbAccess);
        }
        int respCode = rowsAdded >= 0 ? PSReqWorker.NORMAL : PSReqWorker.SERVER_ERR;
        StringBuilder temp = new StringBuilder("<num_of_rows>");
        temp.append(rowsAdded + rowsRemoved).append("</num_of_rows>");
        String[] resp = {temp.toString()};
        buildResponse(null, respBody, resp, dbAccess);
        return respCode;
    }

    /**
     * Executes a chkusrs request by removing any users associated with the
     * stereotype that don't match the stereotype's rule. This does not affect
     * the stereotype's profile.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int checkUsers(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        if (stereot == null) {
            WebServer.win.log.error("-Missing stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        String table = DBAccess.STEREOTYPE_TABLE;
        String[] columns = {DBAccess.STEREOTYPE_TABLE_FIELD_RULE};
        String[] where = {
            buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString(),
            buildWhereClause(DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, stereot).toString(),};
        String rule = "";
        try {
            PServerResultSet rs = dbAccess.executeQuery(DBAccess.buildSelectStatement(table, columns, where).toString());
            if (rs.next()) {
                rule = rs.getRs().getString(DBAccess.STEREOTYPE_TABLE_FIELD_RULE);
            } else {
                WebServer.win.log.error("-Couldn't find stereotype " + stereot);
                return PSReqWorker.REQUEST_ERR;
            }
        } catch (SQLException ex) {
            WebServer.win.log.error("-Problem finding stereotype " + ex);
            return PSReqWorker.SERVER_ERR;
        }
        if (rule.equals("")) {
            return 0;
        }
        int respCode = 0;
        SqlNode parent = new SqlNode(rule);
        parent.propagate();
        String query = "DELETE FROM " + DBAccess.STEREOTYPE_USERS_TABLE
                + " WHERE " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE
                + "='" + stereot + "' AND " + DBAccess.FIELD_PSCLIENT + "='"
                + clientName + "' AND " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER
                + " NOT IN ( " + parent.toSql() + ")";
        try {
            respCode = dbAccess.executeUpdate(query);
        } catch (SQLException ex) {
            WebServer.win.log.error("-Error updating database " + ex);
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Executes a fndusrs request by adding to the stereotype any users that are
     * not currently associated with it but comply to the stereotypes rule. This
     * does not affect the stereotype's profile.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int findUsers(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        if (stereot == null) {
            WebServer.win.log.error("-Missing stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        String table = DBAccess.STEREOTYPE_TABLE;
        String[] columns = {DBAccess.STEREOTYPE_TABLE_FIELD_RULE};
        String[] where = {
            buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString(),
            buildWhereClause(DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, stereot).toString(),};
        String rule = "";
        try {
            PServerResultSet rs = dbAccess.executeQuery(DBAccess.buildSelectStatement(table, columns, where).toString());
            if (rs.next()) {
                rule = rs.getRs().getString(DBAccess.STEREOTYPE_TABLE_FIELD_RULE);
            } else {
                WebServer.win.log.error("-Couldn't find stereotype " + stereot);
                return PSReqWorker.REQUEST_ERR;
            }
        } catch (SQLException ex) {
            WebServer.win.log.error("-Problem finding stereotype " + ex);
            return PSReqWorker.SERVER_ERR;
        }
        if (rule.equals("")) {
            return 0;
        }
        int respCode = 0;
        SqlNode parent = new SqlNode(rule);
        parent.propagate();
        String insUsrSql = "INSERT IGNORE INTO " + DBAccess.STEREOTYPE_USERS_TABLE
                + "(" + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE
                + "," + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER
                + "," + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE
                + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ('" + stereot + "',?,1,'" + clientName + "')";
        try {
            PreparedStatement prep = dbAccess.getConnection().prepareStatement(insUsrSql);
            PServerResultSet rs = dbAccess.executeQuery(parent.toSql());
            while (rs.next()) {
                String user = rs.getRs().getString(1);
                prep.setString(1, user);
                prep.addBatch();
            }
            rs.close();
            for (Integer i : prep.executeBatch()) {
                respCode += i;
            }
            prep.close();
        } catch (SQLException ex) {
            WebServer.win.log.error("-Error updating database " + ex);
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    /**
     * Executes an addftrs request by adding all features provided by tuples
     * (feature, degree) to the stereotype provided by parameter "str".
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int addFeatures(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        queryParam.remove("clnt");
        queryParam.remove("str");
        if (stereot == null) {
            WebServer.win.log.error("-Missing stereotype");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        //execute request
        int rowsAffected = 0;
        try {
            for (String ftr : queryParam.keySet()) {
                String newValue = queryParam.get(ftr).get(0);
                String numNewValue = DBAccess.strToNumStr(newValue);  //numeric version of value
                String table = DBAccess.STEREOTYPE_PROFILES_TABLE;
                String[] columns = {DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_VALUE,
                    DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE};
                String[] values = {newValue, numNewValue};
                String[] where = {
                    buildWhereClause(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE, stereot).toString(),
                    buildWhereClause(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE, ftr).toString(),
                    buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString()
                };
                int tmpRows = dbAccess.executeUpdate(DBAccess.buildUpdateStatement(table, columns, values, where).toString());
                if (tmpRows == 0) {
                    StringBuilder query = new StringBuilder("INSERT INTO ");
                    query.append(DBAccess.STEREOTYPE_PROFILES_TABLE);
                    query.append(" (").append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE);
                    query.append(", ").append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE);
                    query.append(", ").append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_VALUE);
                    query.append(", ").append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE);
                    query.append(", ").append(DBAccess.FIELD_PSCLIENT).append(") ");
                    query.append("SELECT '").append(stereot).append("', ");
                    query.append(DBAccess.FEATURE_TABLE_FIELD_FEATURE);
                    query.append(", ").append(DBAccess.FEATURE_TABLE_FIELD_DEFVALUE);
                    query.append(", ").append(DBAccess.FEATURE_TABLE_FIELD_VALUE_NUMDEFVALUE);
                    query.append(", ").append(DBAccess.FIELD_PSCLIENT).append(" FROM ");
                    query.append(DBAccess.FEATURE_TABLE).append(" WHERE ");
                    query.append(buildWhereClause(DBAccess.FEATURE_TABLE_FIELD_FEATURE, ftr).toString());
                    query.append(" AND ").append(buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString());
                    dbAccess.executeUpdate(query.toString());
                    tmpRows = dbAccess.executeUpdate(DBAccess.buildUpdateStatement(table, columns, values, where).toString());
                }
                rowsAffected += tmpRows;
            }
            //format response body
            StringBuilder temp = new StringBuilder("<num_of_rows>");
            temp.append(rowsAffected).append("</num_of_rows>");
            String[] resp = {temp.toString()};
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem updating DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes a remftrs request by removing all references to the features
     * provided from the stereotypes matching parameter "str".
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int removeFeatures(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        queryParam.remove("clnt");
        queryParam.remove("str");
        if (stereot == null) {
            WebServer.win.log.error("-Missing stereotype");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        int rowsAffected = 0;
        String table = DBAccess.STEREOTYPE_PROFILES_TABLE;
        try {
            if (queryParam.get("ftr") == null) {
                String[] where = {
                    buildWhereClause(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE, "*").toString(),
                    buildWhereClause(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE, stereot).toString(),
                    buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString()
                };
                rowsAffected = dbAccess.executeUpdate(DBAccess.buildDeleteStatement(table, where).toString());
            } else {
                for (String ftr : queryParam.get("ftr")) {
                    String[] where = {
                        buildWhereClause(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE, ftr).toString(),
                        buildWhereClause(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE, stereot).toString(),
                        buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString()
                    };
                    rowsAffected += dbAccess.executeUpdate(DBAccess.buildDeleteStatement(table, where).toString());
                }
            }
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        //format response body
        StringBuilder temp = new StringBuilder("<num_of_rows>");
        temp.append(rowsAffected).append("</num_of_rows>");
        String[] resp = {temp.toString()};
        buildResponse(null, respBody, resp, dbAccess);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes an incftrs request by increasing the stereotype profile values
     * corresponding to the stereotype provided from parameter "str" and the
     * features provided by the values provided from the tuples (feature,
     * value).
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int increaseFeaturesValue(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        queryParam.remove("str");
        queryParam.remove("clnt");
        if (stereot == null) {
            WebServer.win.log.error("-Missing stereotype");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        //execute request
        String query;
        int rowsAffected = 0;
        //TODO fix this to use dbaccess
        try {
            for (String feature : queryParam.keySet()) {
                String step = queryParam.get(feature).get(0);
                Float numStep = DBAccess.strToNum(step);  //is it numeric?
                if (numStep != null) {  //if null, 'step' not numeric, misspelled request
                    //get value for current stereotype, feature record
                    query = "select sp_numvalue from stereotype_profiles where sp_stereotype='" + stereot + "' and sp_feature ='" + feature + "' and FK_psclient='" + clientName + "' ";
                    PServerResultSet rs = dbAccess.executeQuery(query);
                    boolean recFound = rs.next();  //expect one row or none
                    String value = recFound ? rs.getRs().getString("sp_numvalue") : null;
                    rs.close();  //in any case
                    Float numValue = DBAccess.strToNum(value);  //is it numeric?
                    double newNumValue;
                    int tmpRows = 0;
                    if (recFound == false) { //if recFound == false we assume that it was uninitialized and treat it as 0
                        newNumValue = numStep.doubleValue();
                        //insert new feature record
                        String newValue = DBAccess.formatDouble(new Double(newNumValue));
                        query = "INSERT into stereotype_profiles values ('" + stereot + "', '" + feature + "', '" + newNumValue + "', '" + newNumValue + "', '" + clientName + "')";
                        tmpRows = dbAccess.executeUpdate(query);
                    } else if (numValue != null) {  //if null, 'value' does not exist or not numeric
                        newNumValue = numValue.doubleValue() + numStep.doubleValue();
                        //update current stereotype, feature record
                        String newValue = DBAccess.formatDouble(new Double(newNumValue));
                        query = "UPDATE stereotype_profiles set sp_value='" + newValue + "', sp_numvalue=" + newValue + " where sp_stereotype='" + stereot + "' and sp_feature='" + feature + "' and FK_psclient='" + clientName + "' ";
                        tmpRows = dbAccess.executeUpdate(query);
                    }
                    rowsAffected += tmpRows;
                } //else if numStep == null
                else {
                    WebServer.win.log.debug("-Non numerical value");
                    return PSReqWorker.REQUEST_ERR;
                }
            }
            //format response body           
            StringBuilder temp = new StringBuilder("<num_of_rows>");
            temp.append(rowsAffected).append("</num_of_rows>");
            String[] resp = {temp.toString()};
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem updating DB: " + e);
            return PSReqWorker.REQUEST_ERR;
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes an setftrs request by setting the stereotype profile values
     * corresponding to the stereotype provided from parameter "str" and the
     * features provided to the values provided from the tuples (feature,
     * value).
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int setFeaturesValue(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        queryParam.remove("str");
        queryParam.remove("clnt");
        if (stereot == null) {
            WebServer.win.log.error("-Missing stereotype");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        int rowsAffected = 0;
        try {
            //update values of matching stereotype features
            for (String ftr : queryParam.keySet()) {
                String newValue = queryParam.get(ftr).get(0);
                String numNewValue = DBAccess.strToNumStr(newValue);  //numeric version of value
                String table = DBAccess.STEREOTYPE_PROFILES_TABLE;
                String[] columns = {
                    DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_VALUE,
                    DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE
                };
                String[] values = {newValue, numNewValue};
                String[] where = {
                    buildWhereClause(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE, stereot).toString(),
                    buildWhereClause(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE, ftr).toString(),
                    buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString()
                };
                int tmpRows = dbAccess.executeUpdate(DBAccess.buildUpdateStatement(table, columns, values, where).toString());
                if (tmpRows == 0) {
                    StringBuilder query = new StringBuilder("INSERT into ");
                    query.append(DBAccess.STEREOTYPE_PROFILES_TABLE);
                    query.append(" (").append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE);
                    query.append(", ").append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE);
                    query.append(", ").append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_VALUE);
                    query.append(", ").append(DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE);
                    query.append(", ").append(DBAccess.FIELD_PSCLIENT).append(") ");
                    query.append("SELECT '").append(stereot).append("', ");
                    query.append(DBAccess.FEATURE_TABLE_FIELD_FEATURE);
                    query.append(", ").append(DBAccess.FEATURE_TABLE_FIELD_DEFVALUE);
                    query.append(", ").append(DBAccess.FEATURE_TABLE_FIELD_VALUE_NUMDEFVALUE);
                    query.append(", ").append(DBAccess.FIELD_PSCLIENT).append(" FROM ");
                    query.append(DBAccess.FEATURE_TABLE).append(" WHERE ");
                    query.append(buildWhereClause(DBAccess.FEATURE_TABLE_FIELD_FEATURE, ftr).toString());
                    query.append(" AND ").append(buildWhereClause(DBAccess.FIELD_PSCLIENT, clientName).toString());
                    tmpRows += dbAccess.executeUpdate(query.toString());
                    tmpRows += dbAccess.executeUpdate(DBAccess.buildUpdateStatement(table, columns, values, where).toString());
                }
                rowsAffected += tmpRows;
            }
            //format response body           
            StringBuilder temp = new StringBuilder("<num_of_rows>");
            temp.append(rowsAffected).append("</num_of_rows>");
            String[] resp = {temp.toString()};
            buildResponse(null, respBody, resp, dbAccess);
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem updating DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        WebServer.win.log.debug("-Num of rows updated: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    /**
     * Executes an updftrs request by removing all stereotypes features and
     * recollecting all features of the stereotypes users.
     *
     * @param queryParam The parameters of this request.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param dbAccess The database manager.
     * @return int the response code {@link ReqWorker}.
     */
    private int updateFeatures(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        String clientName = queryParam.get("clnt").get(0);
        String stereot = getParameter("str", queryParam);
        HashMap<String, ArrayList<String>> deleteParam = new HashMap<String, ArrayList<String>>();
        deleteParam.put("clnt", queryParam.get("clnt"));
        deleteParam.put("str", queryParam.get("str"));
        queryParam.remove("clnt");
        queryParam.remove("str");
        if (stereot == null) {
            WebServer.win.log.error("-Missing stereotype");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        int resp = removeFeatures(deleteParam, respBody, dbAccess);
        if (resp < 0) {
            return resp;
        }
        int rows = 0;
        //TODO fix this to use DBAccess and account for degree
        StringBuilder query = new StringBuilder("INSERT INTO stereotype_profiles ");
        query.append("SELECT '").append(stereot).append("', up_feature");
        query.append(",SUM(up_value), SUM(up_value), s.FK_psclient from ");
        query.append("user_profiles u join stereotype_users s on su_user=up_user");
        query.append(" where su_stereotype='").append(stereot).append("' and ");
        query.append("u.FK_psclient='").append(clientName).append("' and ");
        query.append("s.FK_psclient='").append(clientName).append("' group by");
        query.append(" up_feature");
        try {
            rows = dbAccess.executeUpdate(query.toString());
        } catch (SQLException ex) {
            WebServer.win.log.debug("-Problem updating DB: " + ex);
            return PSReqWorker.SERVER_ERR;
        }
        return PSReqWorker.NORMAL;
    }

    /**
     * Builds a standarized response containing the rows provided.
     *
     * @param xslPath wheter a specialized header is needed.
     * @param respBody A <code>StringBuffer</code> to append the results to.
     * @param content The rows to append to the respBody.
     * @param dbAccess The database manager.
     */
    private void buildResponse(String xslPath, StringBuffer respBody, String[] content, DBAccess dbAccess) {
        respBody.setLength(0);
        respBody.append(DBAccess.xmlHeader(xslPath));
        respBody.append("<result>\n");
        for (String s : content) {
            respBody.append("<row>").append(s).append("</row>\n");
        }
        respBody.append("</result>");
    }

    /**
     * Escapes certain characters to make sure that the resulting string can be
     * put inside an xml response.
     *
     * @param s The <code>String</code> to be encoded
     * @return The encoded <code>String</code>
     */
    private static String xml_encode(String s) {
        String[] replaceables = {"&", "<", ">", "'", "\""};
        String[] replacements = {"&amp;", "&lt;", "&gt;", "&apos;", "&quot;"};
        for (int i = 0; i < replaceables.length; i++) {
            s = s.replaceAll(replaceables[i], replacements[i]);
        }
        return s;
    }

    /**
     * Converts a <code>VectorMap</code> of parameters to a <code>HashMap</code>
     * of parameters for ease of use.
     *
     * @param queryParam The <code>VectorMap</code> to be converted
     * @return The converted <code>HashMap</code>
     */
    private HashMap<String, ArrayList<String>> getParameters(VectorMap queryParam) {
        HashMap<String, ArrayList<String>> res = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < queryParam.size(); i++) {
            String key = queryParam.getKey(i).toString().toLowerCase();
            String val = queryParam.getVal(i).toString().toLowerCase();
            String temp = "";
            if (res.containsKey(key)) {
                res.get(key).add(val);
            } else {
                ArrayList<String> value = new ArrayList<String>();
                value.add(val);
                res.put(key, value);
            }
        }
        return res;
    }

    /**
     * Formats a standarized simple where clause.
     *
     * @param column The target column
     * @param value The target value
     * @return the where clause
     */
    private StringBuilder buildWhereClause(String column, String value) {
        if (value == null) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        res.append(column);
        if (value.endsWith("*")) {
            res.append(" LIKE('");
            res.append(value.replace("*", "")).append("%')");
        } else {
            res.append("='").append(value).append("'");
        }
        return res;
    }

    /**
     * This class handles the building of a complex sql statement that matches
     * the rules provided for stereotypes.
     */
    private class SqlNode {

        /**
         * This nodes current statement
         */
        private String statement;
        /**
         * Left side of the statement if applicable
         */
        private SqlNode leftChild;
        /**
         * Right side of the statement if applicable
         */
        private SqlNode rightChild;

        /**
         * Constructor.
         *
         * @param statement initial statement
         */
        public SqlNode(String statement) {
            this.statement = statement;
        }

        /**
         * Returns current statement.
         *
         * @return current statement
         */
        public String getStatement() {
            return statement;
        }

        /**
         * Sets current statement.
         *
         * @param statement new statement
         */
        public void setStatement(String statement) {
            this.statement = statement;
        }

        /**
         * Returns left side of the statement.
         *
         * @return left child
         */
        public SqlNode getLeftChild() {
            return leftChild;
        }

        /**
         * Adds a child node, if it's the first one as the left child else as
         * the right child.
         *
         * @param child the new child
         */
        public void addChild(SqlNode child) {
            if (leftChild == null) {
                leftChild = child;
            } else if (rightChild == null) {
                rightChild = child;
            } else {
                throw new RuntimeException();
            }
        }

        /**
         * Returns right side of the statement.
         *
         * @return right child
         */
        public SqlNode getRightChild() {
            return rightChild;
        }

        /**
         * Returns wether this node has children
         *
         * @return true or false
         */
        public boolean isLeaf() {
            return leftChild == null;
        }

        /**
         * Checks if a statement is valid.
         *
         * @param str the statement to check
         * @return the result of the check (true/false)
         */
        private int evaluateString(String str) {
            int openPar = 0;
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == '(') {
                    openPar++;
                } else if (c == ')') {
                    openPar--;
                }
            }
            return openPar;
        }

        /**
         * Finds where this node's current statement should be cut into a left
         * child an unbreakable simple statement and a right child.
         *
         * @return where the statement should be cut or -1 if it cannot
         */
        private int breakStatement() {
            int openPar = 0;
            int index = 0;
            do {
                char c = statement.charAt(index);
                if (c == '(') {
                    openPar++;
                } else if (c == ')') {
                    openPar--;
                }
                index++;
            } while (openPar != 0 && index != statement.length());
            if (index == statement.length()) {
                if (openPar != 0) {
                    throw new RuntimeException();
                }
                statement = statement.substring(1, statement.length() - 1);
                return breakStatement();
            }
            if (index == 1) {
                return statement.indexOf("|");
            }
            return index;
        }

        /**
         * Break this nodes statement and then continue breaking it's childs etc
         * until we are left only with nodes with simple (unbreakable)
         * statements.
         */
        public void propagate() {
            int firstPar = statement.indexOf("(");
            int lastPar = statement.lastIndexOf(")");
            int firstBreak;
            int secondBreak;
            if (firstPar != 0) {
                firstBreak = statement.indexOf("|");
            } else if (lastPar != statement.length() - 1) {
                firstBreak = statement.indexOf("|", lastPar + 1);
            } else {
                firstBreak = breakStatement();
            }
            if (firstBreak == -1) {
                return;
            }
            secondBreak = statement.indexOf("|", firstBreak + 1);
            String left = statement.substring(0, firstBreak);
            String mid = statement.substring(firstBreak + 1, secondBreak);
            String right = statement.substring(secondBreak + 1);
            if (evaluateString(left) == 0 && evaluateString(right) == 0 && evaluateString(mid) == 0) {
                statement = mid;
                addChild(new SqlNode(left));
                addChild(new SqlNode(right));
                leftChild.propagate();
                rightChild.propagate();
            } else {
                throw new RuntimeException();
            }
        }

        /**
         * Output a nice formate string of this node and its children
         *
         * @param n how deep this node is
         * @return the resulting string
         */
        public String print(int n) {
            StringBuilder res = new StringBuilder();
            if (isLeaf()) {
                return statement;
            }
            res.append(statement).append("\t").append(leftChild.print(n + 1));
            res.append("\n");
            for (int i = 0; i <= n; i++) {
                res.append("\t");
            }
            res.append(rightChild.print(n + 1));
            return res.toString();
        }

        /**
         * Generate the sql code corresponding to this node and it's children.
         *
         * @return the resulting string sql statement
         */
        public String toSql() {
            StringBuilder res = new StringBuilder();
            if (isLeaf()) {
                String[] tokens = statement.split("[:<>]+");
                Pattern pat = Pattern.compile("[:<>]+");
                Matcher m = pat.matcher(statement);
                m.find();
                String comparison = m.group();
                comparison = comparison.replace(':', '=');
                if (comparison.equals("<>")) {
                    comparison = "!=";
                }
                if (tokens.length != 2) {
                    throw new RuntimeException(statement);
                }
                res.append("select u.user from users u join user_attributes ");
                res.append("at on u.user=at.user where at.attribute='");
                res.append(tokens[0]).append("' AND at.attribute_value");
                res.append(comparison).append(tokens[1]);
            } else {
                if (statement.equalsIgnoreCase("AND")) {
                    res.append("select a1.user from((").append(leftChild.toSql());
                    res.append(") a1 join (").append(rightChild.toSql());
                    res.append(") a2 on a1.user=a2.user)");
                } else if (statement.equalsIgnoreCase("OR")) {
                    res.append("select a1.user from(").append(leftChild.toSql());
                    res.append(") a1 left join (").append(rightChild.toSql());
                    res.append(") a2 on a1.user=a2.user UNION ALL ");
                    res.append("select a2.user from(").append(leftChild.toSql());
                    res.append(") a1 right join (").append(rightChild.toSql());
                    res.append(") a2 on a1.user=a2.user");
                } else {
                    throw new RuntimeException();
                }
            }
            return res.toString();
        }
    }

    private String getParameter(String parameter, HashMap parameters) {
        Object res = parameters.get(parameter);
        if (res == null) {
            return null;
        }
        String result;
        try {
            result = (String) ((ArrayList) res).get(0);
        } catch (Exception e) {
            return null;
        }
        return result;
    }
}
