package com.harsh.panchal.relivesol;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class LgeLockScreenHook {
	public static void init(ClassLoader classLoader) {
		Class<?> LgeLockScreenPackageManager = XposedHelpers.findClass("com.android.internal.policy.impl.LgeLockScreenPackageManager", classLoader);
		
		XposedHelpers.findAndHookMethod(LgeLockScreenPackageManager, "loadLockScreenPackage", Context.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				/**
				 * LgeLockScreenPackageManager class is responsible for loading LG Lockscreen.
				 * It loads LG Lockscreen by checking whether com.lge.lockscreen exist or not.
				 * If it doesn't exist then LgeLockScreenPackageManager loads default (aosp)
				 * lockscreen. We're assigning null to mLockScreenFactory so that it thinks
				 * com.lge.lockscreen doesn't exist and loads AOSP lockscreen instead.
				 */
				XposedHelpers.setObjectField(param.thisObject, "mPackageName", null);
				XposedHelpers.setObjectField(param.thisObject, "mPackageContext", null);
				XposedHelpers.setObjectField(param.thisObject, "mLockScreenFactory", null);
			}
		});
	}
}
