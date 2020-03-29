package a3.actions.camera;

import a3.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ResetCamera extends AbstractAction {
    Camera camera;

    public ResetCamera(Camera c){
        camera = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.resetCamera();
    }
}
