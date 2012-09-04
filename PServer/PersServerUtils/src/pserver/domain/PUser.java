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

package pserver.domain;

import java.util.*;

public class PUser {

    private String name = null;
    private Map<Set<String>, Float> ftrAssocs = null;
    private Map<String, Float> ftrReqs = null;
    private Map<String, Float> features = null;
    private Map<String, String> attributes = null;
    private PServerVector vector = new PServerVector();
    
    public PUser() {
        this.name = "";
        this.features = new HashMap<String, Float>();
        this.attributes = new HashMap<String, String>();
        vector.setName(name);
        vector.setVectorValues(features);
    }

    public PUser( int featureNum ) {
        this.name = "";
        this.features = new HashMap<String, Float>( featureNum );
        this.attributes = new HashMap<String, String>( featureNum );
        vector.setName(name);
        vector.setVectorValues(features);
    }

    public PUser( String name ) {
        this.name = name;
        this.features = new HashMap<String, Float>();
        this.attributes = new HashMap<String, String>();
    }

    public PUser( String name, int featureNum ) {
        this.name = name;
        if ( featureNum > 0 ) {
            this.features = new HashMap<String, Float>( featureNum );
            this.attributes = new HashMap<String, String>();
        }
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setFeature( String ftrName, float value ) {
        this.features.put( ftrName, value );
    }

    public void setAttribute( String attrName, String value ) {
        attributes.put( attrName, value );
    }

    public float getFeatureValue( String feature ) {
        if ( this.features.get( feature ) == null ) {
            return 0.0f;
        } else {
            return this.features.get( feature );
        }
    }

    public boolean featureExist( String feature ) {
        return this.features.containsKey( feature );
    }

    public Set<String> getFeatures() {
        return features.keySet();/*
        Set<String> keys = features.keySet();
        if( keys.size() ==0 )
            return null;
        String[] keyNames = new String[ keys.size() ];
        int i = 0;
        for ( String key : keys ) {
            keyNames[i] = key;
            i++;
        }
        return keyNames;*/
    }

    public int getFeaturesNumber() {
        return this.features.size();
    }

    /**
     * @return the attributes
     */
    public String[] getAttributes() {
        Set<String> keys = attributes.keySet();
        String[] keyNames = new String[ keys.size() ];
        int i = 0;
        for ( String key : keys ) {
            keyNames[i] = key;
            i++;
        }
        return keyNames;
    }

    public String getAttributeValue( String attr ) {
        return attributes.get( attr );
    }

    /**
     * @param features the features to set
     */
    public void setFeatures(Map<String, Float> features) {
        this.features = features;
    }

    /**
     * @param features the features to set
     */
    public Map<String, Float> getProfile() {
        return this.features;
    }

    /**
     * @return the ftrAssocs
     */
    public Map<Set<String>, Float> getFtrAssocs() {
        return ftrAssocs;
    }

    /**
     * @param ftrAssocs the ftrAssocs to set
     */
    public void setFtrAssocs(Map<Set<String>, Float> ftrAssocs) {
        this.ftrAssocs = ftrAssocs;
    }

    /**
     * @return the ftrReqs
     */
    public Map<String, Float> getFtrReqs() {
        return ftrReqs;
    }

    /**
     * @param ftrReqs the ftrReqs to set
     */
    public void setFtrReqs(Map<String, Float> ftrReqs) {
        this.ftrReqs = ftrReqs;
    }
    
    public PServerVector getVector(){        
        return vector;
    }
}
