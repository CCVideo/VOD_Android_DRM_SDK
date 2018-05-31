package com.bokecc.sdk.mobile.demo.drm.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.lang.reflect.Method;

public class MediaUtil {

	public static String PCM_FILE_SUFFIX = ".pcm";

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
