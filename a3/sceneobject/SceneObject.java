package a3.sceneobject;

import a3.ImportedObject;
import a3.ShaderTools;
import a3.models.Sphere;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static com.jogamp.opengl.GL.*;

public class SceneObject {

    ImportedObject model;

    int texture;
    float scale = 1.0f;

    // SceneObjects default to silver material
    private float[] matAmb = ShaderTools.silverAmbient();
    private float[] matDif = ShaderTools.silverDiffuse();
    private float[] matSpe = ShaderTools.silverSpecular();
    private float matShi = ShaderTools.silverShininess();

    public float[] getAmb(){return matAmb;}
    public float[] getDif(){return matDif;}
    public float[] getSpe(){return matSpe;}
    private Vector3f position;

    private int[] indices;
    private int precision;

    private int numIndices;

    private int[] vbo = new int[4];

    public SceneObject(){}
    public SceneObject(ImportedObject o, Vector3f pos){
        model = o;
        position = pos;
    }

    public SceneObject(ImportedObject o, int tex, Vector3f pos){
        model = o;
        position = pos;
        texture = tex;
    }

    public SceneObject(int r){}

    public void setLighting(int renderingProgram){
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
        int numVertices = model.getNumVertices();
        Vector3f[] vertices = model.getVertices();
        Vector2f[] texCoords = model.getTexCoords();
        Vector3f[] normals = model.getNormals();

        precision = numVertices;

        float[] pvalues = new float[numVertices*3];
        float[] tvalues = new float[numVertices*2];
        float[] nvalues = new float[numVertices*3];

        for (int i=0; i<numVertices; i++)
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

        //gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
        //IntBuffer idxBuf = Buffers.newDirectIntBuffer(model.getIndices());
        //gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuf.limit()*4, idxBuf, GL_STATIC_DRAW);

    }

    public Vector3f[] getVerts(){return model.getVertices();}
    public Vector2f[] getTexCoords(){return model.getTexCoords();}
    public Vector3f[] getNormals(){return model.getNormals();}
    public int[] getVBO(){return vbo;}

    public void setPosition(float[] newPos){
        position = new Vector3f(newPos[0], newPos[1], newPos[2]);
    }

    public void setPosition(Vector3f newPos){
        position = newPos;
    }

    public int getTexture(){return texture;}

    public void setScale(float s){scale = s;}
    public float getScale(){return scale;}

    public void setMats(ArrayList<float[]> mats){
        matAmb = mats.get(0);
        matDif = mats.get(1);
        matSpe = mats.get(2);
    }
}
