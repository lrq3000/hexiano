/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, Copyright 2011 David A. Randolph                             *
 *                                                                         *
 *   FILE: HexKeyboard.java                                                *
 *                                                                         *
 *   This file is part of IsoKeys, an open-source project                  *
 *   hosted at http://isokeys.sourceforge.net.                            *
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.image.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.world.VoidWorld;
import android.world.Posn;
import java.util.ArrayList;

public class HexKeyboard extends View
{
    static final int TITLE_BAR_HEIGHT = 50;
    static Bitmap mBitmap;
    static Paint mPaint = Image.WHITE;
    static int mDisplayWidth = 0;
    static int mDisplayHeight = 0;
    static int mRowCount = 0;
    static int mColumnCount = 0;
    static int mTileRadius = 64; // What about dip (density-independent pixels)?!
    static int mTileHeight = 0;
    
    static ArrayList<HexKey> mKeys = new ArrayList<HexKey>();
    
    void setUpBoard(int displayHeight, int displayWidth)
    {
        mDisplayWidth = displayWidth;
        mDisplayHeight = displayHeight;
    	mTileHeight = (int)(Math.round(Math.sqrt(3.0) * mTileRadius));
       
        mRowCount = displayHeight/mTileHeight + 1;
        // mRowCount = 10;
        
        mColumnCount = displayWidth/(mTileRadius * 3) + 2;
    	
    	if (! mKeys.isEmpty())
    	{
    		return;
    	}
        
        int y = 0;
		int pitch = 0;
		
        for (int j = 0; j < mRowCount; j++)
        {	
        	int x = mTileRadius;
        	
        	for (int i = 0; i < mColumnCount; i++)
        	{
        		int kittyCornerX = (int)Math.round(x - mTileRadius * 1.5);
        		int kittyCornerY = y + mTileHeight/2;
        		HexKey kittyCornerKey = 
        				new HexKey(mTileRadius, new Posn(kittyCornerX, kittyCornerY), pitch, "green");
        		
        		mKeys.add(kittyCornerKey);
        		pitch++;
        		
        		HexKey key = new HexKey(mTileRadius, new Posn(x, y), pitch, "red");
        		mKeys.add(key);
        		pitch++;
        		
        		x += 3 * mTileRadius;
        	}
        	
        	y += mTileHeight;
        }
        
    	mBitmap = Bitmap.createBitmap(mDisplayWidth, mDisplayHeight, Bitmap.Config.ARGB_8888);
    	Canvas tempCanvas = new Canvas(mBitmap);
    	this.onDraw(tempCanvas);
    }
   
    public HexKeyboard(Context context, int height, int width, int keyRadius)
	{
		super(context);
    	setUpBoard(height, width);
	}
    
    private void pressKey(int x, int y) throws Exception
    {
    	for (int i = 0; i < mKeys.size(); i++)
    	{
    		HexKey key = mKeys.get(i);
    		if (key.contains(x, y))
    		{
    			key.play();
    			return;
    		}
    	}
    	
    	throw new Exception("Key not found");
    }
    
    private void releaseKey(int x, int y) throws Exception
    {
    	for (int i = 0; i < mKeys.size(); i++)
    	{
    		HexKey key = mKeys.get(i);
    		if (key.contains(x, y))
    		{
    			key.stop();
    			return;
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
   
    public void onDraw(Canvas canvas)
    { 
    	Canvas tempCanvas = new Canvas(mBitmap);
    	for (HexKey k : mKeys)
    	{
    		k.paint(tempCanvas);
    	}
    	
    	canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    public boolean onTouchEvent(MotionEvent event){
    	Log.v("onMouse", event.toString());

    	int x = -1;
    	int y = -1;
    	
    	int actId = event.getAction();
    	try
    	{
    		if (actId == MotionEvent.ACTION_DOWN ||
    				actId == MotionEvent.ACTION_POINTER_DOWN)
    		{
    			x = (int)event.getX();
    			y = (int)event.getY();
    			this.pressKey(x, y);
    			this.invalidate();
    		}
    		else if (actId == MotionEvent.ACTION_UP ||
    				actId == MotionEvent.ACTION_POINTER_UP)
    		{
    			x = (int)event.getX();
    			y = (int)event.getY();
    			this.releaseKey(x, y);
    			this.invalidate();
    		}
    		
    	}
    	catch (Exception e)
    	{
    		Log.e("HexKeyboard::onMouse", "HexKey not found at (" + x + ", " + y + ")");
    	}
    	
        return true;
    }
}