package a4.actions;

import a4.Starter;

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
