/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.data;

import java.sql.SQLException;

/**
 *
 * @author alexm
 */
public class VectorResultSet {

    protected PServerResultSet rs;

    public VectorResultSet( PServerResultSet resultSet) {
        rs = resultSet;
    }

    public boolean next() throws SQLException {
        return rs.next();
    }

    public boolean previous() throws SQLException {
        return rs.previous();
    }

    public void close() throws SQLException {
        rs.close();
    }

    public float getFirstVectorElementValue() throws SQLException {
        return this.rs.getRs().getFloat( 1 );
    }

    public float getSecondVectorElementValue() throws SQLException {
        return this.rs.getRs().getFloat( 2 );
    }
}
