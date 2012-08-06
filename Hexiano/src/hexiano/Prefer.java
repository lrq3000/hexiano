/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano™, an isomorphic musical keyboard for Android                  *
 *   Copyright © 2012 James Haigh                                          *
 *   Copyright © 2011, 2012 David A. Randolph                              *
 *                                                                         *
 *   FILE: Prefer.java                                                     *
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
package @CONFIG.APP_PACKAGE_NAME@;

import android.os.Bundle;
import android.preference.*;
import android.util.Log;
import android.content.Intent;
import android.net.Uri;

import @CONFIG.APP_PACKAGE_NAME@.R;

public class Prefer extends PreferenceActivity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// Links.
		// FIXME: These links should be in the XML if only I new how.
		// <ugly>
		Preference donate = (Preference) findPreference("donate");
		donate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference donate)
				{
					Log.d("Prefer", "onPreferenceClick, donate");
					Uri webpage = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=2UZVTYPPP9AUG&lc=GB&item_name=Hexiano%2Eorg&currency_code=GBP");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		Preference issue_45 = (Preference) findPreference("issue-45");
		issue_45.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference issue_45)
				{
					Log.d("Prefer", "onPreferenceClick, issue_45");
					Uri webpage = Uri.parse("https://sourceforge.net/p/isokeys/tickets/45/");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		Preference issue_18812 = (Preference) findPreference("issue-18812");
		issue_18812.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference issue_18812)
				{
					Log.d("Prefer", "onPreferenceClick, issue_18812");
					Uri webpage = Uri.parse("https://code.google.com/p/android/issues/detail?id=18812");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		Preference issue_30198 = (Preference) findPreference("issue-30198");
		issue_30198.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference issue_30198)
				{
					Log.d("Prefer", "onPreferenceClick, issue_30198");
					Uri webpage = Uri.parse("https://code.google.com/p/android/issues/detail?id=30198");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		Preference issue_10176 = (Preference) findPreference("issue-10176");
		issue_10176.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference issue_10176)
				{
					Log.d("Prefer", "onPreferenceClick, issue_10176");
					Uri webpage = Uri.parse("https://code.google.com/p/android/issues/detail?id=10176");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		Preference issue_8201 = (Preference) findPreference("issue-8201");
		issue_8201.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference issue_8201)
				{
					Log.d("Prefer", "onPreferenceClick, issue_8201");
					Uri webpage = Uri.parse("https://code.google.com/p/android/issues/detail?id=8201");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		// </ugly>
	}
}
