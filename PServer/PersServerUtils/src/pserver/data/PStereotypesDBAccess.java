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
package pserver.data;

import java.sql.SQLException;

public class PStereotypesDBAccess {

    private DBAccess dbAccess;
    private Barrier barrier;

    public PStereotypesDBAccess(DBAccess db) throws SQLException {
        dbAccess = db;
    }

    public int addUserToStereotype(String user, String stereotype, float degree, String clientName) throws SQLException {
        int rowsAffected = updateStereotypeWithUser(clientName, stereotype, user, degree);
        String query = "insert into stereotype_users " + "(su_user, su_stereotype, su_degree, FK_psclient) values ('" + user + "', '" + stereotype + "', " + degree + ",'" + clientName + "')";
        rowsAffected += dbAccess.executeUpdate(query);
        return rowsAffected;
    }

    public int updateStereotypeWithUser(String clientName, String stereotype, String user, float degree) throws SQLException {
        String subSelect = "SELECT '" + stereotype + "',up_feature, 0,'" + clientName + "' FROM " + DBAccess.UPROFILE_TABLE
                + " WHERE up_user ='" + user + "' AND FK_psclient='" + clientName + "'";

        String sql = "INSERT IGNORE INTO " + DBAccess.STEREOTYPE_PROFILES_TABLE
                + "(" + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE
                + "," + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE
                + "," + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE
                + "," + DBAccess.FIELD_PSCLIENT + ") " + subSelect + "";
        int ret = dbAccess.executeUpdate(sql);

        sql = "UPDATE " + DBAccess.STEREOTYPE_PROFILES_TABLE + "," + DBAccess.UPROFILE_TABLE
                + " SET " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + "=" + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + "+"
                + degree + "*" + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE
                + " WHERE " + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND "
                + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "= " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE;
        ret += dbAccess.executeUpdate(sql);
        return ret;
    }

    public int removeUserFromStereotype(String user, String stereotype, String clientName) throws SQLException {
        float degree = getUserDegree(stereotype, user, clientName);
        String sql = "DELETE FROM stereotype_users WHERE " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + " = '" + user + "' AND " + DBAccess.FIELD_PSCLIENT + " = '" + clientName + "'";
        int total = dbAccess.executeUpdate(sql);
        total += updateStereotypeWithRemovedUser(clientName, stereotype, user, degree);
        return total;
    }

    /**
     * Returns the degree of the corellation of the user for the specified
     * stereotype
     *
     * @param stereotype the stereotype
     * @param user the user name
     * @param clientName the pserver client
     */
    public float getUserDegree(String stereotype, String user, String clientName) throws SQLException {
        String sql = "SELECT " + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_DEGREE + " FROM " + DBAccess.STEREOTYPE_USERS_TABLE + " WHERE "
                + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.STEREOTYPE_USERS_TABLE_FIELD_USER + "='" + user + "' AND "
                + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        PServerResultSet rs = dbAccess.executeQuery(sql);
        rs.next();
        float val = rs.getRs().getFloat(1);
        rs.close();
        return val;
    }

    private int updateStereotypeWithRemovedUser(String clientName, String stereotype, String user, float degree) throws SQLException {
        String sql = "UPDATE " + DBAccess.STEREOTYPE_PROFILES_TABLE + "," + DBAccess.UPROFILE_TABLE
                + " SET " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + "=" + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_NUMVALUE + "-"
                + degree + "*" + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE
                + " WHERE " + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_USER + "='" + user + "' AND "
                + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_STEREOTYPE + "='" + stereotype + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.FIELD_PSCLIENT + "='" + clientName + "' AND "
                + DBAccess.UPROFILE_TABLE + "." + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + "= " + DBAccess.STEREOTYPE_PROFILES_TABLE + "." + DBAccess.STEREOTYPE_PROFILES_TABLE_FIELD_FEATURE;
        return dbAccess.executeUpdate(sql);
    }
}
