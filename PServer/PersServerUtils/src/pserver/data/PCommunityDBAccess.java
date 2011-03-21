/*
 * This class is a decorator on the top of the DBAccess class
 * MiningDBAccess provides methods to access data that are needed
 * for data mining algorithms
 * 
 */
package pserver.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import pserver.algorithms.metrics.VectorMetric;
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
        getDbAccess().executeUpdate("DELETE FROM " + DBAccess.UASSOCIATIONS_TABLE + " WHERE " + DBAccess.UASSOCIATIONS_TABLE_FIELD_TYPE + " = "
                + relationType + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'");
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
     * @param whrCondition the condition for the sql query to constrained the results
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

    public PFeatureGroupResultSet getFeatureGroups(String whrCondition) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.FTRGROUPS_TABLE + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery(query);
        PFeatureGroupResultSet result = new PFeatureGroupResultSet(prs.getStmt(), prs.getRs());
        return result;
    }

    public PFeatureGroupProfileResultSet getFeatureGroupProfiles(String whrCondition) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.FTRGROUP_FEATURES_TABLE + " " + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery(query);
        PFeatureGroupProfileResultSet result = new PFeatureGroupProfileResultSet(prs.getStmt(), prs.getRs());
        return result;
    }

    public void generateUserDistances(String clientName, VectorMetric metric, int dataStorageType, int numOfThreads, String features) throws SQLException {
        features = features.replace("*", "%");
        String ftrs[] = null;
        if (features != null) {
            ftrs = features.split("\\|");
        }

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

        for (int i = 0; i < users.size(); i++) {
            String userName1 = users.get(i);
            System.out.println("Calculatining distances for user " + userName1 );
            PUser user1 = pudb.getUserProfile(userName1, ftrs, clientName);
            for (int j = i + 1; j < users.size(); j++) {                
                String userName2 = users.get(j);                
                PUser user2 = pudb.getUserProfile(userName2, ftrs, clientName);                
                threadExecutor.execute(new CompareThread(clientName, metric, dataStorageType, user1, user2));
            }
        }
        threadExecutor.shutdown();
        while (threadExecutor.isTerminated() == false) {
            try {
                Thread.sleep(5000);
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

    class CompareThread extends Thread {

        String clientName;
        VectorMetric metric;
        int dataStorageType;
        int offset;
        int limit;
        private SQLException exception = null;
        private String[] ftrs;
        private final PUser user1;
        private final PUser user2;

        public CompareThread(String clientName, VectorMetric metric, int dataStorageType, PUser user1, PUser user2) {
            //System.out.println( "Thread and i have offset " + offset + " and limit " + limit );
            this.clientName = clientName;
            this.metric = metric;
            this.dataStorageType = dataStorageType;
            this.user1 = user1;
            this.user2 = user2;
        }

        @Override
        public void run() {
            try {
                float dist = metric.getDistance(user1, user2);
                Statement stmt = getDbAccess().getConnection().createStatement();
                int i = 0;
                stmt.executeUpdate("INSERT INTO " + DBAccess.UASSOCIATIONS_TABLE + "(" + DBAccess.UASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UASSOCIATIONS_TABLE_FIELD_DST + "," + DBAccess.UASSOCIATIONS_TABLE_FIELD_WEIGHT + "," + DBAccess.UASSOCIATIONS_TABLE_FIELD_TYPE + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ('" + user1.getName() + "','" + user2.getName() + "'," + dist + "," + dataStorageType + ",'" + clientName + "')");
                stmt.close();

            } catch (SQLException ex) {
                Logger.getLogger(PCommunityDBAccess.class.getName()).log(Level.SEVERE, null, ex);
                exception = ex;
            }
        }

        /**
         * @return the exception
         */
        public SQLException getException() {
            return exception;
        }
    }
}
