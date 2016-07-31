package com.gearvrf.fasteater;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
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
import org.siprop.bullet.Bullet;
import org.siprop.bullet.Geometry;
import org.siprop.bullet.MotionState;
import org.siprop.bullet.RigidBody;
import org.siprop.bullet.Transform;
import org.siprop.bullet.shape.BoxShape;
import org.siprop.bullet.shape.StaticPlaneShape;
import org.siprop.bullet.util.Point3;
import org.siprop.bullet.util.Vector3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FEViewManager extends GVRScript {
	private static final String TAG = Log.tag(FEViewManager.class);
	private GVRAnimationEngine mAnimationEngine;
	private GVRScene mMainScene;
	private GVRContext mGVRContext;
	private GVRSceneObject mainSceneObject, headTracker, astronautMeshObject;
	private GVRTextViewSceneObject textMessageObject;
	private GVRSceneObject burger;
	private List<GVRSceneObject> mObjects = new ArrayList<GVRSceneObject>();
    private Bullet mBullet = null;
    private static final float OBJECT_MASS = 0.5f;
    private RigidBody boxBody;
    private Boolean gameStart = false;
    private Map<RigidBody, GVRSceneObject> rigidBodiesSceneMap = new HashMap<RigidBody, GVRSceneObject>();
    private Timer timer;
    private GameStateMachine gameState;

	private GVRSceneObject asyncSceneObject(GVRContext context, String meshName, String textureName)
			throws IOException {
		return new GVRSceneObject(context, //
				new GVRAndroidResource(context, meshName), new GVRAndroidResource(context, textureName));
	}

	@Override
	public void onInit(GVRContext gvrContext) throws IOException, InterruptedException {
        gameState = new GameStateMachine();
        gameState.setStatus(GameStateMachine.GameStatus.STATE_BOOT_ANIMATION);

		mGVRContext = gvrContext;
		mAnimationEngine = mGVRContext.getAnimationEngine();
		mMainScene = mGVRContext.getNextMainScene();
		mMainScene.setFrustumCulling(true);

        loadGameScene(mGVRContext, mMainScene);
	}

    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                gvrContext.getContext().getResources(),
                R.drawable.boot_screen);
        //Bitmap scaledBitmap = bitmap.createScaledBitmap(bitmap, 2, 2, true);
        return new GVRBitmapTexture(gvrContext, bitmap);
    }

    private void loadGameScene(GVRContext context, GVRScene scene) throws IOException {
        gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_LOAD);
        // load all audio files. TODO: change this to spacial Audio
        AudioClip.getInstance(context.getContext());

        mainSceneObject = new GVRSceneObject(context);
        mMainScene.addSceneObject(mainSceneObject);
        mMainScene.getMainCameraRig().getTransform().setPosition(0.0f, 6.0f, 8.0f);

        GVRMesh mesh = context.loadMesh(new GVRAndroidResource(context,
                "space_sphere.obj"));

        GVRSceneObject leftScreen = new GVRSceneObject(context, mesh,
                context.loadTexture(new GVRAndroidResource(context,
                        "city_domemap_left.png")));
        leftScreen.getTransform().setScale(200,200,200);
        GVRSceneObject rightScreen = new GVRSceneObject(context, mesh,
                context.loadTexture(new GVRAndroidResource(context,
                        "city_domemap_right.png")));
        rightScreen.getTransform().setScale(200,200,200);

        mainSceneObject.addChildObject(leftScreen);
        mainSceneObject.addChildObject(rightScreen);

        // add head-tracking pointer
        headTracker = new GVRSceneObject(context, new FutureWrapper<GVRMesh>(context.createQuad(0.5f, 0.5f)),
                context.loadFutureTexture(new GVRAndroidResource(context, "mouth_open.png")));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -2.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mMainScene.getMainCameraRig().addChildObject(headTracker);

        /*mBullet = new Bullet();
        mBullet.createPhysicsWorld(new Vector3(-480.0f, -480.0f, -480.0f),
                new Vector3(480.0f, 480.0f, 480.0f), 1024, new Vector3(0.0f,
                        -9.8f, 0.0f));

        StaticPlaneShape floorShape = new StaticPlaneShape(new Vector3(0.0f,
                1.0f, 0.0f), 0.0f);
        Geometry floorGeometry = mBullet.createGeometry(floorShape, 0.0f,
                new Vector3(0.0f, 0.0f, 0.0f));
        MotionState floorState = new MotionState();
        mBullet.createAndAddRigidBody(floorGeometry, floorState);*/

        //setDisplayMessage("Welcome to FastEater", 2, 1, Color.BLACK, 10);

        /*FETimerTask task = new FETimerTask();
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(task, 0, 10 * 1000);*/
        _throwObject();
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

    private void _throwObject()
    {
        Timer timer = new Timer();
        TimerTask gameOver = new TimerTask()
        {
            public void run() {
                try {
                    int num_throw = Helper.randomNextInt(MAX_THROW);
                    for(int i = 0; i < num_throw; i++) {
                        throwAnObject();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        int THROW_OBJECT_RATE_MIN = 2 * 1000;
        int THROW_OBJECT_RATE_MAX = 4 * 1000;
        int THROW_OBJECT_DELAY_MIN = 2 * 1000;
        int THROW_OBJECT_DELAY_MAX = 4 * 1000;
        timer.scheduleAtFixedRate(gameOver,
                Helper.randomInRange(THROW_OBJECT_DELAY_MIN, THROW_OBJECT_DELAY_MAX),
                Helper.randomInRange(THROW_OBJECT_RATE_MIN, THROW_OBJECT_RATE_MAX));
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

    private int MIN_GAME_WIDTH = -10;
    private int MAX_GAME_WIDTH = 10;
    private int MIN_GAME_HEIGHT_START = 5;
    private int MAX_GAME_HEIGHT_START = 7;
    private int MIN_GAME_HEIGHT_REACH = 5;
    private int MAX_GAME_HEIGHT_REACH = 7;
    private int MIN_SPEED = 2;
    private int MAX_SPEED = 0;

    private String[][] OverEatObjects = new String[][]{
            { "hotdog.obj", "hotdog.png" },
            { "hamburger.obj", "hamburger.png" },
            { "bomb.obj", "bomb.png" },
            { "sodacan.obj", "sodacan.png" },
    };

    private int MAX_THROW = 5;

    public void throwAnObject() throws IOException {
        int rand_index = Helper.randomNextInt(OverEatObjects.length);
        GVRSceneObject object = asyncSceneObject(mGVRContext, OverEatObjects[rand_index][0], OverEatObjects[rand_index][1]);
		object.getTransform().setPosition(
                Helper.randomInRangeFloat(MIN_GAME_WIDTH, MAX_GAME_WIDTH),
                Helper.randomInRangeFloat(MIN_GAME_HEIGHT_START, MAX_GAME_HEIGHT_START),
                -20);
		mainSceneObject.addChildObject(object);
        mObjects.add(object);

        relativeMotionAnimation(object,
                Helper.randomInRange(MIN_SPEED, MAX_SPEED),
                0,
                0,
                -(object.getTransform().getPositionZ() - 10));
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

        for (int i = 0; i < mObjects.size(); i++) {
            if(mObjects.get(i) != null && mObjects.get(i).getRenderData().getMesh() != null) {
                if (mObjects.get(i).isColliding(headTracker) ||
                        (mObjects.get(i).getTransform().getPositionZ() >
                                mMainScene.getMainCameraRig().getTransform().getPositionZ())) {
                    mainSceneObject.removeChildObject(mObjects.get(i));
                    mObjects.remove(i);
                    try {
                        headTracker.getRenderData().getMaterial().setMainTexture(
                                mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "mouth_close.png")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        //mBullet.applyCentralImpulse();
        //mBullet.setactive - true

		mMainScene.getMainCameraRig()
		.getTransform()
		.setPosition(getXLinearDistance(
				mMainScene.getMainCameraRig().getHeadTransform().getRotationRoll()),
				mMainScene.getMainCameraRig().getTransform().getPositionY(), 
				mMainScene.getMainCameraRig().getTransform().getPositionZ());
		//Log.d(TAG, "Roll: %f", mMainScene.getMainCameraRig().getHeadTransform().getRotationRoll());

	}
	
	private float minLinearX = -12.0f;
	private float maxLinearX = 12.0f;
	private float yawToLinearScale = 0.15f;

	private float getXLinearDistance(float headRotationRoll) {
		float val = headRotationRoll * yawToLinearScale;

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
                //_throwObject();
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
