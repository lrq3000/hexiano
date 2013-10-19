/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, Copyright © 2012 James Haigh                                *
 *   Copyright Â© 2011 David A. Randolph                                    *
 *                                                                         *
 *   FILE: HexKey.java                                                     *
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
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class HexKey
{
	static SharedPreferences mPrefs;
	static int mBlankColor;
	static int mBlackColor;
	static int mBlackHighlightColor;
	static int mWhiteColor;
	static int mWhiteHighlightColor;
	static int mTextColor;
	static int mOutlineColor;
	static int mPressedColor;
	Point mCenter;
	Point mTop;
	Point mBottom;
	Point mUpperLeft;
	Point mUpperRight;
	Point mLowerLeft;
	Point mLowerRight;
	Point mMiddleLeft;
	Point mMiddleRight;
	Paint mPaint = new Paint();
	Paint mPressPaint = new Paint();
	Paint mOverlayPaint = new Paint();
	Paint mTextPaint = new Paint();
	Paint mBlankPaint = new Paint();
	static String mKeyOrientation = null;
	static boolean mKeyOverlap = false;
	static int mKeyCount = 0;
	static int mRadius;
	int mStreamId;
    private boolean mPressed;
    private boolean mDirty;
	private boolean sound_loaded = false;
    
    protected static Instrument mInstrument;
    protected Note mNote;
    protected int mMidiNoteNumber;
    
	public HexKey(Context context, int radius, Point center, int midiNoteNumber, Instrument instrument)
	{
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		getPrefs();

		setColors();
		
		mInstrument = instrument;
		mNote = new Note(midiNoteNumber);
		mMidiNoteNumber = mNote.getMidiNoteNumber();
		mStreamId = -1;
		mPressed = false;
		mDirty = true;
		mRadius = radius;
		mCenter = center;
		
		setCriticalPoints();

		mPressPaint.setColor(mPressedColor);
        mPressPaint.setAntiAlias(true);
        mPressPaint.setStyle(Paint.Style.FILL);
        mPressPaint.setStrokeWidth(2);
		
		mKeyCount++;
	}

	protected void getPrefs()
	{
	}

	protected void setColors()
	{
		// Colours have been left from the historically used AndroidWorld library.
		String colorPref = mPrefs.getString("colorScheme", null);
		if (colorPref.equals("Khaki"))
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF843C24; // Brown.
			mBlackHighlightColor = 0xFFD2691E; // Chocolate.
			//mBlackHighlightColor = 0xFFA0522D; // Sienna.
			mWhiteColor = 0xFFF0E68C; // Khaki.
			mWhiteHighlightColor = 0xFFBDB76B; // Dark khaki.
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF000000; // Black.
			mPressedColor = 0xFFA9A9A9; // Dark grey.
		}
		else if (colorPref.equals("Azure"))
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF4682B4; // Steel blue.
			mBlackHighlightColor = 0xFF5F9EA0; // Cadet blue.
			mWhiteColor = 0xFFF0FFFF; // Azure.
			mWhiteHighlightColor = 0xFFAFEEEE; // Pale turquoise.
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF000000; // Black.
			mPressedColor = 0xFFA9A9A9; // Dark grey.
		}
		else if (colorPref.equals("White") ||/*...renamed to...*/ colorPref.equals("Slate"))
		// TODO: Remove 'White' preference when appropriate.
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF2F4F4F; // Dark slate grey.
			mBlackHighlightColor = 0xFF708090; // Slate grey.
			mWhiteColor = 0xFFFFFFFF; // White.
			mWhiteHighlightColor = 0xFFC0C0C0; // Silver.
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF000000; // Black.
			mPressedColor = 0xFFA9A9A9; // Dark grey.
		}
		else if (colorPref.equals("Silhouette"))
		{
			mBlankColor = 0xFFFFFFFF; // White.
			mBlackColor = 0xFF000000; // Black.
			mBlackHighlightColor = 0xFF696969; // Dim grey.
			mWhiteColor = 0xFFA9A9A9; // Dark grey.
			mWhiteHighlightColor = 0xFFD3D3D3; // Light grey.
			mOutlineColor = 0xFFFFFFFF; // White.
			mTextColor = 0xFFFFFFFF; // White.
			mPressedColor = 0xFFFFFFFF; // White.
		}
		else if (colorPref.equals("Grey & White"))
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF555555;
			mBlackHighlightColor = 0xFF666666;
			mWhiteColor = 0xFFFFFFFF; // White.
			mWhiteHighlightColor = 0xFFCCCCCC;
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = mOutlineColor;
			mPressedColor = 0xFFA9A9A9; // Dark grey.
		}
		else if (colorPref.equals("Ebony & Ivory"))
		{
			mBlankColor = 0xFF432620; // WP:Piano case sample.
			//mBlankColor = 0xFF673a31; // WP:Piano case sample 2.
			//mBlackColor = 0xFF162632; // WT:Ebony.
			mBlackColor = 0xFF382c25; // WP:Ebony sample.
			//mBlackColor = 0xFF544238; // Lightened WP:Ebony sample.
			mBlackHighlightColor = mBlackColor;
			mWhiteColor = 0xFFFFFFEE; // Ivory.
			mWhiteHighlightColor = mWhiteColor;
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF666666;
			mPressedColor = 0xFFA9A9A9; // Dark grey.
		}
		else if (colorPref.equals("Blank"))
		{
			mBlankColor = 0xFFFFFFFF; // White.
			mBlackColor = mBlankColor;
			mBlackHighlightColor = mBlankColor;
			mWhiteColor = mBlankColor;
			mWhiteHighlightColor = mBlankColor;
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = mOutlineColor;
			mPressedColor = 0xFFA9A9A9; // Dark grey.
		}
		else // Default: Azure.
		// Fail to default colour scheme if saved preference doesn't have a match.
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF4682B4; // Steel blue.
			mBlackHighlightColor = 0xFF5F9EA0; // Cadet blue.
			mWhiteColor = 0xFFF0FFFF; // Azure.
			mWhiteHighlightColor = 0xFFAFEEEE; // Pale turquoise.
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF000000; // Black.
			mPressedColor = 0xFFA9A9A9; // Dark grey.
		}
	}
	
	abstract public int getColor();
	
	static public String getKeyOrientation(Context context)
	{
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String layoutPref = mPrefs.getString("layout", null);
		if (layoutPref.equals("Sonome")) // Sonome
		{
			mKeyOverlap = false;
			return mPrefs.getString("sonomeKeyOrientation", null);
		}
		else if (layoutPref.equals("Janko")) // Janko
		{
			mKeyOverlap = false;
			return mPrefs.getString("jankoKeyOrientation", null);
		}
		else // Jammer
		{
			return mPrefs.getString("jammerKeyOrientation", null);
		}
	}
	
	protected Path getHexagonPath()
	{
		if (mKeyOrientation.equals("Horizontal"))
		{
			return getHorizontalHexagonPath();
		}
		return getVerticalHexagonPath();
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
		this.sound_not_loaded(); // Sets mDirty if just loaded.
    	if (! mDirty)
    	{
    		return;	
    	}
    
  		Path hexPath = getHexagonPath();
    	
		String labelPref  = mPrefs.getString("labelType", null);
		String label = mNote.getDisplayString(labelPref, true);
	
		if (this.mMidiNoteNumber < 21 || this.mMidiNoteNumber > 108)
		{
    		hexPath.offset(mCenter.x, mCenter.y);
    		canvas.drawPath(hexPath, mBlankPaint);
		}
		else if (mPressed || this.sound_not_loaded())
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
    	}
    	
    	mDirty = false;
    }
    
	public boolean sound_not_loaded() {
		if (sound_loaded == true) {
			return false;
		} else {
			// Not all keys have sounds to be loaded.
			if (Instrument.mRootNotes.containsKey(mMidiNoteNumber)) {
				int index = Instrument.mRootNotes.get(mMidiNoteNumber);
				sound_loaded = Instrument.mSounds.containsKey(index);
			}
			if (sound_loaded == true) {
				// Set mDirty if just loaded.
				mDirty = true;
			}
			return !sound_loaded;
		}
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
    
	@Override
	public String toString()
	{
		String str = new String("HexKey: ");
		str += "Center: (" + mCenter.x + ", " + mCenter.y + ")";
		return str;
	}
	
	public boolean contains(Point pos)
	{
		return this.contains(pos.x, pos.y);
	}
	
	public boolean contains(int x, int y)
	{
		if (mKeyOverlap)
		{
			return overlapContains(x, y);
		}
		else if (mKeyOrientation.equals("Horizontal"))
		{
			return horizontalContains(x, y);
		}
		else
		{
			return verticalContains(x, y);
		}
	}

	private void setCriticalPoints()
	{
		if (mKeyOrientation.equals("Horizontal") || mKeyOverlap)
		{
			setHorizontalCriticalPoints();
		}
		else
		{
			setVerticalCriticalPoints();
		}
	}
	
	private void setHorizontalCriticalPoints()
	{
		mTop = new Point(mCenter.x, mCenter.y - mRadius);
		mBottom = new Point(mCenter.x, mCenter.y + mRadius);

		double angle = Math.PI / 6;
		mUpperRight = new Point((int)(mCenter.x + mRadius * Math.cos(angle)),
				(int)(mCenter.y - mRadius * Math.sin(angle)));
		mLowerRight = new Point((int)(mCenter.x + mRadius * Math.cos(angle)),
				(int)(mCenter.y + mRadius * Math.sin(angle)));
		mLowerLeft = new Point((int)(mCenter.x - mRadius * Math.cos(angle)),
				(int)(mCenter.y + mRadius * Math.sin(angle)));
		mUpperLeft = new Point((int)(mCenter.x - mRadius * Math.cos(angle)),
				(int)(mCenter.y - mRadius * Math.sin(angle)));
		
		Log.d("setHorizontalCriticalPoints", 
				"Center: " + mCenter.toString() +
				" Radius: " + mRadius +
				"Critical points: " +
				mUpperRight.toString() +
				mLowerRight.toString() +
				mBottom.toString() + 
				mLowerLeft.toString() +
				mUpperLeft.toString() +
				mTop.toString());
	}
	
	private void setVerticalCriticalPoints()
	{
		mMiddleLeft = new Point(mCenter.x - mRadius, mCenter.y);
		mMiddleRight = new Point(mCenter.x + mRadius, mCenter.y);
		mLowerLeft = new Point(mCenter.x - mRadius/2, 
				mCenter.y + (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
		mLowerRight = new Point(mCenter.x + mRadius/2, 
				mCenter.y + (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
		mUpperLeft = new Point(mCenter.x - mRadius/2, 
				mCenter.y - (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
		mUpperRight = new Point(mCenter.x + mRadius/2, 
				mCenter.y - (int)(Math.round(Math.sqrt(3.0) * mRadius)/2));
	}

	public boolean overlapContains(int x, int y)
	{
		Log.e("HexKey::overlapContains", "Not supported by layout!");
		return false;
	}

	public boolean horizontalContains(int x, int y)
	{
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
		if (x >= mLowerLeft.x && x <= mLowerRight.x &&
			y >= mUpperLeft.y && y <= mLowerLeft.y)
		{
			Log.d("HexKey::horizontalContains", "Center cut");
			return true; // Center cut.
		}
		if (x < mUpperLeft.x || x > mUpperRight.x ||
			y < mTop.y || y > mBottom.y)
		{
			return false; // Air ball.
		}
		if (y <= mUpperLeft.y) // Could be in top "triangle."
		{
			if (x <= mTop.x)
			{
				// We are in left half of the top triangle if the
				// slope formed by the line from the (x, y) to the top
				// vertex is >= the slope from the upper-left vertex to the
				// top vertex. We take the negative because the y-coordinate's
				// sign is reversed.
				double sideSlope = (-1.0) *
						(mTop.y - mUpperLeft.y)/(mTop.x - mUpperLeft.x);
				double pointSlope = (-1.0) * (mTop.y - y)/(mTop.x - x);
				
				Log.d("HexKey::horizontalContains", "Upper-left side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Upper-left point slope: " + pointSlope);
				
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
			else
			{
				// We are in right half of the top triangle if the
				// slope formed by the line from the (x, y) to the top
				// vertex is <= (more negative than) the slope from the
				// upper-left vertex to the top vertex. We take the
				// negative because the y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(mTop.y - mUpperRight.y)/(mTop.x - mUpperRight.x);
				double pointSlope = (-1.0) * (mTop.y - y)/(mTop.x - x);
				Log.d("HexKey::horizontalContains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Lower-left point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
		}
		else // Could be in bottom triangle
		{
			if (x <= mBottom.x)
			{
				// We are in left half of the lower triangle if the
				// slope formed by the line from the (x, y) to the bottom
				// vertex is <= (more negative than) the slope from the
				// lower-left vertex to the bottom vertex. We take the
				// negative because the y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(mLowerLeft.y - mBottom.y)/(mLowerLeft.x - mBottom.x);
				double pointSlope = (-1.0) * (y - mBottom.y)/(x - mBottom.x);
				Log.d("HexKey::horizontalContains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Lower-left point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
			else // Check right half
			{
				// We are in right half of the lower triangle if the
				// slope formed by the line from the (x, y) to the bottom
				// vertex is >= the slope from the lower-left vertex to 
				// the bottom vertex. We take the negative because the 
				// y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(mLowerRight.y - mBottom.y)/(mLowerRight.x - mBottom.x);
				double pointSlope = (-1.0) * (y - mBottom.y)/(x - mBottom.x);
				Log.d("HexKey::horizontalContains", "Lower-right side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Lower-right point slope: " + pointSlope);
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean verticalContains(int x, int y)
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
			Log.d("HexKey::verticalContains", "Center cut");
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
				
				Log.d("HexKey::verticalContains", "Upper-left side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Upper-left point slope: " + pointSlope);
				
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
				Log.d("HexKey::verticalContains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Lower-left point slope: " + pointSlope);
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
				Log.d("HexKey::verticalContains", "Upper-right side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Upper-right point slope: " + pointSlope);
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
				Log.d("HexKey::verticalContains", "Lower-right side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Lower-right point slope: " + pointSlope);
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
		if (mStreamId != -1) {
			//if (HexKeyboard.mSustain == true || HexKeyboard.mSustainAlwaysOn == true) {
				this.stop(); // better always stop if there is already a stream playing, no matter the reason
			//} else {
				// Should never get here.
			//	Log.e("HexKey::play", mMidiNoteNumber + ": Already playing and no sustain! Should not happen!");
			//}
		}
		mStreamId = mInstrument.play(mMidiNoteNumber);
		if (mStreamId == -1) {return;} // May not yet be loaded.
		String pitchStr = String.valueOf(mMidiNoteNumber);
		Log.d("HexKey::play", pitchStr);
		this.setPressed(true);
		return;
	}
	
	public void stop() {
		this.stop(false);
	}
	
	public void stop(boolean force)
	{
		if (mStreamId == -1) {return;} // May not have been loaded when played.
		if (force == true | (HexKeyboard.mSustain == false && HexKeyboard.mSustainAlwaysOn == false)) {
			mInstrument.stop(mStreamId);
			mStreamId = -1;
		}
		String pitchStr = String.valueOf(mMidiNoteNumber);
		Log.d("HexKey::stop", pitchStr);
		this.setPressed(false);
		return;
	}
}
