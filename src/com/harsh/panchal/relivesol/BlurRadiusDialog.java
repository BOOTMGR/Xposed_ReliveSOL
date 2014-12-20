package com.harsh.panchal.relivesol;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class BlurRadiusDialog extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	
	private SeekBar mSeekBar;
	private TextView mValueText;
	private Context mContext;
	
	private int mValue = 0;
	
	public BlurRadiusDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
	    LinearLayout layout = new LinearLayout(mContext);
	    layout.setOrientation(LinearLayout.VERTICAL);
	    layout.setPadding(6,6,6,6);

	    mValueText = new TextView(mContext);
	    mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
	    mValueText.setTextSize(32);
	    params = new LinearLayout.LayoutParams(
	        LinearLayout.LayoutParams.FILL_PARENT, 
	        LinearLayout.LayoutParams.WRAP_CONTENT);
	    layout.addView(mValueText, params);

	    mSeekBar = new SeekBar(mContext);
	    mSeekBar.setOnSeekBarChangeListener(this);
	    layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

	    if (shouldPersist())
	      mValue = getPersistedInt(35);

	    mSeekBar.setMax(100);
	    mSeekBar.setProgress(mValue);
	    return layout;
	};
	
	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
	    mSeekBar.setMax(100);
	    mSeekBar.setProgress(mValue);
	}
	
	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
	    if (restore) 
	    	mValue = shouldPersist() ? getPersistedInt(35) : 0;
	    else 
	    	mValue = (Integer)defaultValue;
	}
	
	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
	    mValueText.setText(String.valueOf(value));
	    if (shouldPersist())
	      persistInt(value);
	    callChangeListener(Integer.valueOf(value));
	}
	
	public void onStartTrackingTouch(SeekBar seek) { }
	public void onStopTrackingTouch(SeekBar seek) { }

	public void setProgress(int progress) { 
		mValue = progress;
		if (mSeekBar != null)
			mSeekBar.setProgress(progress); 
	}
	public int getProgress() { return mValue; }
}
