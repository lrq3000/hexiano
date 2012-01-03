/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, Copyright 2011 David A. Randolph                             *
 *                                                                         *
 *   FILE: HexKey.java                                                     *
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

import java.util.Hashtable;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.image.ColorDatabase;
import android.image.Image;
import android.graphics.Matrix;
import android.image.RegularPolygon;
import android.util.Log;
import android.world.Posn;

public abstract class HexKey
{
	Posn mCenter;
	Posn mLowerLeft;
	Posn mLowerRight;
	Posn mMiddleLeft;
	Posn mMiddleRight;
	Posn mUpperLeft;
	Posn mUpperRight;
	String mColorStr;
	int mColorId;
	Paint mPaint = new Paint();
	Paint mPressPaint = new Paint();
	Paint mOverlayPaint = new Paint();
	Paint mTextPaint = new Paint();
	Paint mReversePaint;
	static int mKeyCount = 0;
	static int mRadius;
	int mStreamId;
    private boolean mPressed;
    private boolean mDirty;
    
    protected static Instrument mInstrument;
    protected Note mNote;
    protected int mMidiNoteNumber;
    
	public HexKey(int radius, Posn center, int midiNoteNumber, Instrument instrument)
	{
		mInstrument = instrument;
		mNote = new Note(midiNoteNumber);
		mMidiNoteNumber = mNote.getMidiNoteNumber();
		mStreamId = -1;
		mPressed = false;
		mDirty = true;
		mRadius = radius;
		mCenter = center;
		mMiddleLeft = new Posn(mCenter.x - mRadius, mCenter.y);
		mMiddleRight = new Posn(mCenter.x + mRadius, mCenter.y);
		mLowerLeft = new Posn(mCenter.x - mRadius/2, 
			mCenter.y + (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
		mLowerRight = new Posn(mCenter.x + mRadius/2, 
			mCenter.y + (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
		mUpperLeft = new Posn(mCenter.x - mRadius/2, 
			mCenter.y - (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
		mUpperRight = new Posn(mCenter.x + mRadius/2, 
			mCenter.y - (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
	
		int pressId = ColorDatabase.color("darkgray");
        mPressPaint.setColor(pressId);
        mPressPaint.setAntiAlias(true);
        mPressPaint.setStyle(Paint.Style.FILL);
        mPressPaint.setStrokeWidth(2);
        
		mKeyCount++;
	}

	protected String getTextColor(String keyColor)
	{
	    return "black";
	}
	
	protected String getOverlayColor(String keyColor)
	{
	    return "black";
	}
	
    protected Path getHexagonPath()
    {
        Path hexy = new Path();
        double first = Math.PI/6 + Math.PI/6;
        double mult = 2*Math.PI/6;
        hexy.moveTo((int)(mRadius*Math.cos(first)),
                    (int)(mRadius*Math.sin(first)));
        for(int i = 1; i < 6; i++)
        {
            hexy.lineTo((int)(mRadius*Math.cos(first+i*mult)),
                        (int)(mRadius*Math.sin(first+i*mult)));
        }
        hexy.close();
        return hexy;
    }
    
    /** Paint this Polygon into the given graphics */
    public void paint(Canvas canvas)
    {
    	if (! mDirty)
    	{
    		return;	
    	}
    	
    	if (mPressed)
    	{
    		Path hexPath = getHexagonPath();
    		hexPath.offset(mCenter.x, mCenter.y);
    		canvas.drawPath(hexPath, mPressPaint);
    		canvas.drawPath(hexPath, mOverlayPaint);
    	}
    	else
    	{
    		Path hexPath = getHexagonPath();
    		hexPath.offset(mCenter.x, mCenter.y);
    		canvas.drawPath(hexPath, mPaint);
    		canvas.drawPath(hexPath, mOverlayPaint);
    		String label = mNote.getSharpName();
    		// String label = "" + mNote.getMidiNoteNumber();
    		Rect bounds = new Rect();
    		mTextPaint.getTextBounds("X", 0, 1, bounds);
    		Log.d("HexKey::paint", "Bottom: " + bounds.bottom + " Top: " + bounds.top);
    		int labelHeight = bounds.bottom - bounds.top;
    		canvas.drawText(label, mCenter.x, mCenter.y + Math.abs(labelHeight/2), mTextPaint);
    	}
    	
    	mDirty = false;
    }
    
    public boolean getPressed()
    {
    	return mPressed;
    }
   
    public void setPressed(boolean state)
    {
    	if (state != mPressed)
    	{
    	    mPressed = state;
    	    mDirty = true;
    	}
    }
    
	public String toString()
	{
		String str = new String("HexKey: ");
		str += "Center: (" + mCenter.x + ", " + mCenter.y + ")";
		return str;
	}
	
	public boolean contains(Posn pos)
	{
		return this.contains(pos.x, pos.y);
	}
	
	public boolean contains(int x, int y)
	{
		/*
		 * 
		 * We split the hexagon into three areas to determine if the specified coordinates
		 * are included. These are a "center-cut" rectangle, where most positive examples are
		 * expected, and a right and left triangle:
		        ______
               /|    |\
		      / |    | \
		      \ |    | /
		       \|____|/
		  
		  */
		if (x >= mLowerLeft.x && x <= mLowerRight.x &&
			y >= mUpperLeft.y && y <= mLowerLeft.y)
		{
			Log.d("HexKey::contains", "Center cut");
			return true; // Center cut.
		}
		if (x < mMiddleLeft.x || x > mMiddleRight.x ||
			y < mUpperLeft.y || y > mLowerLeft.y)
		{
			return false; // Air ball.
		}
		if (x <= mUpperLeft.x) // Could be in left "triangle."
		{
			if (y <= mMiddleLeft.y)
			{
				// We are in upper half of the left triangle if the
				// slope formed by the line from the (x, y) to the upper-left
				// vertex is >= the slope from the middle-left vertex to the
				// upper-left vertex. We take the negative because the y-coordinate's
				// sign is reversed.
				double sideSlope = (-1.0) *
						(mUpperLeft.y - mMiddleLeft.y)/(mUpperLeft.x - mMiddleLeft.x);
				double pointSlope = (-1.0) * (mUpperLeft.y - y)/(mUpperLeft.x - x);
				
				Log.d("HexKey::contains", "Upper-left side slope: " + sideSlope);
				Log.d("HexKey::contains", "Upper-left point slope: " + pointSlope);
				
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
			else
			{
				// We may be in the lower half of the left triangle.
				double sideSlope = (-1.0) *
						(mLowerLeft.y - mMiddleLeft.y)/(mLowerLeft.x - mMiddleLeft.x);
				double pointSlope = (-1.0) * (mMiddleLeft.y - y)/(mMiddleLeft.x - x);
				Log.d("HexKey::contains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::contains", "Lower-left point slope: " + pointSlope);
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
		}
		else // Could be in right triangle
		{
			if (y <= mMiddleRight.y)
			{
				// We are in upper half of the right triangle if the
				// slope formed by the line from the (x, y) to the upper-right
				// vertex is <= the slope from the middle-right
				// vertex to the upper-upper vertex. We take the negative because 
				// the y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(mUpperRight.y - mMiddleRight.y)/(mUpperRight.x - mMiddleRight.x);
				double pointSlope = (-1.0) *(mUpperRight.y - y)/(mUpperRight.x - x);
				Log.d("HexKey::contains", "Upper-right side slope: " + sideSlope);
				Log.d("HexKey::contains", "Upper-right point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
			else
			{
				double sideSlope = (-1.0) *
						(mLowerRight.y - mMiddleRight.y)/(mLowerRight.x - mMiddleRight.x);
				double pointSlope = (-1.0) *(mMiddleRight.y - y)/(mMiddleRight.x - x);
				Log.d("HexKey::contains", "Lower-right side slope: " + sideSlope);
				Log.d("HexKey::contains", "Lower-right point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void play()
	{
		mStreamId = mInstrument.play(mMidiNoteNumber);
		String pitchStr = String.valueOf(mMidiNoteNumber);
		Log.d("HexKey::play", pitchStr);
		this.setPressed(true);
		return;
	}
	
	public void stop()
	{
		mInstrument.stop(mStreamId);
		mStreamId = -1;
		String pitchStr = String.valueOf(mMidiNoteNumber);
		Log.d("HexKey::stop", pitchStr);
		this.setPressed(false);
		return;
	}
}

