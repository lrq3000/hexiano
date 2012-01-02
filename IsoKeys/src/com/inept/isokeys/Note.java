/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, Copyright 2011 David A. Randolph                             *
 *                                                                         *
 *   FILE: Note.java                                                       *
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
import java.util.Map;

public class Note
{
	protected int mOctave;
	protected String mFlatName;
    protected String mSharpName;
	protected int mMidiNoteNumber;

	public Note(int midiNumber)
	{
	    mMidiNoteNumber = midiNumber;
	    mFlatName = this.getFlatNameForNoteNumber(mMidiNoteNumber);
	    mSharpName = this.getSharpNameForNoteNumber(mMidiNoteNumber);
	    mOctave = this.getOctaveForNoteNumber(mMidiNoteNumber); 
	}
	
	public String getFlatName()
	{
		return mFlatName + mOctave;
	}
	
	public String getSharpName()
	{
		return mSharpName + mOctave;
	}

	public int getMidiNoteNumber()
	{
		return mMidiNoteNumber;
	}
	
	static final HashMap<Integer, String> mFlatForNumber;
	static
	{
	    mFlatForNumber = new HashMap<Integer, String>();
	    mFlatForNumber.put(0, "C");
	    mFlatForNumber.put(1, "Db");
	    mFlatForNumber.put(2, "D");
	    mFlatForNumber.put(3, "Eb");
	    mFlatForNumber.put(4, "E");
	    mFlatForNumber.put(5, "F");
	    mFlatForNumber.put(6, "Gb");
	    mFlatForNumber.put(7, "G");
	    mFlatForNumber.put(8, "Ab");
	    mFlatForNumber.put(9, "A");
	    mFlatForNumber.put(10, "Bb");
	    mFlatForNumber.put(11, "B");
	}

	static final HashMap<Integer, String> mSharpForNumber;
	static
	{
	    mSharpForNumber = new HashMap<Integer, String>();
	    mSharpForNumber.put(0, "C");
	    mSharpForNumber.put(1, "C#");
	    mSharpForNumber.put(2, "D");
	    mSharpForNumber.put(3, "D#");
	    mSharpForNumber.put(4, "E");
	    mSharpForNumber.put(5, "F");
	    mSharpForNumber.put(6, "F#");
	    mSharpForNumber.put(7, "G");
	    mSharpForNumber.put(8, "G#");
	    mSharpForNumber.put(9, "A");
	    mSharpForNumber.put(10, "A#");
	    mSharpForNumber.put(11, "B");
	}
	
	public String getFlatNameForNoteNumber(int midiNoteNumber)
	{		
		int flatNumber = midiNoteNumber % 12;
		return mFlatForNumber.get(flatNumber);
	}
	
	public String getSharpNameForNoteNumber(int midiNoteNumber)
	{		
		int flatNumber = midiNoteNumber % 12;
		return mSharpForNumber.get(flatNumber);
	}
	
	public int getOctaveForNoteNumber(int midiNoteNumber)
	{
		int octavePlusOne = midiNoteNumber/12;
		return octavePlusOne - 1;
	}
}
