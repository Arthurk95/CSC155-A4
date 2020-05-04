package a3.actions;

import a3.Starter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ControlFog extends AbstractAction {
    private Starter program;

    public ControlFog(Starter p){program = p;}
    @Override
    public void actionPerformed(ActionEvent e) {
        program.toggleFogControl();
    }
}
