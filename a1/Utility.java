package a1;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.color.ColorSpace;

public class Utility {

	public Utility() {}

	public static int createShaderProgram(String vS, String fS) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int vShader  = prepareShader(GL_VERTEX_SHADER, vS);
		int fShader  = prepareShader(GL_FRAGMENT_SHADER, fS);
		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		finalizeProgram(vfprogram);
		return vfprogram;
	}

	public static int finalizeProgram(int sprogram) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] linked = new int[1];
		gl.glLinkProgram(sprogram);
		checkOpenGLError();
		gl.glGetProgramiv(sprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] != 1)
		{	System.out.println("linking failed");
			printProgramLog(sprogram);
		}
		return sprogram;
	}
	
	private static int prepareShader(int shaderTYPE, String shader) {
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
		return shaderRef;
	}
	
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

	private static void printShaderLog(int shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++) {
				System.out.print((char) log[i]);
			}
		}
	}

	public static void printProgramLog(int prog) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine length of the program compilation log
		gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++) {
				System.out.print((char) log[i]);
			}
		}
	}

	public static boolean checkOpenGLError() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		boolean foundError = false;
		GLU glu = new GLU();
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR) {
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		return foundError;
	}

	private static BufferedImage getBufferedImage(String fileName)
	{	BufferedImage img;
		try {
			img = ImageIO.read(new File(fileName));	// assumes GIF, JPG, PNG, BMP
		} catch (IOException e) {
			System.err.println("Error reading '" + fileName + '"');
			throw new RuntimeException(e);
		}
		return img;
	}

	private static byte[] getRGBAPixelData(BufferedImage img, boolean flip) {
		int height = img.getHeight(null);
		int width = img.getWidth(null);

		// create an (empty) BufferedImage with a suitable Raster and ColorModel
		WritableRaster raster = Raster.createInterleavedRaster(
				DataBuffer.TYPE_BYTE, width, height, 4, null);

		// convert to a color model that OpenGL understands
		ComponentColorModel colorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 }, // bits
				true,  // hasAlpha
				false, // isAlphaPreMultiplied
				ComponentColorModel.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);

		BufferedImage newImage = new BufferedImage(colorModel, raster, false, null);
		Graphics2D g = newImage.createGraphics();

		if (flip){	// flip image vertically
			AffineTransform gt = new AffineTransform();
			gt.translate(0, height);
			gt.scale(1, -1d);
			g.transform(gt);
		}
		g.drawImage(img, null, null); // draw original image into new image
		g.dispose();

		// now retrieve the underlying byte array from the raster data buffer
		DataBufferByte dataBuf = (DataBufferByte) raster.getDataBuffer();
		return dataBuf.getData();
	}

}