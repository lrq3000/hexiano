/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano™, an isomorphic musical keyboard for Android                  *
 *   Copyright © 2012 James Haigh                                          *
 *   Copyright © 2011 David A. Randolph                                    *
 *                                                                         *
 *   FILE: Piano.java                                                      *
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
 *   NOTE: The sounds used for this instrument were derived from           *
 *   the acoustic-piano soundfont created by Roberto Gordo Saez.
 *   Here is its license, which we will display more prominently
 *   in the application and our web site, as soon as we get 
 *   organized:
 *   
Acoustic grand piano soundfont (Yamaha Disklavier Pro), release 2008-09-10
116 samples, 44100Hz, 16bit.

The acoustic grand piano soundfont is free. It is built from the Zenph
Studios Yamaha Disklavier Pro Piano Multisamples for OLPC.

The soundfont itself and all modifications made to the original
samples by Roberto Gordo Saez, published under a Creative Commons
Attribution 3.0 license.

Copyright 2008, Roberto Gordo Saez roberto.gordo@gmail.com Creative 
Commons Attribution 3.0 license http://creativecommons.org/licenses/by/3.0/

Zenph Studios Yamaha Disklavier Pro Piano Multisamples for OLPC:

A collection of Grand Piano samples played by a Yamaha Disklavier
Pro. Performed by computer and specifically recorded for OLPC by
Dr. Mikhail Krishtal, Director of Music Research and Production, and
his team at Zenph Studios. They are included in the OLPC sound
sample library.

How is it being done: "The Disklavier Pro has an internal
electronically-controlled mechanism that allows it to play sounds
with very precise specifications. It has its own file format known
as XP MIDI, an extension of standard midi. I Mikhail Krishtal
prepare the files for it to play -- in this case, representing notes
of different registers, durations, and dynamic levels."

http://csounds.com/olpc/pianoSamplesMikhail/pianoMikhail.html
Produced by Zenph Studios in Chapel Hill, North Carolina. The main
studio location is in Raleigh, North Carolina.

http://zenph.com/
Samples from the OLPC sound sample library:

This huge collection of new and original samples have been donated
to Dr. Richard Boulanger @ cSounds.com specifically to support the
OLPC developers, students, XO users, and computer and electronic
musicians everywhere. They are FREE and are offered under a CC-BY
license.

http://wiki.laptop.org/go/Sound_samples http://csounds.com/boulanger

Creative Commons Attribution 3.0 license
http://creativecommons.org/licenses/by/3.0/

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package @CONFIG.APP_PACKAGE_NAME@;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;

public class Piano extends Instrument
{
	public Piano(Context context)
	{
		super(context);

		Pattern pat = Pattern.compile("^pno0*([0-9]+)v");
		Class raw = R.raw.class;
		Field[] fields = raw.getFields();
		List<int[]> sounds = new ArrayList<int[]>(); // Are there really no tuples in Java?!
		for (Field field : fields)
		{
		    try
		    {
		    	String fieldName = field.getName();
		        if (fieldName.startsWith("pno", 0))
		        {
		        	int midiNoteNumber;
		        	Matcher mat = pat.matcher(fieldName);
		        	if (mat.find())
		        	{
		        		String midiNoteNumberStr = mat.group(1);
		        		midiNoteNumber = Integer.parseInt(midiNoteNumberStr);
		        		int fieldValue = field.getInt(null);	
		        		Log.d("Piano", midiNoteNumberStr + ": " + fieldValue);
						int[] tuple = {midiNoteNumber, fieldValue};
						sounds.add(0, tuple);
		        		mRootNotes.put(midiNoteNumber, midiNoteNumber);
		        		mRates.put(midiNoteNumber, 1.0f);
		        	}
		        }
		    }
		    catch(IllegalAccessException e) {
		        Log.e("REFLECTION", String.format("%s threw IllegalAccessException.",
		            field.getName()));
		    }
		}

		sound_load_queue = sounds.iterator();
		// Start loading the first sound, the rest are started from the OnLoadCompleteListener.
		int[] tuple = sound_load_queue.next();
		addSound(tuple[0], tuple[1]);

		float previousRate = 1.0f;
		int previousRootNote = 21;
		for (int noteId = 21; noteId < 110; noteId++)
		{
			if (mRootNotes.containsKey(noteId))
			{
				previousRootNote = noteId;
				previousRate = 1.0f;
			}
			else
			{
				mRootNotes.put(noteId, previousRootNote);
				double oneTwelfth = 1.0/12.0;
			    double newRate = previousRate * Math.pow(2, oneTwelfth);	
			    previousRate = (float)newRate;
				mRates.put(noteId, previousRate);
			}
		}
	}
}
