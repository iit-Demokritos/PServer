package pserver.data;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import pserver.algorithms.metrics.VectorMetric;
import pserver.domain.PFeature;
import pserver.domain.PUser;

/**
 *
 * @author alexm
 */
public class PFeatureGroupDBAccess {

    private DBAccess dbAccess;
    private Barrier barrier;
    private Statement stmt;


    public PFeatureGroupDBAccess( DBAccess db ) throws SQLException {
        dbAccess = db;
        stmt = db.getConnection().createStatement();
    }

    /*
     * Deletes the user Graph of a specific type
     */
    public void deleteFeatureAccociations( String clientName, int relationType ) throws SQLException {
        getDbAccess().executeUpdate( "DELETE FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + " = " +
                relationType + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'" );
    }

    public void generateBinarySimilarities( DBAccess dbAccess, String clientName, int op, float threashold ) throws SQLException {
        String sql;
        if( op == 1 )  {
            sql = "INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + " SELECT " +
                    DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + ", 1, " + DBAccess.RELATION_BINARY_SIMILARITY + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "," + DBAccess.FIELD_PSCLIENT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " +
                    DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + "<" + threashold + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        } else {
            sql = "INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + " SELECT " +
                    DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + ", 1, " + DBAccess.RELATION_BINARY_SIMILARITY + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_USR + "," + DBAccess.FIELD_PSCLIENT + " FROM " + DBAccess.UFTRASSOCIATIONS_TABLE + " WHERE " +
                    DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + ">" + threashold + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        }
        //System.out.println( "sql = " + sql );
        dbAccess.executeUpdate( sql );
    }

    /**
     *
     * This function returns a Result Set on the top of the Ftr_group table
     *
     * @param whrCondition the condition for the sql query to constrained the results
     * @return the result set
     * @throws java.sql.SQLException
     */
    public PFeatureGroupResultSet getFeatureFroups( String whrCondition ) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.FTRGROUPS_TABLE + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery( query );
        PFeatureGroupResultSet result = new PFeatureGroupResultSet( prs.getStmt(), prs.getRs() );
        return result;
    }

    public PFeatureGroupProfileResultSet getFeatureGroupProfiles( String whrCondition ) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.FTRGROUP_FEATURES_TABLE + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery( query );
        PFeatureGroupProfileResultSet result = new PFeatureGroupProfileResultSet( prs.getStmt(), prs.getRs() );
        return result;
    }

    public PFeatureGroupResultSet getFeatureGroups( String whrCondition ) throws SQLException {
        String query = "SELECT * FROM " + DBAccess.FTRGROUPS_TABLE + whrCondition;
        PServerResultSet prs = getDbAccess().executeQuery( query );
        PFeatureGroupResultSet result = new PFeatureGroupResultSet( prs.getStmt(), prs.getRs() );
        return result;
    }

    public void generateFtrDistances( String clientName, VectorMetric metric, int dataStorageType, int numOfThreads ) throws SQLException {

        if ( numOfThreads == 1 ) {
            PFeatureResultSet userRs = new PFeatureResultSet( getDbAccess(),clientName, 2000 );

            HashMap<String, PFeature> ftr = userRs.next();
            HashMap<String, PFeature> ftrTmp;
            Statement ftrStmt = getDbAccess().getConnection().createStatement();
            while ( (ftr = userRs.next()) != null ) {
                PFeatureResultSet ftrRsTmp = new PFeatureResultSet( getDbAccess(),clientName, 10000, ftr.values().iterator().next().getName() );
                while ( (ftrTmp = ftrRsTmp.next()) != null ) {
                    float weight = metric.getDistance( ftr, ftrTmp );
                    ftrStmt.executeUpdate( "INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + "(" + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC +
                            "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT +
                            "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ('" +
                            ftr.values().iterator().next() + "','" + ftrTmp.values().iterator().next() + "'," + weight + "," + dataStorageType + ",'" + clientName + "')" );
                }
                ftrRsTmp.close();
                //System.out.println( "User " + user.getName() );
            }
            ftrStmt.close();
            userRs.close();
        } else {
            PServerResultSet tmp = getDbAccess().executeQuery( "SELECT count(*) FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'" );
            tmp.next();
            int numOfUserClusters = tmp.getRs().getInt( 1 ) / numOfThreads;
            if ( tmp.getRs().getInt( 1 ) % numOfThreads != 0 ) {
                numOfUserClusters++;
            }
            tmp.close();
            barrier = new Barrier( numOfThreads + 1 );
            CompareThread threads[] = new CompareThread[ numOfThreads ];
            for ( int i = 0; i < numOfThreads; i++ ) {
                threads[i] = new CompareThread( clientName, metric, dataStorageType, i * numOfUserClusters, numOfUserClusters );
                threads[i].start();
            }
            try {
                barrier.waitForRelease();
            } catch ( InterruptedException ex ) {
                Logger.getLogger( PCommunityDBAccess.class.getName() ).log( Level.SEVERE, null, ex );
            }

            for ( int i = 1; i < numOfThreads; i++ ) {
                if ( threads[i].getException() != null ) {
                    throw threads[i].getException();
                }
            }
        }
    }

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

        public CompareThread( String clientName, VectorMetric metric, int dataStorageType, int offset, int limit ) {
            //System.out.println( "Thread and i have offset " + offset + " and limit " + limit );
            this.clientName = clientName;
            this.metric = metric;
            this.dataStorageType = dataStorageType;
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public void run() {
            try {
                PUserResultSet userRs = new PUserResultSet( getDbAccess(),clientName, 1000, Math.min( 1000, limit ), this.offset, null );
                PUser user = userRs.next();
                PUser userTmp;
                Statement stmt = getDbAccess().getConnection().createStatement();
                int i = 0;
                while ( (user = userRs.next()) != null ) {
                    PUserResultSet userRsTmp = new PUserResultSet( getDbAccess(),clientName, 1000, user.getName(), null );
                    while ( (userTmp = userRsTmp.next()) != null ) {
                        float weight = metric.getDistance( user, userTmp );
                        stmt.executeUpdate( "INSERT INTO " + DBAccess.UFTRASSOCIATIONS_TABLE + "(" + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_SRC + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_DST + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT + "," + DBAccess.UFTRASSOCIATIONS_TABLE_FIELD_TYPE + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ('" + user.getName() + "','" + userTmp.getName() + "'," + weight + "," + dataStorageType + ",'" + clientName + "')" );
                    }
                    userRsTmp.close();                    
                    i++;
                    if ( i >= limit ) {
                        break;
                    }
                }
                stmt.close();
                userRs.close();
                try {
                    barrier.waitForRelease();
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            } catch ( SQLException ex ) {
                Logger.getLogger( PCommunityDBAccess.class.getName() ).log( Level.SEVERE, null, ex );
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