package com.yue.statusbarlyric;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.media.app.NotificationCompat;


import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE;

public class NotificationTask  extends NotificationListenerService {
    public static int xlast = 0;
    public static int ylast = 0;
    public static float si = 12;
//歌词坐标，判断歌词是否显示出来
    private int xlabel = 0;
    private String str = "Nothing";
    private String beginstr = "";
    private int ylabel = 0;
    private float siz = 12;
    private int num = 0;
    private boolean judgelyric = false;
    private boolean startservice = false;
//    接收上述几个参数的receiver
    private receiverlyrics receiverlyrics;
    private TextView txt;
    public static boolean isStarted = false;
//悬浮窗
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate() {
        txt = new TextView(getApplicationContext());
        txt.setText(str);
        //接收停止接收歌词信息通知广播
        receiverlyrics = new receiverlyrics();
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("yueServicelyrics");
        registerReceiver(receiverlyrics,intentFilter1);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        System.out.println("Destroy");
        this.unregisterReceiver(receiverlyrics);
        stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (judgelyric) {
                //获取歌词信息
                str = metadata.getString(METADATA_KEY_TITLE);
                txt.setText(metadata.getString(METADATA_KEY_TITLE));
//            防止死循环,迭代，不断发送请求，进行歌词更新（但是会极大占用内存）
                if (str.equals(beginstr)) {
                    if (num <= 10) {
                        num++;
                        request();
                    } else {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        request();
                    }
                } else {
                    num = 0;
                    beginstr = str;
                }
                System.out.println(str);
            }
        }
    };

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        request();
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (startservice) {
//            以悬浮窗形式
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            layoutParams = new WindowManager.LayoutParams();
            //根据安卓版本进行悬浮窗选择
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            //透明
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN ;
            //初始位置
            layoutParams.width = 1080;
            layoutParams.height = 100;
            layoutParams.x = xlabel;
            layoutParams.y = ylabel;
            if (judgelyric) {
                isStarted = true;
                showFloatWindow();
                request();
                Toast.makeText(NotificationTask.this, "Start", Toast.LENGTH_SHORT).show();
            } else {
                isStarted = false;
                closeFloatWindow();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void request() {
        for (StatusBarNotification sbn : getActiveNotifications()) {
            if (sbn.getPackageName().equals("com.netease.cloudmusic")) {
                MediaSessionCompat.Token sessiontoken = NotificationCompat.MediaStyle.getMediaSession(sbn.getNotification());
                if (sessiontoken != null) {
                    try {
                        MediaControllerCompat mediaControllerCompat = new MediaControllerCompat(NotificationTask.this,sessiontoken);
                        mediaControllerCompat.registerCallback(mMediaControllerCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

//    通知出现变化
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        request();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showFloatWindow() {
        if (Settings.canDrawOverlays(this)) {
            txt.setTextColor(Color.BLUE);
            txt.setTextSize(siz);
            windowManager.addView(txt,layoutParams);
            txt.setOnTouchListener(new FloatingOntouchListener());
        }
    }

    private void closeFloatWindow() {
        if (Settings.canDrawOverlays(this)) {
            if (!Integer.toString(layoutParams.x).equals("") && !Integer.toString(layoutParams.y).equals("")) {
                xlast = layoutParams.x;
                ylast = layoutParams.y;
                si = siz;
            } else {
                si = 12;
                xlast = 0;
                ylast = 0;
            }
            windowManager.removeView(txt);
        }
    }

    private class FloatingOntouchListener implements View.OnTouchListener {
        private int x;
        private int y;
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
//                    更新悬浮窗布局
                    windowManager.updateViewLayout(v, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private class receiverlyrics extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            judgelyric = intent.getBooleanExtra("judgelyric",false);
            startservice = intent.getBooleanExtra("startservice",false);
//            x，y坐标
            xlabel = intent.getIntExtra("x",0);
            ylabel = intent.getIntExtra("y",0);
            siz = intent.getFloatExtra("s",12);
        }
    }
}
