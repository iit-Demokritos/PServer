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
 * This metric is a combination of Pearson product-moment correlation coefficient and Jaccard
 * Returns the product of Pearson * Jaccard
 */
public class WPearsonCorrelationMetric implements VectorMetric {

    public float getDistance(PServerVector vec1, PServerVector vec2) throws SQLException {
        
        int commonFeatures = 0;
        float sumX = 0.0f;
        float sumY = 0.0f;
        float sumXY = 0.0f;
        float sumSqrX = 0.0f;
        float sumSqrY = 0.0f;

        Iterator<Map.Entry<String, Float>> it = vec1.getVectorValues().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Float> pair = it.next();
            Float otherVal = vec2.getVectorValues().get(pair.getKey());
            if (otherVal == null) {
                continue;
            }
            commonFeatures++;
            float val = pair.getValue();
            sumX += val;
            sumY += otherVal;
            sumSqrX += val * val;
            sumSqrY += otherVal * otherVal;
            sumXY += val * otherVal;
        }

        if (commonFeatures == 0) {
            return 0.0f;
        } else {
            return (float) ((commonFeatures * sumXY - sumX * sumY)
                    / (Math.sqrt(commonFeatures * sumSqrX - sumX * sumX)
                    * Math.sqrt(commonFeatures * sumSqrY - sumY * sumY))) * (commonFeatures / (vec1.getVectorValues().size() + vec2.getVectorValues().size() - commonFeatures) );
        }
    }

    public float getMaximuxmCoefficientValue() {
        return 1.0f;
    }

    public float getMinimumCoefficientValue() {
        return 0.0f;
    }
}
