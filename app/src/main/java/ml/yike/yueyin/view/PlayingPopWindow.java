package ml.yike.yueyin.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ml.yike.yueyin.R;
import ml.yike.yueyin.database.DBManager;
import ml.yike.yueyin.entity.MusicInfo;
import ml.yike.yueyin.service.MusicPlayerService;
import ml.yike.yueyin.util.Constant;
import ml.yike.yueyin.util.MyMusicUtil;

import java.util.List;


public class PlayingPopWindow extends PopupWindow{

    private View view;

    private Activity activity;

    private TextView countText;

    private RelativeLayout closeLayout;

    private RecyclerView recyclerView;

    private PlayingPopWindowAdapter playingPopWindowAdapter;

    private List<MusicInfo> musicInfoList;

    private DBManager dbManager;

    private RelativeLayout playModeLayout;

    private ImageView playModeImage;

    private TextView playModeText;

    public PlayingPopWindow(Activity activity) {
        super(activity);
        this.activity = activity;
        dbManager = DBManager.getInstance(activity);
        musicInfoList = MyMusicUtil.getCurrentPlayList(activity);
        initView();
    }


    private void initView(){
        this.view = LayoutInflater.from(activity).inflate(R.layout.playbar_menu_window, null);
        this.setContentView(this.view);
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        int height = (int)(size.y * 0.5);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(height);

        this.setFocusable(true);
        this.setOutsideTouchable(true);

        // 设置弹出窗体的背景
        this.setBackgroundDrawable(activity.getResources().getDrawable(R.color.colorWhite));
        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.pop_window_animation);

        // 添加OnTouchListener监听判断获取触屏位置，如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int height = view.getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.playing_list_rv);
        playingPopWindowAdapter = new PlayingPopWindowAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(playingPopWindowAdapter);

        closeLayout = (RelativeLayout) view.findViewById(R.id.playing_list_close_rv);
        playModeLayout = (RelativeLayout) view.findViewById(R.id.playing_list_playmode_rl);
        playModeImage = (ImageView) view.findViewById(R.id.playing_list_playmode_iv);
        playModeText = (TextView) view.findViewById(R.id.playing_list_playmode_tv);
        countText = (TextView)view.findViewById(R.id.playing_list_count_tv);

        initDefaultPlayModeView();

        //  顺序 --> 随机-- > 单曲
        playModeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int playMode = MyMusicUtil.getIntSharedPreference(Constant.KEY_MODE);
                switch (playMode) {
                    case Constant.PLAYMODE_SEQUENCE:
                        playModeText.setText(Constant.PLAYMODE_RANDOM_TEXT);
                        countText.setText("("+musicInfoList.size()+")");
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_MODE, Constant.PLAYMODE_RANDOM);
                        break;
                    case Constant.PLAYMODE_RANDOM:
                        countText.setText("");
                        playModeText.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_MODE, Constant.PLAYMODE_SINGLE_REPEAT);
                        break;
                    case Constant.PLAYMODE_SINGLE_REPEAT:

                        countText.setText("("+musicInfoList.size()+")");
                        playModeText.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_MODE, Constant.PLAYMODE_SEQUENCE);
                        break;
                }
                initPlayMode();
            }
        });

        closeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

}

    private void initDefaultPlayModeView() {
        int playMode = MyMusicUtil.getIntSharedPreference(Constant.KEY_MODE);
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                playModeText.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
                countText.setText("("+musicInfoList.size()+")");
                break;
            case Constant.PLAYMODE_RANDOM:
                playModeText.setText(Constant.PLAYMODE_RANDOM_TEXT);
                countText.setText("("+musicInfoList.size()+")");
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                playModeText.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
                countText.setText("");
                break;
        }
        initPlayMode();
    }


    private void initPlayMode() {
        int playMode = MyMusicUtil.getIntSharedPreference(Constant.KEY_MODE);
        if (playMode == -1) {
            playMode = 0;
        }
        playModeImage.setImageLevel(playMode);
    }


    private class PlayingPopWindowAdapter extends RecyclerView.Adapter<PlayingPopWindowAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder{
            LinearLayout contentLayout;
            TextView nameText;
            TextView singerText;

            public ViewHolder(View itemView) {
                super(itemView);
                this.contentLayout = (LinearLayout) itemView.findViewById(R.id.palybar_list_item_ll);
                this.nameText = (TextView) itemView.findViewById(R.id.palybar_list_item_name_tv);
                this.singerText = (TextView) itemView.findViewById(R.id.palybar_list_item_singer_tv);
            }
        }


        @Override
        public int getItemCount() {
            return musicInfoList.size();
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_playbar_rv_list,parent,false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder,final int position) {
            final MusicInfo musicInfo = musicInfoList.get(position);
            holder.nameText.setText(musicInfo.getName());
            holder.singerText.setText("-"+musicInfo.getSinger());

            if (musicInfo.getId() == MyMusicUtil.getIntSharedPreference(Constant.KEY_ID)){
                holder.nameText.setTextColor(activity.getResources().getColor(R.color.colorAccent));
                holder.singerText.setTextColor(activity.getResources().getColor(R.color.colorAccent));
            }else {
                holder.nameText.setTextColor(activity.getResources().getColor(R.color.grey700));
                holder.singerText.setTextColor(activity.getResources().getColor(R.color.grey500));
            }

            holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = dbManager.getMusicPath(musicInfo.getId());
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    intent.putExtra(Constant.KEY_PATH, path);
                    activity.sendBroadcast(intent);
                    MyMusicUtil.setIntSharedPreference(Constant.KEY_ID,musicInfo.getId());
                    notifyDataSetChanged();
                }
            });
        }
    }
}
