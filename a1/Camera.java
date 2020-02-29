package a1;

import org.joml.Vector3f;

public class Camera {
    private Vector3f location = new Vector3f(0.0f, 0.0f, 8.0f);
    private Vector3f uVector = new Vector3f(1.0f, 0.0f, 0.0f);
    private Vector3f vVector = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f nVector = new Vector3f(0.0f, 0.0f, 1.0f);

    private void moveForward(){ location.add(nVector); }
    private void moveBackward(){ location.sub(nVector); }

    private void moveLeft(){ location.sub(uVector); }
    private void moveRight(){ location.add(uVector); }

    private void moveUp(){ location.add(vVector); }
    private void moveDown(){ location.sub(vVector); }

}
