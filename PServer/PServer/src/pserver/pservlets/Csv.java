package pserver.pservlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import pserver.WebServer;
import pserver.data.DBAccess;
import pserver.data.VectorMap;
import pserver.logic.PSReqWorker;

/**
 *
 * @author alexm
 */
public class Csv implements pserver.pservlets.PService {

    public void init( String[] params ) throws Exception {
    }

    public String getMimeType() {
        return pserver.pservlets.PService.xml;
    }

    public int service( VectorMap parameters, StringBuffer response, DBAccess dbAccess ) {
        int respCode;
        VectorMap queryParam;
        StringBuffer respBody;

        respBody = new StringBuffer();
        queryParam = parameters;

        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        clientName = clientName.substring( 0, clientName.indexOf( '|' ) );
        queryParam.updateVal( clientName, clntIdx );

        //commands of ADMIN_MODE here!
        //find 'com' query param (case independent)
        int comIdx = parameters.qpIndexOfKeyNoCase( "com" );
        //if 'com' param not present, request is invalid
        if ( comIdx == -1 ) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error( "-Request command does not exist" );
            return respCode;  //no point in proceeding
        }

        //recognize command encoded in request
        String com = ( String ) queryParam.getVal( comIdx );        
        if ( com.equalsIgnoreCase( "loadftr" ) ) {//create user communities
            respCode = loadFeatures( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "loadusr" ) ) {//create user communities
            respCode = loadUsers( queryParam, respBody, dbAccess );
        } else if ( com.equalsIgnoreCase( "loadlog" ) ) {//create user communities
            respCode = loadLog( queryParam, respBody, dbAccess );
        } else {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error( "-Request command not recognized" );
        }

