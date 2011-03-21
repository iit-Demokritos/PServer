/*
 * Maintaines resultSet for users
 * 
 */
package pserver.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import pserver.domain.PUser;

/**
 *
 * @author alexm
 */
public class PUserResultSet {

    private DBAccess dbAccess;
    private Connection con;
    private Statement stmt;
    private String clientName;
    private Vector<PUser> users;
    private int vectorPtr;
    private int firstTotalPtr;
    private int totalPtr;
    private int cacheSize;
    private String ftrs[];

    public PUserResultSet(DBAccess dbAccess, String clientName, int cacheSize, String ftrs[]) throws SQLException {
        this.dbAccess = dbAccess;
        vectorPtr = totalPtr = firstTotalPtr = 0;
        this.cacheSize = cacheSize;
        this.users = new Vector(cacheSize);

        con = dbAccess.newConnection();
        stmt = con.createStatement();
        this.clientName = clientName;

        this.ftrs = ftrs;
        loadNextPackOfUsers();
    }

    public PUserResultSet(DBAccess dbAccess, String clientName, int cacheSize, String firstUser, String ftrs[]) throws SQLException {
        this.dbAccess = dbAccess;
        vectorPtr = 0;
        this.cacheSize = cacheSize;
        this.users = new Vector(cacheSize);

        con = dbAccess.newConnection();
        stmt = con.createStatement();
        this.clientName = clientName;

        String sql = "SELECT COUNT(*) FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName
                + "' AND " + DBAccess.USER_TABLE_FIELD_USER + "<='" + firstUser + "'";

        ResultSet tmp = stmt.executeQuery(sql);
        tmp.next();
        this.firstTotalPtr = totalPtr = tmp.getInt(1);

        this.ftrs = ftrs;
        loadNextPackOfUsers();
    }

    PUserResultSet(DBAccess dbAccess, String clientName, int cacheSize, int limit, int offset, String ftrs[]) throws SQLException {
        this.dbAccess = dbAccess;
        vectorPtr = 0;
        totalPtr = firstTotalPtr = offset;
        this.cacheSize = cacheSize;
        this.users = new Vector(cacheSize);

        con = dbAccess.newConnection();
        stmt = con.createStatement();
        this.clientName = clientName;

        this.ftrs = ftrs;
        loadNextPackOfUsers();
    }

    public PUser next() throws SQLException {
        if (users.size() == 0) {
            return null;
        }
        if (vectorPtr >= users.size()) {
            loadNextPackOfUsers();
            if (users.size() == 0) {
                return null;
            }
        }
        PUser user = users.get(vectorPtr);
        vectorPtr++;
        return user;

    }

    public void close() throws SQLException {
        stmt.close();
        con.close();
    }

    void restart(int newOffset) throws SQLException {
        this.vectorPtr = 0;
        this.totalPtr = firstTotalPtr + newOffset;
        loadNextPackOfUsers();
    }

    private void loadNextPackOfUsers() throws SQLException {
        System.out.println( "Total Memory " + Runtime.getRuntime().totalMemory() );
        System.out.println( "Free Memory " + Runtime.getRuntime().freeMemory() );
        this.users.clear();

        String sql = "SELECT * FROM " + DBAccess.USER_TABLE + " WHERE " + DBAccess.FIELD_PSCLIENT + "='" + clientName
                + "' ORDER BY " + DBAccess.USER_TABLE_FIELD_USER + " LIMIT " + cacheSize + " OFFSET " + totalPtr;

        ResultSet result = stmt.executeQuery(sql);
        if (result.next() == false) {
            return;
        }
        String firstUser = result.getString(DBAccess.USER_TABLE_FIELD_USER);
        result.last();
        String lastUser = result.getString(DBAccess.USER_TABLE_FIELD_USER);
        result.close();
        //System.out.println( "first user is " + firstUser + " and last is " + lastUser  );

        sql = "SELECT * FROM " + DBAccess.UPROFILE_TABLE + " WHERE " + DBAccess.UPROFILE_TABLE_FIELD_USER + " >= '" + firstUser + "' AND " + DBAccess.UPROFILE_TABLE_FIELD_USER + " <='" + lastUser + "' AND " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        if (ftrs != null) {
            for (int i = 0; i < ftrs.length; i++) {
                sql += " AND " + DBAccess.UPROFILE_TABLE_FIELD_FEATURE + " LIKE ? ";
            }
        }
        sql += " ORDER BY " + DBAccess.UPROFILE_TABLE_FIELD_USER;
        
        PreparedStatement stmtProfile = this.con.prepareStatement(sql);
        if (ftrs != null) {
            for (int i = 0; i < ftrs.length; i++) {
                stmtProfile.setString(i + 1, ftrs[i]);
            }
        }

        //System.out.println( stmtProfile.toString() );

        ResultSet profiles = stmtProfile.executeQuery();

        String prevUser = "";
        PUser userObj = null;

        int i = 0;
        while (profiles.next()) {
            String user = profiles.getString(DBAccess.UPROFILE_TABLE_FIELD_USER);
            if (prevUser.equals(user) == false) {
                if (userObj != null) {
                    this.users.add(userObj);
                }
                userObj = new PUser(user);
                prevUser = String.copyValueOf(user.toCharArray());
            }
            userObj.setFeature(profiles.getString(DBAccess.UPROFILE_TABLE_FIELD_FEATURE), profiles.getFloat(DBAccess.UPROFILE_TABLE_FIELD_NUMVALUE));
            i++;
        }
        users.add(userObj);
        this.vectorPtr = 0;
        totalPtr += this.cacheSize;
        profiles.close();
        stmtProfile.close();
    }
}
