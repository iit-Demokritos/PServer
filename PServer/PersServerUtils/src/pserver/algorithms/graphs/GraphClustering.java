/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pserver.algorithms.graphs;

import java.sql.SQLException;
import pserver.data.DBAccess;
import pserver.data.GraphClusterManager;

/**
 *
 * @author alexm
 */
public interface GraphClustering {
    /**
     *
     * @param sql the sql that must be run to get the connectivity Graph
     * @param dbAccess the data access object
     */
    public void execute( String querySql, GraphClusterManager graphClusterManager, DBAccess dbAccess ) throws SQLException;
}
