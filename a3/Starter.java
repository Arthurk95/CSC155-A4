/* Author: Arthur Kharit
 * CSC155 - Assignment 1
 * Some code used from provided resources
 *
 * 	Tested in RVR 5029 on machine PACMAN
 * */

package a3;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import a3.actions.ToggleAxes;
import a3.actions.ToggleLight;
import a3.actions.camera.*;
import a3.material.*;
import a3.models.Cube;
import a3.sceneobject.SceneObject;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;
import org.joml.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.Math;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/* All planet textures from https://www.solarsystemscope.com/textures/,
 * Stated at the bottom that their textures are distributed under
 * Attribution 4.0 International license
 *
 * */
public class Starter extends JFrame implements GLEventListener {
	public static final float MAX_SCALE = 2.0f;
	public static final float MIN_SCALE = 0.1f;
	private GLCanvas myCanvas;
	private int shadowProgram, mainProgram, skyBoxProgram, axisProgram, terrainProgram, waterProgram;
	private int[] vao = new int[1];
	private int[] vboSkyBox = new int[2];
	private float scale = 1.0f;
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f mvMat = new Matrix4f();
	private Matrix4f vMat = new Matrix4f();
	private Matrix4f mMat = new Matrix4f();
	private Matrix4f pMat = new Matrix4f();
	private Matrix4f mvpMat = new Matrix4f(); // model-view-perspective matrix
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose

	private Material defaultMaterial = new Material();
	private Material darkGrassMaterial = new DarkGrass();

	private Water water = new Water();

	// Shadow declarations
	private int scSizeX, scSizeY;
	private int [] shadowTex = new int[1];
	private int [] shadowBuffer = new int[1];
	private Matrix4f lightVmat = new Matrix4f();
	private Matrix4f lightPmat = new Matrix4f();

	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();
	private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
	private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

	private MobileLight mobileLight;
	private boolean controlMobileLight = false;

	private float depthLookup;
	private int dOffsetLoc;
	private long lastTime = System.currentTimeMillis();

	private int mvLoc, projLoc, nLoc, sLoc, vLoc, mvpLoc;
	private float aspect;

	private Light mainLight;

	private int redTexture, greenTexture, blueTexture, skyboxTexture,
		woodTexture, groundTexture, squareMoonTexture, squareMoonHeight, squareMoonNormalMap, noiseTexture;

	private boolean drawAxes = true;
	private Camera camera;
	private GL4 gl;
	private Cube cube = new Cube();


	private ArrayList<SceneObject> sceneObjects = new ArrayList<>();

	public Starter() {
		setTitle("CSC 155 - a2");
		setSize(800, 800);
		camera = new Camera(0.0f, 3.0f, 10.0f);

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
		//this.add(makeBottomBar(), BorderLayout.SOUTH);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	// Display method for the GLSL canvas
	public void display(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		vMat = camera.getView();

		// NOT DISPLAYING - NO IDEA WHY



		drawSkyBox();

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);


		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_POLYGON_OFFSET_FILL);	//  for reducing
		gl.glPolygonOffset(3.0f, 5.0f);		//  shadow artifacts

		lightVmat.identity().setLookAt(mainLight.getLightPos(), origin, up);	// vector from light to origin
		lightPmat.identity().setPerspective((float) Math.toRadians(120.0f), aspect, 0.1f, 1000.0f);
		shadowPass(mainLight);
		if(controlMobileLight){
			lightVmat.identity().setLookAt(mobileLight.getLightPos(), origin, up);	// vector from light to origin
			lightPmat.identity().setPerspective((float) Math.toRadians(120.0f), aspect, 0.1f, 1000.0f);
			shadowPass(mobileLight);
		}

