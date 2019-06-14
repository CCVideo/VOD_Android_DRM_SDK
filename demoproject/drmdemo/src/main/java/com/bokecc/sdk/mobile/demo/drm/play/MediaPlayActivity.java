package com.bokecc.sdk.mobile.demo.drm.play;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.drm.DemoApplication;
import com.bokecc.sdk.mobile.demo.drm.R;
import com.bokecc.sdk.mobile.demo.drm.db.VideoPositionDBHelper;
import com.bokecc.sdk.mobile.demo.drm.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.drm.model.VideoPosition;
import com.bokecc.sdk.mobile.demo.drm.model.VisitorInfo;
import com.bokecc.sdk.mobile.demo.drm.play.Subtitle.OnSubtitleInitedListener;
import com.bokecc.sdk.mobile.demo.drm.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.drm.util.DataSet;
import com.bokecc.sdk.mobile.demo.drm.util.MediaUtil;
import com.bokecc.sdk.mobile.demo.drm.util.ParamsUtil;
import com.bokecc.sdk.mobile.demo.drm.view.CheckNetwork;
import com.bokecc.sdk.mobile.demo.drm.view.CommitOrJumpVisitorInfo;
import com.bokecc.sdk.mobile.demo.drm.view.CustomDialogCheckNetwork;
import com.bokecc.sdk.mobile.demo.drm.view.LandscapeVisitorInfoDialog;
import com.bokecc.sdk.mobile.demo.drm.view.OnSubtitleRadioButton;
import com.bokecc.sdk.mobile.demo.drm.view.PlayChangeVideoPopupWindow;
import com.bokecc.sdk.mobile.demo.drm.view.PlayTopPopupWindow;
import com.bokecc.sdk.mobile.demo.drm.view.PopMenu;
import com.bokecc.sdk.mobile.demo.drm.view.PopMenu.OnItemClickListener;
import com.bokecc.sdk.mobile.demo.drm.view.PortraitVisitorInfoDialog;
import com.bokecc.sdk.mobile.demo.drm.view.VerticalSeekBar;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import com.bokecc.sdk.mobile.play.MediaMode;
import com.bokecc.sdk.mobile.play.OnAuthMsgListener;
import com.bokecc.sdk.mobile.play.OnDreamWinErrorListener;
import com.bokecc.sdk.mobile.play.OnPlayModeListener;
import com.bokecc.sdk.mobile.play.OnSubtitleMsgListener;
import com.bokecc.sdk.mobile.play.OnVisitMsgListener;
import com.bokecc.sdk.mobile.play.PlayInfo;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * 视频播放界面
 *
 * @author CC视频
 */
