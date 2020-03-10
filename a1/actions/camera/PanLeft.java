package a1.actions.camera;

import a1.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PanLeft extends AbstractAction {
    Camera camera;

    public PanLeft(Camera c){
        camera = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.panLeft();
    }
}
