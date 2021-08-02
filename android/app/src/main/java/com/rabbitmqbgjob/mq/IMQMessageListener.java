package com.rabbitmqbgjob.mq;

import javax.annotation.Nullable;

public interface IMQMessageListener {
    void onMessageReceived(byte[] byteArray );
    void onErrorOccurred(String message, @Nullable Throwable error);
}
