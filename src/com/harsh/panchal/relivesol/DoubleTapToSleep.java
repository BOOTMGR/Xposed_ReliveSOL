/*******************************************************************************
 * Copyright (c) 2014 Harsh Panchal<panchal.harsh18@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package com.harsh.panchal.relivesol;

import android.content.Context;
import android.os.PowerManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Original Patch: http://review.cyanogenmod.org/#/c/55645/1/packages/SystemUI/src/com/android/systemui/statusbar/phone/PhoneStatusBarView.java
 *
 */
public class DoubleTapToSleep {
	
	private DoubleTapToSleep() {}
	
	public static void init(ClassLoader loader) {
		final Class<?> statusbarView = XposedHelpers.findClass("com.android.systemui.statusbar.phone.PhoneStatusBarView", loader);
		// Inject our fields
		XposedBridge.hookAllConstructors(statusbarView, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				final Context mContext = (Context) param.args[0];
				GestureDetector mDoubleTapGesture = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
		            @Override
		            public boolean onDoubleTap(MotionEvent e) {
		                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		                if(pm != null)
		                    pm.goToSleep(e.getEventTime());
		                return true;
		            }
		        });
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mDoubleTapGesture", mDoubleTapGesture);
			}
		});
		
		XposedHelpers.findAndHookMethod(statusbarView, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				MotionEvent event = (MotionEvent) param.args[0];
				GestureDetector detector = (GestureDetector) XposedHelpers.getAdditionalInstanceField(param.thisObject, "mDoubleTapGesture");
				detector.onTouchEvent(event);
			}
		});
	}

}
