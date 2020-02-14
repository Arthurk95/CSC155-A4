package a1.actions;

import a1.Starter;

import javax.swing.*;
import java.awt.event.ActionEvent;

// Action that sets the draw mode to circle for the GL Object
public class CircleAction extends AbstractAction {
    public static Starter program;

    public CircleAction(Starter s){
        CircleAction.program = s;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        program.setDrawMode(1);
    }
}
