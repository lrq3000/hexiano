package com.inept.isokeys;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;

public class Play extends Activity {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Display display = getWindowManager().getDefaultDisplay();

		int displayWidth = display.getWidth();
		int displayHeight = display.getHeight(); // - TITLE_BAR_HEIGHT;

		HexKeyboard keyWorld = new HexKeyboard(displayHeight, displayWidth);	  
		keyWorld.bigBangLandscapeFullscreen(this);
		
		// setContentView(R.layout.main);
	}
}
