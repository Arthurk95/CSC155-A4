package a2.actions.camera;

import a2.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MoveRight extends AbstractAction {
    Camera camera;

    public MoveRight(Camera c){
        camera = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.moveRight();
    }
}
