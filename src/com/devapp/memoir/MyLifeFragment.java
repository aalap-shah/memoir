package com.devapp.memoir;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class MyLifeFragment extends Fragment {

	ListView mDateList = null;
	MyLifeDateListArrayAdapter mDateAdapter = null;
	List<List<Video>> mVideos = null;
	View mRootView = null;
	MediaController mMc = null;
	VideoView mVv = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_my_life, container,
				false);
		Log.d("asd", "Calling onCreateView");
		mRootView = rootView;
		return rootView;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	mDateList = (ListView) getActivity().findViewById(R.id.MyLifeDateLV);
    	
		mVv = (VideoView)getActivity().findViewById(R.id.MyLifeVV);
    	Context c = this.getActivity().getApplicationContext();
    	Log.d ("asd", "Output file" + MemoirApplication.getConcatenatedOutputFile(c));
    	mVv.setVideoPath(MemoirApplication.getConcatenatedOutputFile(c));
    	mMc = new MediaController(getActivity());
		mVv.setMediaController(mMc);
    	mVv.requestFocus();
    	
    	mVv.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
            	Log.d("asd", "in onPreapraed");
            	
            	LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.MyLifeLL);
            	ll.setLayoutParams(new FrameLayout.LayoutParams(mVv.getWidth(), mVv.getHeight() - 155));
            	mMc.setAnchorView(ll);
            	
            	mp.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
					@Override
					public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {
		            	LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.MyLifeLL);
		            	ll.setLayoutParams(new FrameLayout.LayoutParams(mVv.getWidth(), mVv.getHeight() - 155));
		            	mMc.setAnchorView(ll);
					}
            	});
            }
        });
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.d("asd", "OnStart of my Fragement");
		mVideos = ((MemoirApplication) getActivity().getApplication()).getDBA()
				.getVideos(0, -1, false);
		mDateAdapter = new MyLifeDateListArrayAdapter(getActivity(), mVideos);
		mDateList.setAdapter(mDateAdapter);
		
		Intent intent = new Intent(this.getActivity(), TranscodingService.class);
		//this.getActivity().startService(intent);
	}

	public class MyLifeDateListArrayAdapter extends ArrayAdapter<List<Video>> {

		private Context mContext;
		private List<List<Video>> mList;
		private LayoutInflater mInflater;
		private LinearLayout mLinearLayout;
		private Hashtable<Long, RelativeLayout> mHashtable;

		// private MyLifeVideoListArrayAdapter mVideoAdapter;

		public MyLifeDateListArrayAdapter(Context context,
				List<List<Video>> List) {
			super(context, R.layout.fragment_my_life_list_item);

			this.mContext = context;
			this.mList = List;
			this.mInflater = LayoutInflater.from(context);
			// this.mHashtable = new Hashtable<Long, RelativeLayout>();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d("asd", "in getView for position " + position);
			if (convertView == null) {
				Log.d("asd", "convertView turned out to be null ");
				convertView = mInflater.inflate(
						R.layout.fragment_my_life_list_item, null);
				if (position % 2 == 0) {
					convertView
							.setBackgroundResource(R.drawable.alterselector1);
				} else {
					convertView
							.setBackgroundResource(R.drawable.alterselector2);
				}
			} else {
				Log.d("asd", "ConvertView exists");
				return convertView;
			}

			List<Video> VideoList = this.mList.get(position);
			// NOTE: assuming VideoList can never be null here.
			mLinearLayout = (LinearLayout) convertView
					.findViewById(R.id.MyLifeListItemInnerLL);

			Log.d("asd", "Got the linear layout and it contains "
					+ mLinearLayout.getChildCount());
			TextView tv = (TextView) convertView
					.findViewById(R.id.MyLifeListItemTV);
			Date d = new Date(VideoList.get(0).date);
			String date = String.valueOf(VideoList.get(0).date);
			tv.setText(date.substring(6) + "/" + date.substring(4, 6) + "/" + date.substring(0, 4));

			for (Video v : VideoList) {
				FrameLayout FL = (FrameLayout) mInflater.inflate(
						R.layout.fragment_my_life_video_item, null);
				mLinearLayout.addView(FL);
				Log.d("asd", "path is " + v.path);
				ImageView iv = (ImageView) FL.findViewById(R.id.imageView1);
				Bitmap bmp = BitmapFactory.decodeFile(v.thumbnailPath);
				iv.setImageBitmap(bmp);
				final Uri videopath = Uri.fromFile(new File(v.path));

				iv.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent playIntent = new Intent();
						playIntent.setAction(Intent.ACTION_VIEW);
						playIntent.setDataAndType(videopath, "video/*");
						getActivity().startActivity(playIntent);
					}
				});

				if (v.selected) {
					FL.findViewById(R.id.checkBox1).setActivated(true);
				}

			}

			/*
			 * if(!VideoList.isEmpty()) { Video v1 = VideoList.get(0); Long l =
			 * new Long(v1.date); if(mHashtable.containsKey(l)) { RelativeLayout
			 * RL = (RelativeLayout) mHashtable.get(l);
			 * mLinearLayout.addView(RL, 0); } else { for(Video v : VideoList) {
			 * RelativeLayout RL = (RelativeLayout)
			 * mInflater.inflate(R.layout.fragment_my_life_video_item, null);
			 * mLinearLayout.addView(RL); VideoView videoV = (VideoView)
			 * RL.findViewById(R.id.videoView1); //videoV.setVideoPath(v.path);
			 * Log.d("asd", "path is " + v.path); } } }
			 */
			return convertView;
		}

		@Override
		public int getCount() {
			if (mList == null)
				return 0;
			return mList.size();
		}

		@Override
		public List<Video> getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	/*
	 * public class MyLifeVideoListArrayAdapter extends ArrayAdapter<Video> {
	 * 
	 * private Context mContext; private List<Video> mList; private
	 * LayoutInflater mInflater;
	 * 
	 * public MyLifeVideoListArrayAdapter(Context context, List<Video> List) {
	 * super(context, R.layout.fragment_my_life_video_item);
	 * 
	 * this.mContext = context; this.mList = List; this.mInflater =
	 * LayoutInflater.from(context); }
	 * 
	 * @Override public View getView(int position, View convertView, ViewGroup
	 * parent) {
	 * 
	 * if(convertView == null) { convertView =
	 * mInflater.inflate(R.layout.fragment_my_life_video_item, parent); }
	 * 
	 * Video video = this.mList.get(position); VideoView view = (VideoView)
	 * convertView; view.setVideoPath(video.path);
	 * 
	 * return convertView; }
	 * 
	 * }
	 */

}
