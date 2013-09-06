/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.pservlets;

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
 *
 * @author scify
 */
public class Stereotypes2 implements pserver.pservlets.PService {

    public static final int REQUIRED = 0;
    public static final int OPTIONAL = 1;
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
     * Overridden method of init from {@link PService} Does nothing here.
     *
     * @param params An array of strings containing the parameters
     * @throws Exception Default Exception is thrown.
     */
    @Override
    public void init(String[] params) throws Exception {
        //initialize commands
        commands = new HashMap();
        //add stereotype
        commands.put("addstr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return addStereotype(queryParam, respBody, dbAccess);
            }
        });
        //list stereotypes
        commands.put("liststr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return listStereotypes(queryParam, respBody, dbAccess);
            }
        });
        //get stereotypes users
        commands.put("getstrusr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return getStereotypesUsers(queryParam, respBody, dbAccess);
            }
        });
        //get stereotypes features
        commands.put("getstrftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return getStereotypesFeatures(queryParam, respBody, dbAccess);
            }
        });
        //remake stereotype
        commands.put("rmkstr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return remakeStereotype(queryParam, respBody, dbAccess);
            }
        });
        //remove stereotype
        commands.put("remstr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return removeStereotype(queryParam, respBody, dbAccess);
            }
        });
        //add user to stereotype
        commands.put("addusr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return addUser(queryParam, respBody, dbAccess);
            }
        });
        //get users stereotypes
        commands.put("getusrstrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return getUsersStereotypes(queryParam, respBody, dbAccess);
            }
        });
        //increase users degree of relevance with a stereotype
        commands.put("incdgr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return increaseUsersDegree(queryParam, respBody, dbAccess);
            }
        });
        //sets users degree of relevance with a stereotype
        commands.put("setdgr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return setUsersDegree(queryParam, respBody, dbAccess);
            }
        });
        //remove a user
        commands.put("remusr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return removeUser(queryParam, respBody, dbAccess);
            }
        });
        //update a stereotypes users removing current users and adding all users
        //that comply with the stereotypes rule
        commands.put("updusrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return updateUsers(queryParam, respBody, dbAccess);
            }
        });
        //check current users and remove all that don't comply with the rule
        commands.put("chkusrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return checkUsers(queryParam, respBody, dbAccess);
            }
        });
        //add users that comply with the rule
        commands.put("fndusrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return findUsers(queryParam, respBody, dbAccess);
            }
        });
        //add features
        commands.put("addftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return addFeatures(queryParam, respBody, dbAccess);
            }
        });
        //remove features
        commands.put("remftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return removeFeatures(queryParam, respBody, dbAccess);
            }
        });
        //increase a feature value
        commands.put("incftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return increaseFeaturesValue(queryParam, respBody, dbAccess);
            }
        });
        //set a feature value
        commands.put("setftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return setFeaturesValue(queryParam, respBody, dbAccess);
            }
        });
        //remove current features and add new based on the stereotypes users
        commands.put("updftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return updateFeatures(queryParam, respBody, dbAccess);
            }
        });
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
        HashMap<String, ArrayList<String>> queryParam = getParameters(parameters);

        StringBuffer respBody = response;

        if (!ClientCredentialsChecker.check(dbAccess, parameters)) {
            return PSReqWorker.REQUEST_ERR;  //no point in proceeding
        }

        ArrayList<String> clientName = queryParam.get("clnt");
        clientName.add(0, clientName.get(0).substring(0, clientName.get(0).indexOf('|')));
        clientName.remove(1);

        respCode = execute(queryParam, respBody, dbAccess);
        return respCode;
    }

    public int execute(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;

        String com = queryParam.get("com") == null ? null : queryParam.get("com").get(0);
        queryParam.remove("com");
        //if 'com' param not present, or not recognized request is invalid
        if (com == null) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command does not exist");
            return respCode;    //no point in proceeding
        } else if (!commands.containsKey(com)) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command not recognized");
            return respCode;
        }
        //execute the command
        try {
            //first connect to DB
            dbAccess.connect();
            dbAccess.setAutoCommit(false);//transaction guarantees integrity
            //-start transaction body
            respCode = commands.get(com).runCommand(queryParam, respBody, dbAccess);
            //-end transaction body
            if (respCode == PSReqWorker.NORMAL) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        } finally {
            try {
                //disconnect from DB anyway
                dbAccess.disconnect();
            } catch (SQLException e) {
                respCode = PSReqWorker.SERVER_ERR;
                WebServer.win.log.error("-DB Transaction problem: " + e);
            }
        }
        return respCode;
    }

    private int addStereotype(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = queryParam.get("str") == null ? null : queryParam.get("str").get(0);
        String rule = queryParam.get("rule") == null ? null : queryParam.get("rule").get(0);
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

    private int listStereotypes(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = queryParam.get("str") == null ? null : queryParam.get("str").get(0);
        String mode = queryParam.get("mod") == null ? null : queryParam.get("mod").get(0);
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
            ArrayList<String> response = new ArrayList();
            //format response body        
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                String strVal = rs.getRs().getString(columns[0]);
                String rule = rs.getRs().getString(columns[1]);
                row.append("<row><str>").append(strVal).append("</str><rule>");
                row.append(rule).append("</rule></row>\n");
                rowsAffected += 1;  //number of result rows
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

    private int getStereotypesUsers(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //Gather conditions
        String clientName = queryParam.get("clnt").get(0);
        String stereot = queryParam.get("str") == null ? null : queryParam.get("str").get(0);
        String users = queryParam.get("usr") == null ? null : queryParam.get("usr").get(0);
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
            ArrayList<String> response = new ArrayList();
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                String strVal = rs.getRs().getString(columns[0]);
                String rule = rs.getRs().getString(columns[1]);
                row.append("<row><usr>").append(strVal).append("</usr><dgr>");
                row.append(rule).append("</dgr></row>\n");
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

    private int getStereotypesFeatures(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = queryParam.get("str") == null ? null : queryParam.get("str").get(0);
        String features = queryParam.get("ftr") == null ? null : queryParam.get("ftr").get(0);
        if (stereot == null) {
            WebServer.win.log.error("-Request missing str parameter");
            return PSReqWorker.REQUEST_ERR;
        } else if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }

        StringBuilder strCondition = buildWhereClause(
                DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE, stereot);
        StringBuilder clntCondition = buildWhereClause(
                DBAccess.FIELD_PSCLIENT, clientName);
        StringBuilder ftrCondition = buildWhereClause(
                DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE, features);

        //execute request
        int rowsAffected = 0;
        String query;
        try {
            //get matching records
            String[] columns = {
                DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE,
                DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_VALUE
            };
            String table = DBAccess.STEREOTYPE_STATISTICS_TABLE;
            String[] where;
            if (ftrCondition != null) {
                where = new String[3];
                where[2] = ftrCondition.toString();
            } else {
                where = new String[2];
            }
            where[0] = strCondition.toString();
            where[1] = clntCondition.toString();
            query = DBAccess.buildSelectStatement(table, columns, where).toString();
            PServerResultSet rs = dbAccess.executeQuery(query);

            ArrayList<String> response = new ArrayList();
            //format response body         
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                String strVal = rs.getRs().getString(columns[0]);
                String rule = rs.getRs().getString(columns[1]);
                row.append("<row><ftr>").append(strVal).append("</ftr><value>");
                row.append(rule).append("</value></row>\n");
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

    private int remakeStereotype(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = updateUsers(queryParam, respBody, dbAccess);
        if (respCode == PSReqWorker.NORMAL) {
            respCode = updateFeatures(queryParam, respBody, dbAccess);
        }
        return respCode;
    }

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
            }
            //delete specified stereotypes
            for (String str : stereot) {
                for (int i = 0; i < tables.length; i++) {
                    StringBuilder strCondition = buildWhereClause(
                            stereotype_columns[i], str);
                    String[] where = {strCondition.toString(), clntCondition.toString()};
                    rowsAffected += dbAccess.executeUpdate(DBAccess.buildDeleteStatement(tables[i], where).toString());
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

    private int addUser(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String userName = queryParam.get("usr") == null ? null : queryParam.get("usr").get(0);
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
                String degree = queryParam.get(stereot) == null ? null : queryParam.get(stereot).get(0);
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
        query.insert(6, " IGNORE");
        query.delete(query.indexOf("VALUES"), query.indexOf("'", query.indexOf("VALUES")));
        query.setLength(query.length() - 2);
        res += dbAccess.executeUpdate(query.toString());

        //TODO fix this
        String sql = "UPDATE " + DBAccess.STEREOTYPE_PROFILES_TABLE + "," + DBAccess.UPROFILE_TABLE
                + " SET " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + "=" + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + "+"
                + degree + "*" + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE
                + " WHERE " + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND "
                + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "= " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE;
        res += dbAccess.executeUpdate(sql);
        return res;
    }

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

    private int getUsersStereotypes(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String user = queryParam.get("usr") == null ? null : queryParam.get("usr").get(0);
        String stereot = queryParam.get("str") == null ? null : queryParam.get("str").get(0);
        String numberOfRes = queryParam.get("num") == null ? "*" : queryParam.get("num").get(0);
        String sortOrder = queryParam.get("srt") == null ? "desc" : queryParam.get("srt").get(0);
        if (user == null) {
            WebServer.win.log.error("-Missing user name");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalUsrName(user)) {
            WebServer.win.log.error("-Invalid user name");
            return PSReqWorker.REQUEST_ERR;
        }
        if (stereot == null) {
            WebServer.win.log.error("-Missing stereotype");
            return PSReqWorker.REQUEST_ERR;
        }
        if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
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
            String[] columns = {DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE,
                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE,};
            String[] where = {usrCondition, clntCondition};
            query = DBAccess.buildSelectStatement(table, columns, where);
            query.append(" order by ");
            query.append(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE);
            query.append(srtCondition).append(", ").append(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE);
            PServerResultSet rs = dbAccess.executeQuery(query.toString());
            //format response body
            //select first rows as specified by query parameter 'num'
            ArrayList<String> response = new ArrayList();
            //format response body            
            while (rowsAffected < limit && rs.next()) {
                StringBuilder row = new StringBuilder();
                String stereotVal = rs.getRs().getString(columns[0]);  //cannot be null
                String degreeVal = (new Double(rs.getRs().getDouble(columns[1]))).toString();
                if (rs.getRs().wasNull()) {
                    degreeVal = "";
                }
                row.append("<row><str>").append(stereotVal);
                row.append("</str><deg>").append(degreeVal);
                row.append("</deg></row>\n");
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

    private int increaseUsersDegree(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String user = queryParam.get("usr") == null ? null : queryParam.get("usr").get(0);
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

    private int setUsersDegree(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String user = queryParam.get("usr") == null ? null : queryParam.get("usr").get(0);
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
    
    private int updateUsers(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = checkUsers(queryParam, respBody, dbAccess);
        if (respCode == PSReqWorker.NORMAL) {
            respCode = findUsers(queryParam, respBody, dbAccess);
        }
        return respCode;
    }
    
    private int checkUsers(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //TODO implementation
        int respCode = PSReqWorker.NORMAL;
        return respCode;
    }
    
    private int findUsers(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //TODO implementation
        return PSReqWorker.SERVER_ERR;
    }
    
    private int addFeatures(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        String clientName = queryParam.get("clnt").get(0);
        String stereot = queryParam.get("str") == null ? null : queryParam.get("str").get(0);
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
        String query;
        int rowsAffected = 0;
        try {
            for (String ftr : queryParam.keySet()) {
                String newValue = queryParam.get(ftr).get(0);
                String ftrCondition = DBAccess.ftrPatternCondition(ftr);
                //TODO complete implementation
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
    
    private int removeFeatures(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        String clientName = queryParam.get("clnt").get(0);
        String stereot = queryParam.get("lke") == null ? null : queryParam.get("lke").get(0);
        //TODO continue implementation
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
                        query = "delete from stereotype_users where su_stereotype='" + stereot + "' and FK_psclient='" + clientName + "' ";
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
                query = "delete from stereotype_users where su_stereotype like '" + stereotPattern + "%' and FK_psclient='" + clientName + "' ";
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
                query = "delete from stereotype_attributes where FK_psclient='" + clientName + "' ";
                rowsAffected += dbAccess.executeUpdate(query);
                query = "delete from stereotype_users where FK_psclient='" + clientName + "' ";
                rowsAffected += dbAccess.executeUpdate(query);
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
    
    private int increaseFeaturesValue(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //TODO implement
        return PSReqWorker.SERVER_ERR;
    }
    
    private int setFeaturesValue(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //TODO implement
        return PSReqWorker.SERVER_ERR;
    }
    
    private int updateFeatures(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //TODO implement
        return PSReqWorker.SERVER_ERR;
    }
    
    private void buildResponse(String xslPath, StringBuffer respBody, String[] content, DBAccess dbAccess) {
        respBody.append(DBAccess.xmlHeader(xslPath));
        respBody.append("<result>\n");
        for (String s : content) {
            respBody.append("<row>").append(content).append("</row>\n");
        }
        respBody.append("</result>");
    }

    private HashMap<String, ArrayList<String>> getParameters(VectorMap queryParam) {
        HashMap<String, ArrayList<String>> res = new HashMap();
        for (int i = 0; i < queryParam.size(); i++) {
            String key = queryParam.getKey(i).toString().toLowerCase();
            String val = queryParam.getVal(i).toString().toLowerCase();
            String temp = "";
            if (res.containsKey(key)) {
                res.get(key).add(val);
            } else {
                ArrayList<String> value = new ArrayList();
                value.add(val);
                res.put(key, value);
            }
        }
        return res;
    }

    private StringBuilder buildWhereClause(String column, String value) {
        if (value == null) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        res.append(column);
        if (value.contains("*")) {
            res.append(" LIKE('");
            res.append(value.replace("*", "")).append("%')");
        } else {
            res.append("='").append(value).append("'");
        }
        return res;
    }
    
    private class SqlNode {

        private String statement;
        private SqlNode leftChild;
        private SqlNode rightChild;

        public SqlNode(String statement) {
            this.statement = statement;
        }

        public String getStatement() {
            return statement;
        }

        public void setStatement(String statement) {
            this.statement = statement;
        }

        public SqlNode getLeftChild() {
            return leftChild;
        }

        public void addChild(SqlNode child) {
            if (leftChild == null) {
                leftChild = child;
            } else if (rightChild == null) {
                rightChild = child;
            } else {
                throw new RuntimeException();
            }
        }

        public SqlNode getRightChild() {
            return rightChild;
        }

        public boolean isLeaf() {
            return leftChild == null;
        }

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
                res.append(comparison).append("'").append(tokens[1]);
                res.append("'");
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
}