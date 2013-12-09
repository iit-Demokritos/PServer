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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import pserver.WebServer;
import pserver.data.DBAccess;
import pserver.data.VectorMap;
import pserver.logic.PSReqWorker;
import pserver.utilities.ClientCredentialsChecker;

/**
 *
 * @author alexm
 */
public class Spread implements pserver.pservlets.PService {

    private float pCorrects = 0;
    private float cCorrects = 0;
    private float sCorrects = 0;
    private float cCorrects2 = 0;
    private float sCorrects2 = 0;
    private float all2 = 0;
    private float all = 0;
    private float pParam = 0.46447277f;
    private float cParam = 0.18148465f;
    private float sParam = 0.35405377f;
    private float cParam2 = 0.47481096f;
    private float sParam2 = 1.0f;
    private TreeMap<String, TreeMap<String, TreeMap<String, Float>>> profiles;
    private TreeMap<String, TreeMap<String, LinkedList<String>>> accocs;

    /**
     *
     * @return
     */
    @Override
    public String getMimeType() {
        return pserver.pservlets.PService.xml;
    }

    /**
     *
     * @param params
     * @throws Exception
     */
    @Override
    public void init(String[] params) throws Exception {
        profiles = new TreeMap<String, TreeMap<String, TreeMap<String, Float>>>();
        accocs = new TreeMap<String, TreeMap<String, LinkedList<String>>>();
    }

