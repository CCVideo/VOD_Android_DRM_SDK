package com.bokecc.sdk.mobile.demo.drm.downloadutil;

import android.content.Context;
import android.text.format.Formatter;

import com.bokecc.sdk.mobile.demo.drm.model.DownloadInfo;
import com.bokecc.sdk.mobile.demo.drm.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.drm.util.DataSet;
import com.bokecc.sdk.mobile.demo.drm.util.MediaUtil;
import com.bokecc.sdk.mobile.download.DownloadListener;
import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.play.MediaMode;

import java.io.File;

/**
 * 下载downloader包装类
 */

public class DownloaderWrapper {
    Downloader downloader;
    DownloadInfo downloadInfo;

    long lastStart;

    public DownloaderWrapper(final DownloadInfo downloadInfo, String verificationCode) {
        this.downloadInfo = downloadInfo;

        lastStart = downloadInfo.getStart();

        File file = MediaUtil.createFile(downloadInfo.getTitle());
        downloader = new Downloader(file, downloadInfo.getVideoId(), ConfigUtil.USERID, ConfigUtil.API_KEY,verificationCode);
        //设置下载重连次数 取值范围（0--100）,Demo设置的是重试60次
        downloader.setReconnectLimit(ConfigUtil.DOWNLOAD_RECONNECT_LIMIT);
        //设置下载重连间隔，单位ms，demo设置是3000ms
        downloader.setDownloadRetryPeriod(3 * 1000);
        downloader.setDownloadDefinition(downloadInfo.getDefinition());
        downloader.setHttps(false);

        //下载模式
        downloader.setDownloadMode(MediaUtil.DOWNLOAD_MODE);

        downloader.setDownloadListener(new DownloadListener() {
            @Override
            public void handleProcess(long start, long end, String videoId) {
                downloadInfo.setStart(start).setEnd(end);
            }

            @Override
            public void handleException(DreamwinException exception, int status) {
                downloadInfo.setStatus(status);
            }

            @Override
            public void handleStatus(String videoId, int status) {
                if (status == downloadInfo.getStatus()) {
                	return;
                } else {
                	downloadInfo.setStatus(status);
                	DataSet.updateDownloadInfo(downloadInfo);
                }
            }

            @Override
            public void handleCancel(String videoId) {}
        });

        if (downloadInfo.getStatus() == Downloader.DOWNLOAD) {
            downloader.start();
        }
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    public int getStatus() {
        return downloadInfo.getStatus();
    }

    public String getDownloadProgressText(Context context) {
        String start = Formatter.formatFileSize(context, downloadInfo.getStart());
        String end = Formatter.formatFileSize(context, downloadInfo.getEnd());
        String downloadText = String.format("%s/%s", start, end);
        return downloadText;
    }

    public long getDownloadProgressBarValue() {
        if (downloadInfo.getEnd() == 0) {
            return 0;
        } else {
            return downloadInfo.getStart() * 100 / downloadInfo.getEnd();
        }
    }

    //TODO 待优化
    public String getSpeed(Context context) {
        String speed = Formatter.formatFileSize(context, downloadInfo.getStart() - lastStart) + "/s";
        lastStart = downloadInfo.getStart();
        return speed;
    }

    public void start() {
    	downloadInfo.setStatus(Downloader.DOWNLOAD);
        downloader.start();
    }

    public void resume() {
    	downloadInfo.setStatus(Downloader.DOWNLOAD);
        downloader.resume();
    }

    public void setToWait() {
    	downloadInfo.setStatus(Downloader.WAIT);
        downloader.setToWaitStatus();
    }

    public void pause() {
    	downloadInfo.setStatus(Downloader.PAUSE);
        downloader.pause();
    }

    public void cancel() {
    	downloadInfo.setStatus(Downloader.PAUSE);
        downloader.cancel();
    }
}
