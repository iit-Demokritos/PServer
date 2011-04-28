package pserver.pservlets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pserver.PersServer;
import pserver.WebServer;
import pserver.algorithms.graphs.GraphClustering;
import pserver.algorithms.metrics.VectorMetric;
import pserver.data.PCommunityDBAccess;
import pserver.data.PCommunityProfileResultSet;
import pserver.data.VectorMap;
import pserver.data.DBAccess;
import pserver.data.FeatureGroupManager;
import pserver.data.PFeatureGroupDBAccess;
import pserver.data.PFeatureGroupProfileResultSet;
import pserver.data.PUserDBAccess;
import pserver.data.UserCommunityManager;
import pserver.domain.PFeatureGroup;
import pserver.domain.PUser;
import pserver.logic.PSReqWorker;

/**
 *
 * @author alexm
 */
public class Communities implements pserver.pservlets.PService {

    @Override
    public void init(String[] params) throws Exception {
    }

    @Override
    public String getMimeType() {
        return pserver.pservlets.PService.xml;
    }

    @Override
    public int service(VectorMap parameters, StringBuffer response, DBAccess dbAccess) {
        int respCode;
        VectorMap queryParam;
        StringBuffer respBody;

        respBody = new StringBuffer();
        queryParam = parameters;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        clientName = clientName.substring(0, clientName.indexOf('|'));
        queryParam.updateVal(clientName, clntIdx);

        int comIdx = parameters.qpIndexOfKeyNoCase("com");
        if (comIdx == -1) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command does not exist");
            return respCode;  //no point in proceeding
        }

