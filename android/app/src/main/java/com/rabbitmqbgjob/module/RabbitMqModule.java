package com.rabbitmqbgjob.module;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.rabbitmqbgjob.service.RabbitMqService;

import java.util.HashMap;

import javax.annotation.Nonnull;

public class RabbitMqModule extends ReactContextBaseJavaModule {

    public static final String REACT_CLASS = "RabbitMq";
    private static Intent service;

    public RabbitMqModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.service = new Intent(reactContext, RabbitMqService.class);
    }

    @Nonnull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void startMq(String mqHost, String mqUsername, String mqPassword, String exchangeName, String type) {
        System.out.println(REACT_CLASS + " started! -> " + mqHost + " - " + mqPassword + " - " + mqUsername + " - " + exchangeName + " - " + type);
        HashMap hashMap = new HashMap<String,String>();
        hashMap.put("mqHost",mqHost);
        hashMap.put("mqUsername",mqUsername);
        hashMap.put("mqPassword",mqPassword);
        hashMap.put("exchangeName",exchangeName);
        hashMap.put("type",type);
        getReactApplicationContext().startService(this.service.putExtra("config", hashMap));
    }

    @ReactMethod
    public void stopService() {
        System.out.println(REACT_CLASS + " stopped!");
        getReactApplicationContext().stopService(this.service);
    }
}
