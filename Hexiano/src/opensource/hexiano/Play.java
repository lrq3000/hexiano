/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft  @ 2013 Stephen Larroque                                     *
 *   Copyright © 2012 James Haigh                                          *
 *   Copyright © 2011, 2012 David A. Randolph                              *
 *                                                                         *
 *   FILE: Play.java                                                       *
 *                                                                         *
 *   This file is part of Hexiano, an open-source project hosted at:       *
 *   https://gitorious.org/hexiano                                         *
 *                                                                         *
 *   Hexiano is free software: you can redistribute it and/or              *
 *   modify it under the terms of the GNU General Public License           *
 *   as published by the Free Software Foundation, either version          *
 *   3 of the License, or (at your option) any later version.              *
 *                                                                         *
 *   Hexiano is distributed in the hope that it will be useful,            *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with Hexiano.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package opensource.hexiano;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.SoundPool;
import android.media.AudioManager;

import opensource.hexiano.R;

public class Play extends Activity implements OnSharedPreferenceChangeListener
{
	final static int ABOUT_DIALOG_ID = 1;
	static SharedPreferences mPrefs;
	static FrameLayout mFrame;
	static HexKeyboard mBoard;
	static String mInstrument;

	private String getVersionName()
	{
		String versionName = "";
		
		try
		{
			versionName = getPackageManager().
					getPackageInfo(getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e)
		{
			Log.e("getVersionName", e.getMessage());
		}
		
		return versionName;
	}

	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Lock volume control to Media volume.
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
		
    	String versionStr = this.getVersionName();
        
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
        loadKeyboard();
	}

	protected int setOrientation()
	{
		String layout = mPrefs.getString("layout", null);

		int orientationId = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

		if (layout.equals("Sonome"))
		{
			boolean isLandscape = mPrefs.getBoolean("sonomeLandscape", false);
			if (! isLandscape)
			{
				orientationId = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}
		else if (layout.equals("Jammer"))
		{
			boolean isLandscape = mPrefs.getBoolean("jammerLandscape", false);
			if (! isLandscape)
			{
				orientationId = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}
		else if (layout.equals("Janko"))
		{
			boolean isLandscape = mPrefs.getBoolean("jankoLandscape", false);
			if (! isLandscape)
			{
				orientationId = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}

		this.setRequestedOrientation(orientationId);
		return orientationId;
	}
	
	protected void loadKeyboard()
	{
	    int orientationId = setOrientation();
		Context con = this.getApplicationContext();
		
		mFrame = new FrameLayout(con);
		mBoard = new HexKeyboard(con);
		// This really speeds up orientation switches!
		HexKeyboard.mInstrument = (Instrument) getLastNonConfigurationInstance();
		String instrument = mPrefs.getString("instrument", "Piano");
		// If no retained audio (or changed), load it all up (slow).
		if (HexKeyboard.mInstrument == null || mInstrument != instrument) {
			mInstrument = instrument;
			if (mInstrument.equals("Piano") || mInstrument.equals("DUMMY (dont choose)")) // Place an if conditional for each staticInstrument (included in APK resources)
			{
				HexKeyboard.mInstrument = new Piano(HexKeyboard.mContext);
			} else {
				try {
					HexKeyboard.mInstrument = new GenericInstrument(HexKeyboard.mContext, mInstrument);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Redraw whenever a new note is ready.
			HexKeyboard.mInstrument.mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
					@Override
					public void onLoadComplete(SoundPool mSoundPool, int sampleId, int status) {
						mBoard.invalidate();
						if (HexKeyboard.mInstrument.sound_load_queue.hasNext()) {
							if (!HexKeyboard.mInstrument.mExternal) {
								ArrayList tuple = HexKeyboard.mInstrument.sound_load_queue.next();
								HexKeyboard.mInstrument.addSound((Integer)tuple.get(0), (Integer)tuple.get(1));
							} else {
								ArrayList tuple = HexKeyboard.mInstrument.sound_load_queue.next();
								HexKeyboard.mInstrument.addSound((Integer)tuple.get(0), (String)tuple.get(1));
							}
						}
					}
			});
		}
		mBoard.setUpBoard(orientationId);
		mBoard.invalidate();

		// mFrame.addView(mBoard);
		LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL); 
        
		// this.setContentView(mFrame);
		this.setContentView(mBoard);
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		// Retain the audio across configuration changes.
		return HexKeyboard.mInstrument;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		Log.d("Play", "Menu, ho!");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu); 
		return true; 
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ABOUT_DIALOG_ID:
                return new AboutDialog(this);
            default:
                return null;
        }
    }
   
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		    case R.id.preferences:
			    startActivity(new Intent(this, Prefer.class)); 
			    break;
		    case R.id.quit:
			    finish();
			    break;
		    case R.id.about:
			    showDialog(ABOUT_DIALOG_ID);
			    break;
		}

		return true; 
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		mBoard.setUpBoard(setOrientation());
		mBoard.invalidate();
	}
	
	// Clean all playing states (eg: sounds playing, etc)
	public void cleanStates() {
		HexKeyboard.stopAll();
	}
	
	@Override
	protected void onPause() {
		// If app is closed/minimized (home button is pressed)
		//if (this.isFinishing()){ // The function isFinishing() returns a boolean. True if your App is actually closing, False if your app is still running but for example the screen turns off.
			// Clean all playing states
			this.cleanStates();
		//}
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		cleanStates();
		super.onDestroy();
	}
}
