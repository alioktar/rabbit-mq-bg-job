package com.rabbitmqbgjob.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.os.Build;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.rabbitmqbgjob.MainActivity;
import com.rabbitmqbgjob.R;
import com.rabbitmqbgjob.mq.IMQMessageListener;
import com.rabbitmqbgjob.mq.RabbitMqListener;
import com.rabbitmqbgjob.receiver.ActionReceiver;

import java.nio.charset.Charset;
import java.util.HashMap;

import javax.annotation.Nullable;

public class RabbitMqService extends Service implements IMQMessageListener {

    private static final int SERVICE_NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "BACKGROUNDJOBS";

    HashMap mqConfig = null;
    HashMap mqError = null;
    private String mqMessage = null;

    private RabbitMqListener rabbitMQConsumer = null;

    private Handler handler = new Handler();
    private Thread thread = null;
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            Context context = getApplicationContext();
            Intent intent = new Intent(context, RabbitMqEventService.class);
            if (mqMessage != null){
                intent.putExtra("mqMessage",mqMessage);
                mqMessage=null;
            }else if (mqError != null){
                intent.putExtra("mqError", mqError);
                mqError=null;
            }
            context.startService(intent);
            HeadlessJsTaskService.acquireWakeLockNow(context);
            handler.postDelayed(this, 1000);
        }
    };

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "BACKGROUNDJOBS", importance);
            channel.setDescription("RABBIT MQ BACKGROUND JOBS NOTIFICATION CHANNEL");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        rabbitMQConsumer.destroy();
        rabbitMQConsumer = null;
        thread.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setMqConfig(intent);
        System.out.println("on start command");
        connectMq();
        thread = new Thread(this.runnableCode);
        thread.start();
        createNotificationChannel();
        showNotification();
        return START_STICKY;
    }

    private void setMqConfig(Intent intent) {
        System.out.println(intent.toString());
        Bundle extras = intent.getExtras();
        System.out.println("setMqConfig");
        if (extras != null) {
            mqConfig = (HashMap<String, String>) extras.get("config");
            try {
                rabbitMQConsumer = new RabbitMqListener(
                        (String) mqConfig.get("mqHost"),
                        (String) mqConfig.get("mqUsername"),
                        (String) mqConfig.get("mqPassword"),
                        (String) mqConfig.get("exchangeName"),
                        (String) mqConfig.get("type"),
                        this
                );
            } catch (Exception e) {
                Log.e("Error", "Error Starting RabbitMq Listener : " + e);
            }
        }
    }

    private void connectMq() {
        try {
            System.out.println("connect mq");
            rabbitMQConsumer.connectMqServer(getApplicationContext());
        } catch (Exception e) {
            Log.e("Error","RabbitMq Connection Errror : " + e);
        }
    }

    private void showNotification(){
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("RabbitMq Listening")
                .setContentText("RabbitMq Connection...")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();
        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    private void showNotificationWithButton(){
        Intent notificationIntent = new Intent(getApplicationContext(), ActionReceiver.class);
        notificationIntent.setAction("YES");
        notificationIntent.putExtra("testAction","TestActionName");
        PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Background Job Service")
                .setContentText("Running...")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .addAction(R.mipmap.ic_launcher_round,"YES",contentIntent)
                .build();
        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    @Override
    public void onMessageReceived(byte[] byteArray) {
        this.mqMessage = new String(byteArray, Charset.forName("UTF-8"));
        System.out.println(mqMessage);
    }

    @Override
    public void onErrorOccurred(String message, @Nullable Throwable error) {
        this.mqError = new HashMap();
        this.mqError.put("message",message);
        this.mqError.put("error",error);
    }
}
