/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, an isomorphic musical keyboard for Android                   *
 *   Copyright 2011 David A. Randolph                                      *
 *                                                                         *
 *   FILE: SonomeKey.java                                                  *
 *                                                                         *
 *   This file is part of IsoKeys, an open-source project                  *
 *   hosted at http://isokeys.sourceforge.net.                             *
 *                                                                         *
 *   IsoKeys is free software: you can redistribute it and/or              *
 *   modify it under the terms of the GNU General Public License           *
 *   as published by the Free Software Foundation, either version          *
 *   3 of the License, or (at your option) any later version.              *
 *                                                                         *
 *   AndroidWorld is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with IsoKeys.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.inept.isokeys;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.image.ColorDatabase;
import android.world.Posn;

public class JammerKey extends HexKey
{
	public JammerKey(Context context, int radius, Posn center,
			int midiNoteNumber, Instrument instrument)
	{
		super(context, radius, center, midiNoteNumber, instrument);

		mColorStr = getColor();
		mColorId = ColorDatabase.color(mColorStr);
        mPaint.setColor(mColorId);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2);
       
		int overlayId = ColorDatabase.color(mOutlineColor);
        mOverlayPaint.setColor(overlayId);
        mOverlayPaint.setAntiAlias(true);
        mOverlayPaint.setStyle(Paint.Style.STROKE);
        mOverlayPaint.setStrokeWidth(2);
        
		int textId = ColorDatabase.color(mTextColor);
        mTextPaint.setColor(textId);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(20);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        
		int blankId = ColorDatabase.color(mBlankColor);
        mBlankPaint.setColor(blankId);
        mBlankPaint.setStyle(Paint.Style.FILL);
        
		mKeyOrientation = mPrefs.getString("jammerKeyOrientation", "Vertical");
	}

	public String getColor()
	{
		String sharpName = mNote.getSharpName();
		String color = mWhiteColor;
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
}
