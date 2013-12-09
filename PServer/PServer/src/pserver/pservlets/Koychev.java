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

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pserver.PersServer;
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
public class Koychev implements pserver.pservlets.PService {

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
        if (com.equalsIgnoreCase("update")) {//create user communities
            respCode = updateProfile(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("updateAuto")) {//create user communities
            respCode = updateAutoProfile(queryParam, respBody, dbAccess);
        } else {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command not recognized");
        }

        response.append(respBody.toString());
        return respCode;
    }

    private int updateProfile(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execUpdateProfile(queryParam, respBody, dbAccess);
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

    private boolean execUpdateProfile(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            boolean success = true;
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int t0, t;
            int t0Idx = queryParam.qpIndexOfKeyNoCase("s0");
            if (t0Idx == -1) {
                t0 = 0;
            } else {
                t0 = Integer.parseInt((String) queryParam.getVal(t0Idx));
            }

            if (t0 == 0) {
                PServerResultSet prs = dbAccess.executeQuery("SELECT MIN( convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ",SIGNED)) FROM " + DBAccess.NUM_DATA_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'");
                if (prs.getRs().next()) {
                    t0 = prs.getRs().getInt(1);
                }
                prs.close();
            }
            int tIdx = queryParam.qpIndexOfKeyNoCase("s");
            if (tIdx == -1) {
                t = Integer.MAX_VALUE;
            } else {
                t = Integer.parseInt((String) queryParam.getVal(tIdx));
            }
            int kIdx = queryParam.qpIndexOfKeyNoCase("k");
            float k;
            if (kIdx == -1) {
                WebServer.win.log.error("Needed parameter k is being missed");
                return false;
            } else {
                k = Float.parseFloat((String) queryParam.getVal(kIdx));
            }
            Statement stmt = dbAccess.getConnection().createStatement();
            String sql;

            String subSql = "SELECT " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "," + DBAccess.NUM_DATA_TABLE_FIELD_FEATURE + "," + DBAccess.NUM_DATA_TABLE_FIELD_VALUE + "," + DBAccess.NUM_DATA_TABLE_FIELD_VALUE + ",'" + clientName + "' " + " FROM " + DBAccess.NUM_DATA_TABLE
                    + " WHERE " + DBAccess.NUM_DATA_TABLE_FIELD_TIMESTAMP + ">=" + t0 + " AND " + DBAccess.NUM_DATA_TABLE_FIELD_TIMESTAMP + "<=" + t + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";

            sql = "REPLACE DELAYED INTO " + DBAccess.UPROFILE_TABLE + "(" + DBAccess.UPROFILE_TABLE_FIELD_USER + "," + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "," + DBAccess.UPROFILE_TABLE_FIELD_VALUE + "," + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE + "," + DBAccess.FIELD_PSCLIENT + ")"
                    + subSql;
            final int userSize = 1000;
            int offset = 0;
            int i = 0;
            int threadNum = Integer.parseInt(PersServer.pref.getPref("thread_num"));

            ExecutorService threadExecutor = Executors.newFixedThreadPool(threadNum);
            do {
                i = 0;
                sql = "SELECT " + DBAccess.USER_TABLE_FIELD_USER + " FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' LIMIT " + userSize + " OFFSET " + offset;
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String user = rs.getString(1);
                    threadExecutor.execute(new KoychevUserLikeThread(dbAccess, user, clientName, t0, t, k));
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
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
    }

