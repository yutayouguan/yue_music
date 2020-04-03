package ml.yike.yueyin.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ml.yike.yueyin.R;
import ml.yike.yueyin.database.DBManager;
import ml.yike.yueyin.entity.MusicInfo;
import ml.yike.yueyin.service.MusicPlayerService;
import ml.yike.yueyin.util.ChineseToEnglish;
import ml.yike.yueyin.util.Constant;
import ml.yike.yueyin.util.MyMusicUtil;
import ml.yike.yueyin.view.RadarView;

public class ScanActivity extends BaseActivity {

    private DBManager dbManager;

    private Toolbar toolbar;

    /**
     * 扫描按钮
     */
    private Button scanButton;


    /*扫描动画*/

    private RadarView radarView;


    private TextView scanPathText;

    private TextView scanCountText;

    private CheckBox checkBox;

    /**
     * 自定义扫描控件
     */


    private Handler handler;

    private Message msg;

    /**
     * 显示的歌曲的数量
     */
    private int progress = 0;

    /**
     * 扫描的歌曲的数量
     */
    private int musicCount = 0;

    /**
     * 是否在扫描
     */
    private boolean isScanning = false;

    /**
     * 当前音乐id
     */
    private int currentMusicId;

    /**
     * 当前音乐路径
     */
    private String currentMusicPath;

    private List<MusicInfo> musicInfoList;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        dbManager = DBManager.getInstance(ScanActivity.this);
        scanButton = (Button) findViewById(R.id.start_scan_btn);
        toolbar = (Toolbar) findViewById(R.id.scan_music_toolbar);
        scanCountText = (TextView) findViewById(R.id.scan_count);
        scanPathText = (TextView) findViewById(R.id.scan_path);
        checkBox = (CheckBox) findViewById(R.id.scan_filter_cb);
        radarView = (RadarView) findViewById(R.id.radar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
                    scanPathText.setVisibility(View.INVISIBLE);
                    radarView.setVisibility(View.VISIBLE);
                    isScanning = true;
                    //扫描动画开始
                    radarView.start();
                    startScanLocalMusic();
                    scanButton.setText("停止扫描");

                } else {
                    scanPathText.setVisibility(View.GONE);
                    isScanning = false;
                    radarView.setVisibility(View.INVISIBLE);
                    scanCountText.setText("");
                    scanButton.setText("开始扫描");
                }
            }
        });


        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Constant.SCAN_NO_MUSIC:  //本地没有歌曲
                        Toast.makeText(ScanActivity.this, "本地没有歌曲，快去下载吧", Toast.LENGTH_SHORT).show();
                        scanComplete();
                        break;
                    case Constant.SCAN_ERROR:  //扫描出错
                        Toast.makeText(ScanActivity.this, "数据库错误", Toast.LENGTH_LONG).show();
                        scanComplete();
                        break;
                    case Constant.SCAN_COMPLETE:  //扫描完成
                        initCurrentPlaying();
                        scanComplete();
                        break;
                    case Constant.SCAN_UPDATE:  //动态更新歌曲数量
                        String path = msg.getData().getString("scanPath");
                        scanCountText.setText("已扫描到" + progress + "首歌曲");
                        scanPathText.setText(path);
                        break;
                }
            }
        };

    }


    /**
     * 扫描完成，设置控件
     */
    private void scanComplete() {
        scanButton.setText("完成");
        radarView.stop();
        isScanning = false;
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
                    ScanActivity.this.finish();
                }
            }
        });
       radarView.setVisibility(View.INVISIBLE);
    }


    /**
     * 开始扫描音乐
     */
    public void startScanLocalMusic() {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void run() {
                super.run();
                try {
                    String[] muiscInfoArray = new String[]{
                            MediaStore.Audio.Media.TITLE,               //歌曲名称
                            MediaStore.Audio.Media.ARTIST,              //歌曲歌手
                            MediaStore.Audio.Media.ALBUM,               //歌曲的专辑名
                            MediaStore.Audio.Media.DURATION,            //歌曲时长
                            MediaStore.Audio.Media.DATA};               //歌曲文件的全路径

                    Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            muiscInfoArray, null, null, null);  //内容提供器获取歌曲信息

                    if (cursor != null && cursor.getCount() != 0) {
                        musicInfoList = new ArrayList<MusicInfo>();
                        while (cursor.moveToNext()) {
                            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
                            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
                            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
                            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
                            String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));

                            if (checkBox.isChecked() && duration != null && Long.valueOf(duration) < 1000 * 60) {  //如果勾选checkBox
                                continue;
                            }

                            File file = new File(path);
                            String parentPath = file.getParentFile().getPath();

                            MusicInfo musicInfo = new MusicInfo();
                            musicInfo.setName(name);
                            musicInfo.setSinger(singer);
                            musicInfo.setAlbum(album);
                            musicInfo.setPath(path);
                            musicInfo.setParentPath(parentPath);
                            musicInfo.setFirstLetter(ChineseToEnglish.StringToPinyinSpecial(name).toUpperCase().charAt(0) + "");  //设置该歌曲拼音的第一个字母
                            musicInfoList.add(musicInfo);

                            progress++;
                            musicCount = cursor.getCount();
                            msg = new Message();    //每次都必须new，必须发送新对象，不然会报错
                            msg.what = Constant.SCAN_UPDATE;
                            msg.arg1 = musicCount;
                            handler.sendMessage(msg);  //更新UI界面
                            try {
                                sleep(200);  //每一次睡眠200ms，以免太快
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        //扫描完成获取一下当前播放音乐及路径
                        currentMusicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
                        currentMusicPath = dbManager.getMusicPath(currentMusicId);

                        Collections.sort(musicInfoList);  //将所有歌曲按照字母进行排序
                        dbManager.updateAllMusic(musicInfoList);

                        msg = new Message();
                        msg.what = Constant.SCAN_COMPLETE;  //扫描完成

                        handler.sendMessage(msg);  //更新UI界面
                    } else {  //如果扫描结果为空
                        msg = new Message();
                        msg.what = Constant.SCAN_NO_MUSIC;
                        handler.sendMessage(msg);  //更新UI界面
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    msg = new Message();
                    msg.what = Constant.SCAN_ERROR;  //扫描出现异常
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }


    /**
     * 初始化正在播放的音乐
     */
    private void initCurrentPlaying() {
        try {
            boolean isContain = false;  //扫描到的歌曲里面是否有当前播放的音乐
            int id = 1;
            if (musicInfoList != null) {
                for (MusicInfo musicInfo : musicInfoList) {
                    if (musicInfo.getPath().equals(currentMusicPath)) {
                        isContain = true;
                        id = musicInfoList.indexOf(musicInfo) + 1;
                    }
                }
            }
            if (isContain) {
                MyMusicUtil.setIntSharedPreference(Constant.KEY_ID, id);  //存储当前播放的歌曲id
            } else {
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                sendBroadcast(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }
}