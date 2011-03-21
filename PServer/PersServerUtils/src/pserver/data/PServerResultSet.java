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
public class PServerResultSet {
    private Statement stmt;
    private ResultSet rs;

    public PServerResultSet( Statement st, ResultSet rset ){
        stmt = st;
        rs = rset;
    }

    public boolean next() throws SQLException {
        return getRs().next();
    }

    public boolean previous() throws SQLException {
        return getRs().previous();
    }

    public void close() throws SQLException {
        getRs().close();
        getStmt().close();
    }

    /**
     * @return the stmt
     */
    public Statement getStmt() {
        return stmt;
    }

    /**
     * @param stmt the stmt to set
     */
    public void setStmt( Statement stmt ) {
        this.stmt = stmt;
    }

    /**
     * @return the rs
     */
    public ResultSet getRs() {
        return rs;
    }

    /**
     * @param rs the rs to set
     */
    public void setRs( ResultSet rs ) {
        this.rs = rs;
    }
}
