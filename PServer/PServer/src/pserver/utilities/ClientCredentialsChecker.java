/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.utilities;

import pserver.WebServer;
import pserver.data.DBAccess;
import pserver.data.VectorMap;

/**
 *
 * @author scify
 */
public abstract class ClientCredentialsChecker {
    
        /**
     * Check whether the client is registered
     */
    public static boolean check(DBAccess dbAccess, VectorMap queryParam){
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        if (clntIdx == -1) {
            WebServer.win.log.error("-Missing client credentials");
            return false;
        }
        String clientCredentials = (String) queryParam.getVal(clntIdx);
        if (clientCredentials.indexOf('|')<=0&&clientCredentials.indexOf("%7C")<=0){
            WebServer.win.log.error("-Malformed client credentials \""+clientCredentials+"\"");
            return false;
        }
        String clientName=clientCredentials.substring(0, Math.max(clientCredentials.indexOf('|'),clientCredentials.indexOf("%7C")));
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
