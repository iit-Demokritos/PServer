/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pserver.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author alexm
 */
public class PFeatureGroupProfileResultSet extends PServerResultSet {

    public PFeatureGroupProfileResultSet( Statement stmt, ResultSet rs) {
        super( stmt, rs );
    }

    public String getFeatureGroupName() throws SQLException{
        return getRs().getString( DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP );
    }

    public String getFeatureGroupyFeatureName() throws SQLException{
        return getRs().getString( DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_NAME );
    }
}
