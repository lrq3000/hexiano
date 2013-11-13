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

	// Play a note sound given the midi number, the pressure and optionally a loop number (-1 for indefinite looping, 0 for no looping, >0 for a definite number of loops)
	// @return int[] array int of SoundPool StreamId (to be able to stop the streams later on)
	public int[] play(int midiNoteNumber, float pressure, int loop)
	{ 
		// == Get root note and frequency if this note is interpolated
		// Note: a root note is a midi number where we have a sound, the other midi notes sounds being interpolated
		Log.d("Instrument", "play(" + midiNoteNumber + ")");
		if (mRootNotes.size() == 0) return new int[] {0}; // no sound note available, exit
		int index = mRootNotes.get(midiNoteNumber); // get root note for this midi number
		Log.d("Instrument", "rootNote is " + index + ")");
		if (!mSounds.containsKey(index)) return new int[] {-1}; // no sound (root or interpolated) available (yet?) for this note, exit
	    float rate = mRates.get(midiNoteNumber); // Get the rate to which to play this note: 1.0f (normal) for root notes, another number for other notes (changing rate interpolates the note sound; rate is computed at loading)
	
		//float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	    float streamVolume = 1.0f; // streamVolume range: 0.0f - 1.0f

		// == Play with the correct velocity
		TreeMap<Integer, Integer> velocity_soundid = mSounds.get(index); // use a TreeMap to make sure that sounds are sorted in ascending order by velocity
		
		// -- Compute the velocity value from user's pressure and scale the value (normalize between min and max pressure, and then scale over min and max velocity notes for this midi note)
		int max_vel = velocity_soundid.lastKey(); // max velocity available for this note
		int min_vel = velocity_soundid.firstKey(); // min velocity available for this note
		int velocity = 0; // user's velocity from touch pressure/surface (will be computed from var pressure)
		if (HexKeyboard.mVelocityRelativeRange) {
			// Relative range = min midi note velocity - max midi note velocity (change for each note!)
			velocity = Math.round( (pressure-HexKeyboard.mMinPressure)/(HexKeyboard.mMaxPressure-HexKeyboard.mMinPressure) * (max_vel-min_vel) + min_vel );
		} else {
			// Absolute range = 0-127 (like real midi velocity)
			velocity = Math.round( (pressure-HexKeyboard.mMinPressure)/(HexKeyboard.mMaxPressure-HexKeyboard.mMinPressure) * 127 );
		}
		
		// -- Get the corresponding sound(s) for the user's velocity and from the available velocities
		int previous_vel = 0; // lower velocity bound if user's velocity is in-between
		int current_vel = 0; // higher velocity bound if user's velocity is in-between
		int soundid = 0; // lower velocity sound or sound exactly equal to user's velocity
		int soundid2 = 0; // higher velocity sound if user's velocity is in-between, or null
		float stream1Volume = 0; // volume for lower velocity sound if user's velocity is in-between, to allow for blending of two velocity sounds
		float stream2Volume = 0; // volume for higher velocity sound if user's velocity is in-between, to allow for blending of two velocity sounds

		// TreeMap ensures that entries are always ordered by velocity (from lowest to highest), thus a subsequent velocity may only be higher than the previous one
		// For each velocity available for this note (iterate in ascending order from lowest velocity to highest)
		for (TreeMap.Entry<Integer, Integer> vel : velocity_soundid.entrySet()) {
			current_vel = vel.getKey(); // get the current velocity in TreeMap

			// Case 1: higher bound: one sound when user's velocity is equal or lower than any available velocity sound
			if (current_vel == velocity || // if current available velocity is exactly equal to user's velocity
					(current_vel > velocity && previous_vel == 0)) { // or if it's above usen's velocity but there's no lower velocity available
				// Just use the current velocity sound
				soundid = vel.getValue();
				break; // Found our sound, exit the loop
			// Case 2: middle bound: two sounds when user's velocity is in-between two available velocity sounds
			} else if (current_vel > velocity && previous_vel != 0) {
				soundid = velocity_soundid.get(previous_vel); // get lower bound velocity sound
				soundid2 = vel.getValue(); // == velocity_soundid.get(current_vel); // higher bound velocity sound
				// Compute the streams volumes for sounds blending
				int vdiff = current_vel - previous_vel; // compute ratio between max and min available velocity, which will represent the max volume ratio
				stream2Volume = (float)(velocity - previous_vel) / vdiff * streamVolume; // compute lower velocity sound volume (note: inversed s1 and s2 on purpose, to avoid computing stream1Volume = 1.0f - stream1Volume and stream1Volume = 1.0f - stream2Volume)
				stream1Volume = (float)(current_vel - velocity) / vdiff * streamVolume; // compute higher velocity sound volume
				break; // Found our sounds, exit the loop
			}
			previous_vel = current_vel; // keep previous velocity (= lower bound velocity)
		}
		// Case 3: lower bound: one sound when user's velocity is higher than any available velocity (we iterated all available velocities and could not find any higher)
		if (soundid == 0) {
			if (previous_vel != 0) {
				soundid = velocity_soundid.get(previous_vel);
			} else {
				soundid = velocity_soundid.get(current_vel);
			}
		}

		Log.d("Instrument::play", "VelocityCheck: midinote: " + midiNoteNumber + " soundid: " + Integer.toString(soundid) + " soundid2: "+ Integer.toString(soundid2) + " velocity " + velocity + " previous_vel " + previous_vel + "current_vel" + current_vel + " pressure " + Float.toString(pressure) + " max/min " + Float.toString(HexKeyboard.mMaxPressure) + "/" + Float.toString(HexKeyboard.mMinPressure) + " s/s1/s2 vol " + Float.toString(streamVolume) + "/" + Float.toString(stream1Volume) + "/" + Float.toString(stream2Volume));
		
		// Velocity interpolation via Blending: we blend two velocity sounds (lower and higher bound) with volumes proportional to the user's velocity to interpolate the missing velocity sound
		if (soundid2 != 0) {
			return new int[] {mSoundPool.play(soundid, stream1Volume, stream1Volume, 1, 0, rate),
			        mSoundPool.play(soundid2, stream2Volume, stream2Volume, 1, 0, rate)};
		// Else no interpolation, we have found an exact match for the user's velocity
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
