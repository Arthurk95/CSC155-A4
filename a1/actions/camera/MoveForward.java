package a1.actions.camera;

import a1.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MoveForward extends AbstractAction {
    Camera camera;

    public MoveForward(Camera c){
        camera = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.moveForward();
    }
}
