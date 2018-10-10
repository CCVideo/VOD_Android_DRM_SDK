package com.bokecc.sdk.mobile.demo.drm;

import android.app.Application;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.drm.model.MyObjectBox;
import com.bokecc.sdk.mobile.demo.drm.util.DataSet;
import com.bokecc.sdk.mobile.drm.DRMServer;
import io.objectbox.BoxStore;

public class DemoApplication extends Application{
	
	private DRMServer drmServer;

	private BoxStore boxStore;
	@Override
	public void onCreate() {
		startDRMServer();
		super.onCreate();
		boxStore = MyObjectBox.builder().androidContext(this).build();
		DataSet.init(boxStore);
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

}
