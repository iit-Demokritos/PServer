/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.pservlets;

import java.util.ArrayList;
import java.util.HashMap;
import pserver.data.DBAccess;

/**
 *
 * @author scify
 */
public interface PServerCommand {
    
    int runCommand(HashMap<String, ArrayList<String>> queryParam, StringBuffer respBody, DBAccess dbAccess);
    
}
