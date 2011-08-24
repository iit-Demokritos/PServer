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

package pservertester;
import java.io.*;
import java.net.*;
import java.util.*;

//===================================================================
// PSClientRequest
//
// This class is intented to be part of applications that use the
// Personalization Server (PersServer). It delegates requests to
// PersServer through HTTP messages, and receives and parses the
// response. The class has the following main responsibilities:
// 1. Connect to the specified server at the specified port
// 2. Form and send an HTTP message with the specified request,
//    using the specified method (GET and POST HTTP methods are
//    supported)
// 3. Get the HTTP response message and detect possible errors
// 4. Extract the respose body (the actual response to the request)
//    in XML format, parse it, extract the pieces of data, and
//    insert those fields in a dynamically created two-dimensional
//    array
// The parameter 'timeout' is the maximum time in millisecs that
// this class will wait for reading the server response. If it is
// set to 0 the class will wait infinitely for the response.
//
// Note that response data are always arranged by the PersServer
// in columns and rows in the response XML document. Also note that
// the corresponding two-dimensional array is a perception of the
// class user, the actual implementation is a single dimensional
// Vector.
//
// USAGE: each request to PersServer must be an instance of
// PSClientRequest. At initialization, the object performs its main
// responsibilities. Then, the application can check for errors,
// get the number of rows and columns of the response data, and
// access those data as in a two-dimensional array.
//===================================================================
public class PSClientRequest {
    //raw response variables
    private StringBuffer response;  //the response body
    private int respCode;           //communication error code
    private String errMsg;          //error message while dispatching request
    //parsed response variables
    private int cols = 0;           //num of cols
    private int rows = 0;           //num of rows
    private Vector ra = null;       //response array
    private String userAttributes;  //pserver client attributes
    
    //initializers
    public PSClientRequest(InetAddress dest, int port, String request, boolean methodPost, int timeout) {
        //make request and get response
        response = new StringBuffer();  //initialization important
        respCode=responseTomcat(dest, port, request, methodPost, response, timeout);
        if(respCode==200)
            parseResponse();     //if error 'ra' remains null
    }    
    //public methods
    public void execute(InetAddress dest, int port, String request, boolean methodPost, int timeout){
        response = new StringBuffer();  //initialization important
        respCode=responseTomcat(dest, port, request, methodPost, response, timeout);
        if(respCode==200)
            parseResponse();     //if error 'ra' remains null
    }    
    
    public int getRows() {
        //returns num of rows of parsed response
        return rows;
    }    
    
