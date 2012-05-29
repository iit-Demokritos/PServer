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
import pserver.data.VectorResultSet;
import pserver.domain.PFeature;
import pserver.domain.PUser;
import java.util.HashMap;
import java.util.Set;
/**
 *
 * @author alexm
 */
public class CosineVectorMetric implements VectorMetric {

    public float getDistance( VectorResultSet vectors ) throws SQLException{
        float sum = 0.0f;
        float magnitude1 = 0.0f;
        float magnitude2 = 0.0f;

        while( vectors.next() ) {
            float tmp1 = vectors.getFirstVectorElementValue();
            float tmp2 = vectors.getSecondVectorElementValue();
            sum += tmp1 * tmp2;
            magnitude1 += tmp1 * tmp1;
            magnitude2 += tmp2 * tmp2;
        }

        if( magnitude1 < Math.abs( 0.000001 ) || magnitude2 < Math.abs( 0.000001 )  )
            return 0;
        else
            return sum / ( (float)Math.sqrt( magnitude1 ) * (float)Math.sqrt( magnitude2 ) );
    }

    public float getDistance( PUser user1, PUser user2 ) throws SQLException {
        float sum = 0.0f;
        float magnitude1 = 0.0f;
        float magnitude2 = 0.0f;
        Set<String> features = user1.getFeatures();
        for( String ftr : features ) {
            float tmp1 = user1.getFeatureValue( ftr );
            float tmp2 = user2.getFeatureValue( ftr );
            sum += tmp1 * tmp2;
            magnitude1 += tmp1 * tmp1;            
        }
        features = null;
        magnitude2 = 0.0f;
        features = user2.getFeatures();
        for( String ftr : features ) {
            float tmp2 = user2.getFeatureValue( ftr );
            magnitude2 += tmp2 * tmp2;
        }
        features = null;
        if( magnitude1 < Math.abs( 0.000001 ) || magnitude2 < Math.abs( 0.000001 )  )
            return 0;
        else
            return sum / ( (float)Math.sqrt( magnitude1 ) * (float)Math.sqrt( magnitude2 ) );
    }

    public float getDistance( HashMap<String, PFeature> ftrs1, HashMap<String, PFeature> ftrs2 ) throws SQLException {
        float sum = 0.0f;
        float magnitude1 = 0.0f;
        float magnitude2 = 0.0f;
        Set<String> keys = ftrs1.keySet();
        for( String k : keys ){
            float tmp1 = ftrs1.get( k ).getValue();
            if( ftrs2.get( k ) != null ) {
                float tmp2 = ftrs2.get( k ).getValue();
                sum += tmp1 * tmp2;
            }
            magnitude1 += tmp1 * tmp1;
        }

        magnitude2 = 0.0f;
        keys = ftrs2.keySet();
        for( String k : keys ){
            float tmp1 = ftrs2.get( k ).getValue();
            magnitude2 += tmp1 * tmp1;
        }

        if( magnitude1 < Math.abs( 0.000001 ) || magnitude2 < Math.abs( 0.000001 )  )
            return 0;
        else
            return sum / ( (float)Math.sqrt( magnitude1 ) * (float)Math.sqrt( magnitude2 ) );
    }

}
