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
//                return getStereotypesUsers(queryParam, respBody, dbAccess);
                throw new UnsupportedOperationException("Not supported yet.");
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
//                return remakeStereotype(queryParam, respBody, dbAccess);
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        //remove stereotype
        commands.put("remstr", new PServerCommand() {
            @Override
            public int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
                return removeStereotype(queryParam, respBody, dbAccess);
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
                rowsAffected = dbAccess.executeUpdate(DBAccess.buildInsertStatement(table, columns, values).toString());
            } else {
                String[] columns = {DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE, DBAccess.STEREOTYPE_TABLE_FIELD_RULE, DBAccess.FIELD_PSCLIENT};
                String[] values = {stereot, rule, clientName};
                rowsAffected = dbAccess.executeUpdate(DBAccess.buildInsertStatement(table, columns, values).toString());
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
            query = DBAccess.buildSelectStatement(table, columns, where).toString();
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

//    private int getStereotypesUsers(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
//        //Gather conditions
//        HashMap<String, Integer> conditions = new HashMap();
//        conditions.put("str", REQUIRED);
//        conditions.put("usr", OPTIONAL);
//        conditions.put("clnt", REQUIRED);
//        String[] condColumns = {
//            DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE,
//            DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER,
//            DBAccess.FIELD_PSCLIENT
//        };
//        try {
//            conditions = buildConditions(queryParam, conditions, condColumns);
//        } catch (Exception e) {
//            WebServer.win.log.error("-Request missing parameters");
//            return PSReqWorker.REQUEST_ERR;
//        }
//
////        int strIdx = queryParam.qpIndexOfKeyNoCase("str");
////        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
////        if (strIdx == -1) {
////            WebServer.win.log.error("-Request missing str parameter");
////            return PSReqWorker.REQUEST_ERR;
////        }  //must exist
////
////        String stereotype = queryParam.getVal(strIdx).toString();
////
////        StringBuilder strCondition = new StringBuilder();
////        strCondition.append(DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE);
////        strCondition.append("='").append(stereotype);
////        strCondition.append("') ");
////
////        //if a mod is specified add the corresponding check
////        StringBuilder usrCondition = null;
////        if (usrIdx != -1) {
////            usrCondition = new StringBuilder();
////            String usr = (String) queryParam.getVal(usrIdx);
////            String column = DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER;
////            //build condition
////            strCondition.append("AND ").append(column).append("='");
////            if (usr.contains("*")) {
////                strCondition.append(usr.replace("*", "")).append("%'");
////            } else {
////                strCondition.append(usr).append("'");
////            }
////        }
////
////        StringBuilder clntCondition = new StringBuilder();
////        clntCondition.append(DBAccess.FIELD_PSCLIENT);
////        clntCondition.append("='").append(clientName).append("'");
//
//        //execute request
//        int rowsAffected = 0;
//        String query;
//        try {
//            //get matching records
//            String[] columns = {
//                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER,
//                DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE
//            };
//            String table = DBAccess.STEREOTYPE_USERS_TABLE;
//            String[] where;
//            if (usrCondition != null) {
//                where = new String[3];
//                where[0] = strCondition.toString();
//                where[1] = usrCondition.toString();
//                where[2] = clntCondition.toString();
//            } else {
//                where = new String[2];
//                where[0] = strCondition.toString();
//                where[1] = clntCondition.toString();
//            }
//            query = DBAccess.buildSelectStatement(table, columns, where).toString();
//            PServerResultSet rs = dbAccess.executeQuery(query);
//
//            ArrayList<String> response = new ArrayList();
//            //format response body            
//            while (rs.next()) {
//                StringBuilder row = new StringBuilder();
//                String strVal = rs.getRs().getString(columns[0]);
//                String rule = rs.getRs().getString(columns[1]);
//                row.append("<row><usr>").append(strVal).append("</usr><dgr>");
//                row.append(rule).append("</dgr></row>\n");
//                rowsAffected += 1;  //number of result rows
//                response.add(row.toString());
//            }
//            //close resultset and statement
//            rs.close();
//            String[] resp = response.toArray(new String[response.size()]);
//            buildResponse(null, respBody, resp, dbAccess);
//        } catch (SQLException e) {
//            WebServer.win.log.debug("-Problem executing query: " + e);
//            return PSReqWorker.SERVER_ERR;
//        }
//        WebServer.win.log.debug("-Num of rows returned: " + rowsAffected);
//        return PSReqWorker.NORMAL;
//    }
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

//    private int remakeStereotype(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
//    }
    private int removeStereotype(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {

        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int comIdx = queryParam.qpIndexOfKeyNoCase("com");
        int lkeIdx = queryParam.qpIndexOfKeyNoCase("lke");
        //execute request
        int success = PSReqWorker.NORMAL;
        String query;
        int rowsAffected = 0;
        try {
            String table;
            String[] where;

            StringBuilder clntCondition = new StringBuilder();
            clntCondition.append(DBAccess.FIELD_PSCLIENT);
            clntCondition.append("='").append(clientName).append("'");
            //delete specified stereotypes            
            for (int i = 0; i < qpSize; i++) {
                StringBuilder strCondition = new StringBuilder();
                strCondition.append(DBAccess.FIELD_PSCLIENT);
                strCondition.append("='").append(clientName).append("'");
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
                        success = PSReqWorker.SERVER_ERR;
                    }  //request is not valid, rollback
                }
                if (success == PSReqWorker.SERVER_ERR) {
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
            success = PSReqWorker.SERVER_ERR;
            WebServer.win.log.debug("-Problem deleting from DB: " + e);
        }
        WebServer.win.log.debug("-Num of rows deleted: " + rowsAffected);
        return success;

    }

    /**
     *
     * @param xslPath
     * @param respBody
     * @param content
     * @param dbAccess
     */
    private void buildResponse(String xslPath, StringBuffer respBody, String[] content, DBAccess dbAccess) {
        respBody.append(DBAccess.xmlHeader(xslPath));
        respBody.append("<result>\n");
        for (String s : content) {
            respBody.append("<row>").append(content).append("</row>\n");
        }
        respBody.append("</result>");
    }

    /**
     *
     * @param queryParam
     * @param conditions
     * @param columns
     * @return
     */
    public HashMap buildConditions(VectorMap queryParam, HashMap<String, Integer> conditions, String[] columns) {
        HashMap res = new HashMap();
        int i = -1;
        for (String curKey : conditions.keySet()) {
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