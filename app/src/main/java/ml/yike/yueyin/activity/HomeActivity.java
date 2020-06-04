package ml.yike.yueyin.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ml.yike.yueyin.R;
import ml.yike.yueyin.adapter.HomeListViewAdapter;
import ml.yike.yueyin.database.DBManager;
import ml.yike.yueyin.entity.PlayListInfo;
import ml.yike.yueyin.service.MusicPlayerService;
import ml.yike.yueyin.util.Constant;
import ml.yike.yueyin.util.HttpUtil;
import ml.yike.yueyin.util.MyApplication;
import ml.yike.yueyin.util.MyMusicUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class HomeActivity extends PlayBarBaseActivity {

    /**
     * 判断app是否是第一次启动
     */
    private boolean isFirst = true;

    private DBManager dbManager;

    private DrawerLayout drawerLayout;

    private NavigationView navView;

    private ImageView navHeadImage;

    private LinearLayout localMusicLayout;

    private LinearLayout lastPlayLayout;

    private LinearLayout myLoveLayout;

    private LinearLayout myListTitleLayout;

    private Toolbar toolbar;

    private TextView localMusicCountText;

    private TextView lastPlayCountText;

    private TextView myLoveCountText;

    private TextView myPlayListCountText;

    /**
     * 添加歌单+
     */
    private ImageView myPlayListAddImage;

    /**
     * 歌单列表view
     */
    private ListView listView;

    private HomeListViewAdapter adapter;

    /**
     * 歌单列表
     */
    private List<PlayListInfo> playListInfo;

    private int count;

    private long exitTime = 0;

    private boolean isStartTheme = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setStyle();
        isFirst = MyMusicUtil.getIsFirst();
        Log.d("isFirst!!!!", String.valueOf(isFirst));
        if (isFirst == true) {  //app是第一次启动
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("yueMusic");
            dialog.setMessage("第一次启动yueMusic，是否先扫描本地音乐");
            dialog.setCancelable(true);
            dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {  //跳转到扫描activity
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
                    startActivity(intent);
                }
            });
            dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {  //直接消失
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
            isFirst = false;
            MyMusicUtil.setIsFirst(isFirst);  //存储状态
        }

        dbManager = DBManager.getInstance(HomeActivity.this);
        /*设置工具栏图标*/
        toolbar = (Toolbar) findViewById(R.id.home_activity_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.drawer_menu);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        navHeadImage = (ImageView) headerView.findViewById(R.id.nav_head_bg_iv);
        loadBingPic();

        refreshNightModeTitleAndIcon();

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_sleep:  //定时关闭
                        scheduleQuit();
                        break;
                    case R.id.nav_night_mode:  //夜间模式
                        if (MyMusicUtil.getNightMode(HomeActivity.this)) {   //当前为夜间模式，则恢复之前的主题
                            MyMusicUtil.setNightMode(HomeActivity.this, false);  //点击之后是日间模式
                            int preTheme = MyMusicUtil.getPreTheme(HomeActivity.this);  //得到夜间模式之前的日间模式的颜色主题
                            MyMusicUtil.setTheme(HomeActivity.this, preTheme);
                        } else {  //当前为日间模式，则切换到夜间模式
                            MyMusicUtil.setNightMode(HomeActivity.this, true);
                            MyMusicUtil.setTheme(HomeActivity.this, ThemeActivity.THEME_SIZE - 1);
                        }
                        recreate();  //重新加载之后才能切换成功
                        refreshNightModeTitleAndIcon();
                        break;
                    case R.id.nav_about_me:  //关于
                        Intent aboutTheme = new Intent(HomeActivity.this, AboutActivity.class);
                        startActivity(aboutTheme);
                        break;
                    case R.id.nav_logout:  //退出
                        quit();
                        break;
                }
                return true;
            }
        });

        init();

        Intent startIntent = new Intent(HomeActivity.this, MusicPlayerService.class);  //开启后台服务
        startService(startIntent);

    }

    private void scheduleQuit() {
        Toast.makeText(getApplicationContext(),"悦音将在30分钟后退出",Toast.LENGTH_LONG).show();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                quit();
            }
        };
        timer.schedule(task, 1800000); //设定30分钟后退出应用


    }

    public void quit() {
        finish();
        Intent intentBroadcast = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        intentBroadcast.putExtra(Constant.COMMAND, Constant.COMMAND_RELEASE);
        sendBroadcast(intentBroadcast);
        Intent stopIntent = new Intent(HomeActivity.this, MusicPlayerService.class);
        stopService(stopIntent);
    }

    /**
     * 滑动窗口显示的文字与icon
     */
    private void refreshNightModeTitleAndIcon() {
        if (MyMusicUtil.getNightMode(HomeActivity.this)) {
            navView.getMenu().findItem(R.id.nav_night_mode).setTitle("日间模式");
            navView.getMenu().findItem(R.id.nav_night_mode).setIcon(R.drawable.nav_day);
        } else {
            navView.getMenu().findItem(R.id.nav_night_mode).setTitle("夜间模式");
            navView.getMenu().findItem(R.id.nav_night_mode).setIcon(R.drawable.nav_night);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        count = dbManager.getMusicCount(Constant.LIST_ALLMUSIC);
        localMusicCountText.setText(count + "");
        count = dbManager.getMusicCount(Constant.LIST_LASTPLAY);
        lastPlayCountText.setText(count + "");
        count = dbManager.getMusicCount(Constant.LIST_MYLOVE);
        myLoveCountText.setText(count + "");
        count = dbManager.getMusicCount(Constant.LIST_MYPLAY);
        myPlayListCountText.setText("(" + count + ")");
        adapter.updateDataList();
    }

    /**
     * 初始化控件
     */
    private void init() {
        localMusicLayout = (LinearLayout) findViewById(R.id.home_local_music_ll);
        lastPlayLayout = (LinearLayout) findViewById(R.id.home_recently_music_ll);
        myLoveLayout = (LinearLayout) findViewById(R.id.home_my_love_music_ll);
        myListTitleLayout = (LinearLayout) findViewById(R.id.home_my_list_title_ll);
        listView = (ListView) findViewById(R.id.home_my_list_lv);
        localMusicCountText = (TextView) findViewById(R.id.home_local_music_count_tv);
        lastPlayCountText = (TextView) findViewById(R.id.home_recently_music_count_tv);
        myLoveCountText = (TextView) findViewById(R.id.home_my_love_music_count_tv);
        myPlayListCountText = (TextView) findViewById(R.id.home_my_list_count_tv);
        myPlayListAddImage = (ImageView) findViewById(R.id.home_my_pl_add_iv);

        localMusicLayout.setOnClickListener(new View.OnClickListener() {  //本地音乐
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LocalMusicActivity.class);
                startActivity(intent);
            }
        });

        lastPlayLayout.setOnClickListener(new View.OnClickListener() {  //最近音乐
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LastMyloveActivity.class);
                intent.putExtra(Constant.LABEL, Constant.LABEL_LAST);
                startActivity(intent);
            }
        });

        myLoveLayout.setOnClickListener(new View.OnClickListener() {  //我的喜爱
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LastMyloveActivity.class);
                intent.putExtra(Constant.LABEL, Constant.LABEL_MYLOVE);
                startActivity(intent);
            }
        });

        playListInfo = dbManager.getMyPlayList();
        adapter = new HomeListViewAdapter(playListInfo, this, dbManager);
        listView.setAdapter(adapter);
        myPlayListAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.dialog_create_playlist, null);
                final EditText playlistEdit = (EditText) view.findViewById(R.id.dialog_playlist_name_et);
                builder.setView(view);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = playlistEdit.getText().toString();
                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(HomeActivity.this, "请输入歌单名", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dbManager.createPlaylist(name);
                        dialog.dismiss();
                        adapter.updateDataList();
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();//配置好后再builder show
            }
        });

    }


    /**
     * 更新歌单的数量
     */
    public void updatePlaylistCount() {
        count = dbManager.getMusicCount(Constant.LIST_MYPLAY);
        myPlayListCountText.setText("(" + count + ")");
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (isStartTheme) {  //如果去设置主题
            HomeActivity.this.finish();  //让活动结束，否则主题切换不成功
        }
        isStartTheme = false;
    }

    /**
     * 设置双次返回
     */
    @SuppressLint("WrongConstant")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                drawerLayout.closeDrawer(Gravity.START);
                Toast.makeText(getApplicationContext(), "再按一次切换到桌面", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                moveTaskToBack(true);
            }
            return true;
        }
        finish();
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 加载滑动窗口上方的图像
     */
    private void loadBingPic() {
        HttpUtil.sendOkHttpRequest(HttpUtil.requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String bingPic = response.body().string();
                    MyMusicUtil.setBingSharedPreference(bingPic);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MyApplication.getContext()).load(bingPic).into(navHeadImage);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    navHeadImage.setImageResource(R.drawable.bg_playlist);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                navHeadImage.setImageResource(R.drawable.bg_playlist);
            }
        });
        navHeadImage.setImageResource(R.drawable.bg_playlist);
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


