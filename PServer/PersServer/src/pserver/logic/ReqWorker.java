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
//===================================================================
// ReqWorker
//
// Handles a single request. Analyses the request and forwards
// the corresponding response. Supports only requests for files.
// It can be used as a base class by subclasses that extend the
// basic FILE_MODE 'respMode' functionality (requests for files)
// to other modes of response, simulating calls to CGI scripts.
//
// This class, for the purpose of sending files to clients, does
// not need the 'queryParam' variable, or the method 'parseQueryParam',
// or the call to this method. Consequently, the class 'VectorMap'
// is not needed either. However, all those exist for supporting
// possible subclasses that will need to have the query parameters
// of the request parsed.
//===================================================================
package pserver.logic;

import pserver.data.VectorMap;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.text.*;

import pserver.*;

/**
 *
 * @author scify
 * @author Nick Zorbas <nickzorb@gmail.con>
 */
public class ReqWorker extends Thread {

    /**
     *
     */
    public Socket sock;                //socket for particular client request
    /**
     *
     */
    public InetAddress clientAddr;     //client address
    /**
     *
     */
    public int port;                   //socket port (dynamically allocated)
    //request parameters
    /**
     *
     */
    public String request = null;          //the incoming HTTP message
    /**
     *
     */
    public String method = null;           //HTTP method
    /**
     *
     */
    public String resURI = null;           //requested resource URI (no query string)
    /**
     *
     */
    public String queryStr = null;         //query string
    /**
     *
     */
    public VectorMap queryParam = null;    //query parameter name-value pairs
    /**
     *
     */
    public String[] initParam = new String[1];
    //control variables
    /**
     *
     */
    public int respCode;            //code of response (error-handling)
    /**
     *
     */
    public int respMode;            //mode of response (response process)
    //response variables
    /**
     *
     */
    public StringBuffer httpMsg;           //HTTP message
    /**
     *
     */
    public String status;                  //HTTP status code
    /**
     *
     */
    public String mimeType;                //MIME type
    /**
     *
     */
    public FileInputStream rbFile;         //response body comes from a file
    /**
     *
     */
    public String rbString;                //response body is a string (alternatively)
    /**
     *
     */
    public long rbLength;                  //no of bytes of response body
    //codes of response handle errors, <= 0
    /**
     *
     */
    static public final int NORMAL = 0;    //request OK, normal response
    /**
     *
     */
    static public final int NO_RESOURCE = -1;    //requested resource not found
    /**
     *
     */
    static public final int REQUEST_ERR = -2;    //client request not understood
    /**
     *
     */
    static public final int NO_SERVICE = -3;    //requested service not supported
    /**
     *
     */
    static public final int SERVER_ERR = -4;    //unexpected server error
    /**
     *
     */
    static public final int ACCESS_DENIED = -5;    //access denied error
    /**
     *
     */
    static public final int DUBLICATE_ERROR = -6;    //access denied error
    //modes of response, specify process of responding, > 0
    /**
     *
     */
    static public final int FILE_MODE = 1;    //request for physical file
    /**
     *
     */
    static public final int UPLOAD_MODE = 2;    //request to upload file - NOT SUPPORTED yet
    //variables relevant to response body
    /**
     *
     */
    public String resPath = null;          //requested file physical path
    /**
     *
     */
    public boolean resAccessible = false;  //true if resource is accessible
    /**
     *
     */
    public String resFileExt = null;       //file extension of resource

    //initializers
    /**
     *
     * @param sock
     */
    public ReqWorker(Socket sock) {
        super();
        this.sock = sock;
        clientAddr = sock.getInetAddress();
        port = sock.getPort();  //can be used as request ID
    }

    //run method
    /**
     *
     */
    @Override
    public void run() {
        //log the request
        logRequest();
        //do the job
        analyseRequest();
        respond();
    }

