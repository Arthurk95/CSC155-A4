package a1.actions;

import a1.Starter;

import javax.swing.*;
import java.awt.event.ActionEvent;

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
