/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pserver.algorithms.metrics;

import java.sql.SQLException;
import java.util.HashMap;
import pserver.data.VectorResultSet;
import pserver.domain.PFeature;
import pserver.domain.PUser;

/**
 *
 * @author alexm
 */
public interface VectorMetric {
    float getDistance( VectorResultSet vectors ) throws SQLException;
    float getDistance( PUser user1, PUser user2 ) throws SQLException;
    float getDistance( HashMap<String, PFeature> ftrs1, HashMap<String, PFeature> ftrs2 ) throws SQLException;
}
