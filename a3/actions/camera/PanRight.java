package a3.actions.camera;

import a3.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PanRight extends AbstractAction {
    Camera camera;

    public PanRight(Camera c){
        camera = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.panRight();
    }
}