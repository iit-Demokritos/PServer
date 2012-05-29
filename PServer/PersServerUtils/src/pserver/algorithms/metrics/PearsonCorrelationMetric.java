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
import java.util.ArrayList;
import pserver.data.VectorResultSet;
import pserver.domain.PFeature;
import pserver.domain.PUser;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author alexm
 */
public class PearsonCorrelationMetric implements VectorMetric {

    public float getDistance(VectorResultSet vectors) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public float getDistance(PUser user1, PUser user2) throws SQLException {
        float result = 0;
        float sum_sq_x = 0;
        float sum_sq_y = 0;
        float sum_coproduct = 0;
        float mean_x;
        float mean_y;

        ArrayList<String> commonFeatures = new ArrayList<String>();

        Set<String> ftrs = user1.getFeatures();
        int pos = 0;

        float common = 0;
        float united = 0;
        for( String ftr: ftrs ){
            if( user2.featureExist( ftr ) ) {
                commonFeatures.add( ftr );
                common++;
            }
        }
        if ( commonFeatures.size() == 0 )
            return 0;
        united = ftrs.size() + user1.getFeatures().size() - common;

        mean_x= user1.getFeatureValue( commonFeatures.get(0) );
        mean_y= user2.getFeatureValue( commonFeatures.get(0) );

        for(int i=pos + 2;i<commonFeatures.size()+1;i++){
            float sweep = Float.valueOf(i-1)/i;
            float delta_x = user1.getFeatureValue( commonFeatures.get(i-1) )-mean_x;
            float delta_y = user2.getFeatureValue( commonFeatures.get(i-1) )-mean_y;
            sum_sq_x += delta_x * delta_x * sweep;
            sum_sq_y += delta_y * delta_y * sweep;
            sum_coproduct += delta_x * delta_y * sweep;
            mean_x += delta_x / i;
            mean_y += delta_y / i;
        }
        float pop_sd_x = (float) Math.sqrt(sum_sq_x/commonFeatures.size());
        float pop_sd_y = (float) Math.sqrt(sum_sq_y/commonFeatures.size());
        float cov_x_y = sum_coproduct / commonFeatures.size();
        result = cov_x_y / (pop_sd_x*pop_sd_y);
        //System.out.println("reult " + result + " jaccard " + ( common /united ) + " " + common + " " + united );
        if( Float.isNaN(result ))
            return 0;
        return ( common /united ) * ( result + 1 );
    }

    public float getDistance(HashMap<String, PFeature> ftrs1, HashMap<String, PFeature> ftrs2) throws SQLException {
        float sumF1 = 0.0f;
        float sumFSqr1 = 0.0f;
        float sumF2 = 0.0f;
        float sumFSqr2 = 0.0f;
        float sumF12 = 0.0f;

        Set<String> keys = ftrs1.keySet();
        for (String k : keys) {
            sumF1 += ftrs1.get(k).getValue();
            sumFSqr1 += ftrs1.get(k).getValue() * ftrs1.get(k).getValue();

            if (ftrs2.get(k) != null) {
                sumF12 += ftrs1.get(k).getValue() * ftrs2.get(k).getValue();
            }
        }

        keys = ftrs2.keySet();
        for (String k : keys) {
            sumF2 += ftrs2.get(k).getValue();
            sumFSqr2 += ftrs2.get(k).getValue() * ftrs2.get(k).getValue();
        }

        int n = Math.max(ftrs1.size(), ftrs2.size());
        if (n == 0) {
            return 0.0f;
        }
        return (sumF12 - sumF1 * sumF1 / n) / (float) Math.sqrt((sumFSqr1 - sumF1 * sumF1 / n) * (sumFSqr2 - sumF2 * sumF2 / n));
    }
}
