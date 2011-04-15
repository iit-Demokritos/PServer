/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.ArrayList;
import pserver.domain.PFeature;

/**
 *
 * @author alexm
 */
class PFeatureResultSet {

    DBAccess dbAccess;
    Connection con;
    Statement stmt;
    String clientName;
    ArrayList<HashMap<String, PFeature>> features;
    int ArrayListPtr;
    int firstTotalPtr;
    int totalPtr;
    int cacheSize;

    PFeatureResultSet( DBAccess dbAccess, String clientName, int cacheSize ) throws SQLException {
        this.dbAccess = dbAccess;
        ArrayListPtr = totalPtr = firstTotalPtr = 0;
        this.cacheSize = cacheSize;
        this.features = new ArrayList<HashMap<String, PFeature>>( cacheSize );

        con = dbAccess.newConnection();
        stmt = con.createStatement();
        this.clientName = clientName;

        loadNextPackOfFeatures();
    }

    PFeatureResultSet( DBAccess dbAccess, String clientName, int cacheSize, String name ) throws SQLException {
        this.dbAccess = dbAccess;
        ArrayListPtr = 0;
        this.cacheSize = cacheSize;
        this.features = new ArrayList<HashMap<String, PFeature>>( cacheSize );

        con = dbAccess.newConnection();
        stmt = con.createStatement();
        this.clientName = clientName;

        String sql = "SELECT COUNT(*) FROM " + DBAccess.FEATURE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName +
                "' AND " + DBAccess.FEATURE_TABLE_FIELD_FEATURE + "<='" + name + "'";

        ResultSet tmp = stmt.executeQuery( sql );
        tmp.next();
        this.firstTotalPtr = totalPtr = tmp.getInt( 1 );

        loadNextPackOfFeatures();
    }

    private void loadNextPackOfFeatures() throws SQLException {
        //System.out.println( "Total Memory " + Runtime.getRuntime().totalMemory() );
        //System.out.println( "Free Memory " + Runtime.getRuntime().freeMemory() );
        this.features.clear();

        String sql = "SELECT * FROM " + DBAccess.FEATURE_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName +
                "' ORDER BY " + DBAccess.FEATURE_TABLE_FIELD_FEATURE + " LIMIT " + cacheSize + " OFFSET " + totalPtr;

        ResultSet result = stmt.executeQuery( sql );
        if ( result.next() == false ) {
            return;
        }

        while ( result.next() ) {
            String curFtr = result.getString( DBAccess.FEATURE_TABLE_FIELD_FEATURE );
            Statement stmtProfile = this.con.createStatement();
            sql = "SELECT * FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + " = '" + curFtr + "' AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' ORDER BY " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE;
            //System.out.println( "sql = " + sql );
            ResultSet featuresRS = stmtProfile.executeQuery( sql );

            String prevFtr = "";

            int i = 0;
            HashMap<String, PFeature> values = new HashMap<String, PFeature>();
            String usr = "";
            PFeature ftrObj = null;
            while ( featuresRS.next() ) {
                usr = featuresRS.getString( DBAccess.UPROFILE_TABLE_FIELD_USER );
                String ftr = featuresRS.getString( DBAccess.UPROFILE_TABLE_FIELD_FEATURE );

                ftrObj = new PFeature( ftr, featuresRS.getFloat( DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE ) + "", featuresRS.getFloat( DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE ) + "" );
                i++;
            }
            values.put( usr, ftrObj );
            this.features.add( values );
            this.ArrayListPtr = 0;
            totalPtr += this.cacheSize;
            featuresRS.close();
            stmtProfile.close();
        }
    }

    HashMap<String, PFeature> next() throws SQLException {
        if ( features.size() == 0 ) {
            return null;
        }
        if ( ArrayListPtr >= features.size() ) {
            loadNextPackOfFeatures();
            if ( features.size() == 0 ) {
                return null;
            }
        }
        ArrayListPtr++;
        return features.get( ArrayListPtr );
    }

    public void close() throws SQLException {
        stmt.close();
        con.close();
    }
}
