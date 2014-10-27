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
package pserver.data;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import pserver.domain.PDecayData;
import pserver.domain.PAttribute;
import pserver.domain.PFeature;
import pserver.domain.PNumData;
import pserver.domain.PStereotype;
import pserver.domain.PUser;

/**
 *
 * @author scify
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 * @author Nick Zorbas <nickzorb@gmail.con>
 */
public class DBAccess {

    public static final int RELATION_TEMPORARY = 0;
    public static final int RELATION_SIMILARITY = 1;
    public static final int RELATION_BINARY_SIMILARITY = 2;
    public static final int RELATION_PHYSICAL = 3;
    public static final int RELATION_PHYSICAL_NORMALIZED = 4;
    public static final int RELATION_FEATURE_SIMILARITY = 5;
    public static final int STATISTICS_FREQUENCY = 0;
    public static final int STATISTICS_FREQUENCY_NORMALIZED = 1;
    public static final String FIELD_PSCLIENT = "FK_psclient";
    public static final String ATTRIBUTES_TABLE = "attributes";
    public static final String ATTRIBUTES_TABLE_FIELD_ATTRIBUTE_NAME = "attr_name";
    public static final String ATTRIBUTES_TABLE_FIELD_DEF_VALUE = "attr_defvalue";
    public static final String COMMUNITIES_TABLE = "communities";
    public static final String COMMUNITIES_TABLE_FIELD_COMMUNITY = "community";
    public static final String COMMUNITY_PROFILES_TABLE = "community_profiles";
    public static final String COMMUNITY_PROFILES_TABLE_FIELD_COMMUNITY = "community";
    public static final String COMMUNITY_PROFILES_TABLE_FIELD_FEATURE = "feature";
    public static final String COMMUNITY_PROFILES_TABLE_FIELD_FEATURE_VALUE = "feature_value";
    public static final String FTRGROUPS_TABLE = "ftrgroups";
    public static final String FTRGROUPS_TABLE_FIELD_FTRGROUP = "ftrgroup";
    public static final String FTRGROUP_FEATURES_TABLE = "ftrgroup_features";
    public static final String FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP = "feature_group";
    public static final String FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_NAME = "feature_name";
    public static final String FTRGROUPSUSERS_TABLE = "ftrgroup_users";
    public static final String FTRGROUPSUSERS_TABLE_FIELD_GROUP = "feature_group";
    public static final String FTRGROUPSUSERS_TABLE_FIELD_USER = "user_name";
    public static final String FTRGROUPSFTRS_TABLE = "ftrgroup_features";
    public static final String FTRGROUPSFTRS_TABLE_FIELD_GROUP = "feature_group";
    public static final String FTRGROUPSFTRS_TABLE_TABLE_FIELD_FTR = "feature_name";
    public static final String DECAY_DATA_TABLE = "decay_data";
    public static final String DECAY_DATA_TABLE_FIELD_USER = "dd_user";
    public static final String DECAY_DATA_TABLE_FIELD_FEATURE = "dd_feature";
    public static final String DECAY_DATA_TABLE_FIELD_TIMESTAMP = "dd_timestamp";
    public static final String DECAY_DATA_TABLE_FIELD_SESSION = "FK_session";
    public static final String NUM_DATA_TABLE = "num_data";
    public static final String NUM_DATA_TABLE_FIELD_USER = "nd_user";
    public static final String NUM_DATA_TABLE_FIELD_FEATURE = "nd_feature";
    public static final String NUM_DATA_TABLE_FIELD_VALUE = "nd_value";
    public static final String NUM_DATA_TABLE_FIELD_TIMESTAMP = "nd_timestamp";
    public static final String NUM_DATA_TABLE_FIELD_SESSION = "sessionId";
    public static final String USER_TABLE = "users";
    public static final String USER_TABLE_FIELD_USER = "user";
    public static final String USER_TABLE_FIELD_DECAY_FACTOR = "decay_factor";
    public static final String FEATURE_TABLE = "up_features";
    public static final String FEATURE_TABLE_FIELD_FEATURE = "uf_feature";
    public static final String FEATURE_TABLE_FIELD_DEFVALUE = "uf_defvalue";
    public static final String FEATURE_TABLE_FIELD_VALUE_NUMDEFVALUE = "uf_numdefvalue";
    public static final String UPROFILE_TABLE = "user_profiles";
    public static final String UPROFILE_TABLE_FIELD_USER = "up_user";
    public static final String UPROFILE_TABLE_FIELD_FEATURE = "up_feature";
    public static final String UPROFILE_TABLE_FIELD_VALUE = "up_numvalue";
    public static final String UPROFILE_TABLE_FIELD_NUMVALUE = "up_numvalue";
    public static final String CFPROFILE_TABLE = "collaborative_profiles";
    public static final String CFPROFILE_TABLE_FIELD_USER = "cp_user";
    public static final String CFPROFILE_TABLE_FIELD_FEATURE = "cp_feature";
    public static final String CFPROFILE_TABLE_FIELD_VALUE = "cp_value";
    public static final String CFPROFILE_TABLE_FIELD_NUMVALUE = "cp_numvalue";
    public static final String SESSIONS_TABLE = "user_sessions";
    public static final String SESSIONS_TABLE_FIELD_ID = "id";
    public static final String SESSIONS_TABLE_FIELD_USER = "FK_user";
    public static final String UASSOCIATIONS_TABLE = "user_associations";
    public static final String UASSOCIATIONS_TABLE_FIELD_SRC = "user_src";
    public static final String UASSOCIATIONS_TABLE_FIELD_DST = "user_dst";
    public static final String UASSOCIATIONS_TABLE_FIELD_WEIGHT = "weight";
    public static final String UASSOCIATIONS_TABLE_FIELD_TYPE = "type";
    public static final String UFTRASSOCIATIONS_TABLE = "user_feature_associations";
    public static final String UFTRASSOCIATIONS_TABLE_FIELD_SRC = "ftr_src";
    public static final String UFTRASSOCIATIONS_TABLE_FIELD_DST = "ftr_dst";
    public static final String UFTRASSOCIATIONS_TABLE_FIELD_WEIGHT = "weight";
    public static final String UFTRASSOCIATIONS_TABLE_FIELD_TYPE = "type";
    public static final String UFTRASSOCIATIONS_TABLE_FIELD_USR = "user";
    public static final String CFFTRASSOCIATIONS_TABLE = "collaborative_feature_associations";
    public static final String CFFTRASSOCIATIONS_TABLE_FIELD_SRC = "ftr_src";
    public static final String CFFTRASSOCIATIONS_TABLE_FIELD_DST = "ftr_dst";
    public static final String CFFTRASSOCIATIONS_TABLE_FIELD_WEIGHT = "weight";
    public static final String CFFTRASSOCIATIONS_TABLE_FIELD_TYPE = "type";
    public static final String CFFTRASSOCIATIONS_TABLE_FIELD_USR = "profile";
    public static final String UATTR_TABLE = "user_attributes";
    public static final String UATTR_TABLE_FIELD_USER = "user";
    public static final String UATTR_TABLE_FIELD_ATTRIBUTE = "attribute";
    public static final String UATTR_TABLE_FIELD_VALUE = "attribute_value";
    public static final String UCOMMUNITY_TABLE = "user_community";
    public static final String UCOMMUNITY_TABLE_FIELD_USER = "user";
    public static final String UCOMMUNITY_TABLE_FIELD_COMMUNITY = "community";
    public static final String FEATURE_STATISTICS_TABLE = "user_feature_statistics";
    public static final String FEATURE_STATISTICS_TABLE_FIELD_USER = "user";
    public static final String FEATURE_STATISTICS_TABLE_FIELD_FEATURE = "ftr";
    public static final String FEATURE_STATISTICS_TABLE_FIELD_VALUE = "value";
    public static final String FEATURE_STATISTICS_TABLE_FIELD_TYPE = "type";
    public static final String CFFEATURE_STATISTICS_TABLE = "collaborative_feature_statistics";
    public static final String CFFEATURE_STATISTICS_TABLE_FIELD_USER = "profile";
    public static final String CFFEATURE_STATISTICS_TABLE_FIELD_FEATURE = "ftr";
    public static final String CFFEATURE_STATISTICS_TABLE_FIELD_VALUE = "value";
    public static final String CFFEATURE_STATISTICS_TABLE_FIELD_TYPE = "type";
    public static final String STEREOTYPE_TABLE = "stereotypes";
    public static final String STEREOTYPE_TABLE_FIELD_STEREOTYPE = "st_stereotype";
    public static final String STEREOTYPE_TABLE_FIELD_RULE = "st_rule";
    public static final String STEREOTYPE_USERS_TABLE = "stereotype_users";
    public static final String STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE = "su_stereotype";
    public static final String STEREOTYPE_USERS_TABLE_FIELD_USER = "su_user";
    public static final String STEREOTYPE_USERS_TABLE_FIELD_DEGREE = "su_degree";
    public static final String STEREOTYPE_PROFILES_TABLE = "stereotype_profiles";
    public static final String STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE = "sp_stereotype";
    public static final String STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE = "sp_feature";
    public static final String STEREOTYPE_PROFILES_TABLE_FIELD_VALUE = "sp_value";
    public static final String STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE = "sp_numvalue";
    public static final String STEREOTYPE_STATISTICS_TABLE = "stereotype_feature_statistics";
    public static final String STEREOTYPE_STATISTICS_TABLE_FIELD_STEREOTYPE = "stereotype";
    public static final String STEREOTYPE_STATISTICS_TABLE_FIELD_FEATURE = "ftr";
    public static final String STEREOTYPE_STATISTICS_TABLE_FIELD_TYPE = "type";
    public static final String STEREOTYPE_STATISTICS_TABLE_FIELD_VALUE = "value";
    public static final String SFTRASSOCIATIONS_TABLE = "stereotype_feature_associations";
    public static final String SFTRASSOCIATIONS_TABLE_FIELD_SRC = "ftr_src";
    public static final String SFTRASSOCIATIONS_TABLE_FIELD_DST = "ftr_dst";
    public static final String SFTRASSOCIATIONS_TABLE_FIELD_WEIGHT = "weight";
    public static final String SFTRASSOCIATIONS_TABLE_FIELD_TYPE = "type";
    public static final String SFTRASSOCIATIONS_TABLE_FIELD_STEREOTYPE = "stereotype";
    public static final String FIELD_PSUSERID = "user";
    private Connection connection = null;
    private Statement statement = null;
    private boolean connected;
    private String url;         //JDBC string specifying the data source
    private String user;        //The database user
    private String pass;        //The database passsword

