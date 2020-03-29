/* Author: Arthur Kharit
 * CSC155 - Assignment 1
 * Some code used from provided resources
 *
 * 	Tested in RVR 5029 on machine PACMAN
 * */

package a1;

import javax.rmi.CORBA.Util;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import a1.actions.camera.*;
import a1.models.Cube;
import a1.models.Diamond;
import a1.models.Sphere;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;
import org.joml.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.Math;
import java.nio.FloatBuffer;

/* All planet textures from https://www.solarsystemscope.com/textures/,
 * Stated at the bottom that their textures are distributed under
 * Attribution 4.0 International license
 *
 * */
public class Starter extends JFrame implements GLEventListener, MouseWheelListener {
	public static final float MAX_SCALE = 2.0f;
	public static final float MIN_SCALE = 0.1f;
	private double startTime = 0.0;
	private double elapsedTime;
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int[] vao = new int[1];
	private int[] vboDiamond = new int[2];
	private int[] vboCube = new int[2];
	private int[] vboSphere = new int[3]; // VBO for the sphere object
	private float gradient = 0.0f; // 0 means solid color; 1 means gradient
	private float scale = 1.0f;
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(6);
	private Matrix4f pMat = new Matrix4f();
	private Matrix4f mvMat = new Matrix4f();
	private Matrix4f mMat = new Matrix4f();
	private Matrix4f vMat = new Matrix4f();
	private int mvLoc, projLoc;
	private float aspect;
	private double tf;
	private int earthTexture, moonTexture, marsTexture, venusTexture, lineTexture, sunTexture, jupiterTexture;
	private float[] mustangPos = {0.0f, 0.0f, 0.0f};
	private int[] vboMustang = new int[3];
	private ObjectReader mustang;


	private Camera camera;

	private Sphere sphere;
	private int numSphereVerts;
	private GL4 gl;

	private float verticalIncrement = 0.0f;
	private float inc = 0.01f;
	private float theta = 1.0f; // bigger number = faster circle
	private float thetaTotal = 0.0f;
	private int drawMode = 0; // 0 line, 1 circle

	public Starter() {
		setTitle("CSC 155 - a2");
		setSize(800, 800);
		camera = new Camera(0.0f, 0.0f, 10.0f);
		this.addMouseWheelListener(this);

		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);

		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


		// BorderLayout with center being GLCanvas, south being buttons
		this.setLayout(new BorderLayout());
		this.add(myCanvas, BorderLayout.CENTER);
		setupKeyBindings();
		/*
		setKeyCommand(KeyEvent.VK_W, new MoveForward(camera));
		setKeyCommand(KeyEvent.VK_A, new MoveLeft(camera));
		setKeyCommand(KeyEvent.VK_S, new MoveBackward(camera));
		setKeyCommand(KeyEvent.VK_D, new MoveRight(camera));
		setKeyCommand(KeyEvent.VK_Q, new MoveUp(camera));
		setKeyCommand(KeyEvent.VK_E, new MoveDown(camera));
		//setKeyCommand((char)KeyEvent.VK_LEFT, new a1.actions.camera.PanLeft(camera));
		//setKeyCommand((char)KeyEvent.VK_RIGHT, new a1.actions.camera.PanRight(camera));
		//setKeyCommand((char)KeyEvent.VK_UP, new a1.actions.camera.PitchUp(camera));
		//setKeyCommand((char)KeyEvent.VK_DOWN, new a1.actions.camera.PitchDown(camera));
		*/
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
		pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		vMat = camera.getView();



