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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.image.ColorDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.world.Posn;

public abstract class HexKey
{
	static SharedPreferences mPrefs;
	static String mBlankColor;
	static String mBlackColor;
	static String mBlackHighlightColor;
	static String mWhiteColor;
	static String mWhiteHighlightColor;
	static String mTextColor;
	static String mOutlineColor;
	static String mPressedColor;
	Posn mCenter;
	String mColorStr;
	int mColorId;
	Paint mPaint = new Paint();
	Paint mPressPaint = new Paint();
	Paint mOverlayPaint = new Paint();
	Paint mTextPaint = new Paint();
	Paint mBlankPaint = new Paint();
	static String mKeyOrientation = "Horizontal";
	static int mKeyCount = 0;
	static int mRadius;
	int mStreamId;
    private boolean mPressed;
    private boolean mDirty;
    
    protected static Instrument mInstrument;
    protected Note mNote;
    protected int mMidiNoteNumber;
    
	public HexKey(Context context, int radius, Posn center, int midiNoteNumber, Instrument instrument)
	{
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	
		setColors();
		
		mInstrument = instrument;
		mNote = new Note(midiNoteNumber);
		mMidiNoteNumber = mNote.getMidiNoteNumber();
		mStreamId = -1;
		mPressed = false;
		mDirty = true;
		mRadius = radius;
		mCenter = center;

		int pressId = ColorDatabase.color(mPressedColor);
        mPressPaint.setColor(pressId);
        mPressPaint.setAntiAlias(true);
        mPressPaint.setStyle(Paint.Style.FILL);
        mPressPaint.setStrokeWidth(2);
		
		mKeyCount++;
	}

	protected void setColors()
	{
		String colorPref = mPrefs.getString("colorScheme", "Khaki");
		if (colorPref.equals("Khaki"))
		{
			mBlankColor = "black";
			mBlackColor = "brown";
			mBlackHighlightColor = "sienna";
			mWhiteColor = "khaki";
			mWhiteHighlightColor = "darkKhaki";
			mOutlineColor = "black";
			mTextColor = "black";
			mPressedColor = "darkgray";
		}
		else if (colorPref.equals("Azure"))
		{
			mBlankColor = "black";
			mBlackColor = "steelblue";
			mBlackHighlightColor = "cadetblue";
			mWhiteColor = "azure";
			mWhiteHighlightColor = "paleturquoise";
			mOutlineColor = "black";
			mTextColor = "black";
			mPressedColor = "darkgray";
		}
		else if (colorPref.equals("White"))
		{
			mBlankColor = "black";
			mBlackColor = "darkslategray";
			mBlackHighlightColor = "slategrey";
			mWhiteColor = "white";
			mWhiteHighlightColor = "silver";
			mOutlineColor = "black";
			mTextColor = "black";
			mPressedColor = "darkgray";
		}
		else if (colorPref.equals("Black"))
		{
			mBlankColor = "white";
			mBlackColor = "black"; 
			mBlackHighlightColor = "dimgray";
			mWhiteColor = "darkgray";
			mWhiteHighlightColor = "lightgray";
			mOutlineColor = "white";
			mTextColor = "white";
			mPressedColor = "white";
		}
	}
	
	abstract public String getColor();
	
	static public String getKeyOrientation(Context context)
	{
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (mPrefs.getString("layout", "Sonome").equals("Sonome"))
		{
			return mPrefs.getString("sonomeKeyOrientation", "Vertical");
		}
		return mPrefs.getString("jammerKeyOrientation", "Horizontal");
	}
	
	protected Path getHexagonPath()
	{
		if (mKeyOrientation.equals("Vertical"))
		{
			return getVerticalHexagonPath();
		}
		
		return getHorizontalHexagonPath();
	}
	
    protected Path getVerticalHexagonPath()
    {
        Path hexy = new Path();
        double angle = Math.PI / 3;
        double increment = Math.PI / 3;
        hexy.moveTo((int)(mRadius * Math.cos(angle)),
                    (int)(mRadius * Math.sin(angle)));
        for(int i = 1; i < 6; i++)
        {
            hexy.lineTo((int)(mRadius * Math.cos(angle + i * increment)),
                        (int)(mRadius * Math.sin(angle + i * increment)));
        }
        
        hexy.close();
        
        return hexy;
    }
	