    private int updateAutoProfile(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execUpdateAutoProfile(queryParam, respBody, dbAccess);
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

    private boolean execUpdateAutoProfile(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            boolean success = true;
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int t0, t;
            int t0Idx = queryParam.qpIndexOfKeyNoCase("s0");
            if (t0Idx == -1) {
                t0 = 0;
            } else {
                t0 = Integer.parseInt((String) queryParam.getVal(t0Idx));
            }

            if (t0 == 0) {
                PServerResultSet prs = dbAccess.executeQuery("SELECT MIN( convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ",SIGNED)) FROM " + DBAccess.NUM_DATA_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'");
                if (prs.getRs().next()) {
                    t0 = prs.getRs().getInt(1);
                }
                prs.close();
            }
            int tIdx = queryParam.qpIndexOfKeyNoCase("s");
            if (tIdx == -1) {
                t = Integer.MAX_VALUE;
            } else {
                t = Integer.parseInt((String) queryParam.getVal(tIdx));
            }

            Statement stmt = dbAccess.getConnection().createStatement();
            String sql;

            String subSql = "SELECT " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "," + DBAccess.NUM_DATA_TABLE_FIELD_FEATURE + "," + DBAccess.NUM_DATA_TABLE_FIELD_VALUE + "," + DBAccess.NUM_DATA_TABLE_FIELD_VALUE + ",'" + clientName + "' " + " FROM " + DBAccess.NUM_DATA_TABLE
                    + " WHERE " + DBAccess.NUM_DATA_TABLE_FIELD_TIMESTAMP + ">=" + t0 + " AND " + DBAccess.NUM_DATA_TABLE_FIELD_TIMESTAMP + "<=" + t + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";

            sql = "REPLACE DELAYED INTO " + DBAccess.UPROFILE_TABLE + "(" + DBAccess.UPROFILE_TABLE_FIELD_USER + "," + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "," + DBAccess.UPROFILE_TABLE_FIELD_VALUE + "," + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE + "," + DBAccess.FIELD_PSCLIENT + ")"
                    + subSql;
            final int userSize = 1000;
            int offset = 0;
            int i = 0;
            int threadNum = Integer.parseInt(PersServer.pref.getPref("thread_num"));
            threadNum = 4;

            String usersFileName = "/home/alexm/resultset.csv";
            String testFileName = "/home/alexm/workspacePServer/PServer/TestSets/medium/test/u.data25";
            BufferedReader usersInput = new BufferedReader(new FileReader(usersFileName));
            String line;
            LinkedList<String> userNames = new LinkedList<String>();
            while ((line = usersInput.readLine()) != null) {
                //System.out.println(line);
                userNames.add(line);
            }

            BufferedReader input = new BufferedReader(new FileReader(testFileName));
            HashMap<String, HashMap<String, Float>> ftrValues = new HashMap<String, HashMap<String, Float>>();
            int k = 0;
            while ((line = input.readLine()) != null) {
                String[] tokens = line.split("::");
                if (userNames.contains(tokens[ 0]) == false) {
                    continue;
                }
                if (ftrValues.get(tokens[ 0]) == null) {
                    ftrValues.put(tokens[ 0], new HashMap<String, Float>());
                }
                ftrValues.get(tokens[ 0]).put("movie." + tokens[ 1], Float.parseFloat(tokens[ 2]));
                k++;
            }

            ExecutorService threadExecutor = Executors.newFixedThreadPool(threadNum);
            do {
                i = 0;
                sql = "SELECT " + DBAccess.USER_TABLE_FIELD_USER + " FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' LIMIT " + userSize + " OFFSET " + offset;
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String user = rs.getString(1);
                    threadExecutor.execute(new KoychevUserAutoLikeThread(dbAccess, user, clientName, t0, t, ftrValues.get(user)));
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
        } catch (Exception ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
    }
}

class KoychevUserLikeThread extends Thread {

    String user;
    DBAccess dbAccess;
    String clientName;
    int s0;
    int s;
    float k;

    public KoychevUserLikeThread(DBAccess dbAccess, String user, String clientName, int s0, int s, float k) {
        this.dbAccess = dbAccess;
        this.user = user;
        this.clientName = clientName;
        this.s0 = s0;
        this.s = s;
        this.k = k;
    }

    public void init(DBAccess dbAccess, String user, String clientName, int s0, int s, float k) {
        this.dbAccess = dbAccess;
        this.user = user;
        this.clientName = clientName;
        this.s0 = s0;
        this.s = s;
        this.k = k;
    }