		mMat.translation(0.0f, 0.0f, 0.0f);
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		drawSphere(earthTexture);
		mMat.translation(0.0f, 0.0f, 0.0f);
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		drawAxisLines();
		/*
		Vector4f pc = camera.getLoc();
		// push view matrix onto the stack
		mvStack.pushMatrix();
		mvStack.translate(pc.x, pc.y, pc.z);

		tf = elapsedTime/1000.0;  // time factor



		drawSun();

		//------- venus
		mvStack.pushMatrix();
		mvStack.scale(0.4f, 0.4f, 0.4f);
		mvStack.translate((float)Math.sin(tf)*6.0f, 0.0f, (float)Math.cos(tf)*6.0f);
		mvStack.rotate(-(float)tf/8, 0.0f, 1.0f, 0.2f);
		drawSphere(venusTexture);
		mvStack.popMatrix();

		//----------------------- Draw earth
		mvStack.pushMatrix();
		mvStack.translate((float)Math.sin(tf/4)*6.0f, 0.0f, (float)Math.cos(tf/4)*6.0f);
		mvStack.pushMatrix();
		mvStack.scale(0.6f, 0.6f, 0.6f);
		mvStack.rotate((float)tf/2, 0.0f, 1.0f, 0.3f);

		drawSphere(earthTexture);

		mvStack.popMatrix();

		//----------------------- earth's moon
		mvStack.pushMatrix();
		mvStack.translate((float)Math.sin(tf), (float)Math.sin(tf)*2.0f, (float)Math.cos(tf)*2.0f);
		mvStack.rotate((float)tf, 0.0f, 0.0f, 1.0f);
		mvStack.scale(0.25f, 0.25f, 0.25f);
		drawSphere(moonTexture);
		popMultipleTimes(3);

		//-------- mars
		mvStack.pushMatrix();
		mvStack.scale(0.5f, 0.5f, 0.5f);
		mvStack.translate((float)Math.sin(tf/5)*20.0f, 0.0f, (float)Math.cos(tf/5)*20.0f);
		mvStack.rotate((float)tf/2, 0.0f, 1.0f, 0.2f);
		drawSphere(marsTexture);
		mvStack.popMatrix();

		//------- jupiter
		mvStack.pushMatrix();
		mvStack.scale(1.0f, 1.0f, 1.0f);
		mvStack.translate((float)Math.sin(tf/8)*30.0f, 0.0f, (float)Math.cos(tf/8)*30.0f);
		mvStack.rotate(-(float)tf/5, 0.0f, 1.0f, 0.1f);
		drawSphere(jupiterTexture);


		popMultipleTimes(2);
		*/
	}

	// Outputs versions, creates and links shaders
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();
		startTime = System.currentTimeMillis();

		System.out.println("OpenGL Version: " + gl.glGetString(gl.GL_VERSION));
		System.out.println("JOGL Version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion());
		System.out.println("Java Version: " + System.getProperty("java.version"));

		renderingProgram = ShaderTools.createShaderProgram("vertShader.glsl", "fragShader.glsl");

		//mustang = new ObjectReader("\\mustang_GT.obj");

		int shaderColor = gl.glGetUniformLocation(renderingProgram, "color");
		gl.glProgramUniform1f(renderingProgram, shaderColor, gradient);

		setupVertices();

		earthTexture = ShaderTools.loadTexture("\\a1\\textures\\earth.jpg");
		moonTexture = ShaderTools.loadTexture("\\a1\\textures\\moon.jpg");
		venusTexture = ShaderTools.loadTexture("\\a1\\textures\\venus.jpg");
		lineTexture = ShaderTools.loadTexture("\\a1\\textures\\lines.jpg");
		marsTexture = ShaderTools.loadTexture("\\a1\\textures\\mars.jpg");
		moonTexture = ShaderTools.loadTexture("\\a1\\textures\\moon.jpg");
		sunTexture = ShaderTools.loadTexture("\\a1\\textures\\sun.jpg");
		jupiterTexture = ShaderTools.loadTexture("\\a1\\textures\\jupiter.jpg");

	}

	private void setupVertices()
	{	gl = (GL4) GLContext.getCurrentGL();
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		//setup3DModel(mustang);

		setupCube();

		//setupDiamond();
		setupSphere();

	}

	private void setup3DModel(ObjectReader model){
		int numObjVertices = model.getNumVertices();
		Vector3f[] vertices = model.getVertices();
		Vector2f[] texCoords = model.getTexCoords();
		Vector3f[] normals = model.getNormals();

		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];

		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   = (float) (vertices[i]).x();
			pvalues[i*3+1] = (float) (vertices[i]).y();
			pvalues[i*3+2] = (float) (vertices[i]).z();
			tvalues[i*2]   = (float) (texCoords[i]).x();
			tvalues[i*2+1] = (float) (texCoords[i]).y();
			nvalues[i*3]   = (float) (normals[i]).x();
			nvalues[i*3+1] = (float) (normals[i]).y();
			nvalues[i*3+2] = (float) (normals[i]).z();
		}

		gl.glGenBuffers(vboMustang.length, vboMustang, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboMustang[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

	}


	private void setupCube(){
		Cube cube = new Cube();

		gl.glGenBuffers(vboCube.length, vboCube, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboCube[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(cube.getPositions());
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboCube[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(cube.getTextCoords());
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

	}

	private void setupDiamond(){
		Diamond diamond = new Diamond();

		gl.glGenBuffers(vboDiamond.length, vboDiamond, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboDiamond[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(diamond.getPositions());
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboDiamond[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(diamond.getTextureCoords());
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

	}

	private void setupSphere(){
		sphere = new Sphere(96);
		numSphereVerts = sphere.getIndices().length;

		int[] indices = sphere.getIndices();
		Vector3f[] vert = sphere.getVertices();
		Vector2f[] tex  = sphere.getTexCoords();
		Vector3f[] norm = sphere.getNormals();

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

		gl.glGenBuffers(vboSphere.length, vboSphere, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSphere[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSphere[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSphere[2]);
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

	private void drawSun(){
		mvStack.pushMatrix();
		mvStack.translate(0.0f, 0.0f, 0.0f);
		mvStack.pushMatrix();
		mvStack.translate(0.0f, 1.25f, 0.0f);
		mvStack.scale(2.5f, 2.5f, 2.5f);
		mvStack.rotate((float)tf/3, 0.0f, 1.0f, 0.0f);
		drawDiamond(sunTexture);
		mvStack.popMatrix();
	}

	// Re-uses the cube model to draw very thin lines along the xyz axes
	private void drawAxisLines(){
		// x-axis
		mvStack.pushMatrix();
		mvStack.translate(25.0f, 0.0f, 0.0f);
		mvStack.scale(50.0f, 0.04f, 0.04f);
		drawCube(lineTexture);
		mvStack.popMatrix();


		mvStack.pushMatrix();
		mvStack.translate(0.0f, 25.0f, 0.0f);
		mvStack.scale(0.04f, 50.0f, 0.04f);
		drawCube(lineTexture);
		mvStack.popMatrix();

		mvStack.pushMatrix();
		mvStack.translate(0.0f, 0.0f, 25.0f);
		mvStack.scale(0.04f, 0.04f, 50.0f);
		drawCube(lineTexture);
		mvStack.popMatrix();
	}

	private void drawSphere(int tex){
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSphere[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSphere[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSphere[2]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		bindTexture(tex);

		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
	}

	private void drawCube(int tex){
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboCube[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboCube[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		bindTexture(tex);

		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
	}

	private void drawDiamond(int tex){
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboDiamond[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboDiamond[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glEnable(GL_DEPTH_TEST);

		bindTexture(tex);
		gl.glDrawArrays(GL_TRIANGLES, 0, 24);
	}

	private void drawAsteroidBelt(int tex){
		float ranNum = (float)Math.random()*360;
		mvStack.pushMatrix();
		mvStack.translate((float)Math.sin(ranNum)*5.0f, 0.0f, (float)Math.cos(ranNum)*5.0f);
		mvStack.scale(0.3f, 0.3f, 0.3f);
		mvStack.rotate((float)tf/2, 0.0f, 1.0f, 0.3f);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSphere[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSphere[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glEnable(GL_DEPTH_TEST);
		bindTexture(tex);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		mvStack.popMatrix();



	}

	private void bindTexture(int tex){
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, tex);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
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
	private void setKeyCommand(int key, AbstractAction action){
		JComponent window = (JComponent) this.getContentPane(); // entire JFrame window
		int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW; // window is selected
		InputMap inputMap = window.getInputMap(mapName);

		KeyStroke gKey = KeyStroke.getKeyStroke(key, 0); // KeyStroke represents the key

		inputMap.put(gKey, key);
		ActionMap actionMap = window.getActionMap(); // get the action map for the window

		actionMap.put(gKey, action);
	}


	private void setupKeyBindings(){
		JComponent window = (JComponent) this.getContentPane(); // entire JFrame window
		InputMap imap = window.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap amap = window.getActionMap();

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), KeyEvent.VK_W);
		amap.put(KeyEvent.VK_W, new MoveForward(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), KeyEvent.VK_S);
		amap.put(KeyEvent.VK_S, new MoveBackward(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), KeyEvent.VK_A);
		amap.put(KeyEvent.VK_A, new MoveLeft(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), KeyEvent.VK_D);
		amap.put(KeyEvent.VK_D, new MoveRight(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), KeyEvent.VK_E);
		amap.put(KeyEvent.VK_E, new MoveDown(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), KeyEvent.VK_Q);
		amap.put(KeyEvent.VK_Q, new MoveUp(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), KeyEvent.VK_LEFT);
		amap.put(KeyEvent.VK_LEFT, new PanLeft(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), KeyEvent.VK_RIGHT);
		amap.put(KeyEvent.VK_RIGHT, new PanRight(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KeyEvent.VK_UP);
		amap.put(KeyEvent.VK_UP, new PitchUp(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KeyEvent.VK_DOWN);
		amap.put(KeyEvent.VK_DOWN, new PitchDown(camera));
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