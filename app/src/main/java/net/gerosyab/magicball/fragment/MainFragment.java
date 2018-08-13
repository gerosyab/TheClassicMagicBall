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

package net.gerosyab.magicball.fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import net.gerosyab.magicball.R;
import net.gerosyab.magicball.activity.MainActivity;
import net.gerosyab.magicball.util.MyLog;
import net.gerosyab.magicball.view.FrontView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class MainFragment extends Fragment {

	FrontView frontView;
	Context context;
	MainActivity activity;
	TextSwitcher textSwitcher;
	TextView switcherTextView, infoTextView;
	
	static boolean isTimerRunning = false;
	static Timer timer;
	static Handler mHandler;
	Animation in;
	Animation out;
	
	int msgIdx = -1;
	int second = -1;
	boolean isEmptyString = true;
	boolean isBallTouched = false;
	
	String[] msg = {
			"SHAKE ME",
			"OR",
			"TOUCH ME",
			"MISS ME?",
			"BORING...",
			"ASK ME",
			"AND",
			"FIND THE ANSWER",
			"BUT",
			"DO NOT TRUST ME TOO MUCH",
			"I MIGHT BE WRONG",
			"SOMETIMES"
	};

	public MainFragment() {
		super();
	}

	@SuppressLint("HandlerLeak")
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.d("MainFragment", "onCreateView");
        if(context == null) context = getActivity();

		View view = inflater.inflate(R.layout.main_fragment, container, false);
		frontView = (FrontView) view.findViewById(R.id.frontview);
		textSwitcher = (TextSwitcher) view.findViewById(R.id.textswitcher);
        infoTextView = (TextView) view.findViewById(R.id.info_text);
		textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

			@Override
			public View makeView() {
			    MyLog.d("MainFragment", "onCreateView() context : " + context);
				switcherTextView = new TextView(context);
				switcherTextView.setGravity(Gravity.CENTER);
				switcherTextView.setTextSize(20);
				switcherTextView.setTextColor(Color.WHITE);
				return switcherTextView;
			}
		});

        infoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = null;
                InputStream inputStream = getResources().openRawResource(R.raw.info);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                int i;
                try {
                    i = inputStream.read();
                    while (i != -1) {
                        byteArrayOutputStream.write(i);
                        i = inputStream.read();
                    }
                    message = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final SpannableString s = new SpannableString(message);
                Linkify.addLinks(s, Linkify.WEB_URLS);

                AlertDialog infoAlertDialog = new AlertDialog.Builder(context).setMessage(s)
                        .setTitle("Information").create();

                infoAlertDialog.show();
                TextView messageText = (TextView) infoAlertDialog.findViewById(android.R.id.message);
                messageText.setTextSize(12);
            }
        });

		// for determine whether ball is clicked or not
		frontView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				float cx = frontView.getCx();
				float cy = frontView.getCy();
				float radius = frontView.getRadius();

				switch(action){
				case MotionEvent.ACTION_DOWN:
					if(Math.pow(event.getX() - cx, 2) + Math.pow(event.getY() - cy, 2) <= radius * radius){
						isBallTouched = true;
					}
					break;
				case MotionEvent.ACTION_UP:
					if(isBallTouched){
						if(Math.pow(event.getX() - cx, 2) + Math.pow(event.getY() - cy, 2) <= radius * radius){
							isBallTouched = false;
							activity.onShakingDetected();
						}
					}
					break;
				}
				return true;
			}
		});

		in = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        out = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);

        textSwitcher.setInAnimation(in);
        textSwitcher.setOutAnimation(out);

        timer = new Timer();

        // to show text message 3 seconds and not show 1 seconds in every 4 seconds
        mHandler = new Handler() {
    	    public void handleMessage(Message msg) {
    	    	second++;
    	    	if(second % 4 == 3 && !isEmptyString){
    	    		isEmptyString = true;
    	    		textSwitcher.setText("");
    	    	}
    	    	else if(second % 4 == 0 && isEmptyString) {
    	    		isEmptyString = false;
    	    		msgIdx++;
        	        if(msgIdx >= MainFragment.this.msg.length){
        	        	msgIdx = 0;
        	        }
    	    		textSwitcher.setText(MainFragment.this.msg[msgIdx]);
    	    	}


    	    }
    	};

    	startTimer();

		return view;
	}
	
	protected static void startTimer() {
	    isTimerRunning = true; 
	    
	    timer.scheduleAtFixedRate(new TimerTask() {
	        public void run() {
	            mHandler.obtainMessage(1).sendToTarget();
	        }
	    }, 0, 1000);
	}

	@Override
	public void onAttach(Context context) {
		MyLog.d("MainFragment", "onAttach");
		super.onAttach(context);
        if (context instanceof Activity) {
            activity = (MainActivity) context;
        }
	}

	@Override
	public void onResume() {
		MyLog.d("MainFragment", "onResume");
		super.onResume();
		msgIdx = -1;
		second = -1;
		isEmptyString = true;
	}

	@Override
	public void onPause() {
		timer.cancel();
		super.onPause();
	}
}
