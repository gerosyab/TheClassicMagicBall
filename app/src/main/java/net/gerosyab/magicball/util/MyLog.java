/*
 *     TheClassicMagicBall - Android Magic 8 Ball Simulator
 *     Copyright (C) 2014 gerosyab
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.gerosyab.magicball.util;

import net.gerosyab.magicball.data.Const;
import android.util.Log;

public class MyLog {
	
	public static void i(String tag, String message)	{ if(Const.DEBUG) Log.i(tag, message); }
	public static void w(String tag, String message) 
	{
		if(Const.DEBUG) Log.w(tag, message);
	}
	public static void d(String tag, String message) 
	{
		if(Const.DEBUG) Log.d(tag, message);
	}
	public static void e(String tag, String message) 
	{
		if(Const.DEBUG) Log.e(tag, message);
	}
}
