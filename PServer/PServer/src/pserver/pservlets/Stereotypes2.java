/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.pservlets;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import pserver.WebServer;
import pserver.data.DBAccess;
import pserver.data.PServerResultSet;
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
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //increase users degree of relevance with a stereotype
        commands.put("incdgr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //sets users degree of relevance with a stereotype
        commands.put("setdgr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //remove a user
        commands.put("remusr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //update a stereotypes users removing current users and adding all users
        //that comply with the stereotypes rule
        commands.put("updusrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //check current users and remove all that don't comply with the rule
        commands.put("chkusrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //add users that comply with the rule
        commands.put("fndusrs", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //add features
        commands.put("addftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //remove features
        commands.put("remftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //increase a feature value
        commands.put("incftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //set a feature value
        commands.put("setftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //remove current features and add new based on the stereotypes users
        commands.put("updftr", new PServerCommand() {
            @Override
            public int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
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
                rowsAffected += updateStereotypesUsers(queryParam, respBody, dbAccess);
                rowsAffected += updateStereotypesFeatures(queryParam, respBody, dbAccess);
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
            StringBuilder row = new StringBuilder();
            while (rs.next()) {
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
            StringBuilder row = new StringBuilder();
            while (rs.next()) {
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
        int respCode = updateStereotypesUsers(queryParam, respBody, dbAccess);
        if (respCode == PSReqWorker.NORMAL) {
            respCode = updateStereotypesFeatures(queryParam, respBody, dbAccess);
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
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        //execute request
        String query;
        int rowsAffected = 0;
        try {
            //insert each (user, stereotype, degree) in a new row in 'stereotype_users'.
            //Note that the specified stereotypes must already exist in 'stereotypes'
            for (String stereot : queryParam.keySet()) {
                if (stereotypeExists(dbAccess, stereot, clientName) == false) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " does not exists");
                    continue;
                }
                if (stereotypeHasUser(dbAccess, stereot, userName, clientName)) {
                    WebServer.win.log.debug("-Stereotype " + stereot + " already has the user " + userName);
                    continue;
                }
                String degree = queryParam.get(stereot) == null? null : queryParam.get(stereot).get(0);
                String numDegree = DBAccess.strToNumStr(degree);  //numeric version of degree                    
                updateStereotypeWithUser(dbAccess, clientName, stereot, userName, Float.parseFloat(numDegree));
                query = "insert into stereotype_users " + "(su_user, su_stereotype, su_degree, FK_psclient) values ('" + userName + "', '" + stereot + "', " + numDegree + ",'" + clientName + "')";
                rowsAffected += dbAccess.executeUpdate(query);
            }
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }

        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    private void updateStereotypeWithUser(DBAccess dbAccess, String clientName, String stereotype, String user, float degree) throws SQLException {
        String subSelect = "SELECT '" + stereotype + "',up_feature, 0,'" + clientName + "' FROM " + DBAccess.UPROFILE_TABLE
                + " WHERE up_user ='" + user + "' AND FK_psclient='" + clientName + "'";

        String sql = "INSERT IGNORE INTO " + DBAccess.STEREOTYPE_PROFILES_TABLE
                + "(" + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE
                + "," + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE
                + "," + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE
                + "," + DBAccess.FIELD_PSCLIENT + ") " + subSelect + "";
        dbAccess.executeUpdate(sql);

        sql = "UPDATE " + DBAccess.STEREOTYPE_PROFILES_TABLE + "," + DBAccess.UPROFILE_TABLE
                + " SET " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + "=" + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + "+"
                + degree + "*" + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE
                + " WHERE " + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND "
                + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "= " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE;
        dbAccess.executeUpdate(sql);
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
}