    @Override
    public void run() {
        WebServer.win.log.echo("Building profile for user " + user);
        String tmpSql = "";
        try {
            Statement ftrStmt = dbAccess.getConnection().createStatement();
            Statement ftrStmt2 = dbAccess.getConnection().createStatement();
            String sql;
            sql = "REPLACE DELAYED INTO " + DBAccess.UPROFILE_TABLE + "(" + DBAccess.UPROFILE_TABLE_FIELD_USER + "," + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "," + DBAccess.UPROFILE_TABLE_FIELD_VALUE + "," + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE + "," + DBAccess.FIELD_PSCLIENT + ")"
                    + " VALUES ('" + user + "',?,?,?,'" + clientName + "')";
            PreparedStatement insertStmt = dbAccess.getConnection().prepareStatement(sql);

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
            String subSql = "SELECT " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "," + DBAccess.NUM_DATA_TABLE_FIELD_FEATURE + "," + DBAccess.NUM_DATA_TABLE_FIELD_VALUE + ",convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ", SIGNED),'" + clientName + "' " + " FROM " + DBAccess.NUM_DATA_TABLE
                    + " WHERE convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ", SIGNED)>=" + s0 + " AND convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ", SIGNED)<=" + s + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
            HashMap<String, Float> featureValues = new HashMap<String, Float>(1000);
            HashMap<String, Float> featureNeighWeight = new HashMap<String, Float>(1000);
            ResultSet tmpRs = ftrStmt.executeQuery(subSql + " AND " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "='" + user + "'");

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

            while (tmpRs.next()) {
                String ftr = tmpRs.getString(2);
                float val = tmpRs.getFloat(3);
                int ses = tmpRs.getInt(4);

                insertStmt.clearParameters();
                insertStmt.setString(1, ftr);
                insertStmt.setString(2, val + "");
                insertStmt.setFloat(3, val);
                insertStmt.addBatch();

                assocs.setString(1, ftr);
                assocs.setString(2, ftr);
                ResultSet tmpRs2 = assocs.executeQuery();

                float cval = 2 * (k / n) * (ses - s0);
                while (tmpRs2.next()) {
                    String ftrOther = tmpRs2.getString(1);
                    float extraWeight = tmpRs2.getFloat(2);
                    float weight = 1 + extraWeight * (1 + k + cval);
                    if (featureValues.get(ftrOther) == null) {
                        featureValues.put(ftrOther, val * weight);
                        featureNeighWeight.put(ftrOther, weight);
                    } else {
                        featureValues.put(ftrOther, featureValues.get(ftrOther) + val * weight);
                        featureNeighWeight.put(ftrOther, featureNeighWeight.get(ftrOther) + weight);
                    }
                }
                tmpRs2.close();
                sql = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "='" + ftr + "' AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
                tmpRs2 = ftrStmt2.executeQuery(sql);
                while (tmpRs2.next()) {
                    String ftrOther = tmpRs2.getString(1);
                    float extraWeight = tmpRs2.getFloat(2);
                    extraWeight = 1;
                    float weight = 1 + extraWeight * (1 + k + cval);
                    //weight = 1;
                    if (weight < 1) {
                        weight = 1;
                    }
                    if (featureValues.get(ftrOther) == null) {
                        featureValues.put(ftrOther, val * weight);
                        featureNeighWeight.put(ftrOther, weight);
                    } else {
                        featureValues.put(ftrOther, featureValues.get(ftrOther) + val * weight);
                        featureNeighWeight.put(ftrOther, featureNeighWeight.get(ftrOther) + weight);
                    }
                }
                tmpRs2.close();
            }
            tmpRs.close();
            Set<String> ftrs = featureValues.keySet();

