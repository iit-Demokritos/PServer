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
