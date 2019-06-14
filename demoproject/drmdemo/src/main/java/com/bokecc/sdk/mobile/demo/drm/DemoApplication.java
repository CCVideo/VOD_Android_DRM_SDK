package com.bokecc.sdk.mobile.demo.drm;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.drm.model.MyObjectBox;
import com.bokecc.sdk.mobile.demo.drm.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.drm.util.DataSet;
import com.bokecc.sdk.mobile.drm.DRMServer;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;

import io.objectbox.BoxStore;

public class DemoApplication extends Application{
	
	private DRMServer drmServer;

	private BoxStore boxStore;

	SharedPreferences accountSp;
	final String avPlayDownloadFileName = "account_sp";
	NotificationManager mNotificationManager;
	public static Context context;

	@Override
	public void onCreate() {
		startDRMServer();
		super.onCreate();
		context = this;
		accountSp = getSharedPreferences(avPlayDownloadFileName, Context.MODE_PRIVATE);

		boxStore = MyObjectBox.builder().androidContext(this).build();
		DataSet.init(boxStore);
		initBroadcaseReceiver();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	public BoxStore getBoxStore() {
		return boxStore;
	}

	// 启动DRMServer
	public void startDRMServer() {
		if (drmServer == null) {
			drmServer = new DRMServer();
			drmServer.setRequestRetryCount(20);
		}
		
		try {
			drmServer.start();
			setDrmServerPort(drmServer.getPort());
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "启动解密服务失败，请检查网络限制情况", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onTerminate() {
		if (drmServer != null) {
			drmServer.stop();
		}
		super.onTerminate();
	}

	private int drmServerPort;

	public int getDrmServerPort() {
		return drmServerPort;
	}

	public void setDrmServerPort(int drmServerPort) {
		this.drmServerPort = drmServerPort;
	}
	
	public DRMServer getDRMServer() {
		return drmServer;
	}

	public SharedPreferences getAccountSp() {
		return accountSp;
	}

	public static Context getContext() {
		return context;
	}

	public DWMediaPlayer dwPlayer;
	public DWMediaPlayer getDWPlayer() {
		if (dwPlayer == null) {
			dwPlayer = new DWMediaPlayer();
		}
		return dwPlayer;
	}

	public void releaseDWPlayer() {
		if (dwPlayer != null) {
			dwPlayer.reset();
			dwPlayer.release();
			dwPlayer = null;
		}
	}
	private MyBroadcastRecevier myBroadcastRecevier = new MyBroadcastRecevier();
	private void initBroadcaseReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConfigUtil.ACTION_BUTTON);
		registerReceiver(myBroadcastRecevier, filter);
	}

	public NotificationManager getmNotificationManager() {
		return mNotificationManager;
	}

	public void removeNotification() {
		mNotificationManager.cancel(ConfigUtil.BROADCASE_ID);
	}

	RemoteViews remoteViews;
	Notification notification;
	public void showNotification(boolean isLocalPlay, Class<?> cls, String videoId) {
		remoteViews = new RemoteViews(getPackageName(), R.layout.audio_notification_layout);

		remoteViews.setTextViewText(R.id.audio_notify_videoid, videoId);

		if (dwPlayer.isPlaying()) {
			remoteViews.setImageViewResource(R.id.audio_notify_play_pause, R.drawable.audio_pause_icon);
		} else {
			remoteViews.setImageViewResource(R.id.audio_notify_play_pause, R.drawable.audio_play_icon);
		}

		Intent intent = new Intent(ConfigUtil.ACTION_BUTTON);

		intent.putExtra(ConfigUtil.INTENT_BUTTONID_TAG, ConfigUtil.INTENT_BUTTON_CLOSE);
		PendingIntent pendingIntentClose = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.audio_notify_close, pendingIntentClose);

		intent.putExtra(ConfigUtil.INTENT_BUTTONID_TAG, ConfigUtil.INTENT_BUTTON_PLAY);
		PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.audio_notify_play_pause, pendingIntentPlay);

		intent.putExtra(ConfigUtil.INTENT_BUTTONID_TAG, ConfigUtil.INTENT_BUTTON_BACK_15s);
		PendingIntent pendingIntentBack15s = PendingIntent.getBroadcast(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.audio_notify_back_15s_view, pendingIntentBack15s);

