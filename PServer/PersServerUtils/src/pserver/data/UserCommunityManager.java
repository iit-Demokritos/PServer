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
public class UserCommunityManager implements GraphClusterManager{

    private DBAccess dbAccess;
    private int numOfCliques;
    private final String clientName;

    public UserCommunityManager( DBAccess dbAccess, String clientName ) {
        this.dbAccess = dbAccess;
        numOfCliques = 0;
        this.clientName = clientName;
    }

    public void addGraphCluster( Set<String> cluster ) throws SQLException {        
        numOfCliques++;        
        String sql;
        Statement stmt = dbAccess.getConnection().createStatement();
        stmt.execute( "INSERT INTO " + DBAccess.COMMUNITIES_TABLE + " (" + DBAccess.COMMUNITIES_TABLE_FIELD_COMMUNITY + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ( 'community_" + numOfCliques +"', '" + clientName + "')" );

        for( String n : cluster ){
            sql = "INSERT INTO " + DBAccess.UCOMMUNITY_TABLE + " (" + DBAccess.UCOMMUNITY_TABLE_FIELD_USER + "," +
                    DBAccess.UCOMMUNITY_TABLE_FIELD_COMMUNITY + "," + DBAccess.FIELD_PSCLIENT + ") VALUES ( '" + n + "','community_"+ numOfCliques +"', '" + clientName + "')";
            stmt.execute( sql );
        }      
/*
        sql = "INSERT INTO " + DBAccess.COMMUNITY_PROFILES_TABLE + " (" +
                DBAccess.COMMUNITY_PROFILES_TABLE_FIELD_COMMUNITY + "," +
                DBAccess.COMMUNITY_PROFILES_TABLE_FIELD_FEATURE + "," +
                DBAccess.COMMUNITY_PROFILES_TABLE_FIELD_FEATURE_VALUE + "," +
                DBAccess.FIELD_PSCLIENT + ") SELECT 'community_"+ numOfCliques + "'," +
                DBAccess.UPROFILE_TABLE_FIELD_FEATURE + ", AVG(" + DBAccess.UPROFILE_TABLE_FIELD_VALUE + ")," +
                "'" + clientName + "' FROM " + DBAccess.UPROFILE_TABLE +
                " WHERE " + DBAccess.UPROFILE_TABLE_FIELD_USER  + " IN (" +
                " SELECT " + DBAccess.UCOMMUNITY_TABLE_FIELD_USER + " FROM " + DBAccess.UCOMMUNITY_TABLE +
                " WHERE " + DBAccess.UCOMMUNITY_TABLE_FIELD_COMMUNITY + "='community_"+ numOfCliques + "'" +
                " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "')" +
                " GROUP BY " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE;
        
        stmt.execute( sql );
*/
        stmt.close();
    }

}
