/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, an isomorphic musical keyboard for Android                   *
 *   Copyright 2011, 2012 David A. Randolph                                *
 *                                                                         *
 *   FILE: Play.java                                                       *
 *                                                                         *
 *   This file is part of IsoKeys, an open-source project                  *
 *   hosted at http://isokeys.sourceforge.net.                             *
 *                                                                         *
 *   IsoKeys is free software: you can redistribute it and/or              *
 *   modify it under the terms of the GNU General Public License           *
 *   as published by the Free Software Foundation, either version          *
 *   3 of the License, or (at your option) any later version.              *
 *                                                                         *
 *   AndroidWorld is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with IsoKeys.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package @CONFIG.APP_PACKAGE_NAME@;

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

import @CONFIG.APP_PACKAGE_NAME@.R;

public class Play extends Activity implements OnSharedPreferenceChangeListener
{
	final static int ABOUT_DIALOG_ID = 1;
	static SharedPreferences mPrefs;
	static FrameLayout mFrame;
	static HexKeyboard mBoard;

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

		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
		
    	String versionStr = this.getVersionName();
        
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
        loadKeyboard();
	}

	protected void setOrientation()
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
	}
	
	protected void loadKeyboard()
	{
	    setOrientation();	
		Context con = this.getApplicationContext();
		
		mFrame = new FrameLayout(con);
		mBoard = new HexKeyboard(con);
		// This really speeds up orientation switches!
		mBoard.mInstrument = (Instrument) getLastNonConfigurationInstance();
		if (mBoard.mInstrument == null) {
			// If no retained audio, load it all up (slow).
			mBoard.mInstrument = new Piano(mBoard.mContext);
			// Redraw whenever a new note is ready.
			mBoard.mInstrument.mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
					// This runs from the 'loader' thread.
					@Override
					public void onLoadComplete(SoundPool mSoundPool, int sampleId, int status) {
						mBoard.post(new Runnable() {
								public void run() {
									mBoard.invalidate();
									if (mBoard.mInstrument.sound_load_queue.hasNext()) {
										int[] tuple = mBoard.mInstrument.sound_load_queue.next();
										mBoard.mInstrument.addSound(tuple[0], tuple[1]);
									}
								}
						});
					}
			});
		}
		mBoard.setUpBoard(this.getRequestedOrientation());
		mBoard.invalidate();

		// mFrame.addView(mBoard);
		LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL); 
        
		// this.setContentView(mFrame);
		this.setContentView(mBoard);
	}

	public Object onRetainNonConfigurationInstance()
	{
		// Retain the audio across configuration changes.
		return mBoard.mInstrument;
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
			    setOrientation();
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
		setOrientation();
		mBoard.setUpBoard(this.getRequestedOrientation());
		mBoard.invalidate();
	}
	
	  @Override
	  protected void onDestroy() {
	    super.onDestroy();
	  }
}
