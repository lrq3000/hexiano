/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, an isomorphic musical keyboard                               *
 *   Copyright (C) 2011, 2012 David A. Randolph                            *
 *                                                                         *
 *   FILE: HexKeyboard.java                                                *
 *                                                                         *
 *   This file is part of IsoKeys, an open-source project                  *
 *   hosted at http://isokeys.sourceforge.net.                             *
 *                                                                         *
 *   IsoKeys is free software: you can redistribute it and/or              *
 *   modify it under the terms of the GNU General Public License           *
 *   as published by the Free Software Foundation, either version          *
 *   3 of the License, or (at your option) any later version.              *
 *                                                                         *
 *   IsoKeys is distributed in the hope that it will be useful,            *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with IsoKeys.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.inept.isokeys;

import java.lang.Math;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.world.Posn;
import java.util.ArrayList;
import java.util.HashMap;

public class HexKeyboard extends View 
{
	SharedPreferences mPrefs;
	static Context mContext;
	static Bitmap mBitmap;
	static int mDpi = 0;
	static int mDisplayWidth = 0;
	static int mDisplayHeight = 0;
	static int mRowCount = 0;
	static int mColumnCount = 0;
	static int mTileRadius = 64; 
	static int mTileHeight = 0;

	static HashMap<Integer, Integer> mTouches = new HashMap<Integer, Integer>();
	static Instrument mInstrument;

	static ArrayList<HexKey> mKeys = new ArrayList<HexKey>();

	protected void setUpJammerBoard()
	{
		int y = 0;
		
		String firstNote = mPrefs.getString("baseJammerNote", "C");
		String firstOctaveStr = mPrefs.getString("baseJammerOctave", "8");
		int firstOctave = Integer.parseInt(firstOctaveStr);
		int pitch = Note.getNoteNumber(firstNote, firstOctave); 
		Log.d("setUpJammerBoard", "" + pitch);
		int rowFirstPitch = pitch;

		for (int j = 0; j < mRowCount; j++)
		{	
			int x = mTileRadius;

			for (int i = 0; i < mColumnCount; i++)
			{
				int kittyCornerX = (int)Math.round(x - mTileRadius * 1.5);
				int kittyCornerY = y + mTileHeight/2;
				JammerKey kittyCornerKey = new JammerKey(
						mContext,
						mTileRadius,
						new Posn(kittyCornerX, kittyCornerY),
						pitch,
						mInstrument);

				mKeys.add(kittyCornerKey);
				pitch-=5;

				JammerKey key = new JammerKey(
						mContext,
						mTileRadius,
						new Posn(x, y),
						pitch,
						mInstrument);
				mKeys.add(key);
				pitch-=7;

				x += 3 * mTileRadius;
			}

			pitch = rowFirstPitch - 2; // Down a full tone.
			rowFirstPitch = pitch;
			y += mTileHeight;
		}
	}

	protected void setUpSonomeBoard()
	{
		int y = 0;
		String firstNote = mPrefs.getString("baseSonomeNote", "E");
		String firstOctaveStr = mPrefs.getString("baseSonomeOctave", "1");
		int firstOctave = Integer.parseInt(firstOctaveStr);
		int pitch = Note.getNoteNumber(firstNote, firstOctave); 
		pitch += (mRowCount - 1) * 7;
		Log.d("setUpSonomeBoard", "pitch: " + pitch);
		Log.d("setUpSonomeBoard", "rowCount: " + mRowCount);
		int rowFirstPitch = pitch;

		for (int j = 0; j < mRowCount; j++)
		{	
			int x = mTileRadius;

			for (int i = 0; i < mColumnCount; i++)
			{
				int kittyCornerX = (int)Math.round(x - mTileRadius * 1.5);
				int kittyCornerY = y + mTileHeight/2;
				SonomeKey kittyCornerKey = new SonomeKey(
						mContext,
						mTileRadius,
						new Posn(kittyCornerX, kittyCornerY),
						pitch,
						mInstrument);

				mKeys.add(kittyCornerKey);
				pitch+=4;

				SonomeKey key = new SonomeKey(
						mContext,
						mTileRadius,
						new Posn(x, y),
						pitch,
						mInstrument);
				mKeys.add(key);
				pitch-=3;

				x += 3 * mTileRadius;
			}

			pitch = rowFirstPitch - 7; // Down a fifth.
			rowFirstPitch = pitch;
			y += mTileHeight;
		}
	}
	
