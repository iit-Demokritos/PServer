//===================================================================
// WebServer
//
// The main class. One object of this class is created and assigned
// to a static variable, so that other classes can access its public
// variables. Creates the server daemon and the UI window plus a
// number of other classes, that are assigned to static variables so
// that they can access each other.
//
// In the frame of other applications, this class may be overriden
// by other classes that expand or modify the server functionality.
//===================================================================
package pserver;

import java.util.*;
import java.io.*;

import pserver.userInterface.*;
import pserver.logic.*;
import pserver.utilities.*;

public class WebServer {
    //private variables
    protected int port;            //port server is running, free: > 1024
    protected int maxReq;          //max concurrent client requests
    protected String logMode;      //on: screen & file, screen: screen only, file: file only, off: no msgs
    protected boolean debugMode;   //if true (and logMode allowed), debug msgs displayed on screen
    protected String prefFile;     //application settings ini file
    protected String prefHeader;   //header in ini file
    protected String mimeFile;     //path to mime file
    protected String logPath;      //log file path
    protected long maxFLSize;      //max FileLog size in bytes
    protected int maxALLines;      //max ActivityLog (display msg) lines
    protected boolean sslOn;        //enables https 
    //public variables
    public int reqTimeout;       //timeout for reading requests in millisecs, 0 for infinite
    public String mainDir;       //main HTML directory
    public String defHTML;       //default HTML page
    public String appName;       //application name
    public String appVers;       //application version
    public String appCapt;       //application window caption
    //one object for each of the main classes
    static public WebServer obj;        //the single WebServer object
    static public WServer srv;          //the actual web server
    static public ControlWin win;       //the UI window
    static public Preferences pref;     //application settings
    static public Properties mime;      //mime types correspondence
    static public FileLog flog;         //log file
    
    /*
    //main method
    static public void main(String[] args) {  //not used any more, replaced by subclass!
        obj = new WebServer();
    }
    */
    //terminates properly the application
    static public void terminate(boolean normal) {
        pref.store();  //save application settings
        flog.forceWriteln("Shutting down server, normal: " + normal);
        if (normal) System.exit(0);
        else System.exit(1);
    }
    
    //initializer
    protected WebServer() {
        //all is done by the overriding subclass
    }
    
    //auxiliary methods
    protected void initVars() {
        prefFile = "./server.ini";
        mimeFile = "./mime.ini";
    }
    protected void loadSettings() {
        port = Integer.parseInt(pref.getPref("port"));
        maxReq = Integer.parseInt(pref.getPref("max_requests"));
        logMode = pref.getPref("log_mode");
        debugMode = (pref.getPref("debug_mode")).equals("on")? true: false;  //on - off
        reqTimeout = Integer.parseInt(pref.getPref("read_request_timeout"));
        mainDir = pref.getPref("main_html_dir");
        defHTML = pref.getPref("def_html_file");
        logPath  = pref.getPref("log_file_path");
        maxFLSize  = Long.parseLong(pref.getPref("max_log_size"));        
        sslOn=(pref.getPref("ssl")).equals("on")? true: false;  //on - off
    }
    protected void setMessageMode() {
        //set if msgs are displayed on screen and/or written in log file
        if (logMode.equals("on")) {  //both screen and file
            win.log.setLog(true);
            flog.setLog(true);
        } else if (logMode.equals("screen")) {  //screen only
            win.log.setLog(true);
            flog.setLog(false);
        } else if (logMode.equals("file")) {  //file only
            win.log.setLog(false);
            flog.setLog(true);
        } else {  //logMode is 'off', neither screen nor file
            win.log.setLog(false);
            flog.setLog(false);
        }
        //set detail of msgs displayed on screen (if they are displayed)
        win.log.setDebug(debugMode);
    }
    protected void loadMime() {
        try {
            FileInputStream in = new FileInputStream(mimeFile);
            mime.load(in);
        } catch(IOException e) {
            win.log.error("Problem loading mime types: " + e);
            flog.writeln("Problem loading mime types: " + e);
        }
    }
}

