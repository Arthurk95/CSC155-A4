package a1;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import a1.components.ActionButton;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;

import java.awt.*;

public class Starter extends JFrame implements GLEventListener {
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int vao[] = new int[1];

	private GL4 gl;

	private float verticalIncrement = 0.0f;
	private float inc = 0.01f;
	private float theta = 1.0f;
	private float thetaTotal = 0.0f;
	private int drawMode = 0; // 0 line, 1 circle

	public Starter() {
		setTitle("CSC 155 - a1");
		setSize(500, 500);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.setLayout(new BorderLayout());
		this.add(myCanvas, BorderLayout.CENTER);
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

		if(drawMode == 0)
			animateLine();
		else animateCircle();
	}

	public void init(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();

		System.out.println("OpenGL Version: " + gl.glGetString(gl.GL_VERSION));
		System.out.println("JOGL Version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion());
		System.out.println("Java Version: " + System.getProperty("java.version"));

		renderingProgram = Utility.createShaderProgram("vertShader.glsl", "fragShader.glsl");
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

	}

	public static void main(String[] args) { new Starter(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	private void animateCircle(){

		thetaTotal += theta;

		int shaderTheta = gl.glGetUniformLocation(renderingProgram, "theta");
		gl.glProgramUniform1f(renderingProgram, shaderTheta, thetaTotal);


		gl.glDrawArrays(GL_TRIANGLES,0,3);
	}

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

	public void setDrawMode(int d){ drawMode = d; }

}