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
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
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