        response.append( respBody.toString() );
        return respCode;
    }

    //-----
    //this is the handler function that imports features
    private int loadFeatures( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execLoadFeatures( queryParam, respBody, dbAccess );
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

    private boolean execLoadFeatures( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        boolean success = true;
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        //System.out.println( "clnt = " + clientName );

        int filePathIdx = queryParam.qpIndexOfKeyNoCase( "path" );
        if ( filePathIdx == -1 ) {
            WebServer.win.log.error( "Parameter path is missing" );
            return false;
        }
        String filePath = ( String ) queryParam.getVal( filePathIdx );
        //System.out.println( "filePath = " + filePath );

        int csIdx = queryParam.qpIndexOfKeyNoCase( "cs" );
        if ( csIdx == -1 ) {
            WebServer.win.log.error( "Parameter cs is missing" );
            return false;
        }
        String cs = ( String ) queryParam.getVal( csIdx );
        //System.out.println( "cs = " + cs );

        int ftrColIdx = queryParam.qpIndexOfKeyNoCase( "ftrcol" );
        int ftrCol;
        if ( ftrColIdx == -1 ) {
            WebServer.win.log.debug( "Parameter ftrcol is missing, assuming it is 0" );
            ftrCol = 0;
        }
        ftrCol = Integer.parseInt( ( String ) queryParam.getVal( ftrColIdx ) );
        //System.out.println( "ftrCol = " + ftrCol );

        int defValueIdx = queryParam.qpIndexOfKeyNoCase( "defvalue" );
        String defValue;
        if ( ftrColIdx == -1 ) {
            WebServer.win.log.debug( "Parameter defvalue is missing, assuming it is 0.0" );
            defValue = "0.0";
        } else
            defValue = ( String ) queryParam.getVal( defValueIdx );

        int prefixIdx = queryParam.qpIndexOfKeyNoCase( "pref" );
        String prefix;
        if ( prefixIdx == -1 ) {
            WebServer.win.log.debug( "Parameter prefixIdx is missing, assuming it is NULL" );
            prefix = "";
        } else
            prefix = ( String ) queryParam.getVal( prefixIdx );
        //System.out.println( "defValue = " + defValue );

        try {
            File csvFile = new File( filePath );
            if ( csvFile.exists() == false || csvFile.isDirectory() == true ) {
                WebServer.win.log.error( "The specified file does not exists or it is a directory" );
                return false;
            }

            BufferedReader input = new BufferedReader( new FileReader( csvFile ) );
            String line;
            int rows = 0;
            Connection con = dbAccess.getConnection();
            Statement stmt = con.createStatement();
            while ( ( line = input.readLine() ) != null ) {
                String tokens[] = line.split( cs );
                String ftr = prefix + tokens[ftrCol];
                //PFeature feature = new PFeature( ftr, defValue, defValue );
                String sql = "REPLACE INTO " + DBAccess.FEATURE_TABLE + " " + "(uf_feature, uf_defvalue, uf_numdefvalue, " + DBAccess.FIELD_PSCLIENT + " ) VALUES ('" + ftr + "', '" + defValue + "', " + defValue + ",'" + clientName + "')";
                //rows += dbAccess.insertNewFeature( feature , clientName);
                rows += stmt.executeUpdate( sql );
            }
            stmt.close();

            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rows + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( Exception e ) {
            success = false;
            WebServer.win.log.error( "-Problem inserting to DB: " + e );
        }
        return success;
    }

    //this is the handler function that imports users
    private int loadUsers( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execLoadUsers( queryParam, respBody, dbAccess );
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

    private boolean execLoadUsers( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        boolean success = true;
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        //System.out.println( "clnt = " + clientName );

        int filePathIdx = queryParam.qpIndexOfKeyNoCase( "path" );
        if ( filePathIdx == -1 ) {
            WebServer.win.log.error( "Parameter path is missing" );
            return false;
        }
        String filePath = ( String ) queryParam.getVal( filePathIdx );
        //System.out.println( "filePath = " + filePath );

        int csIdx = queryParam.qpIndexOfKeyNoCase( "cs" );
        if ( csIdx == -1 ) {
            WebServer.win.log.error( "Parameter cs is missing" );
            return false;
        }
        String cs = ( String ) queryParam.getVal( csIdx );
        //System.out.println( "cs = " + cs );

        int usrColIdx = queryParam.qpIndexOfKeyNoCase( "usrcol" );
        int usrCol;
        if ( usrColIdx == -1 ) {
            WebServer.win.log.debug( "Parameter usrcol is missing, assuming it is 0" );
            usrCol = 0;
        }
        usrCol = Integer.parseInt( ( String ) queryParam.getVal( usrColIdx ) );
        //System.out.println( "ftrCol = " + ftrCol );
        //System.out.println( "defValue = " + defValue );

        try {
            File csvFile = new File( filePath );
            if ( csvFile.exists() == false || csvFile.isDirectory() == true ) {
                WebServer.win.log.error( "The specified file does not exists or it is a directory" );
                return false;
            }

            BufferedReader input = new BufferedReader( new FileReader( csvFile ) );
            String line;
            Connection con = dbAccess.getConnection();
            Statement stmt = con.createStatement();
            int rows = 0;
            while ( ( line = input.readLine() ) != null ) {
                String tokens[] = line.split( cs );                
                String usr = tokens[usrCol];                
                String sql = "REPLACE DELAYED INTO " + DBAccess.USER_TABLE + " ( " + DBAccess.USER_TABLE_FIELD_USER + "," + DBAccess.FIELD_PSCLIENT + " ) VALUES ('" + usr + "', '" + clientName + "')";
                rows += stmt.executeUpdate( sql );
            }
            stmt.close();

            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + 0 + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( Exception e ) {
            success = false;
            WebServer.win.log.error( "-Problem inserting to DB: " + e );
        }
        return success;
    }

    //loads num data logs
    private int loadLog( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        //execute the command
        try {
            boolean success = true;
            dbAccess.setAutoCommit( false );
            success = execLoadLog( queryParam, respBody, dbAccess );
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

    private boolean execLoadLog( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        boolean success = true;
        int clntIdx = queryParam.qpIndexOfKeyNoCase( "clnt" );
        String clientName = ( String ) queryParam.getVal( clntIdx );
        //System.out.println( "clnt = " + clientName );

        int filePathIdx = queryParam.qpIndexOfKeyNoCase( "path" );
        if ( filePathIdx == -1 ) {
            WebServer.win.log.error( "Parameter path is missing" );
            return false;
        }
        String filePath = ( String ) queryParam.getVal( filePathIdx );
        //System.out.println( "filePath = " + filePath );

        int csIdx = queryParam.qpIndexOfKeyNoCase( "cs" );
        if ( csIdx == -1 ) {
            WebServer.win.log.error( "Parameter cs is missing" );
            return false;
        }
        String cs = ( String ) queryParam.getVal( csIdx );
        cs.replace( "\\", "\\\\");
        System.out.println( "cs = " + cs );

        int usrColIdx = queryParam.qpIndexOfKeyNoCase( "usrcol" );
        int usrCol;
        if ( usrColIdx == -1 ) {
            WebServer.win.log.debug( "Parameter usrcol is missing, assuming it is 0" );
            usrCol = 0;
        }
        usrCol = Integer.parseInt( ( String ) queryParam.getVal( usrColIdx ) );

        int ftrColIdx = queryParam.qpIndexOfKeyNoCase( "ftrcol" );
        int ftrCol;
        if ( ftrColIdx == -1 ) {
            WebServer.win.log.debug( "Parameter ftrcol is missing, assuming it is 1" );
            ftrCol = 1;
        }
        ftrCol = Integer.parseInt( ( String ) queryParam.getVal( ftrColIdx ) );

        int numColIdx = queryParam.qpIndexOfKeyNoCase( "numcol" );
        int numCol;
        if ( numColIdx == -1 ) {
            WebServer.win.log.debug( "Parameter numcol is missing, num_data will not be filled with new entries" );
            numCol = -1;
        } else {
            numCol = Integer.parseInt( ( String ) queryParam.getVal( numColIdx ) );
        }

        int timeIdx = queryParam.qpIndexOfKeyNoCase( "timecol" );
        int timeCol;
        if ( timeIdx == -1 ) {
            WebServer.win.log.debug( "Parameter timecol is missing, assuming it is 3" );
            timeCol = 1;
        }
        timeCol = Integer.parseInt( ( String ) queryParam.getVal( timeIdx ) );

        String extra;
        int renIdx = queryParam.qpIndexOfKeyNoCase( "ren" );
        if ( renIdx == -1 ) {
            extra = "";
        } else
            extra = ( String ) queryParam.getVal( renIdx );

        int sesColIdx = queryParam.qpIndexOfKeyNoCase( "sescol" );
        int sesGenColIdx = queryParam.qpIndexOfKeyNoCase( "sesgen" );
        int ses;
        if ( sesColIdx == -1 && sesGenColIdx == -1 ) {
            WebServer.win.log.debug( "Parameter sescol and sesgen is missing" );
            return false;
        }
        if ( sesColIdx != -1 && sesGenColIdx != -1 ) {
            WebServer.win.log.debug( "The parameters sescol and sesgen must not coexists" );
            return false;
        }
        if ( sesColIdx != -1 ) {
            ses = Integer.parseInt( ( String ) queryParam.getVal( sesColIdx ) );
        } else {
            ses = Integer.parseInt( ( String ) queryParam.getVal( sesGenColIdx ) );
        }

        try {
            File csvFile = new File( filePath );
            if ( csvFile.exists() == false || csvFile.isDirectory() == true ) {
                WebServer.win.log.error( "The specified file does not exists or it is a directory" );
                return false;
            }

            int sesId;
            BufferedReader input = new BufferedReader( new FileReader( csvFile ) );
            String line;
            //int sesId = 0;
            long curTime = System.currentTimeMillis();
            int rowsAffected = 0;
            Statement stmtDecay = dbAccess.getConnection().createStatement();
            Statement stmtNum = dbAccess.getConnection().createStatement();
            HashMap<String, Integer> nextSesIds = new HashMap<String, Integer>(100);
            HashMap<String, Long> prevTime = new HashMap<String, Long>(100);
            int lineNum = 0;
            while ( ( line = input.readLine() ) != null ) {
                lineNum ++;
                if( line.trim().equals("") )
                    continue;
                String tokens[] = line.split( cs );
                if( tokens.length < usrCol - 1 || tokens.length < ftrCol - 1 ) {
                    throw new Exception( "invalid contant at line " + lineNum  );
                }
                String usr = tokens[usrCol];
                String ftr = extra + tokens[ftrCol];
                long time = Long.parseLong( tokens[timeCol] );
                System.out.println( usr + " --- " + ftr + " --- " + time );
                if ( sesColIdx != -1 ) {
                    //System.out.println("i will read the session id");
                    sesId = Integer.parseInt( tokens[sesColIdx] );
                } else {

                    Integer nextSes = nextSesIds.get( usr );
                    //System.out.println( "User " + usr );
                    if( nextSes == null ) {
                        nextSes = 0;
                        sesId = 0;
                        nextSesIds.put( usr, nextSes );
                        prevTime.put( usr, time );
                    } else {
                        Long prevTimeStamp = prevTime.get( usr );
                        if( prevTimeStamp == null){
                            prevTimeStamp = 0L;
                        }
                        //System.out.println( "prev " + prevTimeStamp + " diff " + ( time - prevTimeStamp ) + " ses " + ses );
                        if( time - prevTimeStamp > ses ){
                            nextSes ++;
                            nextSesIds.put( usr, nextSes );
                            prevTime.put( usr, time );
                        }
                        sesId = nextSes;
                    }
                    //System.out.println("new ses " + nextSes );
                }
                //System.out.println( "User " + usr + " sid " + sesId );
                String query = "INSERT DELAYED INTO decay_data (dd_user, dd_feature, dd_timestamp, " +
                        DBAccess.FIELD_PSCLIENT + ", FK_session ) VALUES ('" + usr + "', '" +
                        ftr + "', '" + time + "' ,'" +
                        clientName + "'," + sesId + ")";
                rowsAffected += stmtDecay.executeUpdate( query );

                if ( numCol != -1 ) {
                    float val = Float.parseFloat( tokens[numCol] );
                    //PNumData num = new PNumData( usr, ftr, val, time, sesId );
                    //dbAccess.insertDelayedNewNumData( num, clientName );
                    query = "INSERT DELAYED INTO num_data (nd_user, nd_feature, nd_timestamp, nd_value, sessionId, " +
                            DBAccess.FIELD_PSCLIENT + " ) VALUES ('" + usr + "', '" + ftr +
                            "', " + time + ", " + val + "," +
                            sesId + ",'" + clientName + "')";

                    rowsAffected += stmtNum.executeUpdate( query );
                }
            }

            stmtDecay.close();
            stmtNum.close();

            respBody.append( DBAccess.xmlHeader( "/resp_xsl/rows.xsl" ) );
            respBody.append( "<result>\n" );
            respBody.append( "<row><num_of_rows>" + rowsAffected + "</num_of_rows></row>\n" );
            respBody.append( "</result>" );
        } catch ( Exception e ) {
            success = false;
            WebServer.win.log.error( "-Problem inserting to DB: " + e );
            e.printStackTrace();
        }
        return success;
    }
}
