/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.domain;

/**
 *
 * @author alexm
 */
public class PNumData {

    private String user;
    private String feature;
    private float featureValue;
    private long timeStamp;
    private int sessionId;

    public PNumData() {
        this.user = this.feature = null;
        this.featureValue = 0.0f;
        this.timeStamp = 0;
        this.sessionId = 0;
    }

    public PNumData( String user, String feature, float value, long time, int sid) {
        this.user = user;
        this.feature = feature;
        this.featureValue = value;
        this.timeStamp = time;
        this.sessionId = sid;
    }

    public String getUser() {
        return user;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature( String feature ) {
        this.feature = feature;
    }

    public float getFeatureValue() {
        return featureValue;
    }

    public void setFeatureValue( float featureValue ) {
        this.featureValue = featureValue;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp( long timeStamp ) {
        this.timeStamp = timeStamp;
    }

    /**
     * @return the sessionId
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId( int sessionId ) {
        this.sessionId = sessionId;
    }
}
