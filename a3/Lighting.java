package a3;

import a3.material.Material;
import a3.sceneobject.SceneObject;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Lighting {
    private SceneObject lightObject;

    // white light properties
    private float[] globalAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
    private float[] lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
    private float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

    private Vector3f initialLightLoc = new Vector3f(3.0f, 8.0f, 0.0f);
    private Vector3f currentLightPos = new Vector3f();

    private float amt = 0.0f;
    private float[] lightPos = new float[3];

    public Lighting(SceneObject o){
        lightObject = o;
        lightObject.setupVBO();
        lightObject.setScale(0.3f);
    }

    public void installLights(int renderingProgram, Matrix4f vMatrix, Material objMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        currentLightPos.set(initialLightLoc);
        amt += 0.01f;
        currentLightPos.rotateAxis((float)Math.toRadians(amt), 0.1f, 1.0f, 0.1f);

        //currentLightPos.mulPosition(vMatrix);
        lightPos[0]=currentLightPos.x(); lightPos[1]=currentLightPos.y(); lightPos[2]=currentLightPos.z();

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


        lightObject.setPosition(lightPos);
    }



    public SceneObject getLightObject(){return lightObject;}

    public Vector3f getLightPos(){return new Vector3f(lightPos[0], lightPos[1], lightPos[2]);}
}
