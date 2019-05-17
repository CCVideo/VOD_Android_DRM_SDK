package com.bokecc.sdk.mobile.demo.drm.download;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;


import com.bokecc.sdk.mobile.demo.drm.R;
import com.bokecc.sdk.mobile.demo.drm.adapter.DownloadedViewAdapter;
import com.bokecc.sdk.mobile.demo.drm.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.drm.downloadutil.DownloaderWrapper;
import com.bokecc.sdk.mobile.demo.drm.play.MediaPlayActivity;
import com.bokecc.sdk.mobile.demo.drm.play.SpeedIjkMediaPlayActivity;
import com.bokecc.sdk.mobile.demo.drm.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.drm.util.MediaUtil;

import java.io.File;
import java.util.List;

/**
 * 已下载标签页
 * 
 * @author CC视频
 *
 */
public class DownloadedFragment extends Fragment implements DownloadController.Observer {

	private ListView downloadedListView;

	private List<DownloaderWrapper> downloadedInfos = DownloadController.downloadedList;
	
	private Context context;

	private DownloadedViewAdapter videoListViewAdapter;
	
	private FragmentActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity = getActivity();
		context = activity.getApplicationContext();
		RelativeLayout downloadLayout = new RelativeLayout(activity.getApplicationContext());
		downloadLayout.setBackgroundColor(Color.WHITE);
		
		downloadedListView = new ListView(context);
		downloadedListView.setPadding(10, 10, 10, 10);
		downloadedListView.setDivider(getResources().getDrawable(R.drawable.line));
		LayoutParams downloadedLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		downloadLayout.addView(downloadedListView, downloadedLayoutParams);

		initData();

		downloadedListView.setOnItemClickListener(onItemClickListener);
		downloadedListView.setOnCreateContextMenuListener(onCreateContextMenuListener);

		return downloadLayout;
	}
	
	private void initData(){
		videoListViewAdapter = new DownloadedViewAdapter(context, downloadedInfos);
		downloadedListView.setAdapter(videoListViewAdapter);
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@SuppressWarnings("unchecked")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DownloaderWrapper wrapper = (DownloaderWrapper) parent.getItemAtPosition(position);
			Intent intent = new Intent(context, MediaPlayActivity.class);
//			Intent intent = new Intent(context, SpeedIjkMediaPlayActivity.class);
			intent.putExtra("videoId", wrapper.getDownloadInfo().getTitle());
			intent.putExtra("isLocalPlay", true);
			intent.putExtra("playMode", MediaUtil.PLAY_MODE.getMode());
			Log.i("dwdemo", "本地播放模式：" + MediaUtil.PLAY_MODE.getMode());
			startActivity(intent);
		}
	};

	OnCreateContextMenuListener onCreateContextMenuListener = new OnCreateContextMenuListener() {
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			menu.setHeaderTitle("操作");
			menu.add(ConfigUtil.DOWNLOADED_MENU_GROUP_ID, 0, 0, "删除");
		}
	};
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != ConfigUtil.DOWNLOADED_MENU_GROUP_ID) {
			return false;
		}
		
		int selectedPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;

		DownloaderWrapper wrapper = (DownloaderWrapper)videoListViewAdapter.getItem(selectedPosition);
		DownloadController.deleteDownloadedInfo(selectedPosition);

		File file = new File(Environment.getExternalStorageDirectory()+"/"+ConfigUtil.DOWNLOAD_DIR, wrapper.getDownloadInfo().getTitle()+".pcm");
		if(file.exists()){
			file.delete();
		}
		
		updateView();
	
		if (getUserVisibleHint()) {
			return true;
		}

		return false;
	}

	@Override
	public void update() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateView();
			}
		});
	}
	
	private void updateView() {
		videoListViewAdapter.notifyDataSetChanged();
		downloadedListView.invalidate();
	}
	
	@Override
	public void onPause() {
		DownloadController.detach(this);
		super.onPause();
	}

	@Override
	public void onResume() {
		DownloadController.attach(this);
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
				
}