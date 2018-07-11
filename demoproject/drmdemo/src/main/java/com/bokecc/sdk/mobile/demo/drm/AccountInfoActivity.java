package com.bokecc.sdk.mobile.demo.drm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.bokecc.sdk.mobile.demo.drm.adapter.AccountViewAdapter;
import com.bokecc.sdk.mobile.demo.drm.util.ConfigUtil;

/**
 * 
 * 账户信息界面
 * 
 * @author CC视频
 *
 */
public class AccountInfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		RelativeLayout accountLayout = new RelativeLayout(this);
		accountLayout.setBackgroundColor(Color.WHITE);
		LayoutParams accountLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		accountLayout.setLayoutParams(accountLayoutParams);
		
		ListView accountListView = new ListView(this);
		accountListView.setPadding(10, 10, 10, 10);
		accountListView.setDivider(getResources().getDrawable(R.drawable.line));
		LayoutParams accountListViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		accountLayout.addView(accountListView, accountListViewParams);
		
		List<Pair<String, String>> pairs = new ArrayList<Pair<String,String>>();
		Pair<String, String> userIdPair = new Pair<String, String>("User ID",ConfigUtil.USERID);
		pairs.add(userIdPair);
		Pair<String, String> apiKeyPair = new Pair<String, String>("API Key", ConfigUtil.API_KEY);
		pairs.add(apiKeyPair);
		
		AccountViewAdapter accountViewAdapter = new AccountViewAdapter(this, pairs);
		accountListView.setAdapter(accountViewAdapter);
		
		setContentView(accountLayout);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

}
