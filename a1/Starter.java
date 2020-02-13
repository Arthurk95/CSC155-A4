/* Author: Arthur Kharit
 * CSC155 - Assignment 1
 * Some code used from provided resources
 *
 *
 * */

package a1;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Starter extends JFrame implements GLEventListener, MouseWheelListener {
	public static final float MAX_SCALE = 2.0f;
	public static final float MIN_SCALE = 0.1f;
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int vao[] = new int[1];
	private float gradient = 0.0f;
	private float scale = 1.0f;

	private GL4 gl;

	private float verticalIncrement = 0.0f;
	private float inc = 0.01f;
	private float theta = 1.0f;
	private float thetaTotal = 0.0f;
	private int drawMode = 0; // 0 line, 1 circle

	public Starter() {
		setTitle("CSC 155 - a1");
		setSize(500, 500);

		this.addMouseWheelListener(this);

		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);

		this.setLayout(new BorderLayout());
		this.add(myCanvas, BorderLayout.CENTER);
		setKeyCommand('g', new a1.actions.ColorAction(this));
		this.add(makeBottomBar(), BorderLayout.SOUTH);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	public void display(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glUseProgram(renderingProgram);

		int shaderColor = gl.glGetUniformLocation(renderingProgram, "color");
		gl.glProgramUniform1f(renderingProgram, shaderColor, gradient);
		int shaderScale = gl.glGetUniformLocation(renderingProgram, "scale");
		gl.glProgramUniform1f(renderingProgram, shaderScale, scale);

		if(drawMode == 0)
			animateLine();
		else animateCircle();
	}

	// Outputs versions, creates and links shaders
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();

		System.out.println("OpenGL Version: " + gl.glGetString(gl.GL_VERSION));
		System.out.println("JOGL Version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion());
		System.out.println("Java Version: " + System.getProperty("java.version"));

		renderingProgram = Utility.createShaderProgram("vertShader.glsl", "fragShader.glsl");

		int shaderColor = gl.glGetUniformLocation(renderingProgram, "color");
		gl.glProgramUniform1f(renderingProgram, shaderColor, gradient);
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

	}

	public static void main(String[] args) { new Starter(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	// Increments a theta value to animate the triangle in a circular motion
	private void animateCircle(){
		thetaTotal += theta;
		if(thetaTotal > 360.0f) // prevent future overflow?
			thetaTotal = thetaTotal - 360.0f;

		int shaderTheta = gl.glGetUniformLocation(renderingProgram, "theta");
		gl.glProgramUniform1f(renderingProgram, shaderTheta, thetaTotal);


		gl.glDrawArrays(GL_TRIANGLES,0,3);
	}

	// Increments a verticalIncrement variable up to 1.0f/-1.0f and passes it to the vertical shader
	private void animateLine(){
		verticalIncrement += inc;
		if (verticalIncrement > 1.0f) inc = -0.01f;
		if (verticalIncrement < -1.0f) inc = 0.01f;
		int offsetInc = gl.glGetUniformLocation(renderingProgram, "increment");
		gl.glProgramUniform1f(renderingProgram, offsetInc, verticalIncrement);

		int shaderTheta = gl.glGetUniformLocation(renderingProgram, "theta");
		gl.glProgramUniform1f(renderingProgram, shaderTheta, 0);

		gl.glDrawArrays(GL_TRIANGLES,0,3);
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