/*******************************************************************************
 * Copyright (c) 2014 Harsh Panchal<panchal.harsh18@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package com.harsh.panchal.relivesol;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Process;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class LongPressBackToKill implements IXposedHookLoadPackage {
	
	private Object phwmInstance = null;
	XSharedPreferences pref = new XSharedPreferences("com.harsh.panchal.relivesol");

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if(!lpparam.packageName.equals("android"))
			return;
		if(!pref.getBoolean("long_press_kill", false))
			return;
		Class<?> phwmPolicy = XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader);
		XposedHelpers.findAndHookMethod(phwmPolicy, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				phwmInstance = param.thisObject;
				Handler handler = (Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler");
				KeyEvent event = (KeyEvent)param.args[0];
				int code = event.getKeyCode();
				if(code == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
					handler.postDelayed(runnable, ViewConfiguration.getLongPressTimeout());
				else
					handler.removeCallbacks(runnable);
			}
		});
	}
	
	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			if(phwmInstance != null) {
				try {
					Context mContext = (Context) XposedHelpers.getObjectField(phwmInstance, "mContext");
					Intent intent = new Intent(Intent.ACTION_MAIN);
	                PackageManager pm = mContext.getPackageManager();
	                String launcher = "com.android.launcher";
	                intent.addCategory(Intent.CATEGORY_HOME);
	                
	                // for excluding launcher
	                ResolveInfo res = pm.resolveActivity(intent, 0);
	                if (res.activityInfo != null && !res.activityInfo.packageName.equals("android")) {
	                    launcher = res.activityInfo.packageName;
	                }
	                
	                ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningAppProcessInfo> apps = am.getRunningAppProcesses();
                    
                    for(RunningAppProcessInfo info : apps) {
                    	if(info.uid >= Process.FIRST_APPLICATION_UID && info.uid <= Process.LAST_APPLICATION_UID 
                    			&& info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    			!info.processName.equals(launcher)) {
                    		if (info.pkgList != null && info.pkgList.length > 0) {
                                for (String pkg : info.pkgList) {
                                    XposedHelpers.callMethod(am, "forceStopPackage", pkg);
                                }
                            } else {
                                Process.killProcess(info.pid);
                            }
                    		Toast.makeText(mContext, (String) pm.getApplicationLabel(pm.getApplicationInfo(info.processName, 0)) + " killed", Toast.LENGTH_SHORT).show();
                        	break;
                    	}
                    }
				} catch(Exception ex) {}
			} else XposedBridge.log("[harsh_debug] PhoneWindowManager instance null");
		}
	};

}
