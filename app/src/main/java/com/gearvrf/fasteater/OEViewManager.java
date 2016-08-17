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
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.ZipLoader;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;
import org.gearvrf.utility.Log;
import org.siprop.bullet.Bullet;
import org.siprop.bullet.RigidBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

public class OEViewManager extends GVRScript {
	private static final String TAG = Log.tag(OEViewManager.class);

	private GVRScene mMainScene;
	private GVRContext mGVRContext;
	private GVRSceneObject mainSceneObject, headTracker;
	private GVRTextViewSceneObject textMessageObject, scoreTextMessageObject, livesTextMessageObject, tapTOStart;
	private GVRSceneObject burger;
	private List<FlyingItem> mObjects = new ArrayList<FlyingItem>();
    private Bullet mBullet = null;
    private static final float OBJECT_MASS = 0.5f;
    private RigidBody boxBody;
    private Boolean gameStart = false;
    private Map<RigidBody, GVRSceneObject> rigidBodiesSceneMap = new HashMap<RigidBody, GVRSceneObject>();
    private GameStateMachine gameState;
    private OEAnimation animator;
    private Player ovrEater;
    private Boolean isBGAudioOnce = false;
    private Timer timer;
    private List<Future<GVRTexture>> explodeTextures, splatTextures;
    private long prevTime;

    private GVRSceneObject asyncSceneObject(GVRContext context, String meshName, String textureName)
			throws IOException {
		return new GVRSceneObject(context,
				new GVRAndroidResource(context, meshName),
                new GVRAndroidResource(context, textureName));
	}

