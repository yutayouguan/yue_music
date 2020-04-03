package ml.yike.yueyin.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import ml.yike.yueyin.receiver.PlayerManagerReceiver;

/**
 * 播放服务
 * 主要目的是向后台发送广播
 * 在HomeActivity就开启服务
 */
public class MusicPlayerService extends Service {

    public static final String PLAYER_MANAGER_ACTION = "ml.yike.yueyin.service.MusicPlayerService.player.action";

    private PlayerManagerReceiver mReceiver;


    public MusicPlayerService() {}


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        register();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
    }


    private void register() {
        mReceiver = new PlayerManagerReceiver(MusicPlayerService.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAYER_MANAGER_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }


    private void unRegister() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
}

