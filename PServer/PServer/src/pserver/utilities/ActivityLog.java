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

/**
 *
 * @author scify
 */
public class ActivityLog {

    private boolean logOn = true;    //if false, no msgs at all are displayed
    private boolean debugOn = true;  //if false, debug msgs are not displayed

    /**
     *
     */
    public ActivityLog() {
    }

    //activity reporting methods - they all change line afterwards
    /**
     *
     * @param msg
     */
    public void report(String msg) {
        echo("-- " + msg);
    }

    /**
     *
     * @param msg
     */
    public void warn(String msg) {
        echo(" ! " + msg);
    }

    /**
     *
     * @param msg
     */
    public void error(String msg) {
        echo(" # " + msg);
    }

    /**
     *
     * @param msg
     */
    public void debug(String msg) {
        if (debugOn) {
            echo("*>" + msg + "<*");
        }
    }

    /**
     *
     */
    public void markStart() {
        echo("*** STARTED at " + now() + " ***");
    }

    /**
     *
     */
    public void markSuccess() {
        echo("*** COMPLETED at " + now() + " ***");
        echo("");  //another new line
    }

    /**
     *
     */
    public void markFailure() {
        echo("### ABORDED at " + now() + " ###");
        echo("");  //another new line
    }

    /**
     *
     * @param msg
     */
    public void echo(String msg) {  //displays msg exactly as it is, plus newline
        //System.out.print('\r');
        for (int i = 0; i < "Type quit to terminate the server".length(); i++) {
            System.out.print("\b");
        }
        for (int i = 0; i < "Type quit to terminate the server".length(); i++) {
            System.out.print(" ");
        }
        for (int i = 0; i < "Type quit to terminate the server".length(); i++) {
            System.out.print("\b");
        }
        //System.out.print('\r');
        if (logOn) {
            System.out.println(msg);
        }
        System.out.print("Type quit to terminate the server");
    }

    /**
     *
     * @param msg
     */
    public void forceReport(String msg) {  //report irrespective of 'logOn' value
        boolean old = logOn;
        logOn = true;
        report(msg);
        logOn = old;
    }

    /**
     *
     * @param mode
     */
    public void setLog(boolean mode) {
        logOn = mode;
    }

    /**
     *
     * @param mode
     */
    public void setDebug(boolean mode) {
        debugOn = mode;
    }

    //utility methods
    private String now() {  //current date - time
        Calendar rightNow = Calendar.getInstance();
        return rightNow.get(Calendar.DAY_OF_MONTH) + "/"
                + (rightNow.get(Calendar.MONTH) + 1) + "/" + //months from 0 to 11
                rightNow.get(Calendar.YEAR) + " "
                + rightNow.get(Calendar.HOUR_OF_DAY) + ":"
                + rightNow.get(Calendar.MINUTE) + ":"
                + rightNow.get(Calendar.SECOND);
    }
}
