package com.barnewall.matthew.musicplayer.Album;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.barnewall.matthew.musicplayer.R;

import java.util.ArrayList;

/**
 * Created by Matthew on 8/5/2015.
 */
public class AlbumAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<AlbumListViewItem> data;
    private LayoutInflater layoutInflater;

    public AlbumAdapter(ArrayList<AlbumListViewItem> items, Context context){
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
        //Create the view if it does not exist
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.album_listview_item, parent, false);
        }

        //Get the views
        TextView    name    = (TextView)    convertView.findViewById(R.id.albumNameTextview);
        TextView    extra   = (TextView)    convertView.findViewById(R.id.artistNameAndNumberOfSongsTextView);
        ImageView   art     = (ImageView)   convertView.findViewById(R.id.artworkImageView);

        //Set the views to correct values
        AlbumListViewItem lv = data.get(position);
        name.setText(lv.getTitle());
        name.setSelected(true);
        String extraText = lv.getArtistName();
        if(lv.getYear() != null){
            extraText = extraText + "(" + lv.getYear() + ")";
        }
        extra.setText(extraText);
        art.setImageBitmap(lv.getAlbumArt());

        convertView.findViewById(R.id.popMenuButton).setTag(position);

        return convertView;
    }
}
