package com.example.aacamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public Context context;
	public static final String PREFS_NAME = "MyPrefsFile";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_main);

		Button btn_scan = (Button) findViewById(R.id.btn_scan_color);
		btn_scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),
						ScanActivity.class);
				startActivity(i);
			}
		});

		Button btn_pick_color_camera = (Button) findViewById(R.id.btn_pick_color_camera);
		btn_pick_color_camera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),
						ColorPickCameraActivity.class);
				startActivity(i);
			}
		});

		Button btn_color_picker = (Button) findViewById(R.id.btn_color_picker);
		btn_color_picker.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(context, "Hopa!", Toast.LENGTH_LONG).show();
				/*Intent i = new Intent(getApplicationContext(),ColorPickCameraActivity.class);
				startActivity(i);*/
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		int red = settings.getInt("red", 1);
		int blue = settings.getInt("blue", 1);
		int green = settings.getInt("green", 1);
		ImageView iv_picked_color = (ImageView) findViewById(R.id.iv_picked_color);
		iv_picked_color.setBackgroundColor(Color.rgb(red, green, blue));
	}

}