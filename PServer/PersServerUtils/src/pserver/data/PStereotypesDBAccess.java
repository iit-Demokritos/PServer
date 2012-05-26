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

import java.sql.SQLException;

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
