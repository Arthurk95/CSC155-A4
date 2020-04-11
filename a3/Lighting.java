package a3;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Lighting {
    // white light properties
    private float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
    private float[] lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
    private float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

    private float[] matAmb = ShaderTools.silverAmbient();
    private float[] matDif = ShaderTools.silverDiffuse();
    private float[] matSpe = ShaderTools.silverSpecular();
    private float matShi = ShaderTools.silverShininess();

    private Vector3f initialLightLoc = new Vector3f(1.0f, 5.0f, 0.0f);
    public static Vector3f currentLightPos = new Vector3f(5.0f, 2.0f, 2.0f);

    private float amt = 0.0f;
    private int renderingProgram;
    private float[] lightPos = new float[3];

    public Lighting(int r){
        renderingProgram = r;
    }

    public GL4 installLights(Matrix4f vMatrix, GL4 gl)
    {
        currentLightPos.set(initialLightLoc);
        amt += 0.5f;
        currentLightPos.rotateAxis((float)Math.toRadians(amt), 0.0f, 0.0f, 1.0f);

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
        gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
        gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
        gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
        gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);

        return gl;
    }
}
