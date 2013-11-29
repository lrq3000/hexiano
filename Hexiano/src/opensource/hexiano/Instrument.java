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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import android.content.Context;
import android.util.Log;

public abstract class Instrument {

	private Context mContext;
	//private AudioManager mAudioManager;
	// Instrument type definition
	public boolean mExternal = false; // Loading external files (needing to pass Strings instead of int[]?). Defines if the third argument in sounds tuples is a resource id (int) or a file path (string)
	public String mInstrumentName; // Important for reference
	// Sounds loading variables
	public TreeMap<Integer, List<ArrayList>> sounds_to_load; // raw list of sounds to load in SoundPool, each entry being a midiNoteNumber associated to a tuple of [midiNoteNumber, velocity, file resource id int or string file path]
	public Iterator<List<ArrayList>> notes_load_queue; // = sounds.iterator(); when iterating through sounds_to_load in SoundPool.onLoadComplete(), contains a list of sound files (of different velocities) for one note
	public List<ArrayList> currListOfTuples; // = notes_load_queue.next(); temporary holder that contains all sounds file (of different velocities) for one midi note
	public Iterator<ArrayList> sound_load_queue; // = currListOfTuples.iterator(); contains one sound file at a time
	// Sounds holder variables (when loading is completed)
	protected HashMap<Integer, TreeMap<Integer, Integer>> mSounds; // List of all already loaded sound files, with their id (relative to velocity) for each midi note, to easily play the sounds. Format: [midiNoteNumber, [velocity, SoundPool sound id]]
	protected HashMap<Integer, Float> mRates; // Extrapolation rates: All rates for each midi note. Used to extrapolate missing note sounds from existing notes sounds (rootNotes). The frequency is extrapolated from the nearest rootNote available (see mRootNotes for the association). Format: [midiNote, rate]
	protected HashMap<Integer, Integer> mRootNotes; // Extrapolation association vector: for each midi note, define the nearest rootNote from which it should be extrapolated (if it's not already a rootNote). Format: [midiNote, rootNote]

	public Instrument(Context context)
	{
		mContext = context;
		init(context);
	}

	public void init(Context context)
	{		
		mSounds = new HashMap<Integer, TreeMap<Integer, Integer>>(); 
		mRates = new HashMap<Integer, Float>(); 
		mRootNotes = new HashMap<Integer, Integer>(); 
		//mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE); 	     
	} 

	// Load into SoundPool a sound from the APK ressources
	public void addSound(int midiNoteNumber, int velocity, int soundId) // soundId is the APK ressource ID (given by java); index is the midiNoteNumber
	{
		// If there's already an entry for this midinote, we update it to add the new velocity subentry
		if (mSounds.containsKey(midiNoteNumber)) {
			TreeMap<Integer, Integer> velocity_soundid = mSounds.get(midiNoteNumber); // fetch the midinote entry (containing all previous velocity subentries)
			velocity_soundid.put(velocity, Play.mSoundPool.load(mContext, soundId, 1)); // add the new velocity subentry
			mSounds.put(midiNoteNumber, velocity_soundid); // update it back into the midinote entry
		// Else there's no entry for this midinote, we just create it
		} else {
			// Just create an entry for this midinote and use the velocity as the only subentry (until it gets updated if there are other velocities available for this midi note)
			TreeMap<Integer, Integer> velocity_soundid = new TreeMap<Integer, Integer>();
			velocity_soundid.put(velocity, Play.mSoundPool.load(mContext, soundId, 1));
			mSounds.put(midiNoteNumber, velocity_soundid);
		}
	}
	
