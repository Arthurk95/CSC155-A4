package a1.actions.camera;

import a1.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PitchDown extends AbstractAction {
    Camera camera;

    public PitchDown(Camera c){
        camera = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.pitchDown();
    }
}
