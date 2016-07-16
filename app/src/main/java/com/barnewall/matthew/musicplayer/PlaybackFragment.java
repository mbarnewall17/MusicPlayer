package com.barnewall.matthew.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.barnewall.matthew.musicplayer.Song.SongFragment;
import com.barnewall.matthew.musicplayer.Song.SongListViewItem;


/**
 * Fragment that handles playback of music
 */
public class PlaybackFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public PlaybackFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playback, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load the song info incase it changed
        if (mListener.isInValidState()) {
            setInfo(mListener.getNowPlaying());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);

        public boolean getNowPlayingBoolean();

        public SongListViewItem getNowPlaying();

        public int getDuration();

        public int getCurrentPosition();

        public void seekTo(int position);

        public boolean isPlaybackShowing();

        public boolean isPaused();

        public void togglePlayback(View view);

        public boolean isShuffle();

        public void setUpRepeatIcon(MediaPlayerManager.Repeat repeat);

        public boolean isInValidState();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private Handler handler;
    private Runnable incrementSongCurrentTimeEverySecondWhenPlaying;
    private SeekBar seekBar;
    private String duration;
    static final int MIN_DISTANCE = 50;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Connect seekbar resource to variable
        seekBar = ((SeekBar) getActivity().findViewById(R.id.timeSeekBar));

        enableSwipeToCloseOnCollapseBar();

        // Set up the views with the correct info
        setInfo(mListener.getNowPlaying());

        changeBackgroundColorWhenPlayPauseButtonIsBeingHeldDown();

        setUpSeekBarListener();
    }

    private float y1, y2;

    private void enableSwipeToCloseOnCollapseBar() {
        // Adds the ability to close the fragment by swiping
        getActivity().findViewById(R.id.collapseBarRelativeLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case (MotionEvent.ACTION_DOWN):
                        y1 = event.getY();
                        break;
                    case (MotionEvent.ACTION_UP):
                        y2 = event.getY();
                        if ((y2 - y1) > MIN_DISTANCE) {
                            mListener.togglePlayback(null);
                        }
                }
                return false;
            }
        });
    }

    private void changeBackgroundColorWhenPlayPauseButtonIsBeingHeldDown() {
        getActivity().findViewById(R.id.playImageButton).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.getBackground().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
                return false;
            }
        });
    }

    public void setInfo(SongListViewItem nowPlaying) {
        // Remove the callbacks on the handler to update the seekbar
        if (handler != null) {
            handler.removeCallbacks(incrementSongCurrentTimeEverySecondWhenPlaying);
        }
        if (nowPlaying != null) {

            setUpPlayPauseButton();

            setUpShuffleButton();

            mListener.setUpRepeatIcon(null);

            setUpDurationTextViews(nowPlaying);

            seekBar.setProgress(mListener.getCurrentPosition());

            handler = new Handler();

            incrementSongCurrentTimeEverySecondWhenPlaying = new Runnable() {
                @Override
                public void run() {
                    if (mListener != null && getActivity().findViewById(R.id.currentTimeTextView) != null && mListener.getNowPlayingBoolean()) {
                        int currentTime = mListener.getCurrentPosition();
                        ((TextView) getActivity().findViewById(R.id.currentTimeTextView)).setText(SongFragment.msToMin(Integer.toString(currentTime)));
                        seekBar.setProgress(currentTime);
                        handler.postDelayed(incrementSongCurrentTimeEverySecondWhenPlaying, 1000);
                    }
                }
            };

            checkIfHandlerCallbacksAreNecessary();

            setUpAlbumArt(nowPlaying);

            setUpCollapseBarRelativeLayout();

            setUpSongMetadataTextViews(nowPlaying);
        } else {
            mListener.togglePlayback(null);
        }
    }

    private void setUpPlayPauseButton() {
        // Set the appropriate play/pause icon
        if (!mListener.isInValidState() || mListener.isPaused()) {
            getActivity().findViewById(R.id.playImageButton).setBackgroundResource(R.drawable.ic_action_play);
        } else {
            getActivity().findViewById(R.id.playImageButton).setBackgroundResource(R.drawable.ic_action_pause);
        }
    }

    private void setUpShuffleButton() {
        // Set up the shuffle icon
        if (mListener.isShuffle()) {
            getActivity().findViewById(R.id.shuffleImageButton).getBackground().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        } else {
            getActivity().findViewById(R.id.shuffleImageButton).getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void setUpDurationTextViews(SongListViewItem nowPlaying) {
        // Set up the duration
        duration = nowPlaying.getDuration();

        // Gets the duration and sets the max on the seekbar
        ((TextView) getActivity().findViewById(R.id.endTimeTextView)).setText(nowPlaying.getDuration());
        seekBar.setMax(getDuration(nowPlaying.getDuration()));
    }

    private void setUpAlbumArt(SongListViewItem nowPlaying) {
        // Gets the album art and sets the view as well as the background using it
        Bitmap albumArt = GlobalFunctions.getBitmapFromID(nowPlaying.getAlbumID(), 300, getActivity());
        ((ImageView) getView().findViewById(R.id.albumImageView)).setImageBitmap(albumArt);
        BitmapDrawable modifiedArt = new BitmapDrawable(BlurBuilder.blur(getActivity(), albumArt));
        getView().findViewById(R.id.albumWrapperLinearLayout).setBackground(modifiedArt);
    }

    private void setUpCollapseBarRelativeLayout() {
        // Sets the background of the collapseBarRelativeLayout
        Drawable drawable = getResources().getDrawable(R.drawable.gradient);
        drawable.setAlpha(80);
        getView().findViewById(R.id.collapseBarRelativeLayout).setBackground(drawable);
    }

    private void setUpSongMetadataTextViews(SongListViewItem nowPlaying) {
        // Sets the song, artist, and album TextViews
        ((TextView) getView().findViewById(R.id.songNameTextView)).setText(nowPlaying.getTitle());
        getView().findViewById(R.id.songNameTextView).setSelected(true);
        ((TextView) getView().findViewById(R.id.albumNameTextView)).setText(nowPlaying.getAlbumName());
        getView().findViewById(R.id.albumNameTextView).setSelected(true);
        ((TextView) getView().findViewById(R.id.artistNameTextView)).setText(nowPlaying.getArtistName());
        getView().findViewById(R.id.artistNameTextView).setSelected(true);
    }

    /*
     * Given a String in the format MM:HH returns an int representation in ms
     */
    private int getDuration(String text) {
        String first = text.substring(0, text.indexOf(":"));
        String second = text.substring(text.indexOf(":") + 1);
        int total = Integer.parseInt(first) * 60000;
        total = total + (Integer.parseInt(second) * 1000);
        return total;
    }

    public void checkIfHandlerCallbacksAreNecessary() {
        if (mListener.getNowPlayingBoolean()) {
            handler.postDelayed(incrementSongCurrentTimeEverySecondWhenPlaying, 1000);

        }
    }

    public void setUpSeekBarListener(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mListener.seekTo(seekBar.getProgress());
                ((TextView) getActivity().findViewById(R.id.currentTimeTextView)).setText(SongFragment.msToMin(Integer.toString(mListener.getCurrentPosition())));
            }
        });
    }

    public void pause() {
        if (handler != null) {
            handler.removeCallbacks(incrementSongCurrentTimeEverySecondWhenPlaying);
        }
    }

    public void play() {
        handler.postDelayed(incrementSongCurrentTimeEverySecondWhenPlaying, 1000);
    }

    public void finished() {
        pause();
        seekBar.setProgress(seekBar.getMax());
        ((TextView) getActivity().findViewById(R.id.currentTimeTextView)).setText(duration);
        getActivity().findViewById(R.id.playImageButton).setBackgroundResource(R.drawable.ic_action_play);
    }

}

// https://futurestud.io/blog/how-to-blur-images-efficiently-with-androids-renderscript
class BlurBuilder {
    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 7.5f;

    public static Bitmap blur(Context context, Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }
}
