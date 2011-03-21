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
public class PCommunityResultSet extends PServerResultSet {

    public PCommunityResultSet( Statement stmt, ResultSet resultSet ) {
        super( stmt, resultSet );
    }

    public String getCommunityName() throws SQLException{
        return getRs().getString( "community" );
    }

}
