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
