/*******************************************************************************
 * Copyright (c) 2014 Harsh Panchal<panchal.harsh18@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package com.harsh.panchal.relivesol;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

@SuppressLint("SdCardPath")
public class BlurLockScreen implements IXposedHookLoadPackage {
	
	static {
		System.load("/data/data/com.harsh.panchal.relivesol/lib/libharsh.so");
	}
	
	XSharedPreferences pref = new XSharedPreferences("com.harsh.panchal.relivesol");
	private native void blurImage(Bitmap in, Bitmap out, int radius);
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if(!lpparam.packageName.equals("com.lge.lockscreen"))
			return;
		if(!pref.getBoolean("blur_lockscreen", false))
			return;
		
		XC_MethodHook handleLockscreenConst = new XC_MethodHook() {
			@SuppressWarnings("deprecation")
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				Bitmap map = takeSurfaceScreenshot();
				if(map != null) {
					Bitmap out = map.copy(Bitmap.Config.ARGB_8888, true);
					blurImage(map, out, 25);
				    BitmapDrawable drawable = new BitmapDrawable(out);
				    map.recycle();
				    XposedHelpers.callMethod(param.thisObject, "setBackgroundDrawable", drawable);
				} else XposedBridge.log("[harsh_debug] takeSurfaceScreenshot() returned null");
			}
		};
		
		Class<?> patternUnlockScreen = XposedHelpers.findClass("com.lge.lockscreen.LgePatternUnlockScreen", lpparam.classLoader);
		Class<?> passwordUnlockScreen = XposedHelpers.findClass("com.lge.lockscreen.LgePasswordUnlockScreen", lpparam.classLoader);
		Class<?> simpleUnlockScreen = XposedHelpers.findClass("com.lge.lockscreen.LgeLockScreen", lpparam.classLoader);
		XposedBridge.hookAllConstructors(patternUnlockScreen, handleLockscreenConst);	// pattern unlock screen
		XposedBridge.hookAllConstructors(passwordUnlockScreen, handleLockscreenConst);	// password, PIN unlock screen
		XposedBridge.hookAllConstructors(simpleUnlockScreen, handleLockscreenConst);	// default lge swipe unlock screen
	}

	public static Bitmap takeSurfaceScreenshot() {
		DisplayMetrics metrics = new DisplayMetrics();
		
		Bitmap screenBitmap = null;
        float[] dims = { metrics.widthPixels, metrics.heightPixels };
        
        try {
        	Class<?> Surface = Class.forName("android.view.Surface");
        	screenBitmap = (Bitmap) XposedHelpers.callStaticMethod(Surface, "screenshot", (int) dims[0], (int) dims[1]);
        } catch (ClassNotFoundException e) {
        	e.printStackTrace();
        }
        
        if (screenBitmap == null) {
        	return null;
        }
        
        screenBitmap.setHasAlpha(false);
        screenBitmap.prepareToDraw();
        return screenBitmap;
    }
}