		gl = (GL4) GLContext.getCurrentGL();

		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued

		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);

		gl.glDrawBuffer(GL_FRONT);

		if(drawAxes){
			drawAxes();
		}

		mainPass();

	}

	private void shadowPass(Light light){
		gl = (GL4) GLContext.getCurrentGL();
		sLoc = gl.glGetUniformLocation(shadowProgram, "shadowMVP");
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		gl.glUseProgram(shadowProgram);

		drawSceneObjectShadow(light.getLightObject());
		for (int i = 0; i < sceneObjects.size(); i++) {
			drawSceneObjectShadow(sceneObjects.get(i));
		}
	}

	private void drawSceneObjectShadow(SceneObject object){
		gl = (GL4) GLContext.getCurrentGL();

		mMat.identity();
		mMat.translate(object.getPosition().x, object.getPosition().y, object.getPosition().z);
		mMat.scale(object.getScale());

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

		int[] vbo = object.getVBO();


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		//gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[4]);
		gl.glDrawArrays(GL_TRIANGLES, 0, object.getNumVerts());
	}

	private void mainPass(){
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		vMat = camera.getView();
		drawTerrain(sceneObjects.get(0));
		renderWater();
		gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(mainProgram);

		mvLoc = gl.glGetUniformLocation(mainProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(mainProgram, "proj_matrix");
		nLoc = gl.glGetUniformLocation(mainProgram, "norm_matrix");
		sLoc = gl.glGetUniformLocation(mainProgram, "shadowMVP");


		for (int i = 1; i < sceneObjects.size(); i++) {
			drawSceneObject(sceneObjects.get(i));
		}
		if(controlMobileLight) {
			drawSceneObject(mobileLight.getLightObject());
		}
		else{
			drawSceneObject(mainLight.getLightObject());
		}

	}

	private void drawTerrain(SceneObject object){
		gl = (GL4) GLContext.getCurrentGL();

		mvpLoc = gl.glGetUniformLocation(terrainProgram, "mvp");
		mvLoc = gl.glGetUniformLocation(terrainProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(terrainProgram, "proj_matrix");
		nLoc = gl.glGetUniformLocation(terrainProgram, "norm_matrix");

		gl.glUseProgram(terrainProgram);

		mMat.identity();
		mMat.translate(object.getPosition().x, object.getPosition().y, object.getPosition().z);
		mMat.scale(object.getScale());

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);

		mvpMat.identity();
		mvpMat.mul(pMat);
		mvpMat.mul(vMat);
		mvpMat.mul(mMat);


		mainLight.installLights(terrainProgram, vMat, object.getMaterial());

		if(controlMobileLight){
			mobileLight.installLights(terrainProgram, vMat, object.getMaterial());
		}

		gl.glUniformMatrix4fv(mvpLoc, 1, false, mvpMat.get(vals));
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, squareMoonTexture);
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, squareMoonHeight);
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, squareMoonNormalMap);

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);

		gl.glPatchParameteri(GL_PATCH_VERTICES, 4);
		gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		gl.glDrawArraysInstanced(GL_PATCHES, 0, 4, 64*64);
	}

	// Draws a SceneObject using its position and scale
	private void drawSceneObject(SceneObject object){

		mMat.identity();
		mMat.translate(object.getPosition().x, object.getPosition().y, object.getPosition().z);
		mMat.scale(object.getScale());

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);


		mainLight.installLights(mainProgram, vMat, object.getMaterial());

		if(controlMobileLight){
			mobileLight.installLights(mainProgram, vMat, object.getMaterial());
		}
		gl = (GL4) GLContext.getCurrentGL();
		int[] vbo = object.getVBO();

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		int toMap = object.mapType();
		int mapLoc = gl.glGetUniformLocation(mainProgram, "map");
		gl.glUniform1i(mapLoc, toMap);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		bindTexture(object.getTexture());
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		//gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboSphere[3]);
		gl.glDrawArrays(GL_TRIANGLES, 0, object.getNumVerts());
	}

	private void drawSkyBox(){
		gl.glUseProgram(skyBoxProgram);

		vMat = camera.getView();

		vLoc = gl.glGetUniformLocation(skyBoxProgram, "v_matrix");
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));

		projLoc = gl.glGetUniformLocation(skyBoxProgram, "proj_matrix");
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSkyBox[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	     // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);
	}

	private void renderWater()
	{	gl = (GL4) GLContext.getCurrentGL();

		long currentTime = System.currentTimeMillis();
		long elapsedTime = currentTime - lastTime;
		lastTime = currentTime;

		depthLookup += (float)elapsedTime * .0001f;

		gl.glUseProgram(waterProgram);

		mvLoc = gl.glGetUniformLocation(waterProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(waterProgram, "proj_matrix");
		nLoc = gl.glGetUniformLocation(waterProgram, "norm_matrix");
		int aboveLoc = gl.glGetUniformLocation(waterProgram, "isAbove");
		int dOffsetLoc = gl.glGetUniformLocation(waterProgram, "depthOffset");

		Vector3f waterPos = water.getPosition();

		mMat.translation(waterPos.x(), waterPos.y(), waterPos.z());

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		mainLight.installLights(waterProgram, vMat, defaultMaterial);


		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		if (camera.getLoc().y() > waterPos.y())
			gl.glUniform1i(aboveLoc, 0);
		else
			gl.glUniform1i(aboveLoc, 1);

		gl.glUniform1f(dOffsetLoc, depthLookup);

		int[] vbo = water.getVBO();

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, water.getReflectTextureId());
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, water.getRefractTextureId());
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_3D, noiseTexture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glFrontFace(GL_CW);
		gl.glDrawArrays(GL_TRIANGLES, 0, 6);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, 6);
	}

	private void drawAxes(){
		gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(axisProgram);

		mvLoc = gl.glGetUniformLocation(axisProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(axisProgram, "proj_matrix");

		mMat.identity();
		mMat.translate(0.0f, 0.0f, 0.0f);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glDrawArrays(GL_LINES, 0, 6);
	}

	// Outputs versions, creates and links shaders and textures
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();

		System.out.println("OpenGL Version: " + gl.glGetString(gl.GL_VERSION));
		System.out.println("JOGL Version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion());
		System.out.println("Java Version: " + System.getProperty("java.version"));

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		shadowProgram = ShaderTools.createShaderProgram("\\Shaders\\Shadow\\shadowvert.glsl", "\\Shaders\\Shadow\\shadowfrag.glsl");
		mainProgram = ShaderTools.createShaderProgram("\\Shaders\\Main\\mainvert.glsl", "\\Shaders\\Main\\mainfrag.glsl");
		skyBoxProgram = ShaderTools.createShaderProgram("\\Shaders\\Skybox\\skyvert.glsl", "\\Shaders\\Skybox\\skyfrag.glsl");
		axisProgram = ShaderTools.createShaderProgram("\\Shaders\\Axis\\axisvert.glsl", "\\Shaders\\Axis\\axisfrag.glsl");
		terrainProgram = ShaderTools.createShaderProgram("\\Shaders\\Terrain\\vertShader.glsl", "\\Shaders\\Terrain\\tessCShader.glsl", "\\Shaders\\Terrain\\tessEShader.glsl", "\\Shaders\\Terrain\\fragShader.glsl");
		waterProgram = ShaderTools.createShaderProgram("\\Shaders\\Water\\vertshader.glsl", "\\Shaders\\Water\\fragshader.glsl");

		redTexture = ShaderTools.loadTexture("\\textures\\red.jpg");
		greenTexture = ShaderTools.loadTexture("\\textures\\green.jpg");
		blueTexture = ShaderTools.loadTexture("\\textures\\blue.jpg");
		skyboxTexture = ShaderTools.loadCubeMap("cubeMap");

		squareMoonTexture = ShaderTools.loadTexture("\\textures\\squareMoonMap.jpg");
		squareMoonHeight = ShaderTools.loadTexture("\\textures\\squareMoonBump.jpg");
		squareMoonNormalMap = ShaderTools.loadTexture("\\textures\\squareMoonNormal.jpg");

		b.set(
				0.5f, 0.0f, 0.0f, 0.0f,
				0.0f, 0.5f, 0.0f, 0.0f,
				0.0f, 0.0f, 0.5f, 0.0f,
				0.5f, 0.5f, 0.5f, 1.0f);

		// bark texture from: https://freestocktextures.com/texture/forest-nature-bark,17.html
		// This site uses the Creative Commons Zero license, meaning their textures are free to use.
		// Link: https://freestocktextures.com/license/
		woodTexture = ShaderTools.loadTexture("\\textures\\bark.jpg");
		// ground texture from same website.
		// link: https://freestocktextures.com/texture/nature-ground-weed,101.html
		groundTexture = ShaderTools.loadTexture("\\textures\\ground.jpg");

		setupVertices();
		setupShadowBuffers();
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

		ImportedObject temp = new ImportedObject("\\OBJ_files\\sphere.obj");
		mainLight = new Light(new SceneObject(temp, blueTexture, defaultMaterial, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)));
		mobileLight = new MobileLight(camera.getView(),
				new SceneObject(temp, blueTexture, defaultMaterial, new Vector4f(0.0f, 5.0f, 0.0f, 1.0f)));

		myCanvas.addMouseMotionListener(mobileLight);
		myCanvas.addMouseWheelListener(mobileLight);
		mobileLight.setWindowSize(myCanvas.getWidth(), myCanvas.getHeight());

	}

	private void addNewSceneObject(ImportedObject obj, Vector4f pos, float scale, int texture, Material mat){
		SceneObject temp = new SceneObject(obj, texture, mat, pos);
		temp.setScale(scale);
		sceneObjects.add(temp);
	}

	private void setupVertices() {
		gl = (GL4) GLContext.getCurrentGL();
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		water.setupVertices();

		water.createReflectRefractBuffers(myCanvas.getWidth(), myCanvas.getHeight());

		noiseTexture = water.buildNoiseTexture();

		ImportedObject pine = new ImportedObject("\\OBJ_files\\PineTree1.obj");
		ImportedObject pineLeaves = new ImportedObject("\\OBJ_files\\PineTree1_Leaves.obj");
		ImportedObject terrain = new ImportedObject("\\OBJ_files\\flat_plane.obj");

		addNewSceneObject(terrain, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), 50.0f, groundTexture, defaultMaterial);

		addNewSceneObject(pine, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), 0.5f, woodTexture, defaultMaterial);
		sceneObjects.get(sceneObjects.size() - 1).applyMapping(0);
		addNewSceneObject(pineLeaves, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), 0.5f, greenTexture, darkGrassMaterial);

		addNewSceneObject(pine, new Vector4f(-3.0f, 0.0f, 1.0f, 1.0f), 0.7f, woodTexture, defaultMaterial);
		sceneObjects.get(sceneObjects.size() - 1).applyMapping(0);
		addNewSceneObject(pineLeaves, new Vector4f(-3.0f, 0.0f, 1.0f, 1.0f), 0.7f, greenTexture, darkGrassMaterial);

		addNewSceneObject(pine, new Vector4f(5.0f, 0.0f, -3.0f, 1.0f), 0.65f, woodTexture, defaultMaterial);
		sceneObjects.get(sceneObjects.size() - 1).applyMapping(0);
		addNewSceneObject(pineLeaves, new Vector4f(5.0f, 0.0f, -3.0f, 1.0f), 0.65f, greenTexture, darkGrassMaterial);

		addNewSceneObject(pine, new Vector4f(1.0f, 0.0f, 5.0f, 1.0f), 0.6f, woodTexture, defaultMaterial);
		sceneObjects.get(sceneObjects.size() - 1).applyMapping(0);
		addNewSceneObject(pineLeaves, new Vector4f(1.0f, 0.0f, 5.0f, 1.0f), 0.6f, greenTexture, darkGrassMaterial);



		for(int i = 0; i < sceneObjects.size(); i++){
			sceneObjects.get(i).setupVBO();
		}


		setupSkyBox();

		//setupDiamond();
		//setupSphere();
	}

	private void setupShadowBuffers()
	{	gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();

		gl.glGenFramebuffers(1, shadowBuffer, 0);

		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
				scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	private void setupSkyBox(){

		gl.glGenBuffers(vboSkyBox.length, vboSkyBox, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboSkyBox[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(cube.getPositions());
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
	}

	public static void main(String[] args) { new Starter(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

		setupShadowBuffers();
	}
	public void dispose(GLAutoDrawable drawable) {}

	private void bindTexture(int tex){
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, tex);

		//mip-mapping
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);

		// Anisotropic filtering
		if(gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")){
			float[] anisoSetting = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoSetting, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoSetting[0]);
		}
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

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), KeyEvent.VK_R);
		amap.put(KeyEvent.VK_R, new ResetCamera(camera));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyEvent.VK_SPACE);
		amap.put(KeyEvent.VK_SPACE, new ToggleAxes(this));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), KeyEvent.VK_C);
		amap.put(KeyEvent.VK_C, new ToggleLight(this));
	}

	public void toggleAxes(){
		drawAxes = !drawAxes;
	}

	public void toggleMobileLight(){controlMobileLight = !controlMobileLight; mobileLight.toggleMobileLight();}
}