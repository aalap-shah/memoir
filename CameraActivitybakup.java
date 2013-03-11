package com.devapp.memoir;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

class RecorderPreview extends SurfaceView implements SurfaceHolder.Callback {
	// Create objects for MediaRecorder and SurfaceHolder.
	MediaRecorder recorder;
	SurfaceHolder holder;
	boolean mrecorderInitialized = true;

	// Create constructor of Preview Class. In this, get an object of
	// surfaceHolder class by calling getHolder() method. After that add
	// callback to the surfaceHolder. The callback will inform when surface is
	// created/changed/destroyed. Also set surface not to have its own buffers.

	public RecorderPreview(Context context, MediaRecorder temprecorder) {
		super(context);
		recorder = temprecorder;
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	// Implement the methods of SurfaceHolder.Callback interface

	// SurfaceCreated : This method gets called when surface is created.
	// In this, initialize all parameters of MediaRecorder object as explained
	// above.

	public void surfaceCreated(SurfaceHolder holder) {
		// Step 2: Set sources
		recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

		// Step 4: Set output file
		recorder.setOutputFile("/sdcard/recordvideooutput.3gpp");

		recorder.setPreviewDisplay(holder.getSurface());

		try {
			recorder.prepare();
		} catch (Exception e) {
			mrecorderInitialized = false;
			String message = e.getMessage();
			Log.d("RecorderPreview", "Error message = " + message);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d("RecorderPreview", "SurfaceHolder changed");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("RecorderPreview", "SurfaceHolder destroyed");
		if (recorder != null) {
			recorder.release();
			recorder = null;
		}

	}
}

public class CameraActivity extends Activity {
	private MediaRecorder mMediaRecorder;
	private RecorderPreview preview;
	Camera mCamera;
	boolean start = true;

	private Camera getCameraInstance() {
		/** A safe way to get an instance of the Camera object. */
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			Log.d("RecorderPreview", "Camera is unavailable");
		}
		return c; // returns null if camera is unavailable
	}

	boolean isRecording = false;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private SurfaceView mPreview;
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	private boolean prepareVideoRecorder(){

	    mCamera = getCameraInstance();
	    mMediaRecorder = new MediaRecorder();

	    // Step 1: Unlock and set camera to MediaRecorder
	    mCamera.unlock();
	    mMediaRecorder.setCamera(mCamera);

	    // Step 2: Set sources
	    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
	    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
	    
	    // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
	    mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

	    // Step 4: Set output file
	    mMediaRecorder.setOutputFile(getOutputMediaFile(2).toString());
	    //mMediaRecorder.setOutputFile("/mnt/sdcard/Movies/1.mp4");

		preview = new RecorderPreview(this, mMediaRecorder);
		FrameLayout framelayout = (FrameLayout) findViewById(R.id.camera_preview);
		framelayout.addView(preview);

	    // Step 5: Set the preview output
	    //mMediaRecorder.setPreviewDisplay(preview.getHolder().getSurface());

	    // Step 6: Prepare configured MediaRecorder
	    try {
	        mMediaRecorder.prepare();
	    } catch (IllegalStateException e) {
	        Log.d("asd", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	        Log.d("asd", "IOException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    }
	    
		// Add a listener to the Capture button
		Button captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
		    new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
		            if (isRecording) {
		                // stop recording and release camera
		                mMediaRecorder.stop();  // stop the recording
		                releaseMediaRecorder(); // release the MediaRecorder object
		                mCamera.lock();         // take camera access back from MediaRecorder

		                // inform the user that recording has stopped
		                //setCaptureButtonText("Capture");
		                isRecording = false;
		            } else {
		                // initialize video camera
		                if (prepareVideoRecorder()) {
		                    // Camera is available and unlocked, MediaRecorder is prepared,
		                    // now you can start recording
		                    mMediaRecorder.start();

		                    // inform the user that recording has started
		                    //setCaptureButtonText("Stop");
		                    isRecording = true;
		                } else {
		                    // prepare didn't work, release the camera
		                    releaseMediaRecorder();
		                    // inform user
		                }
		            }
		        }
		    }
		);
	    return true;

	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);

		
		
		//prepareVideoRecorder();
		
		
		
		
		
		mCamera = getCameraInstance();
		mMediaRecorder = new MediaRecorder();

		if (mCamera != null) {
			mCamera.unlock();
			mMediaRecorder.setCamera(mCamera);

			preview = new RecorderPreview(this, mMediaRecorder);
			FrameLayout framelayout = (FrameLayout) findViewById(R.id.camera_preview);
			framelayout.addView(preview);

			Button captureButton = (Button) findViewById(R.id.button_capture);
			captureButton.setOnClickListener(new View.OnClickListener() {
				boolean isRecording = false;

				@Override
				public void onClick(View v) {
					if (isRecording) {
						// stop recording and release camera
						mMediaRecorder.stop(); // stop the recording
						releaseMediaRecorder(); // release the MediaRecorder
												// object
						mCamera.lock(); // take camera access back from
										// MediaRecorder

						isRecording = false;
					} else {
						// initialize video camera
						if (preview.mrecorderInitialized) {
							// Camera is available and unlocked, MediaRecorder
							// is prepared,
							// now you can start recording
							mMediaRecorder.start();
						} else {
							// prepare didn't work, release the camera
							releaseMediaRecorder();
							// inform user
						}
					}
				}
			});
		} else
			Log.d ("CameraActivity", "Camera is null");
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaRecorder(); // if you are using MediaRecorder, release it
								// first
		releaseCamera(); // release the camera immediately on pause event
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

}