    /**
     *
     * @param parameters
     * @param response
     * @param dbAccess
     * @return
     */
    @Override
    public int service(VectorMap parameters, StringBuffer response, DBAccess dbAccess) {
        int respCode;
        VectorMap queryParam;
        StringBuffer respBody;

        respBody = new StringBuffer();
        queryParam = parameters;

        if (!ClientCredentialsChecker.check(dbAccess, queryParam)) {
            return PSReqWorker.REQUEST_ERR;  //no point in proceeding
        }

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
        if (com.equalsIgnoreCase("estmt")) {
            respCode = estimateFtr(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtpfr")) {
            respCode = estimateUsingFeatureFrequencies(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtpfrSim")) {
            respCode = estimateUsingFeatureSimilarities(queryParam, respBody, dbAccess);

        } else if (com.equalsIgnoreCase("estmtster")) {
            respCode = estimateByStereotypes(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtpfrster")) {
            respCode = estimateBasedOnStersUsingFrequencies(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtpfrsimster")) {
            respCode = estimateBasedOnStersUsingFeatureSimilarities(queryParam, respBody, dbAccess);

        } else if (com.equalsIgnoreCase("estmtusrster")) {
            respCode = estimateCombineUserAndStereotypes(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtusersterfr")) {
            respCode = estimateCombineUserAndStereotypesUsingFrequencies(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtusersterfrsim")) {
            respCode = estimateCombineUserAndStereotypesUsingFeatureSimilarities(queryParam, respBody, dbAccess);

        } else if (com.equalsIgnoreCase("estmtcf")) {
            respCode = estimateByCollaborativeFiltering(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtpfrcf")) {
            respCode = estimateBasedOnCfUsingFrequencies(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtpfrsimcf")) {
            respCode = estimateBasedOnCfUsingFeatureSimilarities(queryParam, respBody, dbAccess);

        } else if (com.equalsIgnoreCase("estmtusrcf")) {
            respCode = estimateCombineUserAndCf(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtusercffr")) {
            respCode = estimateCombineAndCfUsingFrequencies(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtusercffrsim")) {
            respCode = estimateCombineUserAndCfUsingFeatureSimilarities(queryParam, respBody, dbAccess);

        } else if (com.equalsIgnoreCase("estmtusercfster")) {
            respCode = estimateCombineUserCfAndStereotypes(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtusercfsterfr")) {
            respCode = estimateCombineUserCfAndStereotypesUsingFrequencies(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("estmtusercfsterfrsim")) {
            respCode = estimateCombineUserCfAndStereotypesUsingFeatureSimilarities(queryParam, respBody, dbAccess);

        } else if (com.equalsIgnoreCase("mkftrsim")) {
            respCode = createFtrSimilarities(queryParam, respBody, dbAccess);

        } else if (com.equalsIgnoreCase("commoncf")) {
            respCode = estimateCommoncf(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("commoncfster")) {
            respCode = estimateByStereotypesClassicCf(queryParam, respBody, dbAccess);

        } else if (com.equalsIgnoreCase("super")) {
            respCode = estimateSuper(queryParam, respBody, dbAccess);


        } else {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command not recognized");
        }

        response.append(respBody.toString());
        return respCode;
    }

    private int estimateFtr(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimate(queryParam, respBody, dbAccess);
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

    private boolean execEstimate(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");
            String sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectFtr.setString(1, feature);
                    //rs = dbAccess.executeQuery(sql).getRs();
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        weight += rs.getFloat(1) * coFeatureWeights.get(j);
                        sum += coFeatureWeights.get(j);
                    }
                    rs.close();
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateUsingFeatureFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateUsingFeatureFrequencies(queryParam, respBody, dbAccess);
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

    private boolean execEstimateUsingFeatureFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectFtr.setString(1, feature);
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectFtrFreq.setString(1, feature);
                        rs = selectFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);

                        weight += val * freq * coFeatureWeights.get(j);
                        sum += freq * coFeatureWeights.get(j);
                    } else {
                        rs.close();
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            selectFtrFreq.close();
            assocs.close();

            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateUsingFeatureSimilarities(queryParam, respBody, dbAccess);
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

    private boolean execEstimateUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement userAssocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectFtr.setString(1, feature);
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectFtrFreq.setString(1, feature);
                        rs = selectFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);
                        rs.close();

                        userAssocs.setString(1, feature);
                        userAssocs.setString(2, feature);
                        rs = userAssocs.executeQuery();
                        float ftrSimilarity = 1.0f;
                        while (rs.next()) {
                            if (coFeatures.contains(rs.getString(1)) == false) {
                                continue;
                            }
                            ftrSimilarity += rs.getFloat(2) / freq;
                        }
                        rs.close();
                        //System.out.println( ftrSimilarity );
                        weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                        sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                    } else {
                        rs.close();
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            selectFtrFreq.close();
            assocs.close();
            userAssocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateByStereotypes(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateByStereotypes(queryParam, respBody, dbAccess);
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

    private boolean execEstimateByStereotypes(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectFtr.setString(1, feature);
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        weight += rs.getFloat(1) * coFeatureWeights.get(j);
                        sum += coFeatureWeights.get(j);
                    }
                    rs.close();
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateByStereotypesClassicCf(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateByStereotypesClassicCf(queryParam, respBody, dbAccess);
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

    private boolean execEstimateByStereotypesClassicCf(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_FEATURE_SIMILARITY;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_FEATURE_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectFtr.setString(1, feature);
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        weight += rs.getFloat(1) * coFeatureWeights.get(j);
                        sum += coFeatureWeights.get(j);
                    }
                    rs.close();
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateBasedOnStersUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateBasedOnStersUsingFrequencies(queryParam, respBody, dbAccess);
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

    private boolean execEstimateBasedOnStersUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.STEREOTYPE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectFtr.setString(1, feature);
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectFtrFreq.setString(1, feature);
                        //System.out.println( selectFtrFreq.toString() );
                        rs = selectFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);
                        weight += val * freq * coFeatureWeights.get(j);
                        sum += freq * coFeatureWeights.get(j);
                        rs.close();
                    } else {
                        rs.close();
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            selectFtrFreq.close();
            assocs.close();

            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateBasedOnStersUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execEstimateBasedOnStersUsingFeatureSimilarities(queryParam, respBody, dbAccess);
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

    private boolean execEstimateBasedOnStersUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.STEREOTYPE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.SFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.SFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement sterAssocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {
                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    //System.out.println( j );
                    selectFtr.setString(1, feature);
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectFtrFreq.setString(1, feature);
                        rs = selectFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);
                        rs.close();

                        sterAssocs.setString(1, feature);
                        sterAssocs.setString(2, feature);
                        rs = sterAssocs.executeQuery();
                        float ftrSimilarity = 1.0f;
                        while (rs.next()) {
                            if (coFeatures.contains(rs.getString(1)) == false) {
                                continue;
                            }
                            ftrSimilarity += rs.getFloat(2) / freq;
                        }
                        rs.close();
                        //System.out.println( ftrSimilarity );
                        weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                        sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                    } else {
                        rs.close();
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            selectFtrFreq.close();
            assocs.close();
            sterAssocs.close();
            respBody.append("</result>");
            //System.out.println( respBody.toString() );
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCombineUserAndStereotypes(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCombineUserAndStereotypes(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCombineUserAndStereotypes(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        weight += rs.getFloat(1) * coFeatureWeights.get(j);
                        sum += coFeatureWeights.get(j);
                        rs.close();
                    } else {
                        rs.close();
                        selectFtr.setString(1, feature);
                        rs = selectFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            weight += rs.getFloat(1) * coFeatureWeights.get(j);
                            sum += coFeatureWeights.get(j);
                        }
                        rs.close();
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectUFtr.close();
            selectFtr.close();
            assocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCombineUserAndStereotypesUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCombineUserAndStereotypesUsingFrequencies(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCombineUserAndStereotypesUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectUFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.STEREOTYPE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectUFtrFreq.setString(1, feature);
                        rs = selectUFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);

                        weight += val * freq * coFeatureWeights.get(j);
                        sum += freq * coFeatureWeights.get(j);
                        rs.close();
                    } else {
                        selectFtr.setString(1, feature);
                        rs = selectFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            float val = rs.getFloat(1);
                            rs.close();
                            selectFtrFreq.setString(1, feature);
                            rs = selectFtrFreq.executeQuery();
                            rs.next();
                            float freq = rs.getFloat(1);
                            weight += val * freq * coFeatureWeights.get(j);
                            sum += freq * coFeatureWeights.get(j);
                            rs.close();
                        } else {
                            rs.close();
                        }
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            selectFtrFreq.close();
            selectUFtrFreq.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCombineUserAndStereotypesUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCombineUserAndStereotypesUsingFeatureSimilarities(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCombineUserAndStereotypesUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectUFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.STEREOTYPE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.SFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.SFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement sterAssocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement userAssocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                TreeSet<String> coFeatures = new TreeSet<String>();
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectFtrFreq.setString(1, feature);
                        rs = selectFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);
                        rs.close();

                        userAssocs.setString(1, feature);
                        userAssocs.setString(2, feature);
                        rs = userAssocs.executeQuery();
                        float ftrSimilarity = 1.0f;
                        while (rs.next()) {
                            if (coFeatures.contains(rs.getString(1)) == false) {
                                continue;
                            }
                            ftrSimilarity += rs.getFloat(2) / freq;
                        }
                        rs.close();
                        //System.out.println( ftrSimilarity );
                        weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                        sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                    } else {
                        selectFtr.setString(1, feature);
                        rs = selectFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            float val = rs.getFloat(1);
                            rs.close();
                            selectFtrFreq.setString(1, feature);
                            rs = selectFtrFreq.executeQuery();
                            rs.next();
                            float freq = rs.getFloat(1);
                            rs.close();

                            sterAssocs.setString(1, feature);
                            sterAssocs.setString(2, feature);
                            rs = sterAssocs.executeQuery();
                            float ftrSimilarity = 1.0f;
                            while (rs.next()) {
                                if (coFeatures.contains(rs.getString(1)) == false) {
                                    continue;
                                }
                                ftrSimilarity += rs.getFloat(2) / freq;
                            }
                            rs.close();
                            //System.out.println( ftrSimilarity );
                            weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                            sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                        } else {
                            rs.close();
                        }
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            selectFtrFreq.close();
            selectUFtrFreq.close();
            sterAssocs.close();
            userAssocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateByCollaborativeFiltering(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateByCollaborativeFiltering(queryParam, respBody, dbAccess);
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

    private boolean execEstimateByCollaborativeFiltering(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            int simIdxIdx = queryParam.qpIndexOfKeyNoCase("attr");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            String sql1;
            String sql2;
            if (simIdxIdx != -1) {
                sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
                sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            } else {
                sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_FEATURE_SIMILARITY;
                sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_FEATURE_SIMILARITY;
            }
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectFtr.setString(1, feature);
                    //System.out.println( selectFtr.toString() );
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        weight += rs.getFloat(1) * coFeatureWeights.get(j);
                        sum += coFeatureWeights.get(j);
                    }
                    rs.close();
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateBasedOnCfUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateBasedOnCfUsingFrequencies(queryParam, respBody, dbAccess);
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

    private boolean execEstimateBasedOnCfUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.CFFEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectFtr.setString(1, feature);
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectFtrFreq.setString(1, feature);
                        //System.out.println( selectFtrFreq.toString() );
                        rs = selectFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);
                        weight += val * freq * coFeatureWeights.get(j);
                        sum += freq * coFeatureWeights.get(j);
                        rs.close();
                    } else {
                        rs.close();
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            selectFtrFreq.close();
            assocs.close();

            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateBasedOnCfUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            success = execEstimateBasedOnCfUsingFeatureSimilarities(queryParam, respBody, dbAccess);
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

    private boolean execEstimateBasedOnCfUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.CFFEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.CFFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.CFFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement sterAssocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {
                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    //System.out.println( j );
                    selectFtr.setString(1, feature);
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectFtrFreq.setString(1, feature);
                        rs = selectFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);
                        rs.close();

                        sterAssocs.setString(1, feature);
                        sterAssocs.setString(2, feature);
                        rs = sterAssocs.executeQuery();
                        float ftrSimilarity = 1.0f;
                        while (rs.next()) {
                            if (coFeatures.contains(rs.getString(1)) == false) {
                                continue;
                            }
                            ftrSimilarity += rs.getFloat(2) / freq;
                        }
                        rs.close();
                        //System.out.println( ftrSimilarity );
                        weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                        sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                    } else {
                        rs.close();
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            selectFtrFreq.close();
            assocs.close();
            sterAssocs.close();
            respBody.append("</result>");
            //System.out.println( respBody.toString() );
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCombineUserAndCf(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCombineUserAndCf(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCombineUserAndCf(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        weight += rs.getFloat(1) * coFeatureWeights.get(j);
                        sum += coFeatureWeights.get(j);
                        rs.close();
                    } else {
                        rs.close();
                        selectFtr.setString(1, feature);
                        rs = selectFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            weight += rs.getFloat(1) * coFeatureWeights.get(j);
                            sum += coFeatureWeights.get(j);
                        }
                        rs.close();
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCombineAndCfUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCombineUserAndCfUsingFrequencies(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCombineUserAndCfUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectUFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.CFFEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectUFtrFreq.setString(1, feature);
                        rs = selectUFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);

                        weight += val * freq * coFeatureWeights.get(j);
                        sum += freq * coFeatureWeights.get(j);
                        rs.close();
                    } else {
                        selectFtr.setString(1, feature);
                        rs = selectFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            float val = rs.getFloat(1);
                            rs.close();
                            selectFtrFreq.setString(1, feature);
                            rs = selectFtrFreq.executeQuery();
                            rs.next();
                            float freq = rs.getFloat(1);
                            weight += val * freq * coFeatureWeights.get(j);
                            sum += freq * coFeatureWeights.get(j);
                            rs.close();
                        } else {
                            rs.close();
                        }
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            selectFtrFreq.close();
            selectUFtrFreq.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCombineUserAndCfUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCombineUserAndCfUsingFeatureSimilarities(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCombineUserAndCfUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectUFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.CFFEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.CFFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.CFFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement sterAssocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement userAssocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                TreeSet<String> coFeatures = new TreeSet<String>();
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectFtrFreq.setString(1, feature);
                        rs = selectFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);
                        rs.close();

                        userAssocs.setString(1, feature);
                        userAssocs.setString(2, feature);
                        rs = userAssocs.executeQuery();
                        float ftrSimilarity = 1.0f;
                        while (rs.next()) {
                            if (coFeatures.contains(rs.getString(1)) == false) {
                                continue;
                            }
                            ftrSimilarity += rs.getFloat(2) / freq;
                        }
                        rs.close();
                        //System.out.println( ftrSimilarity );
                        weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                        sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                    } else {
                        selectFtr.setString(1, feature);
                        rs = selectFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            float val = rs.getFloat(1);
                            rs.close();
                            selectFtrFreq.setString(1, feature);
                            rs = selectFtrFreq.executeQuery();
                            rs.next();
                            float freq = rs.getFloat(1);
                            rs.close();

                            sterAssocs.setString(1, feature);
                            sterAssocs.setString(2, feature);
                            rs = sterAssocs.executeQuery();
                            float ftrSimilarity = 1.0f;
                            while (rs.next()) {
                                if (coFeatures.contains(rs.getString(1)) == false) {
                                    continue;
                                }
                                ftrSimilarity += rs.getFloat(2) / freq;
                            }
                            rs.close();
                            //System.out.println( ftrSimilarity );
                            weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                            sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                        } else {
                            rs.close();
                        }
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            selectFtrFreq.close();
            selectUFtrFreq.close();
            sterAssocs.close();
            userAssocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCombineUserCfAndStereotypes(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCombineUserCfAndStereotypes(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCombineUserCfAndStereotypes(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectCfFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        weight += rs.getFloat(1) * coFeatureWeights.get(j);
                        sum += coFeatureWeights.get(j);
                        rs.close();
                    } else {
                        rs.close();
                        selectCfFtr.setString(1, feature);
                        rs = selectCfFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            weight += rs.getFloat(1) * coFeatureWeights.get(j);
                            sum += coFeatureWeights.get(j);
                        } else {
                            rs.close();
                            selectFtr.setString(1, feature);
                            rs = selectFtr.executeQuery();
                            if (rs.next()) {
                                numOfnownCoFtrs++;
                                weight += rs.getFloat(1) * coFeatureWeights.get(j);
                                sum += coFeatureWeights.get(j);
                            } else {
                                rs.close();
                            }
                        }
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectCfFtr.close();
            selectUFtr.close();
            selectFtr.close();
            assocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCombineUserCfAndStereotypesUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCombineUserCfAndStereotypesUsingFrequencies(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCombineUserCfAndStereotypesUsingFrequencies(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectCfFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.CFFEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectCfFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectUFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.STEREOTYPE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectUFtrFreq.setString(1, feature);
                        rs = selectUFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);

                        weight += val * freq * coFeatureWeights.get(j);
                        sum += freq * coFeatureWeights.get(j);
                        rs.close();
                    } else {
                        selectCfFtr.setString(1, feature);
                        rs = selectCfFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            float val = rs.getFloat(1);
                            rs.close();
                            selectCfFtrFreq.setString(1, feature);
                            rs = selectCfFtrFreq.executeQuery();
                            rs.next();
                            float freq = rs.getFloat(1);
                            weight += val * freq * coFeatureWeights.get(j);
                            sum += freq * coFeatureWeights.get(j);
                            rs.close();
                        } else {
                            rs.close();
                            selectFtr.setString(1, feature);
                            rs = selectFtr.executeQuery();
                            if (rs.next()) {
                                numOfnownCoFtrs++;
                                float val = rs.getFloat(1);
                                rs.close();
                                selectFtrFreq.setString(1, feature);
                                rs = selectFtrFreq.executeQuery();
                                rs.next();
                                float freq = rs.getFloat(1);
                                weight += val * freq * coFeatureWeights.get(j);
                                sum += freq * coFeatureWeights.get(j);
                                rs.close();
                            } else {
                                rs.close();
                            }
                        }
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectCfFtr.close();
            selectCfFtrFreq.close();
            selectFtr.close();
            assocs.close();
            selectFtrFreq.close();
            selectUFtrFreq.close();
            selectCfFtr.close();
            selectCfFtrFreq.close();
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCombineUserCfAndStereotypesUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCombineUserCfAndStereotypesUsingFeatureSimilarities(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCombineUserCfAndStereotypesUsingFeatureSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectCfFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.CFFEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectCfFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectUFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.STEREOTYPE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.SFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.SFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.SFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement sterAssocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement userAssocs = dbAccess.getConnection().prepareStatement(sql);

            sql1 = "SELECT " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.CFFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql2 = "SELECT " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.CFFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_USR + "='" + user + "' AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.CFFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement cfAssocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                TreeSet<String> coFeatures = new TreeSet<String>();
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectFtrFreq.setString(1, feature);
                        rs = selectFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);
                        rs.close();

                        userAssocs.setString(1, feature);
                        userAssocs.setString(2, feature);
                        rs = userAssocs.executeQuery();
                        float ftrSimilarity = 1.0f;
                        while (rs.next()) {
                            if (coFeatures.contains(rs.getString(1)) == false) {
                                continue;
                            }
                            ftrSimilarity += rs.getFloat(2) / freq;
                        }
                        rs.close();
                        //System.out.println( ftrSimilarity );
                        weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                        sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                    } else {
                        selectCfFtr.setString(1, feature);
                        rs = selectCfFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            float val = rs.getFloat(1);
                            rs.close();
                            selectCfFtrFreq.setString(1, feature);
                            rs = selectCfFtrFreq.executeQuery();
                            rs.next();
                            float freq = rs.getFloat(1);
                            rs.close();

                            cfAssocs.setString(1, feature);
                            cfAssocs.setString(2, feature);
                            rs = cfAssocs.executeQuery();
                            float ftrSimilarity = 1.0f;
                            while (rs.next()) {
                                if (coFeatures.contains(rs.getString(1)) == false) {
                                    continue;
                                }
                                ftrSimilarity += rs.getFloat(2) / freq;
                            }
                            rs.close();
                            //System.out.println( ftrSimilarity );
                            weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                            sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                        } else {
                            rs.close();
                            selectFtr.setString(1, feature);
                            rs = selectFtr.executeQuery();
                            if (rs.next()) {
                                numOfnownCoFtrs++;
                                float val = rs.getFloat(1);
                                rs.close();
                                selectFtrFreq.setString(1, feature);
                                rs = selectFtrFreq.executeQuery();
                                rs.next();
                                float freq = rs.getFloat(1);
                                rs.close();

                                sterAssocs.setString(1, feature);
                                sterAssocs.setString(2, feature);
                                rs = sterAssocs.executeQuery();
                                float ftrSimilarity = 1.0f;
                                while (rs.next()) {
                                    if (coFeatures.contains(rs.getString(1)) == false) {
                                        continue;
                                    }
                                    ftrSimilarity += rs.getFloat(2) / freq;
                                }
                                rs.close();
                                //System.out.println( ftrSimilarity );
                                weight += val * freq * coFeatureWeights.get(j) * ftrSimilarity;
                                sum += freq * coFeatureWeights.get(j) * ftrSimilarity;
                            } else {
                                rs.close();
                            }
                        }
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectCfFtr.close();
            selectCfFtrFreq.close();
            cfAssocs.close();
            selectFtr.close();
            assocs.close();
            selectFtrFreq.close();
            selectUFtrFreq.close();
            sterAssocs.close();
            userAssocs.close();
            respBody.append("</result>");
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int createFtrSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execCreateFtrSimilarities(queryParam, respBody, dbAccess);
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

    private boolean execCreateFtrSimilarities(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);
            Statement stmt = dbAccess.getConnection().createStatement();
            String sql = "SELECT * FROM " + DBAccess.FEATURE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_TABLE_FIELD_FEATURE + " LIKE 'movie.%'";
            ResultSet rs = stmt.executeQuery(sql);
            Vector<String> ftrs = new Vector<String>();
            while (rs.next()) {
                ftrs.add(rs.getString(DBAccess.FEATURE_TABLE_FIELD_FEATURE));
            }
            rs.close();

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            String sqlIns = "REPLACE DELAYED INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + "(" + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ("
                    + "?,?," + DBAccess.RELATION_FEATURE_SIMILARITY + ",?,'" + clientName + "')";
            PreparedStatement insAssoc = dbAccess.getConnection().prepareStatement(sqlIns);


            for (int i = 0; i < ftrs.size(); i++) {
                insAssoc.clearBatch();
                System.out.println("associations for " + ftrs.get(i));
                assocs.setString(1, ftrs.get(i));
                assocs.setString(2, ftrs.get(i));
                rs = assocs.executeQuery();
                Set<String> coFtrs = new HashSet<String>();
                while (rs.next()) {
                    coFtrs.add(rs.getString(1));
                }
                rs.close();
                for (int j = i + 1; j < ftrs.size(); j++) {
                    float common = 0;
                    float all = coFtrs.size();
                    assocs.setString(1, ftrs.get(j));
                    assocs.setString(2, ftrs.get(j));
                    rs = assocs.executeQuery();
                    while (rs.next()) {
                        String ftr = rs.getString(1);
                        if (coFtrs.contains(ftr)) {
                            common++;
                        } else {
                            all++;
                        }
                    }
                    rs.close();

                    if (common > 0) {
                        float dist = common / all;
                        insAssoc.setString(1, ftrs.get(i));
                        insAssoc.setString(2, ftrs.get(j));
                        insAssoc.setFloat(3, dist);
                        insAssoc.addBatch();
                    }
                    //System.out.println( insAssoc.toString() );
                }
                insAssoc.executeBatch();
            }
            insAssoc.close();
            assocs.close();

        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int estimateCommoncf(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateCommoncf(queryParam, respBody, dbAccess);
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

    private boolean execEstimateCommoncf(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = ftrPattern.split("\\|");

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");
            //String sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            String sql = "SELECT " + DBAccess.NUM_DATA_TABLE_FIELD_VALUE + "," + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + " FROM " + DBAccess.NUM_DATA_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.NUM_DATA_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_FEATURE_SIMILARITY;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_FEATURE_SIMILARITY;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            int s0 = 0;
            int s = Integer.MAX_VALUE;
            Statement ftrStmt = dbAccess.getConnection().createStatement();
            String sesSql = "SELECT MAX(convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ", SIGNED)) FROM " + DBAccess.NUM_DATA_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "='" + user + "'";
            ResultSet sesRs = ftrStmt.executeQuery(sesSql);
            if (sesRs.next()) {
                s = sesRs.getInt(1);
            }
            sesRs.close();
            sesSql = "SELECT MIN(convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ", SIGNED)) FROM " + DBAccess.NUM_DATA_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "='" + user + "'";
            sesRs = ftrStmt.executeQuery(sesSql);
            if (sesRs.next()) {
                int tmpS0 = sesRs.getInt(1);
                if (tmpS0 > s0) {
                    s0 = tmpS0;
                }
            }
            int n = s - s0 + 1;
            sesRs.close();
            ftrStmt.close();

            for (int i = 0; i < ftrs.length; i++) {

                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                int k = 25;

                for (String feature : coFeatures) {
                    selectFtr.setString(1, feature);
                    //rs = dbAccess.executeQuery(sql).getRs();
                    rs = selectFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        int ses = rs.getInt(2);
                        float cval = 2 * (k / n) * (ses - s0);
                        weight += rs.getFloat(1) * coFeatureWeights.get(j) * (1 + k + cval);
                        sum += coFeatureWeights.get(j) * (1 + k + cval);
                    }
                    rs.close();
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectFtr.close();
            assocs.close();
            respBody.append("</result>");
        } catch (Exception ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            //System.exit(1);
            return false;
        }
        return true;
    }

    private int estimateSuper(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execEstimateSuper(queryParam, respBody, dbAccess);
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

    private boolean execEstimateSuper(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {

        try {
            FileWriter outFile = null;
            PrintWriter out = null;
            //outFile = new FileWriter("/home/alexm/workspacePServer/PServer/TestSets/medium/test/u.data25new", true);
            //out = new PrintWriter(outFile);

            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int ftrsIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
            String ftrNames = null;
            if (ftrsIdx != -1) {
                ftrNames = (String) queryParam.getVal(ftrsIdx);
            }

            int usrIdx = queryParam.qpIndexOfKeyNoCase("usr");
            if (usrIdx == -1) {
                WebServer.win.log.error("-Missing argument: usr");
                return false;
            }
            String user = (String) queryParam.getVal(usrIdx);

            int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftr");
            if (ftrIdx == -1) {
                WebServer.win.log.error("-Missing argument: ftr");
                return false;
            }
            String ftrPattern = (String) queryParam.getVal(ftrIdx);
            String[] ftrs = null;
            float[] vals = null;
            ftrs = ftrPattern.split("\\|");
            vals = new float[ftrs.length];
            for (int i = 0; i < ftrs.length; i++) {
                String[] tmp = ftrs[i].split("=");
                ftrs[i] = tmp[0];
                vals[i] = Float.parseFloat(tmp[1]);
            }

            respBody.append(DBAccess.xmlHeader("/resp_xsl/estimation_profile.xsl"));
            respBody.append("<result>\n");

            String sql;
            sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                    + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "'";
            Statement stmt = dbAccess.getConnection().createStatement();
            ResultSet stersRs = stmt.executeQuery(sql);
            int num = 0;
            String stereotype = null;
            while (stersRs.next()) {
                num++;
                stereotype = stersRs.getString(1);
            }
            if (num > 1) {
                System.exit(-1);
            }
            stersRs.close();
            stmt.close();

            sql = "SELECT " + DBAccess.CFPROFILE_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.CFPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectCfFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.CFFEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.CFFEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectCfFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.UPROFILE_TABLE_FIELD_VALUE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectUFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_USER + "='" + user + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectUFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + " FROM " + DBAccess.STEREOTYPE_PROFILES_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE + "=?";
            PreparedStatement selectFtr = dbAccess.getConnection().prepareStatement(sql);

            sql = "SELECT " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.STEREOTYPE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.STEREOTYPE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            PreparedStatement selectFtrFreq = dbAccess.getConnection().prepareStatement(sql);

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            for (int i = 0; i < ftrs.length; i++) {

                float trueVal = vals[i];
                assocs.setString(1, ftrs[i]);
                assocs.setString(2, ftrs[i]);
                ResultSet rs = assocs.executeQuery();

                ArrayList<String> coFeatures = new ArrayList<String>(40);
                ArrayList<Float> coFeatureWeights = new ArrayList<Float>(40);
                while (rs.next()) {
                    coFeatures.add(rs.getString(1));
                    coFeatureWeights.add(rs.getFloat(2));
                }
                rs.close();
                int numOfnownCoFtrs = 0;
                int numOfnownCoPFtrs = 0;
                float weight = 0.0f;
                float sum = 0.0f;
                int j = 0;

                for (String feature : coFeatures) {
                    selectUFtr.setString(1, feature);
                    rs = selectUFtr.executeQuery();
                    if (rs.next()) {
                        numOfnownCoFtrs++;
                        float val = rs.getFloat(1);
                        rs.close();
                        selectUFtrFreq.setString(1, feature);
                        rs = selectUFtrFreq.executeQuery();
                        rs.next();
                        float freq = rs.getFloat(1);
                        rs.close();

                        float udist = Math.abs(trueVal - val);

                        selectCfFtr.setString(1, feature);
                        rs = selectCfFtr.executeQuery();
                        rs.next();
                        float cval = rs.getFloat(1);
                        rs.close();

                        float cdist = Math.abs(trueVal - cval);

                        selectFtr.setString(1, feature);
                        rs = selectFtr.executeQuery();
                        rs.next();
                        float sval = rs.getFloat(1);
                        rs.close();

                        val = (val * cParam + cval * sParam + sval * pParam) / (pParam + cParam + sParam);
                        //val = (val + cval + sval ) / 3;

                        weight += val * freq * coFeatureWeights.get(j);
                        sum += freq * coFeatureWeights.get(j);
                        rs.close();

                        float sdist = Math.abs(trueVal - sval);

                        if (udist <= cdist && udist <= sdist) {
                            this.pCorrects++;
                            all++;
                        } else if (cdist <= udist && cdist <= sdist) {
                            this.cCorrects++;
                            all++;
                        } else if (sdist <= udist && sdist <= cdist) {
                            this.sCorrects++;
                            all++;
                        } else {
                            System.out.println("=========================================");
                            System.out.println("=========================================");
                            System.out.println("here hoes the fail");
                            System.out.println("=========================================");
                            System.out.println("=========================================");
                        }

                        /*if(  udist > cdist  )
                         //out.println( freq + "," + cfreq + "," + sfreq + ",1" );
                         else if ( udist > sdist )
                         //out.println( freq + "," + cfreq + "," + sfreq + ",2" );
                         else
                         //out.println( freq + "," + cfreq + "," + sfreq + ",0" );
                         *
                         */
                        weight += val * freq * coFeatureWeights.get(j);
                        sum += freq * coFeatureWeights.get(j);

                    } else {
                        selectCfFtr.setString(1, feature);
                        rs = selectCfFtr.executeQuery();
                        if (rs.next()) {
                            numOfnownCoFtrs++;
                            float val = rs.getFloat(1);
                            rs.close();
                            selectCfFtrFreq.setString(1, feature);
                            rs = selectCfFtrFreq.executeQuery();
                            rs.next();
                            float freq = rs.getFloat(1);

                            float cdist = Math.abs(trueVal - val);

                            selectFtr.setString(1, feature);
                            rs = selectFtr.executeQuery();
                            if (rs.next()) {
                                float sval = rs.getFloat(1);
                                rs.close();

                                float sdist = Math.abs(trueVal - sval);

                                if (cdist <= sdist) {
                                    this.cCorrects2++;
                                    all2++;
                                } else {
                                    this.sCorrects2++;
                                    all2++;
                                }


                                val = (val * cParam2 + sval * sParam2) / (cParam2 + sParam2);
                            }
                            weight += val * freq * coFeatureWeights.get(j);
                            sum += freq * coFeatureWeights.get(j);
                            rs.close();
                        } else {
                            rs.close();
                            selectFtr.setString(1, feature);
                            rs = selectFtr.executeQuery();
                            if (rs.next()) {
                                numOfnownCoFtrs++;
                                float val = rs.getFloat(1);
                                rs.close();
                                selectFtrFreq.setString(1, feature);
                                rs = selectFtrFreq.executeQuery();
                                rs.next();
                                float freq = rs.getFloat(1);
                                weight += val * freq * coFeatureWeights.get(j);
                                sum += freq * coFeatureWeights.get(j);
                                rs.close();
                            } else {
                                rs.close();
                            }
                        }
                    }
                    j++;
                }
                if (numOfnownCoFtrs > 0) {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>" + (weight / sum) + "</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                } else {
                    String record = "<row><ftr>" + ftrs[i] + "</ftr><val>uknown</val><factors>" + (numOfnownCoFtrs * 1.0 / coFeatureWeights.size()) + "</factors></row>";
                    respBody.append(record);
                }
            }
            selectCfFtr.close();
            selectCfFtrFreq.close();
            selectFtr.close();
            assocs.close();
            selectFtrFreq.close();
            selectUFtrFreq.close();
            selectCfFtr.close();
            selectCfFtrFreq.close();

            //outFile.close();

        } catch (Exception ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        System.out.println("    percents " + (this.pCorrects / all) + " : " + (this.cCorrects / all) + " : " + (this.sCorrects / all));
        System.out.println("    percents2 " + (this.cCorrects2 / all2) + " : " + (this.sCorrects2 / all2));
        return true;
    }
}