public class MediaPlayActivity extends Activity implements
        DWMediaPlayer.OnBufferingUpdateListener,
        DWMediaPlayer.OnInfoListener,
        DWMediaPlayer.OnPreparedListener, DWMediaPlayer.OnErrorListener,
        MediaPlayer.OnVideoSizeChangedListener, SurfaceHolder.Callback, SensorEventListener, OnCompletionListener, OnDreamWinErrorListener {

    private boolean networkConnected = true;
    private DWMediaPlayer player;
    private Subtitle subtitle, subtitle2;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressBar bufferProgressBar;
    private SeekBar skbProgress;
    private ImageView backPlayList;
    private TextView videoIdText, playDuration, videoDuration;
    private TextView tvDefinition;
    private PopMenu definitionMenu;
    private LinearLayout playerTopLayout, volumeLayout;
    private LinearLayout playerBottomLayout;
    private AudioManager audioManager;
    private VerticalSeekBar volumeSeekBar;
    private int currentVolume;
    private int maxVolume;
    private TextView subtitleText, subtitleText2, tv_watch_tip, tv_pre_watch_over;
    private LinearLayout ll_rewatch, ll_pre_watch_over;

    private boolean isLocalPlay;
    private boolean isPrepared;

    private Map<String, Integer> definitionMap;

    private Handler playerHandler;
    private Timer timer = new Timer();
    private TimerTask timerTask, networkInfoTimerTask;

    private int currentScreenSizeFlag = 1;
    private int currrentSubtitleSwitchFlag = 0;
    private int currentDefinitionIndex = 0;

    //鉴权
    private int isAllowPlayWholeVideo = 2;
    private int freeWatchTime = 0;
    private String freeWatchOverMsg = "";

    //字幕类型
    private int defSubtitle = 3;
    //字幕名称
    private String firstSubName = "", secondSubName = "";
    private int firstBottom, secondBottom, commonBottom;
    private boolean isTwoSubtitle = false;

    // 默认设置为普清
    private int defaultDefinition = DWMediaPlayer.NORMAL_DEFINITION;

    private boolean firstInitDefinition = true;
    private String path;

    private Boolean isPlaying;
    // 当player未准备好，并且当前activity经过onPause()生命周期时，此值为true
    private boolean isFreeze = false;
    private boolean isSurfaceDestroy = false;

    int currentPosition;
    private Dialog dialog;

    private String[] definitionArray;
    private final String[] screenSizeArray = new String[]{"满屏", "100%", "75%", "50%"};
    private final String[] subtitleSwitchArray = new String[]{"开启", "关闭"};
    private final String subtitleExampleURL = "http://dev.bokecc.com/static/font/example.utf8.srt";

    private DemoApplication demoApplication;

    private GestureDetector detector;
    private float scrollTotalDistance;
    private int currentPlayPosition, errorPosition;
    private VideoPosition lastVideoPosition;
    //Demo的verificationCode是从上个页面传值过来，实际使用时根据具体业务逻辑调整
    private String videoId, verificationCode;
    private RelativeLayout rlBelow, rlPlay;
    private WindowManager wm;
    private ImageView ivFullscreen;
    private boolean isFirstBuffer = true;
    //视频信息
    private PlayInfo videoPlayInfo;
    //解决m3u8
    private boolean isError = false, ism3u8Video = false, isShowErrorToast = true;
    // 隐藏界面的线程
    private Runnable hidePlayRunnable = new Runnable() {
        @Override
        public void run() {
            setLayoutVisibility(View.GONE, false);
        }
    };

    VideoPositionDBHelper videoPositionDBHelper;
    private boolean isBackupPlay = false;
    //是否播放音频
    private boolean isAudio = false;
    private MediaMode currentPlayMode = null;
    //音视频切换
    private boolean isSwitchAv = false;

    //========audio layout=====================
    LinearLayout avChangeLayout;
    TextView changeToVideoPlayView;
    TextView changeToAudioPlayView;

    LinearLayout audioLayout;
    TextView audioSpeedView, audioCurrentTimeView, audioDurationView;
    SeekBar audioSeekBar;
    ImageView audioPlayPauseView, audioForward15sView, audioBack15sView;
    //展示访客信息对话框的时间
    private long showVisitorTime;
    private String visitorImageUrl, visitorJumpUrl, visitorTitle, visitorInfoId;
    private int visitorIsJump;
    private List<VisitorInfo> visitorInfos;
    private LandscapeVisitorInfoDialog visitorInfoDialog;
    private PortraitVisitorInfoDialog portraitVisitorInfoDialog;
    private boolean isShowVisitorInfoDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题
        demoApplication = (DemoApplication) getApplication();
        demoApplication.getDRMServer().reset();
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.new_media_play);

        videoPositionDBHelper = new VideoPositionDBHelper(demoApplication.getBoxStore());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        detector = new GestureDetector(this, new MyGesture());

        initView();

        initPlayHander();

        initPlayInfo();

        if (!isLocalPlay) {
            initNetworkTimerTask();
        }

        demoApplication.removeNotification();
    }

    private void initNetworkTimerTask() {
        networkInfoTimerTask = new TimerTask() {
            @Override
            public void run() {
                parseNetworkInfo();
            }
        };

        timer.schedule(networkInfoTimerTask, 0, 600);
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

    enum NetworkStatus {
        WIFI,
        MOBILEWEB,
        NETLESS,
    }

    private NetworkStatus currentNetworkStatus = NetworkStatus.WIFI;
    ConnectivityManager cm;

    private void parseNetworkInfo() {
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                if (currentNetworkStatus != null && currentNetworkStatus == NetworkStatus.WIFI) {
                    return;
                } else {
                    currentNetworkStatus = NetworkStatus.WIFI;
                    showWifiToast();
                }

            } else {
                if (currentNetworkStatus != null && currentNetworkStatus == NetworkStatus.MOBILEWEB) {
                    return;
                } else {
                    currentNetworkStatus = NetworkStatus.MOBILEWEB;
                    showMobileDialog();
                }
            }

            startPlayerTimerTask();
            networkConnected = true;
            if (ism3u8Video && isError) {
                errorPosition = currentPosition;
                player.pause();
                player.stop();
                player.reset();
                player.prepareAsync();
            }
        } else {
            if (currentNetworkStatus != null && currentNetworkStatus == NetworkStatus.NETLESS) {
                return;
            } else {
                currentNetworkStatus = NetworkStatus.NETLESS;
                showNetlessToast();
            }

//            if (timerTask != null) {
//                timerTask.cancel();
//            }

            networkConnected = false;
        }
    }

    private void showWifiToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "已切换至wifi", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showMobileDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MediaPlayActivity.this);
                AlertDialog dialog = builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).setPositiveButton("继续", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setMessage("当前为移动网络，是否继续播放？").create();

                dialog.show();
            }
        });

    }

    private void showNetlessToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "当前无网络信号，无法播放", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPlayerTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {

                if (!isPrepared) {
                    return;
                }

                playerHandler.sendEmptyMessage(0);
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    ImageView lockView;
    ImageView ivCenterPlay;
    ImageView ivDownload;
    ImageView ivTopMenu;
    TextView tvChangeVideo;
    ImageView ivBackVideo, ivNextVideo, ivPlay;

    private void initView() {

        rlBelow = (RelativeLayout) findViewById(R.id.rl_below_info);
        rlPlay = (RelativeLayout) findViewById(R.id.rl_play);

        rlPlay.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isPrepared) {
                    return true;
                }

                resetHideDelayed();

                // 事件监听交给手势类来处理
                detector.onTouchEvent(event);
                return true;
            }
        });

        rlPlay.setClickable(true);
        rlPlay.setLongClickable(true);
        rlPlay.setFocusable(true);

        ivTopMenu = (ImageView) findViewById(R.id.iv_top_menu);
        ivTopMenu.setOnClickListener(onClickListener);

        surfaceView = (SurfaceView) findViewById(R.id.playerSurfaceView);
        bufferProgressBar = (ProgressBar) findViewById(R.id.bufferProgressBar);

        ivCenterPlay = (ImageView) findViewById(R.id.iv_center_play);
        ivCenterPlay.setOnClickListener(onClickListener);

        backPlayList = (ImageView) findViewById(R.id.backPlayList);
        videoIdText = (TextView) findViewById(R.id.videoIdText);
        ivDownload = (ImageView) findViewById(R.id.iv_download_play);
        ivDownload.setOnClickListener(onClickListener);

        playDuration = (TextView) findViewById(R.id.playDuration);
        videoDuration = (TextView) findViewById(R.id.videoDuration);
        playDuration.setText(ParamsUtil.millsecondsToStr(0));
        videoDuration.setText(ParamsUtil.millsecondsToStr(0));

        ivBackVideo = (ImageView) findViewById(R.id.iv_video_back);
        ivNextVideo = (ImageView) findViewById(R.id.iv_video_next);
        ivPlay = (ImageView) findViewById(R.id.iv_play);

        ivBackVideo.setOnClickListener(onClickListener);
        ivNextVideo.setOnClickListener(onClickListener);
        ivPlay.setOnClickListener(onClickListener);

        tvChangeVideo = (TextView) findViewById(R.id.tv_change_video);
        tvChangeVideo.setOnClickListener(onClickListener);

        tvDefinition = (TextView) findViewById(R.id.tv_definition);

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
        playerBottomLayout = (LinearLayout) findViewById(R.id.playerBottomLayout);

        ivFullscreen = (ImageView) findViewById(R.id.iv_fullscreen);

        ivFullscreen.setOnClickListener(onClickListener);
        backPlayList.setOnClickListener(onClickListener);
        tvDefinition.setOnClickListener(onClickListener);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //2.3及以下使用，不然出现只有声音没有图像的问题
        surfaceHolder.addCallback(this);

        subtitleText = (TextView) findViewById(R.id.subtitleText);
        subtitleText2 = (TextView) findViewById(R.id.subtitleText2);
        tv_watch_tip = (TextView) findViewById(R.id.tv_watch_tip);
        tv_pre_watch_over = (TextView) findViewById(R.id.tv_pre_watch_over);
        ll_rewatch = findViewById(R.id.ll_rewatch);
        ll_pre_watch_over = findViewById(R.id.ll_pre_watch_over);
        ll_rewatch.setOnClickListener(onClickListener);

        ll_pre_watch_over.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        lockView = (ImageView) findViewById(R.id.iv_lock);
        lockView.setSelected(false);
        lockView.setOnClickListener(onClickListener);

        avChangeLayout = (LinearLayout) findViewById(R.id.audio_video_change_layout);
        changeToAudioPlayView = (TextView) findViewById(R.id.change_audio_play);
        changeToAudioPlayView.setOnClickListener(onClickListener);
        changeToVideoPlayView = (TextView) findViewById(R.id.change_video_play);
        changeToVideoPlayView.setOnClickListener(onClickListener);
        initAudioLayout();

    }

    private void initAudioLayout() {
        audioLayout = (LinearLayout) findViewById(R.id.audio_layout);
        audioSpeedView = (TextView) findViewById(R.id.audio_speed);
        audioSpeedView.setVisibility(View.INVISIBLE);

        audioSeekBar = (SeekBar) findViewById(R.id.audioProgress);
        audioSeekBar.setMax(100);
        audioSeekBar.setOnSeekBarChangeListener(audioPlayOnSeekBarChangeListener);

        audioCurrentTimeView = (TextView) findViewById(R.id.audio_current_time);
        audioDurationView = (TextView) findViewById(R.id.audio_duration_time);

        audioPlayPauseView = (ImageView) findViewById(R.id.audio_play_pause);
        audioPlayPauseView.setOnClickListener(onClickListener);
        audioForward15sView = (ImageView) findViewById(R.id.audio_forward_15s_view);
        audioForward15sView.setOnClickListener(onClickListener);
        audioBack15sView = (ImageView) findViewById(R.id.audio_back_15s_view);
        audioBack15sView.setOnClickListener(onClickListener);
    }

    OnSeekBarChangeListener audioPlayOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        int progress = 0;

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if ((networkConnected || isLocalPlay) && isPrepared) {
                float nowProgress = seekBar.getProgress();
                float max = seekBar.getMax();
                if (max > 0) {
                    progress = (int) (nowProgress * player.getDuration() / max);
                }
                player.seekTo(progress);
                playerHandler.postDelayed(hidePlayRunnable, 5000);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            playerHandler.removeCallbacks(hidePlayRunnable);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }
    };

    //====================audio video change========================
    private void changeVideoPlayLayout() {
        changeAVPlayStyle(changeToVideoPlayView, changeToAudioPlayView);
        audioLayout.setVisibility(View.GONE);
        isAudio = false;
        if (currrentSubtitleSwitchFlag == 0) {
            if (defSubtitle == 0) {
                subtitleText.setVisibility(View.VISIBLE);
                subtitleText2.setVisibility(View.GONE);
            } else if (defSubtitle == 1) {
                subtitleText.setVisibility(View.GONE);
                subtitleText2.setVisibility(View.VISIBLE);
            } else {
                subtitleText.setVisibility(View.VISIBLE);
                subtitleText2.setVisibility(View.VISIBLE);
            }

        }
    }

    private void changeAudioPlayLayout() {
        changeAVPlayStyle(changeToAudioPlayView, changeToVideoPlayView);
        audioLayout.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setLayoutVisibility(View.GONE, false);
        isAudio = true;

        playerTopLayout.setVisibility(View.VISIBLE);
        if (currrentSubtitleSwitchFlag == 0) {
            subtitleText.setVisibility(View.GONE);
            subtitleText2.setVisibility(View.GONE);
        }
    }

    private void showNotification() {
        demoApplication.showNotification(isLocalPlay, MediaPlayActivity.class, videoId);
    }

    private void changeAVPlayStyle(TextView selectView, TextView deselectView) {
        selectView.setBackgroundResource(R.drawable.av_change_tag_bg);
        selectView.setTextColor(getResources().getColor(R.color.av_change_text_select));
        deselectView.setBackground(null);
        deselectView.setTextColor(getResources().getColor(R.color.av_change_text_normal));
    }

    private void seekToAudioBack15s() {
        if (!isPrepared) {
            return;
        }

        int currentPosition = player.getCurrentPosition();

        int seekToPosition = currentPosition - 15 * 1000;

        if (seekToPosition > 0) {
            player.seekTo(seekToPosition);
        } else {
            player.seekTo(0);
        }
    }

    private void seekToAudioForword15s() {
        if (!isPrepared) {
            return;
        }
        int currentPosition = player.getCurrentPosition();
        int seekToPosition = currentPosition + 15 * 1000;

        if (seekToPosition > player.getDuration()) {
            player.seekTo(player.getDuration());
        } else {
            player.seekTo(seekToPosition);
        }
    }

    private void initPlayHander() {
        playerHandler = new Handler() {
            public void handleMessage(Message msg) {

                if (player == null) {
                    return;
                }

                // 刷新字幕
                if (subtitle != null) {
                    subtitleText.setText(subtitle.getSubtitleByTime(player
                            .getCurrentPosition()));
                }

                if (subtitle2 != null) {
                    subtitleText2.setText(subtitle2.getSubtitleByTime(player.getCurrentPosition()));
                }

                // 更新播放进度
                currentPlayPosition = player.getCurrentPosition();
                currentPosition = player.getCurrentPosition();
                int duration = player.getDuration();

                //如果大于免费观看时长就暂停
                if (isAllowPlayWholeVideo == 0 && currentPlayPosition > freeWatchTime * 1000) {
                    player.pause();
                    tv_watch_tip.setVisibility(View.GONE);
                    ll_pre_watch_over.setVisibility(View.VISIBLE);
                    playerBottomLayout.setVisibility(View.INVISIBLE);
                }

                if (duration > 0) {
                    long pos = skbProgress.getMax() * currentPlayPosition / duration;
                    String currentTime = ParamsUtil.millsecondsToMinuteSecondStr(player.getCurrentPosition());
                    audioCurrentTimeView.setText(currentTime);
                    playDuration.setText(ParamsUtil.millsecondsToStr(player.getCurrentPosition()));
                    skbProgress.setProgress((int) pos);

                    long audioPos = audioSeekBar.getMax() * currentPlayPosition / duration;
                    audioSeekBar.setProgress((int) audioPos);
                }

                //展示访客信息对话框
                if (currentPlayPosition > showVisitorTime && isShowVisitorInfoDialog) {
                    player.seekTo((int) showVisitorTime);
                    showVisitorInfoDialog();
                } else {
                    showVisitorInfoDialog();
                }
            }

            ;
        };


    }

    //展示访客信息框
    private void showVisitorInfoDialog() {
        if (isShowVisitorDialog(currentPlayPosition) && visitorInfos != null && visitorInfos.size() > 0) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (visitorInfoDialog != null && visitorInfoDialog.isShowing()) {
                    return;
                }
                if (portraitVisitorInfoDialog != null && portraitVisitorInfoDialog.isShowing()) {
                    return;
                }
                if (!isShowVisitorInfoDialog) {
                    return;
                }
                visitorInfoDialog = new LandscapeVisitorInfoDialog(MediaPlayActivity.this, videoId, visitorImageUrl, visitorJumpUrl,
                        visitorTitle, visitorInfoId, visitorIsJump, visitorInfos, new CommitOrJumpVisitorInfo() {
                    @Override
                    public void commit() {
                        isShowVisitorInfoDialog = false;
                        changePlayStatus();
                    }

                    @Override
                    public void jump() {
                        isShowVisitorInfoDialog = false;
                        changePlayStatus();
                    }
                });
                visitorInfoDialog.setCanceledOnTouchOutside(false);
                visitorInfoDialog.show();
                visitorInfoDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                changePlayStatus();

            } else {
                if (portraitVisitorInfoDialog != null && portraitVisitorInfoDialog.isShowing()) {
                    return;
                }
                if (visitorInfoDialog != null && visitorInfoDialog.isShowing()) {
                    return;
                }
                if (!isShowVisitorInfoDialog) {
                    return;
                }
                portraitVisitorInfoDialog = new PortraitVisitorInfoDialog(MediaPlayActivity.this, videoId, visitorImageUrl, visitorJumpUrl,
                        visitorTitle, visitorInfoId, visitorIsJump, visitorInfos, new CommitOrJumpVisitorInfo() {
                    @Override
                    public void commit() {
                        isShowVisitorInfoDialog = false;
                        changePlayStatus();
                    }

                    @Override
                    public void jump() {
                        isShowVisitorInfoDialog = false;
                        changePlayStatus();
                    }
                });
                portraitVisitorInfoDialog.setCanceledOnTouchOutside(false);
                portraitVisitorInfoDialog.show();
                portraitVisitorInfoDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                changePlayStatus();
            }
        }
    }

    private boolean isShowVisitorDialog(int currentPosition) {
        long timeInterval = currentPosition - showVisitorTime;
        if (timeInterval >= 0 && timeInterval < 1000) {
            return true;
        } else {
            return false;
        }
    }

    private void resetVisitorInfo() {
        if (visitorInfos != null && visitorInfos.size() > 0) {
            visitorInfos.removeAll(visitorInfos);
        }
        visitorInfoDialog = null;
        portraitVisitorInfoDialog = null;
        isShowVisitorInfoDialog = false;
    }


    private void initPlayInfo() {

        // 通过定时器和Handler来更新进度
        isPrepared = false;
        demoApplication.releaseDWPlayer();
        player = demoApplication.getDWPlayer();
        player.reset();

        player.setOnDreamWinErrorListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
        player.setOnVideoSizeChangedListener(this);
        player.setOnInfoListener(this);
        player.setDRMServerPort(demoApplication.getDrmServerPort());

        //设置自定义CustomId
        player.setCustomId("OK2019");
        player.setDefaultPlayMode(MediaUtil.PLAY_MODE, new OnPlayModeListener() {
            @Override
            public void onPlayMode(MediaMode playMode) {
                currentPlayMode = playMode;
            }
        });

        videoId = getIntent().getStringExtra("videoId");
        verificationCode = getIntent().getStringExtra("verifyCode");
        videoIdText.setText(videoId);
        isLocalPlay = getIntent().getBooleanExtra("isLocalPlay", false);

        //播放音频
        int playModeInteger = getIntent().getIntExtra("playMode", 1);
        switch (MediaUtil.PLAY_MODE) {
            case AUDIO:
                player.setAudioPlay(true);
                break;
            default:
                player.setAudioPlay(false);
                break;
        }

        if (playModeInteger == 2) {
            currentPlayMode = MediaMode.AUDIO;
        } else {
            currentPlayMode = MediaMode.VIDEO;
        }

        try {

            if (!isLocalPlay) {// 播放线上视频
                player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, verificationCode, this);
                // 设置默认清晰度
                player.setDefaultDefinition(defaultDefinition);

            } else {// 播放本地已下载视频
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    path = Environment.getExternalStorageDirectory() + "/".concat(ConfigUtil.DOWNLOAD_DIR).concat("/").concat(videoId).concat(MediaUtil.PCM_FILE_SUFFIX);
                    if (!new File(path).exists()) {
                        return;
                    }
                }
            }

        } catch (IllegalArgumentException e) {
            Log.e("player error", e.getMessage());
        } catch (SecurityException e) {
            Log.e("player error", e.getMessage());
        } catch (IllegalStateException e) {
            Log.e("player error", e + "");
        }

        //设置字幕监听器
        player.setOnSubtitleMsgListener(new OnSubtitleMsgListener() {
            @Override
            public void onSubtitleMsg(String subtitleName, final int sort, String url, String font, final int size, final String color, final String surroundColor, final double bottom, String code) {
                // 设置视频字幕
                subtitle = new Subtitle(new OnSubtitleInitedListener() {
                    @Override
                    public void onInited(Subtitle subtitle) {
                        // 初始化字幕控制菜单
                    }
                });
                if (!TextUtils.isEmpty(url)) {
                    subtitle.initSubtitleResource(url);
                }
                firstSubName = subtitleName;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (size > 0) {
                            subtitleText.setTextSize(size / 2);
                        }
                        try {
                            if (!TextUtils.isEmpty(color)) {
                                String newColor = color;
                                if (color.contains("0x")) {
                                    newColor = color.replace("0x", "#");
                                }
                                subtitleText.setTextColor(Color.parseColor(newColor));
                                subtitleText.setShadowLayer(10F, 5F, 5F, Color.YELLOW);
                            }

                            if (!TextUtils.isEmpty(surroundColor)) {
                                String newSurroundColor = surroundColor;
                                if (surroundColor.contains("0x")) {
                                    newSurroundColor = surroundColor.replace("0x", "#");
                                }
                                subtitleText.setShadowLayer(10F, 5F, 5F, Color.parseColor(newSurroundColor));
                            }
                        } catch (Exception e) {

                        }

                        if (bottom > 0) {
                            int paddingBottom = 0;
                            Resources resources = getResources();
                            if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                paddingBottom = (int) (resources.getDisplayMetrics().heightPixels * bottom);
                            } else {
                                paddingBottom = (int) (resources.getDisplayMetrics().widthPixels * bottom);
                            }
                            firstBottom = paddingBottom;
                            if (sort == 2) {
                                commonBottom = paddingBottom;
                            }
                            subtitleText.setPadding(0, 0, 0, paddingBottom);
                        }

                        //设置字体  将字体文件拷贝到assets文件夹下
//                        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/roli.ttc");
                    }
                });
            }

            @Override
            public void onSecSubtitleMsg(String subtitleName, final int sort, String url, String font, final int size, final String color, final String surroundColor, final double bottom, String code) {
                // 设置第二种视频字幕
                subtitle2 = new Subtitle(new OnSubtitleInitedListener() {
                    @Override
                    public void onInited(Subtitle subtitle) {
                        // 初始化字幕控制菜单
                    }
                });
                if (!TextUtils.isEmpty(url)) {
                    subtitle2.initSubtitleResource(url);
                }
                secondSubName = subtitleName;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (size > 0) {
                            subtitleText2.setTextSize(size / 2);
                        }
                        try {
                            if (!TextUtils.isEmpty(color)) {
                                String newColor = color;
                                if (color.contains("0x")) {
                                    newColor = color.replace("0x", "#");
                                }
                                subtitleText2.setTextColor(Color.parseColor(newColor));
                                subtitleText2.setShadowLayer(10F, 5F, 5F, Color.YELLOW);
                            }

                            if (!TextUtils.isEmpty(surroundColor)) {
                                String newSurroundColor = surroundColor;
                                if (surroundColor.contains("0x")) {
                                    newSurroundColor = surroundColor.replace("0x", "#");
                                }
                                subtitleText2.setShadowLayer(10F, 5F, 5F, Color.parseColor(newSurroundColor));
                            }
                        } catch (Exception e) {

                        }

                        if (bottom > 0) {
                            int paddingBottom = 0;
                            Resources resources = getResources();
                            if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                paddingBottom = (int) (resources.getDisplayMetrics().heightPixels * bottom);
                            } else {
                                paddingBottom = (int) (resources.getDisplayMetrics().widthPixels * bottom);
                            }
                            secondBottom = paddingBottom;
                            if (sort == 2) {
                                commonBottom = paddingBottom;
                            }
                            subtitleText2.setPadding(0, 0, 0, paddingBottom);
                        }

                        //设置字体  将字体文件拷贝到assets文件夹下
