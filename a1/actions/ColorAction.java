package a1.actions;

import a1.Starter;

import javax.swing.*;
import java.awt.event.ActionEvent;

// Action that changes the color of the GL shader
public class ColorAction extends AbstractAction {
    public static Starter program;

    public ColorAction(Starter s){
        ColorAction.program = s;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
            program.toggleColorMode();
        }
}
