package com.example.aacamera;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

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
import android.widget.FrameLayout;

public class ScanActivity extends Activity{

	private static Camera mCamera;
	private CameraPreview mPreview;
	public static final String TAG = "AACamera";
	public static final String FOLDER_NAME = "Kamera";
	public Context context;

	private static final int colorTolerance = 20;
	private static final float COLOR_MATCH_LIMIT = (float) 0.15; // limit for app to recognize color!
	private static final int mainFrameCountLimit = 50; // every frame to take
	private final int REQUIRED_SIZE = 50; // to resize
	
	private static int red,blue,green;
	public SharedPreferences settings;

	private int mainFrameCount = 0;

	public FrameLayout preview;

	private int picture_height, picture_width;
	private static double mainPixelCounter = 0;
	private static double colorMatchPixelCounter = 0;
	
	public static final String PREFS_NAME = "MyPrefsFile";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.scan_activity);

		mCamera = getCameraInstance();
		
		
		settings = getSharedPreferences(PREFS_NAME, 0);
		red = settings.getInt("red", 1);
		blue = settings.getInt("blue", 1);
		green = settings.getInt("green", 1);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera, previewCb);
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

	}

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			// Log.w("aplikacija", "Broj framea: " + mainFrameCount);
			if (mainFrameCount == mainFrameCountLimit && mainFrameCount != 0) { // every mainFrameCountLimit-th frame
																				// goes to decoder!
				mainFrameCount = 0;
				mainPixelCounter = 0;
				colorMatchPixelCounter = 0;
				Camera.Parameters parameters = camera.getParameters();
				int width = parameters.getPreviewSize().width;
				int height = parameters.getPreviewSize().height;

				/*
				 * format odma u rgb pa bajtovi iz tog u bitmapu
				 * parameters.setPreviewFormat(ImageFormat.RGB_565); final
				 * Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
				 * data.length);
				 */

				// iz yuv u jpeg i onda iz njega u bitmapu
				YuvImage yuv = new YuvImage(data,
						parameters.getPreviewFormat(), width, height, null);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

				byte[] bytes = out.toByteArray();
				final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
						bytes.length);
				if (checkFrameForMatch(bitmap)) {
					finish();
					Intent i = new Intent(getApplicationContext(),
							SecondActivity.class);
					startActivity(i);
				} else {
					Log.w("aplikacija", "NOP!");
				}
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

	private boolean checkFrameForMatch(Bitmap bitmap) {
		try {
			final Bitmap resizedBitmap = resizeBitmap(bitmap);
			picture_height = resizedBitmap.getHeight();
			picture_width = resizedBitmap.getWidth();
			String visina = Integer.toString(picture_height);
			String sirina = Integer.toString(picture_width);
			Log.w("Visina slike: ", visina);
			Log.w("Sirina slike: ", sirina);

			for (int i = 0; i < picture_width; i++) {

				for (int j = 0; j < picture_height; j++) {

					int rgb = resizedBitmap.getPixel(i, j);
					int[] rgbArr = getRGBArr(rgb);

					if (isSimilarToColors(rgbArr)) {
						colorMatchPixelCounter++;
					}
					mainPixelCounter++;
				}
			}
			Log.w("ALGORITAMMMM", "broj: " + mainPixelCounter);
			Log.w("ALGORITAMMMM", "broj pogodaka: " + colorMatchPixelCounter);

			// rezultat statusa
			double rezultat = (colorMatchPixelCounter / mainPixelCounter);
			if (rezultat >= COLOR_MATCH_LIMIT) {
				return true;
			} else {
				return false;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	private Bitmap resizeBitmap(Bitmap bitmap) throws FileNotFoundException {
		/* Saving original picture to Pictures/ directory!
		String path = Environment.getExternalStorageDirectory().toString() + "/Pictures/";
		OutputStream fOut = null;
		File file = new File(path, "OriginalPhoto.png");
		fOut = new FileOutputStream(file);
		bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);*/
		// Find the correct scale value. It should be the power of 2.
		int width_tmp = bitmap.getWidth(), height_tmp = bitmap.getHeight();
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
		}

		// Decode with inSampleSize
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width_tmp,
				height_tmp, false);
		/* Saving resized picture to Pictures/ directory! 
		File file2 = new File(path, "ResizedPhoto.png");
		fOut = new FileOutputStream(file2);
		resizedBitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);*/
		return resizedBitmap;
	}

	public static int[] getRGBArr(int pixel) {

		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		return new int[] { red, green, blue };

	}

	// http://www.rapidtables.com/web/color/RGB_Color.htm
	public static boolean isSimilarToColors(int[] rgbArr) {
		int[] colorNumber1 = { red, green, blue };
		int[] zelena = { 0, 220, 0 };

		int rrDif = rgbArr[0] - colorNumber1[0];
		int rgDif = rgbArr[1] - colorNumber1[1];
		int rbDif = rgbArr[2] - colorNumber1[2];

		/*if ((rrDif >= -colorTolerance)
				&& (rgDif >= -colorTolerance && rgDif <= colorTolerance)
				&& (rbDif >= -colorTolerance && rbDif <= colorTolerance))*/
		if ((rrDif >= -colorTolerance) && (rgDif <= colorTolerance)
				&& (rbDif <= colorTolerance)) {
			return true;
		} else {
			return false;
		}
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
		red = settings.getInt("red", 1);
		blue = settings.getInt("blue", 1);
		green = settings.getInt("green", 1);
		
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
