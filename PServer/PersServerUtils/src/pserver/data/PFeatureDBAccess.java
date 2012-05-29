/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import pserver.domain.PFeature;

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
    
}
