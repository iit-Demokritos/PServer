/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pserver.data;

import java.sql.SQLException;
import java.util.Set;

/**
 *
 * @author alexm
 */
public interface GraphClusterManager {
    public void addGraphCluster( Set<String> cluster ) throws SQLException;
}
