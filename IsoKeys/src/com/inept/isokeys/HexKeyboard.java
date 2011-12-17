/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, Copyright 2011 David A. Randolph                             *
 *                                                                         *
 *   FILE: HexKeyboard.java                                                *
 *                                                                         *
 *   This file is part of IsoKeys, an open-source project                  *
 *   hosted at http://isokeys.sourcedforge.net.                            *
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

import java.lang.Math;

import android.image.*;
import android.util.Log;
import android.world.World;
import android.world.Posn;
import java.util.ArrayList;

public class HexKeyboard extends World
{
    static final int TITLE_BAR_HEIGHT = 50;

    static Scene mBackground;
    static int mDisplayWidth = 0;
    static int mDisplayHeight = 0;
    static int mRowCount = 0;
    static int mColumnCount = 0;
    static int mTileRadius = 64;
    static int mTileHeight = 0;
   
    		
    static final RegularPolygon mOverlay = new RegularPolygon(mTileRadius, 6, "outline", "black");
    static final Image mWhiteHexTileImage = 
    		new RegularPolygon(mTileRadius, 6, "solid", "white").overlay(mOverlay).rotate(90);
    static final Image mBlackHexTileImage =
    		new RegularPolygon(mTileRadius, 6, "solid", "black").overlay(mOverlay).rotate(90);
    static final Image mRedHexTileImage = 
    		new RegularPolygon(mTileRadius, 6, "solid", "red").overlay(mOverlay).rotate(90);
    static final Image mGreenHexTileImage = 
    		new RegularPolygon(mTileRadius, 6, "solid", "green").overlay(mOverlay).rotate(90);
    
    static ArrayList<HexKey> mKeys = new ArrayList<HexKey>();
 
    private class HexKey
    {
    	Posn mCenter;
    	Posn mLowerLeft;
    	Posn mLowerRight;
    	Posn mMiddleLeft;
    	Posn mMiddleRight;
    	Posn mUpperLeft;
    	Posn mUpperRight;
    	String mColor;
    	int mPitch;
    	
    	public HexKey(int x, int y, int pitch, String color)
    	{
    		this(new Posn(x, y), pitch, color);
    	}
    	
    	public HexKey(Posn center, int pitch, String color)
    	{
    		mCenter = center;
    		mMiddleLeft = new Posn(mCenter.x - mTileRadius, mCenter.y);
    		mMiddleRight = new Posn(mCenter.x + mTileRadius, mCenter.y);
    		mLowerLeft = new Posn(mCenter.x - mTileRadius/2, 
    			mCenter.y + (int)(Math.round(Math.sqrt(3.0) * mTileRadius)/2));
    		mLowerRight = new Posn(mCenter.x + mTileRadius/2, 
    			mCenter.y + (int)(Math.round(Math.sqrt(3.0) * mTileRadius)/2));
    		mUpperLeft = new Posn(mCenter.x - mTileRadius/2, 
    			mCenter.y - (int)(Math.round(Math.sqrt(3.0) * mTileRadius)/2));
    		mUpperRight = new Posn(mCenter.x + mTileRadius/2, 
    			mCenter.y - (int)(Math.round(Math.sqrt(3.0) * mTileRadius)/2));
    		
    		mPitch = pitch;
    		mColor = color;
    	}
  
    	public Image getImage()
    	{
    		if (mColor.equals("green"))
    		{
    			return mGreenHexTileImage;
    		}
    		
    		return mRedHexTileImage;
    	}
  
    	public Image getPressImage()
    	{
    		return mBlackHexTileImage;
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
//    		Log.v("HexKey::contains", this.toString());
    		if (x >= mLowerLeft.x && x <= mLowerRight.x &&
    			y >= mUpperLeft.y && y <= mLowerLeft.y)
    		{
    			Log.d("HexKey::contains", "Center cut");
    			return true; // Center cut.
    		}
    		if (x < mMiddleLeft.x || x > mMiddleRight.x ||
    			y < mUpperLeft.y || y > mLowerLeft.y)
    		{
//    			Log.d("HexKey::contains", "Air ball");
    			return false; // Air ball.
    		}
    		
    		if (x <= mUpperLeft.x) // Could be in left triangle.
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
    		String pitchStr = String.valueOf(mPitch);
    		Log.d("HexKey::play", pitchStr);
    		return;
    	}
    	
    	public void stop()
    	{
    		String pitchStr = String.valueOf(mPitch);
    		Log.d("HexKey::stop", pitchStr);
    		return;
    	}
    }
    
    void setUpBoard(int displayHeight, int displayWidth)
    {
        mDisplayWidth = displayWidth;
        mDisplayHeight = displayHeight;
    	mTileHeight = (int)(Math.round(Math.sqrt(3.0) * mTileRadius));
        
        mRowCount = displayHeight/mTileHeight + 1;
        mRowCount = 10;
        
        mColumnCount = displayWidth/(mTileRadius * 3) + 2;
    }
    
    HexKeyboard(int height, int width, Scene scene)
    {
    	mBackground = scene;
    	setUpBoard(height, width);
    }
    
