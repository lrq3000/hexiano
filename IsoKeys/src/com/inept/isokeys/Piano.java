/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   IsoKeys, Copyright 2011 David A. Randolph                             *
 *                                                                         *
 *   FILE: Piano.java                                                *
 *                                                                         *
 *   This file is part of IsoKeys, an open-source project                  *
 *   hosted at http://isokeys.sourceforge.net.                            *
 *                                                                         *
 *   IsoKeys is free software: you can redistribute it and/or              *
 *   modify it under the terms of the GNU General Public License           *
 *   as published by the Free Software Foundation, either version          *
 *   3 of the License, or (at your option) any later version.              *
 *                                                                         *
 *   IsoKeys is distributed in the hope that it will be useful,       *
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

public class Piano extends Instrument
{
	public Piano(Context context)
	{
		super(context);
		addSound(21, R.raw.pno021v116leo);
		addSound(24, R.raw.pno024v117leo);
		addSound(27, R.raw.pno027v119leo);
		addSound(30, R.raw.pno030v115leo);
		addSound(33, R.raw.pno033v110leo);
		addSound(36, R.raw.pno036v120leo);
		addSound(39, R.raw.pno039v120leo);
		addSound(42, R.raw.pno042v120leo);
		addSound(45, R.raw.pno045v117leo);
		addSound(48, R.raw.pno048v118leo);
		addSound(51, R.raw.pno051v115leo);
		addSound(54, R.raw.pno054v117leo);
		addSound(57, R.raw.pno054v117leo);
		addSound(57, R.raw.pno057v117leo);
		addSound(60, R.raw.pno060v117leo);
		addSound(63, R.raw.pno063v115leo);
		addSound(66, R.raw.pno066v120leo);
		addSound(69, R.raw.pno069v115leo);
		addSound(72, R.raw.pno072v117leo);
		addSound(75, R.raw.pno075v115leo);
		addSound(78, R.raw.pno078v117leo);
		addSound(81, R.raw.pno081v117leo);
		addSound(84, R.raw.pno084v117leo);
		addSound(87, R.raw.pno087v117leo);
		addSound(90, R.raw.pno090v120leo);
		addSound(93, R.raw.pno093v112leo);
		addSound(96, R.raw.pno096v115leo);
		addSound(96, R.raw.pno096v115leo);
		addSound(102, R.raw.pno102v118leo);
	}
}