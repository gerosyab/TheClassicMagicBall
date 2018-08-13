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
package net.gerosyab.magicball.data;

import net.gerosyab.magicball.R;

public class Const {
    public static boolean DEBUG = true;
    public static boolean VIEW_DEBUG = false;
    public static final String TAG = "magicball";
    public static final int vibTime = 300;
    public static final int[] msgID = {
            R.drawable.msg01, R.drawable.msg02, R.drawable.msg03, R.drawable.msg04, R.drawable.msg05,
            R.drawable.msg06, R.drawable.msg07, R.drawable.msg08, R.drawable.msg09, R.drawable.msg10,
            R.drawable.msg11, R.drawable.msg12, R.drawable.msg13, R.drawable.msg14, R.drawable.msg15,
            R.drawable.msg16, R.drawable.msg17, R.drawable.msg18, R.drawable.msg19, R.drawable.msg20};

    public static void setViewDebuggingMode(boolean status){
        VIEW_DEBUG = status;
    }
}
