package pserver.pservlets;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import pserver.WebServer;
import pserver.domain.PDecayData;
import pserver.domain.PAttribute;
import pserver.domain.PFeature;
import pserver.domain.PNumData;
import pserver.data.DBAccess;
import pserver.data.PServerResultSet;
import pserver.data.VectorMap;
import pserver.logic.PSReqWorker;

/**
 *
 * @author alexm
 */
public class Personal implements pserver.pservlets.PService {

    public String getMimeType() {
        return pserver.pservlets.PService.xml;
    }

    public void init( String[] params ) throws Exception {
    }

    public int service( VectorMap parameters, StringBuffer response, DBAccess dbAccess ) {
        int respCode;
        VectorMap queryParam;

        StringBuffer respBody = response;
        queryParam = parameters;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        if( clntIdx == -1 ){
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Parameter clnt does not exist");
            return respCode;  //no point in proceeding
        }
        String clientName = (String) queryParam.getVal(clntIdx);
        clientName = clientName.substring(0, clientName.indexOf('|'));
        queryParam.updateVal(clientName, clntIdx);
        //System.out.println( "client name = " + queryParam.getVal( clntIdx ) );

        //commANDs of PERS_MODE here!
        //find 'com' query param (case independent)
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //if 'com' param not present, request is invalid
        if ( comIdx == -1 ) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error( "-Request commAND does not exist" );
            return respCode;  //no point in proceeding
        }
        //recognize commAND encoded in request
        String com = ( String ) queryParam.getVal( comIdx );
        //operations of features
        if ( com.equalsIgnoreCase( "addftr" ) ) {       //add new feature(s)
            respCode = comPersAddFtr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "addattr" ) ) { //add new attribute(s)
            respCode = comPersAddAttr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "remftr" ) ) {  //remove feature(s)
            respCode = comPersRemFtr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "remAttr" ) ) {  //remove attributes
            respCode = comPersRemAttr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "setdef" ) || com.equalsIgnoreCase( "setftrdef" ) ) {  //update the def value of ftr
            respCode = comPersSetFtrDef( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "setattrdef" ) ) {  //update the def value of ftr
            respCode = comPersSetAttrDef( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "getdef" ) || com.equalsIgnoreCase( "getftrdef" ) ) {  //get ftr(s) AND def val(s)
            respCode = comPersGetFtrDef( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "getattrdef" ) ) {
            respCode = comPersGetAttrDef( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "setusr" ) ) {  //add AND update user
            respCode = comPersSetUsr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "incval" ) ) {  //increment numeric values         
            respCode = comPersIncVal( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "setattr" ) ) {
            respCode = comPersSetAttr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "getusrs" ) ) {  //get feature values for a user
            respCode = comPersGetUsrs( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "getusr" ) || com.equalsIgnoreCase( "getusrftr" ) ) {  //get feature values for a user
            respCode = comPersGetUsrFtr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "getusrattr" ) ) {  //get feature values for a user
            respCode = comPersGetUsrAttr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "sqlusr" ) || com.equalsIgnoreCase( "sqlftrusr" ) ) {  //specify conditions AND select users
            respCode = comPersSqlUsrFtr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "sqlattrusr" ) ) {
            respCode = comPersSqlUsrAttr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "remusr" ) ) {  //remove user(s)
            respCode = comPersRemUsr( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "setdcy" ) ) {  //add AND update decay feature groups
            respCode = comPersSetDcy( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "getdrt" ) ) {  //get decay rate for a group
            respCode = comPersGetDrt( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "remdcy" ) ) {  //remove decay feature groups
            respCode = comPersRemDcy( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "addddt" ) ) {  //add new decay data
            respCode = comPersAddDdt( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "sqlddt" ) ) {  //retrieve decay data under conditions
            respCode = comPersSqlDdt( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "remddt" ) ) {  //remove decay data
            respCode = comPersRemDdt( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "caldcy" ) ) {  //calculate decay values for a user
            respCode = comPersCalDcy( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "addndt" ) ) {  //add new numeric data
            respCode = comPersAddNdt( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "sqlndt" ) ) {  //retrieve numeric data under conditions
            respCode = comPersSqlNdt( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "remndt" ) ) {  //remove numeric data
            respCode = comPersRemNdt( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "getavg" ) ) {  //calculate average values for a user
            respCode = comPersGetAvg( queryParam, respBody, dbAccess );
        } else {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error( "-Request commAND not recognized" );
        }

        return respCode;
    }

    private int comPersAddAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {

        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );//transaction guarantees integrity
            //the new (feature, def value) pairs must be inserted, AND
            //the user attributes must be expANDed with the new features
            //-start transaction body
            success &= execPersAddAttr( queryParam, respBody, dbAccess );
            success &= persExpAndUserAttributes( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersAddAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert each (attribute, def value) in a new row
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                    //'feature' cannot be empty string, 'queryParam' does not allow it                    
                    String attrName = ( ( String ) queryParam.getKey( i ) );
                    if ( DBAccess.legalFtrOrAttrName( attrName ) == true ) {  //check if name is legal
                        PAttribute attr = new PAttribute();
                        attr.setName( attrName );
                        String defValue = ( String ) queryParam.getVal( i );
                        attr.setValue( defValue );
                        attr.setDefValue( defValue );

                        rowsAffected += dbAccess.insertNewAttribute( attr, clientName );
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if ( !success ) {
                    break;
                }
            }
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem inserting to DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows inserted: " + rowsAffected );
        //format response body
        //response will be used only in case of success        
        respBody.append( "<?xml version=\"1.0\"?>\n" );
        respBody.append( "<result>\n" );
        respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
        respBody.append( "</result>" );
        return success;
    }

    private boolean persExpAndUserAttributes( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert new features in user profiles accordingly
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                    //'attribute' cannot be empty string, 'queryParam' does not allow it
                    String attribute = ( String ) queryParam.getKey( i );
                    String defValue = ( String ) queryParam.getVal( i );
                    //if (db.compareTo("ACCESS") == 0) {  //database type is MS-Access
                    query = "insert into user_attributes " + "(user, attribute, attribute_value, FK_psclient)" + " select user, '" + attribute + "', '" + defValue + "', '" + clientName + "' from users WHERE FK_psclient='" + clientName + "'";
                    dbAccess.executeUpdate( query );
                }
            }
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem inserting to DB: " + e );
        }
        WebServer.win.log.debug( "-Rows inserted in user_attributes: " + rowsAffected );
        return success;
    }

    //-addddt
    //template: pers?com=addddt&usr=<usr>&<ftr_1>=<timestamp_pattern_1>&<ftr_2>=...
    //          Order of query params may be important: among features whose
    //          timestamp pattern delegates the timestamping to PServer, the
    //          leftmost features are considered first when assigning a
    //          timestamp (as if being visited first by the user).
    //pattern : - | <date/time>, WHERE '-' means that the PServer assigns a
    //          transaction timestamp, otherwise the <date/time> provided by
    //          the application is used. The <date/time> format must be a
    //          long integer giving the milliseconds passed since January
    //          1st, 1970 00:00:00.000 GMT.
    //descript: for the specified user AND for each 'ftr' parameter in the
    //          request, a new tuple (user, feature, timestamp) is inserted
    //          in table 'decay_data'. In case the timestamp for a feature
    //          is provided by the application, it denotes when the (user,
    //          feature) interaction took place. In case the timestamping is
    //          is delegated to PServer, it is the date/time of the
    //          insertion of the tuple in the DB (the transaction time).
    //          In both cases the timestamps can be used to put feature
    //          interactions in the order they occured. For this however,
    //          timestamping of all features in a decay feature group must
    //          be consistent AND correct (either PServer or application
    //          generated, not both / mixed). If a request contains many
    //          features AND some of them are assigned to be timestamped
    //          by PServer, some may end up with identical timestamps.
    //          This does not create any problem as long as the features
    //          are different. The DB however cannot record more than one
    //          identical (user, feature, timestamp) tuples. In case some
    //          timestamps are the same, the order of the corresponding
    //          interactions will be arbitrary. If a timestamp pattern is
    //          not legal, or if the request leads to two or more identical
    //          tuples, or if a feature 'ftr' does not already exist in the
    //          'up_features' table (feature column of two tables is linked
    //          through a referential integrity constraint), 401 is returned.
    //          If the error code 401 is returned then no changes have taken
    //          place in the DB.
    //example : pers?com=addddt&usr=kostas&advert.link16=-&page5.combo8.choice2=1005854668670
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    //          Note that the contents of the table 'decay_data' are not
    //          constraint or in any other way affected by the table
    //          'decay_groups'.
    private int comPersAddDdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //first connect to DB
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success;
            dbAccess.setAutoCommit( false );  //transaction guarantees integrity
            //-start transaction body
            success = execPersAddDdt( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //problem with client request
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersAddDdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        int sidIdx = queryParam.qpIndexOfKeyNoCase( "sid" );
        if ( usrIdx == -1 ) {
            return false;
        }
        String user = ( String ) queryParam.getVal( usrIdx );
        int sid;
        if ( sidIdx != -1 ) {
            try {
                sid = Integer.parseInt( ( String ) queryParam.getVal( sidIdx ) );
            } catch ( NumberFormatException e ) {
                Logger.getLogger( Personal.class.getName() ).log( Level.SEVERE, null, e );
                return false;
            }
        } else {
            try {
                sid = dbAccess.getLastSessionId( user, clientName );
            } catch ( SQLException ex ) {
                Logger.getLogger( Personal.class.getName() ).log( Level.SEVERE, null, ex );
                return false;
            }
        }
        //execute request
        boolean success = true;
        int rowsAffected = 0;
        try {
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != usrIdx && i != clntIdx && i != sidIdx ) {  //'com' AND 'usr' query parameters excluded
                    //get current parameter pair
                    String feature = ( String ) queryParam.getKey( i );
                    String strTimestamp = ( String ) queryParam.getVal( i );
                    Long timestamp = DBAccess.timestampPattern( strTimestamp );
                    if ( timestamp != null ) {  //if null, 'timestamp' not numeric, misspelled request
                        //insert current (user, feature, timestamp) tuple
                        PDecayData data = new PDecayData( user, feature, timestamp, sid );
                        rowsAffected += dbAccess.insertNewDecayData( data, clientName );
                    //else if timestamp is null
                    } else {
                        success = false;
                    }  //misspelled request, abort AND rollback
                }
                if ( !success ) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        //close statement
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem inserting to DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows inserted: " + rowsAffected );
        //format response body
        //response will be used only in case of success        
        respBody.append( "<?xml version=\"1.0\"?>\n" );
        respBody.append( "<result>\n" );
        respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
        respBody.append( "</result>" );
        return success;
    }

    //-addftr
    //template: pers?com=addftr&<ftr_1>=<def_val_1>&<ftr_2>=...
    //          Order of query params is not important. Feature
    //          names must not end with '*' to be legal.
    //descript: inserts new (feature, default value) pairs. Must
    //          be used while initializing the personalization service
    //          (features must exist before users enter the system)!
    //          For features that are added at a later stage, the
    //          profile of all users is updated to include them as well.
    //          If one or more feature names already exist in DB, or
    //          if one or more feature names are not legal names,
    //          code 401 (request error) will be returned. If the
    //          error code 401 is returned then none of the feature
    //          pairs in the request has been inserted in the DB.
    //example : pers?com=addftr&lang.en=0&lang.fr=0&lang.gr=1&gender=male
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : no response body exists.
    private int comPersAddFtr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //first connect to DB
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );  //transaction guarantees integrity
            //the new (feature, def value) pairs must be inserted, AND
            //the user profiles must be expANDed with the new features
            //-start transaction body
            success &= execPersAddFtr( queryParam, respBody, dbAccess );
            //success &= persExpANDProfiles( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.warn( "-DB Transaction problem: " + e );
        }
        //disconnect from DB anyway
        return respCode;
    }

    private boolean execPersAddFtr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert each (feature, def value) in a new row
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                    //'feature' cannot be empty string, 'queryParam' does not allow it
                    String feature = ( String ) queryParam.getKey( i );
                    if ( DBAccess.legalFtrOrAttrName( feature ) ) {  //check if name is legal
                        String defValue = ( String ) queryParam.getVal( i );
                        PFeature featureObj = new PFeature( feature, defValue, defValue );
                        rowsAffected += dbAccess.insertNewFeature( featureObj, clientName );
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if ( !success ) {
                    break;
                }  //discontinue loop, rollback
            }
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem inserting to DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows inserted: " + rowsAffected );
        //format response body
        //response will be used only in case of success
        respBody.append( "<?xml version=\"1.0\"?>\n" );
        respBody.append( "<result>\n" );
        respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
        respBody.append( "</result>" );
        return success;
    }

    /*private boolean persExpANDProfiles ( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
    //request properties
    int qpSize = queryParam.size();
    int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
    String clientName = ( String ) queryParam.getVal( clntIdx );
    int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
    //execute request
    boolean success = true;
    String query;
    int rowsAffected = 0;
    try {
    //insert new features in user profiles accordingly
    for ( int i = 0 ; i < qpSize ; i++ ) {
    if ( i != comIdx && i != clntIdx) {  //'com' query parameter excluded
    //'feature' cannot be empty string, 'queryParam' does not allow it
    String feature = ( String ) queryParam.getKey( i );
    String defValue = ( String ) queryParam.getVal( i );
    String numDefValue = DBAccess.strToNumStr( defValue );  //numeric version of def value
    //if (db.compareTo("ACCESS") == 0) {  //database type is MS-Access
    query = "insert into user_profiles " + "(up_user, up_feature, up_value, up_numvalue, FK_psclient)" //+ " select distinct up_user, '"
    + " select user, '" + feature + "', '" + defValue + "', " + numDefValue + ", '" + clientName + "'" //+ " from user_profiles";
    + " from users WHERE FK_psclient='" + clientName + "'";
    rowsAffected += dbAccess.executeUpdate( query );
    }
    }
    } catch ( SQLException e ) {
    success = false;
    WebServer.win.log.debug( "-Problem inserting to DB: " + e );
    }
    WebServer.win.log.debug( "-Rows inserted in user_profiles: " + rowsAffected );
    return success;
    }*/

    //-addndt
    //template: pers?com=addndt&usr=<usr>[&tms=<timestamp_pattern>]&<ftr_1>=<value_1>&<ftr_2>=...
    //          Order of query params are not important: the timestamp applies
    //          to all features in request. Feature values must be numeric.
    //pattern : - | <date/time>, WHERE '-' means that the PServer assigns a
    //          transaction timestamp, otherwise the <date/time> provided by
    //          the application is used. The <date/time> format must be a
    //          long integer giving the milliseconds passed since January
    //          1st, 1970 00:00:00.000 GMT. The pattern is optional. In
    //          case it is missing, its value is assumed '-' (PServer assigns
    //          a transaction timestamp).
    //descript: for the specified user AND for each 'ftr' parameter in the
    //          request, a new tuple (user, feature, timestamp, value) is
    //          inserted in table 'num_data'. In case the timestamp is
    //          provided by the application, it denotes when all the (user,
    //          feature) interactions took place. In case the timestamping is
    //          is delegated to PServer, it is the date/time of the
    //          insertion of the tuples in the DB (the transaction time).
    //          In both cases the timestamps can be used to put feature
    //          interactions in the order they occured. For this however,
    //          timestamping of all features must be consistent AND correct
    //          (either PServer or application generated, not both / mixed).
    //          If a request contains many features they will have identical
    //          timestamps. This does not create any problem as long as the
    //          features are different. The DB however cannot record more
    //          than one identical (user, feature, timestamp) tuples. For
    //          timestamps that are the same, the order of the corresponding
    //          interactions will be arbitrary. If a timestamp pattern is
    //          not legal, or if the request leads to two or more identical
    //          tuples, or the value of some feature is not numeric, 401 is
    //          returned. If the error code 401 is returned then no changes
    //          have taken place in the DB.
    //example : pers?com=addndt&usr=kostas&tms=1005854668670&laptop.weight=2.55
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersAddNdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //first connect to DB
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success;
            dbAccess.setAutoCommit( false );  //transaction guarantees integrity
            //-start transaction body
            success = execPersAddNdt( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //problem with client request
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersAddNdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        int sidIdx = queryParam.qpIndexOfKeyNoCase( "sid" );
        if ( usrIdx == -1 ) {
            return false;
        }
        String user = ( String ) queryParam.getVal( usrIdx );
        int sid;
        if ( sidIdx == -1 ) {
            try {
                sid = dbAccess.getLastSessionId( user, clientName );
            } catch ( SQLException ex ) {
                Logger.getLogger( Personal.class.getName() ).log( Level.SEVERE, null, ex );
                return false;
            }
        } else {
            try {
                sid = Integer.parseInt( ( String ) queryParam.getVal( sidIdx ) );
            } catch ( NumberFormatException e ) {
                Logger.getLogger( Personal.class.getName() ).log( Level.SEVERE, null, e );
                return false;
            }
        }

        int tmsIdx = queryParam.qpIndexOfKeyNoCase( "tms" );
        String strTimestamp;
        if ( tmsIdx == -1 ) {
            strTimestamp = new String( "-" );
        } else {
            strTimestamp = ( String ) queryParam.getVal( tmsIdx );
        }
        Long timestamp = DBAccess.timestampPattern( strTimestamp );
        if ( timestamp == null ) {
            return false;
        }
        //if null, 'timestamp' not numeric, misspelled request
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert all (user, feature, timestamp, value) tuples
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != usrIdx && i != tmsIdx && i != clntIdx ) {  //'com', 'usr', 'tms', 'clnt' query parameters excluded
                    //get current parameter pair
                    String feature = ( String ) queryParam.getKey( i );
                    String strValue = ( String ) queryParam.getVal( i );
                    Float value = DBAccess.strToNum( strValue );
                    if ( value != null ) {  //if null, 'value' not numeric, misspelled request
                        //insert current (user, feature, timestamp, value) tuple
                        PNumData numData = new PNumData( user, feature, value.floatValue(), timestamp, sid );
                        rowsAffected += dbAccess.insertNewNumData( numData, clientName );
                    } //else if value is null
                    else {
                        success = false;
                    }  //misspelled request, abort AND rollback
                }
                if ( !success ) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        //close statement
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem inserting to DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows inserted: " + rowsAffected );
        return success;
    }

    //-caldcy
    //template: pers?com=caldcy&usr=<user>&grp=<ftr_group>[&drt=<decay_rate>]
    //              [&num=<num_pattern>]
    //          Order of query params is not important. Query params
    //          'drt' AND 'num' are optional. If ommited, decay rate
    //          is looked up in table 'decay_groups', AND if it is
    //          not found there either, it defaults to 0. If ommited,
    //          'num' defaults to '*'.
    //pattern : For 'num', * | <integer>.
    //descript: for the specified user, AND for the features matching
    //          the specified feature group (that is, for the features
    //          whose pathname starts with <ftr_group>), the (user, feature,
    //          timestamp) tuples are retrieved from decay data, sorted
    //          by timestamp descenting (10->1). Based on those data, a
    //          decay value is calculated, using a special formula, for
    //          each of the distinct features in the group visited by
    //          the user. The decay value of a feature depends on how
    //          many times the user selected the feature in comparison
    //          to the rest of the features in the group, AND how recent
    //          those selections are in comparison again to the rest of
    //          the features in the group. High decay values mean frequent
    //          AND / or recent selections of a feature. The higher the
    //          decay value, the more interested the user is in a feature.
    //          The features of the group are then sorted by their decay
    //          value descenting. If 'num' is specified as an <integer>,
    //          then the first <integer> (feature, decay value) tuples
    //          will be returned. Else, if 'num' is ommited or if it is
    //          set to '*', all (feature, decay value) of the group will
    //          be returned. If no feature in 'decay_data' matches the
    //          specified group, or no user matches the specified user,
    //          or 'num' < 1, the result will not have any 'row' elements
    //          (200 OK will still be returned). Note that decay value
    //          has only a meaning when compared with other decay values
    //          from within the same calculation. Also check the 'addddt'
    //          for further information on the validity of decay data used
    //          to calculate decay values. An important issue is the decay
    //          rate, which is a number between 1 AND 0, both inclusive.
    //          The higher the rate, the more easily users forget (loose
    //          interest in) visited features. If rate is set to 0, the user
    //          does not forget (or loose interest) AND the decay mechanism
    //          is reduced to sorting features based only on how frequently
    //          a user has visited them (not when). In this case, the decay
    //          value calculated by the server for any feature of the group
    //          for a specified user, is the total number of visits the
    //          user paid to the feature. The decay rate can be set in the
    //          request, but it is optional. If it is ommited, the specified
    //          feature group is looked up in table 'decay_groups', AND if
    //          it exists the associated rate is extracted from there. If
    //          no rate is defined AND the specified group does not exist
    //          in 'decay_groups', 0 is used as a default rate. In case
    //          the specified rate is out of range ([0,1]), or rate is not
    //          numeric, or the specified number pattern 'num' is illegal,
    //          error code 401 will be returned.
    //example : pers?com=caldcy&usr=takis&grp=advert.banners
    //          pers?com=caldcy&usr=petros&grp=page10.links&num=*
    //          pers?com=caldcy&usr=takis&grp=page10&drt=1&num=3
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/decay_values.xsl"?>
    //          <result>
    //              <row><ftr>feature</ftr><decay_val>decay value</decay_val></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    //          Note that the formula used for calculating the decay
    //          is explained in the paper "Adaptation to drifting user's
    //          interest" by Ivan Koychev AND Ingo Schwab.
    private int comPersCalDcy( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersCalDcy( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersCalDcy( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        if ( qpSize < 3 || qpSize > 5 ) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        int grpIdx = queryParam.qpIndexOfKeyNoCase( "grp" );
        int drtIdx = queryParam.qpIndexOfKeyNoCase( "drt" );
        int numIdx = queryParam.qpIndexOfKeyNoCase( "num" );
        if ( usrIdx == -1 || grpIdx == -1 ) {
            return false;
        }  //must exist
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String user = ( String ) queryParam.getVal( usrIdx );
        String group = ( String ) queryParam.getVal( grpIdx );
        //transform group to a condition that matches features
        String ftrCondition = DBAccess.ftrGroupCondition( group );
        //decay rate query param: decide use of default, convert to numeric, validate
        String rateStr;
        if ( drtIdx == -1 ) {                       //decay rate absent
            rateStr = getDecayRate( dbAccess, group, clientName );  //look it up in DB
            if ( rateStr == null ) {
                rateStr = "0";
            }   //not in DB, use default value
        } else //decay rate specified in request
        {
            rateStr = ( String ) queryParam.getVal( drtIdx );
        }
        Float rateDbl = DBAccess.strToNum( rateStr );                        //converts string to Double
        if ( rateDbl == null ) {
            return false;
        }  //if null, 'rate' not numeric, discontinue request
        double rate_dbl = rateDbl.doubleValue();
        if ( rate_dbl < 0 || rate_dbl > 1 ) {
            return false;
        }                  //discontinue request
        float rate = ( float ) rate_dbl;
        //'num' query param: decide use of default, convert to numeric, validate
        String numOfResultsStr = ( numIdx == -1 ) ? "*" : ( String ) queryParam.getVal( numIdx );
        int numOfResults = DBAccess.numPatternCondition( numOfResultsStr );
        if ( numOfResults == -1 ) {
            return false;
        }         //'num' not numeric, discontinue request
        //variables for the calculation of decay values
        VectorMap ftrVisits = null;       //user visiting features: (feature, timestamp)
        VectorMap ftrDecayValues = null;  //(feature, decay value) pairs
        //populate 'ftrVisits' with (feature, timestamp) pairs,
        //for specified user AND feature group, ordered by timestamp desc
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //prepare query
            query = "select dd_feature, dd_timestamp from decay_data WHERE dd_user='" + user + "' AND dd_feature" + ftrCondition + " AND FK_psclient='" + clientName + "' order by dd_timestamp desc";
            //count number of tuples
            PServerResultSet rs = dbAccess.executeQuery( query );
            while ( rs.next() ) {
                rowsAffected++;
            }
            //rs.close();
            //retrieve matching records AND populate 'ftrVisits'
            ftrVisits = new VectorMap( rowsAffected );
            //rs = dbAccess.executeQuery( query );        //reopen to reposition cursor at start
            rs.getRs().beforeFirst();
            while ( rs.next() ) {
                String feature = rs.getRs().getString( "dd_feature" );            //cannot be null
                Long timestamp = new Long( rs.getRs().getLong( "dd_timestamp" ) );  //cannot be null
                ftrVisits.add( feature, timestamp );
            }
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        //if array 'ftrVisits' populated, proceed to
        //calculate decay values AND format response
        rowsAffected = 0;
        if ( success ) {  //proceed if no problem with query
            //calculate values: result array with (distinct feature, decay value) pairs
            //for the specified user, ftr group, AND rate, ordered by decay value desc.
            //'ftrVisits' should be null here, but 'ftrDecayValues' is checked for null.
            ftrDecayValues = calcDecayValues( ftrVisits, rate );
            if ( ftrDecayValues == null ) {
                WebServer.win.log.debug( "-Problem calculating decay values" );
                return false;
            }
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/decay_values.xsl" ) );
            respBody.append( "<result>\n" );
            //select first rows as specified by query parameter 'num'
            int i = 0;
            while ( i < ftrDecayValues.size() && i < numOfResults ) {
                String featureVal = ( String ) ftrDecayValues.getKey( i );
                Double valueVal = ( Double ) ftrDecayValues.getVal( i );
                respBody.append( "<row><ftr>" + featureVal +
                        "</ftr><decay_val>" + valueVal.toString() +
                        "</decay_val></row>\n" );
                i += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            rowsAffected = i;
        }
        WebServer.win.log.debug( "-Num of rows returned: " + rowsAffected );
        return success;
    }

    private String getDecayRate( DBAccess dbAccess, String group, String clientName ) {
        //checks DB (table decay_groups) for the feature group 'group',
        //AND if there returns its corresponding decay rate, else null.
        String decayRate = null;  //init to null
        String query;
        try {
            //get decay rate of specified feature group
            //Statement stmt = conn.createStatement();
            query = "select dg_rate from decay_groups WHERE dg_group='" + group + "' AND FK_psclient='" + clientName + "'";
            PServerResultSet rs = dbAccess.executeQuery( query );
            if ( rs.next() ) //one or none 'group' can exist (primary key)
            {
                decayRate = String.valueOf( rs.getRs().getFloat( "dg_rate" ) );
            }  //cannot be null
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Decay rate returned: " + decayRate );  //may be null
        return decayRate;
    }

    private VectorMap calcDecayValues( VectorMap ftrVisits, float rate ) {
        //--------------------------------------------------------------------
        //'ftrVisits' is a sequence of (feature, timestamp) tuples ordered
        //by timestamp descenting (timestamp is not used, but it is included
        //for future use). Those represent the selections of a user (most
        //recently selected feature first). 'rate' is a number between (0,1)
        //both inclusive, that gives a measure of how much the interest of the
        //user for a feature decreases as new features are visited (higher
        //rates mean a stronger decrease of interest). The function returns
        //'ftrDecayValues', a sequence of (feature, decay value) pairs. The
        //features are those in 'ftrVisits', but only one occurrence of each
        //distinct feature exists in 'ftrDecayValues'. The decay value is a
        //metric of the interest of the user towards a feature in comparison
        //with all the other features. In case the rate is 0, the decay value
        //gives simply the number of visits of the user to the feature.
        //In case the rate is higher than 0, the decay rate takes into account
        //not only how many times a feature has been visited, but also when
        //it was visited in comparison to other features. Recently visited
        //AND / or frequently visited features receive higher decay values.
        //The 'ftrDecayValues' array is sorted by decay value descenting.
        //The method used for calculating the decay values is explained in
        //the paper "Adaptation to drifting user's interest" by Ivan Koychev
        //AND Ingo Schwab. In short, the decay value of a feature is the sum
        //of the weights for all visit of the feature. For every visit there
        //corresponds a weight which depends on the rate AND on the order
        //of the visit (number of visit in the sequence of all visits of all
        //features). The weight formula is: -((2*rate)/(total-1))*(i-1)+1+rate
        //WHERE total is the total number of visits the user paid to all
        //features AND i is the sequence number of the current visit.
        //We assume that the first value of i in the formula should be 1.
        //--------------------------------------------------------------------
        if ( ftrVisits == null ) {
            return null;
        }
        int visits = ftrVisits.size();
        VectorMap ftrDecayValues = new VectorMap( visits / 4, visits / 12 );  //a mean of 4 visits per feature?
        //for each visit calculate weight(i, rate) AND
        //update (add to) decay value of corresponding feature
        for ( int i = 0; i < visits; i++ ) {
            double weight = -( ( 2 * rate * i ) / ( visits - 1 ) ) + 1 + rate;  //the weight formula
            String feature = ( String ) ftrVisits.getKey( i );
            int pos = ftrDecayValues.indexOfKey( feature, 0 );
            if ( pos == -1 ) //feature does not exist, insert
            {
                ftrDecayValues.add( feature, new Double( weight ) );
            } else {          //feature exists, update decay value (add weight)
                Double currValue = ( Double ) ftrDecayValues.getVal( pos );
                Double newValue = new Double( currValue.doubleValue() + weight );
                ftrDecayValues.updateVal( newValue, pos );
            }
        }
        ftrDecayValues.trimToSize();  //not necessary, just being tidy
        //sort 'ftrDecayValues' by decay value descenting
        // - the SelectSort algorithm is used for sorting
        for ( int i = ftrDecayValues.size() - 1; i > 0; i-- ) {
            //find index of min entry in range (0, i)
            int min = 0;
            for ( int j = 1; j <= i; j++ ) {
                double jValue = ( ( Double ) ftrDecayValues.getVal( j ) ).doubleValue();
                double minValue = ( ( Double ) ftrDecayValues.getVal( min ) ).doubleValue();
                if ( jValue < minValue ) {
                    min = j;
                }
            }
            //swap min entry with entry at i
            Object tmp;
            tmp = ftrDecayValues.getKey( min );
            ftrDecayValues.updateKey( ftrDecayValues.getKey( i ), min );
            ftrDecayValues.updateKey( tmp, i );
            tmp = ftrDecayValues.getVal( min );
            ftrDecayValues.updateVal( ftrDecayValues.getVal( i ), min );
            ftrDecayValues.updateVal( tmp, i );
        }
        return ftrDecayValues;
    }

    //getattrdef
    //template: pers?com=getattrdef&attr=<attr_pattern>
    //          Order of query params is not important.
    //pattern : * | name[.*], WHERE name is a path expression
    //descript: selects all attributes matching the attribute
    //          pattern AND formats an XML answer with their
    //          names AND def values. If no feature in DB
    //          matches the pattern, the result will not have
    //          any row elements (200 OK will still be returned).
    //          The results are sorted according to ascending
    //          attribute name (a->z, 1->10).
    //example : pers?com=getattrdef&attr=lang.*
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/up_attributes.xsl"?>
    //          <result>
    //              <row><ftr>feature</ftr><defval>default value</defval></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersGetAttrDef( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetAttrDef( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersGetAttrDef( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        if ( qpSize != 3 ) {
            return false;
        }
        int clnt = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int attr = queryParam.qpIndexOfKeyNoCase( "attr" );
        if ( attr == -1 ) {
            return false;
        }
        String clientName = ( String ) queryParam.getVal( clnt );
        String ftrCondition = DBAccess.ftrPatternCondition( ( String ) queryParam.getVal( attr ) );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get def values of matching features            
            query = "SELECT attr_name, attr_defvalue FROM attributes" + " WHERE attr_name" + ftrCondition + " AND FK_psclient='" + clientName + "' ORDER BY attr_name";  //ascending
            /*WebServer.win.log.debug("=============================================");
            WebServer.win.log.debug(query);
            WebServer.win.log.debug(clientName);
            WebServer.win.log.debug("=============================================");*/
            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/up_attributes.xsl" ) );
            respBody.append( "<result>\n" );
            while ( rs.next() ) {
                String feature = rs.getRs().getString( "attr_name" );  //cannot be null
                String defValue = rs.getRs().getString( "attr_defvalue" );
                if ( rs.getRs().wasNull() ) {
                    defValue = "";
                }
                respBody.append( "<row><attr>" + feature +
                        "</attr><defval>" + defValue +
                        "</defval></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows found: " + rowsAffected );
        return success;
    }

    //-getavg
    //template: pers?com=getavg&ftr=<ftr_pattern>[&usr=<usr>]
    //          Order of query params is not important. Query param 'usr'
    //          is optional. If ommited, all users are considered.
    //pattern : * | name[.*], WHERE name is a path expression.
    //descript: for the specified feature(s) AND (maybe) user, the average
    //          value of the numeric feature values 'num_value' is returned.
    //          If no feature in DB matches the pattern, or if user does not
    //          exist, the result will not have any 'row' elements (200 OK
    //          will still be returned).
    //example : pers?com=getavg&ftr=laptop.weight&usr=kostas
    //          pers?com=getavg&ftr=speed.*
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/average_featureval.xsl"?>
    //          <result>
    //              <row><avg>average value</avg></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersGetAvg( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetAvg( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersGetAvg( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        if ( qpSize < 2 || qpSize > 3 ) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int ftrIdx = queryParam.qpIndexOfKeyNoCase( "ftr" );
        if ( ftrIdx == -1 ) {
            return false;
        }  //must exist
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String feature = ( String ) queryParam.getVal( ftrIdx );
        String ftrCondition = DBAccess.ftrPatternCondition( feature );
        //optional query params
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        String usrCondition;
        if ( usrIdx == -1 ) {
            usrCondition = "";
        } else {
            String user = ( String ) queryParam.getVal( usrIdx );
            usrCondition = " AND nd_user = '" + user + "'";
        }
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get average value
            query = "select avg(nd_numvalue) as average from num_data WHERE nd_feature in " + "(select nd_feature from num_data WHERE nd_feature" + ftrCondition + " AND FK_psclient='" + clientName + "' )" + usrCondition + " AND FK_psclient='" + clientName + "'";
            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/average_featureval.xsl" ) );
            respBody.append( "<result>\n" );
            String averageStr;
            rs.next();
            double averageVal = rs.getRs().getDouble( "average" );
            if ( rs.getRs().wasNull() ) {
                averageStr = "";
            } else {
                averageStr = DBAccess.formatDouble( new Double( averageVal ) );
            }
            respBody.append( "<row><avg>" + averageStr + "</avg></row>\n" );
            respBody.append( "</result>" );
            rowsAffected = 1;  //number of result rows
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows returned: " + rowsAffected );
        return success;
    }

    //-getdrt
    //template: pers?com=getdrt[&grp=<ftr_group_1>&grp=...]
    //          Order of query params is not important. Feature group
    //          is a template path expression 'pathname', representing
    //          all features with name starting with 'pathname'. The
    //          'grp' query parameters are optional.
    //descript: selects the corresponding decay rates for the specified
    //          feature groups AND formats an XML answer with the group
    //          names AND rates. If the specified groups do not exist
    //          in DB, the result will not have any row elements (200
    //          OK will still be returned). If no 'grp' query parameters
    //          exist in the request, all the feature groups AND decay
    //          rates in the DB are selected. The results are sorted
    //          according to descenting decay rate (1->0).
    //example : pers?com=getdrt&grp=page10.banners&grp=frequency.allfeatures
    //          pers?com=getdrt   (selects all group, rate pairs in DB)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/decay_groups.xsl"?>
    //          <result>
    //              <row><grp>feature group</grp><rate>decay rate</rate></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersGetDrt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetDrt( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersGetDrt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );

        String clientName = ( String ) queryParam.getKey( clntIdx );
        //concatenate all group names in request
        StringBuffer groups = new StringBuffer();
        for ( int i = 0; i < qpSize; i++ ) {
            if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                //append current group name
                String key = ( String ) queryParam.getKey( i );
                if ( key.equalsIgnoreCase( "grp" ) ) {
                    if ( groups.length() > 0 ) {
                        groups.append( "," );
                    }  //separate with ","
                    groups.append( "'" );
                    groups.append( queryParam.getVal( i ) );
                    groups.append( "'" );
                }
            }
        }
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get decay rates of specified feature groups
            //(or of all groups in DB if no 'grp' query parameters exist in request)            
            if ( qpSize == 1 ) {
                query = "select dg_group, dg_rate from decay_groups WHERE FK_psclient='" + clientName + "' order by dg_rate desc";
            } else {
                query = "select dg_group, dg_rate from decay_groups WHERE dg_group in (" + groups.substring( 0 ) + ") AND FK_psclient='" + clientName + "' order by dg_rate desc";
            }
            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body                        
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/decay_groups.xsl" ) );
            respBody.append( "<result>\n" );
            while ( rs.next() ) {
                String group = rs.getRs().getString( "dg_group" );  //cannot be null
                Float rate = new Float( rs.getRs().getFloat( "dg_rate" ) );    //ditto
                respBody.append( "<row><grp>" + group +
                        "</grp><rate>" + rate.toString() +
                        "</rate></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows found: " + rowsAffected );
        return success;
    }

    //-getftrdef of getdef
    //template: pers?com=getdef&ftr=<ftr_pattern>
    //          Order of query params is not important.
    //pattern : * | name[.*], WHERE name is a path expression
    //descript: selects all features matching the feature
    //          pattern AND formats an XML answer with their
    //          names AND def values. If no feature in DB
    //          matches the pattern, the result will not have
    //          any row elements (200 OK will still be returned).
    //          The results are sorted according to ascending
    //          feature name (a->z, 1->10).
    //example : pers?com=getdef&ftr=lang.*
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/up_features.xsl"?>
    //          <result>
    //              <row><ftr>feature</ftr><defval>default value</defval></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersGetFtrDef( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetFtrDef( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersGetFtrDef( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        if ( qpSize != 3 ) {
            return false;
        }
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "ftr" );
        int ftrIdx = queryParam.qpIndexOfKeyNoCase( "ftr" );
        if ( ftrIdx == -1 ) {
            return false;
        }
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String ftrCondition = DBAccess.ftrPatternCondition( ( String ) queryParam.getVal( ftrIdx ) );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get def values of matching features            
            query = "SELECT uf_feature, uf_defvalue FROM up_features" + " WHERE uf_feature" + ftrCondition + " AND FK_psclient='" + clientName + "' ORDER BY uf_feature";  //ascending
            //WebServer.win.log.debug("=============================================");
            //WebServer.win.log.debug(query);
            //WebServer.win.log.debug("=============================================");
            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/up_features.xsl" ) );
            respBody.append( "<result>\n" );
            while ( rs.next() ) {
                String feature = rs.getRs().getString( "uf_feature" );  //cannot be null
                String defValue = rs.getRs().getString( "uf_defvalue" );
                if ( rs.getRs().wasNull() ) {
                    defValue = "";
                }
                respBody.append( "<row><ftr>" + feature +
                        "</ftr><defval>" + defValue +
                        "</defval></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows found: " + rowsAffected );
        return success;
    }

    //-getusrattr
    //template: pers?com=getusrattr&usr=<usr>&attr=<attr_pattern>[&num=
    //          <num_pattern>&srt=<order_pattern>&cmp=<comp_pattern>]
    //          Order of query params is not important. Query params
    //          'num', 'srt', AND 'cmp' are optional. If ommited, 'num'
    //          defaults to '*', 'srt' defaults to 'desc', AND 'cmp' to 'n'.
    //pattern : for attribute, * | name[.*], WHERE name is a path expression.
    //          For num, * | <integer>.
    //          For srt, asc | desc. For A->Z use 'asc', for 10->1 use 'desc'.
    //          For cmp, s | n. Values are compared as strings if cmp==s,
    //          while they are compared as numbers (doubles) if cmp==n.
    //          String values that cannot be converted to doubles are
    //          represented as NULLs in numeric comparison.
    //descript: for the specified user, the attributes matching the pattern
    //          are found AND sorted according to value (based on 'srt' AND
    //          'cmp'), AND secondarily according to attribute name (asc, A->Z) .
    //          Then the first <num_pattern> rows are selected (or all, if
    //          <num_pattern> is '*') AND an XML answer is formed. If no
    //          atttribute in DB matches the pattern or if <num_pattern> <=0
    //          or if user does not exist, the result will not have any
    //          'row' elements (200 OK will still be returned).
    //          Note that 'srt' AND 'cmp' affect the sorting on value.
    //          Note that in case a number of attribute matching the pattern
    //          have the same value, some of them may be part of the
    //          results, while others not. This depends on 'num', which
    //          determines in absolute terms the number of result rows.
    //          Which of the attributes with the same value will be part of
    //          the result depends on the attribute name, which is a secondary
    //          field of ordering.
    //example : pers?com=getusrattr&usr=john&attr=lang.*&num=3
    //          pers?com=getusrattr&usr=152&attr=page5.*
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/singleuser_profile.xsl"?>
    //          <result>
    //              <row><attr>feature</attr><val>value</val></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored. 
    private int comPersGetUsrAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetUsrAttr( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersGetUsrAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        int attrIdx = queryParam.qpIndexOfKeyNoCase( "attr" );
        int numIdx = queryParam.qpIndexOfKeyNoCase( "num" );
        int srtIdx = queryParam.qpIndexOfKeyNoCase( "srt" );
        int cmpIdx = queryParam.qpIndexOfKeyNoCase( "cmp" );
        if ( usrIdx == -1 || attrIdx == -1 ) {
            return false;
        }  //must exist
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String user = ( String ) queryParam.getVal( usrIdx );
        String feature = ( String ) queryParam.getVal( attrIdx );
        //if optional query params absent, use defaults
        String numberOfRes = ( numIdx == -1 ) ? "*" : ( String ) queryParam.getVal( numIdx );
        String sortOrder = ( srtIdx == -1 ) ? "desc" : ( String ) queryParam.getVal( srtIdx );
        String comparStyle = ( cmpIdx == -1 ) ? "n" : ( String ) queryParam.getVal( cmpIdx );
        //check if upper limit of result number can be obtained
        int limit = DBAccess.numPatternCondition( numberOfRes );
        if ( limit == -1 ) {
            return false;
        }
        String attrCondition = DBAccess.ftrPatternCondition( feature );
        String srtCondition = DBAccess.srtPatternCondition( sortOrder );
        //comparison style decides on which field to perform SQL order by.
        //Since both fields contain the same values as strings AND as doubles,
        //this actually decides whether to treat values as strings or doubles.
        //That is actually the whole point of having same values in two fields.
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching records
            if ( user.contains( "*" ) ) {
                query = "select attribute, attribute_value from user_attributes WHERE user like'" + user.replaceAll( "\\*", "%" ) + "' AND attribute in " + "(select attribute from user_attributes WHERE attribute" + attrCondition + ") AND FK_psclient='" + clientName + "' order by attribute_value " + srtCondition + ", attribute";
            } else {
                query = "select attribute, attribute_value from user_attributes WHERE user='" + user + "' AND attribute in " + "(select attribute from user_attributes WHERE attribute" + attrCondition + ") AND FK_psclient='" + clientName + "' order by  attribute_value " + srtCondition + ", attribute";
            }

//          WebServer.win.log.debug( "=============================================" );
//          WebServer.win.log.debug( query );
//          WebServer.win.log.debug( "=============================================" );

            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/singleuser_attributes.xsl" ) );
            respBody.append( "<result>\n" );
            //select first rows as specified by query parameter 'num'
            while ( rowsAffected < limit && rs.next() ) {
                String featureVal = rs.getRs().getString( "attribute" );  //cannot be null
                String valueVal = rs.getRs().getString( "attribute_value" );
                if ( rs.getRs().wasNull() ) {
                    valueVal = "";
                }
                respBody.append( "<row><attr>" + featureVal +
                        "</attr><val>" + valueVal +
                        "</val></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows returned: " + rowsAffected );
        return success;
    }

    //-getusrftr or getusr
    //template: pers?com=getusrftr&usr=<usr>&ftr=<ftr_pattern>[&num=
    //          <num_pattern>&srt=<order_pattern>&cmp=<comp_pattern>]
    //          Order of query params is not important. Query params
    //          'num', 'srt', AND 'cmp' are optional. If ommited, 'num'
    //          defaults to '*', 'srt' defaults to 'desc', AND 'cmp' to 'n'.
    //pattern : for feature, * | name[.*], WHERE name is a path expression.
    //          For num, * | <integer>.
    //          For srt, asc | desc. For A->Z use 'asc', for 10->1 use 'desc'.
    //          For cmp, s | n. Values are compared as strings if cmp==s,
    //          while they are compared as numbers (doubles) if cmp==n.
    //          String values that cannot be converted to doubles are
    //          represented as NULLs in numeric comparison.
    //descript: for the specified user, the features matching the pattern
    //          are found AND sorted according to value (based on 'srt' AND
    //          'cmp'), AND secondarily according to feature name (asc, A->Z) .
    //          Then the first <num_pattern> rows are selected (or all, if
    //          <num_pattern> is '*') AND an XML answer is formed. If no
    //          feature in DB matches the pattern or if <num_pattern> <=0
    //          or if user does not exist, the result will not have any
    //          'row' elements (200 OK will still be returned).
    //          Note that 'srt' AND 'cmp' affect the sorting on value.
    //          Note that in case a number of features matching the pattern
    //          have the same value, some of them may be part of the
    //          results, while others not. This depends on 'num', which
    //          determines in absolute terms the number of result rows.
    //          Which of the features with the same value will be part of
    //          the result depends on the feature name, which is a secondary
    //          field of ordering.
    //example : pers?com=getusrftr&usr=john&ftr=lang.*&num=3
    //          pers?com=getusrftr&usr=152&ftr=page5.*
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/singleuser_profile.xsl"?>
    //          <result>
    //              <row><ftr>feature</ftr><val>value</val></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersGetUsrFtr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetUsrFtr( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersGetUsrFtr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();

        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        int ftrIdx = queryParam.qpIndexOfKeyNoCase( "ftr" );
        //int attrIdx = queryParam.qpIndexOfKeyNoCase("attr");
        int numIdx = queryParam.qpIndexOfKeyNoCase( "num" );
        int srtIdx = queryParam.qpIndexOfKeyNoCase( "srt" );
        int cmpIdx = queryParam.qpIndexOfKeyNoCase( "cmp" );
        //if (usrIdx == -1 || (ftrIdx == -1 && attrIdx==-1)) return false;  //must exist
        if ( usrIdx == -1 || ftrIdx == -1 ) {
            return false;
        }  //must exist
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String user = ( String ) queryParam.getVal( usrIdx );
        String feature = ( String ) queryParam.getVal( ftrIdx );
        //if optional query params absent, use defaults
        String numberOfRes = ( numIdx == -1 ) ? "*" : ( String ) queryParam.getVal( numIdx );
        String sortOrder = ( srtIdx == -1 ) ? "desc" : ( String ) queryParam.getVal( srtIdx );
        String comparStyle = ( cmpIdx == -1 ) ? "n" : ( String ) queryParam.getVal( cmpIdx );
        //check if upper limit of result number can be obtained
        int limit = DBAccess.numPatternCondition( numberOfRes );
        if ( limit == -1 ) {
            return false;
        }
        String ftrCondition = DBAccess.ftrPatternCondition( feature );
        String srtCondition = DBAccess.srtPatternCondition( sortOrder );
        //comparison style decides on which field to perform SQL order by.
        //Since both fields contain the same values as strings AND as doubles,
        //this actually decides whether to treat values as strings or doubles.
        //That is actually the whole point of having same values in two fields.
        String comparField = comparStyle.equals( "s" ) ? "up_value" : "up_numvalue";
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching records

            String query1;
            String query2;
            if ( user.contains( "*" ) ) {
                query1 = "SELECT uf_feature AS up_feature, uf_numdefvalue AS up_numvalue FROM up_features WHERE uf_feature NOT IN " + " ( SELECT up_feature FROM user_profiles WHERE up_user LIKE '" + user.replaceAll( "\\*", "%" ) + "' AND FK_psclient = '" + clientName + "') AND FK_psclient = '" + clientName + "'";
                query2 = "SELECT up_feature, up_numvalue AS up_numvalue FROM user_profiles WHERE up_user LIKE '" + user.replaceAll( "\\*", "%" ) + "' AND up_feature in " + "(SELECT up_feature FROM user_profiles WHERE up_feature " + ftrCondition + " AND FK_psclient='" + clientName + "' ) AND FK_psclient='" + clientName + "'";
            } else {
                query1 = "SELECT uf_feature AS up_feature, uf_numdefvalue AS up_numvalue FROM up_features WHERE uf_feature NOT IN " + " ( SELECT up_feature FROM user_profiles WHERE up_user = '" + user + "' AND FK_psclient = '" + clientName + "') AND FK_psclient = '" + clientName + "'";
                query2 = "SELECT up_feature, up_numvalue AS up_numvalue FROM user_profiles WHERE up_user = '" + user + "' AND up_feature in " + "(SELECT up_feature FROM user_profiles WHERE up_feature " + ftrCondition + " AND FK_psclient='" + clientName + "' ) AND FK_psclient='" + clientName + "'";
            }
            query = " ( " + query1 + " ) UNION ( " + query2 + " ) order by " + comparField + srtCondition + ", up_feature;";

            //WebServer.win.log.debug( "=============================================" );
            //WebServer.win.log.debug( query );
            //WebServer.win.log.debug( "=============================================" );

            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/singleuser_profile.xsl" ) );
            respBody.append( "<result>\n" );
            //select first rows as specified by query parameter 'num'
            while ( rowsAffected < limit && rs.next() ) {
                String featureVal = rs.getRs().getString( "up_feature" );  //cannot be null
                String valueVal = rs.getRs().getString( "up_numvalue" );
                if ( rs.getRs().wasNull() ) {
                    valueVal = "";
                }
                respBody.append( "<row><ftr>" + featureVal +
                        "</ftr><val>" + valueVal +
                        "</val></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows returned: " + rowsAffected );
        return success;
    }

    //-getusrs
    //template: pers?com=getusrs&whr=<cond>
    //          <num_pattern>&srt=<order_pattern>&cmp=<comp_pattern>
    //          Order of query params is not important.
    //pattern : * The '*' means all character sequences.                 
    //descript: Returns all the users that has a name that satisfies the whr pattern
    //example : pers?com=getusrs
    //          pers?com=getusrs&whr=sample*
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/user_profile.xsl"?>
    //          <result>
    //              <row><usr>feature</usr></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser.  In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //           reference to xsl can be ignored.
    private int comPersGetUsrs( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersGetUsrs( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersGetUsrs( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int whrIdx = queryParam.qpIndexOfKeyNoCase( "whr" );
        if ( whrIdx == -1 ) {
            return false;
        }  //must exist
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String WHERE = ( String ) queryParam.getVal( whrIdx );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching records            
            query = "select user from users WHERE user like'" + WHERE.replaceAll( "\\*", "%" ) + "' AND FK_psclient='" + clientName + "' ";
            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/user.xsl" ) );
            respBody.append( "<result>\n" );
            while ( rs.next() ) {
                String user = rs.getRs().getString( "user" );  //cannot be null
                respBody.append( "<row><usr>" + user +
                        "</usr></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows returned: " + rowsAffected );
        return success;
    }

    //-incval
    //template: pers?com=incval&usr=<usr>&<ftr_1>=<step_1>&...
    //          Order of query params is not important: updates of feature
    //          values are performed in the order they appear in the request,
    //          however the changes are of accummulative nature, so the final
    //          result is the same.
    //descript: for the specified user, the value for each specified feature
    //          is increased by x (decreased if x is negative), WHERE x is
    //          the step corresponding to that feature. Rows with string
    //          values that cannot be converted to numeric, are not affected.
    //          If no matches are found, or if all matches have values that
    //          cannot be converted to numeric, no records will be updated
    //          (200 OK will still be returned). If any <step_i> parameter
    //          cannot be converted to numeric, 401 is returned. If the error
    //          code 401 is returned then no updates have taken place in the DB.
    //example : pers?com=incval&usr=kostas&lang.gr=1&expert=-0.34
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersIncVal( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execPersIncVal( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersIncVal( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        int logIdx = queryParam.qpIndexOfKeyNoCase( "log" );
        if ( usrIdx == -1 ) {
            return false;
        }
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String user = ( String ) queryParam.getVal( usrIdx );
        //execute request
        boolean success = true;
        String sqlString;
        int rowsAffected = 0;
        try {
            //increment numeric values of features in profile of user
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != usrIdx && i != clntIdx && i != logIdx ) {  //'com' AND 'usr' query parameters excluded
                    //get current parameter pair
                    String feature = ( String ) queryParam.getKey( i );
                    String step = ( String ) queryParam.getVal( i );
                    Float numStep = DBAccess.strToNum( step );  //is it numeric?
                    if ( numStep != null ) {  //if null, 'step' not numeric, misspelled request
                        //get value for current user, feature record
                        sqlString = "select up_value from user_profiles WHERE up_user='" + user + "' AND up_feature='" + feature + "' AND FK_psclient='" + clientName + "'";
                        PServerResultSet rs = dbAccess.executeQuery( sqlString );
                        if ( rs.next() == false ) {
                            rs.close();
                            sqlString = "insert into user_profiles (up_user, up_feature, up_value, up_numvalue, FK_psclient )" + " select '" + user + "', uf_feature, uf_defvalue, uf_numdefvalue, FK_psclient FROM up_features WHERE uf_feature = '" + feature + "' AND FK_psclient = '" + clientName + "'";
                            dbAccess.executeUpdate( sqlString );
                            sqlString = "select up_value from user_profiles WHERE up_user='" + user + "' AND up_feature='" + feature + "' AND FK_psclient='" + clientName + "'";
                            rs = dbAccess.executeQuery( sqlString );
                            if ( rs.next() == false ) {
                                WebServer.win.log.debug( "-Problem updating DB: Feature name does not exists" );
                                success = false;
                                break;
                            }
                        }

                        //boolean recFound = rs.next();  //expect one row or none
                        //String value = recFound ? rs.getRs().getString( "up_value" ) : null;
                        String value = rs.getRs().getString( "up_value" );
                        Float numValue = DBAccess.strToNum( value );  //is it numeric?
                        float newNumValue = numValue.floatValue() + numStep.floatValue();
                        String newValue = DBAccess.formatDouble( new Double( newNumValue ) );
                        rs.close();  //in any case

                        //if ( numValue != null ) {  //if null, 'value' does not exist or not numeric
                        //update current user, feature record                        
                        sqlString = "UPDATE user_profiles SET up_value='" + newValue + "', up_numvalue=" + newValue + " WHERE up_user='" + user + "' AND FK_psclient='" + clientName + "' AND up_feature='" + feature + "'";
                        rowsAffected += dbAccess.executeUpdate( sqlString );
                        int sid = dbAccess.getLastSessionId( user, clientName );
                        PNumData data = new PNumData( user, feature, newNumValue, new Date().getTime() , sid );
                        rowsAffected += dbAccess.insertNewNumData( data, clientName );
                        rowsAffected += dbAccess.updateStereotypesFromUserAction( user, feature, numStep.floatValue(), clientName  );
                    //ignore current user, feature record AND continue with next
                    } //else if numStep == null
                    else {
                        success = false;
                    }  //misspelled request, abort AND rollback
                }
                if ( !success ) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        //close statement
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem updating DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows updated: " + rowsAffected );
        return success;
    }
    /*
    -remattr
    template: pers?com=remattr&attr=<attr_pattern_1>&ftr=...
    Order of query params is not important.
    pattern : * | name[.*], WHERE name is a path expression
    descript: removes all records with attributes matching the
    attribute pattern(s). Referential integrity constraints
    will cause records of tables WHERE those attributes are
    foreign keys to be removed as well. If no feature
    in DB matches a pattern, no record will be deleted
    (200 OK will still be returned). If the error code
    401 is returned then none of the attributes matching
    the request pattern(s) has been deleted. Can be used
    to initialize the Personal Mode DB, by deleting all
    data in tables (note that table'num_data' are not affected, however).
    example : pers?com=remattr&attr=lang.*&attr=gender
    pers?com=remattr&attr=*  (deletes everything from DB tables)
    returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    200 OK  : in this case the response body is as follows
    <?xml version="1.0"?>
    <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    <result>
    <row><num_of_rows>number of relevant rows</num_of_rows></row>
    </result>
    comments: the reference to the xsl file allows to view results
    in a web browser. In case the response body is hANDled
    directly by an application AND not by a browser, this
    reference to xsl can be ignored.
     */

    private int comPersRemAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execPersRemAttr( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersRemAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //execute request
        String clientName = ( String ) queryParam.getVal( clntIdx );
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows of matching features
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                    String key = ( String ) queryParam.getKey( i );
                    if ( key.equalsIgnoreCase( "attr" ) ) {
                        String ftrCondition = DBAccess.ftrPatternCondition( ( String ) queryParam.getVal( i ) );
                        query = "delete from user_attributes WHERE attribute" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate( query );
                        query = "delete from attributes WHERE attr_name" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate( query );
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if ( !success ) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        //close statement
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem deleting from DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows deleted: " + rowsAffected );
        return success;
    }

    //-remdcy
    //template: pers?com=remdcy[&grp=<ftr_group_1>&grp=...]
    //          Order of query params is not important. The 'grp'
    //          query parameters are optional.
    //descript: removes all decay group records specified by 'grp'
    //          query parameters. If no matching records are found
    //          no records will be deleted (200 OK will still be
    //          returned). If no 'grp' query parameters exist, the
    //          records for all feature groups will be deleted. If
    //          the error code 401 is returned then no record has
    //          been deleted. Note that for initializing the Personal
    //          Mode DB (removing all records in all tables) this
    //          request (the 'delete everything' version) must be
    //          used together with 'remftr', as the 'remftr' does not
    //          affect the table 'decay_groups'.
    //example : pers?com=remdcy&grp=page10&grp=advertisements.clothing
    //          pers?com=remdcy   (deletes everything in table)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    //          Note that an important reason why this request does not
    //          delete decay data as well (the timestamped user-feature
    //          interactions with features that belong to the specified
    //          groups) is that a feature name may correspond to more
    //          than one groups (eg. page10.banners.banner2 corresponds
    //          to both page10 AND page10.banners groups).
    private int comPersRemDcy( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execPersRemDcy( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersRemDcy( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //execute request
        String clientName = ( String ) queryParam.getVal( clntIdx );
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows of specified feature groups
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                    String key = ( String ) queryParam.getKey( i );
                    String group = ( String ) queryParam.getVal( i );
                    if ( key.equalsIgnoreCase( "grp" ) ) {
                        query = "delete from decay_groups WHERE dg_group='" + group + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate( query );
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if ( !success ) {
                    break;
                }  //discontinue loop, rollback
            }
            if ( qpSize == 1 ) {  //no 'grp' query parameters specified
                //delete rows of all groups
                query = "delete from decay_groups";
                rowsAffected = dbAccess.executeUpdate( query );
            }
            //format response body
            //response will be used only in case of success            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        //close statement
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem deleting from DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows deleted: " + rowsAffected );
        return success;
    }

    //-remddt
    //template: pers?com=remddt&whr=<WHERE_pattern>
    //          Order of query params is not important.
    //pattern : * | <SQL part following WHERE>. The '*' means all.
    //          A special syntax must be used: ':' for = AND '|' for <space>.
    //          This is because spaces AND '=' are replaced in WWW requests.
    //          Note that string values must be enclosed in single quotes.
    //descript: removes all decay data records specified by 'whr' query
    //          parameter. If no matching records are found no records
    //          will be deleted (200 OK will still be returned). If the
    //          error code 401 is returned then no record has been deleted.
    //          Note that the 'remftr' commAND has also the effect of
    //          removing records of the corresponding features from this
    //          table, due to referential integrity constraints.
    //example : pers?com=remddt&whr=dd_user:'us101'
    //          pers?com=remddt&whr=dd_feature|like|'page9.%'
    //          pers?com=remddt&whr=dd_timestamp>1005854664588
    //          pers?com=remddt&whr=*   (deletes everything in table)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    //          Note that if a user is to be removed from the personal mode,
    //          the requests 'remddt' AND 'remndt' should be called to remove
    //          decay data AND numeric data respectively, together with the
    //          request 'remusr' to remove user features.
    private int comPersRemDdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execPersRemDdt( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersRemDdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int whrIdx = queryParam.qpIndexOfKeyNoCase( "whr" );
        if ( whrIdx == -1 ) {
            return false;
        }
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String whrCondition = DBAccess.whrPatternCondition( ( String ) queryParam.getVal( whrIdx ), clientName );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows specified in 'whr' query parameter
            if ( whrCondition.equals( "" ) ) //'whr=*', no conditions, all records in table
            {
                query = "delete from decay_data WHERE FK_psclient='" + clientName + "' ";
            } else {
                query = "delete from decay_data" + whrCondition + " AND FK_psclient='" + clientName + "' ";
            }
            rowsAffected += dbAccess.executeUpdate( query );
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem deleting from DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows deleted: " + rowsAffected );
        return success;
    }

    //-remftr
    //template: pers?com=remftr&ftr=<ftr_pattern_1>&ftr=...
    //          Order of query params is not important.
    //pattern : * | name[.*], WHERE name is a path expression
    //descript: removes all records with features matching the
    //          feature pattern(s). Referential integrity constraints
    //          will cause records of tables WHERE those features are
    //          foreign keys to be removed as well. If no feature
    //          in DB matches a pattern, no record will be deleted
    //          (200 OK will still be returned). If the error code
    //          401 is returned then none of the features matching
    //          the request pattern(s) has been deleted. Can be used
    //          to initialize the Personal Mode DB, by deleting all
    //          data in tables (note that tables 'decay_groups' AND
    //          'num_data' are not affected, however).
    //example : pers?com=remftr&ftr=lang.*&ftr=gender
    //          pers?com=remftr&ftr=*  (deletes everything from DB tables)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersRemFtr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execPersRemFtr( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersRemFtr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //execute request
        String clientName = ( String ) queryParam.getVal( clntIdx );
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows of matching features
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                    String key = ( String ) queryParam.getKey( i );
                    if ( key.equalsIgnoreCase( "ftr" ) ) {
                        String ftrCondition = DBAccess.ftrPatternCondition( ( String ) queryParam.getVal( i ) );
                        query = "delete from user_profiles WHERE up_feature" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        //System.out.println("===============");
                        //System.out.println(query);
                        //System.out.println("===============");
                        rowsAffected += dbAccess.executeUpdate( query );
                        query = "delete from up_features WHERE uf_feature" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                        //System.out.println("===============");
                        //System.out.println(query);
                        //System.out.println("===============");
                        rowsAffected += dbAccess.executeUpdate( query );
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if ( !success ) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem deleting from DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows deleted: " + rowsAffected );
        return success;
    }

    //-remndt
    //template: pers?com=remndt&whr=<WHERE_pattern>
    //          Order of query params is not important.
    //pattern : * | <SQL part following WHERE>. The '*' means all.
    //          A special syntax must be used: ':' for = AND '|' for <space>.
    //          This is because spaces AND '=' are replaced in WWW requests.
    //          Note that string values must be enclosed in single quotes.
    //descript: removes all numeric data records specified by 'whr' query
    //          parameter from table 'num_data'. If no matching records are
    //          found no records will be deleted (200 OK will still be
    //          returned). If the error code 401 is returned then no record
    //          has been deleted.
    //example : pers?com=remndt&whr=nd_user:'kostas'
    //          pers?com=remndt&whr=nd_feature|like|'laptop.%'
    //          pers?com=remndt&whr=nd_timestamp>1005854664588
    //          pers?com=remndt&whr=*   (deletes everything in table)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    //          Note that if a user is to be removed from the personal mode,
    //          the requests 'remddt' AND 'remndt' should be called to remove
    //          decay data AND numeric data respectively, together with the
    //          request 'remusr' to remove user features.
    private int comPersRemNdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execPersRemNdt( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
            }
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersRemNdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int whrIdx = queryParam.qpIndexOfKeyNoCase( "whr" );
        if ( whrIdx == -1 ) {
            return false;
        }
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String whrCondition = DBAccess.whrPatternCondition( ( String ) queryParam.getVal( whrIdx ), clientName );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows specified in 'whr' query parameter
            /*if ( whrCondition.equals( "" ) ) //'whr=*', no conditions, all records in table
            {
            query = "delete from num_data WHERE FK_psclient='" + clientName + "' ";
            } else {
            query = "delete from num_data " + whrCondition + " AND FK_psclient='" + clientName + "' ";
            }*/
            query = "delete from num_data " + whrCondition;

            rowsAffected += dbAccess.executeUpdate( query );
            //format response body
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem deleting from DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows deleted: " + rowsAffected );
        return success;
    }

    //-remusr
    //template: pers?com=remusr[&usr=<usr_1>&usr=...]
    //          Order of query params is not important. The 'usr'
    //          query parameters are optional.
    //descript: removes all records specified by 'usr' query
    //          parameters. If no matching records are found
    //          no records will be deleted (200 OK will still
    //          be returned). If no 'usr' query parameters exist,
    //          the records for all users will be deleted. If the
    //          error code 401 is returned then no record has
    //          been deleted.
    //example : pers?com=remusr&usr=john&usr=kostas
    //          pers?com=remusr   (deletes everything in table)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //          Note that the 'number of relevant rows' is not the
    //          number of removed users. The removed users are given
    //          by : (deleted rows / number of features).
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    //          Note that if a user is to be removed from the personal mode,
    //          the requests 'remddt' AND 'remndt' should be called to remove
    //          decay data AND numeric data respectively, together with the
    //          request 'remusr' to remove user features.
    private int comPersRemUsr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execPersRemUsr( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersRemUsr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //execute request
        String clientName = ( String ) queryParam.getVal( clntIdx );
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //delete rows of specified users
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                    String key = ( String ) queryParam.getKey( i );
                    String user = ( String ) queryParam.getVal( i );
                    if ( key.equalsIgnoreCase( "usr" ) ) {
                        dbAccess.removeUserFromStereotypes( user, clientName );
                        query = "delete from num_data WHERE nd_user like '" + user.replaceAll( "\\*", "%" ) + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate( query );
                        query = "delete from decay_data WHERE dd_user like '" + user.replaceAll( "\\*", "%" ) + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate( query );
                        query = "delete from user_sessions WHERE FK_user like '" + user.replaceAll( "\\*", "%" ) + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate( query );
                        query = "delete from stereotype_users WHERE su_user like '" + user.replaceAll( "\\*", "%" ) + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate( query );
                        query = "delete from user_attributes WHERE user like '" + user.replaceAll( "\\*", "%" ) + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate( query );
                        query = "delete from user_profiles WHERE up_user like '" + user.replaceAll( "\\*", "%" ) + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate( query );
                        query = "delete from users WHERE user like '" + user.replaceAll( "\\*", "%" ) + "' AND FK_psclient='" + clientName + "' ";
                        rowsAffected += dbAccess.executeUpdate( query );
                    } else {
                        success = false;
                    }  //request is not valid, rollback
                }
                if ( !success ) {
                    break;
                }  //discontinue loop, rollback
            }
            if ( qpSize == 1 ) {  //no 'usr' query parameters specified
                //delete rows of all users
                query = "delete from user_profiles";
                rowsAffected = dbAccess.executeUpdate( query );
                query = "delete from users";
                rowsAffected = dbAccess.executeUpdate( query );
            }
            //format response body
            //response will be used only in case of success                       
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem deleting from DB: " + e );
            e.printStackTrace();
        }
        WebServer.win.log.debug( "-Num of rows deleted: " + rowsAffected );
        return success;
    }

    //-setattrdef
    //template: pers?com=setattrdef&<attr_pattern_1>=<new_def_val_1>&...
    //          Order of query params is important: position of 'com'
    //          is not important, however updates of attribute values
    //          are performed in the order they appear in the request.
    //pattern : * | name[.*], WHERE name is a path expression
    //descript: updates the def value of all features matching
    //          the feature pattern(s) to the new def value(s).
    //          The new def values will affect only subsequent
    //          user profiles. Old profiles keep the old def values.
    //          If no feature in DB matches a pattern, no def value
    //          will be updated (200 OK will still be returned).
    //          If the error code 401 is returned then none
    //          of the features matching the request pattern(s)
    //          has been updated to the new def value(s).
    //example : pers?com=setdef&age=24&lang.*=0&lang.gr=1
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersSetAttrDef( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execPersSetAttrDef( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersSetAttrDef( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //execute request
        String clientName = ( String ) queryParam.getVal( clntIdx );
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //update def values of matching features
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx ) {  //'com' query parameter excluded
                    String newDefValue = ( String ) queryParam.getVal( i );
                    String ftrCondition = DBAccess.ftrPatternCondition( ( String ) queryParam.getKey( i ) );
                    String numNewDefValue = DBAccess.strToNumStr( newDefValue );  //numeric version of def value
                    query = "UPDATE attributes set attr_defvalue='" + newDefValue + "'" + " WHERE attr_name" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                    rowsAffected += dbAccess.executeUpdate( query );
                }
            }
            //format response body
            //response will be used only in case of success
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem updating DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows updated: " + rowsAffected );
        return success;
    }

    //-setattr
    //template: pers?com=setattr&usr=<usr>&<attr_1>=<attr_1>&...
    //          Order of query params is not important: updates of attribute
    //          values are performed in the order they appear in the request,
    //          however the changes are of accummulative nature, so the final
    //          result is the same.
    //descript: for the specified user, the value for each specified attribute
    //          is updated to x , WHERE x is the value corresponding
    //          to that attribute.
    //          If no matches are found no records will be updated
    //          (200 OK will still be returned). If the error
    //          code 401 is returned then no updates have taken place in the DB.
    //example : pers?com=setattr&usr=kostas&lang=GR
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersSetAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execPersSetAttr( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersSetAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        if ( usrIdx == -1 ) {
            return false;
        }
        String clientName = ( String ) queryParam.getVal( clntIdx );
        String user = ( String ) queryParam.getVal( usrIdx );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //increment numeric values of features in profile of user
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != usrIdx && i != clntIdx ) {  //'com' AND 'usr' query parameters excluded
                    //get current parameter pair
                    String attribute = ( String ) queryParam.getKey( i );
                    String newVal = ( String ) queryParam.getVal( i );
                    //get value for current user, feature record
                    //update current user, attribute record
                    query = "UPDATE user_attributes set attribute_value ='" + newVal + "' WHERE user = '" + user + "' AND FK_psclient='" + clientName + "' AND attribute ='" + attribute + "'";
                    //System.out.println("============================="+query);
                    rowsAffected += dbAccess.executeUpdate( query );
                }
                if ( !success ) {
                    break;
                }  //discontinue loop, rollback
            }
            //format response body
            //response will be used only in case of success            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem updating DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows updated: " + rowsAffected );
        return success;
    }

    //-setdcy
    //template: pers?com=setdcy&<ftr_group_1>=<decay_rate_1>&...
    //          Order of query params is important: position of 'com'
    //          is not important, however updates of feature group decay
    //          rates are performed in the order they appear in the request.
    //          Decay rate must exist AND be a number from 0 to 1, both
    //          inclusive (eg: 0.5). Feature group is a template path
    //          expression 'pathname', representing all features with name
    //          starting with 'pathname'. Feature group cannot be "*".
    //descript: if the feature group already exists in the DB, the
    //          corresponding decay rate is updated to the new value. If the
    //          feature group is a new group, the new (group, rate) pair is
    //          inserted in the DB. If the error code 401 is returned, (eg. in
    //          case some decay rate was not numeric or was not in the proper
    //          range, or in case the feature group was illegal, eg. "*") then
    //          no changes at all have taken place in the DB. Note however
    //          that in case a feature group is the empty string, the
    //          corresponding (group, rate) pair will just be ignored.
    //example : pers?com=setdcy&page10.banners=0.5&the_times.politics=1
    //          pers?com=setdcy&frequency.allfeatures=0  (see comments)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : no response body exists.
    //comments: - Note that if rate is set to 0, the decay calculated for
    //          a feature is reduced to the total number of visits the
    //          user paid to the feature.
    //          - Note that the same feature may be organized as part of
    //          two different hierarchies, eg. page10.banners.banner2,
    //          frequency.allfeatures.banner2, with possibly different
    //          rates. In this case, when the feature is selected by a
    //          user, the application must notify the server about the
    //          interaction for both (or more) names of the feature.
    //          - Note that it is possible to define overlapping sets of
    //          features, or sets who are subsets of other sets, giving
    //          possibly different rates for each, as in:
    //            pers?com=setdcy&page10=0.3&page10.banners=0.6
    //          This will not result in more user-feature interactions
    //          recorded in the database (as is the case in the previous
    //          situation). The interactions are recorded strictly as the
    //          application dictates. However, this makes it posible for
    //          the application to query the same interaction data in
    //          different ways. By defining various overlapping AND
    //          competing feature groups AND assigning them different
    //          decay rates, the application may get different decay
    //          values for a (feature, user) pair.
    //          - Note that it is not necessary to register a feature
    //          group in 'decay_groups' in order to use the decay
    //          functionality. The decay service is based only on
    //          'decay_data' table, AND populating this table has
    //          nothing to do with defining feature groups. It is
    //          possible to define s group AND a corresponding decay
    //          rate on the fly, in the request that calculates the
    //          decay values. The table 'decay_groups' is just a
    //          convenient place to store groups AND rates in order
    //          not to repeat them in every 'caldcy' request.
    private int comPersSetDcy( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = success && execPersSetDcy( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersSetDcy( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        //for all (group, rate) pairs, execute request
        for ( int i = 0; i < qpSize; i++ ) {
            if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                //get current (group, rate) pair
                String group = ( String ) queryParam.getKey( i );
                String rate = ( String ) queryParam.getVal( i );
                //check pair validity
                if ( !DBAccess.legalFtrOrAttrName( group ) ) //ftr group AND ftr name validity is similar
                {
                    return false;
                }     //discontinue request AND rollback
                Float numRateDbl = DBAccess.strToNum( rate );  //converts string to Double
                if ( numRateDbl == null ) //if null, 'rate' not numeric, misspelled request
                {
                    return false;
                }     //discontinue request AND rollback
                double numRate_dbl = numRateDbl.doubleValue();
                if ( numRate_dbl < 0 || numRate_dbl > 1 ) {
                    return false;
                }     //discontinue request AND rollback
                Float numRate = new Float( numRate_dbl );  //'rate' defined float in DB
                //if new decay feature group, insert (group, rate) in DB,
                //else update rate of existing group to new rate
                boolean success;
                if ( !persExistsDecay( dbAccess, group, clientName ) ) {
                    success = persInsertDecay( dbAccess, group, numRate, clientName );
                } else {
                    success = persSetRate( dbAccess, group, numRate, clientName );
                }
                //if not success discontinue loop AND rollback
                if ( !success ) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean persExistsDecay( DBAccess dbAccess, String group, String clientName ) {
        //returns true if decay feature group already
        //exists in the DB. Returns false otherwise.
        boolean exists = false;  //true if group exists in DB
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get specified decay feature group record
            query = "select dg_group from decay_groups WHERE dg_group='" + group + "' AND FK_psclient='" + clientName + "'";
            PServerResultSet rs = dbAccess.executeQuery( query );
            while ( rs.next() ) {
                rowsAffected += 1;
            }  //number of rows in result should be 0 or 1
            exists = ( rowsAffected > 0 ) ? true : false;
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Decay feature group exists: " + rowsAffected );
        return success && exists;  //'success' expected true here
    }

    private boolean persInsertDecay( DBAccess dbAccess, String group, Float rate, String clientName ) {
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert the (feature group, decay rate) pair
            query = "insert into decay_groups (dg_group, dg_rate, FK_psclient) values ('" + group + "', " + rate.toString() + ",'" + clientName + "')";
            rowsAffected = dbAccess.executeUpdate( query );
        //close statement
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem inserting to DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows inserted: " + rowsAffected );
        return success;
    }

    private boolean persSetRate( DBAccess dbAccess, String group, Float rate, String clientName ) {
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //update decay rate of specified feature group
            query = "UPDATE decay_groups set dg_rate=" + rate.toString() + " WHERE dg_group='" + group + "' AND FK_psclient='" + clientName + "'";
            rowsAffected += dbAccess.executeUpdate( query );
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem updating DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows updated: " + rowsAffected );
        return success;
    }

    //-setdef
    //-setftrdef
    //template: pers?com=setdef&<ftr_pattern_1>=<new_def_val_1>&...
    //          Order of query params is important: position of 'com'
    //          is not important, however updates of feature values
    //          are performed in the order they appear in the request.
    //pattern : * | name[.*], WHERE name is a path expression
    //descript: updates the def value of all features matching
    //          the feature pattern(s) to the new def value(s).
    //          The new def values will affect only subsequent
    //          user profiles. Old profiles keep the old def values.
    //          If no feature in DB matches a pattern, no def value
    //          will be updated (200 OK will still be returned).
    //          If the error code 401 is returned then none
    //          of the features matching the request pattern(s)
    //          has been updated to the new def value(s).
    //example : pers?com=setdef&age=24&lang.*=0&lang.gr=1
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/rows.xsl"?>
    //          <result>
    //          <row><num_of_rows>number of relevant rows</num_of_rows></row>
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersSetFtrDef( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = success && execPersSetFtrDef( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean execPersSetFtrDef( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        //execute request
        String clientName = ( String ) queryParam.getVal( clntIdx );
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //update def values of matching features
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != clntIdx ) {  //'com' query parameter excluded
                    String newDefValue = ( String ) queryParam.getVal( i );
                    String ftrCondition = DBAccess.ftrPatternCondition( ( String ) queryParam.getKey( i ) );
                    String numNewDefValue = DBAccess.strToNumStr( newDefValue );  //numeric version of def value
                    query = "UPDATE up_features set uf_defvalue='" + newDefValue + "', uf_numdefvalue=" + numNewDefValue + " WHERE uf_feature" + ftrCondition + " AND FK_psclient='" + clientName + "'";
                    rowsAffected += dbAccess.executeUpdate( query );
                }
            }
            //format response body
            //response will be used only in case of success
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem updating DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows updated: " + rowsAffected );
        return success;
    }

    //-setusr
    //template: pers?com=setusr&usr=<usr>[&<ftr_pattern_1>=<ftr_value_1>&...]
    //          Order of query params is important: position of 'com'
    //          is not important, however updates of feature values
    //          are performed in the order they appear in the request.
    //          User name cannot be empty string.
    //pattern : * | name[.*], WHERE name is a path expression
    //descript: if the user already exists in the DB, the value(s) of the
    //          feature(s) matching the pattern(s) for this user are updated
    //          to the new value(s). If the user is a new user, the new user
    //          feature values are initialized into the DB (using the def
    //          values in 'up_features' table), AND then, the value(s) of
    //          matching feature(s) will be updated to the new value(s). If
    //          no feature matches a pattern no value will be updated (200 OK
    //          will still be returned). Note that if no (feature pattern, value)
    //          pairs exist in the request, the user profile will still be
    //          initialized if it is a new user (otherwise nothing will happen,
    //          200 OK will still be returned). If the error code 401 is returned,
    //          then no changes have taken place in the DB.
    //example : pers?com=setusr&usr=kostas&lang.*=0&lang.gr=1&gender=male
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : no response body exists.
    private int comPersSetUsr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        //execute the commAND
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            //if new user, initialize user profile for all features,
            //AND then update the values of matching features
            //-start transaction body
            if ( !persExistsUsr( queryParam, dbAccess ) ) {
                success = success && execPersAddUsr( queryParam, dbAccess );
            }
            success = success && execPersSetUsr( queryParam, respBody, dbAccess );
            //-end transaction body
            if ( success ) {
                dbAccess.commit();
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }
        return respCode;
    }

    private boolean persExistsUsr( VectorMap queryParam, DBAccess dbAccess ) {
        //returns true if user in 'usr' query parameter
        //already exists in the DB. Returns false otherwise.
        //request properties
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        if ( usrIdx == -1 ) {
            return false;
        }
        String user = ( String ) queryParam.getVal( usrIdx );
        //execute request
        boolean exists = false;  //true if user exists in DB
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get specified user records
            query = "select user from users WHERE user='" + user + "' AND FK_psclient='" + clientName + "'";
            PServerResultSet rs = dbAccess.executeQuery( query );
            while ( rs.next() ) {
                rowsAffected += 1;
            }  //count number of rows in result
            exists = ( rowsAffected > 0 ) ? true : false;
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of user rows: " + rowsAffected );
        return success && exists;  //'success' expected true here
    }

    private boolean execPersAddUsr( VectorMap queryParam, DBAccess dbAccess ) {
        //request properties
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        if ( usrIdx == -1 ) {
            return false;
        }
        String user = ( String ) queryParam.getVal( usrIdx );
        //values in 'queryParam' can be empty string,
        //user should not be empty string, check it
        if ( !DBAccess.legalUsrName( user ) ) {
            return false;
        }
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //insert all features in profile of new user, with def value
            query = "INSERT INTO users (user,FK_psclient) VALUES('" + user + "','" + clientName + "')";
            rowsAffected += dbAccess.executeUpdate( query );
            //WebServer.win.log.debug("=============================================");
            //WebServer.win.log.debug(query);
            //WebServer.win.log.debug("=============================================");
            //query = "insert into user_profiles (up_user, up_feature, up_value, up_numvalue, FK_psclient )" + " select '" + user + "', uf_feature, uf_defvalue, uf_numdefvalue, FK_psclient FROM up_features WHERE FK_psclient = '" + clientName + "'";
            //WebServer.win.log.debug("=============================================");
            //WebServer.win.log.debug(query);
            //WebServer.win.log.debug("=============================================");
            //rowsAffected = dbAccess.executeUpdate( query );
            query = "insert into user_attributes (user, attribute, attribute_value, FK_psclient )" + " select '" + user + "', attr_name, attr_defvalue, FK_psclient FROM attributes WHERE FK_psclient = '" + clientName + "'";
            //WebServer.win.log.debug("=============================================");
            //WebServer.win.log.debug(query);
            //WebServer.win.log.debug("=============================================");
            rowsAffected += dbAccess.executeUpdate( query );
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem inserting to DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows inserted: " + rowsAffected );
        return success;
    }

    private boolean execPersSetUsr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int comIdx = queryParam.qpIndexOfKeyNoCase( "com" );
        int usrIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        if ( usrIdx == -1 ) {
            return false;
        }
        String user = ( String ) queryParam.getVal( usrIdx );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //update values of matching features in profile of user
            for ( int i = 0; i < qpSize; i++ ) {
                if ( i != comIdx && i != usrIdx && i != clntIdx ) {  //'com' AND 'usr' query parameters excluded
                    String parameter = ( String ) queryParam.getKey( i );
                    if ( parameter.startsWith( "attr_" ) ) {
                        String attribute = parameter.substring( 5 );
                        String ftrCondition = DBAccess.ftrPatternCondition( attribute );
                        String value = ( String ) queryParam.getVal( i );
                        query = "UPDATE user_attributes set attribute_value='" + value + "' WHERE user='" + user + "' AND attribute " + ftrCondition + " AND FK_psclient = '" + clientName + "'";
                        //System.out.println("============================="+query);
                        rowsAffected += dbAccess.executeUpdate( query );
                    } else {
                        String feature;
                        if ( parameter.startsWith( "ftr_" ) ) {
                            feature = parameter.substring( 4 );
                        } else {
                            feature = parameter;
                        }
                        String ftrCondition = DBAccess.ftrPatternCondition( feature );
                        String value = ( String ) queryParam.getVal( i );
                        String numValue = DBAccess.strToNumStr( value );  //numeric version of value
                        query = "insert into user_profiles (up_user, up_feature, up_value, up_numvalue, FK_psclient )" + " select '" + user + "', uf_feature, uf_defvalue, uf_numdefvalue, FK_psclient FROM up_features WHERE uf_feature = '" + feature + "' AND FK_psclient = '" + clientName + "'";
                        rowsAffected += dbAccess.executeUpdate( query );
                        query = "UPDATE user_profiles set up_value='" + value + "', up_numvalue=" + numValue + " WHERE up_user='" + user + "' AND up_feature" + ftrCondition + " AND FK_psclient = '" + clientName + "'";
                        //query = "INSERT INTO user_profiles ( up_user, up_feature, up_value, up_numvalue, FK_psclient ) VALUES ( '" + user + "', '" + feature + "', '" + value + "', " + numValue + ", '" + clientName + "' )";
                        //System.out.println("============================="+query);
                        rowsAffected += dbAccess.executeUpdate( query );
                    }

                }
            }
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem updating DB: " + e );
        }
        WebServer.win.log.debug( "-Num of rows updated: " + rowsAffected );
        return success;
    }

    //-sqlddt
    //template: pers?com=sqlddt&whr=<WHERE_pattern>
    //          Order of query params is not important.
    //pattern : * | <SQL part following WHERE>. The '*' means all.
    //          A special syntax must be used: ':' for = AND '|' for <space>.
    //          This is because spaces AND '=' are replaced in WWW requests.
    //          Note that string values must be enclosed in single quotes.
    //descript: returns part of the table 'decay_data' as specified
    //          by the condition in the 'whr' query parameter. If no
    //          row in DB satisfies the conditions, the result will
    //          not have any 'row' elements (200 OK will still be returned).
    //example : pers?com=sqlddt&whr=dd_user:'us101'|AND|dd_feature:'advert.banner6'|order|by|dd_timestamp|desc
    //          pers?com=sqlddt&whr=*
    //          pers?com=sqlddt&whr=dd_feature:'page3.link8'|AND|dd_timestamp>1005854664569
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/decay_data.xsl"?>
    //          <result>
    //              <row><usr>user</usr><ftr>feature</ftr><timestamp>timestamp</timestamp></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersSqlDdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersSqlDdt( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersSqlDdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int whrIdx = queryParam.qpIndexOfKeyNoCase( "whr" );
        if ( whrIdx == -1 ) {
            return false;
        }
        String whrCondition = DBAccess.whrPatternCondition( ( String ) queryParam.getVal( whrIdx ), clientName );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching decay data
            //query = "select dd_user, dd_feature, dd_timestamp from decay_data" + whrCondition + " AND FK_psclient='" + clientName + "' ";
            query = "select dd_user, dd_feature, dd_timestamp from decay_data" + whrCondition;
            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/decay_data.xsl" ) );
            respBody.append( "<result>\n" );
            while ( rs.next() ) {
                String userVal = rs.getRs().getString( "dd_user" );          //cannot be null
                String featureVal = rs.getRs().getString( "dd_feature" );    //cannot be null
                long timestampVal = rs.getRs().getLong( "dd_timestamp" );    //cannot be null
                respBody.append( "<row><usr>" + userVal +
                        "</usr><ftr>" + featureVal +
                        "</ftr><timestamp>" + timestampVal +
                        "</timestamp></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows found: " + rowsAffected );
        return success;
    }

    //-sqlndt
    //template: pers?com=sqlndt&whr=<WHERE_pattern>
    //          Order of query params is not important.
    //pattern : * | <SQL part following WHERE>. The '*' means all.
    //          A special syntax must be used: ':' for = AND '|' for <space>.
    //          This is because spaces AND '=' are replaced in WWW requests.
    //          Note that string values must be enclosed in single quotes.
    //descript: returns part of the table 'num_data' as specified
    //          by the condition in the 'whr' query parameter. If no
    //          row in DB satisfies the conditions, the result will
    //          not have any 'row' elements (200 OK will still be returned).
    //example : pers?com=sqlndt&whr=nd_user:'kostas'|AND|nd_feature:'laptop.weight'|order|by|nd_timestamp|desc
    //          pers?com=sqlndt&whr=*
    //          pers?com=sqlndt&whr=nd_feature:'laptop.weight'|AND|nd_numvalue<2.8
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/num_data.xsl"?>
    //          <result>
    //              <row>
    //                   <usr>user</usr><ftr>feature</ftr><timestamp>timestamp</timestamp>
    //                   <numvalue>numvalue</numvalue>
    //              </row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersSqlNdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersSqlNdt( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersSqlNdt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int whrIdx = queryParam.qpIndexOfKeyNoCase( "whr" );
        if ( whrIdx == -1 ) {
            return false;
        }
        String whrCondition = DBAccess.whrPatternCondition( ( String ) queryParam.getVal( whrIdx ), clientName );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching numeric data            
            //query = "select nd_user, nd_feature, nd_timestamp, nd_numvalue from num_data" + whrCondition + " AND FK_psclient='" + clientName + "' ";
            query = "select nd_user, nd_feature, nd_timestamp, nd_value from num_data" + whrCondition;
            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/num_data.xsl" ) );
            respBody.append( "<result>\n" );
            while ( rs.next() ) {
                String userVal = rs.getRs().getString( "nd_user" );          //cannot be null
                String featureVal = rs.getRs().getString( "nd_feature" );    //cannot be null
                long timestampVal = rs.getRs().getLong( "nd_timestamp" );    //cannot be null
                String numvalueStr;
                double numvalueVal = rs.getRs().getDouble( "nd_value" );
                if ( rs.getRs().wasNull() ) {
                    numvalueStr = "";
                } else {
                    numvalueStr = DBAccess.formatDouble( new Double( numvalueVal ) );
                }
                respBody.append( "<row><usr>" + userVal +
                        "</usr><ftr>" + featureVal +
                        "</ftr><timestamp>" + timestampVal +
                        "</timestamp><numvalue>" + numvalueStr +
                        "</numvalue></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows found: " + rowsAffected );
        return success;
    }

    //-sqlusrattr
    //template: pers?com=sqlusrattr&whr=<WHERE_pattern>
    //          Order of query params is not important.
    //pattern : * | <SQL part following WHERE>. The '*' means all.
    //          A special syntax must be used: ':' for = AND '|' for <space>.
    //          This is because spaces AND '=' are replaced in WWW requests.
    //          Note that string values must be enclosed in single quotes.
    //          Note that there exist two choices for comparisons on values:
    //          string comparisons for field 'up_value', AND numeric (double)
    //          comparisons for field 'up_numvalue'. String values that cannot
    //          be converted to doubles are represented as NULLs in 'up_numvalue'.
    //descript: returns part of the table 'user_profiles' as specified
    //          by the condition in the 'whr' query parameter. If no
    //          row in DB satisfies the conditions, the result will
    //          not have any 'row' elements (200 OK will still be returned).
    //example : pers?com=sqlusrattr&whr=user:'john'|order|by|attribute
    //          pers?com=sqlusrattr&whr=*
    //          pers?com=sqlusrattr&whr=isnull(attribute_value)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/user_profiles.xsl"?>
    //          <result>
    //              <row><usr>user</usr><ftr>feature</ftr><val>value</val></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersSqlUsrAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersSqlUsrAttr( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersSqlUsrAttr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int whrIdx = queryParam.qpIndexOfKeyNoCase( "whr" );
        if ( whrIdx == -1 ) {
            return false;
        }
        String whrCondition = DBAccess.whrPatternCondition( ( String ) queryParam.getVal( whrIdx ), clientName );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching user profiles            
            query = "select user, attribute, attribute_value from user_attributes" + whrCondition;
            //System.out.println(""+query);
            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/user_attributes.xsl" ) );
            respBody.append( "<result>\n" );
            while ( rs.next() ) {
                String userVal = rs.getRs().getString( "user" );        //cannot be null
                String featureVal = rs.getRs().getString( "attribute" );  //cannot be null
                String valueVal = rs.getRs().getString( "attribute_value" );
                if ( rs.getRs().wasNull() ) {
                    valueVal = "";
                }
                respBody.append( "<row><usr>" + userVal +
                        "</usr><attr>" + featureVal +
                        "</attr><val>" + valueVal +
                        "</val></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows found: " + rowsAffected );
        return success;
    }

    //-sqlusrftr of sqlusr
    //template: pers?com=sqlusr&whr=<WHERE_pattern>
    //          Order of query params is not important.
    //pattern : * | <SQL part following WHERE>. The '*' means all.
    //          A special syntax must be used: ':' for = AND '|' for <space>.
    //          This is because spaces AND '=' are replaced in WWW requests.
    //          Note that string values must be enclosed in single quotes.
    //          Note that there exist two choices for comparisons on values:
    //          string comparisons for field 'up_value', AND numeric (double)
    //          comparisons for field 'up_numvalue'. String values that cannot
    //          be converted to doubles are represented as NULLs in 'up_numvalue'.
    //descript: returns part of the table 'user_profiles' as specified
    //          by the condition in the 'whr' query parameter. If no
    //          row in DB satisfies the conditions, the result will
    //          not have any 'row' elements (200 OK will still be returned).
    //example : pers?com=sqlusrftr&whr=up_user:'john'|AND|up_numvalue<:2|order|by|up_feature
    //          pers?com=sqlusrftr&whr=*
    //          pers?com=sqlusrftr&whr=isnull(up_numvalue)
    //returns : 200 OK, 401 (fail, request error), 501 (fail, server error)
    //200 OK  : in this case the response body is as follows
    //          <?xml version="1.0"?>
    //          <?xml-stylesheet type="text/xsl" href="/resp_xsl/user_profiles.xsl"?>
    //          <result>
    //              <row><usr>user</usr><ftr>feature</ftr><val>value</val></row>
    //              ...
    //          </result>
    //comments: the reference to the xsl file allows to view results
    //          in a web browser. In case the response body is hANDled
    //          directly by an application AND not by a browser, this
    //          reference to xsl can be ignored.
    private int comPersSqlUsrFtr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.connect();
            //execute the commAND
            boolean success;
            success = execPersSqlUsrFtr( queryParam, respBody, dbAccess );
            //check success
            if ( !success ) {
                respCode = PSReqWorker.REQUEST_ERR;  //incomprehensible client request
                WebServer.win.log.debug( "-Possible error in client request" );
            }
            //disconnect from DB anyway
            dbAccess.disconnect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }
        return respCode;
    }

    private boolean execPersSqlUsrFtr( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        //request properties
        int qpSize = queryParam.size();
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        int whrIdx = queryParam.qpIndexOfKeyNoCase( "whr" );
        if ( whrIdx == -1 ) {
            return false;
        }
        String whrCondition = DBAccess.whrPatternCondition( ( String ) queryParam.getVal( whrIdx ), clientName );
        //execute request
        boolean success = true;
        String query;
        int rowsAffected = 0;
        try {
            //get matching user profiles
            query = "select up_user, up_feature, up_value from user_profiles" + whrCondition;
            //System.out.println(""+query);
            PServerResultSet rs = dbAccess.executeQuery( query );
            //format response body            
            respBody.append( DBAccess.xmlHeader( "/resp_xsl/user_profiles.xsl" ) );
            respBody.append( "<result>\n" );
            while ( rs.next() ) {
                String userVal = rs.getRs().getString( "up_user" );        //cannot be null
                String featureVal = rs.getRs().getString( "up_feature" );  //cannot be null
                String valueVal = rs.getRs().getString( "up_value" );
                if ( rs.getRs().wasNull() ) {
                    valueVal = "";
                }
                respBody.append( "<row><usr>" + userVal +
                        "</usr><ftr>" + featureVal +
                        "</ftr><val>" + valueVal +
                        "</val></row>\n" );
                rowsAffected += 1;  //number of result rows
            }
            respBody.append( "</result>" );
            //close resultset AND statement
            rs.close();
        } catch ( SQLException e ) {
            success = false;
            WebServer.win.log.debug( "-Problem executing query: " + e );
        }
        WebServer.win.log.debug( "-Num of rows found: " + rowsAffected );
        return success;
    }
    /*private String getClientName ( VectorMap queryParam ) {
    int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
    if ( clntIdx == -1 ) {
    return null;
    }
    //client attibutes demactrate with the "|" character
    String userANDPass = ( String ) queryParam.getVal( clntIdx );
    StringTokenizer tokenizer = new StringTokenizer( userANDPass, "|" );
    String client = tokenizer.nextToken();//first comes the client name
    return client;
    }*/
}
