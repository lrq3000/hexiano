/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                  *
 *   Copyright © 2012 James Haigh                                          *
 *   Copyright © 2011 David A. Randolph                                    *
 *                                                                         *
 *   FILE: JammerKey.java                                                  *
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

import android.content.Context;
import android.util.Log;

public class JammerKey extends HexKey
{
	public JammerKey(Context context, int radius, Point center,
			int midiNoteNumber, Instrument instrument, int keyNumber)
	{
		super(context, radius, center, midiNoteNumber, instrument, keyNumber);
	}

	@Override
	protected void getPrefs()
	{
		mKeyOrientation = mPrefs.getString("jammerKeyOrientation", null);
		mKeyOverlap = mPrefs.getBoolean("jammerKeyOverlap", false);
	}

	@Override
	public int getColor()
	{
		String sharpName = mNote.getSharpName();
		int color = mWhiteColor;
		if (sharpName.contains("#"))
		{	
			color = mBlackColor;
			if (sharpName.contains("G"))
			{
				color = mBlackHighlightColor;
			}
		}
		else if (sharpName.contains("C"))
		{
			color = mWhiteHighlightColor;
		}
		
		return color;
	}

	@Override
	public boolean overlapContains(int x, int y)
	{
		if (x >= mLowerLeft.x && x <= mLowerRight.x &&
			y >= mTop.y && y <= mBottom.y)
		{
			Log.d("HexKey::overlapContains", "Contains");
			return true;
		}
		return false;
	}
}
