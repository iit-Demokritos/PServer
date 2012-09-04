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

package pserver.algorithms.metrics;

import java.sql.SQLException;
import pserver.domain.PServerVector;

/**
 * This interface provide all the methods that must be implemented when we want 
 * provide a new vector metric for Pserver
 * 
 * @author alexm
 */
public interface VectorMetric {    
    /**
     * 
     * @return the value that the metric returns when there is the maximum coefficient 
     */
    float getMaximuxmCoefficientValue();
    /**
     * 
     * @return the value that the metric returns when there is the minimu coefficient 
     */
    float getMinimumCoefficientValue();
    /**
     * @param vec1 is the first Pserver Vector
     * @param vec2 is the second Pserver Vector
     * @return the value of the coefficient
     */
    float getDistance( PServerVector vec1, PServerVector vec2 ) throws SQLException;
}
