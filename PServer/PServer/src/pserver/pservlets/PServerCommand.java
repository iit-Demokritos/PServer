/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.pservlets;

import pserver.data.DBAccess;
import pserver.data.VectorMap;

/**
 *
 * @author scify
 */
public interface PServerCommand {
    
    int runCommand(VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess);
    
}