            for (String ftrName : ftrs) {
                float count = featureNeighWeight.get(ftrName);
                if ((count + "").equals("NaN")) {
                    continue;
                }
                float val = (featureValues.get(ftrName) / count);
                insertStmt.clearParameters();
                insertStmt.setString(1, ftrName);
                insertStmt.setString(2, val + "");
                insertStmt.setFloat(3, val);
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();

            ftrStmt.cancel();
            ftrStmt2.close();
            insertStmt.close();
            assocs.close();
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            System.exit(0);
        }
    }
}

class KoychevUserAutoLikeThread extends Thread {

    String user;
    DBAccess dbAccess;
    String clientName;
    int s0;
    int s;
    private HashMap<String, Float> testValues;

    KoychevUserAutoLikeThread(DBAccess dbAccess, String user, String clientName, int t0, int t, HashMap<String, Float> testValues) {
        this.dbAccess = dbAccess;
        this.user = user;
        this.clientName = clientName;
        this.s0 = t0;
        this.s = t;
        this.testValues = testValues;
    }

    public void init(DBAccess dbAccess, String user, String clientName, int t0, int t, HashMap<String, Float> testValues) {
        this.dbAccess = dbAccess;
        this.user = user;
        this.clientName = clientName;
        this.s0 = t0;
        this.s = t;
        this.testValues = testValues;
    }

    @Override
    public void run() {
        WebServer.win.log.echo("Building profile for user " + user);
        String tmpSql = "";
        try {
            Statement ftrStmt = dbAccess.getConnection().createStatement();
            Statement ftrStmt2 = dbAccess.getConnection().createStatement();
            String sql;
            sql = "REPLACE DELAYED INTO " + DBAccess.UPROFILE_TABLE + "(" + DBAccess.UPROFILE_TABLE_FIELD_USER + "," + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "," + DBAccess.UPROFILE_TABLE_FIELD_VALUE + "," + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE + "," + DBAccess.FIELD_PSCLIENT + ")"
                    + " VALUES ('" + user + "',?,?,?,'" + clientName + "')";
            PreparedStatement insertStmt = dbAccess.getConnection().prepareStatement(sql);

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

            float k = 0;
            float goodK = 0;
            float error = Float.MAX_VALUE;
            float prevError = Float.MAX_VALUE;
            float dist = 25;
            float start = 0.0f;

            String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
            sql = "(" + sql1 + ") UNION (" + sql2 + ")";
            PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);
            boolean stop = false;
            do {
                String subSql = "SELECT " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "," + DBAccess.NUM_DATA_TABLE_FIELD_FEATURE + "," + DBAccess.NUM_DATA_TABLE_FIELD_VALUE + ",convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ", SIGNED),'" + clientName + "' " + " FROM " + DBAccess.NUM_DATA_TABLE
                        + " WHERE convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ", SIGNED)>=" + s0 + " AND convert(" + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + ", SIGNED)<=" + s + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
                HashMap<String, Float> featureValues = new HashMap<String, Float>(1000);
                HashMap<String, Float> featureNeighWeight = new HashMap<String, Float>(1000);
                ResultSet tmpRs = ftrStmt.executeQuery(subSql + " AND " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "='" + user + "'");


