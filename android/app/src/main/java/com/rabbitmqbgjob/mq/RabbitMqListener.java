package com.rabbitmqbgjob.mq;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmqbgjob.helper.NetworkUtil;

import javax.annotation.Nullable;

public class RabbitMqListener {
    private String host;
    private String username;
    private String password;
    private String exchangeName;
    private String type;
    private IMQMessageListener messageListener;

    private @Nullable Context context;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;

    private Handler messageHandler = new Handler();
    private Handler errorHandler = new Handler();

    private byte[] lastMessage = null;
    private String errorMessage = null;
    private Throwable error= null;

    private Runnable messageRunnable = new Runnable() {
        @Override
        public void run() {
            messageListener.onMessageReceived(lastMessage);
        }
    };

    private Runnable errorRunnable = new Runnable() {
        @Override
        public void run() {
            messageListener.onErrorOccurred(errorMessage,error);
        }
    };

    public RabbitMqListener(String host, String username, String password, String exchangeName, String type, IMQMessageListener messageListener) {
        System.out.println(host+ username+ password+ exchangeName+ type);
        this.host = host;
        this.username = username;
        this.password = password;
        this.exchangeName = exchangeName;
        this.type = type;
        this.messageListener = messageListener;
    }

    public boolean createConnectionFactory(){
        try {
            connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(this.host);
            connectionFactory.setVirtualHost("/");
            connectionFactory.setUsername(this.username);
            connectionFactory.setPassword(this.password);
            return true;
        }catch (Exception e){
            Log.e("ConnectionError", e.getMessage());
            sendError("Error While Starting Consumer!", e);
        }
        return false;
    }

    public boolean connectMqServer(Context context){
        try {
            System.out.println("connectMqServer");
            this.context = context;
            if (createConnectionFactory()){
                System.out.println("consume");
                startConsuming();
                return true;
            }
        }catch (Exception e){
            sendError("Error While Starting Consumer!", e);
        }
        return false;
    }

    private Thread thread;
    private void startConsuming(){
        thread = new Thread(
            () -> {
                try{
                    System.out.println("startConsuming");
                    while (NetworkUtil.getConnectivityStatus(context)==1){
                        if (connection == null){
                            connection = connectionFactory.newConnection();
                            channel = connection.createChannel();
                            channel.exchangeDeclare(exchangeName,type);
                            String queueName = channel.queueDeclare().getQueue();
                            System.out.println(queueName);
                            channel.queueBind(queueName,exchangeName,"");
                            channel.basicConsume(queueName, true, (consumerTag, message) ->{
                                lastMessage = message.getBody();
                                messageHandler.post(messageRunnable);
                            }, (consumerTag) ->{Log.e("RabbitMQ", consumerTag);});
                        }
                    }
                }catch (Exception e){
                    Log.e("RabbitMQ", "Error", e);
                }
            }
        );
        thread.start();
    }

    private void sendError(String message, Exception e) {
        error = e;
        errorMessage=message;
        errorHandler.post(errorRunnable);
    }

    public void destroy() {
        try {
            connection.close();
            channel.abort();
            thread.stop();
        } catch (Exception e) {
            Log.e("RabbitMQ", "Error On Destroy", e);
        }
    }
}
