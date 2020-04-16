package a3;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.text.DecimalFormat;

public class Camera {
    private Vector4f cVector;

    private final float MOVE_INTERVAL = 0.5f;
    private final float TURN_ANGLE = 0.1f;
    private DecimalFormat format = new DecimalFormat("###,###.##");
    private Vector4f startingPos;
    private Vector4f uVector = new Vector4f(1.0f, 0.0f, 0.0f, 0.0f);
    private Vector4f vVector = new Vector4f(0.0f, 1.0f, 0.0f, 0.0f);
    private Vector4f nVector = new Vector4f(0.0f, 0.0f, 1.0f, 0.0f);

    private Matrix4f viewMatrix;

    public Camera(float x, float y, float z){
        startingPos = new Vector4f(x, y, z, 1.0f);
        cVector = new Vector4f(x, y, z, 1.0f);
        viewMatrix = new Matrix4f();
        updateView();
    }



    public void updateView() {
        Matrix4f rMat = new Matrix4f(
                uVector.x, vVector.x, nVector.x, 0.0f,
                uVector.y, vVector.y, nVector.y, 0.0f,
                uVector.z, vVector.z, nVector.z, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        );

        Matrix4f tMat = new Matrix4f(
          1.0f, 0.0f, 0.0f,0.0f,
          0.0f, 1.0f, 0.0f, 0.0f,
          0.0f, 0.0f, 1.0f, 0.0f,
                -cVector.x, -cVector.y, -cVector.z, 1.0f
        );
        viewMatrix.identity();
        viewMatrix.mul(rMat);
        viewMatrix.mul(tMat);
    }

    public void moveBackward(){
        cVector.add(nVector); updateView();}
    public void moveForward(){
        cVector.sub(nVector); updateView();}

    public void moveRight(){ cVector.add(uVector); updateView();}
    public void moveLeft(){ cVector.sub(uVector); updateView(); }

    public void moveUp(){  cVector.add(vVector); updateView();}
    public void moveDown(){ cVector.sub(vVector); updateView();}

    public void pitchUp(){
        nVector.rotateAbout(TURN_ANGLE, uVector.x, uVector.y, uVector.z);
        vVector.rotateAbout(TURN_ANGLE, uVector.x, uVector.y, uVector.z);
        updateView();
    }

    public void pitchDown(){
        nVector.rotateAbout(TURN_ANGLE, -uVector.x, -uVector.y, -uVector.z);
        vVector.rotateAbout(TURN_ANGLE, -uVector.x, -uVector.y, -uVector.z);
        updateView();
    }

    public void panRight(){
        nVector.rotateAbout(TURN_ANGLE, vVector.x, vVector.y, vVector.z);
        uVector.rotateAbout(TURN_ANGLE, vVector.x, vVector.y, vVector.z);
        updateView();
    }
    public void panLeft(){
        nVector.rotateAbout(TURN_ANGLE, -(vVector.x), -(vVector.y), -(vVector.z));
        uVector.rotateAbout(TURN_ANGLE, -(vVector.x), -(vVector.y), -(vVector.z));
        updateView();
    }

    public void resetCamera(){
        cVector.set(startingPos);
        uVector = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
        vVector = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
        nVector = new Vector4f(0.0f, 0.0f, 1.0f, 1.0f);
        viewMatrix = new Matrix4f();
        updateView();
    }

    public Vector4f getLoc() {return cVector;}
    public Matrix4f getView(){ return viewMatrix;}

}
