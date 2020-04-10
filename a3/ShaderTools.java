package a3;


import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import static a3.ErrorHandling.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

// ShaderTools is a class that will contain all useful functions to use for Shader files (.glsl).
// This includes reading shader files, linking, etc.
public class ShaderTools {

	public ShaderTools() {}

	// Acquired from program 2.6 of the provided CD and modified to contain more
	// extensive error handling.
	public static int createShaderProgram(String vS, String fS) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];

		String[] vShaderSource = prepareShader(GL_VERTEX_SHADER, vS);
		String[] fShaderSource = prepareShader(GL_FRAGMENT_SHADER, fS);

		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderSource.length, vShaderSource, null, 0);
		gl.glCompileShader(vShader);

		checkOpenGLError();

		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] != 1) {
			System.out.println("vertex shader compilation failed");
			printShaderLog(vShader);
		}

		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fShaderSource.length, fShaderSource, null, 0);
		gl.glCompileShader(fShader);

		checkOpenGLError();  // can use returned boolean if desired
		gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		if (fragCompiled[0] != 1) {
			System.out.println("fragment shader compilation failed");
			printShaderLog(fShader);
		}


		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		finalizeProgram(vfprogram);

		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);

		return vfprogram;
	}

	// Acquired from program 2.6 of the provided CD.
	// Links the .glsl files to the program.
	public static void finalizeProgram(int sprogram) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] linked = new int[1];
		gl.glLinkProgram(sprogram);
		checkOpenGLError();
		gl.glGetProgramiv(sprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] != 1) {
			System.out.println("linking failed");
			printProgramLog(sprogram);
		}
	}

	// Acquired from program 2.6 of the provided CD.
	private static String[] prepareShader(int shaderTYPE, String shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] shaderCompiled = new int[1];
		String shaderSource[] = readShaderSource(shader);
		int shaderRef = gl.glCreateShader(shaderTYPE);
		gl.glShaderSource(shaderRef, shaderSource.length, shaderSource, null, 0);
		gl.glCompileShader(shaderRef);
		checkOpenGLError();
		gl.glGetShaderiv(shaderRef, GL_COMPILE_STATUS, shaderCompiled, 0);
		if (shaderCompiled[0] != 1) {
			if (shaderTYPE == GL_VERTEX_SHADER) System.out.print("Vertex ");
			if (shaderTYPE == GL_TESS_CONTROL_SHADER) System.out.print("Tess Control ");
			if (shaderTYPE == GL_TESS_EVALUATION_SHADER) System.out.print("Tess Eval ");
			if (shaderTYPE == GL_GEOMETRY_SHADER) System.out.print("Geometry ");
			if (shaderTYPE == GL_FRAGMENT_SHADER) System.out.print("Fragment ");
			System.out.println("shader compilation error.");
			printShaderLog(shaderRef);
		}
		return shaderSource;
	}

	// Acquired from program 2.6 of the provided CD.
	// Reads the shader source files and stores them in a String array
	private static String[] readShaderSource(String filename) {
		Vector<String> lines = new Vector<String>();
		Scanner sc;
		String[] program;
		try {
			sc = new Scanner(new File(filename));
			while (sc.hasNext()) {
				lines.addElement(sc.nextLine());
			}
			program = new String[lines.size()];
			for (int i = 0; i < lines.size(); i++) {
				program[i] = (String) lines.elementAt(i) + "\n";
			}
		}
		catch (IOException e) {
			System.err.println("IOException reading file: " + e);
			return null;
		}
		return program;
	}

	public static int loadTexture(String textureFileName)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int finalTextureRef;
		Texture tex = null;
		String tmp = new File("").getAbsolutePath();
		textureFileName = tmp + textureFileName;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e) { e.printStackTrace(); }
		finalTextureRef = tex.getTextureObject();

		// building a mipmap and use anisotropic filtering
		gl.glBindTexture(GL_TEXTURE_2D, finalTextureRef);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float anisoset[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0]);
		}
		return finalTextureRef;
	}

	// GOLD material - ambient, diffuse, specular, and shininess
	public static float[] goldAmbient()  { return (new float [] {0.2473f,  0.1995f, 0.0745f, 1} ); }
	public static float[] goldDiffuse()  { return (new float [] {0.7516f,  0.6065f, 0.2265f, 1} ); }
	public static float[] goldSpecular() { return (new float [] {0.6283f,  0.5559f, 0.3661f, 1} ); }
	public static float goldShininess()  { return 51.2f; }

	// SILVER material - ambient, diffuse, specular, and shininess
	public static float[] silverAmbient()  { return (new float [] {0.1923f,  0.1923f,  0.1923f, 1} ); }
	public static float[] silverDiffuse()  { return (new float [] {0.5075f,  0.5075f,  0.5075f, 1} ); }
	public static float[] silverSpecular() { return (new float [] {0.5083f,  0.5083f,  0.5083f, 1} ); }
	public static float silverShininess()  { return 51.2f; }


	// BRONZE material - ambient, diffuse, specular, and shininess
	public static float[] bronzeAmbient()  { return (new float [] {0.2125f,  0.1275f, 0.0540f, 1} ); }
	public static float[] bronzeDiffuse()  { return (new float [] {0.7140f,  0.4284f, 0.1814f, 1} ); }
	public static float[] bronzeSpecular() { return (new float [] {0.3936f,  0.2719f, 0.1667f, 1} ); }
	public static float bronzeShininess()  { return 25.6f; }
}