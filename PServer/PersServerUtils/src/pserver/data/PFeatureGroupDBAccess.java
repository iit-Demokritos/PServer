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
package pserver.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pserver.algorithms.metrics.VectorMetric;
import pserver.domain.PFeatureGroup;
import pserver.domain.PServerVector;

/**
 *
 * @author alexm
 */
public class PFeatureGroupDBAccess {

    private DBAccess dbAccess;
    private Barrier barrier;
    private Statement stmt;

    public PFeatureGroupDBAccess(DBAccess db) throws SQLException {
        dbAccess = db;
        stmt = db.getConnection().createStatement();
    }

    /*
     * Deletes the user Graph of a specific type
     */
    public void deleteFeatureAccociations(String clientName, int relationType) throws SQLException {
        getDbAccess().executeUpdate("DELETE FROM " + DBAccess.UFTRASSOCIATIONS_TABLE
                + " WHERE " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + " = " + relationType
                + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'");
    }

    public void generateBinarySimilarities(DBAccess dbAccess, String clientName, int op, float threashold) throws SQLException {
        String sql;
        if (op == 1) {
            sql = "INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + " SELECT "
                    + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + ", 1, " + DBAccess.RELATION_BINARY_SIMILARITY + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "," + DBAccess.FIELD_PSCLIENT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE "
                    + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + "<" + threashold + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        } else {
            sql = "INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + " SELECT "
                    + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + ", 1, " + DBAccess.RELATION_BINARY_SIMILARITY + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "," + DBAccess.FIELD_PSCLIENT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE "
                    + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + ">" + threashold + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        }
        //System.out.println( "sql = " + sql );
        dbAccess.executeUpdate(sql);
    }

    /**
     *
     * This function returns a Result Set on the top of the Ftr_group table
     *
     * @param whrCondition the condition for the sql query to constrained the
     * results
     * @return the result set
     * @throws java.sql.SQLException
     */
    public PFeatureGroupResultSet getFeatureFroups(String whrCondition) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.FTRGROUPS_TABLE + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery(query);
        PFeatureGroupResultSet result = new PFeatureGroupResultSet(prs.getStmt(), prs.getRs());
        return result;
    }

