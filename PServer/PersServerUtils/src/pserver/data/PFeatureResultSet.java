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
