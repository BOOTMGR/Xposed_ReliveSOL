/*******************************************************************************
 * Copyright (c) 2014 Harsh Panchal<panchal.harsh18@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package com.harsh.panchal.relivesol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import eu.chainfire.libsuperuser.Shell;
import eu.chainfire.libsuperuser.Shell.SU;

public class MainActivity extends Activity {
	
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                                .beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();
        mContext = getApplicationContext();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.hotboot:
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Confirm");
					builder.setMessage("Are you sure want to Hotboot your device?");
					builder.setPositiveButton("Yes", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if(SU.available()) {
								Shell.SU.run("pkill -f system_server");
							} else Toast.makeText(mContext, "Please allow SU rights to hotboot device", Toast.LENGTH_SHORT).show();
						}
					});
					builder.setNegativeButton("No", null);
					builder.create().show();
					return true;
			case R.id.about:
				startActivity(new Intent(this, About.class));
				return true;
			default:
	            return super.onOptionsItemSelected(item);
		}
	}
	
	public static class PrefsFragment extends PreferenceFragment {
		
        @Override
        public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	addPreferencesFromResource(R.xml.preference);
        	if(getPreferenceManager().getSharedPreferences().getBoolean("aosp_lockscreen", false)) {
        		findPreference("blur_lockscreen").setEnabled(false);
        		findPreference("blur_radius").setEnabled(false);
        	}
        }
        
        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
        		Preference preference) {
        	if(preference.getKey().equals("blur_lockscreen") && getPreferenceManager().getSharedPreferences().getBoolean("blur_lockscreen", false)) {
        		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Warning");
				builder.setMessage("Blur lockscreen involves CPU intensive tasks which may cause some delay while locking device.");
				builder.setPositiveButton("OK", null);
				builder.create().show();
        	} else if (preference.getKey().equals("aosp_lockscreen")) {
        		findPreference("blur_lockscreen").setEnabled(getPreferenceManager().getSharedPreferences().getBoolean("aosp_lockscreen", false) ? false : true);
        		findPreference("blur_radius").setEnabled(getPreferenceManager().getSharedPreferences().getBoolean("aosp_lockscreen", false) ? false : true);
        	}
        	return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
	}

}
