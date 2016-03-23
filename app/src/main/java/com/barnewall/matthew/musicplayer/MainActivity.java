package com.barnewall.matthew.musicplayer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.app.Fragment;
import android.nfc.Tag;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.barnewall.matthew.musicplayer.Album.AlbumFragment;
import com.barnewall.matthew.musicplayer.Album.AlbumListViewItem;
import com.barnewall.matthew.musicplayer.Artist.ArtistFragment;
import com.barnewall.matthew.musicplayer.Artist.ArtistListViewItem;
import com.barnewall.matthew.musicplayer.Genre.GenreFragment;
import com.barnewall.matthew.musicplayer.Genre.GenreListViewItem;
import com.barnewall.matthew.musicplayer.Playlist.PlaylistFragment;
import com.barnewall.matthew.musicplayer.Song.SongFragment;
import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements
        PlaybackFragment.OnFragmentInteractionListener,
        MusicFragment.OnFragmentInteractionListener,
        NewSongListener {

    // Instance Variables
    private DrawerLayout                drawerLayout;
    private ListView                    drawerListView;
    private ActionBarDrawerToggle       actionBarDrawerToggle;
    private MusicCategories             selectedCategory;
    private View                        selectedCategoryView;
    private boolean                     playbackShowing;
    private String[]                    where;
    private MusicCategories             whereCategory;
    private MediaPlayerManager          manager;
    private IBinder                     service;
    private ServiceConnection           connection;

    public enum MusicCategories{
        ALBUMS,ARTISTS,PLAYLISTS,SONGS,GENRES,FOLDERS,SETTINGS
    }

    public interface PlayAndPauseListener{
        public void pause();
        public void play();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the navigation drawer
        setUpNavigationDrawer();

        // Enable the home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Initialize instance variables
        where = null;
        whereCategory = MusicCategories.ARTISTS;
        selectedCategory = MusicCategories.ARTISTS;
        playbackShowing = false;
    }

    /*
     * Sets up the navigation drawer
     */
    private void setUpNavigationDrawer(){
        // Link views with code
        drawerLayout    = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
        drawerListView  = (ListView) findViewById(R.id.leftDrawerListView);

        // Set the list's click listener
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,  R.drawable.ic_drawer, R.string.app_name, R.string.app_name){

            // Sets the title
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if(selectedCategory != null){
                    setTitle(selectedCategory.toString());
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(selectedCategory != null){
                    setTitle(selectedCategory.toString());
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    // Item click listener for the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Change the background color of the old selected view to the navigation drawer color
            if(selectedCategoryView != null){
                selectedCategoryView.setBackgroundColor(getResources().getColor(R.color.navColor));
            }

            // Set the selected view
            selectedCategoryView = view;
            selectedCategory = MusicCategories.valueOf(((TextView) view).getText().toString().toUpperCase());
            view.setBackgroundColor(getResources().getColor(R.color.selectedColor));

            // Set the title as the new selected category
            setTitle(selectedCategory.toString());

            // Change the category
            if(!selectedCategory.equals(MusicCategories.FOLDERS) && !selectedCategory.equals(MusicCategories.SETTINGS)) {
                changeCategory(selectedCategory);
            }
            drawerListView.setItemChecked(position, true);
            drawerLayout.closeDrawers();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void togglePlayback(View view){
        Fragment f = getFragmentManager()
                .findFragmentByTag("Playback");
        if (f != null) {
            getFragmentManager().popBackStack();
            getSupportActionBar().show();
            findViewById(R.id.playbackRelativeLayout).setVisibility(View.VISIBLE);
        } else {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.slide_up_below,
                            R.animator.slide_up,
                            R.animator.slide_down_above,
                            R.animator.slide_down)
                    .add(R.id.fragment_holder, Fragment
                                    .instantiate(this, PlaybackFragment.class.getName()),
                            "Playback"
                    ).addToBackStack("Listing").commit();
            getSupportActionBar().hide();
            findViewById(R.id.playbackRelativeLayout).setVisibility(View.GONE);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playbackShowing = !playbackShowing;
            }
        }, 400);


    }

    public void createPopUp(View view){
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
        popup.show();
    }

    public void changeCategory(MusicCategories category){
        where = null;
        whereCategory = MusicCategories.SETTINGS;
        Fragment fragment;
        switch(category){
            case ALBUMS:
                fragment = new AlbumFragment();
                break;
            case ARTISTS:
                fragment = new ArtistFragment();
                break;
            case SONGS:
                fragment = new SongFragment();
                break;
            case GENRES:
                fragment = new GenreFragment();
                break;
            case PLAYLISTS:
                fragment = new PlaylistFragment();
                break;
            default:
                fragment = new ArtistFragment();
                break;
        }
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_holder, fragment);
        transaction.commit();

    }

    private void handleClick(String[] where, String fragmentName, String fragmentID){
        ((LinearLayout) findViewById(R.id.blockClicksLinearLayout)).setClickable(true);
        this.where = where;
        getFragmentManager().findFragmentById(R.id.fragment_holder).
                getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_left_off,
                        R.animator.slide_left_on,
                        R.animator.slide_right_off,
                        R.animator.slide_right_on)
                .add(R.id.fragment_holder,
                        Fragment.instantiate(this, fragmentName),
                        fragmentID
                ).addToBackStack(selectedCategory.toString()).commit();
        selectedCategory = MusicCategories.valueOf(fragmentID);

        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    public void handleArtistOnClick(Object object){
        whereCategory = MusicCategories.ARTISTS;

        // Select albums where artistID = artistID and isMusic = 1
        where = new String[3];
        where[0] = MediaStore.Audio.Media.ARTIST_ID + " =? AND " + MediaStore.Audio.Media.IS_MUSIC  + "=?";
        where[1] = ((ArtistListViewItem) object).getArtistID();
        where[2] = "1";
        handleClick(where, AlbumFragment.class.getName(), "ALBUMS");
        setTitle(((ArtistListViewItem) object).getName());
    }
    public void handleAlbumOnClick(Object object){
        whereCategory = MusicCategories.ALBUMS;

        AlbumListViewItem item = (AlbumListViewItem) object;

        String whereText = MediaStore.Audio.Media.IS_MUSIC + " = ?";
        whereText = whereText + " AND " +  MediaStore.Audio.Media.ARTIST_ID + "=?";
        whereText = whereText + " AND (" + MediaStore.Audio.Media.ALBUM  + " = ?";
        whereText = whereText + " OR " + MediaStore.Audio.Media.ALBUM_ID + " = ? )";
        where = new String[5];
        where[0] = whereText;                           // Where text
        where[1] = "1";                                 // isMusic indicator
        where[2] = Long.toString(item.getArtistID());   // ArtistID
        where[3] = item.getTitle();                     // Album title
        where[4] = Long.toString(item.getAlbumID());    // Album ID
        handleClick(where, SongFragment.class.getName(), "SONGS");
        setTitle(((AlbumListViewItem) object).getTitle());
    }


    public void handleSongOnClick(final ArrayList<SongListViewItem> list, final int position){

        Intent intent = new Intent(this, MusicPlayerService.class);
        this.connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder servic) {
                service = servic;
                manager = ((MusicPlayerService.MyBinder)service).getService().startPlaying(list, position, MainActivity.this);
                if(!playbackShowing){
                    findViewById(R.id.playbackRelativeLayout).setVisibility(View.VISIBLE);
                    togglePlayback(null);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        //manager.loadSong(list.get(position));
    }


    public void handleGenreOnClick(Object object) {
        whereCategory = MusicCategories.GENRES;

        where = new String[3];
        where[0] = MediaStore.Audio.Media.IS_MUSIC  + "=?";                 // Where text
        where[1] = "1";                                                     // isMusic indicator
        where[2] = ((GenreListViewItem) object).getId();                    // GenreId
        handleClick(where, AlbumFragment.class.getName(), "ALBUMS");
        setTitle(((GenreListViewItem) object).getName());
    }
    public void handlePlaylistOnClick(View view){

    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 1) {
            actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        }
        if(playbackShowing){
            togglePlayback(null);
        }
        else {
            if (getFragmentManager().getBackStackEntryCount() != 0) {
                setTitle(getFragmentManager().getBackStackEntryAt(0).getName());
                getFragmentManager().popBackStack();
                if (playbackShowing) {
                    playbackShowing = !playbackShowing;
                    getSupportActionBar().show();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    public String[] getWhere(){
        return where;
    }

    public void handleSkip(View view){
        manager.skip();
        Log.d(GlobalFunctions.TAG, "song skipped");
    }

    public void handleBack(View view){
        manager.back();
    }

    public void togglePlay(View view){
        if(manager.isPlaying()){
            manager.pause();
            ((PlaybackFragment) getFragmentManager().findFragmentById(R.id.fragment_holder)).pause();
            ((ImageButton) findViewById(R.id.playImageButton)).setBackgroundResource(R.drawable.ic_action_play);
        }
        else{
            manager.play();
            ((PlaybackFragment) getFragmentManager().findFragmentById(R.id.fragment_holder)).play();
            ((ImageButton) findViewById(R.id.playImageButton)).setBackgroundResource(R.drawable.ic_action_pause);
        }
    }

    public SongListViewItem getNowPlaying(){
        if(manager != null){
            return manager.getNowPlaying();
        }
        else{
            return null;
        }
    }

    public boolean getNowPlayingBoolean(){
        if(manager != null){
            return manager.getNowPlaying() != null && manager.getNowPlayingBoolean();
        }
        else{
            return false;
        }
    }

    public void loadNewSongInfo(SongListViewItem newSong){
        if(playbackShowing){
            ((PlaybackFragment) getFragmentManager().findFragmentById(R.id.fragment_holder)).setInfo(newSong);
        }
    }

    public boolean isPlaybackShowing(){
        return playbackShowing;
    }
    public int getDuration(){
       return manager.getDuration();
    }
    public int getCurrentPosition(){
        return manager.getCurrentPosition();
    }

    public void seekTo(int position){
        manager.seekTo(position);
    }

    public MusicCategories getWhereCategory(){
        return whereCategory;
    }

    public void showNowPlaying(View view){
        Intent intent = new Intent(this, NowPlayingActivity.class);
        intent.putExtra("queue", manager.getQueue());
        startActivity(intent);
    }

    public boolean isPaused(){
        return !manager.isPlaying();
    }

    public void onFinish(){
        if(playbackShowing){
            ((PlaybackFragment) getFragmentManager().findFragmentById(R.id.fragment_holder)).finished();
        }
        getApplicationContext().unbindService(connection);

    }
}