        //recognize command encoded in request
        String com = (String) queryParam.getVal(comIdx);
        if (com.equalsIgnoreCase("calcudist")) {//calculetes user distances
            respCode = comCommuMakeUserDistances(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("calcftrdist")) {//calculetes feature distances
            respCode = comCommuMakeFtrDistances(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("mkcom")) {//create user communities
            respCode = comCommuMakeCommunities(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("erasecom")) {//erase user communities
            respCode = comCommuEraseCommunities(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("eraseftrgrp")) {//erase feature groups
            respCode = comCommuEraseFeatureGroups(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("mkftrgrp")) {//create feature groups
            respCode = comCommuMakeGroups(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("sqlcomp")) {//specify conditions and select assignments
            respCode = comCommuCompSql(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("sqlftrgrp")) {//specify conditions and select assignments
            respCode = comCommuFtrSql(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("mkcfprofiles")) {//specify conditions and select assignments
            respCode = comCommuMakeCollaborativeProfile(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("addftrgrp")) {//specify conditions and select assignments
            respCode = addFeatureGroup(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("rmftrgrp")) {//specify conditions and select assignments
            respCode = removeFeatureGroup(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getftrgrps")) {//specify conditions and select assignments
            respCode = getFeatureGroup(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getftrgrp")) {//specify conditions and select assignments
            respCode = getFeatureGroupFeatures(queryParam, respBody, dbAccess);
        } else {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command not recognized");
        }

        response.append(respBody.toString());
        return respCode;
    }

    private int comCommuCompSql(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
            //execute the command
            boolean success;
            success = execCommuCompSql(queryParam, respBody, dbAccess);
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

    private boolean execCommuCompSql(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int rowsAffected = 0;
        int whrIdx = queryParam.qpIndexOfKeyNoCase("whr");
        String whrCondition = "";
        if (whrIdx != -1) {
            whrCondition = DBAccess.whrPatternCondition((String) queryParam.getVal(whrIdx), clientName);
        }

        //execute request
        boolean success = true;
        try {
            //get matching user profiles
            PCommunityDBAccess mdbAccess = new PCommunityDBAccess(dbAccess);
            PCommunityProfileResultSet rs = mdbAccess.getCommunityProfiles(whrCondition);
            //format response body
            respBody.append(DBAccess.xmlHeader("/resp_xsl/community_profiles.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String community = rs.getCommunityName();
                String feature = rs.getCommunityFeatureName();
                float value = rs.getFeatureValue();
                respBody.append("<row>"
                        + "<community>" + community + "</community>"
                        + "<ftr>" + feature + "</ftr>"
                        + "<ftr_value>" + value + "</ftr_value>"
                        + "</row>\n");
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

    //-----
    //-----
    private int comCommuEraseCommunities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEraseCommunities(queryParam, respBody, dbAccess);
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

    private boolean execEraseCommunities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        boolean success = true;
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        try {
            int rowsDeleted = dbAccess.clearUserCommunities(clientName);
            respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsDeleted + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.error("-Problem inserting to DB: " + e);
        }
        return success;
    }

    //-----
    //-----
    private int comCommuEraseFeatureGroups(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEraseFeatureGroups(queryParam, respBody, dbAccess);
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

    private boolean execEraseFeatureGroups(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        boolean success = true;
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        try {
            int rowsDeleted = dbAccess.clearFeatureGroups(clientName);
            respBody.append("<result>\n");
            respBody.append("<row><num_of_rows>" + rowsDeleted + "</num_of_rows></row>\n");
            respBody.append("</result>");
        } catch (SQLException e) {
            success = false;
            WebServer.win.log.error("-Problem inserting to DB: " + e);
        }
        return success;
    }

    //-----
    //-----
    private int comCommuFtrSql(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
            //execute the command
            boolean success;
            success = execCommuFtrSql(queryParam, respBody, dbAccess);
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

    private boolean execCommuFtrSql(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        //request properties
        int qpSize = queryParam.size();
        int rowsAffected = 0;
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        int whrIdx = queryParam.qpIndexOfKeyNoCase("whr");
        String whrCondition = "";
        if (whrIdx != -1) {
            whrCondition = DBAccess.whrPatternCondition((String) queryParam.getVal(whrIdx), clientName);
        }
        //execute request
        boolean success = true;
        try {
            //get matching user profiles
            PFeatureGroupDBAccess mdbAccess = new PFeatureGroupDBAccess(dbAccess);
            PFeatureGroupProfileResultSet rs = mdbAccess.getFeatureGroupProfiles(whrCondition);
            //format response body
            respBody.append(DBAccess.xmlHeader("/resp_xsl/groups.xsl"));
            respBody.append("<result>\n");
            while (rs.next()) {
                String group = rs.getFeatureGroupName();
                String feature = rs.getFeatureGroupyFeatureName();
                respBody.append("<row>"
                        + "<group>" + group + "</group>"
                        + "<ftr>" + feature + "</ftr>"
                        + "</row>\n");
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
    //-----    

    private int comCommuMakeCommunities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execMakeCommunities(queryParam, respBody, dbAccess);
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

    private boolean execMakeCommunities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int algorithmIdx = queryParam.qpIndexOfKeyNoCase("algorithm");
        if (algorithmIdx == -1) {
            WebServer.win.log.error("-The parameter algorithm is missing: ");
            return false;
        }
        String algorithmName = (String) queryParam.getVal(algorithmIdx);

        int threasholdIdx = queryParam.qpIndexOfKeyNoCase("th");
        if (threasholdIdx == -1) {
            WebServer.win.log.error("-The parameter th is missing: ");
            return false;
        }
        String thStr = (String) queryParam.getVal(threasholdIdx);
        int op = 0;
        if (thStr.startsWith("<")) {
            op = 1;
        } else if (thStr.startsWith(">")) {
            op = 2;
        } else {
            WebServer.win.log.error("-The parameter th starts neither with < nor > ");
            return false;
        }
        float threashold = Float.parseFloat(thStr.substring(1));

        boolean success = true;

        GraphClustering algorithm = PersServer.pbeansLoadader.getGClustering().get(algorithmName);
        if (algorithm == null) {
            WebServer.win.log.error("-algorithm " + algorithm + " does not exists");
            return false;
        }

        try {
            dbAccess.clearUserCommunities(clientName);
            PCommunityDBAccess pdbAccess = new PCommunityDBAccess(dbAccess);
            pdbAccess.deleteUserAccociations(clientName, DBAccess.RELATION_BINARY_SIMILARITY);
            pdbAccess.generateBinarySimilarities(dbAccess, clientName, op, threashold);
            String sql = "SELECT * FROM " + DBAccess.UASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_BINARY_SIMILARITY;
            UserCommunityManager cManager = new UserCommunityManager(dbAccess, clientName);
            algorithm.execute(sql, cManager, dbAccess);
        } catch (SQLException ex) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + ex);
        }
        //String query1 = "SELECT uf_feature AS up_feature, uf_numdefvalue AS up_numvalue FROM up_features WHERE uf_feature NOT IN " + " ( SELECT up_feature FROM user_profiles WHERE up_user = '" + userName + "' AND FK_psclient = '" + clientName + "') AND FK_psclient = '" + clientName + "'";
        //String query2 = "SELECT up_feature, up_numvalue AS up_numvalue FROM user_profiles WHERE up_user = '" + userName + "' AND up_feature in " + "(SELECT up_feature FROM user_profiles WHERE FK_psclient='" + clientName + "' ) AND FK_psclient='" + clientName + "'";
        //String sql = " ( " + query1 + " ) UNION ( " + query2 + " ) " + condition;
        return success;
    }

    private int comCommuMakeCollaborativeProfile(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execMakeCollaborativeProfiles(queryParam, respBody, dbAccess);
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

    private boolean execMakeCollaborativeProfiles(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int threasholdIdx = queryParam.qpIndexOfKeyNoCase("th");
        if (threasholdIdx == -1) {
            WebServer.win.log.error("-The parameter th is missing: ");
            return false;
        }
        String thStr = (String) queryParam.getVal(threasholdIdx);
        int op = 0;
        boolean avtualDist = true;
        if (thStr.startsWith("<")) {
            avtualDist = false;
            op = 1;
        } else if (thStr.startsWith(">")) {
            op = 2;
        } else {
            WebServer.win.log.error("-The parameter th is starts neither with < nor > ");
            return false;
        }
        float threashold = Float.parseFloat(thStr.substring(1));

        boolean success = true;

        try {
            PCommunityDBAccess pdbAccess = new PCommunityDBAccess(dbAccess);
            pdbAccess.deleteUserAccociations(clientName, DBAccess.RELATION_BINARY_SIMILARITY);
            pdbAccess.generateBinarySimilarities(dbAccess, clientName, op, threashold);

            final int userSize = 1000;
            int offset = 0;
            int i = 0;
            int threadNum = Integer.parseInt(PersServer.pref.getPref("thread_num"));
            ///threadNum = 1;

            Statement stmt = dbAccess.getConnection().createStatement();
            String sql = "DELETE FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
            stmt.execute(sql);
            sql = "DELETE FROM " + DBAccess.CFFEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
            stmt.execute(sql);
            sql = "DELETE FROM " + DBAccess.CFFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
            stmt.execute(sql);

            threadNum = 1;
            ExecutorService threadExecutor = Executors.newFixedThreadPool(threadNum);
            do {
                i = 0;
                sql = "SELECT " + DBAccess.USER_TABLE_FIELD_USER + " FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' LIMIT " + userSize + " OFFSET " + offset;
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String user = rs.getString(1);
                    //WebServer.win.log.debug("-Creating collaborative profile for user " + user );
                    threadExecutor.execute(new CollaborativeProfileBuilderThread(dbAccess, user, clientName, avtualDist));
                    i++;
                }
                offset += userSize;
            } while (i == userSize);
            threadExecutor.shutdown();
            while (threadExecutor.isTerminated() == false) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
            }
            stmt.close();
            return success;

        } catch (SQLException ex) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + ex);
        }
        //String query1 = "SELECT uf_feature AS up_feature, uf_numdefvalue AS up_numvalue FROM up_features WHERE uf_feature NOT IN " + " ( SELECT up_feature FROM user_profiles WHERE up_user = '" + userName + "' AND FK_psclient = '" + clientName + "') AND FK_psclient = '" + clientName + "'";
        //String query2 = "SELECT up_feature, up_numvalue AS up_numvalue FROM user_profiles WHERE up_user = '" + userName + "' AND up_feature in " + "(SELECT up_feature FROM user_profiles WHERE FK_psclient='" + clientName + "' ) AND FK_psclient='" + clientName + "'";
        //String sql = " ( " + query1 + " ) UNION ( " + query2 + " ) " + condition;
        return success;
    }

    /**
     * Calculates the user distances
     * 
     * @param queryParam the query parameters
     * @param respBody the xml document
     * @param dbAccess the object that contains information about data nace connection
     * @return success or failed
     */
    private int comCommuMakeUserDistances(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execMakeUserDistances(queryParam, respBody, dbAccess);
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

    private boolean execMakeUserDistances(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int rowsAffected = 0;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int smetricIdx = queryParam.qpIndexOfKeyNoCase("smetric");
        if (smetricIdx == -1) {
            WebServer.win.log.error("-The parameter smetric is missing: ");
            return false;
        }
        String smetricName = (String) queryParam.getVal(smetricIdx);

        int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
        String features = null;
        if (ftrIdx != -1) {
            features = (String) queryParam.getVal(ftrIdx);
        }

        boolean success = true;

        VectorMetric metric = PersServer.pbeansLoadader.getVMetrics().get(smetricName);
        if (metric == null) {
            WebServer.win.log.error("-There is no metric with name: " + smetricName);
            return false;
        }

        try {
            generateDistances(dbAccess, clientName, metric, features);
            //pdbAccess.generateBinaryUserRelations( clientName, DBAccess.SIMILARITY_RELATION, DBAccess.BINARY_SIMILARITY_RELATION, threashold );
        } catch (SQLException ex) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + ex);
        }
        //String query1 = "SELECT uf_feature AS up_feature, uf_numdefvalue AS up_numvalue FROM up_features WHERE uf_feature NOT IN " + " ( SELECT up_feature FROM user_profiles WHERE up_user = '" + userName + "' AND FK_psclient = '" + clientName + "') AND FK_psclient = '" + clientName + "'";
        //String query2 = "SELECT up_feature, up_numvalue AS up_numvalue FROM user_profiles WHERE up_user = '" + userName + "' AND up_feature in " + "(SELECT up_feature FROM user_profiles WHERE FK_psclient='" + clientName + "' ) AND FK_psclient='" + clientName + "'";
        //String sql = " ( " + query1 + " ) UNION ( " + query2 + " ) " + condition;
        return success;
    }

    public void generateDistances(DBAccess dbAccess, String clientName, VectorMetric metric, String features) throws SQLException {
        PCommunityDBAccess pdbAccess = new PCommunityDBAccess(dbAccess);
        pdbAccess.deleteUserAccociations(clientName, DBAccess.RELATION_SIMILARITY);
        pdbAccess.generateUserDistances(clientName, metric, DBAccess.RELATION_SIMILARITY, Integer.parseInt(PersServer.pref.getPref("thread_num")), features);
    }

    /**
     * Calculates the feature distances
     *
     * @param queryParam the query parameters
     * @param respBody the xml document
     * @param dbAccess the object that contains information about data nace connection
     * @return success or failed
     */
    private int comCommuMakeFtrDistances(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execMakeFtrDistances(queryParam, respBody, dbAccess);
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

    private boolean execMakeFtrDistances(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int smetricIdx = queryParam.qpIndexOfKeyNoCase("smetric");
        if (smetricIdx == -1) {
            WebServer.win.log.error("-The parameter smetric is missing: ");
            return false;
        }
        String smetricName = (String) queryParam.getVal(smetricIdx);

        boolean success = true;

        VectorMetric metric = PersServer.pbeansLoadader.getVMetrics().get(smetricName);
        if (metric == null) {
            WebServer.win.log.error("-There is no metric with name: " + smetricName);
            return false;
        }

        try {
            generateFtrDistances(dbAccess, clientName, metric);
            //pdbAccess.generateBinaryUserRelations( clientName, DBAccess.SIMILARITY_RELATION, DBAccess.BINARY_SIMILARITY_RELATION, threashold );
        } catch (SQLException ex) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + ex);
        }
        //String query1 = "SELECT uf_feature AS up_feature, uf_numdefvalue AS up_numvalue FROM up_features WHERE uf_feature NOT IN " + " ( SELECT up_feature FROM user_profiles WHERE up_user = '" + userName + "' AND FK_psclient = '" + clientName + "') AND FK_psclient = '" + clientName + "'";
        //String query2 = "SELECT up_feature, up_numvalue AS up_numvalue FROM user_profiles WHERE up_user = '" + userName + "' AND up_feature in " + "(SELECT up_feature FROM user_profiles WHERE FK_psclient='" + clientName + "' ) AND FK_psclient='" + clientName + "'";
        //String sql = " ( " + query1 + " ) UNION ( " + query2 + " ) " + condition;
        return success;
    }

    public void generateFtrDistances(DBAccess dbAccess, String clientName, VectorMetric metric) throws SQLException {
        PFeatureGroupDBAccess pdbAccess = new PFeatureGroupDBAccess(dbAccess);
        pdbAccess.deleteFeatureAccociations(clientName, DBAccess.RELATION_SIMILARITY);
        pdbAccess.generateFtrDistances(clientName, metric, DBAccess.RELATION_SIMILARITY, Integer.parseInt(PersServer.pref.getPref("thread_num")));
    }
    //-----

    private int comCommuMakeGroups(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execMakeGroups(queryParam, respBody, dbAccess);
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

    private boolean execMakeGroups(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int algorithmIdx = queryParam.qpIndexOfKeyNoCase("algorithm");
        if (algorithmIdx == -1) {
            WebServer.win.log.error("-The parameter algorithm is missing: ");
            return false;
        }
        String algorithmName = (String) queryParam.getVal(algorithmIdx);

        int threasholdIdx = queryParam.qpIndexOfKeyNoCase("th");
        if (threasholdIdx == -1) {
            WebServer.win.log.error("-The parameter th is missing: ");
            return false;
        }
        String thStr = (String) queryParam.getVal(threasholdIdx);
        int op = 0;
        if (thStr.startsWith("<")) {
            op = 1;
        } else if (thStr.startsWith(">")) {
            op = 2;
        } else {
            WebServer.win.log.error("-The parameter th is starts neither with < nor > ");
            return false;
        }
        float threashold = Float.parseFloat(thStr.substring(1));

        boolean success = true;

        GraphClustering algorithm = PersServer.pbeansLoadader.getGClustering().get(algorithmName);
        if (algorithm == null) {
            WebServer.win.log.error("-algorithm " + algorithm + " does not exists");
            return false;
        }

        try {
            dbAccess.clearFeatureGroups(clientName);
            PFeatureGroupDBAccess pdbAccess = new PFeatureGroupDBAccess(dbAccess);
            pdbAccess.deleteFeatureAccociations(clientName, DBAccess.RELATION_BINARY_SIMILARITY);
            pdbAccess.generateBinarySimilarities(dbAccess, clientName, op, threashold);
            String sql = "SELECT * FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_BINARY_SIMILARITY;
            FeatureGroupManager ftrManager = new FeatureGroupManager(dbAccess, clientName);
            algorithm.execute(sql, ftrManager, dbAccess);
        } catch (SQLException ex) {
            success = false;
            WebServer.win.log.debug("-Problem executing query: " + ex);
        }
        return success;
    }

    private int addFeatureGroup(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execAddFeatureGroup(queryParam, respBody, dbAccess);
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
        } catch (Exception e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    private boolean execAddFeatureGroup(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) throws SQLException {
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);
        //System.out.println( "clnt = " + clientName )

        int nameColIdx = queryParam.qpIndexOfKeyNoCase("name");
        String ftrGroupName;
        if (nameColIdx == -1) {
            WebServer.win.log.error("Parameter name is missing");
            return false;
        }
        ftrGroupName = (String) queryParam.getVal(nameColIdx);

        int ftrColIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
        String features;
        if (ftrColIdx == -1) {
            WebServer.win.log.error("Parameter ftrs is missing");
            return false;
        }
        features = (String) queryParam.getVal(ftrColIdx);

        int usrColIdx = queryParam.qpIndexOfKeyNoCase("usrs");
        String users;
        if (usrColIdx == -1) {
            WebServer.win.log.echo("Parameter usrs is missing. Assuming NULL");
            users = null;
        } else {
            users = (String) queryParam.getVal(usrColIdx);
        }

        Connection con = dbAccess.getConnection();
        String sql = "";
        int lineNo = 0;
        PFeatureGroupDBAccess pfAccess = new PFeatureGroupDBAccess(dbAccess);
        PFeatureGroup ftrGroup = new PFeatureGroup(ftrGroupName);

        String[] ftrs = features.split("\\|");
        for (int i = 0; i < ftrs.length; i++) {
            ftrGroup.addFeature(ftrs[i].trim());
        }

        if (users != null) {
            if (users.trim().equals("") == false) {
                String[] usrs = users.split("\\|");
                for (int i = 0; i < usrs.length; i++) {
                    ftrGroup.addUser(usrs[i].trim());
                }
            }
        }

        int rows = pfAccess.addFeatureGroup(ftrGroup, clientName);

        respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
        respBody.append("<result>\n");
        respBody.append("<row><num_of_rows>").append(rows).append("</num_of_rows></row>\n");
        respBody.append("</result>");
        return true;
    }

    private int removeFeatureGroup(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execRemoveGroup(queryParam, respBody, dbAccess);
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
        } catch (Exception e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    private boolean execRemoveGroup(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) throws Exception {
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int grpIdx = queryParam.qpIndexOfKeyNoCase("grp");
        if (grpIdx == -1) {
            WebServer.win.log.error("-The parameter grp is missing: ");
            return false;
        }
        String groupName = (String) queryParam.getVal(grpIdx);

        PFeatureGroupDBAccess fgAccess = new PFeatureGroupDBAccess(dbAccess);
        int rows = fgAccess.remove(groupName, clientName);

        respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
        respBody.append("<result>\n");
        respBody.append("<row><num_of_rows>").append(rows).append("</num_of_rows></row>\n");
        respBody.append("</result>");
        return true;
    }

    private int getFeatureGroup(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execGetFeatureGroup(queryParam, respBody, dbAccess);
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
        } catch (Exception e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    private boolean execGetFeatureGroup(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) throws SQLException {
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
        if (usrIdx == -1) {
            WebServer.win.log.error("-The parameter usr is missing: ");
            return false;
        }
        String user = (String) queryParam.getVal(usrIdx);

        String nameParameter = null;
        int nameIdx = queryParam.qpIndexOfKeyNoCase("name");
        if (usrIdx != -1) {
            nameParameter = (String) queryParam.getVal(nameIdx);
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " + DBAccess.FTRGROUPSFTRS_TABLE_FIELD_GROUP + ",SUM(" + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE + ") AS val FROM " + DBAccess.UPROFILE_TABLE + "," + DBAccess.FTRGROUPSFTRS_TABLE + " WHERE " + DBAccess.UPROFILE_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='").append(clientName).append("' AND " + DBAccess.FTRGROUPSFTRS_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='").append(clientName).append("'");
        sql.append(" AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "=?");
        sql.append(" AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=" + DBAccess.FTRGROUPSFTRS_TABLE_TABLE_FIELD_FTR);

        //creates the sql
        if (nameParameter != null) {
            String[] names = nameParameter.split("|");
            if (names[0].contains("*")) {
                sql.append(" AND ( " + DBAccess.FTRGROUPSFTRS_TABLE_FIELD_GROUP + " LIKE ?");
            } else {
                sql.append(" AND ( " + DBAccess.FTRGROUPSFTRS_TABLE_FIELD_GROUP + "=?");
            }
            for (int i = 1; i < names.length; i++) {
                System.out.println(names[i]);
                if (names[i].contains("*")) {
                    names[i] = names[i].replace("*", "%");
                    sql.append(" OR " + DBAccess.FTRGROUPSFTRS_TABLE_FIELD_GROUP + " LIKE ?");
                } else {
                    sql.append(" OR " + DBAccess.FTRGROUPSFTRS_TABLE_FIELD_GROUP + "=?");
                }
            }
            sql.append(")");
        }
        sql.append(" GROUP BY " + DBAccess.FTRGROUPSFTRS_TABLE_FIELD_GROUP + " ORDER BY val");
        PreparedStatement stmt = dbAccess.getConnection().prepareStatement(sql.toString());
        stmt.setString(1, user);

        //ads the parameters to prepare statement
        if (nameParameter != null) {
            String[] names = nameParameter.split("|");
            for (int i = 0; i < names.length; i++) {
                stmt.setString(2 + i, names[i]);
            }
        }
        ResultSet rs = stmt.executeQuery();
        respBody.append(DBAccess.xmlHeader("/resp_xsl/user_feature_groups.xsl"));
        respBody.append("<result>\n");
        while (rs.next()) {
            String group = rs.getString(1);
            respBody.append("<row>"
                    + "<group>" + group + "</group>"
                    + "</row>\n");
        }
        respBody.append("</result>");
        rs.close();
        stmt.close();
        return true;
    }

    private int getFeatureGroupFeatures(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execGetFeatureGroupFeatures(queryParam, respBody, dbAccess);
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
        } catch (Exception e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    private boolean execGetFeatureGroupFeatures(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) throws SQLException {
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int grpIdx = queryParam.qpIndexOfKeyNoCase("ftrgrp");
        if (grpIdx == -1) {
            WebServer.win.log.error("-The parameter ftrgrp is missing: ");
            return false;
        }
        String group = (String) queryParam.getVal(grpIdx);


        StringBuilder sql = new StringBuilder();

        PreparedStatement stmt = dbAccess.getConnection().prepareStatement(sql.toString());
        stmt.setString(1, group);

        ResultSet rs = stmt.executeQuery();
        respBody.append(DBAccess.xmlHeader("/resp_xsl/group_features.xsl"));
        respBody.append("<result>\n");
        while (rs.next()) {
            String feature = rs.getString(1);
            respBody.append("<row>"
                    + "<feature>" + feature + "</feature>"
                    + "</row>\n");
        }
        respBody.append("</result>");
        rs.close();
        stmt.close();
        return true;
    }
}

class CollaborativeProfileBuilderThread extends Thread {

    private String user;
    private String clientName;
    private DBAccess dbAccess;
    private boolean actualDist;

    public CollaborativeProfileBuilderThread(DBAccess dbAccess, String user, String clientName, boolean actualDist) {
        this.user = user;
        this.clientName = clientName;
        this.dbAccess = dbAccess;
        this.actualDist = actualDist;
    }

    @Override
    public void run() {
        try {
            PUserDBAccess pdbAccess = new PUserDBAccess(dbAccess);
            Statement stmt;
            stmt = dbAccess.getConnection().createStatement();

            PUser collaborativeProfile = pdbAccess.getUserProfile(user, null, clientName);

            Map<String, Float> ftrFreqa = collaborativeProfile.getFtrReqs();
            Map<String, Float> ftrVals = collaborativeProfile.getProfile();
            HashMap<String, Float> ftrSum = new HashMap<String, Float>();

            Map<Set<String>, Float> assocFreqa = collaborativeProfile.getFtrAssocs();
            Map<Set<String>, Float> assocSum = new HashMap<Set<String>, Float>();

            for (String ftr : ftrVals.keySet()) {
                ftrSum.put(ftr, Float.valueOf(1));
            }

            for (Set<String> ftrSet : assocFreqa.keySet()) {
                assocSum.put(ftrSet, Float.valueOf(1));
            }

            //System.out.println( assocFreqa.size() + " " + assocSum.size() );

            String sql = "SELECT * FROM " + DBAccess.UASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UASSOCIATIONS_TABLE_FIELD_SRC + "='" + user + "' AND " + DBAccess.UASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_BINARY_SIMILARITY;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String otherUser = rs.getString(DBAccess.UASSOCIATIONS_TABLE_FIELD_DST);
                float dist = rs.getFloat(DBAccess.UASSOCIATIONS_TABLE_FIELD_WEIGHT);
                PUser otherUserProfile = pdbAccess.getUserProfile(otherUser, null, clientName);
                updateCollaborativeProfile(otherUserProfile, dist, assocFreqa, ftrFreqa, ftrVals, ftrSum, assocSum);
            }
            rs.close();

            sql = "SELECT * FROM " + DBAccess.UASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UASSOCIATIONS_TABLE_FIELD_DST + "='" + user + "' AND " + DBAccess.UASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_BINARY_SIMILARITY;
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String otherUser = rs.getString(DBAccess.UASSOCIATIONS_TABLE_FIELD_SRC);
                float dist = rs.getFloat(DBAccess.UASSOCIATIONS_TABLE_FIELD_WEIGHT);
                PUser otherUserProfile = pdbAccess.getUserProfile(otherUser, null, clientName);
                updateCollaborativeProfile(otherUserProfile, dist, assocFreqa, ftrFreqa, ftrVals, ftrSum, assocSum);
            }
            rs.close();

            //if (ftrVals.size() != ftrFreqa.size()) {
            //    System.out.println(" sizes " + ftrVals.size() + " " + ftrFreqa.size());
            //    System.exit(- 1);
            //}

            sql = "INSERT DELAYED INTO " + DBAccess.CFPROFILE_TABLE + " VALUES (?,?,?,?,'" + clientName + "')";
            PreparedStatement cfFtrStmt = dbAccess.getConnection().prepareStatement(sql);
            cfFtrStmt.setString(1, this.user);
            Set<Entry<String, Float>> entries = ftrVals.entrySet();
            for (Iterator<Entry<String, Float>> it = entries.iterator(); it.hasNext();) {
                Map.Entry<String, Float> entry = it.next();
                String feature = entry.getKey();
                float val = entry.getValue() / ftrSum.get(feature);
                cfFtrStmt.setString(2, feature);
                cfFtrStmt.setString(3, val + "");
                cfFtrStmt.setFloat(4, val);
                cfFtrStmt.addBatch();
                //System.out.println( cfFtrStmt.toString() );
            }
            cfFtrStmt.executeBatch();
            cfFtrStmt.close();

            sql = "INSERT DELAYED INTO " + DBAccess.CFFEATURE_STATISTICS_TABLE + " VALUES (?,?," + DBAccess.STATISTICS_FREQUENCY + ",?,'" + clientName + "')";
            PreparedStatement cfFtrfrStmt = dbAccess.getConnection().prepareStatement(sql);
            cfFtrfrStmt.setString(1, this.user);
            entries = ftrFreqa.entrySet();
            for (Iterator<Entry<String, Float>> it = entries.iterator(); it.hasNext();) {
                Map.Entry<String, Float> entry = it.next();
                String feature = entry.getKey();
                float val = entry.getValue() / ftrSum.get(feature);
                cfFtrfrStmt.setString(2, feature);
                cfFtrfrStmt.setFloat(3, val);
                cfFtrfrStmt.addBatch();
            }
            cfFtrfrStmt.executeBatch();
            cfFtrfrStmt.close();

            sql = "INSERT DELAYED INTO " + DBAccess.CFFTRASSOCIATIONS_TABLE + " VALUES (?,?,?," + DBAccess.RELATION_SIMILARITY + ",?,'" + clientName + "')";
            PreparedStatement cfAssocFtrStmt = dbAccess.getConnection().prepareStatement(sql);
            cfAssocFtrStmt.setString(4, this.user);
            Set<Entry<Set<String>, Float>> asentries = assocFreqa.entrySet();
            //System.out.println( assocFreqa.size() + " " + assocSum.size() );
            for (Iterator<Entry<Set<String>, Float>> it = asentries.iterator(); it.hasNext();) {
                Map.Entry<Set<String>, Float> entry = it.next();
                Set<String> features = entry.getKey();
                float val = entry.getValue() / assocSum.get(features);
                Iterator<String> ftrI = features.iterator();
                cfAssocFtrStmt.setString(1, ftrI.next());
                cfAssocFtrStmt.setString(2, ftrI.next());
                cfAssocFtrStmt.setFloat(3, val);
                cfAssocFtrStmt.addBatch();
            }
            cfAssocFtrStmt.executeBatch();
            cfAssocFtrStmt.close();
            stmt.close();

        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            System.exit(0);
        }
        WebServer.win.log.echo("Processing for " + user + " Collaborative profile completed");
    }

    private void updateCollaborativeProfile(PUser otherUserProfile, float dist, Map<Set<String>, Float> assocFreqa,
            Map<String, Float> ftrFreqa, Map<String, Float> ftrVals, Map<String, Float> ftrSum, Map<Set<String>, Float> assocSum) {
        Map<String, Float> profile = otherUserProfile.getProfile();
        Set<Entry<String, Float>> entries = profile.entrySet();
        for (Iterator<Entry<String, Float>> it = entries.iterator(); it.hasNext();) {
            Map.Entry<String, Float> entry = it.next();
            String feature = entry.getKey();
            float val = entry.getValue() * dist;
            if (ftrVals.get(feature) == null) {
                ftrVals.put(feature, val);
                ftrSum.put(feature, dist);
            } else {
                ftrVals.put(feature, ftrVals.get(feature) + val);
                ftrSum.put(feature, ftrSum.get(feature) + dist);
            }
        }

        Map<String, Float> frequesncies = otherUserProfile.getFtrReqs();
        entries = frequesncies.entrySet();
        for (Iterator<Entry<String, Float>> it = entries.iterator(); it.hasNext();) {
            Map.Entry<String, Float> entry = it.next();
            String feature = entry.getKey();
            float val = entry.getValue() * dist;
            if (ftrFreqa.get(feature) == null) {
                ftrFreqa.put(feature, val);
            } else {
                ftrFreqa.put(feature, ftrFreqa.get(feature) + val);
            }
        }

        Map<Set<String>, Float> assocs = otherUserProfile.getFtrAssocs();
        Set<Entry<Set<String>, Float>> aEntries = assocs.entrySet();
        for (Iterator<Entry<Set<String>, Float>> it = aEntries.iterator(); it.hasNext();) {
            Map.Entry<Set<String>, Float> entry = it.next();
            Set<String> features = entry.getKey();
            float val = entry.getValue() * dist;
            if (assocFreqa.get(features) == null) {
                assocFreqa.put(features, val);
                assocSum.put(features, Float.valueOf(1));
            } else {
                assocFreqa.put(features, assocFreqa.get(features) + val);
                assocSum.put(features, assocSum.get(features) + dist);
            }
        }
    }
}
