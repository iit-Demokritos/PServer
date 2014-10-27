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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import pserver.algorithms.metrics.VectorMetric;
import pserver.domain.PCommunity;
import pserver.domain.PUser;

/**
 *
 * @author alexm
 */
public class PCommunityDBAccess {

    private DBAccess dbAccess;
    private Barrier barrier;

    public PCommunityDBAccess(DBAccess db) throws SQLException {
        dbAccess = db;

    }
    /*
     * Deletes the user Graph of a specific type
     */

    public void deleteUserAccociations(String clientName, int relationType) throws SQLException {
        getDbAccess().executeUpdate("DELETE FROM " + DBAccess.UASSOCIATIONS_TABLE 
                + " WHERE " + DBAccess.UASSOCIATIONS_TABLE_FIELD_TYPE + " = " + relationType 
                + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'");
    }

    public void generateBinarySimilarities(DBAccess dbAccess, String clientName, int op, float threashold) throws SQLException {
        String sql;
        if (op == 1) {
            sql = "INSERT INTO " + DBAccess.UASSOCIATIONS_TABLE + " SELECT "
                    + DBAccess.UASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UASSOCIATIONS_TABLE_FIELD_DST + ", " + DBAccess.UASSOCIATIONS_TABLE_FIELD_WEIGHT + ", " + DBAccess.RELATION_BINARY_SIMILARITY + "," + DBAccess.FIELD_PSCLIENT + " FROM " + DBAccess.UASSOCIATIONS_TABLE + " WHERE "
                    + DBAccess.UASSOCIATIONS_TABLE_FIELD_WEIGHT + "<" + threashold + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        } else {
            sql = "INSERT INTO " + DBAccess.UASSOCIATIONS_TABLE + " SELECT "
                    + DBAccess.UASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UASSOCIATIONS_TABLE_FIELD_DST + ", " + DBAccess.UASSOCIATIONS_TABLE_FIELD_WEIGHT + ", " + DBAccess.RELATION_BINARY_SIMILARITY + "," + DBAccess.FIELD_PSCLIENT + " FROM " + DBAccess.UASSOCIATIONS_TABLE + " WHERE "
                    + DBAccess.UASSOCIATIONS_TABLE_FIELD_WEIGHT + ">" + threashold + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        }
        //System.out.println( "sql = " + sql );
        dbAccess.executeUpdate(sql);
    }

    /**
     *
     * This function returns a Result Set on the top of the Communities table
     *
     * @param whrCondition the condition for the sql query to constrained the
     * results
     * @return the result set
     * @throws java.sql.SQLException
     */
    public PCommunityResultSet getCommunities(String whrCondition) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.COMMUNITIES_TABLE + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery(query);
        PCommunityResultSet result = new PCommunityResultSet(prs.getStmt(), prs.getRs());
        return result;
    }

