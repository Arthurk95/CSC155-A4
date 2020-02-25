/* Author: Arthur Kharit
 * CSC155 - Assignment 1
 * Some code used from provided resources
 *
 * 	Tested in RVR 5029 on machine PACMAN
 * */

package a1;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import a1.models.Sphere;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.FloatBuffer;

public class Starter extends JFrame implements GLEventListener, MouseWheelListener {
	public static final float MAX_SCALE = 2.0f;
	public static final float MIN_SCALE = 0.1f;
	private double startTime = 0.0;
	private double elapsedTime;
	private float cameraX, cameraY, cameraZ;
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int vao[] = new int[1];
	private int vbo[] = new int[4];
	private float gradient = 0.0f; // 0 means solid color; 1 means gradient
	private float scale = 1.0f;
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(6);
	private Matrix4f pMat = new Matrix4f();
	private int mvLoc, projLoc;
	private float aspect;
	private double tf;

	private Sphere sun;
	private int numSphereVerts;
	private GL4 gl;

	private float verticalIncrement = 0.0f;
	private float inc = 0.01f;
	private float theta = 1.0f; // bigger number = faster circle
	private float thetaTotal = 0.0f;
	private int drawMode = 0; // 0 line, 1 circle

	public Starter() {
		setTitle("CSC 155 - a1");
		setSize(800, 800);

		this.addMouseWheelListener(this);

		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);

		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// BorderLayout with center being GLCanvas, south being buttons
		this.setLayout(new BorderLayout());
		this.add(myCanvas, BorderLayout.CENTER);
		setKeyCommand('g', new a1.actions.ColorAction(this));
		this.add(makeBottomBar(), BorderLayout.SOUTH);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	// Display method for the GLSL canvas
	public void display(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		elapsedTime = System.currentTimeMillis() - startTime;

		gl.glUseProgram(renderingProgram);

		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgram, "proj_matrix");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));

		// push view matrix onto the stack
		mvStack.pushMatrix();
		mvStack.translate(-cameraX, -cameraY, -cameraZ);
		mvStack.rotate(0.1f, 1.0f, 0.0f, 0.0f);

		tf = elapsedTime/1000.0;  // time factor

		// ----------------------  pyramid == sun
		mvStack.pushMatrix();
		mvStack.translate(0.0f, 0.0f, 0.0f);
		mvStack.pushMatrix();
		mvStack.rotate((float)tf, 0.0f, 1.0f, 0.0f);
		drawDiamond();

		mvStack.popMatrix();

		//-----------------------  cube == planet
		mvStack.pushMatrix();
		mvStack.translate((float)Math.sin(tf/5)*8.0f, 0.0f, (float)Math.cos(tf/5)*8.0f);
		mvStack.pushMatrix();
		mvStack.rotate((float)tf, 0.0f, 1.0f, 0.0f);

		drawCube();

		mvStack.popMatrix();

		//-----------------------  smaller cube == moon
		mvStack.pushMatrix();
		mvStack.translate(0.0f, (float)Math.sin(tf)*2.0f, (float)Math.cos(tf)*2.0f);
		mvStack.rotate((float)tf, 0.0f, 0.0f, 1.0f);
		mvStack.scale(0.25f, 0.25f, 0.25f);
		drawSphere();
		popMultipleTimes(3);

		mvStack.pushMatrix();
		mvStack.scale(1.0f,1.0f,1.0f);
		mvStack.translate(-(float)Math.sin(tf/5)*6.0f, 0.0f, -(float)Math.cos(tf/5)*6.0f);
		mvStack.rotate(0.2f, 1.0f, 0.0f, 0.0f);
		drawSphere();
		popMultipleTimes(2);
	}

	// Outputs versions, creates and links shaders
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();
		startTime = System.currentTimeMillis();

		System.out.println("OpenGL Version: " + gl.glGetString(gl.GL_VERSION));
		System.out.println("JOGL Version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion());
		System.out.println("Java Version: " + System.getProperty("java.version"));

		renderingProgram = ShaderTools.createShaderProgram("vertShader.glsl", "fragShader.glsl");

		int shaderColor = gl.glGetUniformLocation(renderingProgram, "color");
		gl.glProgramUniform1f(renderingProgram, shaderColor, gradient);
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		setupVertices();

		cameraX = 0.0f; cameraY = 2.0f; cameraZ = 20.0f;

	}

	private void setupVertices()
	{	gl = (GL4) GLContext.getCurrentGL();
		float[] cubePositions =
				{	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
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

		float[] diamondPositions =
				{	-1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 2.0f, 0.0f,    //front
						1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 0.0f, 2.0f, 0.0f,    //right
						1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, 0.0f, 2.0f, 0.0f,  //back
						-1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 2.0f, 0.0f,  //left
						-1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, -2.0f, 0.0f,    //front
						1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 0.0f, -2.0f, 0.0f,    //right
						1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, 0.0f, -2.0f, 0.0f,  //back
						-1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, -2.0f, 0.0f,  //left
				};

		for (int i = 0; i < cubePositions.length; i++){
			cubePositions[i] = cubePositions[i] * 0.5f;
		}


		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cubePositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit()*4, cubeBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer diamondBuffer = Buffers.newDirectFloatBuffer(diamondPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, diamondBuffer.limit()*4, diamondBuffer, GL_STATIC_DRAW);

		setupSphere();

	}

	private void setupSphere(){
		sun = new Sphere(96);
		numSphereVerts = sun.getIndices().length;

		int[] indices = sun.getIndices();
		Vector3f[] vert = sun.getVertices();
		Vector2f[] tex  = sun.getTexCoords();
		Vector3f[] norm = sun.getNormals();

		float[] pvalues = new float[indices.length*3];
		float[] tvalues = new float[indices.length*2];
		float[] nvalues = new float[indices.length*3];

		for (int i=0; i<indices.length; i++) {
			pvalues[i*3] = (float) (vert[indices[i]]).x;
			pvalues[i*3+1] = (float) (vert[indices[i]]).y;
			pvalues[i*3+2] = (float) (vert[indices[i]]).z;
			tvalues[i*2] = (float) (tex[indices[i]]).x;
			tvalues[i*2+1] = (float) (tex[indices[i]]).y;
			nvalues[i*3] = (float) (norm[indices[i]]).x;
			nvalues[i*3+1]= (float)(norm[indices[i]]).y;
			nvalues[i*3+2]=(float) (norm[indices[i]]).z;
		}

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
	}

	public static void main(String[] args) { new Starter(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	// Increments a theta value to animate the triangle in a circular motion
	private void animateCircle(){
		thetaTotal += theta;
		if(thetaTotal > 360.0f) // prevent future overflow?
			thetaTotal = thetaTotal - 360.0f;

		// Update theta value in vertShader.glsl
		int shaderTheta = gl.glGetUniformLocation(renderingProgram, "theta");
		gl.glProgramUniform1f(renderingProgram, shaderTheta, thetaTotal);


		gl.glDrawArrays(GL_TRIANGLES,0,3);
	}

	private void drawSphere(){
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
	}

	private void drawCube(){
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
	}

	private void drawDiamond(){
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 24);
	}

	// Increments a verticalIncrement variable up to 1.0f/-1.0f and passes it to the vertical shader
	private void animateLine(){
		verticalIncrement += inc;
		if (verticalIncrement > 1.0f) inc = -0.01f;
		if (verticalIncrement < -1.0f) inc = 0.01f;

		// Update increment value in vertShader.glsl
		int shaderIncrement = gl.glGetUniformLocation(renderingProgram, "increment");
		gl.glProgramUniform1f(renderingProgram, shaderIncrement, verticalIncrement);

		// Update theta value in vertShader.glsl
		int shaderTheta = gl.glGetUniformLocation(renderingProgram, "theta");
		gl.glProgramUniform1f(renderingProgram, shaderTheta, 0);

		gl.glDrawArraysInstanced(GL_TRIANGLES,0,3, 4);
	}

	// creates a FlowLayout bottom bar with the two Action buttons
	private Container makeBottomBar(){
		Container layout = new Container();
		layout.setLayout(new FlowLayout());

		a1.actions.CircleAction circleAction = new a1.actions.CircleAction(this);
		a1.actions.LineAction lineAction = new a1.actions.LineAction(this);

		JButton circleButton = new JButton("Animate Circle");
		JButton lineButton = new JButton("Animate Line");

		circleButton.addActionListener(circleAction);
		lineButton.addActionListener(lineAction);

		layout.add(circleButton);
		layout.add(lineButton);
		return layout;
	}

	private void popMultipleTimes(int count){
		for(int i = 0; i < count; i++){
			mvStack.popMatrix();
		}
	}

	// Binds a key to a command
	private void setKeyCommand(char key, AbstractAction action){
		JComponent window = (JComponent) this.getContentPane(); // entire JFrame window
		int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW; // window is selected
		InputMap inputMap = window.getInputMap(mapName);

		KeyStroke gKey = KeyStroke.getKeyStroke(key); // KeyStroke represents the key

		inputMap.put(gKey, "color");
		ActionMap actionMap = window.getActionMap(); // get the action map for the window

		actionMap.put("color", action);
	}

	// 0.0f is normal, 1.0f is gradient
	public void toggleColorMode(){
		if(gradient == 0.0f)
			gradient = 1.0f;
		else gradient = 0.0f;
	}

	@Override
	// Increases/decreases scale of triangle when mousewheel scrolled
	public void mouseWheelMoved(MouseWheelEvent e){
		if (e.getWheelRotation() > 0 && scale < MAX_SCALE){ scale += 0.1f; }
		else if(e.getWheelRotation() < 0 && scale > MIN_SCALE){ scale += -0.1f; }
	}

	public void setDrawMode(int d){ drawMode = d; }

}