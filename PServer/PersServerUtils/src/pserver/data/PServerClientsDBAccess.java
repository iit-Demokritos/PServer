/*
 * Copyright 2013 IIT , NCSR Demokritos - http://www.iit.demokritos.gr,
 *                            SciFY NPO - http://www.scify.org
 *
 * This product is part of the PServer Free Software.
 * For more information about PServer visit http://www.pserver-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If this code or its output is used, extended, re-engineered, integrated,
 * or embedded to any extent in another software or hardware, there MUST be
 * an explicit attribution to this work in the resulting source code,
 * the packaging (where such packaging exists), or user interface
 * (where such an interface exists).
 *
 * The attribution must be of the form
 * "Powered by PServer, IIT NCSR Demokritos , SciFY"
 */

package pserver.data;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import pserver.domain.PServerClient;

/**
 *
 * @author alexm
 * @author scify
 * @author Nick Zorbas <nickzorb@gmail.com>
 */
public class PServerClientsDBAccess {
    
    private static char[] SALT_CHARS = new char[62];
    
    private static int SALT_LENGTH = 64;
    
    //private DBAccess dbAccess;   
    private static PServerClientsDBAccess instance = null;
    
    private ArrayList<PServerClient> clients;
    
    private PServerClientsDBAccess( DBAccess dbAccess ) throws SQLException{        
        loadPServerClients(dbAccess);
    }
    
    public static void initialize( DBAccess dBAccess ) throws SQLException, Exception{
        if ( instance != null ) {
            throw new Exception( PServerClientsDBAccess.class.getName() + " is singenton and can only be initialize once");            
        }
        int index = 0;
        for (int i = 'a'; i <= 'z'; i++) {
            SALT_CHARS[index] = (char)i;
            index++;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            SALT_CHARS[index] = (char)i;
            index++;
        }
        for (int i = '0'; i <= '9'; i++) {
            SALT_CHARS[index] = (char)i;
            index++;
        }
        instance = new PServerClientsDBAccess(dBAccess);
    }
    
    public static PServerClientsDBAccess getInstance(){
        return instance;
    }
    
    public boolean clientNameExists( String clientName ) throws SQLException {
        for( PServerClient clnt : clients ){
            if( clnt.getName().equals(clientName)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<PServerClient> getClients(){
        return this.clients;
    }
    
    public void insertPServerClient( DBAccess dbAccess, String clientName, String clientPass ) throws SQLException{
        Statement stmt = dbAccess.getConnection().createStatement();
        SecureRandom random = new SecureRandom();
        StringBuilder salt = new StringBuilder();
        for (int i = 0;i < SALT_LENGTH; i++) {
            salt.append(SALT_CHARS[Math.abs(random.nextInt()%62)]);
        }
        stmt.executeUpdate( "INSERT INTO pserver_clients(name,password,salt) VALUES('" + clientName + "',SHA2('" + salt.toString()+clientPass + "',256), '" + salt.toString() + "');" );
        stmt.close();
        this.clients.add( new PServerClient(clientName, clientPass, salt.toString()));
    }
    
    public void deleteClient( String client , DBAccess dbAccess ) throws SQLException {
        Statement stmt =  dbAccess.getConnection().createStatement();
        stmt.execute( "DELETE FROM ftrgroup_users WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM user_feature_associations WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM user_associations WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM stereotype_feature_associations WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );        
        stmt.execute( "DELETE FROM community_associations WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM community_feature_associations WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM decay_data WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM num_data WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM user_sessions WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM community_profiles WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM decay_data WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM decay_groups WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM ftrgroup_features WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );        
        stmt.execute( "DELETE FROM stereotype_profiles WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM stereotype_users WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM user_attributes WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM user_community WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM user_profiles WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM ftrgroups WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM up_features WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM stereotypes WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM communities WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM attributes WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM users WHERE " + DBAccess.FIELD_PSCLIENT + "=\"" + client + "\";" );
        stmt.execute( "DELETE FROM pserver_clients WHERE name=\"" + client + "\";" );
        stmt.close();
        int pos = 0;
        for( PServerClient clnt : clients ){
            if( clnt.getName().equals(client)) {
                break;
            }
            pos ++;
        }
        clients.remove(pos);
    }   
    
    private void loadPServerClients( DBAccess dbAccess) throws SQLException {
        clients = new ArrayList<PServerClient>(20);
        Statement stmt = dbAccess.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT * FROM pserver_clients" );
        while( rs.next() ) {
            PServerClient client = new PServerClient();
            client.setName(rs.getString("name"));
            client.setSHA2pass(rs.getString("password"));
            client.setSalt(rs.getString("salt"));
            clients.add(client);
        }        
    }
}
