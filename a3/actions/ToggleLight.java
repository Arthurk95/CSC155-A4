package a3.actions;

import a3.Starter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleLight extends AbstractAction {
    private Starter program;

    public ToggleLight(Starter p) {
        this.program = p;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        program.toggleMobileLight();
    }
}
