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
import java.util.Iterator;
import java.util.Map;
import pserver.domain.PServerVector;

/**
 *
 * @author alexm
 */
public class CosineVectorMetric implements VectorMetric {

    public float getMaximuxmCoefficientValue() {
        return 1.0f;
    }

    public float getMinimumCoefficientValue() {
        return 0.0f;
    }

    public float getDistance(PServerVector vec1, PServerVector vec2) throws SQLException {
        float sum = 0.0f;
        float magnitude1 = 0.0f;
        float magnitude2 = 0.0f;

        Iterator<Map.Entry<String, Float>> it = vec1.getVectorValues().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Float> pairs = it.next();
            float tmp1 = pairs.getValue();
            Float tmp2 = vec2.getVectorValues().get(pairs.getKey());
            if (tmp2 != null) {
                sum += tmp1 * tmp2;
            }
            magnitude1 += tmp1 * tmp1;
        }

        magnitude2 = 0.0f;

        it = vec2.getVectorValues().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Float> pairs = it.next();
            float tmp1 = pairs.getValue();
            magnitude2 += tmp1 * tmp1;
        }
        /*
        Set<String> keys = ftrs1.getVectorValues().keySet();
        for (String k : keys) {
        float tmp1 = ftrs1.getVectorValues().get(k);
        if (ftrs2.getVectorValues().get(k) != null) {
        float tmp2 = ftrs2.getVectorValues().get(k);
        sum += tmp1 * tmp2;
        }
        magnitude1 += tmp1 * tmp1;
        }
         *                        
        keys = ftrs2.getVectorValues().keySet();
        for (String k : keys) {
        float tmp1 = ftrs2.getVectorValues().get(k);
        magnitude2 += tmp1 * tmp1;
        }
         */
        if (magnitude1 < Math.abs(0.000001) || magnitude2 < Math.abs(0.000001)) {
            return 0;
        } else {
            return sum / ((float) Math.sqrt(magnitude1) * (float) Math.sqrt(magnitude2));
        }
    }
}