    HexKeyboard(int height, int width)
    {
    	mBackground = new EmptyScene(width, height);
    	
    	boolean needKeys = false;
    	if (mKeys.isEmpty())
    	{
    		needKeys = true;
    	}
    	
    	setUpBoard(height, width);
        
        int y = 0;

//		RegularPolygon redHexTile = new RegularPolygon(mTileRadius, 6, "solid", "red");
//		Image redHexTileImg = redHexTile.rotate(90);
//		RegularPolygon overlay = new RegularPolygon(mTileRadius, 6, "outline", "black");
//		Image overlayImg  = overlay.rotate(90);
//		
//		RegularPolygon greenHexTile = new RegularPolygon(mTileRadius, 6, "solid", "green");
//		Image greenHexTileImg = greenHexTile.rotate(90);
		
//		int [][] upperY = new int[2 * mColumnCount][mRowCount];
//		int [][] lowerY = new int[2 * mColumnCount][mRowCount];
		
		int pitch = 0;
		
        for (int j = 0; j < mRowCount; j++)
        {	
        	int x = mTileRadius;
        	
        	for (int i = 0; i < mColumnCount; i++)
        	{
        		int kittyCornerX = (int)Math.round(x - mTileRadius * 1.5);
        		int kittyCornerY = y + mTileHeight/2;
        		HexKey kittyCornerKey = new HexKey(kittyCornerX, kittyCornerY, pitch, "green");
        		
        		mBackground = mBackground.placeImage(kittyCornerKey.getImage(),
                		x - mTileRadius * 1.5, y + mTileHeight/2);
        		if (needKeys)
        		{
        			mKeys.add(kittyCornerKey);
        		}
        		pitch++;
        		
        		HexKey key = new HexKey(x, y, pitch, "red");
        		mBackground = mBackground.placeImage(key.getImage(), x, y);
        		if (needKeys)
        		{
        			mKeys.add(key);
        		}
        		pitch++;
        		
        		x += 3 * mTileRadius;
        	}
        	
        	y += mTileHeight;
        }
    }
   
    private HexKey touchKey(int x, int y) throws Exception
    {
    	for (int i = 0; i < mKeys.size(); i++)
    	{
    		HexKey key = mKeys.get(i);
    		if (key.contains(x, y))
    		{
    			key.play();
    			return key;
    		}
    	}
    	
    	throw new Exception("Key not found");
    }
    
    private HexKey releaseKey(int x, int y) throws Exception
    {
    	for (int i = 0; i < mKeys.size(); i++)
    	{
    		HexKey key = mKeys.get(i);
    		if (key.contains(x, y))
    		{
    			key.stop();
    			return key;
    		}
    	}
    	
    	throw new Exception("Key not found");
    } 
    
    private int getLowerBound (int x, int y)
    {
    	int lowerBound = mTileHeight/2;
    	for (int j = 0; j <= mRowCount; j++)
    	{
    		if (y < lowerBound)
    		{
    			break;
    		}
    		
    		lowerBound += mTileHeight/2;
    		
    		if (y < lowerBound)
    		{
    			break;
    		}
    		
    		lowerBound += mTileHeight/2;
    	}
    	
    	return lowerBound;
    }
    
    private int getHorizontalSliceNumber(int x, int y)
    {
    	int lowerBound = mTileHeight/2;
    	for (int j = 0; j <= mRowCount; j++)
    	{
    		if (y < lowerBound)
    		{
    			return(2*j);
    		}
    		
    		lowerBound += mTileHeight/2;
    		
    		if (y < lowerBound)
    		{
    			return(2*j + 1);
    		}
    		
    		lowerBound += mTileHeight/2;
    	}
    	
    	return -1;
    }

    private static int getRightBound(int x, int y)
    {
    	int rightBound = mTileRadius/2;
    	
    	for (int i = 0; i < mColumnCount; i++)
    	{
    		if (x < rightBound)
    		{
    			break;
    		}
    		
    		rightBound += mTileRadius;
    		
    		if (x < rightBound)
    		{
    			break;
    		}
    		
    		rightBound += mTileRadius/2;
    		
    		if (x < rightBound)
    		{
    			break;
    		}
    	}
    	
    	return rightBound;
    }

    private int getVerticalSliceNumber(int x, int y)
    {
    	int rightBound = mTileRadius/2;
    	
    	for (int i = 0; i < mColumnCount; i++)
    	{
    		if (x < rightBound)
    		{
    			return(3*i);
    		}
    		
    		rightBound += mTileRadius;
    		
    		if (x < rightBound)
    		{
    			return(3*i + 1);
    		}
    		
    		rightBound += mTileRadius/2;
    		
    		if (x < rightBound)
    		{
    			return(3*i + 2);
    		}
    	}
    	return -1;
    }
   
    private boolean isCentered(int columnSliceNumber)
    {
        if (columnSliceNumber % 2 == 1)
        {
        	return true;
        }
    	return false;
    }
    
    public Scene onDraw()
    { 
    	Log.v("onDraw", "Hand it back");
    	return this.mBackground;
    }

    public World onMouse(int x, int y, String me){
    	Log.v("onMouse", me);
    	HexKeyboard newWorld = null;

    	try
    	{
    		if(me.equals("long-button-down"))
    		{
    			return this;
    		}
    		if(me.equals("button-down"))
    		{
    			HexKey key = this.touchKey(x, y);
    			mBackground = mBackground.placeImage(key.getPressImage(), key.mCenter);
    			newWorld = new HexKeyboard(mDisplayHeight, mDisplayWidth, mBackground);
    		}
    		else if (me.equals("button-up"))
    		{
    			//        	int sliceRowNum = this.getHorizontalSliceNumber(x, y);
    			//        	int sliceColNum = this.getHorizontalSliceNumber(x, y);
    			//        	String msg = "Slice Column: " + sliceColNum + ", Row: " + sliceRowNum;
    			//        	Log.v("onMouse", msg);

    			HexKey key = this.releaseKey(x, y);
    			mBackground = mBackground.placeImage(key.getImage(), key.mCenter);
    			newWorld = new HexKeyboard(mDisplayHeight, mDisplayWidth, mBackground);
    		}
    	}
    	catch (Exception e)
    	{
    		Log.e("HexKeyboard::onMouse", "HexKey not found at (" + x + ", " + ")");
    	}
        
        return newWorld;
    }
}