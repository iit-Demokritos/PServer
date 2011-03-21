package pserver.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author alexm
 */
public class PFeatureGroupResultSet extends PServerResultSet {

    public PFeatureGroupResultSet( Statement stmt, ResultSet rs) {
        super( stmt, rs );
    }

    public String getFeatureGroupName() throws SQLException{
        return getRs().getString( DBAccess.FTRGROUPS_TABLE_FIELD_FTRGROUP );
    }  

}
