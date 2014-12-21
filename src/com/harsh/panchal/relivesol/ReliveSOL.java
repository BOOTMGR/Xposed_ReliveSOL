/*******************************************************************************
 * Copyright (c) 2014 Harsh Panchal<panchal.harsh18@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package com.harsh.panchal.relivesol;

import android.annotation.SuppressLint;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

@SuppressLint("SdCardPath")
public class ReliveSOL implements IXposedHookZygoteInit, IXposedHookLoadPackage {
	
	private static XSharedPreferences mPref;
	static {
		System.load("/data/data/com.harsh.panchal.relivesol/lib/libharsh.so");
	}
	
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
		if(mPref.getBoolean("aosp_lockscreen", false)) {
			XResources.hookSystemWideLayout("android", "layout", "keyguard_screen_tab_unlock", new XC_LayoutInflated() {
				
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam)
						throws Throwable {
					// Fix for text in black color
					TextView carrier = (TextView) liparam.view.findViewById(liparam.res.getIdentifier("carrier", "id", "android"));
					TextView date = (TextView) liparam.view.findViewById(liparam.res.getIdentifier("date", "id", "android"));
					TextView alarmStatus = (TextView) liparam.view.findViewById(liparam.res.getIdentifier("alarm_status", "id", "android"));
					TextView chargerStatus = (TextView) liparam.view.findViewById(liparam.res.getIdentifier("status1", "id", "android"));
					Button emrgButton = (Button) liparam.view.findViewById(liparam.res.getIdentifier("emergencyCallButton", "id", "android"));
					carrier.setTextColor(Color.WHITE);
					date.setTextColor(Color.WHITE);
					alarmStatus.setTextColor(Color.WHITE);
					chargerStatus.setTextColor(Color.WHITE);
					
					/**
					 * Fix for grey bar at bottom
					 * F*** there are two buttons with same ids. So use
					 * them to grab parent and then get child view.
					 */
					GridLayout layout = (GridLayout) emrgButton.getParent();
					layout.getChildAt(6).setVisibility(View.GONE);
				}
			});
			
			// Enhancements to lockscreen music controls
			XResources.hookSystemWideLayout("android", "layout", "keyguard_transport_control", new XC_LayoutInflated() {
				
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam)
						throws Throwable {
					ImageView imageView = (ImageView) liparam.view.findViewById(liparam.res.getIdentifier("albumart", "id", "android"));
					((FrameLayout) imageView.getParent()).setForeground(new ColorDrawable(0x5f000000));
				}
			});
		}
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if(lpparam.packageName.equals("com.lge.lockscreen") && mPref.getBoolean("blur_lockscreen", false))
			BlurLockScreen.init(lpparam.classLoader, mPref.getInt("blur_radius", 35));
		else if(lpparam.packageName.equals("com.android.systemui") && mPref.getBoolean("double_tap_sleep", false))
			DoubleTapToSleep.init(lpparam.classLoader);
		else if(lpparam.packageName.equals("android")) {
			if(mPref.getBoolean("long_press_kill", false))
				LongPressBackToKill.init(lpparam.classLoader);
			if(mPref.getBoolean("vol_control_music", false))
				VolumeKeySkipTrack.init(lpparam.classLoader);
			if(mPref.getBoolean("aosp_lockscreen", false))
				LgeLockScreenHook.init(lpparam.classLoader);
		}
	}
}
