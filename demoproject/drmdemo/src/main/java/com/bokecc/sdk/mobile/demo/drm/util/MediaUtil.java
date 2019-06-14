package com.bokecc.sdk.mobile.demo.drm.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.bokecc.sdk.mobile.play.MediaMode;

import java.io.File;
import java.lang.reflect.Method;

public class MediaUtil {

	public static String PCM_FILE_SUFFIX = ".pcm";
	public static final String SP_DOWNLOAD_KEY = "account_download_mode";
	public static MediaMode DOWNLOAD_MODE = MediaMode.VIDEO;
	public static MediaMode PLAY_MODE = MediaMode.VIDEOAUDIO;
	public static final String SP_PLAY_KEY = "account_play_mode";

	public static String DOWNLOAD_FILE_SUFFIX = ".mp4";
	public static final String MP4_SUFFIX = ".mp4";
	public static final String M4A_SUFFIX = ".m4a";

	/**
	 * 截取视频第一帧
	 * 
	 * @param context
	 * @param uri
	 * @return
	 */
	public static Bitmap getVideoFirstFrame(Context context, Uri uri) {
		Bitmap bitmap = null;
		String className = "android.media.MediaMetadataRetriever";
		Object objectMediaMetadataRetriever = null;
		Method release = null;
		try {
			objectMediaMetadataRetriever = Class.forName(className).newInstance();
			Method setDataSourceMethod = Class.forName(className).getMethod("setDataSource", Context.class, Uri.class);
			setDataSourceMethod.invoke(objectMediaMetadataRetriever, context, uri);
			Method getFrameAtTimeMethod = Class.forName(className).getMethod("getFrameAtTime");
			bitmap = (Bitmap) getFrameAtTimeMethod.invoke(objectMediaMetadataRetriever);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (release != null) {
					release.invoke(objectMediaMetadataRetriever);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	public static File createFile(String title) {
		File file = null;
		// 判断sd卡是否存在
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File sdDir = Environment.getExternalStorageDirectory();// 获取根目录
			File dir = new File(sdDir + "/" + ConfigUtil.DOWNLOAD_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			String path = dir + "/" + title + ".pcm";
			file = new File(path);
		}

		return file;
	}



}