    /**
     * This method analyzes a request.
     */
    public void analyseRequest() {

        respCode = NORMAL;
        if (!getRequest()) {
            respCode = SERVER_ERR;
            return;  //no point in proceeding
        }
        if (!parseRequest()) {
            respCode = REQUEST_ERR;
            return;  //no point in proceeding
        }
        if (!methodSupported()) {
            respCode = NO_SERVICE;
            return;  //no point in proceeding
        }

        //add rest decoder here???
        // restDecoder();
        //////////////////////////
        parseQueryParam();
        //call rest url parser to take extra vars
        parseRestParams();

        switchRespMode();
    }

    /**
     * If request is restfully then convert ti to
     */
    public void restDecoder() {
    }

    /**
     * Accepts the incoming HTTP message.
     *
     * @return True if a successful HTTP message was received, False otherwise.
     */
    public boolean getRequest() {
        //fill 'request' variable
        try {
            sock.setSoTimeout(WebServer.obj.reqTimeout);  //maximum blocking time in millisecs
            //open input stream to read from client
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), Charset.forName("UTF-8")));
            //read client request
            int charRead;
            StringBuilder sBuilder = new StringBuilder();
            while ((charRead = in.read()) != -1) {  //until end of input stream
                char ch = (char) charRead;
                sBuilder.append(ch);
                //HTTP requests from browsers are not always followed by -1.
                //If no more chars to read, assume end of stream is reached!
                if (!in.ready()) {
                    break;
                }
            }
            request = java.net.URLDecoder.decode(sBuilder.toString(), "UTF-8");
            System.out.println(request);
        } catch (InterruptedIOException e) {  //'reqTimeout' expired
            WebServer.win.log.error(port + "-Timeout reading request: " + e);
            WebServer.flog.writeln(port + "-Timeout reading request: " + e);
            //System.out.println(e);
            return false;
        } catch (IOException e) {
            WebServer.win.log.error(port + "-Problem receiving: " + e);
            WebServer.flog.writeln(port + "-Problem receiving: " + e);
            // System.out.println(e);
            return false;
        } catch (NullPointerException e) {
            WebServer.win.log.error(port + "-Empty request " + e);
            WebServer.flog.writeln(port + "-Empty request " + e);
            return false;
        }
        //log info
        WebServer.win.log.debug(port + "-Request body below:\n" + request);
        return true;
    }

    /**
     * Fills all request Parameters, except {@link #queryParam}
     *
     * @return True on success, False if {@link #request} is null.
     */
    public boolean parseRequest() {
        if (request == null) {
            return false;
        }
        try {
            String[] reqLines = request.split("\n"); //Split request to lines
            String reqLine = reqLines[0];   //main request content in first line
            method = reqLine.substring(0, reqLine.indexOf(" "));
            reqLine = reqLine.substring(reqLine.indexOf("/"), reqLine.lastIndexOf(" "));
            if (reqLine.indexOf("?") == -1) {//no query parameters or POST request
                resURI = reqLine.substring(0, reqLine.length());
                queryStr = "";
            } else {   //GET request with parameters
                resURI = reqLine.substring(0, reqLine.indexOf("?"));
                queryStr = reqLine.substring(reqLine.indexOf("?") + 1, reqLine.length()).trim();
            }
            if (method.equalsIgnoreCase("POST")) {
                queryStr = "";
                boolean foundEmptyLine = false;  //search for an empty line
                for (String sCurLine : reqLines) {
                    if (foundEmptyLine) {   //the rest of the lines are 
                        queryStr += "&" + sCurLine;  //query parameters
                    }
                    if (sCurLine.matches("[ \t\n\r]*")) { //find empty line
                        foundEmptyLine = true;
                    }
                }
            }
        } catch (Exception e) {
            WebServer.win.log.error(port + "-Problem parsing request: " + e);
            WebServer.flog.writeln(port + "-Problem parsing request: " + e);
            return false;
        }
        //log info
        WebServer.win.log.debug(port + "-Request method:" + method);
        WebServer.win.log.debug(port + "-Requested resource URI:" + resURI);
        WebServer.win.log.debug(port + "-Request query str:" + queryStr);
        WebServer.flog.writeln(port + "-        " + resURI + ("".equals(queryStr) ? "" : "?" + queryStr));  //..continued from logRequest()
        return true;
    }

    /**
     * Checks if the HTTP request method is supported.
     *
     * @return True if the HTTP request method is supported, False otherwise.
     */
    public boolean methodSupported() {
        //returns true if HTTP request method is supported.
        //'method' must not be null
        if (method == null) {
            return false;
        }
        //GET and POST supported
        if (method.equalsIgnoreCase("GET")
                | method.equalsIgnoreCase("POST")) {
            return true;
        }
        return false;
    }

    /**
     * Fills the {@link #queryParam} parameter.
     */
    public void parseRestParams() {

        if (resURI.substring(1).endsWith(".xml") || resURI.substring(1).endsWith(".json")) {

            HashMap<String, String> var = PersServer.pObj.pservlets.getRestVariables(resURI.substring(1));
            int count = 1;
            for (String temp : var.keySet()) {
                queryParam.add(temp, var.get(temp));
            }

            initParam[0] = resURI.substring(1);


        }
    }

    /**
     * Fills the {@link #queryParam} parameter.
     */
    public void parseQueryParam() {
        //fill 'queryParam': each entry corresponds to an '=' in 'queryStr',
        //but only if the param name (left of '=') is not the empty string.
        //A pattern '&blabla&' without '=' will not be added in 'queryParam'.
        //Note that possible spaces and special chars are inserted encoded!
        //'queryStr' must not be null
        if (queryStr == null) {
            return;
        }
        queryParam = new VectorMap(10, 20);
        //log info
        try {
            StringTokenizer parser = new StringTokenizer(queryStr, "&", false);
            while (parser.hasMoreTokens()) {
                String pair = parser.nextToken();
                int idx = pair.indexOf("=");
                switch (idx) {
                    case -1:
                        break;  //'pair' is "", or without '=' (not added)
                    case 0:
                        break;  //param name is "" (not added)
                    //in case idx == pair.length()-1: param value is "" (added)
                    default:         //'pair' normal case: name=value (added)
                        String name = pair.substring(0, idx);
                        String value = pair.substring(idx + 1);
                        queryParam.add(name, value);
                        break;
                }
            }
        } catch (NoSuchElementException ee) {
        }  //not possible
        //log info
        for (int i = 0; i < queryParam.size(); i++) {
            WebServer.win.log.debug(port + "-Query param:" + queryParam.getKey(i) + "=" + queryParam.getVal(i));
        }
    }
    //-----------------------------------------------------------------------

    /**
     *
     */
    public void switchRespMode() {
        //this method is intented to be overidden by subclasses.
        //At this moment, request parameters are set and the application
        //can decide the type of the response based on request.
        //This class supports only requests for file resources
        if (resURI.equals("/upload")) {  //uploads not supported yet
            respMode = UPLOAD_MODE;
            analyzeUploadMode();
            return;
        } else {
            respMode = FILE_MODE;
            analyzeFileMode();
            return;
        }
    }
    //-----------------------------------------------------------------------

    /**
     *
     */
    public void analyzeUploadMode() {
        //UPLOAD_MODE not supported yet
        //log info
        WebServer.win.log.debug(port + "-Upload mode not supported yet");
    }

    /**
     *
     */
    public void analyzeFileMode() {
        //fill physical file variables
        resPath = resPhysPath(resURI);
        resAccessible = accessibleFile(resPath);
        resFileExt = getFileExt(resPath);
        //set response code if resource does not exist
        if (!resAccessible) {
            respCode = NO_RESOURCE;
        }
        //log info
        WebServer.win.log.debug(port + "-Resource file path:" + resPath);
        WebServer.win.log.debug(port + "-Resource file extension:" + resFileExt);
        WebServer.win.log.debug(port + "-Resource accessible:" + resAccessible);
    }

    //request methods: utility methods
    /**
     *
     * @param resLogURI
     * @return
     */
    public String resPhysPath(String resLogURI) {
        //given the resource logical path,
        //returns the corresponding physical path.
        if (resLogURI == null) {
            return null;
        }
        StringBuffer physPath = new StringBuffer();
        physPath.append(WebServer.obj.mainDir);
        physPath.append(resLogURI);
        if (resLogURI.charAt(resLogURI.length() - 1) == '/') {
            physPath.append(WebServer.obj.defHTML);
        }
        return physPath.substring(0);
    }

    /**
     * Checks if the file specified by the physical path is accessible.
     *
     * @param physPath The path to the file.
     * @return True if the file is accessible, False otherwise.
     */
    public boolean accessibleFile(String physPath) {
        //check if file specified by the physical path is accessible.
        if (physPath == null) {
            return false;
        }
        boolean accessible;
        File theFile = new File(physPath);
        if (theFile.isDirectory()) {
            physPath += WebServer.obj.defHTML;
        }
        theFile = new File(physPath);
        accessible = theFile.canRead();
        //accessible = accessible && theFile.isFile();  //not a directory
        return accessible;
    }

    /**
     *
     * @param filename
     * @return
     */
    public String getFileExt(String filename) {
        //returns the file extension of 'filename'
        if (filename == null) {
            return null;
        }
        String fileExt = "";  //default
        StringBuffer sb = (new StringBuffer(filename)).reverse();  //reverse string
        try {
            StringTokenizer parser = new StringTokenizer(sb.substring(0), "./", true);
            String ext, delim;
            ext = parser.nextToken();
            delim = parser.nextToken();
            if (delim.compareTo(".") == 0) {
                fileExt = (new StringBuffer(ext)).reverse().substring(0);
            }
        } catch (NoSuchElementException e) {
            return fileExt;
        }
        return fileExt;
    }

    //response methods: composing response
    /**
     *
     */
    public void respond() {
        respBody();
        httpHeader();
        sendResponse();
    }

    /**
     * Checks the the response Code. The response Body must be attached to the
     * {@link #rbFile}if from a file, or to the {@link #rbString} if a string.
     * Body, length and MIME type must be determined. First, consider response
     * according to possible errors while analyzing the request.
     */
    public void respBody() {
        //response body must be attached to stream 'rbFile'
        //if from a file, or 'rbString' if a string.
        //body, length, and MIME type must be determined.
        //first, consider response according to
        //possible errors while analysing request
        rbFile = null;
        rbString = null;
        rbLength = 0;
        //mimeType = null;
        switch (respCode) {
            case NORMAL:
                switchRespBody();
                break;
            //error - no response body (just HTTP header)
            case NO_RESOURCE:
            case REQUEST_ERR:
            case NO_SERVICE:
            case ACCESS_DENIED:
            case SERVER_ERR:
                break;
        }
    }
    //-----------------------------------------------------------------------

    /**
     *
     */
    public void switchRespBody() {
        //this method is intented to be overidden by subclasses.
        //determines response body, length, and MIME type.
        //No errors (NORMAL respCode), consider response mode.
        //only the FILE_MODE is currently supported by this class
        switch (respMode) {
            case UPLOAD_MODE:
                rbString = "<html><body>Uploads are not supported yet.</body></html>";
                rbLength = rbString.length();
                mimeType = getMIMEType("html");
                break;
            case FILE_MODE:
                rbFile = streamResponseFile(resPath);
                rbLength = (new File(resPath)).length();
                mimeType = getMIMEType(resFileExt);
                break;
        }
    }
    //-----------------------------------------------------------------------

    /**
     *
     */
    public void httpHeader() {
        //sets 'status' and 'httpMsg' response variables.
        //map response codes to HTTP codes
        //NOTE: codes with confirmed meaning: 200, 404, 501
        status = "501";    //default: server error
        switch (respCode) {
            case NORMAL:
                status = "200 OK";
                break;  //everything OK
            case NO_RESOURCE:
                status = "404";
                break;  //client error
            case REQUEST_ERR:
                status = "401";
                break;  //client error
            case NO_SERVICE:
                status = "504";
                break;  //server error
            case SERVER_ERR:
                status = "501";
                break;  //server error
            case ACCESS_DENIED:
                status = "403.8";
                break;  //client error
        }
        //prepare HTTP message
        httpMsg = new StringBuffer(
                "HTTP/1.0 " + status + "\n"
                + "Date: " + new Date() + "\n"
                + "Server: " + WebServer.obj.appName + " v" + WebServer.obj.appVers + "\n");
        if (rbFile != null || rbString != null) //there exists a response body
        {
            httpMsg.append(
                    "MIME-version: 1.0\n"
                    + "Content-type: " + mimeType + "\n"
                    + "Content-length: " + rbLength + "\n");
        }
        //else
        //  httpMsg.append("MIME-version: 1.0\n" +
        //          "Content-type: text/html\n" +
        //          "\n");
        //          //"Content-length: "  + "\n");
        httpMsg.append("\n");  //a blank line needed anyway
    }

    /**
     *
     */
    public void sendResponse() {
        //response is 'httpMsg' + contents of 'rbFile' or 'rbString' (if any =! null).
        //'httpMsg' assumed to be not null
        try {
            //send HTTP header
            //PrintWriter out = new PrintWriter(sock.getOutputStream());
            //out.print(httpMsg);
            sock.getOutputStream().write(httpMsg.toString().getBytes());
            //PrintWriter out= new PrintWriter(new OutputStreamWriter(sock.getOutputStream(),"utf-8"),true);
            //System.out.println( httpMsg.toString() );
            //out.write( httpMsg.toString() );
            //sock.getOutputStream().write(httpMsg.toString().getBytes());
            //if there exists an HTTP body to be sent, send it
            if (rbFile != null) {
                /*
                 BufferedReader body = new BufferedReader(new InputStreamReader(rbFile));
                 int charRead;
                 while((charRead = body.read()) != -1) {
                 char ch = (char) charRead;
                 out.print(ch);
                 }
                 body.close();
                 */
                ///*
                DataInputStream in = new DataInputStream(rbFile);
                byte buffer[] = new byte[(int) rbLength];
                in.readFully(buffer);
                sock.getOutputStream().write(buffer);
                //*/
                rbFile.close();
            } else if (rbString != null) {
                StringReader body = new StringReader(rbString);
                int charRead;
                /*while((charRead = body.read()) != -1) {
                 char ch = (char) charRead;
                 out.print(ch);
                 }*/
                //sock.getOutputStream().write(rbString.getBytes("UTF-8"));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"), true);
                //System.out.println( rbString );
                out.write(rbString);
                out.close();
                body.close();
            }
            //flush output data and release class socket
            //out.close();
            sock.close();
        } catch (IOException e) {
            WebServer.win.log.error(port + "-Problem responding: " + e);
            WebServer.flog.writeln(port + "-Problem responding: " + e);
        }
        WebServer.win.log.debug(port + "-RESPONSE: HTTP header follows:\n" + httpMsg);
    }

    //response methods: utility methods
    /**
     *
     * @param path
     * @return
     */
    public FileInputStream streamResponseFile(String path) {
        //response body from a file, attaches stream to 'path'
        FileInputStream in;
        try {
            in = new FileInputStream(path);
        } catch (IOException e) {
            WebServer.win.log.error("Problem with response file stream: " + e);
            WebServer.flog.writeln("Problem with response file stream: " + e);
            in = null;
        }
        return in;
    }

    /**
     *
     * @param fileExt
     * @return
     */
    public String getMIMEType(String fileExt) {
        //set MIME with regard to file extension 'fileExt'.
        //'fileExt' must not be null
        if (fileExt == null) {
            return null;
        }
        String mm = null;
        mm = WebServer.mime.getProperty(fileExt);
        if (fileExt.compareTo("") == 0) //no extension
        {
            mm = WebServer.mime.getProperty(".");
        }
        if (mm == null) //no correspondence found
        {
            mm = WebServer.mime.getProperty("default");
        }
        return mm;
    }

    //various helper methods
    /**
     *
     */
    public void logRequest() {
        //log the client request
        String dateTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK).format(new Date());
        WebServer.win.log.report(dateTime + " - Request from " + clientAddr.toString() + " (" + port + ")");
        WebServer.flog.writeln("Request from " + clientAddr.toString() + " (" + port + ")");
    }
}
