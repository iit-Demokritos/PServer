/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.utilities;

import pserver.WebServer;
import pserver.data.DBAccess;
import pserver.data.VectorMap;

/**
 *This is a simple class that checks the client credentials of a
 * request
 * 
 * @author Nick Zorbas <nickzorb@gmail.com>
 * 
 * @since 1.1
 */
public abstract class ClientCredentialsChecker {
    
     /**
     * Check whether there are valid clinet credentials in the query parameters
     * 
     * @param dbAccess the pserver's database connection handler
     * @param queryParam the current request's query parameters
     * @return returnes whether the check was successful or not (true or false)
     */
    public static boolean check(DBAccess dbAccess, VectorMap queryParam){
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        if (clntIdx == -1) {
            WebServer.win.log.error("-Missing client credentials");
            return false;
        }
        String clientCredentials = (String) queryParam.getVal(clntIdx);
        if (clientCredentials.indexOf('|')<=0){
            WebServer.win.log.error("-Malformed client credentials \""+clientCredentials+"\"");
            return false;
        }
        String clientName=clientCredentials.substring(0, clientCredentials.indexOf('|'));
        String clientPass=clientCredentials.replace(clientName, "").replace("|","");
        try {
            dbAccess.connect();
            boolean res = dbAccess.checkClientCredentials(clientName, clientPass);
            if (res == false){
                WebServer.win.log.error("-Invalid client credentials \""+clientCredentials+"\"");
            }
            return res;
        } catch (Exception e) {
            WebServer.win.log.error("-Database exception: "+e.getLocalizedMessage());
            return false;
        }
    }
    
}
