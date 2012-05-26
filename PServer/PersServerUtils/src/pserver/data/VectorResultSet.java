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
