package com.barnewall.matthew.musicplayer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.app.Fragment;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends ActionBarActivity implements
        PlaybackFragment.OnFragmentInteractionListener,
        MusicFragment.OnFragmentInteractionListener,
        ControlListener {

    // Instance Variables

    // Navigation Menu
    private DrawerLayout                drawerLayout;
    private ListView                    drawerListView;
    private ActionBarDrawerToggle       actionBarDrawerToggle;

    // Currently selected category in the navigation menu
    private MusicCategories             selectedCategory;
    private View                        selectedCategoryView;

    // Array containing where clause for the MusicFragment subclasses
    // First index is the where string, all subsequent indexes are the values for the where string
    private String[]                    where;
    private MusicCategories             whereCategory;

    // Music playback controls
    private MediaPlayerManager          manager;

    // Variables for interacting with service that plays the music
    private IBinder                     service;
    private ServiceConnection           connection;

    // Used to pop the PlaybackFragment if it was open when the manager was destoryed
    private boolean popOnResume;

    // Categories in the navigatino menu
    public enum MusicCategories{
        ALBUMS,ARTISTS,PLAYLISTS,SONGS,GENRES,FOLDERS,SETTINGS
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

        startService(new Intent(this,MyService.class));

        // Sets the audio stream so volume changes affect music volume not system volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Set up the navigation drawer
        setUpNavigationDrawer();

        // Enable the home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Where variables
        where = null;
        whereCategory = MusicCategories.ARTISTS;
        selectedCategory = MusicCategories.ARTISTS;
        popOnResume = false;
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


    private float y1,y2;
    static final int MIN_DISTANCE = 50;
    /*
     * Toggles between the main activity and the playback fragment
     */
    public void togglePlayback(View view){

        Fragment f = getFragmentManager()
                .findFragmentByTag("Playback");

        // If the Playback fragment exists on the backstack pop it off
        if (f != null) {
            try {
                getFragmentManager().popBackStack();
                getSupportActionBar().show();

                // Music is playing or could be, show the playback bar
                if (manager.getNowPlaying() != null) {
                    findViewById(R.id.playbackRelativeLayout).setVisibility(View.VISIBLE);
                }

                // Adds the ability to swipe to open the playback bar
                findViewById(R.id.playbackRelativeLayout).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case (MotionEvent.ACTION_DOWN):
                                y1 = event.getY();
                                break;
                            case (MotionEvent.ACTION_UP):
                                y2 = event.getY();
                                if ((y1 - y2) > MIN_DISTANCE) {
                                    togglePlayback(null);
                                }
                        }
                        return false;
                    }
                });
            }
            catch(IllegalStateException e){
                popOnResume = true;
            }
        }

        // If the playback fragment does not exist on the backstack, create a new instance of
        //    the playback fragment and replace the current fragment with it
        else {
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


    }

    // Creates a popup menu of options for the song, artist, album
    public void createPopUp(final View view){
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().equals(getResources().getString(R.string.play))) {
                    menuPlay(view);
                } else if (item.getTitle().equals(getResources().getString(R.string.play_next))) {
                    menuPlayNext(view);
                } else if (item.getTitle().equals(getResources().getString(R.string.add_to_play_queue))) {
                    menuAddToQueue(view);
                } else if (item.getTitle().equals(getResources().getString(R.string.add_to_playlist))) {
                    menuAddToPlaylist(view);
                } else if (item.getTitle().equals(getResources().getString(R.string.delete))) {
                    menuDelete(view);
                }
                return false;
            }
        });
        popup.show();
    }

    // Change the current fragment based on the category passed in
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
        findViewById(R.id.blockClicksLinearLayout).setClickable(true);
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
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
        where[2] = item.getArtistID();   // ArtistID
        where[3] = item.getTitle();                     // Album title
        where[4] = Long.toString(item.getAlbumID());    // Album ID

        handleClick(where, SongFragment.class.getName(), "SONGS");
        setTitle(((AlbumListViewItem) object).getTitle());
    }


    public void handleSongOnClick(final ArrayList<SongListViewItem> list, final int position){

        // If no song has been played yet launch the service and connect to it
        Intent intent = new Intent(this, MusicPlayerService.class);
        this.connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder servic) {
                service = servic;
                if(list.size() != 0) {
                    manager = ((MusicPlayerService.MyBinder) service).getService().startPlaying(list, position, MainActivity.this);
                    if (!isPlaybackShowing()) {
                        findViewById(R.id.playbackRelativeLayout).setVisibility(View.VISIBLE);
                        togglePlayback(null);
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
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
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        if(isPlaybackShowing()){
            togglePlayback(null);
        }
        else {
            if (getFragmentManager().getBackStackEntryCount() != 0) {
                setTitle(getFragmentManager().getBackStackEntryAt(0).getName());
                getFragmentManager().popBackStack();
                if (isPlaybackShowing()) {
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
            findViewById(R.id.playImageButton).setBackgroundResource(R.drawable.ic_action_play);
        }
        else{
            manager.play();
            ((PlaybackFragment) getFragmentManager().findFragmentById(R.id.fragment_holder)).play();
            findViewById(R.id.playImageButton).setBackgroundResource(R.drawable.ic_action_pause);
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
        if(isPlaybackShowing()){
            ((PlaybackFragment) getFragmentManager().findFragmentById(R.id.fragment_holder)).setInfo(newSong);
        }
    }

    public boolean isPlaybackShowing(){
        return findViewById(R.id.playImageButton) != null;
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
        startActivityForResult(intent, NowPlayingActivity.NOW_PLAYING);
    }

    public boolean isPaused(){
        return !manager.isPlaying();
    }

    public void onFinish(){
        if(isPlaybackShowing()){
            ((PlaybackFragment) getFragmentManager().findFragmentById(R.id.fragment_holder)).finished();
        }
        getApplicationContext().unbindService(connection);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If the service is connected, end the music player and unbind
        if(connection != null && service.isBinderAlive()) {
            //TODO:FIX THIS
            if(manager.getNowPlayingBoolean()){
                manager.destroy();
            }
            getApplicationContext().unbindService(connection);
        }
    }

    /*
         * interface implement method
         */
    public void songPlay(){
        findViewById(R.id.playImageButton).setBackgroundResource(R.drawable.ic_action_pause);
    }

    /*
     * interface implement method
     */
    public void songPause(){
        findViewById(R.id.playImageButton).setBackgroundResource(R.drawable.ic_action_play);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == NowPlayingActivity.NOW_PLAYING){
            manager.setListener(this);
            if(resultCode == RESULT_OK){
                SongListViewItem item = manager.getQueue().get(data.getExtras().getInt(NowPlayingActivity.POSITION));
                togglePlayback(null);
                if(data.getExtras().getBoolean(NowPlayingActivity.ALBUM_FRAGMENT)){
                    handleAlbumOnClick(new AlbumListViewItem(item.getAlbumID(),
                            item.getArtistName(), item.getTitle(), null, item.getArtistID()));
                }
                else{
                    handleArtistOnClick(new ArtistListViewItem(item.getArtistName(), null, item.getArtistID()));
                }
            }
        }

    }

    private ArrayList<SongListViewItem> menuGetSongs(View view){

        // Get the position of the item click (position in parents parent)
        int position = ((ListView) view.getParent().getParent()).getPositionForView(view);

        // Get the data that created the view
        Object item = ((ListView) view.getParent().getParent()).getAdapter().getItem(position);

        ArrayList<SongListViewItem> newSongs = new ArrayList<SongListViewItem>();

        // Do appropriate action based on what the item is
        if(item instanceof SongListViewItem){
            newSongs.add((SongListViewItem) item);
        }
        else if(item instanceof ArtistListViewItem){
            newSongs = getSongsFromArtist((ArtistListViewItem) item);
        }
        else if(item instanceof AlbumListViewItem){
            newSongs = getSongsFromAlbum((AlbumListViewItem) item);
        }
        else if(item instanceof GenreListViewItem){
            newSongs = getSongsFromGenre((GenreListViewItem) item);
        }

        return newSongs;
    }

    private void menuPlay(View view){

        // Get the correct songs that item relates to
        ArrayList<SongListViewItem> newSongs = menuGetSongs(view);

        // Release the old manager and start the new one
        if(manager != null) {
            manager.destroy();
        }
        handleSongOnClick(newSongs, 0);
    }

    private void menuPlayNext(View view){
        // Get the correct songs that item relates to
        ArrayList<SongListViewItem> newSongs = menuGetSongs(view);

        if(manager != null){
            manager.playNext(newSongs);
        }
        else{
            handleSongOnClick(newSongs, 0);
        }
    }

    private void menuAddToQueue(View view){
        // Get the correct songs that item relates to
        ArrayList<SongListViewItem> newSongs = menuGetSongs(view);

        if(manager != null){
            manager.addToQueue(newSongs);
        }
        else{
            handleSongOnClick(newSongs, 0);
        }
    }

    private void menuAddToPlaylist(View view){

    }

    private void menuDelete(View view){

    }

    private ArrayList<SongListViewItem> getSongsFromArtist(ArtistListViewItem artist){

        // Select albums where artistID = artistID and isMusic = 1
        String where = MediaStore.Audio.Media.ARTIST_ID + " =? AND " + MediaStore.Audio.Media.IS_MUSIC  + "=?";
        String[] whereParams = new String[2];
        whereParams[0] = artist.getArtistID();
        whereParams[1] = "1";

        return SongFragment.getSongs(where, whereParams, MusicCategories.ARTISTS, this, false);
    }

    private ArrayList<SongListViewItem> getSongsFromAlbum(AlbumListViewItem album){

        String where = MediaStore.Audio.Media.IS_MUSIC + " = ?";
        where = where + " AND " +  MediaStore.Audio.Media.ARTIST_ID + "=?";
        where = where + " AND (" + MediaStore.Audio.Media.ALBUM  + " = ?";
        where = where + " OR " + MediaStore.Audio.Media.ALBUM_ID + " = ? )";
        String[] whereParams = new String[4];
        whereParams[0] = "1";                                  // isMusic indicator
        whereParams[1] = album.getArtistID();   // ArtistID
        whereParams[2] = album.getTitle();                     // Album title
        whereParams[3] = Long.toString(album.getAlbumID());    // Album ID

        return SongFragment.getSongs(where, whereParams, MusicCategories.ALBUMS, this, false);
    }

    private ArrayList<SongListViewItem> getSongsFromGenre(GenreListViewItem genre){

        String where = MediaStore.Audio.Media.IS_MUSIC  + "=?";                 // Where text
        String[] whereParams = new String[2];
        whereParams[0] = "1";                                                     // isMusic indicator
        whereParams[1] = genre.getId();                    // GenreId

        return SongFragment.getSongs(where, whereParams, MusicCategories.GENRES, this, false);
    }

    public Context getContext(){
        return getApplicationContext();
    }

    public String getApplicationName(){
        return getPackageName();
    }

    //TODO: Implement for this, PlaybackFragment, and nowPlayingActivity
    public void destroy(){
        if(isPlaybackShowing()){
            togglePlayback(null);
        }
        findViewById(R.id.playbackRelativeLayout).setVisibility(View.GONE);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(popOnResume){
            destroy();
        }
    }


}
