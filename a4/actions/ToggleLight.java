package a4.actions;

import a4.Starter;

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
