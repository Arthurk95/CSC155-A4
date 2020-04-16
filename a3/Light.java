package a3;

import a3.material.Material;
import a3.sceneobject.SceneObject;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Light {
    private SceneObject lightObject;

    // white light properties
    private float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
    private float[] lightAmbient = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
    private float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

    private Vector3f initialLightLoc = new Vector3f(-10.0f, 6.0f, 5.0f);
    private Vector3f currentLightPos = new Vector3f();

    private float[] lightPos = new float[3];

    public Light(){}

    public Light(SceneObject o){
        lightObject = o;
        lightObject.setupVBO();
        lightObject.setScale(0.5f);

        lightPos[0]=initialLightLoc.x();
        lightPos[1]=initialLightLoc.y();
        lightPos[2]=initialLightLoc.z();
    }

    public void installLights(int renderingProgram, Matrix4f vMatrix, Material objMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        currentLightPos.set(initialLightLoc);

        currentLightPos.mulPosition(vMatrix);
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


        lightObject.setPosition(new Vector4f(getLightPos(), 1.0f));
    }


    public void setSceneObject(SceneObject o){lightObject = o;}
    public SceneObject getLightObject(){return lightObject;}

    public Vector3f getLightPos(){return new Vector3f(lightPos[0], lightPos[1], lightPos[2]);}
    public void setLightPosition(float x, float y, float z){lightPos[0] = x; lightPos[1] = y; lightPos[2] = z;}
}
