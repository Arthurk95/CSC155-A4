package a2.actions.camera;

import a2.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MoveUp extends AbstractAction {
    Camera camera;

    public MoveUp(Camera c){
        camera = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.moveUp();
    }
}