    public int getColumns() {
        //returns num of columns of parsed response
        return cols;
    }    
    public String getValue(int row, int col) {
        //get the response value at [row, col]
        //returns null if error has occured, or
        //if [row, col] out of array boundaries
        if (ra == null) return null;
        if (row >= rows || col >= cols) return null;
        return ((String) ra.elementAt(row*cols + col));
    }
    public String getResponse() {
        //returns the unparsed response body in XML format
        return response.substring(0);
    }
    public boolean isError() {
        //returns true if some error has occured
        if (respCode == 200) return false;
        return true;
    }
    public String getErrorMessage() {
        //returns an error message or empty string if no error
        String msg;
        if (respCode < 0) msg = errMsg;  //error while dispatching request
        else if (respCode >= 400 && respCode < 500) msg = "Client request error";
        else if (respCode >= 500) msg = "Server internal error";
        else msg = "";
        return msg;
    }
    public int getResponseCode() {
        //if < 0 local error occured:
        //       -4 MalformedURL
        //       -3 unable to establish connection
        //       -2 timeout elapsed waiting response
        //if > 0 server error code is conveyed:
        //       if > 500 server internal error
        //       if > 400 client request error
        //       if = 200 everything OK
        return respCode;
    }
    
    
    //private methods
    private void parseResponse() {
        //determines number of rows and columns of XML response
        //and populates an array with the individual response values
        int idx;
        int from, to;
        
        String resp = response.substring(0);
        
        //find num of rows
        idx = 0;
        while ((idx = resp.indexOf("<row>", idx)) != -1) {
            idx++;
            rows++;
        }
        if (rows == 0) return;  //response empty
        //get a single row (the first one)
        from = resp.indexOf("<row>", 0);
        to = resp.indexOf("</row>", 0);
        from += 5;  //pointer after tag
        String aRow = resp.substring(from, to);
        //find num of cols
        idx = 0;
        while ((idx = aRow.indexOf("<", idx)) != -1) {
            idx++;
            cols++;
        }
        if (cols % 2 != 0) {  //unlikely
            cols = 0;
            return;
        }
        cols = cols / 2;
        //create and populate response array
        ra = new Vector(rows * cols);
        to = 0;
        for (int i=0; i < rows; i++) {
            //get a single row
            from = resp.indexOf("<row>", to);
            to = resp.indexOf("</row>", from);
            if (from < 0 || to < 0) return;  //unlikely
            from += 5;  //pointer after tag
            String currRow = resp.substring(from, to);
            StringTokenizer parser = new StringTokenizer(currRow, "<>", true);
            for (int j=0; j < cols; j++) {
                //get a single col value
                //assumes that values do not contain char '<'
                String value;
                parser.nextToken("<");
                parser.nextToken(">");  //element name
                parser.nextToken(">");
                String tmp = parser.nextToken("<");  //value, or '<' if empty value
                value = tmp.equals("<")? "": tmp;
                if ( ! tmp.equals("<")) parser.nextToken("<");
                parser.nextToken(">");  //element name (end tag)
                parser.nextToken(">");
                //insert value in response array
                ra.add(i*cols + j, value);
            }
        }
    }
    //---------------------------------------------------------------------
    //A general-purpose method that connects to a server running at
    //'dest:port', sends a request and gets a response and a status code.
    //Parameter 'request' must contain the requested resource path, plus
    //the query string, if there exists one (as in method GET format).
    //If 'methodPost' is true, the HTTP 'POST' method is used for the
    //request, otherwise if it is false the HTTP 'GET' method is used.
    //When the method is called, 'response' must not be null.
    //Negative return numbers indicate method failure, in that case an
    //explanatory message may be in 'response'. Positive return numbers
    //correspond to server HTTP codes: 200 is OK, 4?? is request error,
    //5?? is server error. If connection with server cannot be established
    //-3 is returned. If 200 is returned (success), 'response' contains
    //the HTTP response body (does not include the header). The parameter
    //'timeout' gives the max time in millisecs for reading the response.
    //If 'timeout' is set to 0, client waits infinitely for response.
    //---------------------------------------------------------------------
    int dispatchServer(InetAddress dest, int port, String request,
            boolean methodPost, StringBuffer response, int timeout) {
        //a maximum size for the full request
        int methodGetMaxSize = 250;
        int methodPostMaxSize = 32000;
        //check parameters
        if (request == null || response == null)
            return -1;  //caller error
        //construct GET or POST full request
        //(if POST, query string goes to body of request)
        String fullRequest;
        if (methodPost) {  //method is POST
            String resource;
            String queryStr;
            int qIdx = request.indexOf('?');  //start of query string
            if (qIdx == -1) {  //no query string
                resource = request;
                queryStr = "";
            } else {  //'?' present
                resource = request.substring(0, qIdx);
                queryStr = request.substring(qIdx + 1);  //exclude '?'
            }
            fullRequest = "POST " + resource + " HTTP/1.1\nHost: "
                    + dest.getHostName() + ":" + (new Integer(port)).toString()
                    + "\n\n"     //empty line marks start of request body
                    + queryStr;  //query str goes in request body
        } else {  //method is GET
            fullRequest = "GET " + request + " HTTP/1.1\nHost: "
                    + dest.getHostName() + ":" + (new Integer(port)).toString()
                    + "\n\n";
        }
        //check size of full request against maximum size
        if (methodPost && fullRequest.length() > methodPostMaxSize) {
            response.setLength(0);
            response.append("Complete POST request longer than maximum of " + methodPostMaxSize);
            return -5;  //method error
        } else if (( ! methodPost) && fullRequest.length() > methodGetMaxSize) {
            response.setLength(0);
            response.append("Complete GET request longer than maximum of " + methodGetMaxSize);
            return -6;  //method error
        }
        //make the connection
   /*     try {
            //connect to server
            Socket client = new Socket(dest, port);
            //send the request
            byte[] requestBytes = fullRequest.substring(0).getBytes();
            OutputStream out = client.getOutputStream();
            out.write(requestBytes, 0, requestBytes.length);
            out.flush();
            //get the response
            InputStream in = client.getInputStream();
            client.setSoTimeout(timeout);    //timeout in millisecs for blocking
            response.setLength(0);  //clear first
            //cannot rely on input stream for returning -1
            //when the end of the character sequence is reached!
            int total = in.available();
            byte[] firstByte = null;
            if (total == 0) {  //block until input avalaible, 0 not acceptable length
                firstByte = new byte[1];
                in.read(firstByte);    //wait for first byte, or until 'timeout' expires
                total = in.available() + 1;
            }
            byte[] reqBytes = new byte[total];
            if (firstByte != null) reqBytes[0] = firstByte[0];
            int pos = (firstByte != null)? 1: 0;  //start position of next copy
            int remain = (firstByte != null)? total-1: total;  //bytes remaining to read
            int bufSize = 512;  //size of chunks to read
            int step = Math.min(bufSize, remain);
            while (pos < total) {
                int read = in.read(reqBytes, pos, step);
                if (read == -1) {  //not expected
                    response.setLength(0);
                    response.append("Problem while receiving");
                    return -7;  //method error
                }
                pos += read;
                remain -= read;
                step = Math.min(bufSize, remain);
            }
            response.append(new String(reqBytes));
    
            //disconnect
            client.close();
        } catch (InterruptedIOException e) {  //timeout elapsed
            response.setLength(0);
            response.append(e.toString());
            return -2;  //method error
        } catch (IOException e) {  //connection not established
            response.setLength(0);
            response.append(e.toString());
            return -3;  //method error
        }
    */
        //get response code (second token in response)
        int from = 0;
        while (from < response.length() &&
                ! Character.isWhitespace(response.charAt(from)))
            from += 1;  //find first space before code
        while (from < response.length() &&
                Character.isWhitespace(response.charAt(from)))
            from += 1;  //find first character of code
        int to = from;  //not 'from+1', may go beyond 'response.length' (in erroneous responses)
        while (to < response.length() &&
                ! Character.isWhitespace(response.charAt(to)))
            to += 1;  //find first space after code
        String theCode = response.substring(from, to);  //'to' excluded
        int respCode;
        try {
            respCode = Integer.parseInt(theCode);
        } catch(NumberFormatException e) {
            response.setLength(0);
            response.append(e.toString());
            return -4;  //method error
        }
        
        //get response body (after an empty line)
        boolean emptyLine = false;
        boolean justSpaces = false;  //true if only spaces in line so far
        int i = 0;
        while (i < response.length() && ! emptyLine) {
            char ch = response.charAt(i);
            if (ch == '\n' && ! justSpaces) justSpaces = true;
            else if ( ! Character.isWhitespace(ch) && justSpaces)
                justSpaces = false;
            else if (ch == '\n' && justSpaces) emptyLine = true;
            i += 1;
            
        }  //'i' now points to end, or to first char after empty line
        response.delete(0, i);
        return respCode;
    }
    
    
    
    
    int responseTomcat(InetAddress dest, int port, String request,
            boolean methodPost, StringBuffer response, int timeout) {
        //a maximum size for the full request
        int methodGetMaxSize = 250;
        int methodPostMaxSize = 32000;
        //check parameters
        if (request == null || response == null)
            return -1;  //caller error
        //construct GET or POST full request
        //(if POST, query string goes to body of request)
        String fullRequest;
        if (methodPost) {  //method is POST
            String resource;
            String queryStr;
            int qIdx = request.indexOf('?');  //start of query string
            if (qIdx == -1) {  //no query string
                resource = request;
                queryStr = "";
            } else {  //'?' present
                resource = request.substring(0, qIdx);
                queryStr = request.substring(qIdx + 1);  //exclude '?'
            }
            fullRequest = "POST " + resource + " HTTP/1.1\nHost: "
                    + dest.getHostName() + ":" + (new Integer(port)).toString()
                    + "\n\n"     //empty line marks start of request body
                    + queryStr;  //query str goes in request body
        } else {  //method is GET
            fullRequest = "GET " + request + " HTTP/1.1\nHost: "
                    + dest.getHostName() + ":" + (new Integer(port)).toString()
                    + "\n\n";
        }
        //check size of full request against maximum size
        if (methodPost && fullRequest.length() > methodPostMaxSize) {
            response.setLength(0);
            response.append("Complete POST request longer than maximum of " + methodPostMaxSize);
            return -5;  //method error
        } else if (( ! methodPost) && fullRequest.length() > methodGetMaxSize) {
            response.setLength(0);
            response.append("Complete GET request longer than maximum of " + methodGetMaxSize);
            return -6;  //method error
        }
        
        //Make the connection
        String inputLine = "";
        
        request = "http://" + dest.getHostName() +":" +(new Integer(port).toString())+request;
        //      request = "http://127.0.0.1:1111".concat(request);
        try {
            URL urlAddress      = new URL(request);
            URLConnection urlC  = urlAddress.openConnection();
            BufferedReader in   = new BufferedReader(
                    new InputStreamReader(
                    urlC.getInputStream(),"UTF-8"));
            while ((inputLine = in.readLine()) != null){                
                response = response.append(inputLine).append("\n") ;
            }
            
        } catch (MalformedURLException e) {
            //System.out.println("MalformedURLException");
            return -4;
        } catch (IOException e) {
            //System.out.println("IOException");
            return -3;
        }
        
        return 200;
    }
    
    
    
}