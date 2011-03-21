
//===================================================================
// ActivityLog
//
// A general-purpose class.
//
// A scrollable text area component that displays messages about
// the progress of the application.
//===================================================================
package pserver.utilities;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class ActivityLog {
    private boolean logOn = true;    //if false, no msgs at all are displayed
    private boolean debugOn = true;  //if false, debug msgs are not displayed
    
    public ActivityLog() {
    }
    
    //activity reporting methods - they all change line afterwards
    public void report(String msg) {
        echo("-- " + msg);
    }
    public void warn(String msg) {
        echo(" ! " + msg);
    }
    public void error(String msg) {
        echo(" # " + msg);
    }
    public void debug(String msg) {
        if (debugOn) echo("*>" + msg + "<*");
    }
    public void markStart() {
        echo("*** STARTED at " + now() + " ***");
    }
    public void markSuccess() {
        echo("*** COMPLETED at " + now() + " ***");
        echo("");  //another new line
    }
    public void markFailure() {
        echo("### ABORDED at " + now() + " ###");
        echo("");  //another new line
    }
    public void echo(String msg) {  //displays msg exactly as it is, plus newline
        //System.out.print('\r');
        for(int i=0;i<"Type quit to terminate the server".length();i++)
            System.out.print("\b");
        for(int i=0;i<"Type quit to terminate the server".length();i++)
            System.out.print(" ");
        for(int i=0;i<"Type quit to terminate the server".length();i++)
            System.out.print("\b");
        //System.out.print('\r');
        if ( logOn )
            System.out.println(msg);
        System.out.print("Type quit to terminate the server");
    }

    public void forceReport(String msg) {  //report irrespective of 'logOn' value
        boolean old = logOn;
        logOn = true;
        report(msg);
        logOn = old;
    }
    
    public void setLog(boolean mode) {
        logOn = mode;
    }
    public void setDebug(boolean mode) {
        debugOn = mode;
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
