package com.bokecc.sdk.mobile.demo.drm.play;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.drm.R;
import com.bokecc.sdk.mobile.demo.drm.DemoApplication;
import com.bokecc.sdk.mobile.demo.drm.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.drm.util.MediaUtil;
import com.bokecc.sdk.mobile.demo.drm.util.ParamsUtil;
import com.bokecc.sdk.mobile.demo.drm.view.PopMenu;
import com.bokecc.sdk.mobile.demo.drm.view.PopMenu.OnItemClickListener;
import com.bokecc.sdk.mobile.demo.drm.view.VerticalSeekBar;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.play.DWIjkMediaPlayer;
import com.bokecc.sdk.mobile.play.OnDreamWinErrorListener;

/**
 * 视频播放界面
 *
 * @author CC视频
 */
public class SpeedIjkMediaPlayActivity extends Activity implements
        OnBufferingUpdateListener, DWIjkMediaPlayer.OnPreparedListener, DWIjkMediaPlayer.OnInfoListener,
        DWIjkMediaPlayer.OnVideoSizeChangedListener, DWIjkMediaPlayer.OnErrorListener,
        SurfaceHolder.Callback, OnDreamWinErrorListener {

    private DWIjkMediaPlayer player;
    private Subtitle subtitle;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressBar bufferProgressBar;
    private SeekBar skbProgress;
    private ImageView playOp, backPlayList;
    private TextView videoIdText, playDuration, videoDuration;
    private Button screenSizeBtn, definitionBtn, subtitleBtn, speedPlayBtn;
    private PopMenu screenSizeMenu, definitionMenu, subtitleMenu, speedPlayMenu;
    private LinearLayout playerTopLayout, volumeLayout;
    private RelativeLayout playerBottomLayout;
    private AudioManager audioManager;
    private VerticalSeekBar volumeSeekBar;
    private int currentVolume;
    private int maxVolume;

    private boolean isLocalPlay;
    private boolean isPrepared;
    private Map<String, Integer> definitionMap;

    private Handler playerHandler;
    private Timer timer = new Timer();
    private TimerTask timerTask;


    private int currentScreenSizeFlag = 1;
    private int currentSpeedFlag = 1;
    private int currrentSubtitleSwitchFlag = 0;
    private int currentDefinition = 0;

    //标志经过onPause生命周期时，视频当前的播放状态
    private Boolean isPlaying;
    //当player未准备好，并且当前activity经过onPause()生命周期时，此值为true
    private boolean isFreeze = false;
    private boolean isSurfaceDestroy = false;
    int currentPosition;
    String path;

    private String[] definitionArray;
    private final String[] screenSizeArray = new String[]{"满屏", "100%", "75%", "50%"};
    private final String[] subtitleSwitchArray = new String[]{"开启", "关闭"};
    private final String subtitleExampleURL = "http://dev.bokecc.com/static/font/example.utf8.srt";
    private final String[] speedArray = new String[]{"0.5", "1.0", "1.5", "2.0"};

    private DemoApplication demoApplication;

    boolean definitionChangeFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        demoApplication = (DemoApplication) getApplication();
        // 隐藏标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.media_play);

        initView();

        initPlayHander();

        initPlayInfo();

        initScreenSizeMenu();

        initSpeedPlayMenu();

        super.onCreate(savedInstanceState);
    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.playerSurfaceView);
        playOp = (ImageView) findViewById(R.id.btnPlay);
        backPlayList = (ImageView) findViewById(R.id.backPlayList);
        bufferProgressBar = (ProgressBar) findViewById(R.id.bufferProgressBar);

        videoIdText = (TextView) findViewById(R.id.videoIdText);
        playDuration = (TextView) findViewById(R.id.playDuration);
        videoDuration = (TextView) findViewById(R.id.videoDuration);
        playDuration.setText(ParamsUtil.millsecondsToStr(0));
        videoDuration.setText(ParamsUtil.millsecondsToStr(0));

        screenSizeBtn = (Button) findViewById(R.id.playScreenSizeBtn);
        definitionBtn = (Button) findViewById(R.id.definitionBtn);
        subtitleBtn = (Button) findViewById(R.id.subtitleBtn);
        speedPlayBtn = (Button) findViewById(R.id.speedPlayBtn);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeSeekBar = (VerticalSeekBar) findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setThumbOffset(2);

        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);
        volumeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        skbProgress = (SeekBar) findViewById(R.id.skbProgress);
        skbProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);

        playerTopLayout = (LinearLayout) findViewById(R.id.playerTopLayout);
        volumeLayout = (LinearLayout) findViewById(R.id.volumeLayout);
        playerBottomLayout = (RelativeLayout) findViewById(R.id.playerBottomLayout);

        playOp.setOnClickListener(onClickListener);
        backPlayList.setOnClickListener(onClickListener);
        screenSizeBtn.setOnClickListener(onClickListener);
        definitionBtn.setOnClickListener(onClickListener);
        subtitleBtn.setOnClickListener(onClickListener);
        speedPlayBtn.setOnClickListener(onClickListener);


        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
        surfaceHolder.addCallback(this);
        surfaceView.setOnTouchListener(touchListener);

    }

    private void initPlayHander() {
        playerHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (player == null) {
                    return;
                }

                if (!isPrepared) {
                    return;
                }

                // 更新播放进度
                int position = (int) player.getCurrentPosition();
                int duration = (int) player.getDuration();
                //更新播放进度
                currentPosition = position;

                if (duration > 0) {
                    long pos = skbProgress.getMax() * position / duration;
                    playDuration.setText(ParamsUtil.millsecondsToStr((int) player.getCurrentPosition()));
                    skbProgress.setProgress((int) pos);
                }


            }

            ;
        };

        // 通过定时器和Handler来更新进度
        timerTask = new TimerTask() {
            @Override
            public void run() {

                if (!isPrepared) {
                    return;
                }

                playerHandler.sendEmptyMessage(0);
            }
        };

    }

    private void initPlayInfo() {
        timer.schedule(timerTask, 0, 1000);
        isPrepared = false;
        player = new DWIjkMediaPlayer();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        player.reset();

        //设置CustomId
        player.setCustomId("HIHA2019");
        player.setOnNativeInvokeListener(new IjkMediaPlayer.OnNativeInvokeListener() {
            @Override
            public boolean onNativeInvoke(int i, Bundle bundle) {
                return false;
            }
        });
        Intent intent = getIntent();
        String videoId = intent.getStringExtra("videoId");
        String verificationCode = getIntent().getStringExtra("verifyCode");
        videoIdText.setText(videoId);

        isLocalPlay = intent.getBooleanExtra("isLocalPlay", false);

        // DRM加密播放
        player.setDRMServerPort(demoApplication.getDrmServerPort());

        try {

            if (!isLocalPlay) {// 播放线上视频

                player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, verificationCode, this);

            } else {// 播放本地已下载视频
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

                    path = Environment.getExternalStorageDirectory() + "/".
                            concat(ConfigUtil.DOWNLOAD_DIR).concat("/").concat(videoId).concat(MediaUtil.PCM_FILE_SUFFIX);

                    if (!new File(path).exists()) {
                        finish();
                    }
                }
            }

        } catch (IllegalArgumentException e) {
            Log.e("player error", e.getMessage());
        } catch (SecurityException e) {
            Log.e("player error", e.getMessage());
        } catch (IllegalStateException e) {
            Log.e("player error", "illegal", e);
        }
    }

    private void initScreenSizeMenu() {
        screenSizeMenu = new PopMenu(this, R.drawable.popdown, currentScreenSizeFlag, 400);
        screenSizeMenu.addItems(screenSizeArray);
        screenSizeMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                // 提示已选择的屏幕尺寸
                Toast.makeText(getApplicationContext(), screenSizeArray[position], Toast.LENGTH_SHORT).show();

                LayoutParams params = getScreenSizeParams(position);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                surfaceView.setLayoutParams(params);
            }
        });

    }

    private void initSpeedPlayMenu() {
        speedPlayBtn.setVisibility(View.VISIBLE);
        speedPlayMenu = new PopMenu(this, R.drawable.popup, currentSpeedFlag, 400);
        speedPlayMenu.addItems(speedArray);
        speedPlayMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                currentSpeedFlag = position;
                Toast.makeText(getApplicationContext(), speedArray[position] + "倍速播放", Toast.LENGTH_SHORT).show();
                player.setSpeed(Float.parseFloat(speedArray[position]));
            }
        });

    }

    @SuppressWarnings("deprecation")
    private LayoutParams getScreenSizeParams(int position) {
        currentScreenSizeFlag = position;
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        int vWidth = player.getVideoWidth();
        int vHeight = player.getVideoHeight();

        if (vWidth > width || vHeight > height) {
            float wRatio = (float) vWidth / (float) width;
            float hRatio = (float) vHeight / (float) height;
            float ratio = Math.max(wRatio, hRatio);

            width = (int) Math.ceil((float) vWidth / ratio);
            height = (int) Math.ceil((float) vHeight / ratio);
        } else {
            float wRatio = (float) width / (float) vWidth;
            float hRatio = (float) height / (float) vHeight;
            float ratio = Math.min(wRatio, hRatio);

            width = (int) Math.ceil((float) vWidth * ratio);
            height = (int) Math.ceil((float) vHeight * ratio);
        }

        String screenSizeStr = screenSizeArray[position];
        if (screenSizeStr.indexOf("%") > 0) {// 按比例缩放
            int screenSize = ParamsUtil.getInt(screenSizeStr.substring(0, screenSizeStr.indexOf("%")));
            width = (width * screenSize) / 100;
            height = (height * screenSize) / 100;

        } else { // 拉伸至全屏
            width = wm.getDefaultDisplay().getWidth();
            height = wm.getDefaultDisplay().getHeight();
        }

        LayoutParams params = new LayoutParams(width, height);
        return params;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            player.setDisplay(surfaceHolder);

            player.setOnDreamWinErrorListener(this);
            player.setOnInfoListener(this);
            player.setOnBufferingUpdateListener(this);
            player.setOnPreparedListener(this);
            player.setOnErrorListener(this);

            surfaceHolder.setFixedSize(1200, 900);
            if (isLocalPlay) {
                player.setOfflineVideoPath(path, this);
            }
            demoApplication.getDRMServer().reset();
            player.prepareAsync();

        } catch (Exception e) {
            Log.e("videoPlayer", "error", e);
        }
        Log.i("videoPlayer", "surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		holder.setFixedSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player == null) {
            return;
        }
        if (isPrepared) {
            currentPosition = (int) player.getCurrentPosition();
        }

        isPrepared = false;
        isSurfaceDestroy = true;

        player.stop();
        player.reset();

    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        isPrepared = true;

        if (!isFreeze) {
            player.start();
            player.setScreenOnWhilePlaying(true);
            player.setSpeed(Float.parseFloat(speedArray[currentSpeedFlag]));
        }

        if (isPlaying != null && !isPlaying.booleanValue()) {
            player.pause();
        }

        if (currentPosition > 0) {
            player.seekTo(currentPosition);
        }

        definitionMap = player.getDefinitions();
        if (!isLocalPlay) {
            initDefinitionPopMenu();
        }

        bufferProgressBar.setVisibility(View.GONE);

        surfaceHolder.setFixedSize(player.getVideoWidth(), player.getVideoHeight());
        LayoutParams params = getScreenSizeParams(currentScreenSizeFlag);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(params);
        videoDuration.setText(ParamsUtil.millsecondsToStr((int) player.getDuration()));
    }

    private void initDefinitionPopMenu() {
        definitionBtn.setVisibility(View.VISIBLE);
        definitionMenu = new PopMenu(this, R.drawable.popup, currentDefinition, 400);
        // 设置清晰度列表
        definitionArray = new String[]{};
        definitionArray = definitionMap.keySet().toArray(definitionArray);

        definitionMenu.addItems(definitionArray);
        definitionMenu.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                try {

                    if (isPrepared) {
                        currentPosition = (int) player.getCurrentPosition();
                        if (player.isPlaying()) {
                            isPlaying = true;
                        } else {
                            isPlaying = false;
                        }
                    }

                    definitionChangeFlag = true;
                    setLayoutVisibility(View.GONE, false);
                    bufferProgressBar.setVisibility(View.VISIBLE);

                    currentDefinition = position;
                    int definitionCode = definitionMap.get(definitionArray[position]);

                    player.setDefaultDefinition(definitionCode);

                    surfaceView.setVisibility(View.INVISIBLE);
                    surfaceView.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    Log.e("player error", e.getMessage() + "");
                }

            }
        });
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        skbProgress.setSecondaryProgress(percent);
    }

    OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnPlay:
                    if (!isPrepared) {
                        return;
                    }

                    if (player.isPlaying()) {
                        player.pause();
                        playOp.setImageResource(R.drawable.smallbegin_ic);

                    } else {
                        player.start();
                        playOp.setImageResource(R.drawable.smallstop_ic);
                    }

                    break;

                case R.id.backPlayList:
                    finish();
                    break;
                case R.id.playScreenSizeBtn:
                    screenSizeMenu.showAsDropDown(v);
                    break;
                case R.id.subtitleBtn:
                    subtitleMenu.showAsDropDown(v);
                    break;
                case R.id.definitionBtn:
                    definitionMenu.showAsDropDown(v);
                    break;
                case R.id.speedPlayBtn:
                    speedPlayMenu.showAsDropDown(v);
                    break;
            }
        }
    };

    OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {
        int progress = 0;

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            player.seekTo(progress);
            currentPosition = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            this.progress = progress * (int) player.getDuration() / skbProgress.getMax();
        }
    };

    VerticalSeekBar.OnSeekBarChangeListener seekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            currentVolume = progress;
            volumeSeekBar.setProgress(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    //控制播放器面板显示
    private boolean isDisplay = false;
    private OnTouchListener touchListener = new OnTouchListener() {

        public boolean onTouch(View v, MotionEvent event) {
            if (!isPrepared) {
                return false;
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isDisplay) {
                    setLayoutVisibility(View.GONE, false);
                } else {
                    setLayoutVisibility(View.VISIBLE, true);
                }
            }
            return false;
        }
    };

    private void initSubtitleSwitchpMenu(Subtitle subtitle) {
        this.subtitle = subtitle;
        subtitleBtn.setVisibility(View.VISIBLE);
        subtitleMenu = new PopMenu(this, R.drawable.popup, currrentSubtitleSwitchFlag, 400);
        subtitleMenu.addItems(subtitleSwitchArray);
        subtitleMenu.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                switch (position) {
                    case 0:// 开启字幕
                        currentScreenSizeFlag = 0;
                        break;
                    case 1:// 关闭字幕
                        currentScreenSizeFlag = 1;
                        break;
                }
            }
        });
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 监测音量变化
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {


            int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume != volume) {
                currentVolume = volume;
                volumeSeekBar.setProgress(currentVolume);
            }

            if (isPrepared) {
                setLayoutVisibility(View.VISIBLE, true);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void setLayoutVisibility(int visibility, boolean isDisplay) {
        if (player == null) {
            return;
        }

        if (player.getDuration() <= 0) {
            return;
        }

        this.isDisplay = isDisplay;
        playerTopLayout.setVisibility(visibility);
        playerBottomLayout.setVisibility(visibility);
        volumeLayout.setVisibility(visibility);
    }

    @Override
    public void onResume() {
        if (isFreeze) {
            isFreeze = false;
            if (isPrepared) {
                player.start();
            }
        } else {
            if (isPlaying != null && isPlaying.booleanValue() && isPrepared) {
                player.start();
            }
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (isPrepared) {
            //如果播放器prepare完成，则对播放器进行暂停操作，并记录状态
            if (player.isPlaying()) {
                isPlaying = true;
            } else {
                isPlaying = false;
            }
            player.pause();
        } else {
            //如果播放器没有prepare完成，则设置isFreeze为true
            isFreeze = true;
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        timerTask.cancel();
        if (player != null) {
            player.reset();
            player.release();
            player = null;
        }
        demoApplication.getDRMServer().disconnectCurrentStream();
        super.onDestroy();
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int i, int j) {
        surfaceHolder.setFixedSize(width, height);
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        //这两行代码实现出错时续播功能
        player.seekTo(currentPosition);
        player.start();

        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        switch (what) {
            case DWIjkMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (player.isPlaying()) {
                    bufferProgressBar.setVisibility(View.VISIBLE);
                }
                break;
            case DWIjkMediaPlayer.MEDIA_INFO_BUFFERING_END:
                bufferProgressBar.setVisibility(View.GONE);
                break;
        }
        return false;
    }

    @Override
    public void onPlayError(final DreamwinException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
