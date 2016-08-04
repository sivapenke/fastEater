package com.gearvrf.fasteater;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by siva.penke on 7/31/2016.
 */
public class ImageFrameAnimation extends GVRAnimation {
    private final List<Future<GVRTexture>> animationTextures;
    private int lastFileIndex = -1;
    private GVRSceneObject sceneObject;

    public GVRSceneObject getSceneObject() {
        return sceneObject;
    }

    public void setSceneObject(GVRSceneObject sceneObject) {
        this.sceneObject = sceneObject;
    }

    /**
     * @param material             {@link GVRMaterial} to animate
     * @param duration             The animation duration, in seconds.
     * @param texturesForAnimation arrayList of GVRTexture used during animation
     */
    public ImageFrameAnimation(GVRMaterial material, float duration,
                               final List<Future<GVRTexture>> texturesForAnimation) {
        super(material, duration);
        animationTextures = texturesForAnimation;
    }

    /**
     * @param material             {@link GVRMaterial} to animate
     * @param duration             The animation duration, in seconds.
     * @param texturesForAnimation arrayList of GVRTexture used during animation
     * @param object               used to retrieve object reference
     */
    public ImageFrameAnimation(GVRMaterial material, float duration,
                                  final List<Future<GVRTexture>> texturesForAnimation, GVRSceneObject object) {
        super(material, duration);
        animationTextures = texturesForAnimation;
        sceneObject = object;
    }

    public List<Future<GVRTexture>> getAnimationTextures() {
        return animationTextures;
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio) {
        final int size = animationTextures.size();
        final int fileIndex = (int) (ratio * size);

        if (lastFileIndex == fileIndex || fileIndex == size) {
            return;
        }

        lastFileIndex = fileIndex;

        GVRMaterial material = (GVRMaterial) target;
        material.setMainTexture(animationTextures.get(fileIndex));
    }
}
