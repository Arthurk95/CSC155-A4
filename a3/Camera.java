package a3;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.text.DecimalFormat;

public class Camera {
    private Vector4f cVector;

    private Vector4f pc = new Vector4f();
    private final float MOVE_INTERVAL = 0.5f;
    private DecimalFormat format = new DecimalFormat("###,###.##");
    private Vector4f uVector = new Vector4f(MOVE_INTERVAL, 0.0f, 0.0f, 1.0f);
    private Vector4f vVector = new Vector4f(0.0f, MOVE_INTERVAL, 0.0f, 1.0f);
    private Vector4f nVector = new Vector4f(0.0f, 0.0f, MOVE_INTERVAL, 1.0f);

    private Matrix4f viewMatrix;

    public Camera(float x, float y, float z){
        cVector = new Vector4f(x, y, z, 1.0f);
        viewMatrix = new Matrix4f();
        updateView();
    }



    public void updateView() {
        Matrix4f rMat = new Matrix4f(
                uVector.x, uVector.y, uVector.z, 0.0f,
                vVector.x, vVector.y, vVector.z, 0.0f,
                nVector.x, nVector.y, nVector.z, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        );

        Matrix4f tMat = new Matrix4f(
          1.0f, 0.0f, 0.0f,0.0f,
          0.0f, 1.0f, 0.0f, 0.0f,
          0.0f, 0.0f, 1.0f, 0.0f,
                -cVector.x, -cVector.y, -cVector.z, 1.0f
        );
        tMat.mul(rMat, viewMatrix); // store rMat * tMat in viewMatrix
        cVector.mul(viewMatrix, pc); // store cVector * viewMatrix in pc
        System.out.println(viewMatrix.toString(format));
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
        nVector.rotateAbout(0.01f, -uVector.x, -uVector.y, -uVector.z);
        vVector.rotateAbout(0.01f, uVector.x, uVector.y, -uVector.z);
        updateView();
    }

    public void pitchDown(){
        nVector.rotateAbout(0.01f, uVector.x, uVector.y, uVector.z);
        vVector.rotateAbout(0.01f, -uVector.x, -uVector.y, -uVector.z);
        updateView();
    }

    public void panRight(){
        nVector.rotateAbout(0.01f, vVector.x, vVector.y, vVector.z);
        uVector.rotateAbout(0.01f, vVector.x, vVector.y, vVector.z);
        updateView();
    }
    public void panLeft(){
        nVector.rotateAbout(0.01f, -(vVector.x), -(vVector.y), -(vVector.z));
        uVector.rotateAbout(0.01f, -(vVector.x), -(vVector.y), -(vVector.z));
        updateView();
    }

    public void resetCamera(){
        cVector = new Vector4f(0.0f, 0.0f, 10.0f, 0.0f);
        uVector = new Vector4f(MOVE_INTERVAL, 0.0f, 0.0f, 1.0f);
        vVector = new Vector4f(0.0f, MOVE_INTERVAL, 0.0f, 1.0f);
        nVector = new Vector4f(0.0f, 0.0f, MOVE_INTERVAL, 1.0f);
        viewMatrix = new Matrix4f();
        updateView();
    }

    public Vector4f getLoc(){ return pc;}
    public Matrix4f getView(){ return viewMatrix;}
}