//                        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/roli.ttc");
                    }
                });
            }

            @Override
            public void onDefSubtitle(final int defaultSubtitle) {
                defSubtitle = defaultSubtitle;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (defaultSubtitle == 0) {
                            subtitleText.setVisibility(View.VISIBLE);
                            subtitleText2.setVisibility(View.GONE);
                            subtitleText.setPadding(0, 0, 0, commonBottom);
                            subtitleText2.setPadding(0, 0, 0, commonBottom);
                        } else if (defaultSubtitle == 1) {
                            subtitleText.setVisibility(View.GONE);
                            subtitleText2.setVisibility(View.VISIBLE);
                            subtitleText.setPadding(0, 0, 0, commonBottom);
                            subtitleText2.setPadding(0, 0, 0, commonBottom);
                        } else {
                            subtitleText.setVisibility(View.VISIBLE);
                            subtitleText2.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        });

        //设置鉴权监听器
        player.setOnAuthMsgListener(new OnAuthMsgListener() {
            @Override
            public void onAuthMsg(final int enable, final int freetime, final String messaage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isAllowPlayWholeVideo = enable;
                        freeWatchTime = freetime;
                        freeWatchOverMsg = messaage;
                        if (isAllowPlayWholeVideo == 0) {
                            if (freeWatchTime > 0) {
                                tv_watch_tip.setVisibility(View.VISIBLE);
                            }
                            int minute = freeWatchTime / 60;
                            int second = freeWatchTime % 60;
                            tv_watch_tip.setText("可试看" + minute + "分钟" + second + "秒，购买会员查看完整版");
                        }
                        tv_pre_watch_over.setText(freeWatchOverMsg);
                    }
                });
            }
        });

        //设置访客信息收集监听器
        visitorInfos = new ArrayList<>();
        player.setOnVisitMsgListener(new OnVisitMsgListener() {
            @Override
            public void onVisitMsg(int appearTime, String imageURL, int isJump, String jumpURL, String title, String visitorId, JSONArray visitorMessage) {
                showVisitorTime = appearTime * 1000;
                visitorImageUrl = imageURL;
                visitorIsJump = isJump;
                visitorJumpUrl = jumpURL;
                visitorTitle = title;
                visitorInfoId = visitorId;
                if (visitorMessage != null && visitorMessage.length() > 0) {
                    isShowVisitorInfoDialog = true;
                    for (int i = 0; i < visitorMessage.length(); i++) {
                        try {
                            VisitorInfo visitorInfo = new VisitorInfo(visitorMessage.getJSONObject(i));
                            visitorInfos.add(visitorInfo);
                        } catch (JSONException e) {

                        }
                    }

                }
            }
        });


    }

    private LayoutParams getScreenSizeParams(int position) {
        currentScreenSizeFlag = position;
        int width = 600;
        int height = 400;
        if (isPortrait()) {
            width = wm.getDefaultDisplay().getWidth();
            height = wm.getDefaultDisplay().getHeight() * 2 / 5; //TODO 根据当前布局更改
        } else {
            width = wm.getDefaultDisplay().getWidth();
            height = wm.getDefaultDisplay().getHeight();
        }


        String screenSizeStr = screenSizeArray[position];
        if (screenSizeStr.indexOf("%") > 0) {// 按比例缩放
            int vWidth = player.getVideoWidth();
            if (vWidth == 0) {
                vWidth = 600;
            }

            int vHeight = player.getVideoHeight();
            if (vHeight == 0) {
                vHeight = 400;
            }

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


            int screenSize = ParamsUtil.getInt(screenSizeStr.substring(0, screenSizeStr.indexOf("%")));
            width = (width * screenSize) / 100;
            height = (height * screenSize) / 100;
        }

        LayoutParams params = new LayoutParams(width, height);
        return params;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (player.isPlaying() && isAudio) {
            return;
        }

        try {
            surfaceHolder = holder;
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnBufferingUpdateListener(this);
            player.setOnPreparedListener(this);
            player.setDisplay(holder);
            player.setScreenOnWhilePlaying(true);


            if (isLocalPlay) {
                player.setOfflineVideoPath(path, this);
                demoApplication.getDRMServer().resetLocalPlay();
            } else {
                demoApplication.getDRMServer().reset();
            }
            player.prepareAsync();
        } catch (Exception e) {
            Log.e("videoPlayer", "error", e);
        }
        Log.i("videoPlayer", "surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        holder.setFixedSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player == null) {
            return;
        }
        if (isPrepared) {
            currentPosition = player.getCurrentPosition();
        }

        if (isAudio) {
            return;
        }

        isPrepared = false;
        isSurfaceDestroy = true;

        player.stop();
        player.reset();

    }

    private void initTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {

                if (!isPrepared) {
                    return;
                }
                if (playerHandler != null) {
                    playerHandler.sendEmptyMessage(0);
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isFirstBuffer = false;
        videoPlayInfo = player.getPlayInfo();
        if (videoPlayInfo != null && videoPlayInfo.getPlayUrl().contains(".m3u8")) {
            ism3u8Video = true;
        }
        initTimerTask();
        isPrepared = true;
        if (!isFreeze) {
            if (isPlaying == null || isPlaying.booleanValue()) {
                player.start();
                ivPlay.setImageResource(R.drawable.smallstop_ic);
            }
        }

        if (!isLocalPlay) {
            if (errorPosition > 0 && ism3u8Video) {
                player.seekTo(errorPosition);
                errorPosition = 0;
                isError = false;
                isShowErrorToast = true;
                bufferProgressBar.setVisibility(View.GONE);
            } else if (currentPosition > 0) {
                player.seekTo(currentPosition);
            } else {
                lastVideoPosition = videoPositionDBHelper.getVideoPosition(videoId);
                if (lastVideoPosition == null) {
                    lastVideoPosition = new VideoPosition(videoId, 0);
                } else if (lastVideoPosition.getPosition() > 0) {
                    player.seekTo(lastVideoPosition.getPosition());
                }
            }
        } else {
            lastVideoPosition = videoPositionDBHelper.getVideoPosition("local" + videoId);
            if (lastVideoPosition == null) {
                lastVideoPosition = new VideoPosition("local" + videoId, 0);
            } else if (lastVideoPosition.getPosition() > 0) {
                player.seekTo(lastVideoPosition.getPosition());
            }

        }

        definitionMap = player.getDefinitions();
        if (!isLocalPlay) {
            initDefinitionPopMenu();
        }

        bufferProgressBar.setVisibility(View.GONE);
        setSurfaceViewLayout();
        String videoDurationStr = ParamsUtil.millsecondsToMinuteSecondStr(player.getDuration());
        videoDuration.setText(videoDurationStr);
        audioDurationView.setText(videoDurationStr);

        avChangeLayout.setVisibility(View.GONE);
        switch (currentPlayMode) {
            case VIDEOAUDIO:
                avChangeLayout.setVisibility(View.VISIBLE);
                break;
            case AUDIO:
                changeAudioPlayLayout();
                break;
            case VIDEO:
                changeVideoPlayLayout();
                break;
        }

    }

    // 设置surfaceview的布局
    private void setSurfaceViewLayout() {
        LayoutParams params = getScreenSizeParams(currentScreenSizeFlag);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(params);
    }

    private void initDefinitionPopMenu() {
        if (definitionMap.size() > 1) {
            currentDefinitionIndex = 1;
            Integer[] definitions = new Integer[]{};
            definitions = definitionMap.values().toArray(definitions);
            // 设置默认为普清，所以此处需要判断一下
            for (int i = 0; i < definitions.length; i++) {
                if (definitions[i].intValue() == defaultDefinition) {
                    currentDefinitionIndex = i;
                }
            }

//			firstInitDefinition = false;
        }

        definitionMenu = new PopMenu(this, R.drawable.popdown, currentDefinitionIndex, getResources().getDimensionPixelSize(R.dimen.popmenu_height));
        // 设置清晰度列表
        definitionArray = new String[]{};
        definitionArray = definitionMap.keySet().toArray(definitionArray);

        definitionMenu.addItems(definitionArray);
        definitionMenu.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                try {

                    currentDefinitionIndex = position;
                    defaultDefinition = definitionMap.get(definitionArray[position]);

                    if (isPrepared) {
                        currentPosition = player.getCurrentPosition();
                        if (player.isPlaying()) {
                            isPlaying = true;
                        } else {
                            isPlaying = false;
                        }
                    }

                    isPrepared = false;

                    setLayoutVisibility(View.GONE, false);
                    bufferProgressBar.setVisibility(View.VISIBLE);

                    demoApplication.getDRMServer().disconnectCurrentStream();
                    player.reset();
                    if (isLocalPlay) {
                        demoApplication.getDRMServer().resetLocalPlay();
                    } else {
                        demoApplication.getDRMServer().reset();
                    }

                    player.setDisplay(surfaceHolder);

                    player.setDefinition(getApplicationContext(), defaultDefinition);

                } catch (IOException e) {
                    Log.e("player error", e.getMessage());
                }

            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        skbProgress.setSecondaryProgress(percent);
        audioSeekBar.setSecondaryProgress(percent);
    }

    OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            resetHideDelayed();

            switch (v.getId()) {
                case R.id.backPlayList:
                    if (isPortrait() || isLocalPlay) {
                        finish();
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    break;
                case R.id.iv_fullscreen:
                    if (isPortrait()) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    break;
                case R.id.tv_definition:
                    definitionMenu.showAsDropDown(v);
                    break;
                case R.id.iv_lock:
                    if (lockView.isSelected()) {
                        lockView.setSelected(false);
                        setLayoutVisibility(View.VISIBLE, true);
                        toastInfo("已解开屏幕");
                    } else {
                        lockView.setSelected(true);
                        setLandScapeRequestOrientation();
                        setLayoutVisibility(View.GONE, true);
                        lockView.setVisibility(View.VISIBLE);
                        toastInfo("已锁定屏幕");
                    }
                    break;
                case R.id.iv_center_play:
                case R.id.iv_play:
                    changePlayStatus();
                    break;
                case R.id.iv_download_play:
                    downloadCurrentVideo();
                    break;
                case R.id.iv_top_menu:
                    setLayoutVisibility(View.GONE, false);
                    showTopPopupWindow();
                    break;
                case R.id.tv_change_video:
                    setLayoutVisibility(View.GONE, false);
                    showChangeVideoWindow();
                    break;
                case R.id.iv_video_back:
                    changeToBackVideo();
                    break;
                case R.id.iv_video_next:
                    changeToNextVideo(false);
                    break;
                case R.id.ll_rewatch:
                    player.seekTo(0);
                    playDuration.setText(ParamsUtil.millsecondsToStr(0));
                    player.start();
                    ll_pre_watch_over.setVisibility(View.GONE);
                    tv_watch_tip.setVisibility(View.VISIBLE);
                    playerBottomLayout.setVisibility(View.VISIBLE);
                    break;

                case R.id.audio_back_15s_view:
                    seekToAudioBack15s();
                    break;
                case R.id.audio_forward_15s_view:
                    seekToAudioForword15s();
                    break;

                case R.id.audio_play_pause:
                    changePlayStatus();
                    break;
                case R.id.change_audio_play:
                    cancelTimerTask();
                    toastInfo("切换到音频播放，请稍候……");
                    changeAudioPlayLayout();
                    prepareAVPlayer(true);
                    break;
                case R.id.change_video_play:
                    cancelTimerTask();
                    toastInfo("切换到视频播放，请稍候……");
                    changeVideoPlayLayout();
                    prepareAVPlayer(false);
                    break;

            }
        }
    };

    //取消计时器
    private void cancelTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    private void prepareAVPlayer(boolean isAudio) {
        if (isPrepared) {
            currentPosition = player.getCurrentPosition();
            if (player.isPlaying()) {
                isPlaying = true;
            } else {
                isPlaying = false;
            }
        }

        isPrepared = false;
        isSwitchAv = true;

        player.pause();
        player.stop();
        demoApplication.getDRMServer().disconnectCurrentStream();
        player.reset();
        player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, verificationCode, MediaPlayActivity.this);
        player.setDisplay(surfaceHolder);
        if (isLocalPlay) {
            demoApplication.getDRMServer().resetLocalPlay();
        } else {
            demoApplication.getDRMServer().reset();
        }
        player.setAudioPlay(isAudio);
        player.prepareAsync();
    }

    // 设置横屏的固定方向，禁用掉重力感应方向
    private void setLandScapeRequestOrientation() {
        int rotation = wm.getDefaultDisplay().getRotation();
        // 旋转90°为横屏正向，旋转270°为横屏逆向
        if (rotation == Surface.ROTATION_90) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (rotation == Surface.ROTATION_270) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }

    private void changeToNextVideo(boolean isCompleted) {
        int currentIndex = getCurrentVideoIndex();
        int length = PlayFragment.playVideoIds.length;
        int position = 0;
        if (currentIndex == length - 1) {
            position = 0;
        } else {
            position = ++currentIndex;
        }
        changeVideo(position, isCompleted);
    }

    private void changeToBackVideo() {
        int currentPosition = getCurrentVideoIndex();
        int length = PlayFragment.playVideoIds.length;
        int position = 0;
        if (currentPosition == 0) {
            position = length - 1;
        } else {
            position = --currentPosition;
        }
        changeVideo(position, false);
    }

    PlayChangeVideoPopupWindow playChangeVideoPopupWindow;

    private void showChangeVideoWindow() {
        if (playChangeVideoPopupWindow == null) {
            initPlayChangeVideoPopupWindow();
        }
        playChangeVideoPopupWindow.setSelectedPosition(getCurrentVideoIndex()).showAsDropDown(rlPlay);
    }

    private int getCurrentVideoIndex() {
        return Arrays.asList(PlayFragment.playVideoIds).indexOf(videoId);
    }

    private void initPlayChangeVideoPopupWindow() {
        playChangeVideoPopupWindow = new PlayChangeVideoPopupWindow(this, surfaceView.getHeight());

        playChangeVideoPopupWindow.setItem(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeVideo(position, false);
                playChangeVideoPopupWindow.setSelectedPosition(position);
                playChangeVideoPopupWindow.refreshView();
            }
        });
    }

    private void changeVideo(int position, boolean isCompleted) {
        if (isCompleted) {
            updateCompleteDataPosition();
        } else {
            updateDataPosition();
        }

        isPrepared = false;

        setLayoutVisibility(View.GONE, false);
        bufferProgressBar.setVisibility(View.VISIBLE);
        ivCenterPlay.setVisibility(View.GONE);

        ism3u8Video = false;
        currentPosition = 0;
        errorPosition = 0;
        currentPlayPosition = 0;
        isFirstBuffer = true;
        //使用主线路播放
        player.setBackupPlay(false);
        //重置字幕信息
        firstSubName = "";
        secondSubName = "";
        subtitle = null;
        subtitle2 = null;
        defSubtitle = 3;
        playTopPopupWindow = null;
        isTwoSubtitle = false;

        //清除访客信息
        resetVisitorInfo();

        timerTask.cancel();

        videoId = PlayFragment.playVideoIds[position];

        if (playChangeVideoPopupWindow != null) {
            playChangeVideoPopupWindow.setSelectedPosition(getCurrentVideoIndex()).refreshView();
        }

        player.pause();
        player.stop();
        demoApplication.getDRMServer().disconnectCurrentStream();
        player.reset();
        player.setDefaultDefinition(defaultDefinition);
        player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, verificationCode, MediaPlayActivity.this);
        player.setDisplay(surfaceHolder);
        if (isLocalPlay) {
            demoApplication.getDRMServer().resetLocalPlay();
        } else {
            demoApplication.getDRMServer().reset();
        }
        player.setAudioPlay(isAudio);
        player.prepareAsync();
    }

    PlayTopPopupWindow playTopPopupWindow;

    private void showTopPopupWindow() {
        if (playTopPopupWindow == null) {
            initPlayTopPopupWindow();
        }
        playTopPopupWindow.showAsDropDown(rlPlay);
    }

    private void initPlayTopPopupWindow() {
        playTopPopupWindow = new PlayTopPopupWindow(this, surfaceView.getHeight(), new OnSubtitleRadioButton() {
            @Override
            public void getRadioButtons(RadioButton fisrtSub, RadioButton secondSub, RadioButton twoSub) {
                if (!TextUtils.isEmpty(firstSubName) && !TextUtils.isEmpty(secondSubName)) {
                    twoSub.setText("双语");
                    isTwoSubtitle = true;
                }
                fisrtSub.setText(firstSubName);
                secondSub.setText(secondSubName);
                if (defSubtitle == 0) {
                    fisrtSub.setChecked(true);
                } else if (defSubtitle == 1) {
                    secondSub.setChecked(true);
                } else if (defSubtitle == 2) {
                    twoSub.setChecked(true);
                }
            }
        });
        playTopPopupWindow.setSubtitleCheckLister(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_two_subtitle:// 双语字幕
                        currrentSubtitleSwitchFlag = 0;
                        subtitleText.setVisibility(View.VISIBLE);
                        subtitleText2.setVisibility(View.VISIBLE);
                        defSubtitle = 2;
                        if (isTwoSubtitle) {
                            subtitleText.setPadding(0, 0, 0, firstBottom);
                            subtitleText2.setPadding(0, 0, 0, secondBottom);
                        }
                        break;
                    case R.id.rb_subtitle_close:// 关闭字幕
                        currrentSubtitleSwitchFlag = 1;
                        subtitleText.setVisibility(View.GONE);
                        subtitleText2.setVisibility(View.GONE);
                        defSubtitle = 3;
                        break;
                    case R.id.rb_first_subtitle:// 第一字幕
                        currrentSubtitleSwitchFlag = 0;
                        subtitleText.setVisibility(View.VISIBLE);
                        subtitleText2.setVisibility(View.GONE);
                        defSubtitle = 0;
                        if (isTwoSubtitle) {
                            subtitleText.setPadding(0, 0, 0, commonBottom);
                        }
                        break;
                    case R.id.rb_second_subtitle:// 第二字幕
                        currrentSubtitleSwitchFlag = 1;
                        subtitleText.setVisibility(View.GONE);
                        subtitleText2.setVisibility(View.VISIBLE);
                        defSubtitle = 1;
                        if (isTwoSubtitle) {
                            subtitleText2.setPadding(0, 0, 0, commonBottom);
                        }
                        break;
                }
            }
        });

        playTopPopupWindow.setScreenSizeCheckLister(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                int position = 0;
                switch (checkedId) {
                    case R.id.rb_screensize_full:
                        position = 0;
                        break;
                    case R.id.rb_screensize_100:
                        position = 1;
                        break;
                    case R.id.rb_screensize_75:
                        position = 2;
                        break;
                    case R.id.rb_screensize_50:
                        position = 3;
                        break;
                }

                Toast.makeText(getApplicationContext(), screenSizeArray[position], Toast.LENGTH_SHORT).show();
                LayoutParams params = getScreenSizeParams(position);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                surfaceView.setLayoutParams(params);
            }
        });


        playTopPopupWindow.setCheckNetwork(new CheckNetwork() {
            @Override
            public void checkNet() {
                CustomDialogCheckNetwork customDialogCheckNetwork = new CustomDialogCheckNetwork(MediaPlayActivity.this, videoId, videoPlayInfo);
                customDialogCheckNetwork.show();
            }
        });

    }

    private void downloadCurrentVideo() {
        if (DataSet.hasDownloadInfo(videoId)) {
            Toast.makeText(this, "文件已存在", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadController.insertDownloadInfo(videoId, verificationCode, videoId);
        Toast.makeText(this, "文件已加入下载队列", Toast.LENGTH_SHORT).show();
    }

    private void toastInfo(String info) {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {
        int progress = 0;

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (networkConnected || isLocalPlay) {
                player.seekTo(progress);
                playerHandler.postDelayed(hidePlayRunnable, 5000);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            playerHandler.removeCallbacks(hidePlayRunnable);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int verifyPosion = progress * player.getDuration() / seekBar.getMax();
            if (verifyPosion > freeWatchTime * 1000 && isAllowPlayWholeVideo == 0) {
                skbProgress.setProgress(progress);
                this.progress = freeWatchTime * 1000;
            } else {
                if (networkConnected || isLocalPlay) {
                    this.progress = progress * player.getDuration() / seekBar.getMax();
                }
            }
        }
    };

    VerticalSeekBar.OnSeekBarChangeListener seekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            currentVolume = progress;
            volumeSeekBar.setProgress(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            playerHandler.removeCallbacks(hidePlayRunnable);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            playerHandler.postDelayed(hidePlayRunnable, 5000);
        }

    };

    // 控制播放器面板显示
    private boolean isDisplay = false;

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

    /**
     * @param visibility 显示状态
     * @param isDisplay  是否延迟消失
     */
    private void setLayoutVisibility(int visibility, boolean isDisplay) {
        if (player == null || player.getDuration() <= 0 || isAudio) {
            return;
        }

        playerHandler.removeCallbacks(hidePlayRunnable);

        this.isDisplay = isDisplay;

        if (definitionMenu != null && visibility == View.GONE) {
            definitionMenu.dismiss();
        }

        if (isDisplay) {
            playerHandler.postDelayed(hidePlayRunnable, 5000);
        }

        if (isPortrait()) {
            ivFullscreen.setVisibility(visibility);

            lockView.setVisibility(View.GONE);

            volumeLayout.setVisibility(View.GONE);
            tvDefinition.setVisibility(View.GONE);
            tvChangeVideo.setVisibility(View.GONE);
            ivTopMenu.setVisibility(View.GONE);
            ivBackVideo.setVisibility(View.GONE);
            ivNextVideo.setVisibility(View.GONE);
        } else {
            ivFullscreen.setVisibility(View.GONE);

            lockView.setVisibility(visibility);
            if (lockView.isSelected()) {
                visibility = View.GONE;
            }

            volumeLayout.setVisibility(visibility);
            tvDefinition.setVisibility(visibility);
            tvChangeVideo.setVisibility(visibility);
            ivTopMenu.setVisibility(visibility);
            ivBackVideo.setVisibility(visibility);
            ivNextVideo.setVisibility(visibility);
        }

        if (isLocalPlay) {
            ivDownload.setVisibility(View.GONE);
            ivTopMenu.setVisibility(View.GONE);

            ivBackVideo.setVisibility(View.GONE);
            ivNextVideo.setVisibility(View.GONE);
            tvChangeVideo.setVisibility(View.GONE);
            tvDefinition.setVisibility(View.GONE);
            ivFullscreen.setVisibility(View.INVISIBLE);
        }

        playerTopLayout.setVisibility(visibility);
        playerBottomLayout.setVisibility(visibility);
    }


    private Handler alertHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (isShowErrorToast) {
                if (isSwitchAv && msg.what == -38) {
                    return;
                }
                toastInfo("播放异常，无法播放。" + msg.what);
                if (ism3u8Video) {
                    if (bufferProgressBar.getVisibility() == View.GONE) {
                        bufferProgressBar.setVisibility(View.VISIBLE);
                    }
                    isShowErrorToast = false;
                }
            }
            super.handleMessage(msg);
        }

    };

    @Override
    public boolean onError(MediaPlayer mp, final int what, int extra) {
        isError = true;
        if (DWMediaPlayer.MEDIA_ERROR_DRM_LOCAL_SERVER == what) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toastInfo("server_error");
                }
            });
        }

        Message msg = new Message();
        msg.what = what;
        if (!isBackupPlay && !isLocalPlay && isFirstBuffer) {
            startBackupPlay();
        } else {
            if (alertHandler != null) {
                alertHandler.sendMessage(msg);
            }
        }
        return true;
    }

    //启用备用播放线路
    private void startBackupPlay() {
        updateDataPosition();
        player.setBackupPlay(true);
        isBackupPlay = true;

        isPrepared = false;
        setLayoutVisibility(View.GONE, false);
        bufferProgressBar.setVisibility(View.VISIBLE);
        ivCenterPlay.setVisibility(View.GONE);

        if (timerTask != null) {
            timerTask.cancel();
        }
        player.pause();
        player.stop();
        demoApplication.getDRMServer().disconnectCurrentStream();
        player.reset();
        player.setDisplay(surfaceHolder);
        demoApplication.getDRMServer().reset();
        try {
            player.prepareAsync();
        } catch (Exception e) {

        }

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        setSurfaceViewLayout();
    }


    // 重置隐藏界面组件的延迟时间
    private void resetHideDelayed() {
        playerHandler.removeCallbacks(hidePlayRunnable);
        playerHandler.postDelayed(hidePlayRunnable, 5000);
    }

    // 手势监听器类
    private class MyGesture extends SimpleOnGestureListener {

        private Boolean isVideo;
        private float scrollCurrentPosition;
        private float scrollCurrentVolume;

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (lockView.isSelected()) {
                return true;
            }
            if (isVideo == null) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    isVideo = true;
                } else {
                    isVideo = false;
                }
            }

            if (isVideo.booleanValue()) {
                parseVideoScroll(distanceX);
            } else {
                parseAudioScroll(distanceY);
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        private void parseVideoScroll(float distanceX) {
            if (!isDisplay) {
                setLayoutVisibility(View.VISIBLE, true);
            }

            scrollTotalDistance += distanceX;

            float duration = (float) player.getDuration();

            float width = wm.getDefaultDisplay().getWidth() * 0.75f; // 设定总长度是多少，此处根据实际调整
            //右滑distanceX为负
            float currentPosition = scrollCurrentPosition - (float) duration * scrollTotalDistance / width;

            if (currentPosition < 0) {
                currentPosition = 0;
            } else if (currentPosition > duration) {
                currentPosition = duration;
            }
            if (currentPosition > freeWatchTime * 1000 && isAllowPlayWholeVideo == 0) {
                currentPosition = freeWatchTime * 1000;
            }
            player.seekTo((int) currentPosition);

            playDuration.setText(ParamsUtil.millsecondsToStr((int) currentPosition));
            int pos = (int) (skbProgress.getMax() * currentPosition / duration);
            skbProgress.setProgress(pos);

            int AudioPos = (int) (audioSeekBar.getMax() * currentPosition / duration);
            audioSeekBar.setProgress(AudioPos);
        }

        private void parseAudioScroll(float distanceY) {
            if (!isDisplay) {
                setLayoutVisibility(View.VISIBLE, true);
            }
            scrollTotalDistance += distanceY;

            float height = wm.getDefaultDisplay().getHeight() * 0.75f;
            // 上滑distanceY为正
            currentVolume = (int) (scrollCurrentVolume + maxVolume * scrollTotalDistance / height);

            if (currentVolume < 0) {
                currentVolume = 0;
            } else if (currentVolume > maxVolume) {
                currentVolume = maxVolume;
            }

            volumeSeekBar.setProgress(currentVolume);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            scrollTotalDistance = 0f;
            isVideo = null;

            scrollCurrentPosition = (float) player.getCurrentPosition();
            scrollCurrentVolume = currentVolume;

            return super.onDown(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (lockView.isSelected()) {
                return true;
            }
            if (!isDisplay) {
                setLayoutVisibility(View.VISIBLE, true);
            }
            changePlayStatus();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isDisplay) {
                setLayoutVisibility(View.GONE, false);
            } else {
                setLayoutVisibility(View.VISIBLE, true);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private void changePlayStatus() {

        if (!isPrepared) {
            return;
        }

        if (player.isPlaying()) {
            player.pause();
            ivCenterPlay.setVisibility(View.VISIBLE);
            ivPlay.setImageResource(R.drawable.smallbegin_ic);
            audioPlayPauseView.setImageResource(R.drawable.audio_play_icon);

        } else {
            player.start();
            ivCenterPlay.setVisibility(View.GONE);
            ivPlay.setImageResource(R.drawable.smallstop_ic);
            audioPlayPauseView.setImageResource(R.drawable.audio_pause_icon);
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case DWMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (player.isPlaying()) {
                    bufferProgressBar.setVisibility(View.VISIBLE);
                }
                break;
            case DWMediaPlayer.MEDIA_INFO_BUFFERING_END:
                bufferProgressBar.setVisibility(View.GONE);
                break;
        }
        return false;
    }

    // 获得当前屏幕的方向
    private boolean isPortrait() {
        int mOrientation = getApplicationContext().getResources().getConfiguration().orientation;
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        } else {
            return true;
        }
    }

    private int mX, mY, mZ;
    private long lastTimeStamp = 0;
    private Calendar mCalendar;
    private SensorManager sensorManager;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        if (!lockView.isSelected() && (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            mCalendar = Calendar.getInstance();
            long stamp = mCalendar.getTimeInMillis() / 1000l;

            int second = mCalendar.get(Calendar.SECOND);// 53

            int px = Math.abs(mX - x);
            int py = Math.abs(mY - y);
            int pz = Math.abs(mZ - z);

            int maxvalue = getMaxValue(px, py, pz);
            if (maxvalue > 2 && (stamp - lastTimeStamp) > 1) {
                lastTimeStamp = stamp;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
            mX = x;
            mY = y;
            mZ = z;
        }
    }

    /**
     * 获取一个最大值
     *
     * @param px
     * @param py
     * @param pz
     * @return
     */
    private int getMaxValue(int px, int py, int pz) {
        int max = 0;
        if (px > py && px > pz) {
            max = px;
        } else if (py > px && py > pz) {
            max = py;
        } else if (pz > px && pz > py) {
            max = pz;
        }
        return max;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onBackPressed() {

        if (isPortrait() || isLocalPlay) {
            super.onBackPressed();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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
        if (!isLocalPlay) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        if (!isAudio && isPrepared) {
            // 如果播放器prepare完成，则对播放器进行暂停操作，并记录状态
            if (player.isPlaying()) {
                isPlaying = true;
            } else {
                isPlaying = false;
            }
            player.pause();
        } else {
            // 如果播放器没有prepare完成，则设置isFreeze为true
            isFreeze = true;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (!isLocalPlay) {
            sensorManager.unregisterListener(this);
            setLandScapeRequestOrientation();
        }
        super.onStop();
    }

    private void updateDataPosition() {

        if (currentPlayPosition > 0) {
            lastVideoPosition.setPosition(currentPosition);
            videoPositionDBHelper.updateVideoPosition(lastVideoPosition);
        }
    }

    private void updateCompleteDataPosition() {
        lastVideoPosition.setPosition(0);
        videoPositionDBHelper.updateVideoPosition(lastVideoPosition);
    }

    @Override
    protected void onDestroy() {
        if (timerTask != null) {
            timerTask.cancel();
        }

        if (isAudio && isPrepared) {
            showNotification();
        }
        playerHandler.removeCallbacksAndMessages(null);
        playerHandler = null;

        alertHandler.removeCallbacksAndMessages(null);
        alertHandler = null;

        updateDataPosition();

        if (!isAudio) {
            demoApplication.releaseDWPlayer();
        }

//        if (player != null) {
//            player.reset();
//            player.release();
//            player = null;
//        }

        demoApplication.getDRMServer().disconnectCurrentStream();

        if (dialog != null) {
            dialog.dismiss();
        }
        if (!isLocalPlay) {
            networkInfoTimerTask.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        if (isPrepared) {
            // 刷新界面
            setLayoutVisibility(View.GONE, false);
            setLayoutVisibility(View.VISIBLE, true);
        }

        lockView.setSelected(false);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            rlBelow.setVisibility(View.VISIBLE);
            ivFullscreen.setImageResource(R.drawable.fullscreen_close);

            if (playChangeVideoPopupWindow != null) {
                playChangeVideoPopupWindow.dismiss();
            }

            if (playTopPopupWindow != null) {
                playTopPopupWindow.dismiss();
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            rlBelow.setVisibility(View.GONE);
            ivFullscreen.setImageResource(R.drawable.fullscreen_open);
        }

        setSurfaceViewLayout();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isLocalPlay) {
            toastInfo("播放完成！");
            finish();
            return;
        }

        if (isPrepared) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toastInfo("切换中，请稍候……");
                    currentPlayPosition = 0;
                    currentPosition = 0;
                    changeToNextVideo(true);
                }
            });
        }
    }
}