                while (tmpRs.next()) {
                    String ftr = tmpRs.getString(2);
                    float val = tmpRs.getFloat(3);
                    int ses = tmpRs.getInt(4);

                    insertStmt.clearParameters();
                    insertStmt.setString(1, ftr);
                    insertStmt.setString(2, val + "");
                    insertStmt.setFloat(3, val);
                    insertStmt.addBatch();

                    assocs.setString(1, ftr);
                    assocs.setString(2, ftr);
                    ResultSet tmpRs2 = assocs.executeQuery();

                    float cval = 2 * (k / n) * (ses - s0);
                    while (tmpRs2.next()) {
                        String ftrOther = tmpRs2.getString(1);
                        float extraWeight = tmpRs2.getFloat(2);
                        float weight = 1 + extraWeight * (1 + k + cval);
                        if (featureValues.get(ftrOther) == null) {
                            featureValues.put(ftrOther, val * weight);
                            featureNeighWeight.put(ftrOther, weight);
                        } else {
                            featureValues.put(ftrOther, featureValues.get(ftrOther) + val * weight);
                            featureNeighWeight.put(ftrOther, featureNeighWeight.get(ftrOther) + weight);
                        }
                    }
                    tmpRs2.close();
                    sql = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "='" + ftr + "' AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
                    tmpRs2 = ftrStmt2.executeQuery(sql);
                    while (tmpRs2.next()) {
                        String ftrOther = tmpRs2.getString(1);
                        float extraWeight = tmpRs2.getFloat(2);
                        extraWeight = 1;
                        float weight = 1 + extraWeight * (1 + k + cval);
                        //weight = 1;
                        if (weight < 1) {
                            weight = 1;
                        }
                        if (featureValues.get(ftrOther) == null) {
                            featureValues.put(ftrOther, val * weight);
                            featureNeighWeight.put(ftrOther, weight);
                        } else {
                            featureValues.put(ftrOther, featureValues.get(ftrOther) + val * weight);
                            featureNeighWeight.put(ftrOther, featureNeighWeight.get(ftrOther) + weight);
                        }
                    }
                    tmpRs2.close();
                }
                tmpRs.close();
                //System.out.println( stop);
                if (stop) {
                    //System.out.println("final error " + error + " final k " + k );
                    Set<String> ftrs = featureValues.keySet();
                    for (String ftrName : ftrs) {
                        float count = featureNeighWeight.get(ftrName);
                        if ((count + "").equals("NaN")) {
                            continue;
                        }
                        float val = (featureValues.get(ftrName) / count);
                        insertStmt.clearParameters();
                        insertStmt.setString(1, ftrName);
                        insertStmt.setString(2, val + "");
                        insertStmt.setFloat(3, val);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                    break;
                }
                error = testFeatureValues(featureValues, featureNeighWeight);
                //System.out.println("error " + error + " k " + k );
                if (prevError <= error) {
                    k -= dist;
                    dist /= 2;
                    if (dist < 1) {
                        stop = true;
                    }
                } else {
                    prevError = error;
                }
                k += dist;
                featureValues.clear();
                featureNeighWeight.clear();
            } while (true);
            ftrStmt.close();
            ftrStmt2.close();
            insertStmt.close();
            assocs.close();
        } catch (SQLException ex) {
            //System.out.println("the executuin was " + tmpSql);
            WebServer.win.log.error(ex.toString());
            System.exit(0);
        }
    }

    private float testFeatureValues(HashMap<String, Float> featureValues, HashMap<String, Float> featureNeighWeight) throws SQLException {
        String sql1 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
        String sql2 = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " ftr," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "='' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "=? AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL;
        String sql = "(" + sql1 + ") UNION (" + sql2 + ")";
        PreparedStatement assocs = dbAccess.getConnection().prepareStatement(sql);

        float error = 0.0f;
        int k = 0;
        for (String testFtr : this.testValues.keySet()) {
            assocs.setString(1, testFtr);
            assocs.setString(2, testFtr);
            ResultSet rs = assocs.executeQuery();
            int i = 0;
            float sum = 0.0f;
            while (rs.next()) {
                String ftr = rs.getString(1);
                float weight = rs.getFloat(2);
                if (featureValues.get(ftr) == null) {
                    continue;
                }
                float storedVal = featureValues.get(ftr) / featureNeighWeight.get(ftr);
                i++;
                sum += storedVal;
            }
            if (i == 0) {
                continue;
            }
            k++;
            error += Math.abs((sum / i) - testValues.get(testFtr));
            //System.out.println( testFtr + " " + ( sum / i)  + " " + testValues.get( testFtr ));
            rs.close();
        }
        assocs.close();
        return error / k;
    }
}
