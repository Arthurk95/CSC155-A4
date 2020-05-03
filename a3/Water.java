package a3;

import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import org.joml.Vector3f;

import static com.jogamp.opengl.GL4.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

public class Water{
    private int noiseHeight = 256;
    private int noiseWidth = 256;
    private int noiseDepth = 256;
    private float surfaceLocX = 0.0f, surfaceLocY = 0.0f, surfaceLocZ = 0.0f;
    private int[] bufferId = new int[1];
    private int[] vbo = new int[4];
    private int refractTextureId;
    private int reflectTextureId;
    private int refractFrameBuffer;
    private int reflectFrameBuffer;

    private double[][][] noise = new double[noiseWidth][noiseHeight][noiseDepth];
    private int noiseTexture;
    private Random random = new Random(5);
    private double PI = 3.1415926535;

    public Vector3f getPosition(){return new Vector3f(surfaceLocX, surfaceLocY, surfaceLocZ);}

    private double smooth(double zoom, double x1, double y1, double z1)
    {	//get fractional part of x, y, and z
        double fractX = x1 - (int) x1;
        double fractY = y1 - (int) y1;
        double fractZ = z1 - (int) z1;

        //neighbor values that wrap
        double x2 = x1 - 1; if (x2<0) x2 = ((int)(noiseHeight / zoom)) + x2;
        double y2 = y1 - 1; if (y2<0) y2 = ((int)(noiseWidth / zoom)) + y2;
        double z2 = z1 - 1; if (z2<0) z2 = ((int)(noiseDepth / zoom)) + z2;

        //smooth the noise by interpolating
        double value = 0.0;

        value += fractX     * fractY     * fractZ     * noise[(int)x1][(int)y1][(int)z1];
        value += fractX     * (1-fractY) * fractZ     * noise[(int)x1][(int)y2][(int)z1];
        value += (1-fractX) * fractY     * fractZ     * noise[(int)x2][(int)y1][(int)z1];
        value += (1-fractX) * (1-fractY) * fractZ     * noise[(int)x2][(int)y2][(int)z1];

        value += fractX     * fractY     * (1-fractZ) * noise[(int)x1][(int)y1][(int)z2];
        value += fractX     * (1-fractY) * (1-fractZ) * noise[(int)x1][(int)y2][(int)z2];
        value += (1-fractX) * fractY     * (1-fractZ) * noise[(int)x2][(int)y1][(int)z2];
        value += (1-fractX) * (1-fractY) * (1-fractZ) * noise[(int)x2][(int)y2][(int)z2];

        return value;
    }

    private double turbulence(double x, double y, double z, double maxZoom)
    {	double sum = 0.0, zoom = maxZoom;

        sum = (Math.sin((1.0/512.0)*(8*PI)*(x+z-4*y)) + 1) * 8.0;
        while(zoom >= 0.9)
        {	sum = sum + smooth(zoom, x/zoom, y/zoom, z/zoom) * zoom;
            zoom = zoom / 2.0;
        }
        sum = 128.0 * sum/maxZoom;
        return sum;
    }

