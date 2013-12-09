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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pserver.PersServer;
import pserver.WebServer;
import pserver.data.Barrier;
import pserver.data.DBAccess;
import pserver.data.PServerResultSet;
import pserver.data.VectorMap;
import pserver.logic.PSReqWorker;
import pserver.utilities.ClientCredentialsChecker;

/**
 *
 * @author alexm
 */
public class Nootropia implements pserver.pservlets.PService {

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
        } else if (com.equalsIgnoreCase("genweights")) {//create user communities
            respCode = genWeights(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("genpersweights")) {//create user communities
            respCode = genPersWeights(queryParam, respBody, dbAccess);
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
            int t0Idx = queryParam.qpIndexOfKeyNoCase("t0");
            if (t0Idx == -1) {
                t0 = 0;
            } else {
                t0 = Integer.parseInt((String) queryParam.getVal(t0Idx));
            }

            if (t0 == 0) {
                PServerResultSet prs = dbAccess.executeQuery("SELECT MIN(" + DBAccess.NUM_DATA_TABLE_FIELD_TIMESTAMP + ") FROM " + DBAccess.NUM_DATA_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'");
                if (prs.getRs().next()) {
                    t0 = prs.getRs().getInt(1);
                }
                prs.close();
            }
            //WebServer.win.log.echo( "t0 = " + t0 );

            int tIdx = queryParam.qpIndexOfKeyNoCase("t");
            if (tIdx == -1) {
                t = Integer.MAX_VALUE;
            } else {
                t = Integer.parseInt((String) queryParam.getVal(tIdx));
            }
            //WebServer.win.log.echo( "t = " + t );

            Statement stmt = dbAccess.getConnection().createStatement();
            String sql = "DELETE FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
            stmt.execute(sql);

            String subSql = "SELECT " + DBAccess.NUM_DATA_TABLE_FIELD_USER + "," + DBAccess.NUM_DATA_TABLE_FIELD_FEATURE + "," + DBAccess.NUM_DATA_TABLE_FIELD_VALUE + "," + DBAccess.NUM_DATA_TABLE_FIELD_VALUE + ",'" + clientName + "' " + " FROM " + DBAccess.NUM_DATA_TABLE
                    + " WHERE " + DBAccess.NUM_DATA_TABLE_FIELD_TIMESTAMP + ">=" + t0 + " AND " + DBAccess.NUM_DATA_TABLE_FIELD_TIMESTAMP + "<=" + t + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";

            sql = "INSERT INTO " + DBAccess.UPROFILE_TABLE + "(" + DBAccess.UPROFILE_TABLE_FIELD_USER + "," + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "," + DBAccess.UPROFILE_TABLE_FIELD_VALUE + "," + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE + "," + DBAccess.FIELD_PSCLIENT + ")"
                    + subSql;
            stmt.execute(sql);

            final int userSize = 100;
            int offset = 0;
            int i = 0;
            Barrier barrier = new Barrier(Integer.parseInt(PersServer.pref.getPref("thread_num")));
            do {
                i = 0;
                sql = "SELECT " + DBAccess.USER_TABLE_FIELD_USER + " FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' LIMIT " + userSize + " OFFSET " + offset;
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String user = rs.getString(1);
                    WebServer.win.log.echo("Building profile for user " + user);
                    //NootropiaThread uThread = new NootropiaThread(dbAccess, barrier, user, clientName, t0, t);
                    //uThread.start();
                    barrier.down();
                    i++;
                    //System.out.println( "" + i );
                }
                offset += userSize;
                //System.out.println( "i is " + i + " offset " );
            } while (i == userSize);

            while (barrier.getCount() != 0) {
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

    private int genWeights(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execGenWeights(queryParam, respBody, dbAccess);
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

    private boolean execGenWeights(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int baseIdx = queryParam.qpIndexOfKeyNoCase("base");

            String base = null;
            if (baseIdx != -1) {
                base = (String) queryParam.getVal(baseIdx);
                base = base.replace("*", "%");
            }

            int corellationSize = 100000;
            int offset = 0;
            int i = 0;
            String sql = "DELETE FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            dbAccess.execute(sql);
            sql = "DELETE FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY_NORMALIZED;
            dbAccess.execute(sql);
            sql = "DELETE FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL_NORMALIZED;
            dbAccess.execute(sql);

            if (base == null) {
                sql = "SELECT ftr, " + DBAccess.STATISTICS_FREQUENCY + ", sum( f ),'" + clientName + "' FROM ( "
                        + "(SELECT ftr_src ftr, count(*) f FROM "
                        + "user_feature_associations WHERE FK_psclient = '" + clientName + "' GROUP BY ftr "
                        + ") UNION ( "
                        + "SELECT ftr_dst ftr, count(*) f FROM "
                        + "user_feature_associations WHERE FK_psclient = '" + clientName + "' GROUP BY ftr"
                        + ") "
                        + ") AS un_ftr GROUP BY ftr";
            } else {
                sql = "SELECT ftr, " + DBAccess.STATISTICS_FREQUENCY + ", sum( f ),'" + clientName + "' FROM ( "
                        + "(SELECT ftr_src ftr, count(*) f FROM "
                        + "user_feature_associations WHERE FK_psclient = '" + clientName + "' AND ftr_dst LIKE '" + base + "' GROUP BY ftr "
                        + ") UNION ( "
                        + "SELECT ftr_dst ftr, count(*) f FROM "
                        + "user_feature_associations WHERE FK_psclient = '" + clientName + "' AND ftr_src LIKE '" + base + "' GROUP BY ftr"
                        + ") "
                        + ") AS un_ftr GROUP BY ftr";
                //System.out.println( sql );
            }
            sql = "INSERT INTO " + DBAccess.FEATURE_STATISTICS_TABLE + "(" + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "," + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "," + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + "," + DBAccess.FIELD_PSCLIENT + ") "
                    + sql;
            //System.out.println("executing " + sql);
            dbAccess.executeUpdate(sql);
            //System.out.println("executed " + sql);

            if (base == null) {
                sql = "SELECT count(*) FROM " + DBAccess.FEATURE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
            } else {
                sql = "SELECT count(*) FROM " + DBAccess.FEATURE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_TABLE_FIELD_FEATURE + " LIKE '" + base + "'";
            }
            ResultSet tmpRs = dbAccess.executeQuery(sql).getRs();
            tmpRs.next();
            int totalFrequesny = tmpRs.getInt(1);
            sql = "INSERT INTO " + DBAccess.FEATURE_STATISTICS_TABLE + "(" + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "," + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "," + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + "," + DBAccess.FIELD_PSCLIENT + ") "
                    + " SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "," + DBAccess.STATISTICS_FREQUENCY_NORMALIZED + "," + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + "/" + totalFrequesny + "," + DBAccess.FIELD_PSCLIENT + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY;
            //System.out.println("executing " + sql);
            dbAccess.executeUpdate(sql);
            //System.out.println("executed " + sql);

            HashMap<String, Float> featureFrequencies = new HashMap<String, Float>(1000);
            sql = "SELECT " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_FEATURE + "," + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_VALUE + " FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FEATURE_STATISTICS_TABLE_FIELD_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
            ResultSet features = dbAccess.executeQuery(sql).getRs();
            while (features.next()) {
                featureFrequencies.put(features.getString(1), features.getFloat(2));
            }
            features.close();

            do {
                i = 0;
                //System.out.println("offset is " + offset);
                String subSql = "SELECT * FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' LIMIT " + corellationSize + " OFFSET " + offset;
                ResultSet cor = dbAccess.executeQuery(subSql).getRs();
                while (cor.next()) {
                    String ftr1 = cor.getString(DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC);
                    String ftr2 = cor.getString(DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST);
                    float weight = cor.getFloat(DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT);
                    //System.out.println( "ftr1 " + ftr1 + " ftr2 " + ftr2 );
                    Float f1 = featureFrequencies.get(ftr1);
                    Float f2 = featureFrequencies.get(ftr2);
                    if (f1 == null || f2 == null) {
                        continue;
                    }
                    float newWeight = weight * weight / (f1 * f2);
                    String sqlIns = "INSERT DELAYED INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + "(" + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ("
                            + "'" + ftr1 + "','" + ftr2 + "'," + DBAccess.RELATION_PHYSICAL_NORMALIZED + "," + newWeight + ",'" + clientName + "')";
                    dbAccess.executeUpdate(sqlIns);
                    i++;
                }
                cor.close();
                offset += corellationSize;
                System.out.println(offset);
            } while (i == corellationSize);

        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private int genPersWeights(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
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
            success = execGenPersWeights(queryParam, respBody, dbAccess);
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

    private boolean execGenPersWeights(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess) {
        try {
            int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
            String clientName = (String) queryParam.getVal(clntIdx);

            int baseIdx = queryParam.qpIndexOfKeyNoCase("base");

            String base = null;
            if (baseIdx != -1) {
                base = (String) queryParam.getVal(baseIdx);
                base = base.replace("*", "%");
            }

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

            String sql;
            /*String sql = "DELETE FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY + " AND " + DBAccess.FEATURE_STATISTICS_TABLE_USER + "<>''";
             dbAccess.execute(sql);
             sql = "DELETE FROM " + DBAccess.FEATURE_STATISTICS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.FEATURE_STATISTICS_TABLE_TYPE + "=" + DBAccess.STATISTICS_FREQUENCY_NORMALIZED + " AND " + DBAccess.FEATURE_STATISTICS_TABLE_USER + "<>''";
             dbAccess.execute(sql);
             sql = "DELETE FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "=" + DBAccess.RELATION_PHYSICAL_NORMALIZED + " AND " + DBAccess.FEATURE_STATISTICS_TABLE_USER + "<>''";
             dbAccess.execute(sql);
             */
            final int userSize = 1000;
            int offset = 0;
            int idx = 0;
            int threadNum = Integer.parseInt(PersServer.pref.getPref("thread_num"));
            //threadNum = 1;
            NootropiaWeightThread threads[] = new NootropiaWeightThread[threadNum];
            for (int j = 0; j < threadNum; j++) {
                threads[j] = new NootropiaWeightThread();
            }
            Statement stmt = dbAccess.getConnection().createStatement();
            ExecutorService threadExecutor = Executors.newFixedThreadPool(threadNum);
            do {
                idx = 0;
                sql = "SELECT " + DBAccess.USER_TABLE_FIELD_USER + " FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' LIMIT " + userSize + " OFFSET " + offset;
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String user = rs.getString(1);
                    threadExecutor.execute(new NootropiaWeightThread(dbAccess, user, clientName, t0, t));
                    idx++;
                }
                offset += userSize;
            } while (idx == userSize);
            threadExecutor.shutdown();
            while (threadExecutor.isTerminated() == false) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
            }
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}

class NootropiaWeightThread extends Thread {

    private String user;
    private DBAccess dbAccess;
    private String clientName;
    private int s0;
    private int s;

    public NootropiaWeightThread(DBAccess dbAccess, String user, String clientName, int s0, int s) {
        this.dbAccess = dbAccess;
        this.user = user;
        this.clientName = clientName;
        this.s0 = s0;
        this.s = s;
    }

    public NootropiaWeightThread() {
    }

    public void init(DBAccess dbAccess, String user, String clientName, int s0, int s) {
        this.dbAccess = dbAccess;
        this.user = user;
        this.clientName = clientName;
        this.s0 = s0;
        this.s = s;
    }

    @Override
    public void run() {
        WebServer.win.log.echo("Calculating frequencies for user " + user);
        int dcySize = 10000;
        int offset = 0;
        HashMap<String, Integer> freqs = new HashMap<String, Integer>();
        HashMap<Set<String>, Integer> setFreqs = new HashMap<Set<String>, Integer>();
        int i = 0;
        try {
            String stmtSql = "REPLACE DELAYED INTO " + DBAccess.FEATURE_STATISTICS_TABLE + " VALUES('" + user + "',?," + DBAccess.STATISTICS_FREQUENCY + ",?,'" + clientName + "')";
            PreparedStatement freqStmt = dbAccess.getConnection().prepareStatement(stmtSql);
            stmtSql = "REPLACE DELAYED INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + " VALUES( ?,?,?," + DBAccess.RELATION_SIMILARITY + ",'" + user + "','" + clientName + "')";
            PreparedStatement coFreqStmt = dbAccess.getConnection().prepareStatement(stmtSql);

            String sql = "SELECT MIN(convert(" + DBAccess.DECAY_DATA_TABLE_FIELD_SESSION + ", SIGNED)) FROM " + DBAccess.DECAY_DATA_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.DECAY_DATA_TABLE_FIELD_USER + "='" + user + "'";
            ResultSet rs = dbAccess.executeQuery(sql).getRs();
            if (rs.next()) {
                int tmpS0 = rs.getInt(1);
                if (tmpS0 > s0) {
                    s0 = tmpS0;
                }
            }
            rs.close();
            Connection tmpCon = dbAccess.newConnection();
            Statement tmpStmt = tmpCon.createStatement();

            sql = "SELECT " + DBAccess.DECAY_DATA_TABLE_FIELD_USER + "," + DBAccess.DECAY_DATA_TABLE_FIELD_FEATURE + ",convert(" + DBAccess.DECAY_DATA_TABLE_FIELD_SESSION + ", SIGNED),'" + clientName + "' " + " FROM " + DBAccess.DECAY_DATA_TABLE
                    + " WHERE convert(" + DBAccess.DECAY_DATA_TABLE_FIELD_SESSION + ", SIGNED)>=" + s0 + " AND convert(" + DBAccess.DECAY_DATA_TABLE_FIELD_SESSION + ", SIGNED)<=" + s + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.DECAY_DATA_TABLE_FIELD_USER + "='" + user + "'";
            rs = dbAccess.executeQuery(sql).getRs();
            while (rs.next()) {
                ArrayList<String> ftrs = new ArrayList<String>();
                String thisFtr = rs.getString(DBAccess.DECAY_DATA_TABLE_FIELD_FEATURE);
                sql = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "='" + thisFtr + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "=''";
                ResultSet tmpRs = tmpStmt.executeQuery(sql);
                while (tmpRs.next()) {
                    String ftr = tmpRs.getString(1);
                    ftrs.add(ftr);
                    if (freqs.get(ftr) == null) {
                        freqs.put(ftr, 1);
                    } else {
                        freqs.put(ftr, freqs.get(ftr) + 1);
                    }
                }
                tmpRs.close();
                sql = "SELECT " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "='" + thisFtr + "' AND " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "=''";
                tmpRs = tmpStmt.executeQuery(sql);
                while (tmpRs.next()) {
                    String ftr = tmpRs.getString(1);
                    ftrs.add(ftr);
                    if (freqs.get(ftr) == null) {
                        freqs.put(ftr, 1);
                    } else {
                        //System.out.println("existing ftr " + ftr);
                        freqs.put(ftr, freqs.get(ftr) + 1);
                    }
                }
                tmpRs.close();
                for (int k = 0; k < ftrs.size(); k++) {
                    for (int j = k + 1; j < ftrs.size(); j++) {
                        Set<String> pair = new HashSet<String>();
                        pair.add(ftrs.get(k));
                        pair.add(ftrs.get(j));
                        if (setFreqs.get(pair) == null) {
                            setFreqs.put(pair, 1);
                        } else {
                            //System.out.println("existing pair " + pair.toString());
                            setFreqs.put(pair, setFreqs.get(pair) + 1);
                        }
                    }
                }
            }
            rs.close();
            //System.out.println("there are " + freqs.size() + " features and " + setFreqs.size() + " corelations" );
            for (String key : freqs.keySet()) {
                freqStmt.clearParameters();
                freqStmt.setString(1, key);
                freqStmt.setFloat(2, freqs.get(key));
                freqStmt.addBatch();
            }
            for (Set<String> pairs : setFreqs.keySet()) {
                if (setFreqs.get(pairs) == 1) {
                    continue;
                }
                Iterator<String> iter = pairs.iterator();
                String ftr1 = iter.next();
                String ftr2 = iter.next();
                coFreqStmt.clearParameters();
                if (ftr1.compareTo(ftr2) < 0) {
                    coFreqStmt.setString(1, ftr1);
                    coFreqStmt.setString(2, ftr2);
                } else {
                    coFreqStmt.setString(1, ftr2);
                    coFreqStmt.setString(2, ftr1);
                }
                coFreqStmt.setFloat(3, setFreqs.get(pairs));
                //System.out.println( pairs.toString() + " " + setFreqs.get(pairs) );
                coFreqStmt.addBatch();
            }

            freqStmt.executeBatch();
            freqStmt.close();
            coFreqStmt.executeBatch();
            coFreqStmt.close();
            tmpStmt.close();
            setFreqs.clear();
            freqs.clear();
            tmpCon.close();
        } catch (SQLException ex) {
            WebServer.win.log.error(ex.toString());
            ex.printStackTrace();
        }
    }
}