    protected Path getHorizontalHexagonPath()
    {
        Path hexy = new Path();
        double angle = Math.PI / 2;
        double increment = Math.PI / 3;
        hexy.moveTo((int)(mRadius * Math.cos(angle)),
                    (int)(mRadius * Math.sin(angle)));
        for(int i = 1; i < 6; i++)
        {
            hexy.lineTo((int)(mRadius * Math.cos(angle + i * increment)),
                        (int)(mRadius * Math.sin(angle + i * increment)));
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
    
  		Path hexPath = getHexagonPath();
    	
		String labelPref  = mPrefs.getString("labelType", "English");
		String label = mNote.getDisplayString(labelPref, true);
	
  		
		if (this.mMidiNoteNumber < 21 || this.mMidiNoteNumber > 108)
		{
    		hexPath.offset(mCenter.x, mCenter.y);
    		canvas.drawPath(hexPath, mBlankPaint);
		}
		else if (mPressed)
    	{
    		hexPath.offset(mCenter.x, mCenter.y);
    		canvas.drawPath(hexPath, mPressPaint);
    		canvas.drawPath(hexPath, mOverlayPaint);
    	}
    	else
    	{
    		hexPath.offset(mCenter.x, mCenter.y);
    		canvas.drawPath(hexPath, mPaint);
    		canvas.drawPath(hexPath, mOverlayPaint);
    		
    		Rect bounds = new Rect();
    		mTextPaint.getTextBounds(label, 0, label.length(), bounds);
    		int labelHeight = bounds.bottom - bounds.top;
    		int x = mCenter.x;
    		int y = mCenter.y + Math.abs(labelHeight/2);
    		canvas.drawText(label, x, y, mTextPaint);
    	
//    		boolean wantLandscape = false;
//    		if (layoutPref.equals("Sonome"))
//    		{
//    			wantLandscape  = mPrefs.getBoolean("sonomeLandscape", true);
//    		}
//    		else
//    		{
//    			wantLandscape  = mPrefs.getBoolean("jammerLandscape", false);
//    		}
//    		
//    		if (wantLandscape)
//    		{
//    			canvas.drawText(label, x, y, mTextPaint);
//    		}
//    		else
//    		{
//    			canvas.save();
//    		    canvas.rotate(-90, mCenter.x, mCenter.y);
//    			canvas.drawText(label, x, y, mTextPaint);
//    			canvas.restore();
//    		}
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
		if (mKeyOrientation.equals("Vertical"))
		{
			return verticalContains(x, y);
		}
		
		return horizontalContains(x, y);
	}

	public boolean horizontalContains(int x, int y)
	{
		Posn top = new Posn(mCenter.x, mCenter.y - mRadius);
		Posn bottom = new Posn(mCenter.x, mCenter.y + mRadius);

		double angle = Math.PI / 3;
		Posn upperRight = new Posn((int)(mCenter.x + mRadius * Math.cos(angle)),
				(int)(mCenter.y - mRadius * Math.sin(angle)));
		Posn lowerRight = new Posn((int)(mCenter.x + mRadius * Math.cos(angle)),
				(int)(mCenter.y + mRadius * Math.sin(angle)));
		Posn lowerLeft = new Posn((int)(mCenter.x - mRadius * Math.cos(angle)),
				(int)(mCenter.y - mRadius * Math.sin(angle)));
		Posn upperLeft = new Posn((int)(mCenter.x - mRadius * Math.cos(angle)),
				(int)(mCenter.y + mRadius * Math.sin(angle)));
		
		/*
		 * 
		 * We split the hexagon into three areas to determine if the specified coordinates
		 * are included. These are a "center-cut" rectangle, where most positive examples are
		 * expected, and a top and bottom triangle:
		 * 
		      /\
		     /  \    <--- Top triangle
		    /    \
		   |------|
		   |      |  <--- Center cut
		   |      |
		   |------|
		    \    /
		     \  /    <--- Bottom triangle
		      \/
		  
		  */
		if (x >= lowerLeft.x && x <= lowerRight.x &&
			y >= upperLeft.y && y <= lowerLeft.y)
		{
			Log.d("HexKey::contains", "Center cut");
			return true; // Center cut.
		}
		if (x < upperLeft.x || x > upperRight.x ||
			y < top.y || y > bottom.y)
		{
			return false; // Air ball.
		}
		if (y <= upperLeft.y) // Could be in top "triangle."
		{
			if (x <= top.x)
			{
				// We are in left half of the top triangle if the
				// slope formed by the line from the (x, y) to the top
				// vertex is >= the slope from the upper-left vertex to the
				// top vertex. We take the negative because the y-coordinate's
				// sign is reversed.
				double sideSlope = (-1.0) *
						(top.y - upperLeft.y)/(top.x - upperLeft.x);
				double pointSlope = (-1.0) * (top.y - y)/(top.x - x);
				
				Log.d("HexKey::verticalContains", "Upper-left side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Upper-left point slope: " + pointSlope);
				
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
			else
			{
				// We may be in the right half of the top triangle.
				double sideSlope = (-1.0) *
						(top.y - upperRight.y)/(top.x - upperRight.x);
				double pointSlope = (-1.0) * (top.y - y)/(top.x - x);
				Log.d("HexKey::verticalContains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Lower-left point slope: " + pointSlope);
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
		}
		else // Could be in bottom triangle
		{
			if (x <= top.x)
			{
				// We are in left half of the lower triangle if the
				// slope formed by the line from the (x, y) to the bottom
				// vertex is <= the slope from the lower-left vertex
				// to the bottom vertex. We take the negative because 
				// the y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(lowerLeft.y - bottom.y)/(lowerLeft.x - bottom.x);
				double pointSlope = (-1.0) * (y - bottom.y)/(x - bottom.x);
				Log.d("HexKey::verticalContains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::contains", "Lower-left point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
			else // Check right half
			{
				double sideSlope = (-1.0) *
						(lowerRight.y - bottom.y)/(lowerRight.x - bottom.x);
				double pointSlope = (-1.0) * (y - bottom.y)/(x - bottom.x);
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
	
	public boolean verticalContains(int x, int y)
	{
		Posn middleLeft = new Posn(mCenter.x - mRadius, mCenter.y);
		Posn middleRight = new Posn(mCenter.x + mRadius, mCenter.y);
		Posn lowerLeft = new Posn(mCenter.x - mRadius/2, 
				mCenter.y + (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
		Posn lowerRight = new Posn(mCenter.x + mRadius/2, 
				mCenter.y + (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
		Posn upperLeft = new Posn(mCenter.x - mRadius/2, 
				mCenter.y - (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
		Posn upperRight = new Posn(mCenter.x + mRadius/2, 
				mCenter.y - (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
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
		if (x >= lowerLeft.x && x <= lowerRight.x &&
			y >= upperLeft.y && y <= lowerLeft.y)
		{
			Log.d("HexKey::contains", "Center cut");
			return true; // Center cut.
		}
		if (x < middleLeft.x || x > middleRight.x ||
			y < upperLeft.y || y > lowerLeft.y)
		{
			return false; // Air ball.
		}
		if (x <= upperLeft.x) // Could be in left "triangle."
		{
			if (y <= middleLeft.y)
			{
				// We are in upper half of the left triangle if the
				// slope formed by the line from the (x, y) to the upper-left
				// vertex is >= the slope from the middle-left vertex to the
				// upper-left vertex. We take the negative because the y-coordinate's
				// sign is reversed.
				double sideSlope = (-1.0) *
						(upperLeft.y - middleLeft.y)/(upperLeft.x - middleLeft.x);
				double pointSlope = (-1.0) * (upperLeft.y - y)/(upperLeft.x - x);
				
				Log.d("HexKey::horizontalContains", "Upper-left side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Upper-left point slope: " + pointSlope);
				
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
			else
			{
				// We may be in the lower half of the left triangle.
				double sideSlope = (-1.0) *
						(lowerLeft.y - middleLeft.y)/(lowerLeft.x - middleLeft.x);
				double pointSlope = (-1.0) * (middleLeft.y - y)/(middleLeft.x - x);
				Log.d("HexKey::horizontalContains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Lower-left point slope: " + pointSlope);
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
		}
		else // Could be in right triangle
		{
			if (y <= middleRight.y)
			{
				// We are in upper half of the right triangle if the
				// slope formed by the line from the (x, y) to the upper-right
				// vertex is <= the slope from the middle-right
				// vertex to the upper-upper vertex. We take the negative because 
				// the y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(upperRight.y - middleRight.y)/(upperRight.x - middleRight.x);
				double pointSlope = (-1.0) *(upperRight.y - y)/(upperRight.x - x);
				Log.d("HexKey::horizontalContains", "Upper-right side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Upper-right point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
			else
			{
				double sideSlope = (-1.0) *
						(lowerRight.y - middleRight.y)/(lowerRight.x - middleRight.x);
				double pointSlope = (-1.0) *(middleRight.y - y)/(middleRight.x - x);
				Log.d("HexKey::horizontalContains", "Lower-right side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Lower-right point slope: " + pointSlope);
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
