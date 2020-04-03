package ml.yike.yueyin.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ml.yike.yueyin.R;
import ml.yike.yueyin.entity.SleepInfo;
import ml.yike.yueyin.util.MyMusicUtil;

import java.util.ArrayList;
import java.util.List;

public class SleepActivity extends BaseActivity {
    public static int SLEEP_SIZE = 11;

    private String[] sleepType = {"不开启", "10分钟", "20分钟","30分钟","","45分钟","60分钟","自定义"};
        

    private RecyclerView recyclerView;

    private SleepAdapter adapter;

    private Toolbar toolbar;

    private int selectSleep = 0;

    private List<SleepInfo> sleepInfoList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        selectSleep = MyMusicUtil.getTheme(SleepActivity.this);
        toolbar = (Toolbar) findViewById(R.id.sleep_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
        init();
    }


    private void init(){
        for (int i = 0; i < sleepType.length; i++){
            SleepInfo sleepInfo = new SleepInfo();
            sleepInfo.setName(sleepType[i]);
            sleepInfo.setSelect((selectSleep == i) ? true : false);
            if (i == sleepType.length-1){  //如果是最后一个夜间模式
                sleepInfo.setBackground(R.color.nightBg);  //设置成暗色
            }else {
                sleepInfo.setBackground(R.color.colorWhite);  //设置成亮色
            }
            sleepInfoList.add(sleepInfo);
        }
        recyclerView = (RecyclerView)findViewById(R.id.sleep_rv);
        adapter = new SleepAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(SleepActivity.this,HomeActivity.class);  //更改主题之后需要重新启动Activity
                startActivity(intent);
                break;
        }
        return true;
    }


    /**
     * 当点击实体键返回
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Intent intent = new Intent(SleepActivity.this,HomeActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    private class SleepAdapter extends RecyclerView.Adapter<SleepAdapter.ViewHolder>{

        public SleepAdapter() {}

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout relativeLayout;
            ImageView circleImage;
            TextView nameText;
            Button selectButton;

            public ViewHolder(View itemView) {
                super(itemView);
                this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.sleep_item_rl);
                this.circleImage = (ImageView) itemView.findViewById(R.id.sleep_iv);
                this.nameText = (TextView) itemView.findViewById(R.id.sleep_name_tv);
                this.selectButton = (Button) itemView.findViewById(R.id.sleep_select_tv);
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(SleepActivity.this).inflate(R.layout.change_sleep_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final SleepInfo sleepInfo = sleepInfoList.get(position);
            if (selectSleep == SLEEP_SIZE -1){  //如果是夜间模式
                holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_night);
                holder.selectButton.setBackgroundResource(R.drawable.shape_theme_btn_day);
            }else {  //日间模式
                holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_day);
                holder.selectButton.setBackgroundResource(R.drawable.shape_theme_btn_day);
            }
            holder.selectButton.setPadding(0,0,0,0);
            if (sleepInfo.isSelect()){
                holder.circleImage.setImageResource(R.drawable.tick);
                holder.selectButton.setText("使用中");
                holder.selectButton.setTextColor(getResources().getColor(sleepInfo.getColor()));  //设置文字颜色
            }else {
                holder.circleImage.setImageBitmap(null);
                holder.selectButton.setText("使用");
                holder.selectButton.setTextColor(getResources().getColor(R.color.grey500));
            }
            holder.circleImage.setBackgroundResource(sleepInfo.getColor());
            holder.nameText.setTextColor(getResources().getColor(sleepInfo.getColor()));
            holder.nameText.setText(sleepInfo.getName());
            holder.selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshSleep(sleepInfo,position);
                }
            });

            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshSleep(sleepInfo,position);
                }
            });
        }


        @Override
        public int getItemCount() {
            return sleepInfoList.size();
        }
    }


    /**
     * 更新主题
     */
    private void refreshSleep(SleepInfo sleepInfo, int position){
        if (position == (SLEEP_SIZE -1)){  //是夜间模式
            MyMusicUtil.setNightMode(SleepActivity.this,true);
        } else if(MyMusicUtil.getNightMode(SleepActivity.this)){
            MyMusicUtil.setNightMode(SleepActivity.this,false);
        }

        selectSleep = position;
        MyMusicUtil.setTheme(SleepActivity.this,position);

        //进行颜色的转换
        toolbar.setBackgroundColor(getResources().getColor(sleepInfo.getColor()));
        recyclerView.setBackgroundColor(getResources().getColor(sleepInfo.getBackground()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //状态栏颜色转换
            getWindow().setStatusBarColor(getResources().getColor(sleepInfo.getColor()));
        }
        for (SleepInfo info : sleepInfoList){
            if (info.getName().equals(sleepInfo.getName())){
                info.setSelect(true);  //设置被选中
            }else {
                info.setSelect(false);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