		intent.putExtra(ConfigUtil.INTENT_BUTTONID_TAG, ConfigUtil.INTENT_BUTTON_FORWARD_15s);
		PendingIntent pendingIntentForward = PendingIntent.getBroadcast(this, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.audio_notify_forward_15s_view, pendingIntentForward);

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

		Intent aIntent = new Intent(this, cls);

		aIntent.putExtra("isLocalPlay", isLocalPlay);
		aIntent.putExtra("isFromNotify", true);
		aIntent.putExtra("videoId", videoId);
		aIntent.putExtra("playMode", 0);

		PendingIntent pendingIntent= PendingIntent.getActivity(this, 100, aIntent , PendingIntent.FLAG_CANCEL_CURRENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			NotificationChannel notificationChannel = new NotificationChannel(ConfigUtil.CHANEL_ID, ConfigUtil.CHANEL_NAME, NotificationManager.IMPORTANCE_LOW);
			mNotificationManager.createNotificationChannel(notificationChannel);
			notification = new Notification.Builder(context)
					.setChannelId(ConfigUtil.CHANEL_ID)
					.setContent(remoteViews)
					.setContentTitle("音频")
					.setContentIntent(pendingIntent)
					.setTicker("音频播放中……")
					.setWhen(System.currentTimeMillis())
					.setPriority(Notification.PRIORITY_DEFAULT)
					.setOngoing(true)
					.setDefaults(Notification.DEFAULT_VIBRATE)
					.setSmallIcon(R.drawable.ic_launcher).build();
		}else {
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
					.setContent(remoteViews)
					.setContentTitle("音频")
					.setContentIntent(pendingIntent)
					.setTicker("音频播放中……")
					.setWhen(System.currentTimeMillis())
					.setPriority(Notification.PRIORITY_DEFAULT)
					.setOngoing(true)
					.setDefaults(Notification.DEFAULT_VIBRATE)
					.setSmallIcon(R.drawable.ic_launcher);
			notification = mBuilder.build();

		}
		mNotificationManager.notify(ConfigUtil.BROADCASE_ID, notification);

	}

	String TAG = "DemoApplication";
	class MyBroadcastRecevier extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String btnStr = intent.getStringExtra(ConfigUtil.INTENT_BUTTONID_TAG);
			Log.e(TAG, intent.getAction() + " " + btnStr);

			if (ConfigUtil.INTENT_BUTTON_CLOSE.equals(btnStr)) {
				if (dwPlayer.isPlaying()) {
					dwPlayer.pause();
					dwPlayer.stop();
				}
				removeNotification();
			} else if (ConfigUtil.INTENT_BUTTON_BACK_15s.equals(btnStr)) {
				if (dwPlayer.isPlaying()) {
					if (dwPlayer.getCurrentPosition() > 15 * 1000) {
						dwPlayer.seekTo(dwPlayer.getCurrentPosition() - 15 * 1000);
					} else {
						dwPlayer.seekTo(0);
					}
				}

			} else if (ConfigUtil.INTENT_BUTTON_FORWARD_15s.equals(btnStr)) {

				if (dwPlayer.isPlaying()) {
					if ((dwPlayer.getDuration() - dwPlayer.getCurrentPosition()) > 15 * 1000) {
						dwPlayer.seekTo(dwPlayer.getCurrentPosition() + 15 * 1000);
					} else {
						dwPlayer.seekTo(dwPlayer.getDuration());
					}
				}

			} else if (ConfigUtil.INTENT_BUTTON_PLAY.equals(btnStr)) {
				if (dwPlayer.isPlaying()) {
					remoteViews.setImageViewResource(R.id.audio_notify_play_pause, R.drawable.audio_play_icon);
					dwPlayer.pause();
				} else {
					remoteViews.setImageViewResource(R.id.audio_notify_play_pause, R.drawable.audio_pause_icon);
					dwPlayer.start();
				}

				mNotificationManager.notify(ConfigUtil.BROADCASE_ID, notification);
			}
		}
	}
}
