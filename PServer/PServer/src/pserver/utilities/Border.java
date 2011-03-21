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

public class Border extends Panel {
    int top, left, bottom, right;  //no of pixels for border
    
    public Border(Component borderMe, int top, int left, int bottom, int right) {
        this.top    = top;
        this.left   = left;
        this.bottom = bottom;
        this.right  = right;
        setLayout(new BorderLayout());
        add(borderMe, "Center");
    }
    public Insets getInsets() {  //overriding the insets
        return new Insets(top, left, bottom, right);
    }
}

