package com.bokecc.sdk.mobile.demo.drm.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bokecc.sdk.mobile.demo.drm.db.DownloadDBHelper;
import com.bokecc.sdk.mobile.demo.drm.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.drm.downloadutil.DownloaderWrapper;
import com.bokecc.sdk.mobile.demo.drm.model.DownloadInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.BoxStore;

public class DataSet {
	private static DownloadDBHelper downloadDBHelper;
	public static void init(BoxStore boxStore){
		downloadDBHelper = new DownloadDBHelper(boxStore);
	}

	public static void saveDownloadData(){
		downloadDBHelper.saveDownloadData();
	}

	public static List<DownloadInfo> getDownloadInfos(){
		return downloadDBHelper.getDownloadInfos();
	}

	public static boolean hasDownloadInfo(String title){
		return downloadDBHelper.hasDownloadInfo(title);
	}

	public static DownloadInfo getDownloadInfo(String title){
		return downloadDBHelper.getDownloadInfo(title);
	}

	public static void addDownloadInfo(DownloadInfo downloadInfo){
		downloadDBHelper.addDownloadInfo(downloadInfo);
	}

	public static void removeDownloadInfo(DownloadInfo downloadInfo){
		downloadDBHelper.removeDownloadInfo(downloadInfo);
	}

	public static void updateDownloadInfo(DownloadInfo downloadInfo){
		downloadDBHelper.updateDownloadInfo(downloadInfo);
	}
}
