package pserver.pservlets;

import pserver.data.VectorMap;
import java.util.*;
import java.sql.*;

import pserver.*;
import pserver.domain.PServerClient;
import pserver.data.DBAccess;
import pserver.logic.PSReqWorker;
import pserver.utilities.*;

/**
 *
 * @author alexandros
 */
public class Admin implements pserver.pservlets.PService {

    //the name that belongs to this servlet, needed becose this pservlet makes requests on its self
    private String myName;
    private String administrator_name;
    private String administrator_pass;

    public void init( String[] params ) throws Exception {
        if ( params == null ) {
            throw new Exception( this.getClass().getName() + ":this class loaded with wrong parameters" );
        } else if ( params.length != 3 ) {
            throw new Exception( this.getClass().getName() + ":this class loaded with wrong parameters" );
        } else {
            myName = params[0];
            administrator_name = params[1];
            administrator_pass = params[2];
        }
    }

    public String getMimeType() {
        return this.html;
    }

    public int service( VectorMap parameters, StringBuffer response, DBAccess dbAccess ) {
        int respCode = PSReqWorker.NORMAL;
        VectorMap queryParam;
        StringBuffer respBody;

        queryParam = parameters;
        respBody = new StringBuffer();

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
        String com = ( String ) parameters.getVal( comIdx );
        //commands about pserver administration
        if ( com.equalsIgnoreCase( "login" ) ) {
            respCode = comAdmiinLogin( queryParam, response );
        } else if ( com.equalsIgnoreCase( "mkusrfrm" ) ) {
            respCode = comAdmiinMkUsrFrm( queryParam, response, dbAccess );
        } else if ( com.equalsIgnoreCase( "addClnt" ) ) {
            respCode = comAdmiinAddClnt( queryParam, response, dbAccess );
        } else if ( com.equalsIgnoreCase( "checkdb" ) ) {
            respCode = comAdmiincheckdb( queryParam, response, dbAccess );
        } else if ( com.equalsIgnoreCase( "chngpropfrm" ) ) {
            respCode = comAdmiinChngPropFrm( queryParam, response );
        } else if ( com.equalsIgnoreCase( "updateProperties" ) ) {
            respCode = comAdmiinUpdateProperties( queryParam, response );
        } else if ( com.equalsIgnoreCase( "delusr" ) ) {
            respCode = comAdmiinDeleteUser( queryParam, response, dbAccess );
        } else {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error( "-Request command not recognized" );
            return respCode;
        }
        response.append( respBody.toString() );
        return respCode;
    }

    //ADMIN_MODE commands:
    private int comAdmiinUpdateProperties( VectorMap queryParam, StringBuffer respBody ) {
        int lnIdx = queryParam.qpIndexOfKeyNoCase( "login_name" );
        int lpIdx = queryParam.qpIndexOfKeyNoCase( "login_password" );

        String login_name = ( String ) queryParam.getVal( lnIdx );
        String login_pass = ( String ) queryParam.getVal( lpIdx );

        if ( login_name == null || login_pass == null ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }

        if ( login_name.equals( administrator_name ) == false || login_pass.equals( administrator_pass ) == false ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }

        queryParam.remove( lnIdx );
        queryParam.remove( lpIdx );
        queryParam.remove( queryParam.qpIndexOfKeyNoCase( "com" ) );

        Preferences pref = PersServer.pref;

        for ( int i = 0; i < queryParam.size(); i++ ) {
            if ( pref.getPref( ( String ) queryParam.getKey( i ) ) != null ) {
                pref.setPref( ( String ) queryParam.getKey( i ), ( String ) queryParam.getVal( i ) );
            }
        }

        String htmlCode;
        int respCode = PSReqWorker.NORMAL;

        if ( pref.silentStore() ) {
            htmlCode = propertiesSaved( administrator_name, administrator_pass );
        } else {
            htmlCode = propertiesDidNotSave( administrator_name, administrator_pass );
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "Request properties update but it was impossible to write on server properties file" );
        }

        respBody.append( htmlCode );
        respBody.append( "\n" );

