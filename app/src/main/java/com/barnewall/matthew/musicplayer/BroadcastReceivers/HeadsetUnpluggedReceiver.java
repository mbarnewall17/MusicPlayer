package com.barnewall.matthew.musicplayer.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.barnewall.matthew.musicplayer.GlobalFunctions;
import com.barnewall.matthew.musicplayer.MusicPlayerService;
import com.barnewall.matthew.musicplayer.NotificationManagement;
import com.barnewall.matthew.musicplayer.QueueListView;
import com.barnewall.matthew.musicplayer.R;
import com.barnewall.matthew.musicplayer.Song.NowPlayingAdapter;

public class HeadsetUnpluggedReceiver extends BroadcastReceiver {
    public HeadsetUnpluggedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(GlobalFunctions.isServiceRunning(context, MusicPlayerService.class)){
            AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if(!manager.isWiredHeadsetOn())
                context.sendBroadcast(new Intent(NotificationManagement.TOGGLE_PLAY_MUSIC));
        }
    }
}