	@Override
	public void onInit(GVRContext gvrContext) throws IOException, InterruptedException {
		mGVRContext = gvrContext;
        animator = new OEAnimation(mGVRContext);
		mMainScene = mGVRContext.getNextMainScene();
		mMainScene.setFrustumCulling(true);

        loadGameScene(mGVRContext, mMainScene);
	}

    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        //TODO: can't prolong stay time of bootscreen
        Bitmap bitmap = BitmapFactory.decodeResource(
                gvrContext.getContext().getResources(),
                R.drawable.boot_screen);
        return new GVRBitmapTexture(gvrContext, bitmap);
    }

    private List<Future<GVRTexture>> preloadAnimationTextures(String assetName) {
        List<Future<GVRTexture>> animationTextures;
        try {
            animationTextures = ZipLoader.load(mGVRContext,
                    assetName, new ZipLoader.ZipEntryProcessor<Future<GVRTexture>>() {
                        @Override
                        public Future<GVRTexture> getItem(GVRContext context, GVRAndroidResource
                                resource) {
                            return context.loadFutureTexture(resource);
                        }
                    });
            return animationTextures;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadGameScene(GVRContext context, GVRScene scene) throws IOException {
        gameState = new GameStateMachine();
        gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_LOAD);

        // load all audio files. TODO: change this to spacial Audio
        AudioClip.getInstance(context.getContext());
        ovrEater = new Player();
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

        explodeTextures = preloadAnimationTextures("explode.zip");
        splatTextures = preloadAnimationTextures("splat.zip");

        tapTOStart = setInfoMessage("Tap to start");
        mainSceneObject.addChildObject(tapTOStart);
    }

    private GVRTextViewSceneObject setInfoMessage(String str)
    {
        GVRTextViewSceneObject textMessageObject = new GVRTextViewSceneObject(mGVRContext, 4, 2, str);
        textMessageObject.setTextColor(Color.BLUE);
        textMessageObject.setGravity(Gravity.CENTER);
        textMessageObject.setKeepWrapper(true);
        textMessageObject.setTextSize(15);
        textMessageObject.setBackgroundColor(Color.TRANSPARENT);
        textMessageObject.setRefreshFrequency(IntervalFrequency.HIGH);
        textMessageObject.getTransform().setPosition(-2.0f, 6.0f, -6.0f);
        textMessageObject.getTransform().rotateByAxisWithPivot(0, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        GVRRenderData renderData = textMessageObject.getRenderData();
        renderData.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        renderData.setDepthTest(false);

        return textMessageObject;
    }

    private GVRTextViewSceneObject makeScoreboard(GVRContext ctx, GVRSceneObject parent) {
        GVRTextViewSceneObject scoreBoard = new GVRTextViewSceneObject(ctx, 1.5f, 1.0f, "000");

        GVRRenderData rdata = scoreBoard.getRenderData();
        scoreBoard.setTextColor(Color.BLUE);
        scoreBoard.setTextSize(6);
        scoreBoard.setBackgroundColor(Color.TRANSPARENT);
        scoreBoard.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        rdata.setDepthTest(false);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(GVRRenderingOrder.OVERLAY);
        parent.addChildObject(scoreBoard);
        return scoreBoard;
    }

    private GVRTextViewSceneObject makeLivesLeft(GVRContext ctx, GVRSceneObject parent) {
        GVRTextViewSceneObject livesLeft = new GVRTextViewSceneObject(ctx, 5.3f, 1.0f, "Lives: 3");

        GVRRenderData rdata = livesLeft.getRenderData();
        livesLeft.setTextColor(Color.BLUE);
        livesLeft.setTextSize(6);
        livesLeft.setBackgroundColor(Color.TRANSPARENT);
        livesLeft.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        rdata.setDepthTest(false);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(GVRRenderingOrder.OVERLAY);
        parent.addChildObject(livesLeft);
        return livesLeft;
    }

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

    private int MAX_THROW = 3;
    int THROW_OBJECT_RATE_MIN = 3 * 1000;
    int THROW_OBJECT_RATE_MAX = 6 * 1000;
    int THROW_OBJECT_DELAY_MIN = 3 * 1000;
    int THROW_OBJECT_DELAY_MAX = 6 * 1000;
    private int MIN_GAME_WIDTH = -5;
    private int MAX_GAME_WIDTH = 5;
    private int MIN_GAME_HEIGHT_START = 5;
    private int MAX_GAME_HEIGHT_START = 7;
    private int MIN_SPEED = 8; //higher is slower
    private int MAX_SPEED = 6; //0 being fastest

    private void _throwObject()
    {
        timer = new Timer();
        TimerTask task = new TimerTask()
        {
            public void run() {
                try {
                    int num_throw = Helper.randomNextInt(incMaxThrow());
                    for(int i = 0; i < num_throw; i++) {
                        throwAnObject();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        timer.scheduleAtFixedRate(task,
                Helper.randomInRange(THROW_OBJECT_DELAY_MIN, THROW_OBJECT_DELAY_MAX),
                Helper.randomInRange(THROW_OBJECT_RATE_MIN, THROW_OBJECT_RATE_MAX));

    }

    private int incMaxThrow() {
        long currTime = System.currentTimeMillis();
        if(currTime - prevTime > 10000) {
            if(MAX_THROW < 20) MAX_THROW += 2;
            if(MIN_SPEED > 2 || MAX_SPEED > 0) {
                MIN_SPEED -= 1;
                MAX_SPEED -= 1;
            }
        }
        prevTime = currTime;

        return MAX_THROW;
    }

    private void stopThrowingObjects() {
        if(timer != null)
            timer.cancel();
    }

    private String[][] OverEatObjects = new String[][]{
            { "hotdog.obj", "hotdog.png", "hotdog" },
            { "hamburger.obj", "hamburger.png", "hamburger" },
            { "bomb.obj", "bomb.png", "bomb" },
            { "sodacan.obj", "sodacan.png", "sodacan" }
    };

    public void throwAnObject() throws IOException {
        if(!ovrEater.isDead()) {
            int rand_index = Helper.randomNextInt(OverEatObjects.length);
            GVRSceneObject object = asyncSceneObject(mGVRContext, OverEatObjects[rand_index][0], OverEatObjects[rand_index][1]);
            FlyingItem item = new FlyingItem(OverEatObjects[rand_index][2], object);
            object.getTransform().setPosition(
                    Helper.randomInRangeFloat(MIN_GAME_WIDTH, MAX_GAME_WIDTH),
                    Helper.randomInRangeFloat(MIN_GAME_HEIGHT_START, MAX_GAME_HEIGHT_START),
                    -20);
            mainSceneObject.addChildObject(object);
            mObjects.add(item);

            animator.relativeMotionAnimation(object,
                    Helper.randomInRange(MIN_SPEED, MAX_SPEED),
                    0,
                    0,
                    -(object.getTransform().getPositionZ() - 10));
        }
    }

    private void updateObject(int i) {
        try {
            headTracker.getRenderData().getMaterial().setMainTexture(
                    mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "mouth_open.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mObjects.get(i) != null && mObjects.get(i).getSceneObject().getRenderData().getMesh() != null) {
            if (mObjects.get(i).getSceneObject().isColliding(headTracker)) {
                if (mObjects.get(i).getName().compareTo("bomb") == 0) {
                    animateTextures(explodeTextures, mObjects.get(i).getSceneObject(), 1.5f);
                    ovrEater.loseALife();
                    AudioClip.getInstance(mGVRContext.getContext()).
                            playSound(AudioClip.getUISoundGrenadeID(), 1.0f, 1.0f);
                } else if (mObjects.get(i).getName().compareTo("hamburger") == 0) {
                    animateTextures(splatTextures, mObjects.get(i).getSceneObject(), 1.5f);
                    AudioClip.getInstance(mGVRContext.getContext()).
                            playSound(AudioClip.getUISoundEatID(), 1.0f, 1.0f);
                    ovrEater.incrementScore(50);
                } else if (mObjects.get(i).getName().compareTo("hotdog") == 0) {
                    ovrEater.incrementScore(30);
                } else if (mObjects.get(i).getName().compareTo("sodacan") == 0) {
                    AudioClip.getInstance(mGVRContext.getContext()).
                            playSound(AudioClip.getUISoundDrinkID(), 1.0f, 1.0f);
                    ovrEater.incrementScore(10);
                }
                scoreTextMessageObject.setText(String.format("%03d", ovrEater.getCurrentScore()));
                livesTextMessageObject.setText("Lives: " + ovrEater.getNumLivesRemaining());
                mainSceneObject.removeChildObject(mObjects.get(i).getSceneObject());
                mObjects.remove(i);
                try {
                    headTracker.getRenderData().getMaterial().setMainTexture(
                            mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "mouth_close.png")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (mObjects.get(i).getSceneObject().getTransform().getPositionZ() >
                    mMainScene.getMainCameraRig().getTransform().getPositionZ()) {
                mainSceneObject.removeChildObject(mObjects.get(i).getSceneObject());
                mObjects.remove(i);
            }
        }
    }

	@Override
	public void onStep() {
        if(gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS) {
            for (int obj = 0; obj < mObjects.size(); obj++) {
                updateObject(obj);
            }

            mMainScene.getMainCameraRig()
                    .getTransform()
                    .setPosition(getXLinearDistance(
                            mMainScene.getMainCameraRig().getHeadTransform().getRotationRoll()),
                            mMainScene.getMainCameraRig().getTransform().getPositionY(),
                            mMainScene.getMainCameraRig().getTransform().getPositionZ());

            if(ovrEater.isDead()) {
                resetGame();
            }
        }
	}

    private void animateTextures(final List<Future<GVRTexture>> loaderTextures,
                                 final GVRSceneObject object, float duration) {
        GVRSceneObject loadingObject = new GVRSceneObject(mGVRContext, 1.0f, 1.0f);

        GVRRenderData renderData = loadingObject.getRenderData();
        GVRMaterial loadingMaterial = new GVRMaterial(mGVRContext);
        renderData.setMaterial(loadingMaterial);
        renderData.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        loadingMaterial.setMainTexture(loaderTextures.get(0));
        GVRAnimation animation = new ImageFrameAnimation(loadingMaterial, duration,
                loaderTextures, loadingObject);

        animation.setRepeatMode(GVRRepeatMode.ONCE);
        animation.setRepeatCount(-1);
        animation.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation animation) {
                mainSceneObject.removeChildObject(((ImageFrameAnimation)animation).getSceneObject());
            }
        });
        animation.start(mGVRContext.getAnimationEngine());

        loadingObject.getTransform().setPosition(
                object.getTransform().getPositionX(),
                object.getTransform().getPositionY(),
                object.getTransform().getPositionZ()
        );
        mainSceneObject.addChildObject(loadingObject);
    }

    private void resetGame() {
        gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_END);
        showMouthPointer(false);
        stopThrowingObjects();
        tapTOStart = setInfoMessage("Game Over   " + String
                .format("Score : %d", ovrEater.getCurrentScore()));
        mainSceneObject.addChildObject(tapTOStart);
        AudioClip.getInstance(mGVRContext.getContext()).
                stopSound(AudioClip.getUISoundBGID());
    }

    private void showMouthPointer(Boolean enable) {
        if(enable) {
            // add head-tracking pointer
            try {
                headTracker = new GVRSceneObject(mGVRContext, new FutureWrapper<GVRMesh>(mGVRContext.createQuad(0.5f, 0.5f)),
                        mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "mouth_open.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            headTracker.getTransform().setPosition(0.0f, 0.0f, -2.0f);
            headTracker.getRenderData().setDepthTest(false);
            headTracker.getRenderData().setRenderingOrder(100000);
            mMainScene.getMainCameraRig().addChildObject(headTracker);
        } else {
            if(headTracker != null)
                mainSceneObject.removeChildObject(headTracker);
        }
    }
	
	private float minLinearX = -12.0f;
	private float maxLinearX = 12.0f;
	private float yawToLinearScale = 0.1f;

	private float getXLinearDistance(float headRotationRoll) {
		float val = headRotationRoll * yawToLinearScale;

		if(val < minLinearX) 		return -minLinearX;
		else if(val > maxLinearX)	return -maxLinearX;
		else						return -val;
	}

    private boolean isOnClick = false;

    private void showRunningScore() {
        if (scoreTextMessageObject == null) {
            scoreTextMessageObject = makeScoreboard(mGVRContext, headTracker);
        }
        scoreTextMessageObject.getTransform().setPosition(-1.2f, 1.5f, -2.2f);

        if (livesTextMessageObject == null) {
            livesTextMessageObject = makeLivesLeft(mGVRContext, headTracker);
        }
        livesTextMessageObject.getTransform().setPosition(1.2f, 1.5f, -2.2f);
    }

	public void onTouchEvent(MotionEvent event) throws IOException {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			isOnClick = true;
			break;
		case MotionEvent.ACTION_UP:
            if(isOnClick && (gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_LOAD ||
                    gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_END)) {
                ovrEater.reset();
                gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS);
                startGame();
            }
		break;
		default:
			break;
		}
	}

    private void startGame() {
        AudioClip.getInstance(mGVRContext.getContext()).
                playLoop(AudioClip.getUISoundBGID(), 0.8f, 0.8f);

        if(tapTOStart != null) // remove "Tap to Play"
            mainSceneObject.removeChildObject(tapTOStart);

        prevTime = System.currentTimeMillis();
        showMouthPointer(true);
        showRunningScore();
        _throwObject();
    }

	private void attachDefaultEyePointee(GVRSceneObject sceneObject) {
		sceneObject.attachEyePointeeHolder();
	}

}
