package com.example.aacamera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewCallback pcb;
		
    private static String TAG = "AACamera";

    public CameraPreview(Context context, Camera camera, PreviewCallback pcb) {
        super(context);
        mCamera = camera;
        this.pcb = pcb;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    	//mCamera.stopPreview();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(pcb);
/*            mCamera.setPreviewCallback(new PreviewCallback() {

            public void onPreviewFrame(byte[] data, Camera camera) { //greška kad se izaðe iz app-a
            	a++;
            	Log.w("aplikacija", String.valueOf(a));
            	if (a==b){
            		
            	}
            	//sleeps 1 sec
            	
            	Camera.Parameters parameters = camera.getParameters();

                int width = parameters.getPreviewSize().width;
                int height = parameters.getPreviewSize().height;

                ByteArrayOutputStream outstr = new ByteArrayOutputStream();
                Rect rect = new Rect(0, 0, width, height); 
                YuvImage yuvimage=new YuvImage(data,ImageFormat.NV21,width,height,null);
                yuvimage.compressToJpeg(rect, 100, outstr);
                Bitmap bmp = BitmapFactory.decodeByteArray(outstr.toByteArray(), 0, outstr.size());
                Log.w("aplikacija", "dajem frame iz onPreviewa!");
            }
        });*/
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            mCamera.stopPreview();
        }
    }
    
}