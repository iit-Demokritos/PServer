package pserver.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import pserver.domain.PAttribute;
import pserver.domain.PFeature;

public class PStereotypesDBAccess {

    private DBAccess dbAccess;
    private Barrier barrier;

    public PStereotypesDBAccess( DBAccess db ) throws SQLException {
        dbAccess = db;
    }

    public boolean insertNewStereotypeIfNotExists( String stereotype, String rule, String clientName ){
        throw new UnsupportedOperationException("Not yet implemented");
        /*String sql = "SELECT * FROM " + DBAccess.STEREOTYPE_TABLE + " WHERE " + DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        Statement stmt = dbAccess.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery( sql );
        if ( rs.next() == true ) {
            return false;
        }
        rs.close();

        sql = "INSERT INTO " + DBAccess.STEREOTYPE_TABLE + "(" + DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE + "," + DBAccess.FIELD_PSCLIENT + ") VALUES('" +
                stereotype + "','" + clientName + "')";
        stmt.execute( sql );        
        stmt.close();
        return true;*/
    }

}
