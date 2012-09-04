/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.domain;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author alex
 */
public class PServerVector {
    private String name;
    private Map<String, Float> vectorValues;

    public PServerVector() {
        vectorValues = new HashMap<String, Float>();
    }

    public PServerVector( int vectorSize ) {
        vectorValues = new HashMap<String, Float>( vectorSize );
    }    
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the vectorValues
     */
    public Map<String, Float> getVectorValues() {
        return vectorValues;
    }

    /**
     * @param vectorValues the vectorValues to set
     */
    public void setVectorValues(Map<String, Float> vectorValues) {
        this.vectorValues = vectorValues;
    }

    
}
