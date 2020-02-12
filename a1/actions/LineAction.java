package a1.actions;

import a1.Starter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LineAction extends AbstractAction {
    public static Starter program;

    public LineAction(Starter s){
        LineAction.program = s;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        program.setDrawMode(0);
    }
}
