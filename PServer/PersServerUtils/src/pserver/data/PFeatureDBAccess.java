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
import java.util.HashMap;
import pserver.domain.PFeature;
import pserver.domain.PServerVector;

/**
 *
 * @author alex
 */
public class PFeatureDBAccess {

    private DBAccess dbAccess;
    
    PFeatureDBAccess(DBAccess dbAccess) {
        this.dbAccess = dbAccess;
    }
    
    public HashMap<String, PFeature> getFeatureVector( String ftr, String clientName ) throws SQLException {
        HashMap<String, PFeature> vector = new HashMap<String, PFeature>();
        String sql = "SELECT * FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=? AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        PreparedStatement ftrStmt = dbAccess.getConnection().prepareStatement(sql);
        ftrStmt.setString(1, ftr);
        ResultSet rs = ftrStmt.executeQuery();        
        while (rs.next()) {
            PFeature newFeature = new PFeature();
            //newFeature.set
        }
        return vector;
    }

    public PServerVector getFeatureVector(String featureName, String clientName,
            boolean b) throws SQLException {
       
        PServerVector vector = new PServerVector();
        vector.setName(featureName);
        
        String sql = "SELECT * FROM " + DBAccess.UPROFILE_TABLE 
                + " WHERE " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "=? "
                + "AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";        
        PreparedStatement ftrStmt = dbAccess.getConnection().prepareStatement(sql);
        ftrStmt.setString(1, featureName);       

        //long t = System.currentTimeMillis();
        ResultSet rs = ftrStmt.executeQuery();
        //System.out.println("sql " +sql + " time " + (System.currentTimeMillis() - t) );
        while (rs.next()) {
            vector.getVectorValues().put( rs.getString(DBAccess.UPROFILE_TABLE_FIELD_USER)
                    , rs.getFloat(DBAccess.UPROFILE_TABLE_FIELD_VALUE));
        }
        rs.close();
        ftrStmt.close();
        return vector;
    }
    
}
