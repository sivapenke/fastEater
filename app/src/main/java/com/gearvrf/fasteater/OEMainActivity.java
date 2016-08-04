package com.gearvrf.fasteater;

import java.io.IOException;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

public class OEMainActivity extends GVRActivity {

	private OEViewManager viewManager;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        viewManager = new OEViewManager();
        setScript(viewManager, "gvr.xml");
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	try {
			viewManager.onTouchEvent(event);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return super.onTouchEvent(event);
    }
}