    public PCommunityProfileResultSet getCommunityProfiles(String whrCondition) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.COMMUNITY_PROFILES_TABLE + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery(query);
        PCommunityProfileResultSet result = new PCommunityProfileResultSet(prs.getStmt(), prs.getRs());
        return result;
    }

    public void generateUserDistances(String clientName, VectorMetric metric,
            int dataRelationType, int numOfThreads, String features) throws SQLException {
        String ftrs[] = null;
        if (features != null) {
            features = features.replace("*", "%");
            ftrs = features.split("\\|");
        }

        // TODO: Add system parameter for step S
        // Fetch all user IDs
        String sql = "SELECT " + DBAccess.FIELD_PSUSERID + " FROM "
                + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT
                + "='" + clientName + "'";
        Statement stmt = dbAccess.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        // Store in users list all the IDs fetched
        ArrayList<String> users = new ArrayList<String>();
        while (rs.next()) {
            users.add(rs.getString(1));
        }
        rs.close();
        stmt.close();
        users.remove("gprofile");
        
        PUserDBAccess pudb = new PUserDBAccess(dbAccess);
        System.out.println("NumOfThreads= "+numOfThreads);
        ExecutorService threadExecutor = Executors.newFixedThreadPool(numOfThreads);
        final int STEP_SIZE = 1000;
        int count = 1;
        // For every S users
        for (int i = 0; i < users.size(); i += STEP_SIZE) {
            //init frame end poind
            int endpoindA = i + STEP_SIZE;
            if (endpoindA > users.size()) {
                endpoindA = users.size();
            }

            //Add the users of the current frame in list
            ArrayList<String> cUserFrameA = new ArrayList<String>(
                    users.subList(i, endpoindA));
            // Get user profiles to compare
            ArrayList<PUser> FrameAUsersProfiles = new ArrayList<PUser>();
            for (String tmpuser : cUserFrameA) {
                FrameAUsersProfiles.add(pudb.getUserProfile(tmpuser, ftrs, clientName, false));
            }

            // For every S users after F, init a list H
            //for each user in the frame
            for (String cUser : cUserFrameA) {
                System.out.println(count++);
                //init a list with users after them
//                ArrayList<String> CompareUserList = new ArrayList<String>(
//                        users.subList(users.indexOf(cUser) + 1, users.size()));
                ArrayList<PUser> CompUsersProfiles = new ArrayList<PUser>(
                        FrameAUsersProfiles.subList(cUserFrameA.indexOf(cUser) + 1,
                        FrameAUsersProfiles.size()));

                if (endpoindA == users.size()) {
                    // Calculate the distance between F and each item of the sublist
                    // and Store results
                    makeUserDistances(FrameAUsersProfiles.get(cUserFrameA.indexOf(cUser)),
                            CompUsersProfiles, dataRelationType, clientName, metric);
                }
//???? Split the list of H into e.g. 4 (# of threads) sublists???
                // from the FrameB set start point the endpoint of the 
                //FrameA and move it by step size until the end of users list
                // For every sublist
                for (int j = endpoindA; j < users.size(); j += STEP_SIZE) {
                    int endpoindB = j + STEP_SIZE;
                    if (endpoindB > users.size()) {
                        endpoindB = users.size();
                    }

                    //Add the users of the current frameB in list
                    ArrayList<String> cUserFrameB = new ArrayList<String>(
                            users.subList(j, endpoindB));
                    // add users profiles from FameB to compare
                    for (String tmpuser : cUserFrameB) {
                        CompUsersProfiles.add(pudb.getUserProfile(tmpuser, ftrs, clientName, false));
                    }

                    // Calculate the distance between F and each item of the sublist
                    // and Store results
                    makeUserDistances(FrameAUsersProfiles.get(cUserFrameA.indexOf(cUser)),
                            CompUsersProfiles, dataRelationType, clientName, metric);
                    CompUsersProfiles.clear();

                }

                // Wait for thread termination
                dbAccess.commit();
            } // end for every user in the frame

        } // end for every S users

    }

    public void generateUserDistancesORIGINAL(String clientName, VectorMetric metric, int dataRelationType, int numOfThreads, String features) throws SQLException {
        String ftrs[] = null;
        if (features != null) {
            features = features.replace("*", "%");
            ftrs = features.split("\\|");
        }

        // TODO: Do stepwise (in K-user steps)
        String sql = "SELECT * FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        Statement stmt = dbAccess.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        ArrayList<String> users = new ArrayList<String>(100);
        while (rs.next()) {
            users.add(rs.getString(1));
        }
        rs.close();
        stmt.close();

        PUserDBAccess pudb = new PUserDBAccess(dbAccess);

        ExecutorService threadExecutor = Executors.newFixedThreadPool(numOfThreads);

        long to = System.currentTimeMillis();
        ArrayList<PUser> pusers = new ArrayList<PUser>(users.size());
        int counter = 0;
        // TODO: Remove hard-coded memory limitations
        for (int i = 0; i < users.size(); i++) {
            String userName1 = users.get(i);
            long totalFree = Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
            long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            double memoryratio = (0.0 + usedMemory) / Runtime.getRuntime().maxMemory();
            //System.out.println("Free memory " + (Runtime.getRuntime().freeMemory() / 1048576.0) + "MB used memory " + (usedMemory / 1048576.0) + "MB memory ratio " + memoryratio + " obj num " + pusers.size());
            //if (memoryratio > 0.2) {
            //System.out.println("total free " + totalFree + " max " +  Runtime.getRuntime().maxMemory() + " used " + usedMemory );
            if (totalFree >= 104857600) {
                PUser user1 = pudb.getUserProfile(userName1, ftrs, clientName, false);
                pusers.add(user1);
            } else {
//                makeUserDistances(pusers, users, i, threadExecutor, dataRelationType, clientName, metric, ftrs, pudb);
                pusers.clear();
                PUser user1 = pudb.getUserProfile(userName1, ftrs, clientName, false);
                pusers.add(user1);
                counter++;
            }
            /*
             long t = System.currentTimeMillis();
             long totalT = 0;            /*
             for (int j = i + 1; j < users.size(); j++) {
             String userName2 = users.get(j);
             PUser user2 = pudb.getUserProfile(userName2, ftrs, clientName, false);
             long t2 = System.currentTimeMillis();                
             totalT += System.currentTimeMillis() - t2;                
             threadExecutor.execute(new UserCompareThread(clientName, metric, dataRelationType, user1, user2));
             }/
             System.out.println("time for " + userName1 + " = " + (System.currentTimeMillis() - t) + " average profile loading time = " + (totalT * 1.0 / users.size()));*/
        }
        // TODO: Check if the following line should be commented
//        makeUserDistances(pusers, users, users.size(), threadExecutor, dataRelationType, clientName, metric, ftrs, pudb);

        threadExecutor.shutdown();
        System.out.println("total time " + ((System.currentTimeMillis() - to) / 60000.0));
        System.out.println("counter " + counter);
        while (threadExecutor.isTerminated() == false) {
            try {
                threadExecutor.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
            }
        }
    }

    public void generateBinaryUserRelations(String clientName, int fromRelationType, int newType, float threasHold) throws SQLException {
        Statement stmt = getDbAccess().getConnection().createStatement();
        stmt.close();
    }

    /**
     * @return the dbAccess
     */
    public DBAccess getDbAccess() {
        return dbAccess;
    }

    public int addNewPCommunity(PCommunity community, String clientName) throws SQLException {
        /*int rows = 0;
         /* //sabe name
         PreparedStatement stmtAddFtrGroup = this.dbAccess.getConnection().prepareStatement("INSERT INTO " + DBAccess.UCOMMUNITY_TABLE+ "(" + DBAccess.UCOMMUNITY_TABLE_FIELD_USER + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ( ?,'" + clientName + "')");
         stmtAddFtrGroup.setString(1, community.getName());
         rows += stmtAddFtrGroup.executeUpdate();
         stmtAddFtrGroup.close();
        
         //save features
         PreparedStatement stmtAddUsers = this.dbAccess.getConnection().prepareStatement("INSERT INTO " + DBAccess.FTRGROUPSFTRS_TABLE + "(" + DBAccess.FTRGROUPSFTRS_TABLE_FIELD_GROUP + "," + DBAccess.FTRGROUPSFTRS_TABLE_TABLE_FIELD_FTR + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ( ?,?, '" + clientName + "')");
         stmtAddFeatures.setString(1, ftrGroup.getName());
         for( String ftr : ftrGroup.getFeatures() ) {
        
         stmtAddFeatures.setString(2, ftr );
         stmtAddFeatures.addBatch();
         }
        
         int[] r= stmtAddFeatures.executeBatch();
         for ( int i = 0; i < r.length ; i ++ ) {
         rows += r[ i];
         }
         stmtAddFeatures.close();
        
        
         r= stmtAddUsers.executeBatch();
         for ( int i = 0; i < r.length ; i ++ ) {
         rows += r[ i];
         }
         stmtAddUsers.close();
         */
        return 0;
    }

    /**
     * Get distances between user profiles and store the data in DB
     *
     * @param user1 The user profile of the first user
     * @param CompareWithUsers A list with users profiles to compare the first
     * user
     * @param dataRelationType data relation type
     * @param clientName the current client name
     * @param metric metric for calculating the distance
     * @throws SQLException
     */
    private void makeUserDistances(PUser user1, ArrayList<PUser> CompareWithUsers, int dataRelationType, String clientName, VectorMetric metric) throws SQLException {
        Statement stmt = getDbAccess().getConnection().createStatement();
        // For each user in the CompareWithUsers do comparison
        for (PUser user2 : CompareWithUsers) {
            //Get distance between user1 and user2
            float dist = metric.getDistance(user1.getVector(), user2.getVector());
            if(Float.isNaN(dist))continue;
            //save distance to DB
            saveUserSimilarity(user1, user2, dist, clientName, dataRelationType, stmt);
        }
        stmt.close();
    }

    private void makeUserDistancesOriginal(ArrayList<PUser> pusers, ArrayList<String> users, int uPos, ExecutorService threadExecutor, int dataRelationType, String clientName, VectorMetric metric, String ftrs[], PUserDBAccess pudb) throws SQLException {
        long memoryTime;
        long batchTime = System.currentTimeMillis();
        // For each user fetched
        for (int i = 0; i < pusers.size(); i++) {
            //System.out.println("Calculatining distances for " + pusers.size() + " users ");

            // Get its profile
            PUser target = pusers.get(i);
            long t = System.currentTimeMillis();

            // For each user following the current
            for (int j = i + 1; j < pusers.size(); j++) {
                // Got to whom we must compare
                PUser comparWith = pusers.get(j);
                // Queue comparison to executor
//                threadExecutor.execute(new UserCompareThread(clientName, metric, dataRelationType, target, comparWith));
            }
            memoryTime = (System.currentTimeMillis() - t);
            //System.out.println("memory time for " + target.getName() + " = " + memoryTime);
            //System.out.println("name 1 " + pusers.get(pusers.size() - 1).getName() + " next " + users.get(uPos));            
        }

        // At this point we have compared to ALL the users fetched
        // For ALL the users after the current block fetched
        for (int j = uPos; j < users.size(); j++) {
//            long totalFree = Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
//            long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            //System.out.println(j + " used memory " + usedMemory);
            PUser comparWith = pudb.getUserProfile(users.get(j), ftrs, clientName, false);
            for (int i = 0; i < pusers.size(); i++) {
                PUser target = pusers.get(i);
//                saveUserSimilarity(target, comparWith, metric, clientName, dataRelationType);
            }
        }
        //System.out.println("Elapsed time for " + pusers.size() + " user is " + (System.currentTimeMillis() - batchTime));
    }

    public void saveUserSimilarity(PUser user1, PUser user2, float dist, String clientName, int dataRelationType, Statement stmt) throws SQLException {
//        Statement stmt = getDbAccess().getConnection().createStatement();
        String sql = "INSERT INTO " + DBAccess.UASSOCIATIONS_TABLE + "(" + DBAccess.UASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UASSOCIATIONS_TABLE_FIELD_DST + "," + DBAccess.UASSOCIATIONS_TABLE_FIELD_WEIGHT + "," + DBAccess.UASSOCIATIONS_TABLE_FIELD_TYPE + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ('" + user1.getName() + "','" + user2.getName() + "'," + dist + "," + dataRelationType + ",'" + clientName + "')";
        stmt.executeUpdate(sql);
//        stmt.close();
    }

//    class UserCompareThread extends Thread {
//
//        String clientName;
//        VectorMetric metric;
//        int dataRelationType;
//        int offset;
//        int limit;
//        private SQLException exception = null;
//        private String[] ftrs;
//        private final PUser user1;
//        private final PUser user2;
//
//        public UserCompareThread(String clientName, VectorMetric metric, int dataRelationType, PUser user1, PUser user2) {
//            //System.out.println( "Thread and i have offset " + offset + " and limit " + limit );
//            this.clientName = clientName;
//            this.metric = metric;
//            this.dataRelationType = dataRelationType;
//            this.user1 = user1;
//            this.user2 = user2;
//        }
//
//        @Override
//        public void run() {
//            try {
//                saveUserSimilarity(user1, user2, metric, clientName, dataRelationType);
//            } catch (SQLException ex) {
//                Logger.getLogger(PCommunityDBAccess.class.getName()).log(Level.SEVERE, null, ex);
//                exception = ex;
//            }
//        }
//
//        /**
//         * @return the exception
//         */
//        public SQLException getException() {
//            return exception;
//        }
//    }
}
