/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.domain;

import java.sql.Timestamp;

/**
 *
 * @author alexm
 */
public class PDecayData {

    private String userName;
    private String feature;
    private long timeStamp;
    private int sessionId;

    public PDecayData() {
        this.userName = feature = null;
        this.timeStamp = System.currentTimeMillis();
        this.sessionId = 0;
    }

    public PDecayData( String user, String feature, long timeStamp, int sid ) {
        this.userName = user;
        this.feature = feature;
        this.timeStamp = timeStamp;
        this.sessionId = sid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName( String userName ) {
        this.userName = userName;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature( String feature ) {
        this.feature = feature;
    }

    public long getTimestamp() {
        return timeStamp;
    }

    public void setTimestamp( long timestamp ) {
        this.timeStamp = timestamp;
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
