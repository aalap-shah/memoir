package com.devapp.memoir;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
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

public class CameraFragment extends Fragment {
	MediaRecorder mMediaRecorder;
	RecorderPreview preview;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View rootView = inflater.inflate(R.layout.camera_activity, container,
				false);

		mCamera = getCameraInstance();
		mMediaRecorder = new MediaRecorder();

		if (mCamera != null) {
			mCamera.unlock();
			mMediaRecorder.setCamera(mCamera);

			preview = new RecorderPreview(this.getActivity().getApplicationContext(), mMediaRecorder);
			FrameLayout framelayout = (FrameLayout) rootView.findViewById(
					R.id.camera_preview);
			
			Log.d("asd", "Activity is " + getView());
			Log.d("asd", "framelayout is " + framelayout);
			framelayout.addView(preview);

			Button captureButton = (Button) rootView.findViewById(
					R.id.button_capture);
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
			Log.d("CameraActivity", "Camera is null");

		return rootView;
	}

	@Override
	public void onPause() {
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
			if (mCamera != null)
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
