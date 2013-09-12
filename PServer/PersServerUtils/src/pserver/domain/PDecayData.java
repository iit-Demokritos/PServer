/*
 * Copyright 2013 IIT , NCSR Demokritos - http://www.iit.demokritos.gr,
 *                            SciFY NPO - http://www.scify.org
 *
 * This product is part of the PServer Free Software.
 * For more information about PServer visit http://www.pserver-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If this code or its output is used, extended, re-engineered, integrated,
 * or embedded to any extent in another software or hardware, there MUST be
 * an explicit attribution to this work in the resulting source code,
 * the packaging (where such packaging exists), or user interface
 * (where such an interface exists).
 *
 * The attribution must be of the form
 * "Powered by PServer, IIT NCSR Demokritos , SciFY"
 */

package pserver.domain;

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
