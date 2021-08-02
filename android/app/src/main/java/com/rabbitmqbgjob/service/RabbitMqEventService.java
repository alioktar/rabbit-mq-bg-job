package com.rabbitmqbgjob.service;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.jstasks.LinearCountingRetryPolicy;

public class RabbitMqEventService extends HeadlessJsTaskService {
    @Override
    protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        return new HeadlessJsTaskConfig(
            "RabbitMq",
            extras != null ? Arguments.fromBundle(extras) : Arguments.createMap(),
            5000, // timeout for the task
            true, // optional: defines whether or not  the task is allowed in foreground. Default is false
            new LinearCountingRetryPolicy(
                    3, // Max number of retry attempts
                    1000 // Delay between each retry attempt
            ));
    }
}