    public PFeatureGroupProfileResultSet getFeatureGroupProfiles(String whrCondition) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.FTRGROUP_FEATURES_TABLE + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery(query);
        PFeatureGroupProfileResultSet result = new PFeatureGroupProfileResultSet(prs.getStmt(), prs.getRs());
        return result;
    }

    public PFeatureGroupResultSet getFeatureGroups(String whrCondition) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.FTRGROUPS_TABLE + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery(query);
        PFeatureGroupResultSet result = new PFeatureGroupResultSet(prs.getStmt(), prs.getRs());
        return result;
    }

    public DBAccess getDbAccess() {
        return dbAccess;
    }

    public int addFeatureGroup(PFeatureGroup ftrGroup, String clientName) throws SQLException {
        int rows = 0;
        //sabe name
        PreparedStatement stmtAddFtrGroup = this.dbAccess.getConnection().prepareStatement("INSERT INTO " + DBAccess.FTRGROUPS_TABLE + "(" + DBAccess.FTRGROUPS_TABLE_FIELD_FTRGROUP + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ( ?,'" + clientName + "')");
        stmtAddFtrGroup.setString(1, ftrGroup.getName());
        rows += stmtAddFtrGroup.executeUpdate();
        stmtAddFtrGroup.close();

        //save features
        PreparedStatement stmtAddFeatures = this.dbAccess.getConnection().prepareStatement("INSERT INTO " + DBAccess.FTRGROUPSFTRS_TABLE + "(" + DBAccess.FTRGROUPSFTRS_TABLE_FIELD_GROUP + "," + DBAccess.FTRGROUPSFTRS_TABLE_TABLE_FIELD_FTR + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ( ?,?, '" + clientName + "')");
        stmtAddFeatures.setString(1, ftrGroup.getName());
        for (String ftr : ftrGroup.getFeatures()) {

            stmtAddFeatures.setString(2, ftr);
            stmtAddFeatures.addBatch();
        }

        int[] r = stmtAddFeatures.executeBatch();
        for (int i = 0; i < r.length; i++) {
            rows += r[ i];
        }
        stmtAddFeatures.close();

        //save users
        PreparedStatement stmtAddUsers = this.dbAccess.getConnection().prepareStatement("INSERT INTO " + DBAccess.FTRGROUPSUSERS_TABLE + "(" + DBAccess.FTRGROUPSUSERS_TABLE_FIELD_GROUP + "," + DBAccess.FTRGROUPSUSERS_TABLE_FIELD_USER + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ( ?,?, '" + clientName + "')");
        stmtAddUsers.setString(1, ftrGroup.getName());
        for (String usr : ftrGroup.getUsers()) {
            stmtAddUsers.setString(2, usr);
            stmtAddUsers.addBatch();
        }

        r = stmtAddUsers.executeBatch();
        for (int i = 0; i < r.length; i++) {
            rows += r[ i];
        }
        stmtAddUsers.close();

        return rows;
    }

    public void generateFtrDistances(String clientName, VectorMetric metric,
            int dataRelationType, int numOfThreads) throws SQLException {
        
        System.out.println("Generating Feature distances");

        // TODO: Add system parameter for step S
        // Fetch all feature names
        String sql = "SELECT DISTINCT " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE
                + " FROM " + DBAccess.UPROFILE_TABLE
                + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        Statement stmt = dbAccess.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        // Store in features list all the feature name fetched
        ArrayList<String> features = new ArrayList<String>();
        while (rs.next()) {
            features.add(rs.getString(1));
        }
        rs.close();
        stmt.close();

        System.out.println("Feature loaded");

        PFeatureDBAccess pfdb = new PFeatureDBAccess(dbAccess);

        System.out.println("NumOfThreads= " + numOfThreads);
        ExecutorService threadExecutor = Executors.newFixedThreadPool(numOfThreads);
        final int STEP_SIZE = 1000;
        int count = 1;
        // For every S features
        for (int i = 0; i < features.size(); i += STEP_SIZE) {
            //init frame end poind
            int endpoindA = i + STEP_SIZE;
            if (endpoindA > features.size()) {
                endpoindA = features.size();
            }

            //Add the features of the current frame in list
            ArrayList<String> cFeatureFrameA = new ArrayList<String>(
                    features.subList(i, endpoindA));
            // Get feature profiles to compare
            ArrayList<PServerVector> FrameAFeatureVectors = new ArrayList<PServerVector>();
            for (String tmpfeature : cFeatureFrameA) {
                FrameAFeatureVectors.add(pfdb.getFeatureVector(tmpfeature, clientName, true));
            }

            // For every S feature after F, init a list H
            //for each feature in the frame
            for (String cfeature : cFeatureFrameA) {
                System.out.println(count++);
                //init a list with features after them
                ArrayList<PServerVector> CompFeatureProfiles = new ArrayList<PServerVector>(
                        FrameAFeatureVectors.subList(cFeatureFrameA.indexOf(cfeature) + 1,
                                FrameAFeatureVectors.size()));

                if (endpoindA == features.size()) {
                    // Calculate the distance between F and each item of the sublist
                    // and Store results
                    makeFeatureDistances(FrameAFeatureVectors.get(cFeatureFrameA.indexOf(cfeature)),
                            CompFeatureProfiles, dataRelationType, clientName, metric);
                }
                //???? Split the list of H into e.g. 4 (# of threads) sublists???
                // from the FrameB set start point the endpoint of the 
                //FrameA and move it by step size until the end of features list
                // For every sublist
                for (int j = endpoindA; j < features.size(); j += STEP_SIZE) {
                    int endpoindB = j + STEP_SIZE;
                    if (endpoindB > features.size()) {
                        endpoindB = features.size();
                    }

                    //Add the feature of the current frameB in list
                    ArrayList<String> cFeatureFrameB = new ArrayList<String>(
                            features.subList(j, endpoindB));
                    // add features profiles from FameB to compare
                    for (String tmpfeature : cFeatureFrameB) {
                        CompFeatureProfiles.add(pfdb.getFeatureVector(tmpfeature, clientName, true));
                    }

                    // Calculate the distance between F and each item of the sublist
                    // and Store results
                    makeFeatureDistances(FrameAFeatureVectors.get(cFeatureFrameA.indexOf(cfeature)),
                            CompFeatureProfiles, dataRelationType, clientName, metric);
                    CompFeatureProfiles.clear();

                }

                // Wait for thread termination
                dbAccess.commit();
            } // end for every feature in the frame

        } // end for every S features

    }

    public void generateFtrDistancesOLD(String clientName, VectorMetric metric,
            int dataRelationType, int numOfThreads) throws SQLException {

        System.out.println("Generating Feature distances");
        String sql = "SELECT DISTINCT " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + " FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        Statement stmt = dbAccess.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        ArrayList<String> features = new ArrayList<String>(100);
        while (rs.next()) {
            features.add(rs.getString(1));
        }
        rs.close();
        stmt.close();

        System.out.println("Feature loaded");

        PFeatureDBAccess pfdb = new PFeatureDBAccess(dbAccess);

        ExecutorService threadExecutor = Executors.newFixedThreadPool(numOfThreads);

        long to = System.currentTimeMillis();
        ArrayList<PServerVector> ftrVectors = new ArrayList<PServerVector>(features.size());
        System.out.println("Calculations just started");
        int counter = 0;
        long time = System.currentTimeMillis();
        for (int i = 0; i < features.size(); i++) {
            String featureName = features.get(i);
            long totalFree = Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
            System.out.println(i + " vectors loaded");
            if (totalFree >= 54000000) {
                PServerVector fvector = pfdb.getFeatureVector(featureName, clientName, false);
                ftrVectors.add(fvector);
            } else {
                makeUserDistancesOriginal(ftrVectors, features, i, threadExecutor, dataRelationType, clientName, metric, pfdb);
                ftrVectors.clear();
                PServerVector fvector = pfdb.getFeatureVector(featureName, clientName, false);
                ftrVectors.add(fvector);
                counter++;
            }
        }
        System.out.println("loading Time is " + (System.currentTimeMillis() - time + " for " + ftrVectors.size() + " vectors"));
        if (ftrVectors.size() > 0) {
            makeUserDistancesOriginal(ftrVectors, features, features.size(), threadExecutor, dataRelationType, clientName, metric, pfdb);
        }

    }

    private void makeFeatureDistances(PServerVector feature1,
            ArrayList<PServerVector> CompareWithFeatures, int dataRelationType,
            String clientName, VectorMetric metric) throws SQLException {

        Statement stmt = getDbAccess().getConnection().createStatement();
        // For each feature in the CompareWithFeatures do comparison
        for (PServerVector feature2 : CompareWithFeatures) {
            //Get distance between feature1 and feature2
            float dist = metric.getDistance(feature1, feature2);
            if (Float.isNaN(dist)) {
                continue;
            }
            //save distance to DB
            saveFeatureSimilarity(feature1, feature2, dist, clientName,
                    dataRelationType, stmt);
        }
        stmt.close();
    }

    public void saveFeatureSimilarity(PServerVector feature1, PServerVector feature2,
            float dist, String clientName, int dataRelationType, Statement stmt) throws SQLException {
//        Statement stmt = getDbAccess().getConnection().createStatement();
        String sql = "INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE
                + "(" + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + ","
                + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + ","
                + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + ","
                + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + ","
                + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + ","
                + DBAccess.FIELD_PSCLIENT + ") VALUES ('"
                + feature1.getName() + "','"
                + feature2.getName() + "',"
                + dist + ","
                + dataRelationType + ",'"
                + clientName + "','"
                + clientName + "')";
        stmt.executeUpdate(sql);
//        stmt.close();
    }

    private void makeUserDistancesOriginal(ArrayList<PServerVector> ftrVectors, ArrayList<String> features, int ftrPos, ExecutorService threadExecutor, int dataRelationType, String clientName, VectorMetric metric, PFeatureDBAccess pfdb) throws SQLException {
        long memoryTime;
        long batchTime = System.currentTimeMillis();
        System.out.println("Calculatining distances for " + ftrVectors.size() + " features ");
        String sql = "INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE 
                + "(" + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," 
                + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "," 
                + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + "," 
                + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "," 
                + DBAccess.FIELD_PSCLIENT + ") VALUES (?, ?,?," + dataRelationType + ",'" + clientName + "')";

        //StringBuilder sqlsb = new StringBuilder();
        //sqlsb.append("INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + "(" + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ");
        final int totalBatchSize = 50000;

        PreparedStatement sstmt = getDbAccess().getConnection().prepareStatement(sql);
        //Statement sstmt = getDbAccess().getConnection().createStatement();        
        int batchSize = 0;
        for (int i = 0; i < ftrVectors.size(); i++) {
            PServerVector target = ftrVectors.get(i);
            long t = System.currentTimeMillis();
            for (int j = i + 1; j < ftrVectors.size(); j++) {
                PServerVector comparWith = ftrVectors.get(j);
                if (addFeatureSimilarity(sstmt, target, comparWith, metric, clientName, dataRelationType)) {
                    batchSize++;
                }

                if (batchSize == totalBatchSize) {
                    //long totalFree = Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();                    
                    //sqlsb = new StringBuilder();
                    //sqlsb.append("INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + "(" + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ");
                    sstmt.executeBatch();
                    sstmt.clearBatch();
                    batchSize = 0;
                }
                //threadExecutor.execute(new CompareThread(clientName, metric, dataRelationType, target, comparWith));
            }
            memoryTime = (System.currentTimeMillis() - t);
            System.out.println("memory time for " + target.getName() + " = " + memoryTime);
        }

        for (int j = ftrPos; j < features.size(); j++) {
            long totalFree = Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
            long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            //System.out.println(j + " used memory " + usedMemory);            
            PServerVector comparWith = pfdb.getFeatureVector(features.get(j), clientName, false);
            for (int i = 0; i < ftrVectors.size(); i++) {
                PServerVector target = ftrVectors.get(i);
                if (addFeatureSimilarity(sstmt, target, comparWith, metric, clientName, dataRelationType)) {
                    batchSize++;
                }
                if (batchSize == totalBatchSize) {
                    sstmt.executeBatch();
                    sstmt.clearBatch();
                    batchSize = 0;
                }
            }
        }
        if (batchSize > 0) {
            sstmt.executeBatch();
            sstmt.clearBatch();
        }
        sstmt.close();

        System.out.println("Elapsed time for " + ftrVectors.size() + " user is " + (System.currentTimeMillis() - batchTime));
    }

    private boolean addFeatureSimilarity(PreparedStatement sstmt, PServerVector vec1, PServerVector vec2, VectorMetric metric, String clientName, int dataRelationType) throws SQLException {
        float dist = metric.getDistance(vec1, vec2);
        if (Math.abs(dist - metric.getMinimumCoefficientValue()) < 0.001) {
            return false;
        }
        //System.out.println( vec1.getName() + "---" + vec2.getName() + " -- " + dist );        
        //sqlsb.append("('").append(vec1.getName()).append("', '").append(vec2.getName()).append("',").append(dist).append(",").append(dataRelationType).append(",'").append(clientName).append("')");        
        sstmt.setString(1, vec1.getName());
        sstmt.setString(2, vec2.getName());
        sstmt.setFloat(3, dist);
        sstmt.addBatch();
        return true;
    }

    /**
     * Removes a feature group from the database
     *
     * @param groupName
     * @param clientName
     * @return
     * @throws SQLException
     */
    public int remove(String groupName, String clientName) throws SQLException {
        int rows = 0;

        PreparedStatement stmt = this.getDbAccess().getConnection().prepareStatement("DELETE FROM " + DBAccess.FTRGROUPSFTRS_TABLE + " WHERE " + DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP + "=? AND " + DBAccess.FIELD_PSCLIENT + "=?");
        stmt.setString(1, groupName);
        stmt.setString(2, clientName);
        rows += stmt.executeUpdate();
        stmt.close();

        stmt = this.getDbAccess().getConnection().prepareStatement("DELETE FROM " + DBAccess.FTRGROUPSUSERS_TABLE + " WHERE " + DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP + "=? AND " + DBAccess.FIELD_PSCLIENT + "=?");
        stmt.setString(1, groupName);
        stmt.setString(2, clientName);
        rows += stmt.executeUpdate();
        stmt.close();

        stmt = this.getDbAccess().getConnection().prepareStatement("DELETE FROM " + DBAccess.FTRGROUPS_TABLE + " WHERE " + DBAccess.FTRGROUPS_TABLE_FIELD_FTRGROUP + "=? AND " + DBAccess.FIELD_PSCLIENT + "=?");
        stmt.setString(1, groupName);
        stmt.setString(2, clientName);
        rows += stmt.executeUpdate();
        stmt.close();

        return rows;
    }

    class CompareThread extends Thread {

        private String clientName;
        private VectorMetric metric;
        private int dataRelationType;
        private int offset;
        private int limit;
        private SQLException exception = null;
        private PServerVector vector1;
        private PServerVector vector2;
        private PreparedStatement sstmt;

        private CompareThread(String clientName, VectorMetric metric, int dataRelationType, PServerVector target, PServerVector comparWith, PreparedStatement sstmt) {
            this.clientName = clientName;
            this.metric = metric;
            this.dataRelationType = dataRelationType;
            this.vector1 = target;
            this.vector2 = comparWith;
            this.sstmt = sstmt;
        }

        @Override
        public void run() {
            /*try {
             //addFeatureSimilarity(sstmt, vector1, vector2, metric, clientName, dataRelationType);
             } catch (SQLException ex) {
             exception = ex;
             }*/
        }

        /**
         * @return the exception
         */
        public SQLException getException() {
            return exception;
        }
    }
}
