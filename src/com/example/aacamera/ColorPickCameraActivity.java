package com.example.aacamera;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class ColorPickCameraActivity extends Activity {
	private static Camera mCamera;
	private CameraPreview mPreview;
	public static final String TAG = "AACamera";
	public static final String FOLDER_NAME = "Kamera";
	public Context context;
	public int[] rgbArr;
	public int red, green, blue;
	private static final int mainFrameCountLimit = 1; // every frame
	private double mainFrameCount = 0;
	public static final String PREFS_NAME = "MyPrefsFile";


	public FrameLayout preview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.color_pick_camera);

		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera, previewCb);
		preview = (FrameLayout) findViewById(R.id.camera_preview_color_picker);
		preview.addView(mPreview);

		Button btn_pick_color = (Button) findViewById(R.id.btn_pick_color);
		btn_pick_color.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				red = rgbArr[0];
				green = rgbArr[1];
				blue = rgbArr[2];
				Log.w("aplikacija boja crvena:", String.valueOf(red));
				Log.w("aplikacija boja zelena:", String.valueOf(green));
				Log.w("aplikacija boja plava:", String.valueOf(blue));
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			      SharedPreferences.Editor editor = settings.edit();
			      editor.putInt("red", red);
			      editor.putInt("green", green);
			      editor.putInt("blue", blue);
			      // Commit the edits!
			      editor.commit();

				// releaseCamera();
				Intent i = new Intent(getApplicationContext(),
						MainActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});

	}

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			// Log.w("aplikacija", "Broj framea: " + mainFrameCount);
			// goes to decoder!
			if (mainFrameCount == mainFrameCountLimit && mainFrameCount != 0) {
				Camera.Parameters parameters = camera.getParameters();
				int width = parameters.getPreviewSize().width;
				int height = parameters.getPreviewSize().height;
				YuvImage yuv = new YuvImage(data,
						parameters.getPreviewFormat(), width, height, null);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

				byte[] bytes = out.toByteArray();
				final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
						bytes.length);
				int rgb = bitmap.getPixel(width / 2, height / 2);
				rgbArr = getRGBArr(rgb);
			}
			mainFrameCount++;

			/*
			 Odlazak na drugi activity ako je ispunjen uvijet!
			 a++; Log.w("aplikacija", String.valueOf(a)); if (a==b){
			 //prepoznat frejm! Intent i = new Intent(getApplicationContext(),
			 SecondActivity.class); startActivity(i); }
			 */
			// Log.w("aplikacija", "Frame došao! Byte lenght: " + data.length);
		}
	};

	public static int[] getRGBArr(int pixel) {

		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		return new int[] { red, green, blue };

	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseCamera();

	}

	public void onResume() {
		super.onResume();
		mCamera = getCameraInstance();
		if (mPreview == null) {
			mPreview = new CameraPreview(this, mCamera, previewCb);
			preview = (FrameLayout) findViewById(R.id.camera_preview_color_picker);
			preview.addView(mPreview);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.w("aplikacija", "Back pritisnut!");
		releaseCamera();
	}

	public static void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

}
