/*******************************************************************************
 * Copyright (c) 2014 Harsh Panchal<panchal.harsh18@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package com.harsh.panchal.relivesol;

import android.content.res.XResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;

public class ZygoteHook implements IXposedHookZygoteInit {
	private XSharedPreferences mPref = null;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPref = new XSharedPreferences("com.harsh.panchal.relivesol");
		XResources.setSystemWideReplacement("android", "bool", "config_animateScreenLights", mPref.getBoolean("crt_off_anim", false) ? false : true);
		XResources.setSystemWideReplacement("android", "bool", "show_ongoing_ime_switcher", mPref.getBoolean("ime_chooser", false) ? true : false);
		XResources.setSystemWideReplacement("android", "bool", "preferences_prefer_dual_pane", mPref.getBoolean("dual_panel", false) ? true : false);
		XResources.setSystemWideReplacement("android", "bool", "config_unplugTurnsOnScreen", mPref.getBoolean("unplug_wake", false) ? true : false);
		XResources.setSystemWideReplacement("android", "bool", "config_allowAllRotations", mPref.getBoolean("all_rotation", false) ? true : false);
		XResources.setSystemWideReplacement("android", "bool", "config_showNavigationBar", mPref.getBoolean("navigation_bar", false) ? true : false);
		XResources.setSystemWideReplacement("android", "bool", "lockscreen_isPortrait", mPref.getBoolean("rotate_lockscreen", false) ? true : false);
		if(mPref.getBoolean("perf_mode", false)) {
			XResources.setSystemWideReplacement("android", "bool", "config_sf_limitedAlpha", true);
			XResources.setSystemWideReplacement("android", "bool", "config_sf_slowBlur", true);
		}
		mPref = null;
	}

}
