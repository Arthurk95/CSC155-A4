package a4.actions.camera;

import a4.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MoveBackward extends AbstractAction {
    Camera camera;

    public MoveBackward(Camera c){
        camera = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.moveBackward();
    }
}
