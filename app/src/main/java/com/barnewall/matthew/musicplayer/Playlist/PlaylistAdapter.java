package com.barnewall.matthew.musicplayer.Playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.barnewall.matthew.musicplayer.Genre.GenreListViewItem;
import com.barnewall.matthew.musicplayer.R;

import java.util.ArrayList;

/**
 * Created by Matthew on 8/5/2015.
 */
public class PlaylistAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PlaylistListViewItem> data;
    private LayoutInflater layoutInflater;

    public PlaylistAdapter(ArrayList<PlaylistListViewItem> items, Context context) {
        this.context = context;
        this.data = items;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = layoutInflater.inflate(R.layout.playlist_listview_item, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.playlistNameTextview);

        PlaylistListViewItem lv = data.get(position);
        name.setText(lv.getName());
        name.setSelected(true);

        convertView.findViewById(R.id.popMenuButton).setTag(position);

        return convertView;
    }
}


