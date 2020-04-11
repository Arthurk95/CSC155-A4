package a3.sceneobject;

import a3.ObjectReader;
import a3.ShaderTools;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

public abstract class SceneObject {

    ObjectReader model;

    // SceneObjects default to silver material
    private float[] matAmb = ShaderTools.silverAmbient();
    private float[] matDif = ShaderTools.silverDiffuse();
    private float[] matSpe = ShaderTools.silverSpecular();
    private float matShi = ShaderTools.silverShininess();

    public float[] getAmb(){return matAmb;}
    public float[] getDif(){return matDif;}
    public float[] getSpe(){return matSpe;}
    private int renderingProgram;

    public SceneObject(){}
    public SceneObject(int r, ObjectReader m){
        renderingProgram = r;
        model = m;
    }

    public SceneObject(int r){renderingProgram = r;}

    public void setLighting(){
        GL4 gl = (GL4) GLContext.getCurrentGL();

        int mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
        int mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
        int mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
        int mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");

        gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
        gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
        gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
        gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
    }

}
