package a2.actions;

import a2.Starter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleAxes extends AbstractAction {
    private Starter program;

    public ToggleAxes(Starter p){
        this.program = p;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        program.toggleAxes();
    }
}