    private static DBAccess instance = null;
    private static DBAccess ConInstance = null;
    private static DBAccess EmptyInstance = null;

    private DBAccess() {
        this.connected = false;
    }

    private DBAccess(String url, String user, String pass) {
        this.connected = false;
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    private DBAccess(Connection con) {
        this.connected = true;
        this.connection = con;
    }

    public static DBAccess getInstance(String url, String user, String pass) {
        if (instance == null) {
            return new DBAccess(url, user, pass);
        } else {
            return instance;
        }
    }

    public static DBAccess getInstance(Connection con) {
        if (ConInstance == null) {
            return new DBAccess(con);
        } else {
            return ConInstance;
        }
    }

    public static DBAccess getInstance() {
        if (EmptyInstance == null) {
            return new DBAccess();
        } else {
            return EmptyInstance;
        }
    }

    public Connection newConnection() throws SQLException {
        Connection con = DriverManager.getConnection(this.url, this.user, this.pass);
        return con;
    }

    public void setAutoCommit(boolean state) throws SQLException {
        this.connection.setAutoCommit(state);
    }

    public void connect() throws SQLException {
        if (this.connected == true) {
            return;
        }
        this.connection = DriverManager.getConnection(this.url, this.user, this.pass);
        this.connected = true;
    }

    public void reconnect() throws SQLException {
        this.connection = DriverManager.getConnection(this.url, this.user, this.pass);
        this.connected = true;
    }

    public void disconnect() throws SQLException {
        if (this.connected == true) {
            connection.close();
        }
    }

    public DatabaseMetaData getMetadata() throws SQLException {
        if (this.connected) {
            return this.connection.getMetaData();
        } else {
            return null;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void commit() throws SQLException {
        this.connection.commit();
    }

    public void rollback() throws SQLException {
        this.connection.rollback();
    }

    /**
     * checks if the client credentials are valid
     */
    public boolean checkClientCredentials(String name, String pass) throws SQLException {
        Statement stmt = this.connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT salt FROM pserver_clients where name='" + name + "'");
        if (!rs.first()) {
            return false;
        }
        String salt = rs.getString("salt");
        rs.close();
        rs = stmt.executeQuery("SELECT * FROM pserver_clients where name='" + name + "' and password=SHA2('" + salt + pass + "',256);");
        return rs.first() ? true : false;
    }

    /**
     * INSERT new user in the database
     */
    public int insertNewUser(PUser user, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        String sql = "INSERT INTO " + USER_TABLE + " ( " + USER_TABLE_FIELD_USER + "," + FIELD_PSCLIENT + " ) VALUES ('" + user.getName() + "', '" + clientName + "')";
        int rows = stmt.executeUpdate(sql);
        Set<String> features = user.getFeatures();

        for (String ftr : features) {
            sql = "REPLACE INTO " + UPROFILE_TABLE + " ( " + UPROFILE_TABLE_FIELD_USER + "," + UPROFILE_TABLE_FIELD_FEATURE + "," + UPROFILE_TABLE_FIELD_NUMVALUE + "," + FIELD_PSCLIENT + " ) VALUES ('" + user.getName() + "','" + ftr + "'," + user.getFeatureValue(ftr) + ",'" + clientName + "')";
            rows += stmt.executeUpdate(sql);
        }

        String[] attributes = user.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            sql = "REPLACE INTO " + UATTR_TABLE + " ( " + UATTR_TABLE_FIELD_USER + "," + UATTR_TABLE_FIELD_ATTRIBUTE + "," + UATTR_TABLE_FIELD_VALUE + "," + FIELD_PSCLIENT + " ) VALUES ('" + user.getName() + "','" + attributes[ i] + "','" + user.getAttributeValue(attributes[ i]) + "','" + clientName + "')";
            rows += stmt.executeUpdate(sql);
        }

        stmt.close();
        return rows;
    }

    /**
     * INSERT new attributes in the database
     */
    public int insertNewAttribute(PAttribute attribute, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        String query = "INSERT INTO attributes ( attr_name, attr_defvalue, " + FIELD_PSCLIENT + " ) VALUES ('" + attribute.getName() + "', '" + attribute.getDefValue() + "', '" + clientName + "')";
        //System.out.println( "Executing SQL " + query );

        int rows = stmt.executeUpdate(query);
        stmt.close();
        return rows;
    }

    /**
     * INSERT new features in the database
     */
    public int insertNewFeature(PFeature feature, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        String query = "INSERT INTO up_features " + "(uf_feature, uf_defvalue, uf_numdefvalue, " + FIELD_PSCLIENT + " ) VALUES ('" + feature.getName() + "', '" + feature.getStrDefValue() + "', " + feature.getDefValue() + ",'" + clientName + "')";
        //System.out.println( "query = " + query );
        int rows = stmt.executeUpdate(query);
        stmt.close();
        return rows;
    }

    public ArrayList<PFeature> getAllFeatures(String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        String query = "SELECT * FROM up_features WHERE " + FIELD_PSCLIENT + " = '" + clientName + "'";
        ResultSet rs = stmt.executeQuery(query);
        ArrayList<PFeature> features = new ArrayList<PFeature>();
        while (rs.next()) {
            PFeature feature = new PFeature();

            feature.setName(rs.getString("uf_feature"));
            feature.setDefValue(rs.getFloat("uf_defvalue"));
            feature.setValue(feature.getDefValue());

            features.add(feature);
        }
        stmt.close();
        return features;
    }

    public List<PStereotype> getStereotypesOfUser(PUser user, String clientName) throws SQLException {
        LinkedList<PStereotype> list = new LinkedList<PStereotype>();
        Statement stmt = this.connection.createStatement();
        String query = "SELECT * FROM stereotype_users WHERE su_user ='" + user.getName() + "' AND " + FIELD_PSCLIENT + " = '" + clientName + "'";
        stmt.executeQuery(query);
        return list;
    }

    /**
     * INSERTs data INTO decay_data table
     */
    public int insertNewDecayData(PDecayData data, String clientName) throws SQLException {
        int rowsAffected = 0;
        Statement stmt = this.connection.createStatement();
        //INSERT all (user, feature, timestamp) tuple
        String query = "INSERT INTO decay_data (dd_user, dd_feature, dd_timestamp, " + FIELD_PSCLIENT + ", FK_session ) VALUES ('" + data.getUserName() + "', '" + data.getFeature() + "', '" + data.getTimestamp() + "' ,'" + clientName + "'," + data.getSessionId() + ")";
        //System.out.println( query );

        rowsAffected = stmt.executeUpdate(query);
        stmt.close();
        return rowsAffected;
    }

    public int insertDelayedNewDecayData(PDecayData data, String clientName) throws SQLException {
        int rowsAffected = 0;
        Statement stmt = this.connection.createStatement();
        //INSERT all (user, feature, timestamp) tuple
        String query = "INSERT DELAYED INTO decay_data (dd_user, dd_feature, dd_timestamp, " + FIELD_PSCLIENT + ", FK_session ) VALUES ('" + data.getUserName() + "', '" + data.getFeature() + "', '" + data.getTimestamp() + "' ,'" + clientName + "'," + data.getSessionId() + ")";
        //System.out.println( query );

        rowsAffected = stmt.executeUpdate(query);
        stmt.close();
        return rowsAffected;
    }

    /**
     * @param sqlQuery
     * @return
     * @throws java.sql.SQLException
     */
    public int insertNewNumData(PNumData numData, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        String query = "INSERT INTO num_data (nd_user, nd_feature, nd_timestamp, nd_value, sessionId, " + FIELD_PSCLIENT + " ) VALUES ('" + numData.getUser() + "', '" + numData.getFeature() + "', " + numData.getTimeStamp() + ", " + numData.getFeatureValue() + "," + numData.getSessionId() + ",'" + clientName + "')";
        int rows = stmt.executeUpdate(query);
        stmt.close();
        return rows;
    }

    public int insertDelayedNewNumData(PNumData numData, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        String query = "INSERT DELAYED INTO num_data (nd_user, nd_feature, nd_timestamp, nd_value, sessionId, " + FIELD_PSCLIENT + " ) VALUES ('" + numData.getUser() + "', '" + numData.getFeature() + "', " + numData.getTimeStamp() + ", " + numData.getFeatureValue() + "," + numData.getSessionId() + ",'" + clientName + "')";
        int rows = stmt.executeUpdate(query);
        stmt.close();
        return rows;
    }

    public PNumDataResultSet getNumDataFromSession(Statement stmt, int minSessionId, String clientName) throws SQLException {
        String sql = "SELECT * FROM " + DBAccess.NUM_DATA_TABLE + " WHERE " + DBAccess.NUM_DATA_TABLE_FIELD_SESSION + " >= " + minSessionId + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        ResultSet rs = stmt.executeQuery(sql);
        return new PNumDataResultSet(stmt, rs);
    }

    public PNumDataResultSet getNumDataFromTime(Statement stmt, long minTimeStamp, String clientName) throws SQLException {
        String sql = "SELECT * FROM " + DBAccess.NUM_DATA_TABLE + " WHERE " + DBAccess.NUM_DATA_TABLE_FIELD_TIMESTAMP + " >= " + minTimeStamp + " AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        ResultSet rs = stmt.executeQuery(sql);
        return new PNumDataResultSet(stmt, rs);
    }

    /**
     * @return the new session id
     * @throws java.sql.SQLException
     */
    public int newSessionId(String user, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        String query = "INSERT INTO " + SESSIONS_TABLE + " ( " + SESSIONS_TABLE_FIELD_USER + ", " + FIELD_PSCLIENT + " ) VALUES ( '" + user + "', '" + clientName + "')";
        stmt.executeUpdate(query);
        ResultSet rs = stmt.getGeneratedKeys();
        rs.next();
        int newId = rs.getInt(1);
        rs.close();
        stmt.close();
        return newId;
    }

    /**
     * @return the new session id
     * @throws java.sql.SQLException
     */
    public int addSessionId(String user, int sid, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        String query = "INSERT INTO user_sessions ( nd_user, id, " + FIELD_PSCLIENT + " ) VALUES ( '" + user + "'," + sid + ", '" + clientName + "')";
        stmt.executeUpdate(query);
        int newId = stmt.getGeneratedKeys().getInt("id");
        stmt.close();
        return newId;
    }

    /**
     * @return the new session id
     * @throws java.sql.SQLException
     */
    public int getLastSessionId(String user, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        String query = "SELECT MAX(id) AS id FROM user_sessions WHERE FK_user = '" + user + "' AND " + FIELD_PSCLIENT + " = '" + clientName + "'";
        ResultSet rs = stmt.executeQuery(query);
        int lastId = 0;
        if (rs.next()) {
            lastId = rs.getInt("id");
        } else {
            lastId = newSessionId(user, clientName);
        }
        rs.close();
        stmt.close();
        return lastId;
    }

    /**
     * @return the maximum timestamp from decay and num data session id
     * @throws java.sql.SQLException
     */
    public long getLastDecayTimeStamp(String usr, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();

        String query = "SELECT MAX(" + DECAY_DATA_TABLE_FIELD_TIMESTAMP + ") FROM " + DECAY_DATA_TABLE + " WHERE " + FIELD_PSCLIENT + " = '" + clientName + "'";
        ResultSet rs = stmt.executeQuery(query);
        long maxTimeStamp = 0;
        if (rs.next()) {
            maxTimeStamp = rs.getLong(1);
        }

        rs.close();

        query = "SELECT MAX(" + NUM_DATA_TABLE_FIELD_TIMESTAMP + ") FROM " + NUM_DATA_TABLE + " WHERE " + FIELD_PSCLIENT + " = '" + clientName + "'";
        rs = stmt.executeQuery(query);
        if (rs.next()) {
            maxTimeStamp = Math.max(maxTimeStamp, rs.getLong(1));
        }

        return maxTimeStamp;
    }

    public int updateStereotypesFromUserAction(String user, String feature, float val, String clientName) throws SQLException {
        Statement stmt = this.connection.createStatement();
        Statement stmt2 = this.connection.createStatement();
        String query = "SELECT su_stereotype, su_degree FROM stereotype_users WHERE su_user = '" + user + "' AND " + FIELD_PSCLIENT + " = '" + clientName + "'";
        ResultSet rs = stmt.executeQuery(query);
        int total = 0;
        while (rs.next()) {
            float degree = rs.getFloat("su_degree");
            String stereotype = rs.getString("su_stereotype");
            float incVal = val * degree;
            String sql = "UPDATE stereotype_profiles SET sp_numvalue = sp_numvalue + " + incVal + " WHERE sp_feature = '" + feature + "' AND " + FIELD_PSCLIENT + " = '" + clientName + "' AND sp_stereotype = '" + stereotype + "'";
            int upNum = stmt2.executeUpdate(sql);
            if (upNum == 0) {
                sql = "INSERT INTO stereotype_profiles ( sp_stereotype, sp_feature, sp_value, sp_numvalue, " + FIELD_PSCLIENT + " ) " + " SELECT '" + stereotype + "', uf_feature, uf_defvalue, uf_numdefvalue, FK_psclient FROM up_features WHERE uf_feature = '" + feature + "' AND FK_psclient = '" + clientName + "'";
                stmt2.executeUpdate(sql);
                sql = "UPDATE stereotype_profiles SET sp_numvalue = sp_numvalue + " + incVal + " WHERE sp_feature = '" + feature + "' AND " + FIELD_PSCLIENT + " = '" + clientName + "' AND sp_stereotype = '" + stereotype + "'";
                upNum = stmt2.executeUpdate(sql);
            }
            total += upNum;
        }
        stmt2.close();
        stmt.close();
        return total;
    }

    public int removeUserFromStereotypes(String user, String clientName) throws SQLException {
        int total = 0;
        Statement stmt = this.connection.createStatement();
        String sql = "SELECT up_feature, up_numvalue FROM user_profiles WHERE up_user = '" + user + "' AND " + FIELD_PSCLIENT + " = '" + clientName + "'";
        ResultSet rs = stmt.executeQuery(sql);
        LinkedList<PFeature> features = new LinkedList<PFeature>();
        while (rs.next()) {
            PFeature feature = new PFeature();
            feature.setName(rs.getString("up_feature"));
            feature.setValue(rs.getFloat("up_numvalue"));
            features.add(feature);
        }
        rs.close();
        if (features.size() == 0) {
            stmt.close();
            return 0;
        }
        Statement stmt2 = this.connection.createStatement();
        for (PFeature feature : features) {
            sql = "SELECT su_stereotype, su_degree FROM stereotype_users WHERE su_user = '" + user + "' AND " + FIELD_PSCLIENT + " = '" + clientName + "'";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                float degree = rs.getFloat("su_degree");
                String stereotype = rs.getString("su_stereotype");
                float incVal = feature.getValue() * degree;
                sql = "UPDATE stereotype_profiles SET sp_numvalue = sp_numvalue - " + incVal + " WHERE sp_feature = '" + feature.getName() + "' AND " + FIELD_PSCLIENT + " = '" + clientName + "' AND sp_stereotype = '" + stereotype + "'";
                total += stmt2.executeUpdate(sql);
            }
            rs.close();
        }
        //rs.close();
        stmt2.close();
        stmt.close();
        return total;
    }

    public int clearUserCommunities(String clientName) throws SQLException {
        int total = 0;
        Statement stmt = this.connection.createStatement();
        total += stmt.executeUpdate("DELETE FROM user_community "
                + "where " + FIELD_PSCLIENT + "='" + clientName + "' ");
        total += stmt.executeUpdate("DELETE FROM community_profiles "
                + "where " + FIELD_PSCLIENT + "='" + clientName + "' ");
        total += stmt.executeUpdate("DELETE FROM communities "
                + "where " + FIELD_PSCLIENT + "='" + clientName + "' ");
        stmt.close();
        return total;
    }

    public int DeleteUserCommunities(String clientName, String pattern) throws SQLException {
        int total = 0;
        Statement stmt = this.connection.createStatement();
        total += stmt.executeUpdate("DELETE FROM user_community where "
                + FIELD_PSCLIENT + "='" + clientName + "' AND "
                + COMMUNITIES_TABLE_FIELD_COMMUNITY + " LIKE '" + pattern + "'");
        total += stmt.executeUpdate("DELETE FROM community_profiles where "
                + FIELD_PSCLIENT + "='" + clientName + "' AND "
                + COMMUNITIES_TABLE_FIELD_COMMUNITY + " LIKE '" + pattern + "'");
        total += stmt.executeUpdate("DELETE FROM communities where "
                + FIELD_PSCLIENT + "='" + clientName + "' AND "
                + COMMUNITIES_TABLE_FIELD_COMMUNITY + " LIKE '" + pattern + "'");
        stmt.close();
        return total;
    }

    public int clearFeatureGroups(String clientName) throws SQLException {
        int total = 0;
        Statement stmt = this.connection.createStatement();
        total += stmt.executeUpdate("DELETE FROM ftrgroup_features "
                + "WHERE " + FIELD_PSCLIENT + " = '" + clientName + "'");
        total += stmt.executeUpdate("DELETE FROM ftrgroups "
                + "WHERE " + FIELD_PSCLIENT + " = '" + clientName + "'");
        stmt.close();
        return total;
    }

    public int DeleteFeatureCommunities(String clientName, String pattern) throws SQLException {
        int total = 0;
        Statement stmt = this.connection.createStatement();
        total += stmt.executeUpdate("DELETE FROM ftrgroup_features WHERE "
                + FIELD_PSCLIENT + "='" + clientName + "' AND "
                + FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP + " LIKE '" + pattern + "'");
        total += stmt.executeUpdate("DELETE FROM ftrgroups WHERE "
                + FIELD_PSCLIENT + "='" + clientName + "' AND "
                 + FTRGROUPS_TABLE_FIELD_FTRGROUP + " LIKE '" + pattern + "'");
        stmt.close();
        return total;
    }

    /**
     * @param sqlQuery
     * @return
     * @throws java.sql.SQLException
     */
    public PServerResultSet executeQuery(String sqlQuery) throws SQLException {
        Statement stmt = this.connection.createStatement();
        ResultSet rs = stmt.executeQuery(sqlQuery);
        return new PServerResultSet(stmt, rs);
    }

    public int executeUpdate(String sqlQuery) throws SQLException {
        Statement stmt = this.connection.createStatement();
        int rows = stmt.executeUpdate(sqlQuery);
        stmt.close();
        return rows;
    }

    public int executeUpdate(LinkedList<String> sqlQueries) throws SQLException {
        Statement stmt = this.connection.createStatement();
        int rows = 0;
        for (String sqlQuery : sqlQueries) {
            rows += stmt.executeUpdate(sqlQuery);
        }
        stmt.close();
        return rows;
    }

    public boolean execute(String sql) throws SQLException {
        Statement stmt = this.connection.createStatement();
        boolean ret = stmt.execute(sql);
        stmt.close();
        return ret;
    }

    //general utility methods
    public static boolean legalFtrOrAttrName(String ftrName) {
        //which names are legal for features depends
        //on the syntax of feature patterns. Patterns
        //and names must not introduce ambiguity.
        //If a name is the same as a pattern, the name
        //cannot be specified individually anymore.
        //Therefore, this function depends on the
        //'ftrPatternCondition' that defines pattern.
        if (ftrName == null) {
            return false;
        }
        if (ftrName.length() == 0) {
            return false;
        }
        if (ftrName.endsWith("*")) {
            return false;
        }
        if (ftrName.startsWith("*")) {
            return false;
        }
        return true;
    }

    public static String xmlHeader(String xslPath) {
        //returns the header of an XML response,
        //including the XSL reference, if any.
        //If 'xslPath' is null, no reference to
        //an XSL file will be included
        StringBuffer header = new StringBuffer();
        header.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        if (xslPath != null) {
            header.append("<?xml-stylesheet type=\"text/xsl\" href=\"");
            header.append(xslPath);
            header.append("\"?>\n");
        }
        return header.substring(0);
    }

    public static Long timestampPattern(String strTimestamp) {
        //The pattern is: - | <date/time>, where '-' means
        //that the PServer asks the system for current
        //date/time which is used as a timestamp, otherwise
        //the <date/time> provided is used. The format must
        //be a long integer giving the milliseconds passed
        //since January 1st, 1970 00:00:00.000 GMT. The same
        //format is used for the returned Long. If the
        //<date/time> provided in 'strTimestamp' could not
        //be converted to numeric, null is returned.
        if (strTimestamp == null) {
            return null;
        }
        Long timestamp;
        if (strTimestamp.equals("-")) //get timestamp from system
        {
            timestamp = new Long(System.currentTimeMillis());
        } else {  //try to convert to numeric, if failed null is returned
            long num;
            try {
                num = Long.parseLong(strTimestamp);
            } catch (NumberFormatException e) {
                return null;
            }
            timestamp = new Long(num);
        }
        return timestamp;
    }

    public static Float strToNum(String str) {
        //converts a string to a numeric value of a
        //double. Returns null if unable to convert.
        if (str == null) {
            return new Float(0.0);
        }
        double num;
        try {
            num = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
        return new Float(num);
    }

    public static String strToNumStr(String str) {
        //converts a string to a numeric value of
        //a double. Returns the double as a string,
        //or the string "null" if unable to convert.
        Float num = strToNum(str);
        if (num == null) {
            return new String("null");
        }
        return num.toString();
    }

    public static String ftrGroupCondition(String group) {
        //A feature group represents all features
        //whose pathname starts with the group name.
        //Transforms 'group' INTO a string to be used
        //after SQL: "...where <field>" + condition
        if (group == null) {
            return null;
        }
        String condition = " like '" + group + ".%'";
        return condition;
    }

    public static int numPatternCondition(String pattern) {
        //The pattern is: * | <integer>, where '*' means all.
        //Transforms 'pattern' INTO an integer that
        //will decide the number of results to be retrieved.
        //Returns -1 if cannot parse integer.
        if (pattern == null) {
            return -1;
        }
        int num;
        if (pattern.equals("*")) {
            num = Integer.MAX_VALUE;
        } //retrieve all
        else {
            try {
                num = Integer.parseInt(pattern);
            } catch (NumberFormatException e) {
                num = -1;
            }
        }
        return num;
    }

    //pattern to condition / pattern resolution methods
    public static String ftrPatternCondition(String pattern) {
        //The pattern is: * | name[.*], where * matches all.
        //Transforms 'pattern' INTO a string to be used
        //after SQL: "...where <field>" + condition
        if (pattern == null) {
            return null;
        }
        String condition;
        //if (pattern.endsWith("*"))
        //    condition = " like '" + pattern.substring(0, pattern.length()-1) + "%'";
        if (pattern.contains("*")) {
            condition = " like '" + pattern.replaceAll("\\*", "%") + "'";
        } else {
            condition = "='" + pattern + "'";
        }
        return condition;
    }

    public static String formatDouble(Double num) {
        //format a double by removing trailing
        //zeros, and return it as a string
        if (num == null) {
            return null;
        }
        StringBuffer strNum = new StringBuffer(num.toString());
        if (strNum.length() == 0) {
            return null;
        }  //length should not be 0
        //check if decimal part exists
        int comma = (num.toString()).indexOf('.');
        if (comma == -1) {
            comma = (num.toString()).indexOf(',');
        }
        if (comma == -1) {
            return strNum.substring(0);
        }  //no decimal part, return all of it
        //remove trailing zeros
        int i = strNum.length() - 1;  //last char, min 0
        while (i > 0 && strNum.charAt(i) == '0') {
            i--;
        }  //last nonzero other than first
        strNum.delete(i + 1, strNum.length());
        //remove comma if followed by zeros only
        if (strNum.charAt(i) == '.' || strNum.charAt(i) == ',') {
            strNum.deleteCharAt(i);
        }
        //if no chars left, assume zero
        if (strNum.length() == 0) {
            strNum.append("0");
        }
        //return string
        return strNum.substring(0);
    }

    public static String srtPatternCondition(String pattern) {
        //The pattern is: asc | desc.
        //Transforms 'pattern' INTO a string to be used
        //after SQL: "...order by <field>" + condition
        if (pattern == null) {
            return "";
        }
        String condition;
        if (pattern.equals("asc")) {
            condition = "";
        } //asc is default in SQL
        else //any other case - desc is default here
        {
            condition = " desc";
        }
        return condition;
    }

    public static String whrPatternCondition(String pattern, String clientName) {
        //The pattern is: * | <SQL part following WHERE>,
        //('where' is excluded), with '*' meaning ALL.
        //A special syntax is used: : for = and | for <space>.
        //Transforms 'pattern' INTO a string to be used
        //in the place of SQL WHERE clause (including 'where')
        if (pattern == null) {
            return " WHERE " + FIELD_PSCLIENT + " = '" + clientName + "'";
        }
        ;
        String condition;
        if (pattern.equals("*") || pattern.equals("")) {
            condition = " WHERE " + FIELD_PSCLIENT + "='" + clientName + "'";
        } //no WHERE clause
        else {   //convert to normal syntax
            String step1 = pattern.replace(':', '=');
            String step2 = step1.replace('|', ' ');
            step2 = step2.replace('*', '%');
            condition = " WHERE " + step2 + " AND " + FIELD_PSCLIENT + "='" + clientName + "'";
        }
        return condition;
    }

    public static boolean legalUsrName(String usrName) {
        //legal names for users cannot be empty string
        if (usrName == null) {
            return false;
        }
        if (usrName.length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean legalStrName(String strName) {
        //which names are legal for stereotypes depends
        //on the syntax of stereotype patterns. Patterns
        //and names must not introduce ambiguity.
        //If a name is the same as a pattern, the name
        //cannot be specified individually anymore.
        //The stereotype pattern depends on the operation,
        //so there is not a single function defining the
        //pattern (as is the case of 'ftrPatternCondition').
        if (strName == null) {
            return false;
        }
        if (strName.length() == 0) {
            return false;
        }
//        if (strName.equals("*")) {
//            return false;
//        }
        return true;
    }

    //TODO injection
    //DONE wrap bellow methods in an object and/or move them to DBAccess
    //These might seem unnecesary but I believe they help in organizing the flow
    //and could be used all arround the program inside a wrapper class that will
    //fully utilize them. If nothing else they use StringBuilder which is way
    //faster than the String concatenation used before.
    public static StringBuilder buildInsertStatement(String table, String[] values) {
        return buildInsertStatement(table, null, values);
    }

    public static StringBuilder buildInsertStatement(String table, String[] columns, String[] values) {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(table).append(" ");
        if (columns != null) {
            query.append("(");
            for (String s : columns) {
                query.append(s).append(", ");
            }
            query.setLength(query.length() - 2);
            query.append(") ");
        }
        query.append("VALUES ('");
        for (String s : values) {
            query.append(s).append("', '");
        }
        query.setLength(query.length() - 4);
        query.append("')");
        return query;
    }

    public static StringBuilder buildSelectStatement(String table) {
        String[] tables = {table};
        return buildSelectStatement(tables, null, null, null, null, null);
    }

    public static StringBuilder buildSelectStatement(String table, String[] where) {
        String[] tables = {table};
        return buildSelectStatement(tables, null, null, null, null, where);
    }

    public static StringBuilder buildSelectStatement(String table, String[] columns, String[] where) {
        String[] tables = {table};
        return buildSelectStatement(tables, null, null, null, columns, where);
    }

    public static StringBuilder buildSelectStatement(String[] tables, String[] aliases, String[] joinTypes, String[] joins, String[] columns, String[] where) {
        StringBuilder query = new StringBuilder("SELECT ");
        if (columns == null) {
            query.append("* ");
        } else {
            for (String s : columns) {
                query.append(s).append(", ");
            }
            query.setLength(query.length() - 2);
            query.append(" ");
        }
        query.append("FROM ");
        if (tables.length != 1) {
            query.append("(").append(tables[0]).append(") as ");
            query.append(aliases[0]);
            for (int i = 0; i < tables.length - 1; i++) {
                query.append(" ").append(joinTypes[i]).append(" (");
                query.append(tables[i + 1]).append(") as ");
                query.append(aliases[i + 1]);
                query.append(" ON ").append(joins[i]).append(" ");
            }
        } else {
            query.append(tables[0]);
        }
        if (where != null) {
            query.append(" WHERE ");
            for (String s : where) {
                query.append(s).append(" AND ");
            }
            query.setLength(query.length() - 5);
        }
        return query;
    }

    public static StringBuilder buildUpdateStatement(String table, String[] columns, String[] values) {
        String[] tables = {table};
        return buildUpdateStatement(tables, null, null, null, columns, values, null);
    }

    public static StringBuilder buildUpdateStatement(String table, String[] columns, String[] values, String[] where) {
        String[] tables = {table};
        return buildUpdateStatement(tables, null, null, null, columns, values, where);
    }

    public static StringBuilder buildUpdateStatement(String[] tables, String[] aliases, String[] joinTypes, String[] joins, String[] columns, String[] values, String[] where) {
        StringBuilder query = new StringBuilder("UPDATE ");
        if (tables.length != 1) {
            query.append("(").append(tables[0]).append(") as ");
            query.append(aliases[0]);
            for (int i = 0; i < tables.length - 1; i++) {
                query.append(" ").append(joinTypes[i]).append(" (");
                query.append(tables[i + 1]).append(") as ");
                query.append(aliases[i + 1]);
                query.append(" ON ").append(joins[i]).append(" ");
            }
        } else {
            query.append(tables[0]);
        }
        query.append(" SET ");
        for (int i = 0; i < columns.length; i++) {
            query.append(columns[i]).append("='").append(values[i]);
            query.append("', ");
        }
        query.setLength(query.length() - 2);
        if (where != null) {
            query.append(" WHERE ");
            for (String s : where) {
                query.append(s).append(" AND ");
            }
            query.setLength(query.length() - 5);
        }
        return query;
    }

    public static StringBuilder buildDeleteStatement(String table, String where) {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(table);
        if (where != null) {
            query.append(" WHERE ").append(where);
        }
        return query;
    }

    public static StringBuilder buildDeleteStatement(String table, String[] where) {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(table);
        if (where != null) {
            query.append(" WHERE ");
            for (String clause : where) {
                if (clause != null && !clause.equals("")) {
                    query.append(clause).append(" AND ");
                }
            }
            query.setLength(query.length() - 5);
        }
        return query;
    }
}
