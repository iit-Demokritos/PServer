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
public class PNumDataResultSet extends PServerResultSet {

    public PNumDataResultSet( Statement stmt, ResultSet resultSet) {
        super( stmt, resultSet );
    }

    public String getUserName() throws SQLException{
        return getRs().getString( DBAccess.NUM_DATA_TABLE_FIELD_USER );
    }

    public String getFeatureName() throws SQLException{
        return getRs().getString( DBAccess.NUM_DATA_TABLE_FIELD_FEATURE );
    }

    public float getFeatureValue() throws SQLException{
        return getRs().getFloat( DBAccess.NUM_DATA_TABLE_FIELD_VALUE );
    }

    public int getSessionId() throws SQLException{
        return getRs().getInt( DBAccess.NUM_DATA_TABLE_FIELD_SESSION );
    }

    public long getTimeStamp() throws SQLException{
        return getRs().getLong( DBAccess.NUM_DATA_TABLE_FIELD_TIMESTAMP );
    }

}
