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
public class PCommunityProfileResultSet extends PServerResultSet {

    public PCommunityProfileResultSet( Statement stmt, ResultSet resultSet) {
        super( stmt, resultSet );
    }

    public String getCommunityName() throws SQLException{
        return getRs().getString( DBAccess.COMMUNITY_PROFILES_TABLE_FIELD_COMMUNITY );
    }

    public String getCommunityFeatureName() throws SQLException{
        return getRs().getString( DBAccess.COMMUNITY_PROFILES_TABLE_FIELD_FEATURE );
    }

    public float getFeatureValue() throws SQLException{
        return getRs().getFloat( DBAccess.COMMUNITY_PROFILES_TABLE_FIELD_FEATURE_VALUE );
    }
}
