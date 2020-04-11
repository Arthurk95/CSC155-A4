package a3.sceneobject;

import a3.ImportedObject;
import a3.ShaderTools;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

import static com.jogamp.opengl.GL.*;

public class SceneObject {

    ImportedObject model;

    // SceneObjects default to silver material
    private float[] matAmb = ShaderTools.silverAmbient();
    private float[] matDif = ShaderTools.silverDiffuse();
    private float[] matSpe = ShaderTools.silverSpecular();
    private float matShi = ShaderTools.silverShininess();

    public float[] getAmb(){return matAmb;}
    public float[] getDif(){return matDif;}
    public float[] getSpe(){return matSpe;}
    private int renderingProgram;
    private Vector3f position;

    private int[] vbo = new int[3];

    public SceneObject(){}
    public SceneObject(int r, ImportedObject m, Vector3f pos){
        renderingProgram = r;
        model = m;
        position = pos;
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

    public int getNumVerts(){return model.getNumVertices(); }
    public Vector3f getPosition(){return position;}

    public void setupVBO(){
        GL4 gl = (GL4) GLContext.getCurrentGL();
        int sphereVertices = model.getNumVertices();
        Vector3f[] vertices = model.getVertices();
        Vector2f[] texCoords = model.getTexCoords();
        Vector3f[] normals = model.getNormals();

        System.out.println("Verticies: " + sphereVertices);
        float[] pvalues = new float[sphereVertices*3];
        float[] tvalues = new float[sphereVertices*2];
        float[] nvalues = new float[sphereVertices*3];

        for (int i=0; i<sphereVertices; i++)
        {	pvalues[i*3]   = (float) (vertices[i]).x();
            pvalues[i*3+1] = (float) (vertices[i]).y();
            pvalues[i*3+2] = (float) (vertices[i]).z();
            tvalues[i*2]   = (float) (texCoords[i]).x();
            tvalues[i*2+1] = (float) (texCoords[i]).y();
            nvalues[i*3]   = (float) (normals[i]).x();
            nvalues[i*3+1] = (float) (normals[i]).y();
            nvalues[i*3+2] = (float) (normals[i]).z();
        }

        gl.glGenBuffers(vbo.length, vbo, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);

        System.out.println(pvalues.length);
    }

    public int[] getVBO(){return vbo;}
}
