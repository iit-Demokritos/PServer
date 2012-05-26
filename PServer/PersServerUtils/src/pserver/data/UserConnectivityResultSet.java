/* 
 * Copyright 2011 NCSR "Demokritos"
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");   
 * you may not use this file except in compliance with the License.   
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package pserver.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author alexm
 */
public class UserConnectivityResultSet {

    DBAccess dbAccess;
    Connection con;
    int size;
    private HashMap<String, LinkedList<String>> connections = null;
    private Statement stmt;
    private String clientName;
    ResultSet rs;

    public UserConnectivityResultSet( DBAccess dbAccess, String clientName, int size ) throws SQLException{
        this.dbAccess = dbAccess;
        this.size = size;
        this.con = dbAccess.newConnection();
        this.stmt = this.con.createStatement();
        loadConnections();
        this.clientName = clientName;

        connections = new HashMap<String, LinkedList<String>>();
    }

    public void close() throws SQLException {
        stmt.close();
        con.close();
    }

    private void loadConnections() throws SQLException {
        String sql = "(SELECT DISTINCT " + DBAccess.UASSOCIATIONS_TABLE_FIELD_SRC + " FROM " + DBAccess.UASSOCIATIONS_TABLE + "  WHERE type = " + DBAccess.RELATION_BINARY_SIMILARITY + 
                " AND " + DBAccess.FIELD_PSCLIENT + "='"+ clientName + "' ) UNION (SELECT DISTINCT " + 
                DBAccess.UASSOCIATIONS_TABLE_FIELD_DST + " As " + DBAccess.UASSOCIATIONS_TABLE_FIELD_SRC + " FROM " + DBAccess.UASSOCIATIONS_TABLE + 
                "  WHERE type = " + DBAccess.RELATION_BINARY_SIMILARITY + " AND " + DBAccess.FIELD_PSCLIENT + "='"+ clientName + "' ) ORDER BY USER_SRC LIMIT " + size;
        rs = stmt.executeQuery( sql );

        ResultSet rsTmp = stmt.executeQuery( sql );
        while( rs.next() ){

        }

        rs.close();
    }
}
