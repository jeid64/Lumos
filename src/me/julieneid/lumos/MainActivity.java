package me.julieneid.lumos;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class MainActivity extends Activity {

	MediaPlayer ourSong;
	private static final int REQUEST_CODE = 1234;
	Button castButton;
	Button stopButton;
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	private boolean flashLock = false;
	private boolean initLock = false;
	private Camera.Parameters parameters;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		if (savedInstanceState != null) {
			initLock = savedInstanceState.getBoolean("init");
			flashLock = savedInstanceState.getBoolean("flash");
		}
		castButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				turnOnFlash();
				//startVoiceRecognitionActivity();
			}
		});

		stopButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				//turnOffCamera();
			}
		});

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putBoolean("init", initLock);
		outState.putBoolean("flash", flashLock);
		super.onSaveInstanceState(outState);
		
	}

	@Override
	public void onResume() {
		super.onResume();

		camera = Camera.open();
		startPreview();
	}

	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}

		camera.release();
		camera = null;
		inPreview = false;

		super.onPause();
	}

	// Check perms and existence of flash+speech recognizer, as well as id for
	// layout

	private void init() {
		castButton = (Button) findViewById(R.id.bCast);
		stopButton = (Button) findViewById(R.id.bStop);
		preview = (SurfaceView) findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0
				|| (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) == false) {
			castButton.setEnabled(false);
			castButton.setText("Recognizer not present");
		}
		ourSong = MediaPlayer.create(this, R.raw.wizard);
		initLock = true;
	}
	
	private void turnOnFlash() {
		// Turn on LED  
		parameters = camera.getParameters();
		parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(parameters);
		flashLock = true;
//		surfaceCallback.surfaceChanged(null, 0, 0,0);
		//ourSong.start();
	}
	
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"Voice recognition Demo...");
		startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			// Populate the wordsList with the String values the recognition
			// engine thought it heard
			ArrayList<String> matches;
			matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			for (int i = 0; i < matches.size(); i++) {
				if (matches.get(i).contentEquals("lumos")) {
					turnOnFlash();
					surfaceCallback.surfaceChanged(null, 0, 0,0);
					castButton.setText("Herro");
				}
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}

	private void initPreview(int width, int height) {
		if (camera != null && previewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(previewHolder);
			} catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
			}
			if (cameraConfigured  == false) {
				parameters = camera.getParameters();
				Camera.Size size = getBestPreviewSize(width, height, parameters);
				stopButton.setText("If statement");
				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);
					camera.setParameters(parameters);
					cameraConfigured = true;
				}
			}
			if (flashLock == true)
				turnOnFlash();
//			else if (cameraConfigured == true && flashLock == false) {
//				stopButton.setText("Else statement");
//				parameters = camera.getParameters();
//				turnOnFlash();
//				camera.setParameters(parameters);
//				flashLock = true;
//			}
//			if (flashLock = true) {
//				parameters = camera.getParameters();
//				turnOnFlash();
//				camera.setParameters(parameters);
//			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera != null) {
			camera.startPreview();
			inPreview = true;
		}
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			initPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};


}
