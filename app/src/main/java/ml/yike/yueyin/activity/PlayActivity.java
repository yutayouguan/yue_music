package ml.yike.yueyin.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

import me.zhengken.lyricview.LyricView;
import ml.yike.yueyin.R;
import ml.yike.yueyin.database.DBManager;
import ml.yike.yueyin.entity.MusicInfo;
import ml.yike.yueyin.fragment.PlayBarFragment;
import ml.yike.yueyin.receiver.PlayerManagerReceiver;
import ml.yike.yueyin.service.MusicPlayerService;
import ml.yike.yueyin.util.Constant;
import ml.yike.yueyin.util.CustomAttrValueUtil;
import ml.yike.yueyin.util.ImageUtils;
import ml.yike.yueyin.util.MyApplication;
import ml.yike.yueyin.util.MyMusicUtil;
import ml.yike.yueyin.view.PlayingPopWindow;

public class PlayActivity extends BaseActivity implements View.OnClickListener {

    private DBManager dbManager;
    /**
     * 返回按钮
     */
    private ImageView backImage;
    /**
     * 播放按钮
     */
    private ImageView playImage;
    /**
     * 歌单按钮
     */
    private ImageView menuImage;
    /**
     * 上一曲按钮
     */
    private ImageView preImage;
    /**
     * 下一曲按钮
     */
    private ImageView nextImage;
    /**
     * 播放模式按钮
     */
    private ImageView modeImage;
    /**
     * 可视化按钮
     */
    private ImageView vImage;

    /**
     * 播放时间
     */
    private TextView curTimeText;
    /**
     * 总时间
     */
    private TextView totalTimeText;
    /**
     * 歌曲名
     */
    private TextView musicNameText;
    /**
     * 歌手名
     */
    private TextView singerNameText;
    /**
     * 播放进度条
     */
    private SeekBar seekBar;

    /**
     * 播放接收器
     */
    private PlayReceiver mReceiver;

    /**
     * 歌曲进度条
     */
    private int mProgress;

    /**
     * 歌曲长度
     */
    private int duration;


    /**
     * 歌曲当前进度
     */
    private int current;

    /**
     * 多行歌词视图
     */

    private LyricView mLyricView;

    private ImageView mCover;
    private ImageView mCoverGauss;
    private ImageView mCoverMirror;
    private LinearLayout mDisplayLrc;

