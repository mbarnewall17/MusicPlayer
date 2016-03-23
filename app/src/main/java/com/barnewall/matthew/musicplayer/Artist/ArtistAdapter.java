package com.barnewall.matthew.musicplayer.Artist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.barnewall.matthew.musicplayer.R;

import java.util.ArrayList;

/**
 * Created by Matthew on 8/5/2015.
 */
public class ArtistAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<ArtistListViewItem> data;
    private LayoutInflater layoutInflater;

    public ArtistAdapter(ArrayList<ArtistListViewItem> items, Context context){
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
            convertView = layoutInflater.inflate(R.layout.artist_listview_item, parent, false);
        }

        //Get the views
        TextView name    = (TextView)    convertView.findViewById(R.id.artistNameTextview);
        TextView    extra   = (TextView)    convertView.findViewById(R.id.numberOfAlbumsTextView);

        //Set the views to correct values
        ArtistListViewItem lv = data.get(position);
        name.setText(lv.getName());
        name.setSelected(true);
        extra.setText(lv.getNumAlbums());

        return convertView;
    }

}
