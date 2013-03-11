package com.devapp.memoir;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.devapp.memoir.R;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class Splash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.splashanimations);
		TextView tv = (TextView) findViewById(R.id.splashtitle);
		tv.startAnimation(animation);
		
		final int splashtime = 3000;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Thread splashthread =  new Thread() {
			int wait = 0;
			
			@Override
			public void run() {
				try {
					super.run();
					while (wait < splashtime){
						sleep(100);
						wait += 100;
					}
				} catch (Exception e) {
					System.out.println ("Exception = " + e);
				} finally {
					Intent i;
			        if(!prefs.getBoolean("first_time", false))
			        {
			            i = new Intent(Splash.this, Welcome.class);
			            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			        }
			        else
			        {
			        	i = new Intent(Splash.this, MainActivity.class);
			        	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			        }
					startActivity (i);
					finish();
				}
			}
		};
		splashthread.start();
		
		try {
			
			InputStream is = this.getResources().openRawResource(R.raw.ffmpeg);
			String path = "/data/local/ffmpeg";
			OutputStream stream = new BufferedOutputStream(new FileOutputStream(path)); 
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int len = 0;
			while ((len = is.read(buffer)) != -1) {
			    stream.write(buffer, 0, len);
			}
			if(stream!=null)
			    stream.close();
			
			
			// Executes the command.
		    Process process = Runtime.getRuntime().exec("/system/bin/chmod 777 /data/local/ffmpeg");
		    
		    // Reads stdout.
		    // NOTE: You can write to stdin of the command using
		    //       process.getOutputStream().
		    BufferedReader reader = new BufferedReader(
		            new InputStreamReader(process.getInputStream()));
		    int read;
		    char[] buffer2 = new char[4096];
		    StringBuffer output = new StringBuffer();
		    while ((read = reader.read(buffer2)) > 0) {
		        output.append(buffer2, 0, read);
		    }
		    reader.close();
		    
		    // Waits for the command to finish.
		    process.waitFor();
		    Log.d("asd", "Output.string > " + output.toString());
	
		    
			// Executes the command.
		    Process process2 = Runtime.getRuntime().exec("/data/local/ffmpeg --version");
		    
		    // Reads stdout.
		    // NOTE: You can write to stdin of the command using
		    //       process.getOutputStream().
		    BufferedReader reader2 = new BufferedReader(
		            new InputStreamReader(process2.getInputStream()));
		    StringBuffer output2 = new StringBuffer();
		    while ((read = reader2.read(buffer2)) > 0) {
		        output2.append(buffer2, 0, read);
		    }
		    reader2.close();
		    
		    // Waits for the command to finish.
		    process2.waitFor();
		    Log.d("asd", "Output2.string > " + output2.toString());
		    //return output.toString();
		    
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("asd", "Exception " + e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("asd", "Exception " + e);
		}
	}

}
