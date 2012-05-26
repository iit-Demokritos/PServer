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
import java.sql.Statement;
import java.util.Set;

/**
 *
 * @author alexm
 */
public class FeatureGroupManager implements GraphClusterManager {

    private DBAccess dbAccess;
    private int numOfCliques;
    private final String clientName;

    public FeatureGroupManager( DBAccess dbAccess, String clientName ) {
        this.dbAccess = dbAccess;
        numOfCliques = 0;
        this.clientName = clientName;
    }

    public void addGraphCluster( Set<String> cluster ) throws SQLException {
        //throw new UnsupportedOperationException( "Not supported yet." );
        numOfCliques++;
        System.out.println( " new clique with size " + cluster.size() );
        System.out.println( " now i have " + numOfCliques );

        Statement stmt = dbAccess.getConnection().createStatement();
        stmt.execute( "INSERT INTO " + DBAccess.FTRGROUPS_TABLE + " (" + DBAccess.FTRGROUPS_TABLE_FIELD_FTRGROUP + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ( 'ftrgroup_" + numOfCliques + "', '" + clientName + "')" );
        for ( String n : cluster ) {
            String subSql = "INSERT INTO " + DBAccess.FTRGROUP_FEATURES_TABLE + " (" + DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_NAME + "," +
                    DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ( '" + n + "','ftrgroup_" + numOfCliques + "', '" + clientName + "')";
            stmt.execute( subSql );
        }
        stmt.close();
    }
}
