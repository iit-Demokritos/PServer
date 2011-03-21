//===================================================================
// FileLog
//
// A general-purpose class.
//
// Manages a log file, writing messages about
// the progress of the application.
//===================================================================
package pserver.utilities;

import java.io.*;
import java.util.*;

import pserver.*;

public class FileLog {
    private String logPath;   //path of log file
    private long maxSize;     //log max size in bytes
    private boolean logOn = true;  //if false, no msgs at all are written
    
    //initializers
    public FileLog(String logPath, long maxSize) {
        this.logPath = logPath;
        this.maxSize = maxSize;
    }
    
    //misc methods
    public String getLogPath() {
        return logPath;
    }
    public long getMaxSize() {
        return maxSize;
    }
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }
    public long getSize() {
        File log = new File(logPath);
        return log.length();
    }
    public void setLog(boolean mode) {
        logOn = mode;
    }
    
    //write to log methods
    public void writeln(String line) {  //write line preceded by date / time
        if ( ! logOn) return;
        write(now() + "  >  " + line + "\n");
    }
    public void write(String msg) {  //plain write
        if ( ! logOn) return;
        boolean append = true;
        if (getSize() + msg.length() > maxSize) append = false;
        //if msg.length() > maxSize, msg will still be writen
        try {
            FileOutputStream out = new FileOutputStream(logPath, append);  //truncate?
            byte[] buf = msg.getBytes();
            out.write(buf);
            out.close();
        } catch (IOException e) {
            WebServer.win.log.warn("Problem writing file " + logPath + ": " + e);
        }
    }
    public void emptyLog() {
        try {
            FileOutputStream out = new FileOutputStream(logPath, false);  //truncate!!!
            out.close();
        } catch (IOException e) {
            WebServer.win.log.warn("Problem truncating file " + logPath + ": " + e);
        }
    }
    public void forceWriteln(String line) {  //writeln irrespective of 'logOn' value
        boolean old = logOn;
        logOn = true;
        writeln(line);
        logOn = old;
    }
    
    //utility methods
    private String now() {  //current date - time
        Calendar rightNow = Calendar.getInstance();
        return rightNow.get(Calendar.DAY_OF_MONTH) + "/" +
                (rightNow.get(Calendar.MONTH)+1) + "/" +  //months from 0 to 11
                rightNow.get(Calendar.YEAR) + " " +
                rightNow.get(Calendar.HOUR_OF_DAY) + ":" +
                rightNow.get(Calendar.MINUTE) + ":" +
                rightNow.get(Calendar.SECOND);
    }
}