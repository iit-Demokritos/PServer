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

/**
 *
 * @author scify
 */
public class ControlWin {

    /**
     *
     */
    public ActivityLog log;   //displays application messages

    //initializer
    /**
     *
     * @param appTitle
     */
    public ControlWin(String appTitle) {
        log = new ActivityLog();
    }
}