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
public class PFeatureGroupProfileResultSet extends PServerResultSet {

    public PFeatureGroupProfileResultSet( Statement stmt, ResultSet rs) {
        super( stmt, rs );
    }

    public String getFeatureGroupName() throws SQLException{
        return getRs().getString( DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP );
    }

    public String getFeatureGroupyFeatureName() throws SQLException{
        return getRs().getString( DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_NAME );
    }
}
