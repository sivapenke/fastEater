package com.gearvrf.fasteater;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;

/**
 * Created by siva.penke on 8/1/2016.
 */
public class OEAnimation {

    private GVRAnimationEngine mAnimationEngine;

    OEAnimation(GVRContext gvrContext) {
        mAnimationEngine = gvrContext.getAnimationEngine();
    }

    private void run(GVRAnimation animation) {
        animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1).start(mAnimationEngine);
    }

    private void runOnce(GVRAnimation animation) {
        animation.setRepeatMode(GVRRepeatMode.ONCE).setRepeatCount(-1).start(mAnimationEngine);
    }

    public void relativeMotionAnimation(GVRSceneObject object, float duration, float x, float y, float z) {
        runOnce(new GVRRelativeMotionAnimation(object, duration, x, y, z));
    }

    public void counterClockwise(GVRSceneObject object, float duration) {
        run(new GVRRotationByAxisWithPivotAnimation(object, duration, 360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
    }

    public void clockwise(GVRSceneObject object, float duration) {
        run(new GVRRotationByAxisWithPivotAnimation(object, duration, -360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
    }

    public void clockwise(GVRTransform transform, float duration) {
        run(new GVRRotationByAxisWithPivotAnimation(transform, duration, -360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
    }

    public void clockwiseOnZ(GVRSceneObject object, float duration) {
        runOnce(new GVRRotationByAxisAnimation(object, duration, -360.0f, 0.0f, 0.0f, 1.0f));
    }

    public void counterClockwiseOnZ(GVRSceneObject object, float duration) {
        runOnce(new GVRRotationByAxisAnimation(object, duration, 360.0f, 0.0f, 0.0f, 1.0f));
    }

    public void scaleAnimation(GVRSceneObject object, float duration, float x, float y, float z) {
        runOnce(new GVRScaleAnimation(object, duration, x, y, z));
    }

}
