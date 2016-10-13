package com.barnewall.matthew.musicplayer.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.barnewall.matthew.musicplayer.MusicPlayerService;
import com.barnewall.matthew.musicplayer.NotificationManagement;

public class RemoteControlReceiver extends BroadcastReceiver {
    public RemoteControlReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                    context.sendBroadcast(new Intent(NotificationManagement.TOGGLE_PLAY_MUSIC));
                else if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT)
                    context.sendBroadcast(new Intent(NotificationManagement.NEXT_MUSIC));
                else if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                    context.sendBroadcast(new Intent(NotificationManagement.BACK_MUSIC));
            }
        }
    }
}
