package ml.yike.yueyin.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.List;

import ml.yike.yueyin.R;
import ml.yike.yueyin.adapter.PlaylistAdapter;
import ml.yike.yueyin.database.DBManager;
import ml.yike.yueyin.entity.MusicInfo;
import ml.yike.yueyin.entity.PlayListInfo;
import ml.yike.yueyin.receiver.PlayerManagerReceiver;
import ml.yike.yueyin.service.MusicPlayerService;
import ml.yike.yueyin.util.Constant;
import ml.yike.yueyin.util.MyMusicUtil;
import ml.yike.yueyin.view.MusicPopMenuWindow;


public class PlaylistActivity extends PlayBarBaseActivity {

    private RecyclerView recyclerView;

    private PlaylistAdapter playlistAdapter;

    private List<MusicInfo> musicInfoList;

    private PlayListInfo playListInfo;

    private Toolbar toolbar;

    /**
     * 歌单内没有歌曲时显示的文字
     */
    private TextView noneText;

    private ImageView backgroundImage;

    private DBManager dbManager;

    private UpdateReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        loadBingPic();
        playListInfo = getIntent().getParcelableExtra("playlistInfo");
        toolbar = (Toolbar) findViewById(R.id.activity_playlist_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
        CollapsingToolbarLayout mCollapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsingToolbarLayout);
        mCollapsingToolbarLayout.setTitle(playListInfo.getName());
        dbManager = DBManager.getInstance(this);
        musicInfoList = dbManager.getMusicListByPlaylist(playListInfo.getId());
        initView();
        register();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void initView(){
        recyclerView = (RecyclerView)findViewById(R.id.activity_playlist_rv);
        playlistAdapter = new PlaylistAdapter(this,playListInfo,musicInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(playlistAdapter);
        noneText = (TextView)findViewById(R.id.activity_playlist_none_tv);
        if (playListInfo.getCount() == 0){
            recyclerView.setVisibility(View.GONE);
            noneText.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            noneText.setVisibility(View.GONE);
        }

        playlistAdapter.setOnItemClickListener(new PlaylistAdapter.OnItemClickListener() {
            @Override
            public void onOpenMenuClick(int position) {
                MusicInfo musicInfo = musicInfoList.get(position);
                showPopFormBottom(musicInfo);
            }

            @Override
            public void onDeleteMenuClick(View swipeView, int position) {
                MusicInfo musicInfo = musicInfoList.get(position);
                final int deleteId = musicInfo.getId();
                final int musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
                //从列表移除
                int ret = dbManager.removeMusicFromPlaylist(deleteId,playListInfo.getId());
                if (ret > 0){
//                   new SweetAlertDialog(PlaylistActivity.this,SweetAlertDialog.SUCCESS_TYPE).setTitleText("删除成功！").show();
                    Toast.makeText(PlaylistActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                }else {
//                    new SweetAlertDialog(PlaylistActivity.this,SweetAlertDialog.ERROR_TYPE).setTitleText("删除失败！").show();
                    Toast.makeText(PlaylistActivity.this,"删除失败",Toast.LENGTH_SHORT).show();
                }
                if (deleteId == musicId) {  //移除的是当前播放的音乐
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                    sendBroadcast(intent);
                }
                musicInfoList = dbManager.getMusicListByPlaylist(playListInfo.getId());
                playlistAdapter.updateMusicInfoList(musicInfoList);
                //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
                ((SwipeMenuLayout) swipeView).quickClose();
            }
        });

        // 当点击外部空白处时，关闭正在展开的侧滑菜单
        findViewById(R.id.activity_playlist).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    SwipeMenuLayout viewCache = SwipeMenuLayout.getViewCache();
                    if (null != viewCache) {
                        viewCache.smoothClose();
                    }
                }
                return false;
            }
        });

        backgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PlaylistActivity.this, "更换图片", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void showPopFormBottom(MusicInfo musicInfo) {
        MusicPopMenuWindow menuPopupWindow = new MusicPopMenuWindow(PlaylistActivity.this,musicInfo,findViewById(R.id.activity_playlist),Constant.ACTIVITY_MYLIST);
//      设置Popupwindow显示位置（从底部弹出）
        menuPopupWindow.showAtLocation(findViewById(R.id.activity_playlist), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = PlaylistActivity.this.getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha=0.7f;
        getWindow().setAttributes(params);

        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        menuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha=1f;
                getWindow().setAttributes(params);
            }
        });

        menuPopupWindow.setOnDeleteUpdateListener(new MusicPopMenuWindow.OnDeleteUpdateListener() {
            @Override
            public void onDeleteUpdate() {
                musicInfoList = dbManager.getMusicListByPlaylist(playListInfo.getId());
                playlistAdapter.updateMusicInfoList(musicInfoList);
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == android.R.id.home){
            this.finish();
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegister();
    }


    private void register() {
        try {
            if (mReceiver != null) {
                this.unRegister();
            }
            mReceiver = new UpdateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PlayerManagerReceiver.ACTION_UPDATE_UI_ADAPTER);
            this.registerReceiver(mReceiver, intentFilter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void unRegister() {
        try {
            if (mReceiver != null) {
                this.unregisterReceiver(mReceiver);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private void loadBingPic(){
        try {
            backgroundImage = (ImageView) findViewById(R.id.playlist_head_bg_iv);
            String bingPic = MyMusicUtil.getBingSharedPreference();
            if (bingPic != null) {
                Glide.with(this).load(bingPic).into(backgroundImage);
            } else {
                backgroundImage.setImageResource(R.drawable.listbg);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            playlistAdapter.notifyDataSetChanged();
        }
    }
}
