/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft  @ 2013 Stephen Larroque                                     *
 *   Copyright � 2012 James Haigh                                          *
 *   Copyright � 2011, 2012 David A. Randolph                              *
 *                                                                         *
 *   FILE: HexKeyboard.java                                                *
 *                                                                         *
 *   This file is part of Hexiano, an open-source project hosted at:       *
 *   https://github.com/lrq3000/hexiano                                         *
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

import java.lang.Math;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import android.view.WindowManager;

public class HexKeyboard extends View 
{
	static int mScreenOrientationId = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	static SharedPreferences mPrefs;
	static SharedPreferences.Editor mPrefsEditor;
	static Context mContext;
	static Bitmap mBitmap;
	static int mDpi = 0;
	static double mDisplayInches = 0;
	static int mDisplayWidth = 0;
	static int mDisplayHeight = 0;
	static int mRowCount = 0;
	static int mColumnCount = 0;
	static int mTileRadius = 64; 
	static int mTileWidth = 0;
	static int mScale = 100; // Unused
	static int mTouchScale = 100;
	private static long mLastRedrawTime = 0L;
	//private static final long mAdDelayMilliseconds = 12000L;
	private static long mStartTime = 0L;
	// Modifier keys options
	static boolean mHideModifierKeys = false; // hide all modifier keys
	static boolean mSustainHold = true; // hold sustain (when you release the key, you will have to press it twice to disable sustain)
	static boolean mSustainAlwaysOn = false; // sustain is always enabled (sustain key will then be used to stop the previously sustained notes)
	// Modifier keys state
	static boolean mSustain = false; // current state of sustain key (pressed or not) - this state is separate from SustainAlwaysOn

	static Set<Integer> old_pressed = new HashSet<Integer>();

	static ArrayList<HexKey> mKeys = new ArrayList<HexKey>();
	
	// Velocity/Pressure sensitivity
	static boolean mVelocityEnabled = false;
	static boolean mVelocityRelativeRange = true; // Absolute range = 0-127, relative range = min midi note velocity - max midi note velocity (change for each note!)
	static int mVelocityBoost = 0; // Boost velocity volume by this percentage
	static float mMaxPressure = 0.0f; // Init max pressure to 0, will be raised automatically upon first (and subsequent) touch event in onTouchEvent()
	static float mMinPressure = 1.0f; // Init min pressure to 1, will be lowered automatically upon first (and subsequent) touch event onTouchEvent()
	static float mPressure;

	/*
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
	*/

	// Given the position and parameters of an HexKey, return the id of the multiInstrument to use for this key
	// Note: in case of conflicts (multiple mappings covering the same note), return the last mapping covering this key
	protected Integer getMultiInstrument(TreeMap<Integer, HashMap<String, String>> multiInstruments, int keyCount, int column, int row) {
		Integer finalInstruId = null;
		String finalInstruName = null;
		// Loop through all mappings, in ascending order of id
		for (HashMap<String, String> mapping : multiInstruments.values()) {
			boolean foundMatch = false;
			Integer instruId = Integer.parseInt(mapping.get("id"));
			String instruName = mapping.get("instrument");
			String rangeType = mapping.get("rangeType");
			int rangeStart = Integer.parseInt(mapping.get("rangeStart"));
			int rangeEnd = Integer.parseInt(mapping.get("rangeEnd"));
			// Mapping range by key
			if (rangeType.equals("Key") && rangeStart <= (keyCount+1) && (keyCount+1) <= rangeEnd ) {
				foundMatch = true;
			// Mapping range by column
			} else if (rangeType.equals("Column") && rangeStart <= column && column <= rangeEnd) {
				foundMatch = true;
			// Mapping range by row
			} else if (rangeType.equals("Row") && rangeStart <= row && row <= rangeEnd) {
				foundMatch = true;
			}
			// If found a mapping
			if (foundMatch) {
				finalInstruId = instruId;
				finalInstruName = instruName;
			}
		}
		if (finalInstruId != null) {
			Log.d("HexKeyboard::getMultiInstrument", "Key " + Integer.toString(keyCount+1) + " Instrument:" + finalInstruName + " Id:" + Integer.toString(finalInstruId));
		} else {
			Log.d("HexKeyboard::getMultiInstrument", "Key " + Integer.toString(keyCount+1) + " Instrument: none found! Backing to single instrument.");
		}
		return finalInstruId;
	}
	
