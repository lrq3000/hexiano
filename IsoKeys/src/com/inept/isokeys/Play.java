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
package com.inept.isokeys;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class Play extends Activity implements OnSharedPreferenceChangeListener
{
	final int ABOUT_DIALOG_ID = 1;
	SharedPreferences mPrefs;
	HexKeyboard mBoard;
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        loadKeyboard();
	}
	
	protected void loadKeyboard()
	{
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		Display display = getWindowManager().getDefaultDisplay();
		int displayWidth = display.getWidth();
		int displayHeight = display.getHeight(); 
	
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int dpi = dm.densityDpi;
        int keyRadius = 3 * dpi / 8;
	    String scaleStr = mPrefs.getString("scale", "100");
	    int scalePct = Integer.parseInt(scaleStr);
	    keyRadius = (keyRadius * scalePct) / 100;
		
		Context con = this.getApplicationContext();
		mBoard = new HexKeyboard(con, displayHeight, displayWidth, keyRadius);	
		mBoard.invalidate();

		this.setContentView(mBoard);
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
		// TODO Auto-generated method stub
		mBoard.setUpBoard();
		mBoard.invalidate();
	}
}
