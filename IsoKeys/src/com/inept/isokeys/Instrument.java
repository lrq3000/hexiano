/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, Copyright 2011 David A. Randolph                             *
 *                                                                         *
 *   FILE: Instrument.java                                                 *
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

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public abstract class Instrument {

	public static final int POLYPHONY_COUNT = 8;
	private SoundPool mSoundPool; 
	protected HashMap<Integer, Integer> mSounds; 
	protected HashMap<Integer, Float> mRates;
	protected HashMap<Integer, Integer> mRootNotes;
	private AudioManager  mAudioManager;
	private Context mContext;

	public Instrument(Context context)
	{
		mContext = context;
		init(context);
	}

	public void init(Context context)
	{ 
		mSoundPool = new SoundPool(POLYPHONY_COUNT, AudioManager.STREAM_MUSIC, 0); 
		mSounds = new HashMap<Integer, Integer>(); 
		mRates = new HashMap<Integer, Float>(); 
		mRootNotes = new HashMap<Integer, Integer>(); 
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE); 	     
	} 

	public void addSound(int index, int soundId)
	{
		mSounds.put(index, mSoundPool.load(mContext, soundId, 1));
	}

	public int play(int midiNoteNumber)
	{ 
		Log.d("Instrument", "play(" + midiNoteNumber + ")");
		int index = mRootNotes.get(midiNoteNumber);
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
