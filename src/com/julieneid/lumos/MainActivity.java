package com.julieneid.lumos;

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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class MainActivity extends Activity {

	Button speakButton;
	Button stopButton;
	MediaPlayer ourSong;
	private static final int REQUEST_CODE = 1234;
	private Camera cam;
	boolean camLock = false;
	boolean wizardLock = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();

		speakButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				startVoiceRecognitionActivity();
			}
		});

		stopButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				turnOffCamera();
			}
		});

	}

	// Check perms and existence of flash+speech recognizer, as well as id for
	// layout

	private void init() {
		speakButton = (Button) findViewById(R.id.bRecord);
		stopButton = (Button) findViewById(R.id.bShutoff);
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0
				|| (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) == false) {
			speakButton.setEnabled(false);
			speakButton.setText("Recognizer not present");
		}
		ourSong = MediaPlayer.create(this, R.raw.wizard);
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
					turnOnCamera();
				}
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void turnOnCamera() {
		if (camLock == false) {
			cam = Camera.open();
			camLock = true;
		}
		Parameters p = cam.getParameters();
		if (p.getFlashMode() != Parameters.FLASH_MODE_TORCH) {
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			cam.setParameters(p);
			cam.startPreview();
		}
		if (camLock == true && wizardLock == false) {
			ourSong.start();
			wizardLock = true;
		}
	}

	private void turnOffCamera() {
		Parameters p = cam.getParameters();
		if (wizardLock == true)
			ourSong.stop();
		if (p != null) {
			cam.stopPreview();
		} else {
			return;
		}
	}

}
