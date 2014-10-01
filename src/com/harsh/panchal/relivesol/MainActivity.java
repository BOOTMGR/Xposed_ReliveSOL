/*******************************************************************************
 * Copyright (c) 2014 Harsh Panchal<panchal.harsh18@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package com.harsh.panchal.relivesol;

import java.io.DataOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
							try {
								// we can't live without su :P
								Process process = Runtime.getRuntime().exec("su");
						        DataOutputStream os = new DataOutputStream(process.getOutputStream());
						        os.writeBytes("pkill -f system_server\n");
						        os.flush();
						        os.close();
						        process.waitFor();
							} catch (Exception e) {
								Toast.makeText(mContext, "Hotboot unavailable, Please manually reboot device",  Toast.LENGTH_SHORT).show();
							}
						}
					});
					builder.setNegativeButton("No", null);
					builder.create().show();
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
        }
        
        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
        		Preference preference) {
        	return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
	}

}
