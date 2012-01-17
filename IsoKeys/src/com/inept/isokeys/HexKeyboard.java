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
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.ads.AdView;

import android.view.WindowManager;

public class HexKeyboard extends View 
{
	static int mScreenOrientationId = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	static SharedPreferences mPrefs;
	static Context mContext;
	static Bitmap mBitmap;
	static View mAdView;
	static int mDpi = 0;
	static int mDisplayWidth = 0;
	static int mDisplayHeight = 0;
	static int mRowCount = 0;
	static int mColumnCount = 0;
	static int mTileRadius = 64; 
	static int mTileWidth = 0;
	private static long mLastRedrawTime = 0L;
	private static final long mAdDelayMilliseconds = 12000L;
	private static long mStartTime = 0L;

	static HashMap<Integer, Integer> mTouches = new HashMap<Integer, Integer>();
	static Instrument mInstrument;

	static ArrayList<HexKey> mKeys = new ArrayList<HexKey>();

	private Handler mAdHandler = new Handler();

	private Runnable mAdUpdater = new Runnable()
	{
		public void run()
		{
			long now = SystemClock.uptimeMillis();

			Log.d("HexKeyboard.AdHandler", "mLastRedrawTime: " + mLastRedrawTime);
			Log.d("HexKeyboard.AdHandler", "            now: " + now);

			if (now > mLastRedrawTime + mAdDelayMilliseconds)
			{
				Log.d("HexKeyboard::mAdUpdater", "Make VISIBLE");
				mAdView.setVisibility(View.VISIBLE);
			}

			mAdHandler.postDelayed(mAdUpdater, mAdDelayMilliseconds);
		}
	};

