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

/**
 *
 * @author scify
 */
public class FileLog {

    private String logPath;   //path of log file
    private long maxSize;     //log max size in bytes
    private boolean logOn = true;  //if false, no msgs at all are written

    //initializers
    /**
     *
     * @param logPath
     * @param maxSize
     */
    public FileLog(String logPath, long maxSize) {
        this.logPath = logPath;
        this.maxSize = maxSize;
    }

    //misc methods
    /**
     *
     * @return
     */
    public String getLogPath() {
        return logPath;
    }

    /**
     *
     * @return
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     *
     * @param maxSize
     */
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    /**
     *
     * @return
     */
    public long getSize() {
        File log = new File(logPath);
        return log.length();
    }

    /**
     *
     * @param mode
     */
    public void setLog(boolean mode) {
        logOn = mode;
    }

    //write to log methods
    /**
     *
     * @param line
     */
    public void writeln(String line) {  //write line preceded by date / time
        if (!logOn) {
            return;
        }
        write(now() + "  >  " + line + "\n");
    }

    /**
     *
     * @param msg
     */
    public void write(String msg) {  //plain write
        if (!logOn) {
            return;
        }
        boolean append = true;
        if (getSize() + msg.length() > maxSize) {
            append = false;
        }
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

    /**
     *
     */
    public void emptyLog() {
        try {
            FileOutputStream out = new FileOutputStream(logPath, false);  //truncate!!!
            out.close();
        } catch (IOException e) {
            WebServer.win.log.warn("Problem truncating file " + logPath + ": " + e);
        }
    }

    /**
     *
     * @param line
     */
    public void forceWriteln(String line) {  //writeln irrespective of 'logOn' value
        boolean old = logOn;
        logOn = true;
        writeln(line);
        logOn = old;
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