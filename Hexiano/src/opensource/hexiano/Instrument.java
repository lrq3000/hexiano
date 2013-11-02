/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft  @ 2013 Stephen Larroque                                     *
 *   Copyright © 2012 James Haigh                                          *
 *   Copyright © 2011 David A. Randolph                                    *
 *                                                                         *
 *   FILE: Instrument.java                                                 *
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
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public abstract class Instrument {

	public static int POLYPHONY_COUNT = 8;
	public SoundPool mSoundPool;
	protected static HashMap<Integer, Integer> mSounds; 
	protected static HashMap<Integer, Float> mRates;
	protected static HashMap<Integer, Integer> mRootNotes;
	private AudioManager  mAudioManager;
	private Context mContext;
	public Iterator<ArrayList> sound_load_queue;
	public boolean mExternal = false; // Loading external files (needing to pass Strings instead of int[]?)
	public String mInstrumentName;

	public Instrument(Context context)
	{
		mContext = context;
		init(context);
	}

	public void init(Context context)
	{
		POLYPHONY_COUNT = Integer.parseInt(HexKeyboard.mPrefs.getString("polyphonyCount", "8"));
		mSoundPool = new SoundPool(POLYPHONY_COUNT, AudioManager.STREAM_MUSIC, 0); 
		mSounds = new HashMap<Integer, Integer>(); 
		mRates = new HashMap<Integer, Float>(); 
		mRootNotes = new HashMap<Integer, Integer>(); 
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE); 	     
	} 

	// Load into SoundPool a sound from the APK ressources
	public void addSound(int index, int soundId) // soundId is the APK ressource ID (given by java); index is the midiNoteNumber
	{
		mSounds.put(index, mSoundPool.load(mContext, soundId, 1));
	}
	
	// Load into SoundPool an external sound from a given path (eg: on SD card)
	public void addSound(int index, String path) // path is the full path to a sound file; index is the midiNoteNumber
	{
		mSounds.put(index, mSoundPool.load(path, 1));
	}

	public int play(int midiNoteNumber)
	{ 
		Log.d("Instrument", "play(" + midiNoteNumber + ")");
		int index = mRootNotes.get(midiNoteNumber);
		if (!mSounds.containsKey(index)) {return -1;}
		Log.d("Instrument", "rootNote is " + index + ")");
	    float rate = mRates.get(midiNoteNumber);
	
		int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
		return  mSoundPool.play(mSounds.get(index), streamVolume, streamVolume, 1, 0, rate); 
	}

	public void stop(int streamId)
	{ 
		mSoundPool.stop(streamId);
	}

	public void loop(int index)
	{ 
		int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
		mSoundPool.play(mSounds.get(index), streamVolume, streamVolume, 1, -1, 1f); 
	}
}
