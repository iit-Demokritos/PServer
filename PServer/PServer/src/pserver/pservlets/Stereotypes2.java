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
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return addStereotype(queryParam, respBody, dbAccess);
            }
        });
        //list stereotypes
        commands.put("liststr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return listStereotypes(queryParam, respBody, dbAccess);
            }
        });
        //get stereotypes users
        commands.put("getstrusr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return getStereotypesUsers(queryParam, respBody, dbAccess);
            }
        });
        //get stereotypes features
        commands.put("getstrftr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return getStereotypesFeatures(queryParam, respBody, dbAccess);
            }
        });
        //remake stereotype
        commands.put("rmkstr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return remakeStereotype(queryParam, respBody, dbAccess);
            }
        });
        //remove stereotype
        commands.put("remstr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //add user to stereotype
        commands.put("addusr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //get users stereotypes
        commands.put("getusrstrs", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //increase users degree of relevance with a stereotype
        commands.put("incdgr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //sets users degree of relevance with a stereotype
        commands.put("setdgr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //remove a user
        commands.put("remusr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //update a stereotypes users removing current users and adding all users
        //that comply with the stereotypes rule
        commands.put("updusrs", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //check current users and remove all that don't comply with the rule
        commands.put("chkusrs", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //add users that comply with the rule
        commands.put("fndusrs", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //add features
        commands.put("addftr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //remove features
        commands.put("remftr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //increase a feature value
        commands.put("incftr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //set a feature value
        commands.put("setftr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //remove current features and add new based on the stereotypes users
        commands.put("updftr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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

        respCode = execute(queryParam, respBody, dbAccess);

        return respCode;
    }

    public int execute(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;

        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        //if 'com' param not present, request is invalid
        if (comIdx == -1) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command does not exist");
            return respCode;    //no point in proceeding
        }
        //recognize command encoded in request
        String com = (String) queryParam.getVal(comIdx);

        if (!commands.containsKey(com)) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command not recognized");
            return respCode;
        }

        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the command
        try {
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

    private int addStereotype(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
        if (strIdx == -1) {
            WebServer.win.log.error("-Request missing str parameter");
            return PSReqWorker.REQUEST_ERR;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        String stereot = (String) queryParam.getVal(strIdx);
        int ruleIdx = queryParam.qpIndexOfKeyNoCase("rule");
        String rule = ruleIdx == -1 ? null : (String) queryParam.getVal(ruleIdx);
        //values in 'queryParam' can be empty string,
        //check if stereotype in 'str' is legal
        if (!DBAccess.legalStrName(stereot)) {
            WebServer.win.log.error("-Invalid stereotype name");
            return PSReqWorker.REQUEST_ERR;
        }
        //execute request
        String query;
        int rowsAffected = 0;
        try {
            String table = DBAccess.STEREOTYPE_TABLE;
            //insert new stereotype 
            if (ruleIdx == -1) {
                String[] columns = {DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, DBAccess.FIELD_PSCLIENT};
                String[] values = {stereot, clientName};
                rowsAffected = dbAccess.executeUpdate(buildInsertStatement(table, columns, values).toString());
            } else {
                String[] columns = {DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, DBAccess.STEREOTYPE_TABLE_FIELD_RULE, DBAccess.FIELD_PSCLIENT};
                String[] values = {stereot, rule, clientName};
                rowsAffected = dbAccess.executeUpdate(buildInsertStatement(table, columns, values).toString());
//                rowsAffected += updateStereotypesUsers(dbAccess, clientName, stereot, rule);
//                rowsAffected += updateStereotypesFeatures(dbAccess, clientName, stereot);
            }
        } catch (SQLException e) {
            WebServer.win.log.debug("-Problem inserting to DB: " + e);
            return PSReqWorker.SERVER_ERR;
        }
        StringBuilder temp = new StringBuilder("<num_of_rows>");
        temp.append(rowsAffected).append("</num_of_rows>");
        String[] resp = {temp.toString()};
        buildResponse(null, respBody, resp, dbAccess);
        WebServer.win.log.debug("-Num of rows inserted: " + rowsAffected);
        return PSReqWorker.NORMAL;
    }

    private int listStereotypes(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        
        //Gather conditions
        HashMap<String, Integer> conditions = new HashMap();
        conditions.put("clnt", REQUIRED);
        conditions.put("str", REQUIRED);
        String[] condColumns = {
            DBAccess.FIELD_PSCLIENT,
            DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE
        };
        try {
            conditions = buildConditions(queryParam, conditions, condColumns);
        } catch (Exception e) {
            WebServer.win.log.error("-Request missing parameters");
            return PSReqWorker.REQUEST_ERR;
        }
        
        int modIdx = queryParam.qpIndexOfKeyNoCase("mod");
        StringBuilder strCondition = new StringBuilder(conditions.get("str"));
        //if a mod is specified add the corresponding check
        if (modIdx != -1) {
            String mode = (String) queryParam.getVal(modIdx);
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
                strCondition.append("AND ");
                strCondition.append(DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE);
                strCondition.append(" NOT IN (SELECT DISTINCT ");
                strCondition.append(column).append(" FROM ").append(table);
                strCondition.append(" WHERE ").append(DBAccess.FIELD_PSCLIENT);
                strCondition.append("='").append(clientName).append("')");
            }
        }

        StringBuilder clntCondition = new StringBuilder();
        clntCondition.append(DBAccess.FIELD_PSCLIENT);
        clntCondition.append("='").append(clientName).append("'");

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
            query = buildSelectStatement(table, columns, where).toString();
            PServerResultSet rs = dbAccess.executeQuery(query);

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

    private int getStereotypesUsers(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //Gather conditions
        HashMap<String, Integer> conditions = new HashMap();
        conditions.put("str", REQUIRED);
        conditions.put("usr", OPTIONAL);
        conditions.put("clnt", REQUIRED);
        String[] condColumns = {
            DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE,
            DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER,
            DBAccess.FIELD_PSCLIENT
        };
        try {
            conditions = buildConditions(queryParam, conditions, condColumns);
        } catch (Exception e) {
            WebServer.win.log.error("-Request missing parameters");
            return PSReqWorker.REQUEST_ERR;
        }
        
//        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
//        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
//        if (strIdx == -1) {
//            WebServer.win.log.error("-Request missing str parameter");
//            return PSReqWorker.REQUEST_ERR;
//        }  //must exist
//
//        String stereotype = queryParam.getVal(strIdx).toString();
//
//        StringBuilder strCondition = new StringBuilder();
//        strCondition.append(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE);
//        strCondition.append("='").append(stereotype);
//        strCondition.append("') ");
//
//        //if a mod is specified add the corresponding check
//        StringBuilder usrCondition = null;
//        if (usrIdx != -1) {
//            usrCondition = new StringBuilder();
//            String usr = (String) queryParam.getVal(usrIdx);
//            String column = DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER;
//            //build condition
//            strCondition.append("AND ").append(column).append("='");
//            if (usr.contains("*")) {
//                strCondition.append(usr.replace("*", "")).append("%'");
//            } else {
//                strCondition.append(usr).append("'");
//            }
//        }
//
//        StringBuilder clntCondition = new StringBuilder();
//        clntCondition.append(DBAccess.FIELD_PSCLIENT);
//        clntCondition.append("='").append(clientName).append("'");

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
                where[0] = strCondition.toString();
                where[1] = usrCondition.toString();
                where[2] = clntCondition.toString();
            } else {
                where = new String[2];
                where[0] = strCondition.toString();
                where[1] = clntCondition.toString();
            }
            query = buildSelectStatement(table, columns, where).toString();
            PServerResultSet rs = dbAccess.executeQuery(query);

            ArrayList<String> response = new ArrayList();
            //format response body            
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                String strVal = rs.getRs().getString(columns[0]);
                String rule = rs.getRs().getString(columns[1]);
                row.append("<row><usr>").append(strVal).append("</usr><dgr>");
                row.append(rule).append("</dgr></row>\n");
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

    private int getStereotypesFeatures(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
        int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
        if (strIdx == -1) {
            WebServer.win.log.error("-Request missing str parameter");
            return PSReqWorker.REQUEST_ERR;
        }  //must exist

        String stereotype = queryParam.getVal(strIdx).toString();

        StringBuilder strCondition = new StringBuilder();
        strCondition.append(DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE);
        strCondition.append("='").append(stereotype);
        strCondition.append("') ");

        //if a mod is specified add the corresponding check
        StringBuilder ftrCondition = null;
        if (ftrIdx != -1) {
            ftrCondition = new StringBuilder();
            String usr = (String) queryParam.getVal(ftrIdx);
            String column = DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE;
            //build condition
            strCondition.append("AND ").append(column).append("='");
            if (usr.contains("*")) {
                strCondition.append(usr.replace("*", "")).append("%'");
            } else {
                strCondition.append(usr).append("'");
            }
        }

        StringBuilder clntCondition = new StringBuilder();
        clntCondition.append(DBAccess.FIELD_PSCLIENT);
        clntCondition.append("='").append(clientName).append("'");

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
                where[0] = strCondition.toString();
                where[1] = ftrCondition.toString();
                where[2] = clntCondition.toString();
            } else {
                where = new String[2];
                where[0] = strCondition.toString();
                where[1] = clntCondition.toString();
            }
            query = buildSelectStatement(table, columns, where).toString();
            PServerResultSet rs = dbAccess.executeQuery(query);

            ArrayList<String> response = new ArrayList();
            //format response body            
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                String strVal = rs.getRs().getString(columns[0]);
                String rule = rs.getRs().getString(columns[1]);
                row.append("<row><ftr>").append(strVal).append("</ftr><value>");
                row.append(rule).append("</value></row>\n");
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

    private int remakeStereotype(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
    }

    //TODO: wrap bellow methods in an object and/or move them to DBAccess
    //These might seem unnecesary but I believe they help in organizing the flow
    //and could be used all arround the program inside a wrapper class that will
    //fully utilize them. If nothing else they use StringBuilder which is way
    //faster than the String concatenation used before.
    private static StringBuilder buildInsertStatement(String table, String[] values) {
        return buildInsertStatement(table, null, values);
    }

    private static StringBuilder buildInsertStatement(String table, String[] columns, String[] values) {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(table).append(" ");
        if (columns != null) {
            query.append("(");
            for (String s : columns) {
                query.append(s).append(", ");
            }
            query.setLength(query.length() - 2);
            query.append(") ");
        }
        query.append("VALUES ('");
        for (String s : values) {
            query.append(s).append("', '");
        }
        query.setLength(query.length() - 4);
        query.append("')");
        return query;
    }

    private static StringBuilder buildSelectStatement(String table) {
        String[] tables = {table};
        return buildSelectStatement(tables, null, null, null, null, null);
    }

    private static StringBuilder buildSelectStatement(String table, String[] where) {
        String[] tables = {table};
        return buildSelectStatement(tables, null, null, null, null, where);
    }

    private static StringBuilder buildSelectStatement(String table, String[] columns, String[] where) {
        String[] tables = {table};
        return buildSelectStatement(tables, null, null, null, columns, where);
    }

    private static StringBuilder buildSelectStatement(String[] tables, String[] aliases, String[] joinTypes, String[] joins, String[] columns, String[] where) {
        StringBuilder query = new StringBuilder("SELECT ");
        if (columns == null) {
            query.append("* ");
        } else {
            for (String s : columns) {
                query.append(s).append(", ");
            }
            query.setLength(query.length() - 2);
            query.append(" ");
        }
        query.append("FROM ");
        if (tables.length != 1) {
            query.append("(").append(tables[0]).append(") as ");
            query.append(aliases[0]);
            for (int i = 0; i < tables.length - 1; i++) {
                query.append(" ").append(joinTypes[i]).append(" (");
                query.append(tables[i + 1]).append(") as ");
                query.append(aliases[i + 1]);
                query.append(" ON ").append(joins[i]).append(" ");
            }
        } else {
            query.append(tables[0]);
        }
        if (where != null) {
            query.append("WHERE ");
            for (String s : where) {
                query.append(s).append(" AND ");
            }
            query.setLength(query.length() - 5);
        }
        return query;
    }

    private StringBuilder buildUpdateStatement(String table, String[] columns, String[] values) {
        String[] tables = {table};
        return buildUpdateStatement(tables, null, null, null, columns, values, null);
    }

    private StringBuilder buildUpdateStatement(String table, String[] columns, String[] values, String[] where) {
        String[] tables = {table};
        return buildUpdateStatement(tables, null, null, null, columns, values, where);
    }

    private StringBuilder buildUpdateStatement(String[] tables, String[] aliases, String[] joinTypes, String[] joins, String[] columns, String[] values, String[] where) {
        StringBuilder query = new StringBuilder("UPDATE ");
        if (tables.length != 1) {
            query.append("(").append(tables[0]).append(") as ");
            query.append(aliases[0]);
            for (int i = 0; i < tables.length - 1; i++) {
                query.append(" ").append(joinTypes[i]).append(" (");
                query.append(tables[i + 1]).append(") as ");
                query.append(aliases[i + 1]);
                query.append(" ON ").append(joins[i]).append(" ");
            }
        } else {
            query.append(tables[0]);
        }
        query.append("SET ");
        for (int i = 0; i < columns.length; i++) {
            query.append(columns[i]).append("='").append(values[i]);
            query.append("', ");
        }
        query.setLength(query.length() - 2);
        if (where != null) {
            query.append(" WHERE ");
            for (String s : where) {
                query.append(s).append(" AND ");
            }
            query.setLength(query.length() - 5);
        }
        return query;
    }

    private StringBuilder buildDeleteStatement(String table, String where) {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(table);
        if (where != null) {
            query.append(" WHERE ").append(where);
        }
        return query;
    }

    private void buildResponse(String xslPath, StringBuffer respBody, String[] content, DBAccess dbAccess) {
        respBody.append(DBAccess.xmlHeader(xslPath));
        respBody.append("<result>\n");
        for (String s : content) {
            respBody.append("<row>").append(content).append("</row>\n");
        }
        respBody.append("</result>");
    }

    public HashMap buildConditions(VectorMap queryParam, HashMap<String,Integer> conditions, String[] columns) {
        HashMap res = new HashMap();
        int i = -1;
        for (String curKey:conditions.keySet()) {
            i++;
            int curIndex = queryParam.qpIndexOfKeyNoCase(curKey);
            if (curIndex == -1) {
                if (conditions.get(curKey) == REQUIRED) {
                    return null;
                }
                continue;
            }
            StringBuilder curCondition = new StringBuilder();
            String curValue = (String) queryParam.getVal(curIndex);
            //build condition
            curCondition.append(columns[i]);
            if (curValue.contains("*")) {
                curCondition.append(" LIKE('");
                curCondition.append(curValue.replace("*", "")).append("%')");
            } else {
                curCondition.append("='").append(curValue).append("'");
            }
            res.put(res, curCondition.toString());
        }
        return res;
    }
}