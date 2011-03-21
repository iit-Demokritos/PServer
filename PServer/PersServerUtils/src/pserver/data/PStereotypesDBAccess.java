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

    public boolean insertNewStereotypeIfNotExists( String stereotype, List<PAttribute> attributes, String clientName ) throws SQLException {
        String sql = "SELECT * FROM " + DBAccess.STEREOTYPE_TABLE + " WHERE " + DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        Statement stmt = dbAccess.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery( sql );
        if ( rs.next() == true ) {
            return false;
        }
        rs.close();

        sql = "INSERT INTO " + DBAccess.STEREOTYPE_TABLE + "(" + DBAccess.STEREOTYPE_TABLE_FIELD_STEREOTYPE + "," + DBAccess.FIELD_PSCLIENT + ") VALUES('" +
                stereotype + "','" + clientName + "')";
        stmt.execute( sql );

        if ( attributes != null ) {
            for ( PAttribute atr : attributes ) {
                sql = "INSERT INTO " + DBAccess.STEREOTYPE_ATTIBUTE_TABLE + "(" + DBAccess.STEREOTYPE_ATTIBUTE_TABLE_FIELD_STEREOTYPE + "," + DBAccess.STEREOTYPE_ATTIBUTE_TABLE_FIELD_ATTRIBUTE + "," + DBAccess.STEREOTYPE_ATTIBUTE_TABLE_FIELD_VALUE + "," + DBAccess.FIELD_PSCLIENT + ") VALUES('" +
                        stereotype + "','" + atr.getName() + "','" + atr.getValue() + "','" + clientName + "')";
                stmt.execute( sql );
            }
        }
        stmt.close();
        return true;
    }
}