	protected void setUpKeyBoard(String board)
	{
		// Get general layout options (options common to all boards)
		String firstNote = mPrefs.getString("base"+board+"Note", null);
		String firstOctaveStr = mPrefs.getString("base"+board+"Octave", null);
		int firstOctave = Integer.parseInt(firstOctaveStr);
		int pitch = Note.getNoteNumber(firstNote, firstOctave);
		int keyCount = 0;
		
		// For Janko only
		String groupSizeStr = mPrefs.getString("jankoRowCount", null);
		groupSizeStr.replaceAll("[^0-9]", "");
		if (groupSizeStr.length() == 0)
		{
			groupSizeStr = "4";
		}
	    int groupSize = Integer.parseInt(groupSizeStr);
	  
	    int groupCount = mColumnCount / groupSize;
	    if (mColumnCount % groupSize > 0)
	    {
	    	groupCount++;
	    }

		// Setup notes progression for boards
		int pitchvpre = 0;
		int pitchv1 = 0;
		int pitchv2 = 0;
		int pitchvpost = 0;
		int pitchhpre = 0;
		int pitchh1 = 0;
		int pitchh2 = 0;
		int pitchhpost = 0;
		if (board.equals("Jammer")) {
			pitchv1 = -5;
			pitchv2 = -7;
			pitchvpost = -2; // Down a full tone.
			pitchhpre = -(mRowCount - 1) * 2;
			pitchh1 = -5;
			pitchh2 = 7;
			pitchhpost = -12; // Down a full tone.
		} else if (board.equals("Sonome")) {
			pitchvpre = (mRowCount - 1) * 7;
			pitchv1 = 4;
			pitchv2 = -3;
			pitchvpost = -7; // Down a fifth.
			pitchh1 = 4;
			pitchh2 = 3;
			pitchhpost = 1; // Up a semitone.
		} else if (board.equals("Janko")) {
			pitch -= (mColumnCount - 1) * 2 - 1;
			pitchvpre = -(groupCount - 1) * 12;
			pitchv1 = -1;
			pitchv2 = 1;
			pitchvpost = 2;
			pitchh1 = 1;
			pitchh2 = 1;
		}
		
		// Setup single instrument and multi-instruments
		boolean soundsLimitRange = mPrefs.getBoolean("soundsLimitRange", true);
		// Range limits for multi-instruments
		HashMap<String, ArrayList<Integer>> miRangeLimits = new HashMap<String, ArrayList<Integer>>(); // instrument's name ; list of notes
		for (String instruName : Play.mInstrument.keySet()) {
			miRangeLimits.put(instruName, new ArrayList<Integer>());
		}
		// Loading mapping of instruments
		boolean multiInstrumentsEnabled = mPrefs.getBoolean("multiInstrumentsEnable", false);
		TreeMap<Integer, HashMap<String, String>> multiInstruments = null;
		String singleInstrument = mPrefs.getString("instrument", "Piano"); // Single instrument: always load it as it will also be the base default instrument when multi-instruments is enabled but some keys are not mapped
		if (multiInstrumentsEnabled) { // Multi instruments with mapping
			multiInstruments = Prefer.getMultiInstrumentsMappingHashMap(mPrefs);
			// If mapping is empty, we switch to single instrument mode
			if (multiInstruments == null || multiInstruments.size() == 0) {
				multiInstrumentsEnabled = false;
				singleInstrument = mPrefs.getString("instrument", "Piano");
			}
		}
		// Get the differential pitch mapping for multi-instruments (how much we must shift the layout's pitch to find one instrument pitch)
		// Note: how it works: we map the whole keyboard using a single pitch var, just as if the whole keyboard was using one single instrument. This way, we get a reference for the layout and evolution of pitch. Then for each multi-instrument mapping, we compute the differential pitch (dpitch) which allow then to compute any multi-instrument's pitch (ipitch) relatively to the general layout pitch - dpitch. This effectively keep the same layout for any multi-instrument mapping at all time, even if the baseNote and baseOctave are different, the pitch will just be different for those notes.
		HashMap<Integer, Integer> miPitchDiff = new HashMap<Integer, Integer>();
		if (multiInstrumentsEnabled) {
			for (HashMap<String, String> mapping : multiInstruments.values()) {
				// Get instrument's id in the mapping
				int id = Integer.parseInt(mapping.get("id"));
				// Get the baseNote and baseOctave
				String baseNote = mapping.get("baseNote");
				String baseOctaveStr = mapping.get("baseOctave");
				int baseOctave = Integer.parseInt(baseOctaveStr);
				// Compute the differential pitch (difference between layout's pitch and instrument's pitch)
				int dpitch = Note.getNoteNumber(baseNote, baseOctave);
				int diffpitch = pitch - dpitch;
				// Memorize in the list for future reference (optimization, avoiding recomputing)
				miPitchDiff.put(id, diffpitch);
			}
				
		}
		

		if (HexKey.getKeyOrientation(mContext).equals("Vertical"))
		{
			Log.d("setUp"+board+"Board", "orientation: vertical");
			Log.d("setUp"+board+"Board", "pitch: " + pitch);
			Log.d("setUp"+board+"Board", "rowCount: " + mRowCount);
			Log.d("setUp"+board+"Board", "columnCount: " + mColumnCount);
	
			int y = 0;
			pitch += pitchvpre;
			int rowFirstPitch = pitch;

			for (int j = 0; j < mRowCount; j++)
			{	
				int x = mTileRadius;
				
				int octaveGroupNumber = 0; // for Janko only
			    int jankoColumnNumber = 0; // for Janko only

				for (int i = 0; i < mColumnCount; i++)
				{
					// Setup instrument to load for the next key
					Instrument instru = null;
					String instruName = null;
					Integer instruId = null;
					if (!multiInstrumentsEnabled) { // Single instrument
						instruName = singleInstrument;
					} else { // Multi instruments
						instruId = getMultiInstrument(multiInstruments, keyCount, i, j); // get the instrument id corresponding to the next key position and parameters
						if (instruId != null) {
							// Get multi-instrument mapping if one is found for the next key
							instruName = multiInstruments.get(instruId).get("instrument");
						} else {
							// If no instrument mapping covers this key, by default play the single instrument on all undefined keys in the mapping
							instruName = singleInstrument;
						}
					}
					instru = Play.mInstrument.get(instruName);
					// Multi-instruments: change pitch only if this multi-instrument uses its own baseNote and baseOctave (does not use the layout's baseNote and baseOctave)
					int ipitch = pitch; // pitch for current instrument
					if (multiInstrumentsEnabled && instruId != null && multiInstruments.get(instruId).get("keepBaseNoteOctave").equals("false")) {
						ipitch = pitch - miPitchDiff.get(instruId);
					}
					
					// Setup kitty corner key
					int kittyCornerX = (int)Math.round(x - mTileRadius * 1.5);
					int kittyCornerY = y + mTileWidth/2;
					
					HexKey kittyCornerKey = null;
					if (board.equals("Jammer")) {
						kittyCornerKey = new JammerKey(
								mContext,
								mTileRadius,
								new Point(kittyCornerX, kittyCornerY),
								ipitch,
								instru,
								++keyCount);
					} else if (board.equals("Sonome")) {
						kittyCornerKey = new SonomeKey(
								mContext,
								mTileRadius,
								new Point(kittyCornerX, kittyCornerY),
								ipitch,
								instru,
								++keyCount);
					} else if (board.equals("Janko")) {
						kittyCornerKey = new JankoKey(
								mContext,
								mTileRadius,
								new Point(kittyCornerX, kittyCornerY),
								ipitch + 12 * octaveGroupNumber,
								instru,
								++keyCount,
								octaveGroupNumber);
					}

					if (kittyCornerKey.isKeyVisible()) {
						mKeys.add(kittyCornerKey);
						Log.d("HexKeyboard::setUpKeyBoard", "setUpKeyBoard Key: " + Integer.toString(keyCount-1) + " kittyCornerKey i/j " + Integer.toString(i) + "/" + Integer.toString(j));
						// Limit sounds range: add this pitch (midiNoteNumber) to the list of notes to keep (notes that are visible on screen)
						if (board.equals("Janko")) {
							miRangeLimits.get(instruName).add(ipitch + 12 * octaveGroupNumber);
						} else {
							miRangeLimits.get(instruName).add(ipitch);
						}
					} else {
						--keyCount;
					}
					pitch += pitchv1;
					
					jankoColumnNumber++; // for Janko only
					if (jankoColumnNumber % groupSize == 0) octaveGroupNumber++; // for Janko only
					
					// Setup instrument to load for the next key
					instru = null;
					instruName = null;
					instruId = null;
					if (!multiInstrumentsEnabled) { // Single instrument
						instruName = singleInstrument;
					} else { // Multi instruments
						instruId = getMultiInstrument(multiInstruments, keyCount, i, j); // get the instrument id corresponding to the next key position and parameters
						if (instruId != null) {
							// Get multi-instrument mapping if one is found for the next key
							instruName = multiInstruments.get(instruId).get("instrument");
						} else {
							// If no instrument mapping covers this key, by default play the single instrument on all undefined keys in the mapping
							instruName = singleInstrument;
						}
					}
					instru = Play.mInstrument.get(instruName);
					// Multi-instruments: change pitch only if this multi-instrument uses its own baseNote and baseOctave (does not use the layout's baseNote and baseOctave)
					ipitch = pitch; // pitch for current instrument
					if (multiInstrumentsEnabled && instruId != null && multiInstruments.get(instruId).get("keepBaseNoteOctave").equals("false")) {
						ipitch = pitch - miPitchDiff.get(instruId);
					}

					// Setup key
					HexKey key = null;
					if (board.equals("Jammer")) {
						key = new JammerKey(
								mContext,
								mTileRadius,
								new Point(x, y),
								ipitch,
								instru,
								++keyCount);
					} else if (board.equals("Sonome")) {
						key = new SonomeKey(
								mContext,
								mTileRadius,
								new Point(x, y),
								ipitch,
								instru,
								++keyCount);
					} else if (board.equals("Janko")) {
						key = new JankoKey(
								mContext,
								mTileRadius,
								new Point(x, y),
								ipitch + 12 * octaveGroupNumber,
								instru,
								++keyCount,
								octaveGroupNumber);
					}

					if (key.isKeyVisible()) {
						mKeys.add(key);
						Log.d("HexKeyboard::setUpKeyBoard", "setUpKeyBoard Key: " + Integer.toString(keyCount-1) + " key i/j " + Integer.toString(i) + "/" + Integer.toString(j));
						// Limit sounds range: add this pitch (midiNoteNumber) to the list of notes to keep (notes that are visible on screen)
						if (board.equals("Janko")) {
							miRangeLimits.get(instruName).add(ipitch + 12 * octaveGroupNumber);
						} else {
							miRangeLimits.get(instruName).add(ipitch);
						}
					} else {
						--keyCount;
					}
					pitch += pitchv2;
					
					jankoColumnNumber++; // for Janko only
					if (jankoColumnNumber % groupSize == 0) octaveGroupNumber++; // for Janko only

					x += 3 * mTileRadius;
				}

				pitch = rowFirstPitch + pitchvpost;
				rowFirstPitch = pitch;
				y += mTileWidth;
				
				/* if (board.equals("Janko") && jankoRowNumber % groupSize == 0)
				{
				    pitch -= 12;
				    rowFirstPitch = pitch;
			    	octaveGroupNumber++;
				} */
			}
		}
		else
		{
			Log.d("setUp"+board+"Board", "orientation: horizontal");
			Log.d("setUp"+board+"Board", "pitch: " + pitch);
			Log.d("setUp"+board+"Board", "rowCount: " + mRowCount);
			Log.d("setUp"+board+"Board", "columnCount: " + mColumnCount);
			
			int y = mTileRadius;
			
			pitch += pitchhpre;
			int rowFirstPitch = pitch;
			
			int octaveGroupNumber = 0; // for Janko only
		    int jankoRowNumber = 0; // for Janko only
			
			for (int j = 0; j < mRowCount; j++)
			{	
				int x = mTileWidth / 2;

				for (int i = 0; i < mColumnCount; i++)
				{
					// Setup instrument to load for the next key
					Instrument instru = null;
					String instruName = null;
					Integer instruId = null;
					if (!multiInstrumentsEnabled) { // Single instrument
						instruName = singleInstrument;
					} else { // Multi instruments
						instruId = getMultiInstrument(multiInstruments, keyCount, i, j); // get the instrument id corresponding to the next key position and parameters
						if (instruId != null) {
							// Get multi-instrument mapping if one is found for the next key
							instruName = multiInstruments.get(instruId).get("instrument");
						} else {
							// If no instrument mapping covers this key, by default play the single instrument on all undefined keys in the mapping
							instruName = singleInstrument;
						}
					}
					instru = Play.mInstrument.get(instruName);
					// Multi-instruments: change pitch only if this multi-instrument uses its own baseNote and baseOctave (does not use the layout's baseNote and baseOctave)
					int ipitch = pitch; // pitch for current instrument
					if (multiInstrumentsEnabled && instruId != null && multiInstruments.get(instruId).get("keepBaseNoteOctave").equals("false")) {
						ipitch = pitch - miPitchDiff.get(instruId);
					}
					
					// Setup kitty corner key
					int kittyCornerX = Math.round(x - mTileWidth / 2);
					int kittyCornerY = (int)Math.round(y - mTileRadius * 1.5);
					
					HexKey kittyCornerKey = null;
					if (board.equals("Jammer")) {
						kittyCornerKey = new JammerKey(
								mContext,
								mTileRadius,
								new Point(kittyCornerX, kittyCornerY),
								ipitch,
								instru,
								++keyCount);
					} else if (board.equals("Sonome")) {
						kittyCornerKey = new SonomeKey(
								mContext,
								mTileRadius,
								new Point(kittyCornerX, kittyCornerY),
								ipitch,
								instru,
								++keyCount);
					} else if (board.equals("Janko")) {
						kittyCornerKey = new JankoKey(
								mContext,
								mTileRadius,
								new Point(kittyCornerX, kittyCornerY),
								ipitch,
								instru,
								++keyCount,
								octaveGroupNumber);
					}
					
					if (kittyCornerKey.isKeyVisible()) {
						mKeys.add(kittyCornerKey);
						Log.d("HexKeyboard::setUpKeyBoard", "setUpKeyBoard Key: " + Integer.toString(keyCount-1) + " kittyCornerKey i/j " + Integer.toString(i) + "/" + Integer.toString(j));
						// Limit sounds range: add this pitch (midiNoteNumber) to the list of notes to keep (notes that are visible on screen)
						miRangeLimits.get(instruName).add(ipitch);
					} else {
						--keyCount;
					}
					
					pitch += pitchh1;
					
					// Setup instrument to load for the next key
					instru = null;
					instruName = null;
					instruId = null;
					if (!multiInstrumentsEnabled) { // Single instrument
						instruName = singleInstrument;
					} else { // Multi instruments
						instruId = getMultiInstrument(multiInstruments, keyCount, i, j); // get the instrument id corresponding to the next key position and parameters
						if (instruId != null) {
							// Get multi-instrument mapping if one is found for the next key
							instruName = multiInstruments.get(instruId).get("instrument");
						} else {
							// If no instrument mapping covers this key, by default play the single instrument on all undefined keys in the mapping
							instruName = singleInstrument;
						}
					}
					instru = Play.mInstrument.get(instruName);
					// Multi-instruments: change pitch only if this multi-instrument uses its own baseNote and baseOctave (does not use the layout's baseNote and baseOctave)
					ipitch = pitch; // pitch for current instrument
					if (multiInstrumentsEnabled && instruId != null && multiInstruments.get(instruId).get("keepBaseNoteOctave").equals("false")) {
						ipitch = pitch - miPitchDiff.get(instruId);
					}

					// Setup key
					HexKey key = null;
					int jpitch = ipitch;
					if (board.equals("Jammer")) {
						key = new JammerKey(
								mContext,
								mTileRadius,
								new Point(x, y),
								ipitch,
								instru,
								++keyCount);
					} else if (board.equals("Sonome")) {
						key = new SonomeKey(
								mContext,
								mTileRadius,
								new Point(x, y),
								ipitch,
								instru,
								++keyCount);
					} else if (board.equals("Janko")) {
						int ogn = octaveGroupNumber;
						if ((jankoRowNumber+1) % groupSize == 0) // check if the next line should be the same octave or another one (because Janko keyboard is made of 3 rows chunks, not 2 like Jammer or Sonome)
					    {
					    	ogn++;
					    	jpitch -= 12;
					    }
						key = new JankoKey(
								mContext,
								mTileRadius,
								new Point(x, y),
								jpitch,
								instru,
								++keyCount,
								ogn);
					}

					if (key.isKeyVisible()) {
						mKeys.add(key);
						Log.d("HexKeyboard::setUpKeyBoard", "setUpKeyBoard Key: " + Integer.toString(keyCount-1) + " key i/j " + Integer.toString(i) + "/" + Integer.toString(j));
						// Limit sounds range: add this pitch (midiNoteNumber) to the list of notes to keep (notes that are visible on screen)
						if (board.equals("Janko")) {
							miRangeLimits.get(instruName).add(jpitch);
						} else {
							miRangeLimits.get(instruName).add(ipitch);
						}
					} else {
						--keyCount;
					}
					
					pitch += pitchh2;

					x += mTileWidth;
				}

				pitch = rowFirstPitch + pitchhpost;
				rowFirstPitch = pitch;
				y += 3 * mTileRadius;
				
				// for Janko only
				jankoRowNumber += 2;
				if (board.equals("Janko") && jankoRowNumber % groupSize == 0)
				{
				    pitch -= 12;
				    rowFirstPitch = pitch;
			    	octaveGroupNumber++;
				}

			}
		}
		
		// Trim uselessly loaded sounds
		if (soundsLimitRange) { // if enabled in config
			for (Entry<String, Instrument> instrument : Play.mInstrument.entrySet()) {
				String instruName = instrument.getKey();
				Instrument instru = instrument.getValue();
				instru.limitRange(miRangeLimits.get(instruName));
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
	
	// Stop playing all sounds
	static public void stopAll()
	{
		for(HexKey key : HexKeyboard.mKeys ) {
			key.stop(true);
		}
	}
	
	private int getModifierKeysCount() {
		return 1; // only Sustain key for now
	}

	void setUpBoard(int screenOrientationId)
	{
		mKeys.clear();
		
		mHideModifierKeys = mPrefs.getBoolean("hideModifierKeys", false);
		mSustainHold = mPrefs.getBoolean("sustainHold", true);
		mSustainAlwaysOn = mPrefs.getBoolean("sustainAlwaysOn", false);
		mVelocityEnabled = mPrefs.getBoolean("velocityEnabled", false);
		// Setup (or reset) mMaxPressure and mMinPressure
		if (!mVelocityEnabled) { // if no velocity is set, we want to make sure that there's a range between min and max pressure, and max pressure will then always be used (TODO: add a slider-like modifier button to set velocity without pressure).
			mMaxPressure = 1.0f; mMinPressure = 0.0f;
		} else { // else we set obvious absurd values so that they get autocalibrated next time the user touch the screen
			mMaxPressure = 0.0f; mMinPressure = 1.0f;
		}
		mVelocityRelativeRange = mPrefs.getBoolean("velocityRelativeRange", true);

		mScreenOrientationId = screenOrientationId;
		Log.d("setUpBoard", "screenOrientationId: " + mScreenOrientationId);

		// Set scale (size) of keys
		String scaleStr = mPrefs.getString("scale", ""); // for backward compatibility, we still have to set it as a string and then do a regexp, even if we could just set android:numeric="integer" in the preferences, but that would break compatibility...
		scaleStr = scaleStr.replaceAll("[^0-9]", "");
		int scalePct = 100;
		if (scaleStr.length() != 0) {
			scalePct = Integer.parseInt(scaleStr);
		// if scale (size of keys) is not yet set in preferences, compute a good one relative to the size of the screen
		} else {

			// By default, set the size to the nominal one of 100
			scalePct = 100;

			// Find if the device has a wide screen, in which case we just leave the size to 100 
			//Android Level 9 and up:
			Configuration config = getResources().getConfiguration();
			if (!((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==  
			    Configuration.SCREENLAYOUT_SIZE_XLARGE)) {
				
				// For small screens below magical size (8.9 inches), we compute a reduced size for keys (down to 50)
				float magicDisplayInches = 8.9f;
				if (mDisplayInches < magicDisplayInches) {
					scalePct = (int) (mDisplayInches * 100 / magicDisplayInches);
					// If size is too small (below 50) we set it to 50
					if (scalePct < 50) {
						scalePct = 50;
					}
				}
			}
			
			// Store the computed scale in preferences
			mPrefsEditor.putString("scale", Integer.toString(scalePct));
			mPrefsEditor.commit();
		}
		mScale = scalePct;
		
		// Set touch scale/surface of keys. This means that a key will appear bigger than the real surface where a user may trigger the key. This leaves a gap between keys to avoid false triggers with nearby keys.
		String touchScaleStr = mPrefs.getString("touchScale", ""); // for backward compatibility, we still have to set it as a string and then do a regexp, even if we could just set android:numeric="integer" in the preferences, but that would break compatibility...
		touchScaleStr = touchScaleStr.replaceAll("[^0-9]", "");
		int touchScalePct = 100;
		if (touchScaleStr.length() != 0) {
			touchScalePct = Integer.parseInt(touchScaleStr);
			if (touchScalePct < 1 || touchScalePct > 200) { // Below 100 will create a gap between keys (excellent to enhance precision); above 100 will create an overlap zone to trigger multiple notes at once
				touchScalePct = 100;
			}
		} else {
			touchScalePct = 100;
		}
		mTouchScale = touchScalePct;
		
		// Velocity boost by this percentage
		String velocityBoostStr = mPrefs.getString("velocityBoost", ""); // for backward compatibility, we still have to set it as a string and then do a regexp, even if we could just set android:numeric="integer" in the preferences, but that would break compatibility...
		velocityBoostStr = velocityBoostStr.replaceAll("[^0-9]", "");
		int velocityBoostPct = 0; // by default, no velocityBoost
		if (velocityBoostStr.length() != 0) {
			velocityBoostPct = Integer.parseInt(velocityBoostStr);
			if (velocityBoostPct < 0 || velocityBoostPct > 1000) {
				velocityBoostPct = 0;
			}
		}
		mVelocityBoost = velocityBoostPct;

		// Computing tile of keys relatively to scale and Dpi
		mTileRadius = (3 * mDpi) / 8;
		mTileRadius = (mTileRadius * scalePct) / 100;
		mTileWidth = (int)(Math.round(Math.sqrt(3.0) * mTileRadius));
		
		mRowCount = getRowCount();
		mColumnCount = getColumnCount();

		// Setup the layout (map the keys onscreen and create the array of functional HexKeys)
		String layoutPref = mPrefs.getString("layout", null);
		if (layoutPref.equals("Sonome"))
		{
			this.setUpKeyBoard("Sonome");
		}
		else if (layoutPref.equals("Jammer"))
		{
			this.setUpKeyBoard("Jammer");
		}
		else if (layoutPref.equals("Janko"))
		{
			this.setUpKeyBoard("Janko");
		}
		
		if (!mHideModifierKeys) {
			this.setUpModifierKeys();
		}
		
		Log.d("setUpBoard", "Total number of keys: " + Integer.toString(mKeys.size()));

		// Paint the board
		int canvasWidth = getCanvasWidth();
		int canvasHeight = getCanvasHeight();
		// Old inefficient way of creating a bitmap
		//mBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
		// New efficient way of creating a bitmap: load it from a dummy resource image (see http://stackoverflow.com/a/8527745/1121352 )
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inSampleSize = 8;
		options.inPurgeable = true;
		Bitmap resImage = BitmapFactory.decodeResource(getResources(), R.drawable.dummy, options);
		mBitmap = Bitmap.createScaledBitmap(resImage, canvasWidth, canvasHeight, false);
		// Setup the bitmap
		mBitmap.eraseColor(mKeys.get(0).mBlankColor);
		Canvas tempCanvas = new Canvas(mBitmap);
		this.draw(tempCanvas);
	}
	
	// Replace the first keys by modifier keys
	private void setUpModifierKeys() {
		// Sustain key
		mKeys.set(0, new SustainKey(
				mContext,
				mTileRadius,
				mKeys.get(0).mCenter,
				64, // useless, set directly in SustainKey class
				null, // null?
				1) // id of the key, used only as a label if set in config
		);
	}

	public HexKeyboard(Context context)
	{
		super(context);
		init(context);
		if (mStartTime == 0L)
		{
			mStartTime = SystemClock.uptimeMillis();
			/*
			mAdHandler.removeCallbacks(mAdUpdater);
			mAdHandler.postDelayed(mAdUpdater, mAdDelayMilliseconds);
			*/
		}
	}

	private void init(Context context)
	{
		mContext = context;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		mPrefsEditor = mPrefs.edit();

		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		mDpi = dm.densityDpi;

		double x = Math.pow(dm.widthPixels/dm.xdpi,2);
		double y = Math.pow(dm.heightPixels/dm.ydpi,2);
		mDisplayInches = Math.sqrt(x+y);

		mDisplayWidth = width;
		mDisplayHeight = height;
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

	@Override
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

	@Override
	public boolean onTouchEvent(MotionEvent event){
		Log.v("onMouse", event.toString());
		Log.v("onMouse", "" + event.getPointerCount());

		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		if (!(
			actionCode == MotionEvent.ACTION_DOWN ||
			actionCode == MotionEvent.ACTION_POINTER_DOWN ||
			actionCode == MotionEvent.ACTION_UP ||
			actionCode == MotionEvent.ACTION_POINTER_UP ||
			actionCode == MotionEvent.ACTION_MOVE
		)) {
			return false;
		}

		// TODO: Enhance that double loop, which for each touch event check that the touch coordinate are within the are of any key, thus it double loops: over each touch event (ok), and over all keys (not ok!). A drastic performance enhancement can be gained here. Be careful so that key touch surface overlapping still works (cannot constraint on an area for the check).
		Set<Integer> new_pressed = new HashSet<Integer>();
		HashMap<Integer, Float> pressed_map = new HashMap<Integer, Float>();
		float pressure;
		for (int i = 0; i < mKeys.size(); i++)
		{
			HexKey key = mKeys.get(i);
			for (int pointerIndex = 0; pointerIndex < event.getPointerCount(); pointerIndex++)
			{
				int x = (int)event.getX(pointerIndex);
				int y = (int)event.getY(pointerIndex);
				if (key.contains(x, y))
				{
					/*
					if (pressed_map.containsKey(i))
					{
						pressed_map.put(i, Math.max(event.getPressure(pointerIndex), pressed_map.get(i)));
					} else {
						pressed_map.put(i, event.getPressure(pointerIndex));
					}
					*/
					// MotionEvent.getPressure() returns the pressure on resistive screens, or fake pressure based on area covered by finger on capacitive screens (but pretty much the same thing since fingers are very squishy).
					// For a list of compatible devices see: https://code.google.com/p/markers-for-android/wiki/DeviceSupport
					// Or use the following command in ADB to check if this method is supported by your touch controller: getevent -pl
					pressure = event.getPressure(pointerIndex);
					pressed_map.put(i, pressure);
					// Auto-Calibration of pressure range (normally a float between 0 and 1 but may be > 1 on some devices, and some others may be totally off like returning always 1 or only a binary 0/1).
					if (pressure > mMaxPressure) {
						mMaxPressure = pressure;
					}
					if (pressure < mMinPressure) {
						mMinPressure = pressure;
					}
					// If this key is newly pressed (was released before), we will play it
					if (!(pointerIndex == event.getActionIndex() && (
						actionCode == MotionEvent.ACTION_UP ||
						actionCode == MotionEvent.ACTION_POINTER_UP )))
					{
						new_pressed.add(i);
					}
				}
			}
		}
		Log.v("onMouse", "old" + old_pressed.toString());
		Log.v("onMouse", "new" + new_pressed.toString());
		Log.v("onMouse", "pressure" + pressed_map.toString());

		Set<Integer> just_pressed = new HashSet<Integer>(new_pressed);
		just_pressed.removeAll(old_pressed);
		Iterator<Integer> it = just_pressed.iterator();
		while (it.hasNext()) {
			int i = it.next();
			try
			{
				// Play with real pressure if velocity sensitivity is enabled
				if (mVelocityEnabled) {
					mKeys.get(i).play(pressed_map.get(i));
				// Else we just always play the maximum value
				} else {
					mKeys.get(i).play(mMaxPressure);
				}
			}
			catch (Exception e)
			{
				Log.e("HexKeyboard::onMouse", "HexKey " + i + " not playable!"); // (or an exception occurred, remove this catch to show it)
				e.printStackTrace();
			}
			//this.invalidate();
		}

		Set<Integer> just_released = new HashSet<Integer>(old_pressed);
		just_released.removeAll(new_pressed);
		it = just_released.iterator();
		while (it.hasNext()) {
			int i = it.next();
			mKeys.get(i).stop();
			//this.invalidate();
		}

		// Set<Integer> same = new HashSet<Integer>(new_pressed);
		// same.retainAll(old_pressed); // Intersection, not used.

		// Log.v("onMouse", "new" + new_pressed.toString());
		old_pressed = new_pressed;
		this.invalidate();

		return true;
	}
}
