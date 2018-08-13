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
package net.gerosyab.magicball.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import net.gerosyab.magicball.R;
import net.gerosyab.magicball.data.Const;
import net.gerosyab.magicball.fragment.MainFragment;
import net.gerosyab.magicball.fragment.MsgFragment;
import net.gerosyab.magicball.util.MyLog;
import net.gerosyab.magicball.util.Shaker;

public class MainActivity extends AppCompatActivity implements Shaker.Callback {

    Context context;
    LinearLayout motherLinear;
    LinearLayout contentLinear;
    static Shaker shaker;

    MainFragment mainFragment;
    MsgFragment msgFragment;

    public static Vibrator vibrator;

    private AdView mAdView;

    private boolean mBackKeyFlag = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            if(msg.what == 0){
                mBackKeyFlag = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-4746845653236332~3245362320");

        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());

        context = getApplicationContext();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        motherLinear = (LinearLayout) findViewById(R.id.mother_linear);
        contentLinear = (LinearLayout) findViewById(R.id.content_linear);

        mainFragment = new MainFragment();
        msgFragment = new MsgFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.content_linear, mainFragment).commit();

    }

    @Override
    public void onResume() {
        MyLog.d("MainActivity", "onResume");
        super.onResume();

        if (mAdView != null) {
            mAdView.resume();
        }

        if(shaker == null){
            shaker = new Shaker(context, this);
        }
        shaker.open();
    }

    @Override
    public void onPause() {
        MyLog.d("MainActivity", "onPause");
        super.onPause();

        if (mAdView != null) {
            mAdView.pause();
        }

        if(shaker != null){
            shaker.close();
        }
    }

    /** Called before the activity is destroyed. */
    @Override
    public void onDestroy() {
        MyLog.d("MainActivity", "onDestroy");
        // Destroy the AdView.
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        MyLog.d("MainActivity", "onBackPressed getSupportFragmentManager().getBackStackEntryCount() " + getSupportFragmentManager().getBackStackEntryCount());
        // Catch back action and pops from backstack
        // (if you called previously to addToBackStack() in your transaction)
        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            MyLog.d("MainActivity", "onBackPressed getSupportFragmentManager().getBackStackEntryCount() > 0");
            getSupportFragmentManager().popBackStack();
        }
        // Default action on back pressed
        else if (!mBackKeyFlag) {
            MyLog.d("MainActivity", "onBackPressed !mBackKeyFlag");
            Toast.makeText(context, "Back Again to Exit", Toast.LENGTH_SHORT).show();
            mBackKeyFlag = true;
            mHandler.sendEmptyMessageDelayed(0, 2000);
        }
        else {
            super.onBackPressed();
        }

    }

    @Override
    public void onShakingDetected() {
        MyLog.d("MainActivity", "onShakingDetected getSupportFragmentManager().getBackStackEntryCount() " + getSupportFragmentManager().getBackStackEntryCount());
        vibrator.vibrate(Const.vibTime);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            MyLog.d("MainActivity", "fragmentManager.getBackStackEntryCount() > 0");
            if(msgFragment != null) {
                MyLog.d("MainActivity", "setNewMessage()");
                msgFragment.setNewMessage();
            }
        }
        else {
            MyLog.d("MainActivity", "fragmentManager.getBackStackEntryCount() == 0");
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE|FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            transaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE|FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_linear, msgFragment).addToBackStack(null).commitAllowingStateLoss();
            MyLog.d("MainActivity", "after transaction fragmentManager.getBackStackEntryCount() " + getSupportFragmentManager().getBackStackEntryCount());
        }
    }

    public static Shaker getShakerInstance(){
        return shaker;
    }
}
