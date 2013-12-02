/*
 * Copyright 2013 IIT , NCSR Demokritos - http://www.iit.demokritos.gr,
 *                            SciFY NPO - http://www.scify.org
 *
 * This product is part of the PServer Free Software.
 * For more information about PServer visit http://www.pserver-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If this code or its output is used, extended, re-engineered, integrated,
 * or embedded to any extent in another software or hardware, there MUST be
 * an explicit attribution to this work in the resulting source code,
 * the packaging (where such packaging exists), or user interface
 * (where such an interface exists).
 *
 * The attribution must be of the form
 * "Powered by PServer, IIT NCSR Demokritos , SciFY"
 */

package pserver.algorithms.metrics;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import pserver.domain.PServerVector;

/**
 *
 * @author alexm
 * 
 * Returns the Jacckard coefficient
 */
public class jaccardMetric implements VectorMetric {

    public float getDistance(PServerVector vec1, PServerVector vec2) throws SQLException {

        int commonFeatures = 0;

        Iterator<Map.Entry<String, Float>> it = vec1.getVectorValues().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Float> pair = it.next();
            Float otherVal = vec2.getVectorValues().get(pair.getKey());
            if (otherVal == null) {
                continue;
            }
            commonFeatures++;            
        }
        if (commonFeatures == 0) {
            return 0.0f;
        } else {
            return commonFeatures / (vec1.getVectorValues().size() + vec2.getVectorValues().size() - commonFeatures);
        }

    }

    public float getMaximuxmCoefficientValue() {
        return 1.0f;
    }

    public float getMinimumCoefficientValue() {
        return 0.0f;
    }
}
