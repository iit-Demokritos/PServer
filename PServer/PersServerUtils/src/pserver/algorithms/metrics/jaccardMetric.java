/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
