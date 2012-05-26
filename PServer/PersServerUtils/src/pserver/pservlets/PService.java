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

package pserver.pservlets;


import pserver.data.VectorMap;
import pserver.data.DBAccess;


public interface PService {
    
    public static final String htm = "text/html";
    public static final String html = "text/html";
    public static final String txt = "text/plain";
    public static final String xml = "text/xml";
    // Image MIME types";
    public static final String gif = "image/gif";
    public static final String jpe = "image/jpeg";
    public static final String jpeg = "image/jpeg";
    public static final String jpg = "image/jpeg";
    
    //  Video MIME types
    public static final String avi = "video/msvideo";
    public static final String mov = "video/quicktime";
    public static final String mpe = "video/mpeg";
    public static final String mpeg = "video/mpeg";
    public static final String mpg = "video/mpeg";
    public static final String qt = "video/quicktime";
    
    //  Audio MIME types
    public static final String au = "audio/basic";
    public static final String snd = "audio/basic";
    public static final String wav = "audio/wav";
    public static final String mid = "audio/midi";
    public static final String mp3 = "audio/mpeg";
    public static final String m3u = "audio/mpegurl";
    
    //  Application MIME types
    public static final String bin = "application/octet-stream";
    public static final String doc = "application/msword";
    public static final String dvi = "application/x-dvi";
    public static final String eps = "application/postscript";
    public static final String gtar = "application/x-gtar";
    public static final String gz = "application/x-gzip";
    public static final String jar = "application/java-archive";
    public static final String js = "application/x-javascript";
    public static final String latex = "application/x-latex";
    public static final String ltx = "application/x-latex";
    public static final String pdf = "application/pdf";
    public static final String ps = "application/postscript";
    public static final String rtf = "application/rtf";
    public static final String tar = "application/tar";
    public static final String tex = "application/x-tex";
    public static final String tgz = "application/x-gzip";
    public static final String tr =  "application/x-troff";
    public static final String zip = "application/zip";
    
    public abstract String getMimeType();
    //this method will be called when the PServlet will be loaded
    public abstract void init( String[] params ) throws Exception;
    //the method is the job that the servlet must do
    public abstract int service( VectorMap parameters, StringBuffer response, DBAccess dbAccess );
}
