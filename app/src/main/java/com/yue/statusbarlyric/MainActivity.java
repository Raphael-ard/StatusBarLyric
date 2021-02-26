package com.yue.statusbarlyric;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
//x，y坐标
    private TextView xlabel;
    private TextView ylabel;
    private int xl = 0;
    private int yl = 0;
    private boolean voluntarily = false;
//    权限相关
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    public boolean isNotificationListenerEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
        return packageNames.contains(context.getPackageName());
    }
    public void openNotificationAccess() {
        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xlabel = findViewById(R.id.xedit);
        ylabel = findViewById(R.id.yedit);

        if (!load()) {
            ////////使用AlertDialog（弹出窗口信息），创建对象
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
            ////////设置标题
            alert.setTitle("提示信息！");
            alert.setIcon(R.drawable.ic_launcher_foreground);
            alert.setMessage("本应用需要使用通知权限，但不需要联网权限，不放心可以将网络权限关闭。\n本应用目前只适配网易云音乐。\n使用本应用需要将应用保留在后台。" +
                    "\n使用状态栏歌词只需点击“开启歌词悬浮窗”即可。\n使用前需要先点击两次“开启歌词悬浮窗按钮”，开启相应权限。\n" +
                    "未经允许不可传播本应用！！！\n未经允许不可传播本应用！！！\n未经允许不可传播本应用！！！\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t——————————龠");
            alert.setButton(AlertDialog.BUTTON_POSITIVE, "已阅", (dialog, which) -> { });
            alert.setCancelable(true);
            save();
            alert.show();
        }
    }

    public void stopFloatService(View view) {
        if (NotificationTask.isStarted) {
            Intent intent = new Intent("yueServicelyrics");
            intent.putExtra("judgelyric",false);
            sendBroadcast(intent);
            startService(new Intent(MainActivity.this,NotificationTask.class));
        }
        voluntarily = false;
        Toast.makeText(MainActivity.this, "已关闭FloatWindow", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Integer.toString(NotificationTask.xlast).equals("") && !Integer.toString(NotificationTask.ylast).equals("")) {
            while (voluntarily) {
                Intent intent = new Intent("yueServicelyrics");
                intent.putExtra("judgelyric",false);
                sendBroadcast(intent);
                startService(new Intent(MainActivity.this,NotificationTask.class));
                Intent intent1 = new Intent("yueServicelyrics");
                intent1.putExtra("judgelyric",true);
                intent1.putExtra("x",NotificationTask.xlast);
                intent1.putExtra("y",NotificationTask.ylast);
                startService(new Intent(MainActivity.this,NotificationTask.class));
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //授权，开启悬浮窗
    public void startFloatingService(View view) {
        if (!xlabel.getText().toString().equals("") && !ylabel.getText().toString().equals("")) {
            xl = Integer.parseInt(xlabel.getText().toString());
            yl = Integer.parseInt(ylabel.getText().toString());
        } else {
            xl = 0;
            yl = 0;
        }
        if (NotificationTask.isStarted) {
            this.stopFloatService(view);
            Intent intent = new Intent("yueServicelyrics");
            intent.putExtra("judgelyric",true);
            intent.putExtra("x",xl);
            intent.putExtra("y",yl);
            sendBroadcast(intent);
            startService(new Intent(MainActivity.this,NotificationTask.class));
            voluntarily = true;
        } else {
            if (isNotificationListenerEnabled(this)) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this,"当前无权限，请授权",Toast.LENGTH_SHORT).show();
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())), 0);
                } else {
                    Intent intent = new Intent("yueServicelyrics");
                    intent.putExtra("judgelyric",true);
                    intent.putExtra("x",xl);
                    intent.putExtra("y",yl);
                    sendBroadcast(intent);
                    startService(new Intent(MainActivity.this,NotificationTask.class));
                    voluntarily = true;
                }
            } else {
                openNotificationAccess();
            }
        }
        Toast.makeText(MainActivity.this, "已开启FloatWindow", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void helpinfo(View v) {
        ///////使用AlertDialog（弹出窗口信息），创建对象
        AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
        ////////设置标题
        alert.setTitle("提示信息！");
        alert.setIcon(R.drawable.ic_launcher_foreground);
        alert.setMessage("本应用需要使用通知权限，但不需要联网权限，不放心可以将网络权限关闭。\n本应用目前只适配网易云音乐。\n使用本应用需要将应用保留在后台。" +
                "\n使用状态栏歌词只需点击“开启歌词悬浮窗”即可。\n使用前需要先点击两次“开启歌词悬浮窗按钮”，开启相应权限。\n" +
                "未经允许不可传播本应用！！！\n未经允许不可传播本应用！！！\n未经允许不可传播本应用！！！\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t——————————龠");
        alert.setButton(AlertDialog.BUTTON_POSITIVE, "已阅", (dialog, which) -> { });
        alert.setCancelable(true);
        alert.show();
    }
    private void save() {
        @SuppressLint("WrongConstant")
        SharedPreferences editor = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor.edit().putBoolean("judge", true).apply();
    }
    private boolean load() {
        SharedPreferences editor = getSharedPreferences("data",Context.MODE_PRIVATE);
        return editor.getBoolean("judge",false);
    }
}