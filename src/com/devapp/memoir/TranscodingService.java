package com.devapp.memoir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import android.app.Application;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class TranscodingService extends IntentService {

	private File extStorePath;
	
	public TranscodingService() {
		super("TranscodingService");
	    extStorePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
	}

	
	@Override
	protected void onHandleIntent(Intent intent) {

		long startDate, endDate;
		startDate = intent.getLongExtra("startDate", 0);
		endDate = intent.getLongExtra("endDate", -1);
		
		ArrayList<ArrayList<Video>> dateList = ((MemoirApplication) getApplication()).getDBA().getVideos(startDate, endDate, true);

		int i = 0;
		ArrayList<Video> videoList = null;
		Video v = null;
		Movie[] inMovies = new Movie[dateList.size()];
		File videoFile = null;
		
		for(i = 0; i < dateList.size(); i++) {
			videoList = dateList.get(i);
			v = videoList.get(0);
			videoFile = new File(v.path);
			
			try {
				inMovies[i] = MovieCreator.build(new FileInputStream(videoFile).getChannel());
			} catch (FileNotFoundException e) {
				Log.d("asd", "In FileNotFoundException " + e);
				e.printStackTrace();
			} catch (IOException e) {
				Log.d("asd", "In IO EXCEPTION " + e);
				e.printStackTrace();
			}
		}
		
		
		try {

			Log.d("asd", "Starting :)");
	        //File video1 = new File(extStorePath, "1.mp4");
	        //File video2 = new File(extStorePath, "2.mp4");

	        //{MovieCreator.build(new FileInputStream(video1).getChannel()), MovieCreator.build(new FileInputStream(video2).getChannel())};

	        List<Track> videoTracks = new LinkedList<Track>();
	        List<Track> audioTracks = new LinkedList<Track>();

	        for (Movie m : inMovies) {
	            for (Track t : m.getTracks()) {
	                if (t.getHandler().equals("soun")) {
	                    audioTracks.add(t);
	                }
	                if (t.getHandler().equals("vide")) {
	                    videoTracks.add(t);
	                }
	            }
	        }

	        Movie result = new Movie();

	        if (audioTracks.size() > 0) {
	            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
	        }
	        if (videoTracks.size() > 0) {
	            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
	        }

	        IsoFile out = new DefaultMp4Builder().build(result);

			boolean mExternalStorageAvailable = false;
			boolean mExternalStorageWriteable = false;
			String state = Environment.getExternalStorageState();

			Log.d("asd", "1111");
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    // We can read and write the media
			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			    
			    Log.d("asd", "Path  " + extStorePath.toString());
			    File file = new File(extStorePath, "/output.mp4");
			    Log.d("asd", "File " + file.getAbsolutePath());
			    FileOutputStream fos = new FileOutputStream(file);
			    Log.d("asd", "size > " + out.getSize());
				out.getBox(fos.getChannel());
				fos.close();
			    
				Log.d("asd", "2");
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
				Log.d("asd", "3");
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
				Log.d("asd", "4");
			}
			
			
			
			//FileOutputStream fos = openFileOutput(String.format("output.mp4"), Context.MODE_WORLD_WRITEABLE);
			Log.d("asd", "Reached till the end :) ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("asd", "In IO EXCEPTION " + e);
			e.printStackTrace();
		}
		
	}
	
	

}