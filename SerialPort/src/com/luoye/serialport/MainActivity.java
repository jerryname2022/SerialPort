package com.luoye.serialport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	EditText mEditText;
	Button mButton;
	TextView mTextView;

	// SerialPortUtil mSerialPortUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mEditText = (EditText) findViewById(R.id.main_edit);
		mButton = (Button) findViewById(R.id.main_button);
		mTextView = (TextView) findViewById(R.id.main_textView);

		
		final SerialHelper helper = new SerialHelper();
		helper.setOnDataReceiveListener(new SerialHelper.OnDataReceiveListener() {

			@Override
			public void onReceive(final byte[] buffer) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {
							mTextView.setText(new String(buffer,"GB2312")+" : "+new String(buffer,"GBK"));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

			}
		});
		
		try {
			helper.open();
		} catch (InvalidParameterException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SerialPortFinder spf = new SerialPortFinder();
		String[] devices = spf.getAllDevicesPath();

		if (devices != null) {
			for (String d : devices) {
				mTextView.setText(mTextView.getText().toString() + " : " + d);
			}
		}

		mButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				try {
					helper.send((mEditText.getText().toString())
							.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
}
