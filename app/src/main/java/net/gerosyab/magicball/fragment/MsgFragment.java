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

import net.gerosyab.magicball.R;
import net.gerosyab.magicball.util.MyLog;
import net.gerosyab.magicball.util.MyRandom;
import net.gerosyab.magicball.view.MsgView;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MsgFragment extends Fragment {

    Context context;
	MsgView msgView;
    TextView infoTextView;

	public MsgFragment() {
		super();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.d("MsgFragment", "onCreateView");
        if(context == null) context = getActivity();

		View view = inflater.inflate(R.layout.msg_fragment, container, false);
		msgView = (MsgView)view.findViewById(R.id.msgview);

		msgView.getHolder().setFormat(PixelFormat.TRANSPARENT);
		
		msgView.setMsgIdx(MyRandom.getNum());

        infoTextView = (TextView) view.findViewById(R.id.info_text);
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
                Linkify.addLinks(s, Linkify.ALL);

                AlertDialog infoAlertDialog = new AlertDialog.Builder(context).setMessage(s)
                        .setTitle("Information").create();

                infoAlertDialog.show();
                TextView messageText = (TextView) infoAlertDialog.findViewById(android.R.id.message);
                messageText.setTextSize(14);
            }
        });

		return view;
	}
	
	public void setNewMessage() {
		MyLog.d("MsgFragment", "setNewMessage");
		if(msgView != null) {
			msgView.setMsgIdx(MyRandom.getNum());
			msgView.notifyMsgChanged();
		}
	}
	
}
