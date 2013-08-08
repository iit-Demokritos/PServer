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
// Border
//
// A general-purpose class.
//
// Class that creates a custom border around user interface elements.
//===================================================================
package pserver.utilities;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author scify
 */
public class Border extends Panel {
    int top, left, bottom, right;  //no of pixels for border
    
    /**
     *
     * @param borderMe
     * @param top
     * @param left
     * @param bottom
     * @param right
     */
    public Border(Component borderMe, int top, int left, int bottom, int right) {
        this.top    = top;
        this.left   = left;
        this.bottom = bottom;
        this.right  = right;
        setLayout(new BorderLayout());
        add(borderMe, "Center");
    }
    /**
     *
     * @return
     */
    public Insets getInsets() {  //overriding the insets
        return new Insets(top, left, bottom, right);
    }
}

