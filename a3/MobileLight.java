package a3;

import a3.material.Material;
import a3.sceneobject.SceneObject;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;

public class MobileLight extends Light implements MouseMotionListener {
    private float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
    private float[] lightAmbient = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
    private float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private boolean listening = false;
    private SceneObject lightObject;
    private Matrix4f viewMatrix;
    private float[] lightPos = new float[3];
    private DecimalFormat format = new DecimalFormat("###,###.##");

    public MobileLight(Matrix4f v, SceneObject o){
        viewMatrix = v;
        lightObject = o;
        lightObject.setupVBO();
        lightObject.setScale(0.5f);
        super.setSceneObject(lightObject);
    }

    public void toggleMobileLight(){
        listening = !listening;
    }

    @Override
    public void installLights(int renderingProgram, Matrix4f vMatrix, Material objMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // get the locations of the light and material fields in the shader
        int globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
        int ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
        int diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
        int specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
        int posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
        int mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
        int mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
        int mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
        int mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");

        //  set the uniform light and material values in the shader
        gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
        gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);
        gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
        gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
        gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
        gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, objMat.getAmbient(), 0);
        gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, objMat.getDiffuse(), 0);
        gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, objMat.getSpecular(), 0);
        gl.glProgramUniform1f(renderingProgram, mshiLoc, objMat.getShine());


        lightObject.setPosition(new Vector4f(lightPos[0], lightPos[1], lightPos[2], 1.0f));
    }
    @Override
    public SceneObject getLightObject(){return lightObject;}

    @Override
    public Vector3f getLightPos(){return new Vector3f(lightPos[0], lightPos[1], lightPos[2]);}

    public boolean listening(){return listening;}
    public void setViewMatrix(Matrix4f v){viewMatrix = v;}

    @Override
    public void mouseDragged(MouseEvent e) {
        if(listening){
            Vector4f v = new Vector4f(e.getX(), e.getY(), 0.0f, 1.0f);
            v.mul(viewMatrix);
            v.normalize();
            v.mul(5.0f);
            lightPos[0] = v.x; lightPos[1] = v.y; lightPos[2] = v.z;
        }
    }



    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
