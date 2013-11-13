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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public abstract class Instrument {

	public static int POLYPHONY_COUNT = 16;
	public SoundPool mSoundPool;
	protected static HashMap<Integer, TreeMap<Integer, Integer>> mSounds; // [midiNoteNumber, [velocity, SoundPool sound id]] 
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
		mSounds = new HashMap<Integer, TreeMap<Integer, Integer>>(); 
		mRates = new HashMap<Integer, Float>(); 
		mRootNotes = new HashMap<Integer, Integer>(); 
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE); 	     
	} 

	// Load into SoundPool a sound from the APK ressources
	public void addSound(int midiNoteNumber, int velocity, int soundId) // soundId is the APK ressource ID (given by java); index is the midiNoteNumber
	{
		// If there's already an entry for this midinote, we update it to add the new velocity subentry
		if (mSounds.containsKey(midiNoteNumber)) {
			TreeMap<Integer, Integer> velocity_soundid = mSounds.get(midiNoteNumber); // fetch the midinote entry (containing all previous velocity subentries)
			velocity_soundid.put(velocity, mSoundPool.load(mContext, soundId, 1)); // add the new velocity subentry
			mSounds.put(midiNoteNumber, velocity_soundid); // update it back into the midinote entry
		// Else there's no entry for this midinote, we just create it
		} else {
			// Just create an entry for this midinote and use the velocity as the only subentry (until it gets updated if there are other velocities available for this midi note)
			TreeMap<Integer, Integer> velocity_soundid = new TreeMap<Integer, Integer>();
			velocity_soundid.put(velocity, mSoundPool.load(mContext, soundId, 1));
			mSounds.put(midiNoteNumber, velocity_soundid);
		}
	}
	
	// Load into SoundPool an external sound from a given path (eg: on SD card)
	public void addSound(int midiNoteNumber, int velocity, String path) // path is the full path to a sound file; index is the midiNoteNumber
	{
		// If there's already an entry for this midinote, we update it to add the new velocity subentry
		if (mSounds.containsKey(midiNoteNumber)) {
			TreeMap<Integer, Integer> velocity_soundid = mSounds.get(midiNoteNumber); // fetch the midinote entry (containing all previous velocity subentries)
			velocity_soundid.put(velocity, mSoundPool.load(path, 1)); // add the new velocity subentry
			mSounds.put(midiNoteNumber, velocity_soundid); // update it back into the midinote entry
		// Else there's no entry for this midinote, we just create it
		} else {
			// Just create an entry for this midinote and use the velocity as the only subentry (until it gets updated if there are other velocities available for this midi note)
			TreeMap<Integer, Integer> velocity_soundid = new TreeMap<Integer, Integer>();
			velocity_soundid.put(velocity, mSoundPool.load(path, 1));
			mSounds.put(midiNoteNumber, velocity_soundid);
		}
	}
	
	public int[] play(int midiNoteNumber, float pressure)
	{
		return this.play(midiNoteNumber, pressure, 0);
	}

	public int[] play(int midiNoteNumber, float pressure, int loop)
	{ 
		Log.d("Instrument", "play(" + midiNoteNumber + ")");
		if (mRootNotes.size() == 0) return new int[] {0}; // no sound note available 
		int index = mRootNotes.get(midiNoteNumber);
		if (!mSounds.containsKey(index)) return new int[] {-1};
		Log.d("Instrument", "rootNote is " + index + ")");
	    float rate = mRates.get(midiNoteNumber);
	
		//float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	    float streamVolume = 1.0f;

		// Play with the correct velocity
		TreeMap<Integer, Integer> velocity_soundid = mSounds.get(index);
		
		// scale the value (normalize between min and max pressure, and then scale over min and max velocity notes for this midi note)
		int max_vel = velocity_soundid.lastKey();
		int min_vel = velocity_soundid.firstKey();
		int velocity = 0;
		if (HexKeyboard.mVelocityRelativeRange) {
			// Relative range = min midi note velocity - max midi note velocity (change for each note!)
			velocity = Math.round( (pressure-HexKeyboard.mMinPressure)/(HexKeyboard.mMaxPressure-HexKeyboard.mMinPressure) * (max_vel-min_vel) + min_vel );
		} else {
			// Absolute range = 0-127 (like real midi velocity)
			velocity = Math.round( (pressure-HexKeyboard.mMinPressure)/(HexKeyboard.mMaxPressure-HexKeyboard.mMinPressure) * 127 );
		}
		
		int previous_vel = 0;
		int current_vel = 0;
		int soundid = 0;
		int soundid2 = 0;
		float stream1Volume = 0;
		float stream2Volume = 0;
		// TreeMap ensures that entries are always ordered by velocity (from lowest to highest), thus a subsequent velocity may only be higher than the previous one
		for (TreeMap.Entry<Integer, Integer> vel : velocity_soundid.entrySet()) {
			current_vel = vel.getKey();
			if (current_vel == velocity ||
					(current_vel > velocity && previous_vel == 0)) {
				soundid = vel.getValue();
				break;
			} else if (current_vel > velocity && previous_vel != 0) {
				soundid = velocity_soundid.get(previous_vel);
				soundid2 = vel.getValue(); // == velocity_soundid.get(current_vel)
				int vdiff = current_vel - previous_vel;
				stream2Volume = (float)(velocity - previous_vel) / vdiff * streamVolume;
				stream1Volume = (float)(current_vel - velocity) / vdiff * streamVolume;
				break;
			}
			previous_vel = current_vel;
		}
		if (soundid == 0) {
			if (previous_vel != 0) {
				soundid = velocity_soundid.get(previous_vel);
			} else {
				soundid = velocity_soundid.get(current_vel);
			}
		}
		
		// TODO: debug line
		Log.d("Instrument::play", "midinote: " + midiNoteNumber + " soundid: " + Integer.toString(soundid) + " soundid2: "+ Integer.toString(soundid2) + " velocity " + velocity + " current_vel " + current_vel + "previous_vel" + previous_vel + " pressure " + Float.toString(pressure) + " max/min " + Float.toString(HexKeyboard.mMaxPressure) + "/" + Float.toString(HexKeyboard.mMinPressure) + " s/s1/s2 vol " + Float.toString(streamVolume) + "/" + Float.toString(stream1Volume) + "/" + Float.toString(stream2Volume));
		
		if (soundid2 != 0) {
			return new int[] {mSoundPool.play(soundid, stream1Volume, stream1Volume, 1, 0, rate),
			        mSoundPool.play(soundid2, stream2Volume, stream2Volume, 1, 0, rate)};
		} else {
			return new int[] {mSoundPool.play(soundid, streamVolume, streamVolume, 1, 0, rate)};
		}
	}

	public void stop(int[] mStreamId)
	{ 
		for(int streamId : mStreamId) {
			mSoundPool.stop(streamId);
		}
	}

	// Play and loop indefinitely a sound
	public int[] loop(int midiNoteNumber, float pressure)
	{
		return this.play(midiNoteNumber, pressure, -1);
		/*
		int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
		mSoundPool.play(mSounds.get(index), streamVolume, streamVolume, 1, -1, 1f);
		*/ 
	}
}
