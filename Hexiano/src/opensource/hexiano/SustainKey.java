/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft 2013 Stephen Larroqu                                         *
 *                                                                         *
 *   FILE: ModifierKey.java                                                  *
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

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;

public class SustainKey extends HexKey
{
	public SustainKey(Context context, int radius, Point center,
			int midiNoteNumber, Instrument instrument)
	{
		super(context, radius, center, midiNoteNumber, instrument);

		mPaint.setColor(getColor());
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2);
       
		mOverlayPaint.setColor(mOutlineColor);
        mOverlayPaint.setAntiAlias(true);
        mOverlayPaint.setStyle(Paint.Style.STROKE);
        mOverlayPaint.setStrokeWidth(2);
        
		mTextPaint.setColor(mTextColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(20);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        
		mBlankPaint.setColor(mBlankColor);
        mBlankPaint.setStyle(Paint.Style.FILL);
	}
	
	@Override
	public int getColor() {
		return mWhiteColor;
	}
	
	@Override
	public void play()
	{
		if (HexKeyboard.mSustainHold == true && this.getPressed() == true) {
			this.stop(true);
		} else {
			HexKeyboard.mSustain = true;
			String pitchStr = String.valueOf(mMidiNoteNumber);
			Log.d("HexKey::play", pitchStr);
			this.setPressed(true);
		}
		return;
	}
	
	@Override
	public void stop(boolean force)
	{
		if (this.getPressed() == true && (force == true | HexKeyboard.mSustainHold == false)) { // TODO: find why not all notes stops sometimes when sustain is released (whether mSustainHold is on or off doesn't matter for this bug)
			String pitchStr = String.valueOf(mMidiNoteNumber);
			Log.d("HexKey::stop", pitchStr);
			this.setPressed(false);
			HexKeyboard.mSustain = false;
			HexKeyboard.stopAll(); // stop all previously sustained notes
		}
		return;
	}
}
