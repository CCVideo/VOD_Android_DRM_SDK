package com.bokecc.sdk.mobile.demo.drm.util;

import android.content.Context;

public class ParamsUtil {
	
	public final static int INVALID = -1;

	public static int getInt(String str){
		int num = INVALID;
		
		try {
			num = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			
		}
		
		return num;
	}
	
	public static long getLong(String str){
		long num = 0l;
		
		try {
			num = Long.parseLong(str);
		} catch (NumberFormatException e) {
		}
		
		return num;
	}
	
	public static String byteToM(long num){
		double m = (double) num / 1024 / 1024;
		return String.format("%.2f", m);
	}
	
	public static String millsecondsToStr(int seconds){
		seconds = seconds / 1000;
		String result = "";
		int hour = 0, min = 0, second = 0;
		hour = seconds / 3600;
		min = (seconds - hour * 3600) / 60;
		second = seconds - hour * 3600 - min * 60;
		if (hour < 10) {
			result += "0" + hour + ":";
		} else {
			result += hour + ":";
		}
		if (min < 10) {
			result += "0" + min + ":";
		} else {
			result += min + ":";
		}
		if (second < 10) {
			result += "0" + second;
		} else {
			result += second;
		}
		return result;
	}
	
	public static int dpToPx(Context context, int height){
		float density = context.getResources().getDisplayMetrics().density;
		height = (int) (height * density + 0.5f);
		return height;
	}
	
}