	void setUpBoard()
	{
		mKeys.clear();

        mTileRadius = (3 * mDpi) / 8;
	    String scaleStr = mPrefs.getString("scale", "100");
	    int scalePct = Integer.parseInt(scaleStr);
	    mTileRadius = (mTileRadius * scalePct) / 100;
		
		mTileHeight = (int)(Math.round(Math.sqrt(3.0) * mTileRadius));
		Log.d("HexKeyboard", "mDisplayWidth: " + mDisplayWidth);
		Log.d("HexKeyboard", "mDisplayHeight: " + mDisplayHeight);
		Log.d("HexKeyboard", "mTileRadius: " + mTileRadius);
		Log.d("HexKeyboard", "mTileHeight: " + mTileHeight);
		mRowCount = mDisplayHeight/mTileHeight + 1; // Add one in case there is an extra kitty-corner half-row.
		if (mDisplayHeight % mTileHeight > 0)
		{
			mRowCount++;
		}
		Log.d("HexKeyboard", "mRowCount: " + mRowCount);
		
		mColumnCount = mDisplayWidth/(mTileRadius * 3) + 1; // Add one for the possible extra kitty-korner.
		int remainder = mDisplayWidth % (mTileRadius * 3);
		
		if (remainder > 0)
		{
			mColumnCount++;
			if (remainder > 2 * mTileRadius)
			{
				mColumnCount++;
			}
		}
		
		String layoutPref = mPrefs.getString("layout", "Sonome");
		if (layoutPref.equals("Sonome"))
		{
			this.setUpSonomeBoard();
		}
		else
		{
			this.setUpJammerBoard();
		}
		
		mBitmap = Bitmap.createBitmap(mDisplayWidth, mDisplayHeight, Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(mBitmap);
		this.onDraw(tempCanvas);
	}

	public HexKeyboard(Context context, int height, int width, int dpi)
	{
		super(context);
		
		mContext = context;
        mDpi = dpi;	
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        
		mInstrument = new Piano(context);
		if (height > width)
		{
			mDisplayHeight = width;
			mDisplayWidth = height;
		}
		else
		{
			mDisplayWidth = width;
			mDisplayHeight = height;
		}
		
		setUpBoard();
	}

	private int keyIdAt(int x, int y) throws Exception
	{
		for (int i = 0; i < mKeys.size(); i++)
		{
			HexKey key = mKeys.get(i);
			if (key.contains(x, y))
			{
				return i;
			}
		}

		throw new Exception("Key not found");
	}

	private int getLowerBound (int x, int y)
	{
		int lowerBound = mTileHeight/2;
		for (int j = 0; j <= mRowCount; j++)
		{
			if (y < lowerBound)
			{
				break;
			}

			lowerBound += mTileHeight/2;

			if (y < lowerBound)
			{
				break;
			}

			lowerBound += mTileHeight/2;
		}

		return lowerBound;
	}

	private int getHorizontalSliceNumber(int x, int y)
	{
		int lowerBound = mTileHeight/2;
		for (int j = 0; j <= mRowCount; j++)
		{
			if (y < lowerBound)
			{
				return(2*j);
			}

			lowerBound += mTileHeight/2;

			if (y < lowerBound)
			{
				return(2*j + 1);
			}

			lowerBound += mTileHeight/2;
		}

		return -1;
	}

	private static int getRightBound(int x, int y)
	{
		int rightBound = mTileRadius/2;

		for (int i = 0; i < mColumnCount; i++)
		{
			if (x < rightBound)
			{
				break;
			}

			rightBound += mTileRadius;

			if (x < rightBound)
			{
				break;
			}

			rightBound += mTileRadius/2;

			if (x < rightBound)
			{
				break;
			}
		}

		return rightBound;
	}

	private int getVerticalSliceNumber(int x, int y)
	{
		int rightBound = mTileRadius/2;

		for (int i = 0; i < mColumnCount; i++)
		{
			if (x < rightBound)
			{
				return(3*i);
			}

			rightBound += mTileRadius;

			if (x < rightBound)
			{
				return(3*i + 1);
			}

			rightBound += mTileRadius/2;

			if (x < rightBound)
			{
				return(3*i + 2);
			}
		}
		return -1;
	}

	private boolean isCentered(int columnSliceNumber)
	{
		if (columnSliceNumber % 2 == 1)
		{
			return true;
		}
		return false;
	}

	public void onDraw(Canvas canvas)
	{ 
		Canvas tempCanvas = new Canvas(mBitmap);
		for (HexKey k : mKeys)
		{
			k.paint(tempCanvas);
		}

		canvas.drawBitmap(mBitmap, 0, 0, null);
	}

	public boolean onTouchEvent(MotionEvent event){
		Log.v("onMouse", event.toString());

		int x = -1;
		int y = -1;

		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		try
		{
			if (actionCode == MotionEvent.ACTION_DOWN ||
					actionCode == MotionEvent.ACTION_POINTER_DOWN)
			{
				for (int pointerId = 0; pointerId < event.getPointerCount(); pointerId++)
				{
					x = (int)event.getX(pointerId);
					y = (int)event.getY(pointerId);
					int touchingId = this.keyIdAt(x, y);
					
					mKeys.get(touchingId).play();
					mTouches.put(pointerId, touchingId);
					this.invalidate();
				}
			}
			else if (actionCode == MotionEvent.ACTION_UP ||
					actionCode == MotionEvent.ACTION_POINTER_UP)
			{
				for (int i = 0; i < event.getPointerCount(); i++)
				{
					x = (int)event.getX(i);
					y = (int)event.getY(i);
					int touchingId = this.keyIdAt(x, y);
					mKeys.get(touchingId).stop();
					mTouches.remove(touchingId);
					this.invalidate();
				}
			}
			else if (actionCode == MotionEvent.ACTION_MOVE)
			{
				for (int pointerId = 0; pointerId < event.getPointerCount(); pointerId++)
				{
					x = (int)event.getX(pointerId);
					y = (int)event.getY(pointerId);
					int touchingId = this.keyIdAt(x, y);
					if (mTouches.containsKey(pointerId))
					{
						int touchedId = mTouches.get(pointerId);
						if (touchedId == touchingId)
						{
							// Nothing to do.
							return true;
						}
						else
						{
							mKeys.get(touchedId).stop();
							mTouches.remove(touchedId);
						}
					}
					
					mKeys.get(touchingId).play();
					mTouches.put(pointerId, touchingId);
					this.invalidate();
				}
			}
		}
		catch (Exception e)
		{
			Log.e("HexKeyboard::onMouse", "HexKey not found at (" + x + ", " + y + ")");
		}

		return true;
	}

}