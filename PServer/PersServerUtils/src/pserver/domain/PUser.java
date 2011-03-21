/*
 * UserProfile.java
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

    public PUser() {
        this.name = "";
        this.features = new Hashtable();
        this.attributes = new Hashtable();
    }

    public PUser( int featureNum ) {
        this.name = "";
        this.features = new Hashtable( featureNum );
        this.attributes = new Hashtable( featureNum );
    }

    public PUser( String name ) {
        this.name = name;
        this.features = new Hashtable();
        this.attributes = new Hashtable();
    }

    public PUser( String name, int featureNum ) {
        this.name = name;
        if ( featureNum > 0 ) {
            this.features = new Hashtable( featureNum );
            this.attributes = new Hashtable();
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

    public String[] getFeatures() {
        Set<String> keys = features.keySet();
        if( keys.size() ==0 )
            return null;
        String[] keyNames = new String[ keys.size() ];
        int i = 0;
        for ( String key : keys ) {
            keyNames[i] = key;
            i++;
        }
        return keyNames;
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
}