        return respCode;
    }

    private int comAdmiinChngPropFrm( VectorMap queryParam, StringBuffer respBody ) {
        Preferences pref = PersServer.pref;
        String[] properties = pref.getProperties();
        String[] pValues = new String[ properties.length ];

        for ( int i = 0; i < properties.length; i++ ) {
            pValues[i] = pref.getPref( properties[i] );
        }

        int lnIdx = queryParam.qpIndexOfKeyNoCase( "login_name" );
        int lpIdx = queryParam.qpIndexOfKeyNoCase( "login_password" );
        String login_name = ( String ) queryParam.getVal( lnIdx );
        String login_pass = ( String ) queryParam.getVal( lpIdx );
        if ( login_name == null || login_pass == null ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }
        if ( login_name.equals( administrator_name ) == false || login_pass.equals( administrator_pass ) == false ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }
        String htmlContent = changePropertiesForm( administrator_name, administrator_pass, properties, pValues );
        respBody.append( htmlContent );
        respBody.append( "\n" );
        return PSReqWorker.NORMAL;
    }

    private int comAdmiincheckdb( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int lnIdx = queryParam.qpIndexOfKeyNoCase( "login_name" );
        int lpIdx = queryParam.qpIndexOfKeyNoCase( "login_password" );
        String login_name = ( String ) queryParam.getVal( lnIdx );
        String login_pass = ( String ) queryParam.getVal( lpIdx );
        if ( login_name == null || login_pass == null ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }
        if ( login_name.equals( administrator_name ) == false || login_pass.equals( administrator_pass ) == false ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }
        String htmlContent = showDbContent( administrator_name, administrator_pass );
        respBody.append( htmlContent );
        respBody.append( "\n" );
        return PSReqWorker.NORMAL;
    }

    private int comAdmiinLogin( VectorMap queryParam, StringBuffer respBody ) {
        int lnIdx = queryParam.qpIndexOfKeyNoCase( "login_name" );
        int lpIdx = queryParam.qpIndexOfKeyNoCase( "login_password" );

        String login_name = ( String ) queryParam.getVal( lnIdx );
        String login_pass = ( String ) queryParam.getVal( lpIdx );

        if ( login_name == null || login_pass == null ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }

        if ( login_name.equals( administrator_name ) == false || login_pass.equals( administrator_pass ) == false ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }

        String htmlContent = loginForm( administrator_name, administrator_pass );
        respBody.append( htmlContent );
        respBody.append( "\n" );
        return PSReqWorker.NORMAL;
    }

    private int comAdmiinMkUsrFrm( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int lnIdx = queryParam.qpIndexOfKeyNoCase( "login_name" );
        int lpIdx = queryParam.qpIndexOfKeyNoCase( "login_password" );

        String login_name = ( String ) queryParam.getVal( lnIdx );
        String login_pass = ( String ) queryParam.getVal( lpIdx );

        if ( login_name == null || login_pass == null ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }

        if ( login_name.equals( administrator_name ) == false || login_pass.equals( administrator_pass ) == false ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }

        String htmlContent = manageUsersForm( administrator_name, administrator_pass, dbAccess );

        respBody.append( htmlContent );
        respBody.append( "\n" );

        return PSReqWorker.NORMAL;
    }

    private int comAdmiinDeleteUser( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int lnIdx = queryParam.qpIndexOfKeyNoCase( "login_name" );
        int lpIdx = queryParam.qpIndexOfKeyNoCase( "login_password" );

        String login_name = ( String ) queryParam.getVal( lnIdx );
        String login_pass = ( String ) queryParam.getVal( lpIdx );

        if ( login_name == null || login_pass == null ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }

        if ( login_name.equals( administrator_name ) == false || login_pass.equals( administrator_pass ) == false ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }

        try {
            //first connect to DB        
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        boolean success;
        int respCode = PSReqWorker.NORMAL;
        try {
            dbAccess.setAutoCommit( false );
            success = execAdminDeleteClient( queryParam, dbAccess );
            if ( success == true ) {
                dbAccess.commit();
                respCode = comAdmiinMkUsrFrm( queryParam, respBody, dbAccess );
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
                String htmlContent = frmAdminFaildToDeleteClient( administrator_name, administrator_pass );

                respBody.append( htmlContent );
                respBody.append( "\n" );
            }
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }

        return respCode;
    }

    private boolean execAdminDeleteClient( VectorMap queryParam, DBAccess dbAccess ) {
        int newClntNameIdx = queryParam.qpIndexOfKeyNoCase( "usr" );
        String clientName = ( String ) queryParam.getVal( newClntNameIdx );
        try {
            dbAccess.deleteClient( clientName );
            return true;
        } catch ( Exception e ) {
            WebServer.win.log.debug( e.toString() );
            return false;
        }
    }

    private int comAdmiinAddClnt( VectorMap queryParam, StringBuffer respBody, DBAccess dbAccess ) {
        int lnIdx = queryParam.qpIndexOfKeyNoCase( "login_name" );
        int lpIdx = queryParam.qpIndexOfKeyNoCase( "login_password" );
        String login_name = ( String ) queryParam.getVal( lnIdx );
        String login_pass = ( String ) queryParam.getVal( lpIdx );
        if ( login_name == null || login_pass == null ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }
        if ( login_name.equals( administrator_name ) == false || login_pass.equals( administrator_pass ) == false ) {
            WebServer.win.log.error( "-Request administration access with wrong attributes" );
            return PSReqWorker.ACCESS_DENIED;
        }
        //first connect to DB
        try {
            dbAccess.connect();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        int respCode = PSReqWorker.NORMAL;
        int success;

        try {
            dbAccess.setAutoCommit( false );
            success = execAdminAddClient( queryParam, dbAccess );
            if ( success == 0 ) {
                dbAccess.commit();
                String htmlContent = userInserted( administrator_name, administrator_pass );
                respBody.append( htmlContent );
                respBody.append( "\n" );
            } else if ( success == 1 ) {
                dbAccess.commit();
                String htmlContent = userAllreadyExist( administrator_name, administrator_pass );
                respBody.append( htmlContent );
                respBody.append( "\n" );
            } else {
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;  //client request data inconsistent?
                WebServer.win.log.warn( "-DB rolled back, data not saved" );
            }
            dbAccess.disconnect();
        } catch ( SQLException e ) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error( "-DB Transaction problem: " + e );
        }

        return respCode;
    }

    private int execAdminAddClient( VectorMap queryParam, DBAccess dbAccess ) {
        int newClntNameIdx = queryParam.qpIndexOfKeyNoCase( "name" );
        int newClntPassIdx = queryParam.qpIndexOfKeyNoCase( "password" );
        String newClntName = ( String ) queryParam.getVal( newClntNameIdx );
        String newClntPass = ( String ) queryParam.getVal( newClntPassIdx );
        try {
            if ( dbAccess.clientNameExists( newClntName ) == true ) {
                return 1;
            }
            dbAccess.insertPServerClient( newClntName, newClntPass );
            return 0;
        } catch ( Exception e ) {
            return -1;
        }
    }

    public String loginForm( String name, String password ) {
        String form;
        form = upperTemplate( name, password ) +
                "A general-puprose Personalization Server called <b>PServer</b>." +
                "<p>" +
                "Manages user profiles and preferences, in the frame of an " +
                "application. Helps the application to tailor itself to " +
                "its users." +
                "<p>" +
                "Extends YServer, a simple but expandable web server. YServer" +
                " supports multiple users, requests for file resources under a" +
                " public directory root, GET and POST HTTP methods, MIME types," +
                " and loading application settings from an initialization file." +
                " Presently PServer uses RDBMS to store its data and had been tested with MS Access " +
                "and MySQL Databases but it is created to work with any database supports" +
                " the standar SQL syntax." +
                "<p>" +
                "An extended documentation of the PServer can be found by" +
                " running the server and connecting through a web browser to:" +
                "<br>" +
                "<pre>\n" +
                "    http://persserver:port/\n" +
                "</pre>" +
                "Three main modes are offered: Personal ,Stereotypes and Communities. Each mode supports a number of operations that can be performed by issuing suitable HTTP requests to the Personalization Server. The two modes are independent from each other. They share the same database, however they are supported by a separate set of DB tables." +
                "<br>The syntax of those special requests are as follows:" +
                "<br>" +
                "<pre>" +
                "http://server:port/&lt;clnt=client_name|client_pass>&lt;mode_id>?&lt;query_string>" +
                "</pre>" +
                "The clnt part contains the name and the password of the client that wants to be serviced by the PServer " +
                "and can excluded if the server sunt in anonymous mode. This can be aranged by PServer properties<br>" +
                "For Personal mode the mode ID is \"pers\", while for Stereotypes mode the mode ID is \"ster\" and for communities mode the mode ID is \"commu\". The query string identifies the operation and its parameters, and is described separetely for each operation.<br> Reserved words in the query string are case independent." +
                "All names in the query string must conform to the syntax defined on URLs, that is, they must not contain spaces, +, =, &, ?, etc. In addition, to avoid confusion with XML syntax, the characters < and > are also not allowed." +
                "The answer is formatted as XML. The XML answer must have a single element called <result>, encompassing a number of <row> elements, each containing the data elements corresponding to the specific request. By having such a standard format it becomes easier for the applications to parse all XML answers in a uniform way." +
                "For error handling, a number of HTTP error codes are used to denote success, client error (wrong request), or server error" +
                bottomTemplate();
        return form;
    }
    //this method creates the nessesary html code to fill the forms with the PServer logo
    //and the administrating menu
    private String upperTemplate( String name, String password ) {
        String htmlCode = "<HTML>" +
                "<HEAD><TITLE>PServer Administration panel</TITLE></HEAD>" +
                "<BODY bgcolor=\"#FFFFCC\">" +
                "<FONT >" +
                "<P align=\"center\"><FONT size=\"18px\">PServer Administration panel</FONT></P>" +
                "<TABLE>" +
                "<TR valign=\"top\">" +
                "<TD>" +
                menu( name, password ) +
                "</TD>" +
                "<TD align=\"left\" valign=\"top\">" +
                "<TABLE bgcolor=\"#ffdc75\"  width=\"100%\"><TR><TD align=\"left\" valign=\"top\">";
        return htmlCode;
    }

    private String bottomTemplate() {
        String htmlCode = "</td></tr></table>" +
                "</TD>" +
                "</TR>" +
                "</TABLE>" +
                "</FONT>" +
                "</BODY>" +
                "</HTML>";
        return htmlCode;
    }
    //this method generates the html code for the administrating menu
    private String menu( String name, String password ) {
        return "<TABLE BORDER=\"1\"><TR><TD><TABLE border=\"1\" bordercolor=\"#000000\" cellpadding=\"3\" cellspacing=\"0\" width=\"180\">" +
                "<TR><TD bgcolor=\"#ffdc75\" nowrap=\"nowrap\" align=\"left\" >" +
                "<A href=\"/admin?login_name=" + name + "&login_password=" + password + "&com=login\">Index page</a>" +
                "</TD</TR>" +
                "<TR><TD bgcolor=\"#ffdc75\" nowrap=\"nowrap\" align=\"left\" >" +
                "<A href=\"/admin?login_name=" + name + "&login_password=" + password + "&com=mkusrfrm\">PServer clients</a>" +
                "</TD</TR>" +
                "<TR><TD bgcolor=\"#ffdc75\" nowrap=\"nowrap\" align=\"left\" >" +
                "<A href=\"/admin?login_name=" + name + "&login_password=" + password + "&com=chngpropfrm\">Change PServer properties</a>" +
                "</TD</TR>" +
                //"<TR><TD bgcolor=\"#ffdc75\" nowrap=\"nowrap\" align=\"left\" >" +
                //"<A href=\"/admin?login_name=" + name + "&login_password=" + password + "&com=checkdb\">Check PServer database</a>" +
                //"</TD</TR>" +
                "<TR><TD bgcolor=\"#ffdc75\" nowrap=\"nowrap\" align=\"left\" >" +
                "<A href=\"/pers_help/pers_mode.html\" target=\"_blank\">Pers mode help</a>" +
                "</TD</TR>" +
                "<TR><TD bgcolor=\"#ffdc75\" nowrap=\"nowrap\" align=\"left\" >" +
                "<A href=\"/ster_help/ster_mode.html\" target=\"_blank\">Ster mode help</a>" +
                "</TD</TR>" +
                "<TR><TD bgcolor=\"#ffdc75\" nowrap=\"nowrap\" align=\"left\" >" +
                "<A href=\"/commu_help/commu_mode.html\" target=\"_blank\">Commu mode help</a>" +
                "</TD</TR>" +
                "</TABLE></TD</TR></TABLE>";
    }

    public String manageUsersForm( String name, String password, DBAccess dbAccess ) {
        String usersPart = "<p>PServer Clients</p>\n" +
                "<hr>\n" +
                "<table>\n";
        try {
            dbAccess.connect();
            LinkedList<PServerClient> clients = dbAccess.getPserverClients();
            for ( PServerClient client : clients ) {
                usersPart += "<tr><td>" + client.getName() + "</td><td><a href=/admin?login_name=" + name + "&login_password=" + password + "&com=delusr&usr=" + client.getName() + ">Delete</a></td></tr>\n";
            }
            dbAccess.disconnect();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        usersPart += "</table>";
        String form;
        form = upperTemplate( name, password ) +
                "<br><p>Fill the fields with the attributes for the new user</p>\n" +
                "<TABLE>\n" +
                "<form name=\"user_creation\" method=\"post\" action=\"admin\">\n" +
                "<input type=\"hidden\" name=\"com\" value=\"addClnt\">\n" +
                "<input type=\"hidden\" name=\"login_name\" value=\"" + name + "\">\n" +
                "<input type=\"hidden\" name=\"login_password\" value=\"" + password + "\">\n" +
                "<TR><TD>Name</TD><TD><input type=\"text\" name=\"name\"></TD></TR>\n" +
                "<TR><TD>Password</TD><TD><input type=\"password\" name=\"password\"></TD></TR>\n" +
                "<TR><TD>Retype password</TD><TD><input type=\"password\" name=\"retyped_password\"></TD></TR>\n" +
                "<TD><input type=\"button\" name=\"insert_user\" value=\"Insert User\"" +
                " onClick=\"if(password.value!=retyped_password.value)" +
                "alert('You did not give the same password twice');" +
                "else document.forms.user_creation.submit();" +
                "\"" +
                "></TD></TR>\n" +
                "</form>\n" +
                "</TABLE>\n" +
                usersPart +
                bottomTemplate();
        return form;
    }

    private String frmAdminFaildToDeleteClient( String name, String password ) {
        String form;
        form = upperTemplate( name, password ) +
                "<p>Failed to delete Pserver client </p>" +
                bottomTemplate();
        return form;
    }

    public String userInserted( String name, String password ) {
        String form;
        form = upperTemplate( name, password ) +
                "<p>New pserver client inserted succesfully, Please remember the password that you just gave" +
                " becouse password is stored encrypted and there is no way to get it back</p>" +
                bottomTemplate();
        return form;
    }

    public String userAllreadyExist( String name, String password ) {
        String form;
        form = upperTemplate( name, password ) +
                "<p>There is allready a users with the same name in the database" +
                " please choose another name</p>" +
                bottomTemplate();
        return form;
    }

    public String showDbContent( String name, String password ) {
        String form;
        form = upperTemplate( name, password ) +
                "<HTML>" +
                "<HEAD>" +
                "<TITLE>Database Tables</TITLE>" +
                "</HEAD>" +
                "<BODY>" +
                "<h1>Database Tables</h1>" +
                "<p>" +
                "<hr>" +
                "<h3>Personal mode</h1>" +
                "<br>" +
                "<a href=\"/pers?clnt=" + name + "|" + password + "&com=getdef&ftr=*\">" +
                "<h3>up_features</h3>" +
                "</a>" +
                "<p>" +
                "<a href=\"/pers?clnt=" + name + "|" + password + "&com=sqlusr&whr=*\">" +
                "<h3>user_profiles</h3>" +
                "</a>" +
                "</p>" +
                "<p>" +
                "<a href=\"/pers?clnt=" + name + "|" + password + "&com=getdrt\">" +
                "<h3>decay_groups</h3>" +
                "</a>" +
                "</p>" +
                "<p>" +
                "<a href=\"/pers?clnt=" + name + "|" + password + "&com=sqlddt&whr=*\">" +
                "<h3>decay_data</h3>" +
                "</a>" +
                "<p>" +
                //"<a href=\"/pers?clnt="+name+"|"+password+"&com=sqlndt&whr=*\">"+
                //"<h3>num_data</h3>"+
                //"</a>"+
                "</p>" +
                "<p>" +
                "<hr>" +
                "<h3>Stereotype mode</h3>" +
                "<br>" +
                "<a href=\"/ster?clnt=" + name + "|" + password + "&com=lststr&str=*\">" +
                "<h3>stereotypes</h3>" +
                "</a>" +
                "<p>" +
                "<a href=\"/ster?clnt=" + name + "|" + password + "&com=sqlstr&whr=*\">" +
                "<h3>stereotype_profiles</h3>" +
                "</a>" +
                "<p>" +
                "<a href=\"/ster?clnt=" + name + "|" + password + "&com=sqlusr&whr=*\">" +
                "<h3>stereotype_users</h3>" +
                "</a>" +
                "</p>" +
                "<hr>" +
                "<h3>Communities mode</h3>" +
                "<br>" +
                "<p>" +
                "<a href=\"/commu?clnt=" + name + "|" + password + "&com=sqlcom&whr=*\">" +
                "<h3>communities</h3>" +
                "</a>" +
                "</p>" +
                "<p>" +
                "<a href=\"/commu?clnt=" + name + "|" + password + "&com=sqlftrgrp&whr=*\">" +
                "<h3>feature groups</h3>" +
                "</a>" +
                "</p>" +
                "<hr>" +
                "</p>" +
                "</BODY>" +
                "</HTML>" +
                bottomTemplate();
        return form;
    }

    public String changePropertiesForm( String name, String password, String[] properties, String[] pValues ) {
        StringBuffer propertiesFormBody = new StringBuffer();
        for ( int i = 0; i < properties.length; i++ ) {
            propertiesFormBody.append( "<tr><td>" + properties[i] + "</td>\n" );
            propertiesFormBody.append( "<td><input type=\"text\" name=\"" + properties[i] + "\" value=\"" + pValues[i] + "\"></td>\n" );
            propertiesFormBody.append( "</td></tr>\n" );
        }
        String form;
        form = upperTemplate( name, password ) +
                "<br>\n" +
                "<p>Make the desirable changes to any field that you want and then press the button that lies in  bottom</p>\n" +
                "<table>\n" +
                "<form name=\"change_properties\" method=\"get\" action=\"admin\">\n" +
                "<input type=\"hidden\" name=\"com\" value=\"updateProperties\">\n" +
                "<input type=\"hidden\" name=\"login_name\" value=\"" + name + "\">\n" +
                "<input type=\"hidden\" name=\"login_password\" value=\"" + password + "\">\n" +
                propertiesFormBody.toString() +
                "<tr><td><input type=\"submit\" value=\"Change properties\"></td></tr>\n" +
                "</form>\n" +
                "</table>\n" +
                bottomTemplate();
        return form;
    }

    public String propertiesSaved( String name, String password ) {
        String form;
        form = upperTemplate( name, password ) +
                "<p>Properties updated and thay will take effenct the next time that the server will be restarted</p>" +
                bottomTemplate();
        return form;
    }

    public String propertiesDidNotSave( String name, String password ) {
        String form;
        form = upperTemplate( name, password ) +
                "<p>It was impossible to save the new properties on server's properties file</p>" +
                bottomTemplate();
        return form;
    }

    public Admin() {
    }
}
