package com.gearvrf.fasteater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTransform;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;
import org.gearvrf.utility.Log;

import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;

import org.siprop.bullet.Bullet;
import org.siprop.bullet.Geometry;
import org.siprop.bullet.MotionState;
import org.siprop.bullet.RigidBody;
import org.siprop.bullet.Transform;
import org.siprop.bullet.shape.BoxShape;
import org.siprop.bullet.util.Point3;
import org.siprop.bullet.util.Vector3;

public class FEViewManager extends GVRScript {
	private static final String TAG = Log.tag(FEViewManager.class);
	private GVRAnimationEngine mAnimationEngine;
	private GVRScene mMainScene;
	private GVRContext mGVRContext;
	private Random random;
	private GVRSceneObject mainSceneObject, headTracker, astronautMeshObject;
	private GVRTextViewSceneObject textMessageObject;
	private GVRSceneObject burger;
	private List<GVRSceneObject> mObjects = new ArrayList<GVRSceneObject>();
    private Bullet mBullet = null;
    private static final float OBJECT_MASS = 0.5f;
    private RigidBody boxBody;
    private Boolean gameStart = false;
    private Map<RigidBody, GVRSceneObject> rigidBodiesSceneMap = new HashMap<RigidBody, GVRSceneObject>();

	private GVRSceneObject asyncSceneObject(GVRContext context, String meshName, String textureName)
			throws IOException {
		return new GVRSceneObject(context, //
				new GVRAndroidResource(context, meshName), new GVRAndroidResource(context, textureName));
	}

	private int randomInRange(int min, int max) {
		return random.nextInt((max - min) + 1) + min;
	}

	@Override
	public void onInit(GVRContext gvrContext) throws IOException, InterruptedException {
		mGVRContext = gvrContext;
		mAnimationEngine = mGVRContext.getAnimationEngine();
		mMainScene = mGVRContext.getNextMainScene();
		mMainScene.setFrustumCulling(true);
		random = new Random();

		// load all audio files. TODO: change this to spacial Audio
		AudioClip.getInstance(mGVRContext.getContext());

		mainSceneObject = new GVRSceneObject(mGVRContext);
		mMainScene.addSceneObject(mainSceneObject);
		mMainScene.getMainCameraRig().getTransform().setPosition(0.0f, 6.0f, 1.0f);

		// add space
		GVRSceneObject spaceMeshObject = asyncSceneObject(mGVRContext, "space_sphere.obj", "clouds.png");
		spaceMeshObject.getTransform().setScale(200, 200, 200);
		mainSceneObject.addChildObject(spaceMeshObject);

		// add head-tracking pointer
		/*headTracker = new GVRSceneObject(mGVRContext, new FutureWrapper<GVRMesh>(mGVRContext.createQuad(0.05f, 0.05f)),
				mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "headtrackingpointer.png")));
		headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
		headTracker.getRenderData().setDepthTest(false);
		headTracker.getRenderData().setRenderingOrder(100000);
		mMainScene.getMainCameraRig().addChildObject(headTracker);*/
		
		//add scoreBoard

		//add character
		/*astronautMeshObject = asyncSceneObject(mGVRContext, "astronaut_default.obj", "astronaut_default.png");
		astronautMeshObject.getTransform().rotateByAxis(180, 0, 1, 0);
		astronautMeshObject.getTransform().setPosition(0.0f, 0.0f, -5.0f);
		mMainScene.getMainCameraRig().addChildObject(astronautMeshObject);*/

        /*mBullet = new Bullet();

        mBullet.createPhysicsWorld(new Vector3(-480.0f, -480.0f, -480.0f),
                new Vector3(480.0f, 480.0f, 480.0f), 1024, new Vector3(0.0f,
                        -9.8f, 0.0f));*/

        GVRSceneObject groundScene = quadWithTexture(300.0f, 300.0f, "floor.jpg");
        groundScene.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
        groundScene.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        mainSceneObject.addChildObject(groundScene);

        /*StaticPlaneShape floorShape = new StaticPlaneShape(new Vector3(0.0f,
                1.0f, 0.0f), 0.0f);
        Geometry floorGeometry = mBullet.createGeometry(floorShape, 0.0f,
                new Vector3(0.0f, 0.0f, 0.0f));
        MotionState floorState = new MotionState();
        mBullet.createAndAddRigidBody(floorGeometry, floorState);*/

		//setDisplayMessage("Welcome to FastEater", 2, 1, Color.BLACK, 10);

        /*FETimerTask task = new FETimerTask();
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(task, 0, 10 * 1000);*/

	}

