/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.data;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import pserver.domain.PServerClient;

/**
 *
 * @author alexm
 */
public class PServerClientsDBAccess {
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
        instance = new PServerClientsDBAccess(dBAccess);
    }
    
    public static PServerClientsDBAccess getInstance(){
        return instance;
    }
    
    public boolean clientNameExists( String clientName ) throws SQLException {
        /*Statement stmt = dbAccess.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT * FROM pserver_clients WHERE name=\"" + clientName + "\";" );
        if ( rs.next() ) {//if the client exists the users cant be add and return the value 1        
            stmt.close();
            return true;
        } else {
            stmt.close();
            return false;
        }*/
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
    
    public void insertPServerClient( DBAccess dbAccess, String clientName, String clientPass ) throws SQLException {
        Statement stmt = dbAccess.getConnection().createStatement();
        String mdClientPass = null;
        try {
            mdClientPass = MD5.encrypt( clientPass );
        } catch (NoSuchAlgorithmException ex) {            
        }
        stmt.executeUpdate( "INSERT INTO pserver_clients(name,password) VALUES(\"" + clientName + "\",\"" + mdClientPass + "\");" );
        stmt.close();
        this.clients.add( new PServerClient(clientName, clientPass));
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
            client.setMd5pass(rs.getString("password"));
            clients.add(client);
        }        
    }

    public boolean isValidClient(String clientName, String clientPass) {
        for( PServerClient clnt : clients ){
            if( clnt.getName().equals(clientName)) {
                try {
                    if( clnt.getMd5pass().equals( MD5.encrypt(clientPass))){
                        return true;
                    } else {
                        return false;
                    }
                } catch (NoSuchAlgorithmException ex) {                    
                }
            }            
        }
        return false;
    }
    
}