	// Load into SoundPool an external sound from a given path (eg: on SD card)
	public void addSound(int midiNoteNumber, int velocity, String path) // path is the full path to a sound file; index is the midiNoteNumber
	{
		// If there's already an entry for this midinote, we update it to add the new velocity subentry
		if (mSounds.containsKey(midiNoteNumber)) {
			TreeMap<Integer, Integer> velocity_soundid = mSounds.get(midiNoteNumber); // fetch the midinote entry (containing all previous velocity subentries)
			velocity_soundid.put(velocity, Play.mSoundPool.load(path, 1)); // add the new velocity subentry
			mSounds.put(midiNoteNumber, velocity_soundid); // update it back into the midinote entry
		// Else there's no entry for this midinote, we just create it
		} else {
			// Just create an entry for this midinote and use the velocity as the only subentry (until it gets updated if there are other velocities available for this midi note)
			TreeMap<Integer, Integer> velocity_soundid = new TreeMap<Integer, Integer>();
			velocity_soundid.put(velocity, Play.mSoundPool.load(path, 1));
			mSounds.put(midiNoteNumber, velocity_soundid);
		}
	}
	
	// Limit the range of sounds and notes to the given list of notes
	public void limitRange(ArrayList<Integer> ListOfMidiNotesNumbers) {
		// Loop through all found notes (from sounds files)
		ArrayList<Integer> notesToDelete = new ArrayList<Integer>();
		for (int midiNoteNumber : sounds_to_load.keySet()) {
			/*
			TreeMap<Integer, Integer> velocity_soundid = mSounds.get(midiNoteNumber);
			for (int soundid : velocity_soundid.values()) {
				Play.mSoundPool.unload(soundid);
			}
			mSounds.remove(new Integer(midiNoteNumber));
			*/

			// If the note is not in the limited range and there's no note extrapolated from this note's sound, we remove it and its associated sounds
			if (!ListOfMidiNotesNumbers.contains(midiNoteNumber) && !mRootNotes.values().contains(midiNoteNumber) ) {
				notesToDelete.add(midiNoteNumber);
			}
		}
		// Delete notes
		if (notesToDelete.size() > 0) {
			for (int midiNoteNumber : notesToDelete) {
				if (sounds_to_load.containsKey(midiNoteNumber)) sounds_to_load.remove( new Integer(midiNoteNumber) );
				if (mRootNotes.containsKey(midiNoteNumber)) mRootNotes.remove( new Integer(midiNoteNumber) );
				if (mRates.containsKey(midiNoteNumber)) mRates.remove( new Integer(midiNoteNumber) );
			}
		}
		// Recreate the iterator to generate all sounds of all notes
		notes_load_queue = sounds_to_load.values().iterator();
	}
	