    /*public class FETimerTask extends TimerTask {

        @Override
        public void run() {

        }
    }*/

	private GVRSceneObject quadWithTexture(float width, float height, String texture) {
		FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(mGVRContext.createQuad(width, height));
		GVRSceneObject object = null;
		try {
			object = new GVRSceneObject(mGVRContext, futureMesh,
					mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, texture)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}

    public void _throwObject()
    {
        Timer timer = new Timer();
        TimerTask gameOver = new TimerTask()
        {
            public void run() {
                try {
                    GVRSceneObject object = asyncSceneObject(mGVRContext, "cube.obj", "cube.jpg");
                    throwAnObject(object);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        long twoSec = 2 * 1000;
        timer.schedule(gameOver, twoSec);
    }

    public void gameOver()
    {
        gameStart = false;
    }

	private void setDisplayMessage(String str, float width, float height, int color, int textSize) {
		textMessageObject = new GVRTextViewSceneObject(mGVRContext, width, height, str);
		textMessageObject.setTextColor(color);
		textMessageObject.setGravity(Gravity.CENTER);
		textMessageObject.setKeepWrapper(true);
		textMessageObject.setTextSize(textSize);
		textMessageObject.setBackgroundColor(Color.TRANSPARENT);
		textMessageObject.setRefreshFrequency(IntervalFrequency.HIGH);
		textMessageObject.getTransform().setPosition(0.0f, 6.0f, -6.0f);
		textMessageObject.getTransform().rotateByAxisWithPivot(0, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);

		GVRRenderData renderData = textMessageObject.getRenderData();
		renderData.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
		renderData.setDepthTest(false);
		
		mainSceneObject.addChildObject(textMessageObject);
	}
	
	private void throwAnObjectPhysics() throws IOException {
		/*burger = asyncSceneObject(mGVRContext, "sphere.obj", "space7.jpg");
		burger.getTransform().setPosition(2, 6, -25);
		mainSceneObject.addChildObject(burger);*/


        BoxShape boxShape = new BoxShape(new Vector3(0.5f, 0.5f, 0.5f));
        Geometry boxGeometry = mBullet.createGeometry(boxShape, OBJECT_MASS,
                new Vector3(0.0f, 0.0f, 0.0f));
        MotionState boxState = new MotionState();
        boxState.worldTransform = new Transform(new Point3(1, 2, -11));
        boxBody = mBullet.createAndAddRigidBody(boxGeometry, boxState);

        burger = asyncSceneObject(mGVRContext, "cube.obj", "cube.jpg");

        burger.getTransform().setPosition(2, 6, -25);
        mainSceneObject.addChildObject(burger);
        rigidBodiesSceneMap.put(boxBody, burger);

	}


    public void throwAnObject(GVRSceneObject object) throws IOException {
		object.getTransform().setPosition(2, 6, -25);
		mainSceneObject.addChildObject(object);

        relativeMotionAnimation(object, 4, 0, 0, -(object.getTransform().getPositionZ() + 2));
    }

	@Override
	public void onStep() {
        /*mBullet.doSimulation(1.0f / 60.0f, 10);
        for (RigidBody body : rigidBodiesSceneMap.keySet()) {
            if (body.geometry.shape.getType() == ShapeType.SPHERE_SHAPE_PROXYTYPE
                    || body.geometry.shape.getType() == ShapeType.BOX_SHAPE_PROXYTYPE) {
                rigidBodiesSceneMap
                        .get(body)
                        .getTransform()
                        .setPosition(
                                body.motionState.resultSimulation.originPoint.x,
                                body.motionState.resultSimulation.originPoint.y,
                                body.motionState.resultSimulation.originPoint.z);
            }
        }*/

		/*mMainScene.getMainCameraRig()
		.getTransform()
		.setPosition(getXLinearDistance(
				mMainScene.getMainCameraRig().getHeadTransform().getRotationYaw()), 
				mMainScene.getMainCameraRig().getTransform().getPositionY(), 
				mMainScene.getMainCameraRig().getTransform().getPositionZ());*/
		/*Log.d(TAG, "Yaw: %f, %f", mMainScene.getMainCameraRig().getTransform().getPositionY(), 
				mMainScene.getMainCameraRig().getHeadTransform().getRotationYaw());*/

	}
	
	private float minLinearX = -5.0f;
	private float maxLinearX = 5.0f;
	private float yawToLinearScale = 0.25f;
	
	private float getXLinearDistance(float headRotationYaw) {
		float val = headRotationYaw * yawToLinearScale;

		if(val < minLinearX) 		return -minLinearX;
		else if(val > maxLinearX)	return -maxLinearX;
		else						return -val;
	}

	private void run(GVRAnimation animation) {
		animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1).start(mAnimationEngine);
	}

	private void runOnce(GVRAnimation animation) {
		animation.setRepeatMode(GVRRepeatMode.ONCE).setRepeatCount(-1).start(mAnimationEngine);
	}

	private GVRSceneObject attachedObject = null;
	private float lastX = 0, lastY = 0;
	private boolean isOnClick = false;

	public void onTouchEvent(MotionEvent event) throws IOException {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			lastX = event.getX();
			lastY = event.getY();
			isOnClick = true;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (isOnClick) {
                gameStart = true;
                _throwObject();
                /*mBullet.applyForce(boxBody, new Vector3(randomInRange(-50,50),randomInRange(-50,50),randomInRange(-50,100)), new Vector3(randomInRange(-50,100),randomInRange(0,90),randomInRange(-25,-1)));
                mBullet.applyImpulse(boxBody, new Vector3(randomInRange(-50,50),randomInRange(-50,50),randomInRange(-50,100)), new Vector3(randomInRange(-50,100),randomInRange(0,90),randomInRange(-25,-1)));
                mBullet.applyCentralImpulse(boxBody, new Vector3(randomInRange(-50,50),randomInRange(-50,50),randomInRange(-50,100)));*/
				/*relativeMotionAnimation(burger, 4,
						0,
						0,
						-(burger.getTransform().getPositionZ() + 2));*/
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		default:
			break;
		}
	}



	private void counterClockwise(GVRSceneObject object, float duration) {
		run(new GVRRotationByAxisWithPivotAnimation(object, duration, 360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	private void clockwise(GVRSceneObject object, float duration) {
		run(new GVRRotationByAxisWithPivotAnimation(object, duration, -360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	private void clockwise(GVRTransform transform, float duration) {
		run(new GVRRotationByAxisWithPivotAnimation(transform, duration, -360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	private void clockwiseOnZ(GVRSceneObject object, float duration) {
		runOnce(new GVRRotationByAxisAnimation(object, duration, -360.0f, 0.0f, 0.0f, 1.0f));
	}

	private void scaleAnimation(GVRSceneObject object, float duration, float x, float y, float z) {
		runOnce(new GVRScaleAnimation(object, duration, x, y, z));
	}

	private void startSpaceShip(GVRSceneObject object, float duration) {

	}
	
	private void relativeMotionAnimation(GVRSceneObject object, float duration, float x, float y, float z) {
		runOnce(new GVRRelativeMotionAnimation(object, duration, x, y, z));
	}

	private void attachDefaultEyePointee(GVRSceneObject sceneObject) {
		sceneObject.attachEyePointeeHolder();
	}

}
