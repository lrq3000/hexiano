/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, an isomorphic musical keyboard for Android                   *
 *   Copyright 2011, 2012 David A. Randolph                                *
 *                                                                         *
 *   FILE: Inneract.java                                                     *
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

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;

import com.inneractive.api.ads.InneractiveAdComponent;
import com.inneractive.api.ads.InneractiveAdEventsListener;
import com.inneractive.api.ads.InneractiveAdView;

public class Inneract extends Activity implements InneractiveAdEventsListener
{
	LinearLayout linear;
	InneractiveAdView iaAdView;
	TextView text;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		linear = new LinearLayout(this);
		linear.setOrientation(LinearLayout.VERTICAL);
		
		Hashtable<String, String> metaData = new Hashtable<String, String>();
		metaData.put(InneractiveAdComponent.KEY_KEYWORDS,
				"music,keyboard,piano,compose,isomorphic,electronic,organ,learn,theory");
		
		iaAdView = InneractiveAdComponent.getAdView(
				Inneract.this, "FloofMachine_IsoKeys_Android",
				InneractiveAdComponent.FULL_SCREEN_AD_TYPE,
				60,
				metaData); 
		iaAdView.setListener(this);
		linear.addView(iaAdView);
		setContentView(linear);
	}
	
	@Override public void inneractiveOnFailedToReceiveAd(InneractiveAdView adView) {
		Log.i("Inneract" , "inneractiveOnFailedToReceiveAd...");
	}
	@Override public void inneractiveOnReceiveAd(InneractiveAdView adView) {
		Log.i("Inneract" , "inneractiveOnReceiveAd...");
	}
	@Override public void inneractiveOnClickAd(InneractiveAdView adView) {
		Log.i("Inneract" , "inneractiveOnClickAd...");
	}
	@Override public void inneractiveOnReceiveDefaultAd(InneractiveAdView adView) {
		Log.i("Inneract" , "inneractiveOnReceiveDefaultAd...");
	}

}