    private boolean displayLrc = false;//默认歌词不显示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setStyle();
        dbManager = DBManager.getInstance(PlayActivity.this);
        initView();
        register();
    }


    /**
     * 初始化控件
     */
    private void initView() {
        backImage = (ImageView) findViewById(R.id.iv_back);
        playImage = (ImageView) findViewById(R.id.iv_play);
        menuImage = (ImageView) findViewById(R.id.iv_menu);
        preImage = (ImageView) findViewById(R.id.iv_prev);
        nextImage = (ImageView) findViewById(R.id.iv_next);
        modeImage = (ImageView) findViewById(R.id.iv_mode);
        curTimeText = (TextView) findViewById(R.id.tv_current_time);
        totalTimeText = (TextView) findViewById(R.id.tv_total_time);
        musicNameText = (TextView) findViewById(R.id.tv_title);
        singerNameText = (TextView) findViewById(R.id.tv_artist);
        seekBar = (SeekBar) findViewById(R.id.activity_play_seekbar);
        mLyricView = (LyricView) findViewById(R.id.custom_lyric_view);
        mCover = (ImageView) findViewById(R.id.cover);
        mCoverGauss = (ImageView) findViewById(R.id.background_blur);
        mDisplayLrc = (LinearLayout) findViewById(R.id.linear_layout_music_cover);
        mCoverMirror = (ImageView) findViewById(R.id.cover_mirror);
        backImage.setOnClickListener(this);
        playImage.setOnClickListener(this);
        menuImage.setOnClickListener(this);
        preImage.setOnClickListener(this);
        nextImage.setOnClickListener(this);
        modeImage.setOnClickListener(this);
        mDisplayLrc.setOnClickListener(this);

        setSeekBarBackground();
        initPlayMode();
        initTitle();
        initPlayImage();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {  //滑动条进行改变
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
                if (musicId == -1) {
                    /* 启动后台播放服务*/
                    sendPlayBackground();
                    Toast.makeText(PlayActivity.this, "歌曲不存在", Toast.LENGTH_LONG).show();
                    return;
                }

                //发送播放请求
                sendPlay();
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mProgress = progress;
                initTimeAndLyricDisplay();

            }


        });

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_mode:
                switchPlayMode();
                break;
            case R.id.iv_play:  //点击中间的播放/暂停
                play();
                break;
            case R.id.iv_next:
                MyMusicUtil.playNextMusic(this);

                break;
            case R.id.iv_prev:
                MyMusicUtil.playPreMusic(this);
                break;
            case R.id.iv_menu:
                showPopFormBottom();
                break;
            case R.id.linear_layout_music_cover:
                convertLyricAndImage();
                break;
        }
    }

    /**
     * 点击专辑图片与歌词之间的显示
     */
    private void convertLyricAndImage() {
        if (displayLrc = !displayLrc) {
            mLyricView.setVisibility(View.VISIBLE);
            mCover.setVisibility(View.GONE);
            mCoverMirror.setVisibility(View.GONE);
        } else {
            mLyricView.setVisibility(View.GONE);
            mCover.setVisibility(View.VISIBLE);
            mCoverMirror.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化专辑图片
     */

    private void initAlbumImage() {
        int musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
        String mSongPath = dbManager.getMusicPath(musicId);
        MusicInfo musicInfo = new MusicInfo();
        Bitmap bitmap = musicInfo.getCover(mSongPath);
        if (bitmap != null) {
            updateCoverGauss(ImageUtils.fastblur(bitmap, 0.1f, 10));
            updateCover(bitmap);
            updateCoverMirror(ImageUtils.createReflectionBitmapForSingle(bitmap,
                    (int) MyApplication.getContext().getResources().getDimension(R.dimen.cover_width_height),
                    (int) MyApplication.getContext().getResources().getDimension(R.dimen.cover_mirror_height)));
        } else {
            updateCoverGauss(null);
            updateCover(null);
            updateCoverMirror(null);
        }
    }

    public void updateCoverMirror(Bitmap bitmap) {
        if (bitmap != null) {
            mCoverMirror.setImageBitmap(bitmap);
        } else {
            mCover.setImageDrawable(getResources().getDrawable(R.drawable.default_cover_mirror));
        }
    }

    public void updateCoverGauss(Bitmap bitmap) {
        if (bitmap != null) {
            mCoverGauss.setImageBitmap(bitmap);
        } else {
            mCoverGauss.setImageDrawable(getResources().getDrawable(R.drawable.default_cover_blur));
        }
    }

    public void updateCover(Bitmap bitmap) {
        if (bitmap != null) {
            mCover.setImageBitmap(bitmap);
        } else {
            mCover.setImageDrawable(getResources().getDrawable(R.drawable.default_cover));
        }
    }

    /**
     * 设置播放按钮icon
     */
    private void initPlayImage() {
        int status = PlayerManagerReceiver.status;
        switch (status) {
            case Constant.STATUS_STOP:
                playImage.setSelected(false);
                break;
            case Constant.STATUS_PLAY:
                playImage.setSelected(true);
                break;
            case Constant.STATUS_PAUSE:
                playImage.setSelected(false);
                break;
            case Constant.STATUS_RUN:
                playImage.setSelected(true);
                break;
        }
    }


    /**
     * 设置播放模式
     */
    private void initPlayMode() {
        int playMode = MyMusicUtil.getIntSharedPreference(Constant.KEY_MODE);
        if (playMode == -1) {
            playMode = 0;
        }
        modeImage.setImageLevel(playMode);
    }


    /**
     * 设置标题
     */
    private void initTitle() {
        int musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
        if (musicId == -1) {
            musicNameText.setText("悦音");
            singerNameText.setText("好音质");
        } else {
            musicNameText.setText(dbManager.getMusicInfo(musicId).get(1));
            singerNameText.setText(dbManager.getMusicInfo(musicId).get(2));
        }
    }


    /**
     * 设置歌曲的播放时间
     */
    private void initTimeAndLyricDisplay() {
        updateLrcView();
        curTimeText.setText(formatTime(current));
        totalTimeText.setText(formatTime(duration));
    }

    /**
     * 设置歌词显示
     */
    private void initLyric() {

        int musicId;
        musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
        String mSongPath = dbManager.getMusicPath(musicId);
        String lrcPath = mSongPath.substring(0, mSongPath.indexOf(".")) + ".lrc";
//        String lrcPath = mSongPath.substring(0, mSongPath.length() - 3) + "lrc";
        File lrcFile = new File(lrcPath);
        mLyricView.setLyricFile(lrcFile);// 设置歌词文件
        OnLrcViewPlayer();

    }

    /**
     * 设置歌词进度
     */
    public void updateLrcView() {
        mLyricView.setCurrentTimeMillis(current);
    }

    /**
     * 设置播放条进度
     */
    public void onProgressChanged(int progress) {
        if (progress != 0) {
            seekBar.setProgress(progress);
        }
    }

    /**
     * 指定到滑动歌词播放
     */
    public void OnLrcViewPlayer() {
        mLyricView.setOnPlayerClickListener(new LyricView.OnPlayerClickListener() {
            @Override
            public void onPlayerClicked(long progress, String s) {
                mProgress = (int) progress;
                onProgressChanged(mProgress);
                initTimeAndLyricDisplay();

                sendPlay();


            }

        });
    }

    /**
     * 格式化时间
     */
    private String formatTime(long time) {
        return formatTime("mm:ss", time);
    }

    /**
     * 发送播放请求
     */
    public void sendPlay() {
        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        intent.putExtra("cmd", Constant.COMMAND_PROGRESS);
        intent.putExtra("current", mProgress);
        sendBroadcast(intent);
    }

    /**
     * 发送后台播放请求
     */
    public void sendPlayBackground() {
        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        intent.putExtra("cmd", Constant.COMMAND_STOP);
        sendBroadcast(intent);
    }

    /**
     * 格式化时间函数
     */
    public static String formatTime(String pattern, long milli) {
        int m = (int) (milli / DateUtils.MINUTE_IN_MILLIS);
        int s = (int) ((milli / DateUtils.SECOND_IN_MILLIS) % 60);
        String mm = String.format(Locale.getDefault(), "%02d", m);
        String ss = String.format(Locale.getDefault(), "%02d", s);
        return pattern.replace("mm", mm).replace("ss", ss);
    }

    /**
     * 播放模式交换
     */
    private void switchPlayMode() {
        int playMode = MyMusicUtil.getIntSharedPreference(Constant.KEY_MODE);
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                MyMusicUtil.setIntSharedPreference(Constant.KEY_MODE, Constant.PLAYMODE_RANDOM);
                Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
                break;
            case Constant.PLAYMODE_RANDOM:
                MyMusicUtil.setIntSharedPreference(Constant.KEY_MODE, Constant.PLAYMODE_SINGLE_REPEAT);
                Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                MyMusicUtil.setIntSharedPreference(Constant.KEY_MODE, Constant.PLAYMODE_SEQUENCE);
                Toast.makeText(this, "列表循环", Toast.LENGTH_SHORT).show();
                break;
        }
        initPlayMode();
    }


    /**
     * 设置歌曲进度条背景
     */
    private void setSeekBarBackground() {
        try {
            int progressColor = CustomAttrValueUtil.getAttrColorValue(R.attr.colorPrimary, R.color.colorAccent, this);
            LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
            ScaleDrawable scaleDrawable = (ScaleDrawable) layerDrawable.findDrawableByLayerId(android.R.id.progress);
            GradientDrawable drawable = (GradientDrawable) scaleDrawable.getDrawable();
            drawable.setColor(progressColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void play() {

        int musicId;
        musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
        if (musicId == -1 || musicId == 0) {
            musicId = dbManager.getFirstId(Constant.LIST_ALLMUSIC);
            Intent intent = new Intent(Constant.MP_FILTER);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
            sendBroadcast(intent);
            Toast.makeText(PlayActivity.this, "歌曲不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if (PlayerManagerReceiver.status == Constant.STATUS_PAUSE) {  //暂停-播放
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
            sendBroadcast(intent);
        } else if (PlayerManagerReceiver.status == Constant.STATUS_PLAY) {  //播放-暂停
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
            sendBroadcast(intent);
        } else {  //停止-播放
            String path = dbManager.getMusicPath(musicId);
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
            intent.putExtra(Constant.KEY_PATH, path);
            sendBroadcast(intent);
        }

    }

    /**
     * 显示操作菜单
     */
    public void showPopFormBottom() {
        PlayingPopWindow playingPopWindow = new PlayingPopWindow(PlayActivity.this);
        playingPopWindow.showAtLocation(findViewById(R.id.activity_play), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.7f;
        getWindow().setAttributes(params);

        playingPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });
    }


    private void register() {
        mReceiver = new PlayReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayBarFragment.ACTION_UPDATE_UI_PlAYBAR);
        registerReceiver(mReceiver, intentFilter);
    }


    private void unRegister() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegister();

    }


    class PlayReceiver extends BroadcastReceiver {
        int status;

        /**
         * 接收广播：ACTION_UPDATE_UI_PlAYBAR
         */
        @Override
        public void onReceive(Context context, Intent intent) {  //根据PlayBar的状态来设置图片
            initTitle(); //初始化歌曲名称与歌手
            initAlbumImage();//初始化专辑图片
            initLyric();//初始化歌词
            status = intent.getIntExtra(Constant.STATUS, 0);
            current = intent.getIntExtra(Constant.KEY_CURRENT, 0);
            duration = intent.getIntExtra(Constant.KEY_DURATION, 100);
            switch (status) {  //根据状态来切换图片以及seekBar
                case Constant.STATUS_STOP:
                    playImage.setSelected(false);
                    break;
                case Constant.STATUS_PLAY:
                    playImage.setSelected(true);
                    break;
                case Constant.STATUS_PAUSE:
                    playImage.setSelected(false);
                    break;
                case Constant.STATUS_RUN:
                    playImage.setSelected(true);
                    seekBar.setMax(duration);
                    seekBar.setProgress(current);
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 设置顶部状态栏
     */
    private void setStyle() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
