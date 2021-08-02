package com.rabbitmqbgjob.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("onReceive -> Action receiver");
        String action=intent.getStringExtra("testAction");
        if(action.equals("TestActionName")){
            testAction();
        }
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public void testAction(){
        System.out.println("Action Worked!");
    }
}
