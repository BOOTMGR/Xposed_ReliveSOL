/*******************************************************************************
 * Copyright (c) 2014 Harsh Panchal<panchal.harsh18@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package com.harsh.panchal.relivesol;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Original Patch: https://github.com/CyanogenMod/android_frameworks_base/commit/fa0c6a58a44fd884d758d47eaa750c9c6476af1a
 * Reference: https://github.com/rovo89/XposedMods/blob/master/XposedTweakbox/src/de/robv/android/xposed/mods/tweakbox/VolumeKeysSkipTrack.java
 */
public class VolumeKeySkipTrack implements IXposedHookLoadPackage {
	private boolean mIsLongPress = false;
	XSharedPreferences pref = new XSharedPreferences("com.harsh.panchal.relivesol");

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if(!lpparam.packageName.equals("android"))
			return;
		if(!pref.getBoolean("vol_control_music", false))
			return;
		Class<?> phwmPolicy = XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader);
		XposedBridge.hookAllConstructors(phwmPolicy, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param)
					throws Throwable {
				Runnable mVolumeUpLongPress = new Runnable() {
			        public void run() {
			            mIsLongPress = true;
			            sendMediaButtonEvent(param.thisObject, KeyEvent.KEYCODE_MEDIA_NEXT);
			        };
			    };
			    
			    Runnable mVolumeDownLongPress = new Runnable() {
			        public void run() {
			            mIsLongPress = true;
			            sendMediaButtonEvent(param.thisObject, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			        };
			    };
			    
			    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVolumeUpLongPress", mVolumeUpLongPress);
			    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVolumeDownLongPress", mVolumeDownLongPress);
			}
		});
		
		XposedHelpers.findAndHookMethod(phwmPolicy, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				KeyEvent event = (KeyEvent)param.args[0];
				int code = event.getKeyCode();
				boolean isVolUpDownKey = (code == KeyEvent.KEYCODE_VOLUME_DOWN || code == KeyEvent.KEYCODE_VOLUME_UP);
				if((Boolean) XposedHelpers.callMethod(param.thisObject, "isMusicActive") && !(Boolean) param.args[2] && isVolUpDownKey) {
					if(event.getAction() == KeyEvent.ACTION_DOWN) {
						mIsLongPress = false;
						handleVolumeLongPress(param.thisObject, code);
						param.setResult(0);
						return;
					} else {
						handleVolumeLongPressAbort(param.thisObject);
						if (mIsLongPress) {
							param.setResult(0);
							return;
						}
						// From: https://github.com/rovo89/XposedMods/blob/master/XposedTweakbox/src/de/robv/android/xposed/mods/tweakbox/VolumeKeysSkipTrack.java
						// send an additional "key down" because the first one was eaten
						// the "key up" is what we are just processing
						Object[] newArgs = new Object[3];
						newArgs[0] = new KeyEvent(KeyEvent.ACTION_DOWN, code);
						newArgs[1] = param.args[1];
						newArgs[2] = param.args[2];
						XposedBridge.invokeOriginalMethod(param.method, param.thisObject, newArgs);
					}
				}
			}
		});
	}	

    private void sendMediaButtonEvent(Object phwminstance, int code) {
    	Context mContext = (Context) XposedHelpers.getObjectField(phwminstance, "mContext");
        long eventtime = SystemClock.uptimeMillis();
        Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent keyEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, code, 0);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        mContext.sendOrderedBroadcast(keyIntent, null);
        keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        mContext.sendOrderedBroadcast(keyIntent, null);
    }

    void handleVolumeLongPress(Object phwminstance, int keycode) {
    	Handler mHandler = (Handler) XposedHelpers.getObjectField(phwminstance, "mHandler");
    	Runnable mVolumeUpLongPress = (Runnable) XposedHelpers.getAdditionalInstanceField(phwminstance, "mVolumeUpLongPress");
		Runnable mVolumeDownLongPress = (Runnable) XposedHelpers.getAdditionalInstanceField(phwminstance, "mVolumeDownLongPress");
        mHandler.postDelayed(keycode == KeyEvent.KEYCODE_VOLUME_UP ? mVolumeUpLongPress :
            mVolumeDownLongPress, ViewConfiguration.getLongPressTimeout());
    }

    void handleVolumeLongPressAbort(Object phwminstance) {
    	Handler mHandler = (Handler) XposedHelpers.getObjectField(phwminstance, "mHandler");
    	Runnable mVolumeUpLongPress = (Runnable) XposedHelpers.getAdditionalInstanceField(phwminstance, "mVolumeUpLongPress");
		Runnable mVolumeDownLongPress = (Runnable) XposedHelpers.getAdditionalInstanceField(phwminstance, "mVolumeDownLongPress");
        mHandler.removeCallbacks(mVolumeUpLongPress);
        mHandler.removeCallbacks(mVolumeDownLongPress);
    }

}