	public void extrapolateSoundNotes() {
		// Extrapolate missing notes from Root Notes (notes for which we have a sound file)
		float previousRate = 1.0f;
		int previousRootNote = -1;
		ArrayList<Integer> beforeEmptyNotes = new ArrayList<Integer>(); // Notes before any root note, that we will extrapolate (by downpitching) as soon as we find one root note. TODO: downpitching by default and uppitch only for the rest. Downpitching should not cause any aliasing, but we have to check if the extrapolation doesn't cause evil downpitching. See http://www.discodsp.com/highlife/aliasing/
		double oneTwelfth = 1.0/12.0;
		boolean firstRootNote = true;
		for (int noteId = 0; noteId < 128; noteId++)
		{
			// Found a new root note, we will extrapolate the next missing notes using this one
			if (mRootNotes.containsKey(noteId))
			{
				previousRootNote = noteId;
				previousRate = 1.0f;
				// Down-pitching extrapolation of before notes (notes before the first root note)
				if (firstRootNote) {
					// Only if we have before notes to extrapolate
					if (beforeEmptyNotes != null && beforeEmptyNotes.size() > 0) {
						for (int bNoteId : beforeEmptyNotes) {
							mRootNotes.put(bNoteId, previousRootNote);
							double beforeRate = previousRate / Math.pow(Math.pow(2, oneTwelfth), (previousRootNote-bNoteId)); // a = b / (2^1/12)^n , with n positive number of semitones between frequency a and b
							mRates.put(bNoteId, (float)beforeRate);
						}
					}
					firstRootNote = false;
				}
			}
			// Else we have a missing note here
			else
			{
				// Up-pitching extrapolation of after notes (notes after we have found the first, and subsequente, root note)
				if (previousRootNote >= 0) {
					mRootNotes.put(noteId, previousRootNote);
				    double newRate = previousRate * Math.pow(Math.pow(2, oneTwelfth), (noteId-previousRootNote)); // b = a * (2^1/12)^n , with n positive number of semitones between frequency a and b
				    //double newRate = previousRate * Math.pow(2, oneTwelfth);
				    //previousRate = (float)newRate;
					mRates.put(noteId, (float)newRate);
				} else {
					beforeEmptyNotes.add(noteId);
				}
			}
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
		Log.d("Instrument", "rootNote found: " + index);
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
		float pdiff = (HexKeyboard.mMaxPressure-HexKeyboard.mMinPressure);
		if (pdiff == 0) pdiff = 1.0f; // avoid division by 0
		int veldiff = (max_vel-min_vel); // no relative range if only one velocity is available! Need to use an absolute range and fake velocity
		if (HexKeyboard.mVelocityRelativeRange && veldiff > 0) {
			// Relative range = min midi note velocity - max midi note velocity (change for each note!)
			velocity = Math.round( (float)((pressure-HexKeyboard.mMinPressure)/pdiff) * veldiff + min_vel );
		} else {
			// Absolute range = 0-127 (like real midi velocity)
			velocity = Math.round( (float)(pressure-HexKeyboard.mMinPressure)/pdiff * 127 );
		}
		// Velocity Boost
		if (HexKeyboard.mVelocityBoost > 0) velocity = Math.round( velocity * (1.0f + (float)HexKeyboard.mVelocityBoost/100 ) );
		// Final check: make sure velocity is never above 127
		if (velocity > 127) velocity = 127;
		
		// -- Get the corresponding sound(s) for the user's velocity and from the available velocities
		int previous_vel = 0; // lower velocity bound if user's velocity is in-between
		int current_vel = 0; // higher velocity bound if user's velocity is in-between
		int soundid = 0; // lower velocity sound or sound exactly equal to user's velocity
		int soundid2 = 0; // higher velocity sound if user's velocity is in-between, or null
		float stream1Volume = 0; // volume for lower velocity sound if user's velocity is in-between, to allow for blending of two velocity sounds
		float stream2Volume = 0; // volume for higher velocity sound if user's velocity is in-between, to allow for blending of two velocity sounds

		// Fake velocity (modulate only the sound volume)
		if (velocity_soundid.size() == 1) {
			current_vel = (Integer) velocity_soundid.keySet().toArray()[0];
			soundid = velocity_soundid.get(current_vel);
			streamVolume = (float)velocity/current_vel * streamVolume;
		// Real velocity with interpolation (either get a sample sound for this velocity and note, or interpolate from two close velocities)
		} else {
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
		}

		Log.d("Instrument::play", "VelocityCheck: midinote: " + midiNoteNumber + " soundid: " + Integer.toString(soundid) + " soundid2: "+ Integer.toString(soundid2) + " velocity/previous_vel/current_vel " + velocity + "/" + previous_vel + "/" + current_vel + " pressure " + Float.toString(pressure) + " max/min " + Float.toString(HexKeyboard.mMaxPressure) + "/" + Float.toString(HexKeyboard.mMinPressure) + " s/s1/s2 vol " + Float.toString(streamVolume) + "/" + Float.toString(stream1Volume) + "/" + Float.toString(stream2Volume));

		// Velocity interpolation via Blending: we blend two velocity sounds (lower and higher bound) with volumes proportional to the user's velocity to interpolate the missing velocity sound
		if (soundid2 != 0) {
			return new int[] {Play.mSoundPool.play(soundid, stream1Volume, stream1Volume, 1, 0, rate),
			        Play.mSoundPool.play(soundid2, stream2Volume, stream2Volume, 1, 0, rate)};
		// Else no interpolation, we have found an exact match for the user's velocity
		} else {
			return new int[] {Play.mSoundPool.play(soundid, streamVolume, streamVolume, 1, 0, rate)};
		}
	}

	public void stop(int[] mStreamId)
	{ 
		for(int streamId : mStreamId) {
			Play.mSoundPool.stop(streamId);
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