    private void fillDataArray(byte data[])
    {	double maxZoom = 32.0;
        for (int i=0; i<noiseWidth; i++)
        {	for (int j=0; j<noiseHeight; j++)
        {	for (int k=0; k<noiseDepth; k++)
        {	noise[i][j][k] = random.nextDouble();
        }	}	}
        for (int i = 0; i<noiseHeight; i++)
        {	for (int j = 0; j<noiseWidth; j++)
        {	for (int k = 0; k<noiseDepth; k++)
        {	data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+0] = (byte)turbulence(i,j,k,maxZoom);
            data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+1] = (byte)turbulence(i,j,k,maxZoom);
            data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+2] = (byte)turbulence(i,j,k,maxZoom);
            data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+3] = (byte)255;
        }	}	}	}

    public int buildNoiseTexture()
    {	GL4 gl = (GL4) GLContext.getCurrentGL();

        byte[] data = new byte[noiseWidth*noiseHeight*noiseDepth*4];

        fillDataArray(data);

        ByteBuffer bb = Buffers.newDirectByteBuffer(data);

        int[] textureIDs = new int[1];
        gl.glGenTextures(1, textureIDs, 0);
        int textureID = textureIDs[0];

        gl.glBindTexture(GL_TEXTURE_3D, textureID);

        gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseWidth, noiseHeight, noiseDepth);
        gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
                noiseWidth, noiseHeight, noiseDepth, GL_RGBA, GL_UNSIGNED_BYTE, bb);

        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        return textureID;
    }

    public void createReflectRefractBuffers(int canvasWidth, int canvasHeight) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // Initialize Reflect Framebuffer
        gl.glGenFramebuffers(1, bufferId, 0);
        reflectFrameBuffer = bufferId[0];
        gl.glBindFramebuffer(GL_FRAMEBUFFER, reflectFrameBuffer);
        gl.glGenTextures(1, bufferId, 0);
        reflectTextureId = bufferId[0];
        gl.glBindTexture(GL_TEXTURE_2D, reflectTextureId);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, canvasWidth, canvasHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, reflectTextureId, 0);
        gl.glDrawBuffer(GL_COLOR_ATTACHMENT0);
        gl.glGenTextures(1, bufferId, 0);
        gl.glBindTexture(GL_TEXTURE_2D, bufferId[0]);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, canvasWidth, canvasHeight, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, bufferId[0], 0);

        // Initialize Refract Framebuffer
        gl.glGenFramebuffers(1, bufferId, 0);
        refractFrameBuffer = bufferId[0];
        gl.glBindFramebuffer(GL_FRAMEBUFFER, refractFrameBuffer);
        gl.glGenTextures(1, bufferId, 0);
        refractTextureId = bufferId[0];
        gl.glBindTexture(GL_TEXTURE_2D, refractTextureId);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, canvasWidth, canvasHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, refractTextureId, 0);
        gl.glDrawBuffer(GL_COLOR_ATTACHMENT0);
        gl.glGenTextures(1, bufferId, 0);
        gl.glBindTexture(GL_TEXTURE_2D, bufferId[0]);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, canvasWidth, canvasHeight, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, bufferId[0], 0);
    }

    public int getRefractTextureId(){return refractTextureId;}
    public int getRefractFrameBuffer(){return refractFrameBuffer;}

    public int getReflectTextureId(){return reflectTextureId;}
    public int getReflectFrameBuffer(){return reflectFrameBuffer;}

    public void setupVertices()
    {	GL4 gl = (GL4) GLContext.getCurrentGL();

        float[] cubeVertexPositions =
                { -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
                        1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
                        1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
                        -1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
                        -1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
                        -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
                        -1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
                        -1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
                        1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
                };
        float[] PLANE_POSITIONS = {
                -128.0f, 0.0f, -128.0f,  -128.0f, 0.0f, 128.0f,  128.0f, 0.0f, -128.0f,
                128.0f, 0.0f, -128.0f,  -128.0f, 0.0f, 128.0f,  128.0f, 0.0f, 128.0f
        };
        float[] PLANE_TEXCOORDS = {
                0.0f, 0.0f,  0.0f, 1.0f,  1.0f, 0.0f,
                1.0f, 0.0f,  0.0f, 1.0f,  1.0f, 1.0f
        };
        float[] PLANE_NORMALS = {
                0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f
        };

        gl.glGenBuffers(vbo.length, vbo, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer planeBuf = Buffers.newDirectFloatBuffer(PLANE_POSITIONS);
        gl.glBufferData(GL_ARRAY_BUFFER, planeBuf.limit()*4, planeBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        FloatBuffer texBuf = Buffers.newDirectFloatBuffer(PLANE_TEXCOORDS);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        FloatBuffer norBuf = Buffers.newDirectFloatBuffer(PLANE_NORMALS);
        gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4, norBuf, GL_STATIC_DRAW);
    }

    public int[] getVBO(){return vbo;}

}
