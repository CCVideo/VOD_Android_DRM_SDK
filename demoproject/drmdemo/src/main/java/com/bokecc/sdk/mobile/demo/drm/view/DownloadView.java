package com.bokecc.sdk.mobile.demo.drm.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.drm.R;

public class DownloadView extends RelativeLayout{

	private TextView videoView;
	
	private TextView statusInfoView;
	
	private TextView progressTextView;
	
	private ProgressBar progressBar;
	
	
	private final int VIDEOIDVIEW_ID = 3000000;
	private final int STATUSVIEW_ID = 3000001;
	private final int PROGRESSTEXT_ID = 3000002;
	
	public DownloadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DownloadView(Context context, String title, String statusInfo, String progressText, int progress) {
		super(context);
		
		videoView = new TextView(context);
		videoView.setText(title);
		videoView.setTextColor(0xFF000000);
		videoView.setId(VIDEOIDVIEW_ID);
		videoView.setTextSize(18);
		videoView.setSingleLine();
		
		statusInfoView = new TextView(context);
		statusInfoView.setText(statusInfo);
		statusInfoView.setId(STATUSVIEW_ID);
		statusInfoView.setTextColor(0xFF000000);
		
		progressTextView = new TextView(context);
		progressTextView.setTextColor(0xFF000000);
		progressTextView.setId(PROGRESSTEXT_ID);
		progressTextView.setPadding(0, 10, 0, 0);
		progressTextView.setText(progressText);
		progressTextView.setTextColor(Color.GRAY);
		
		progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
		
		progressBar.setMax(100);
		progressBar.setMinimumHeight(10);
		progressBar.setIndeterminate(false);
        progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar));
        progressBar.setPadding(0, 10, 0, 0);
        progressBar.setProgress(progress);
		
		 /**
         *  
         *  title               
         *  下载中
         *  0M / 0M                
         *  ********progress**************
         *  
         * */
		
		LayoutParams videoLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		videoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		addView(videoView, videoLayoutParams);
		
		LayoutParams statusInfoLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		statusInfoLayoutParams.addRule(RelativeLayout.BELOW, VIDEOIDVIEW_ID);
		statusInfoLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, VIDEOIDVIEW_ID);
		addView(statusInfoView, statusInfoLayoutParams);
		
		LayoutParams progressTextLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		progressTextLayoutParams.addRule(RelativeLayout.BELOW, STATUSVIEW_ID);
		progressTextLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, VIDEOIDVIEW_ID);
		addView(progressTextView, progressTextLayoutParams);
		
		LayoutParams progressLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 25);
		progressLayoutParams.addRule(RelativeLayout.BELOW, PROGRESSTEXT_ID);
		progressLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, VIDEOIDVIEW_ID);
		addView(progressBar, progressLayoutParams);
		
		setPadding(5, 5, 5, 5);
	}
	
	public String getTitle(){
		return videoView.getText() + "";
	}
	
	public void setProgress(int progress){
		progressBar.setProgress(progress);
	}
	
	public void setProgressText(String text){
		progressTextView.setText(text);
	}

}
