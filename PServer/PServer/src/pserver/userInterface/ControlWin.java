//===================================================================
// ControlWin
//
// Main window of the application.
//===================================================================
package pserver.userInterface;

import javax.swing.*;
import java.awt.*;

import pserver.*;
import pserver.utilities.*;

public class ControlWin {
    public ActivityLog log;   //displays application messages
    
    //initializer
    public ControlWin(String appTitle) {
        log=new ActivityLog();
    }

}