	OnClickListener mStartListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			if (mStartTime == 0L)
			{
				mStartTime = System.currentTimeMillis();
				mAdHandler.removeCallbacks(mAdUpdater);
				mAdHandler.postDelayed(mAdUpdater, mAdDelayMilliseconds);
			}
		}
	};

	protected void setUpJammerBoard()
	{
		int y = 0;

		String firstNote = mPrefs.getString("baseJammerNote", "C");
		String firstOctaveStr = mPrefs.getString("baseJammerOctave", "8");
		int firstOctave = Integer.parseInt(firstOctaveStr);
		int pitch = Note.getNoteNumber(firstNote, firstOctave); 
		Log.d("setUpJammerBoard", "" + pitch);
		if (HexKey.getKeyOrientation(mContext).equals("Vertical"))
		{
			int rowFirstPitch = pitch;

			for (int j = 0; j < mRowCount; j++)
			{	
				int x = mTileRadius;

				for (int i = 0; i < mColumnCount; i++)
				{
					int kittyCornerX = (int)Math.round(x - mTileRadius * 1.5);
					int kittyCornerY = y + mTileWidth/2;
					JammerKey kittyCornerKey = new JammerKey(
							mContext,
							mTileRadius,
							new Point(kittyCornerX, kittyCornerY),
							pitch,
							mInstrument);

					mKeys.add(kittyCornerKey);
					pitch-=5;

					JammerKey key = new JammerKey(
							mContext,
							mTileRadius,
							new Point(x, y),
							pitch,
							mInstrument);
					mKeys.add(key);
					pitch-=7;

					x += 3 * mTileRadius;
				}

				pitch = rowFirstPitch - 2; // Down a full tone.
				rowFirstPitch = pitch;
				y += mTileWidth;
			}
		}
		else
		{
			pitch -= (mRowCount - 1) * 2;
		
			y = mTileRadius;
			
			int rowFirstPitch = pitch;
			
			for (int j = 0; j < mRowCount; j++)
			{	
				int x = mTileWidth / 2;

				for (int i = 0; i < mColumnCount; i++)
				{
					int kittyCornerX = (int)Math.round(x - mTileWidth / 2);
					int kittyCornerY = (int)Math.round(y - mTileRadius * 1.5);
					JammerKey kittyCornerKey = new JammerKey(
							mContext,
							mTileRadius,
							new Point(kittyCornerX, kittyCornerY),
							pitch,
							mInstrument);
					mKeys.add(kittyCornerKey);
					
					pitch-=5;

					JammerKey key = new JammerKey(
							mContext,
							mTileRadius,
							new Point(x, y),
							pitch,
							mInstrument);
					mKeys.add(key);
					
					pitch+=7;

					x += mTileWidth;
				}

				pitch = rowFirstPitch - 12; // Down a full tone.
				rowFirstPitch = pitch;
				y += 3 * mTileRadius;
			}
		}
	}

	protected void setUpJankoBoard()
	{
		int y = 0;

		String highestNote = mPrefs.getString("baseJankoNote", "F");
		String highestOctaveStr = mPrefs.getString("baseJankoOctave", "5");
		int highestOctave = Integer.parseInt(highestOctaveStr);
		int pitch = Note.getNoteNumber(highestNote, highestOctave); 
		String groupSizeStr = mPrefs.getString("jankoRowCount", "4");
		groupSizeStr.replaceAll("[^0-9]", "");
		if (groupSizeStr.length() == 0)
		{
			groupSizeStr = "4";
		}
	    int groupSize = Integer.parseInt(groupSizeStr);
	   
	    pitch -= (mColumnCount - 1) * 2;
		Log.d("setUpJankoBoard", "" + pitch);
		if (HexKey.getKeyOrientation(mContext).equals("Vertical"))
		{
			int rowFirstPitch = pitch;

			for (int j = 0; j < mRowCount; j++)
			{	
				int x = mTileRadius;

				for (int i = 0; i < mColumnCount; i++)
				{
					int kittyCornerX = (int)Math.round(x - mTileRadius * 1.5);
					int kittyCornerY = y + mTileWidth/2;
					JammerKey kittyCornerKey = new JammerKey(
							mContext,
							mTileRadius,
							new Point(kittyCornerX, kittyCornerY),
							pitch,
							mInstrument);

					mKeys.add(kittyCornerKey);
					pitch-=5;

					JammerKey key = new JammerKey(
							mContext,
							mTileRadius,
							new Point(x, y),
							pitch,
							mInstrument);
					mKeys.add(key);
					pitch-=7;

					x += 3 * mTileRadius;
				}

				pitch = rowFirstPitch - 2; // Down a full tone.
				rowFirstPitch = pitch;
				y += mTileWidth;
			}
		}
		else
		{
			y = mTileRadius;
			int rowFirstPitch = pitch;
	
			int octaveGroupNumber = 0;
			int jankoRowNumber = 0;
			
			for (int j = 0; j <= mRowCount; j++)
			{	
				int x = mTileWidth / 2;

				for (int i = 0; i < mColumnCount; i++)
				{
					int kittyCornerX = (int)Math.round(x - mTileWidth / 2);
					int kittyCornerY = (int)Math.round(y - mTileRadius * 1.5);
					JankoKey kittyCornerKey = new JankoKey(
							mContext,
							mTileRadius,
							new Point(kittyCornerX, kittyCornerY),
							pitch,
							mInstrument,
							octaveGroupNumber);
					mKeys.add(kittyCornerKey);
					pitch+=2;
					x += mTileWidth;
				}
			
				jankoRowNumber++;
			    if (jankoRowNumber % groupSize == 0)
			    {
			    	octaveGroupNumber++;
			    	rowFirstPitch-=12;
			    }
				pitch = rowFirstPitch + 1;
				
				x = mTileWidth / 2;
				for (int i = 0; i < mColumnCount; i++)
				{
					JankoKey key = new JankoKey(
							mContext,
							mTileRadius,
							new Point(x, y),
							pitch,
							mInstrument,
							octaveGroupNumber);
					mKeys.add(key);
					
					pitch+=2;

					x+=mTileWidth;
				}
				
				jankoRowNumber++;
				
				if (jankoRowNumber % groupSize == 0)
				{
				    pitch = rowFirstPitch - 12;
				    rowFirstPitch = pitch;
			    	octaveGroupNumber++;
				}
				pitch = rowFirstPitch;
				
				y += 3 * mTileRadius;
			}
		}
	}
	
	protected void setUpSonomeBoard()
	{
		int y = 0;
		String firstNote = mPrefs.getString("baseSonomeNote", "E");
		String firstOctaveStr = mPrefs.getString("baseSonomeOctave", "1");
		int firstOctave = Integer.parseInt(firstOctaveStr);
		int pitch = Note.getNoteNumber(firstNote, firstOctave);

		if (HexKey.getKeyOrientation(mContext).equals("Vertical"))
		{
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
					int kittyCornerY = y + mTileWidth/2;
					SonomeKey kittyCornerKey = new SonomeKey(
							mContext,
							mTileRadius,
							new Point(kittyCornerX, kittyCornerY),
							pitch,
							mInstrument);

					mKeys.add(kittyCornerKey);
					pitch+=4;

					SonomeKey key = new SonomeKey(
							mContext,
							mTileRadius,
							new Point(x, y),
							pitch,
							mInstrument);
					mKeys.add(key);
					pitch-=3;

					x += 3 * mTileRadius;
				}

				pitch = rowFirstPitch - 7; // Down a fifth.
				rowFirstPitch = pitch;
				y += mTileWidth;
			}
		}
		else
		{
			y = mTileRadius;
			
			int rowFirstPitch = pitch;
			
			for (int j = 0; j < mRowCount; j++)
			{	
				int x = mTileWidth / 2;

				for (int i = 0; i < mColumnCount; i++)
				{
					int kittyCornerX = (int)Math.round(x - mTileWidth / 2);
					int kittyCornerY = (int)Math.round(y - mTileRadius * 1.5);
					JammerKey kittyCornerKey = new JammerKey(
							mContext,
							mTileRadius,
							new Point(kittyCornerX, kittyCornerY),
							pitch,
							mInstrument);
					mKeys.add(kittyCornerKey);
					
					pitch+=4;

					JammerKey key = new JammerKey(
							mContext,
							mTileRadius,
							new Point(x, y),
							pitch,
							mInstrument);
					mKeys.add(key);
					
					pitch+=3;

					x += mTileWidth;
				}

				pitch = rowFirstPitch + 1; // Up a semitone.
				rowFirstPitch = pitch;
				y += 3 * mTileRadius;
			}
		}
	}

	boolean screenIsNaturallyLandscape()
	{
		if (mDisplayWidth > mDisplayHeight)
		{
			return true;
		}

		return false;
	}

	private int getCanvasHeight()
	{
		int canvasHeight = mDisplayHeight;

		if (mScreenOrientationId == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		{
			if (mDisplayWidth > mDisplayHeight) // Natural landscape display
			{
				canvasHeight = mDisplayWidth;
			}
		}
		else // orientation is landscape
		{
			if (mDisplayWidth < mDisplayHeight) // Natural portrait display
			{
				canvasHeight = mDisplayWidth;
			}
		}

		return canvasHeight;
	}

	private int getCanvasWidth()
	{
		int canvasWidth = mDisplayWidth;

		if (mScreenOrientationId == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		{
			if (mDisplayWidth > mDisplayHeight) // Natural landscape display
			{
				canvasWidth = mDisplayHeight;
			}
		}
		else // orientation is landscape
		{
			if (mDisplayWidth < mDisplayHeight) // Natural portrait display
			{
				canvasWidth = mDisplayHeight;
			}
		}

		return canvasWidth;
	}

	private int getRowCount()
	{
		int rowCount = 0;

		int canvasHeight = getCanvasHeight();
		if (HexKey.getKeyOrientation(mContext).equals("Vertical"))
		{
			rowCount = canvasHeight/mTileWidth;
			rowCount++; // Add one in case there is an extra kitty-corner half-row.
			if (canvasHeight % mTileWidth > 0)
			{
				rowCount++;
			}
			Log.d("HexKeyboard", "mRowCount: " + mRowCount);
		}
		else
		{
			rowCount = canvasHeight/(3 * mTileRadius);
			int remainder = canvasHeight % (mTileRadius * 3);
			if (remainder > 0)
			{
				rowCount++;
				if (remainder > 2 * mTileRadius)
				{
					rowCount++;
				}
			}
		}

		return rowCount;
	}

	private int getColumnCount()
	{
		int columnCount = 0;

		int canvasWidth = getCanvasWidth();
		if (HexKey.getKeyOrientation(mContext).equals("Vertical"))
		{
			columnCount = canvasWidth/(mTileRadius * 3) + 1; // Add one for the possible extra kitty-korner.
			int remainder = canvasWidth % (mTileRadius * 3);

			if (remainder > 0)
			{
				columnCount++;
				if (remainder > 2 * mTileRadius)
				{
					columnCount++;
				}
			}
		}
		else
		{
			columnCount = canvasWidth/mTileWidth;
			columnCount++; // Add one in case there is an extra kitty-corner half-row.
			if (canvasWidth % mTileWidth > 0)
			{
				columnCount++;
			}
		}

		return columnCount;
	}

	void setUpBoard(int screenOrientationId)
	{
		mKeys.clear();

		mScreenOrientationId = screenOrientationId;
		Log.d("setUpBoard", "screenOrientationId: " + mScreenOrientationId);

		mTileRadius = (3 * mDpi) / 8;
		String scaleStr = mPrefs.getString("scale", "100");
		scaleStr = scaleStr.replaceAll("[^0-9]", "");
		if (scaleStr.length() == 0)
		{
			scaleStr = "100";
		}

		int scalePct = Integer.parseInt(scaleStr);
		mTileRadius = (mTileRadius * scalePct) / 100;
		mTileWidth = (int)(Math.round(Math.sqrt(3.0) * mTileRadius));
		
		mRowCount = getRowCount();
		mColumnCount = getColumnCount();

		String layoutPref = mPrefs.getString("layout", "Sonome");
		if (layoutPref.equals("Sonome"))
		{
			this.setUpSonomeBoard();
		}
		else if (layoutPref.equals("Jammer"))
		{
			this.setUpJammerBoard();
		}
		else if (layoutPref.equals("Janko"))
		{
			this.setUpJankoBoard();
		}

		int canvasWidth = getCanvasWidth();
		int canvasHeight = getCanvasHeight();
		mBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(mBitmap);
		this.onDraw(tempCanvas);
	}

	public HexKeyboard(Context context, View ad)
	{
		super(context);
		mAdView = ad;
		init(context);
		if (mStartTime == 0L)
		{
			mStartTime = SystemClock.uptimeMillis();
			mAdHandler.removeCallbacks(mAdUpdater);
			mAdHandler.postDelayed(mAdUpdater, mAdDelayMilliseconds);
		}
	}

	private void init(Context context)
	{
		mContext = context;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		mDpi = dm.densityDpi;

		mInstrument = new Piano(context);
		mDisplayWidth = width;
		mDisplayHeight = height;
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
		int lowerBound = mTileWidth/2;
		for (int j = 0; j <= mRowCount; j++)
		{
			if (y < lowerBound)
			{
				break;
			}

			lowerBound += mTileWidth/2;

			if (y < lowerBound)
			{
				break;
			}

			lowerBound += mTileWidth/2;
		}

		return lowerBound;
	}

	private int getHorizontalSliceNumber(int x, int y)
	{
		int lowerBound = mTileWidth/2;
		for (int j = 0; j <= mRowCount; j++)
		{
			if (y < lowerBound)
			{
				return(2*j);
			}

			lowerBound += mTileWidth/2;

			if (y < lowerBound)
			{
				return(2*j + 1);
			}

			lowerBound += mTileWidth/2;
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

		mLastRedrawTime = SystemClock.uptimeMillis();
		Log.d("onDraw", "Last redraw: " + mLastRedrawTime);
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
					mAdView.setVisibility(AdView.INVISIBLE);
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