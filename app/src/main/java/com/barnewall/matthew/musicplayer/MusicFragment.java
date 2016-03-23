package com.barnewall.matthew.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class MusicFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public MusicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String[] whereParams = mListener.getWhere();
        String where = null;
        if(whereParams != null && whereParams.length != 0){
            where = whereParams[0];

            whereParams = Arrays.copyOfRange(whereParams, 1, whereParams.length);
        }

        populateListView(where, whereParams, mListener.getWhereCategory());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        public String[] getWhere();
        public MainActivity.MusicCategories getWhereCategory();
        public void handleArtistOnClick(Object object);
        public void handleAlbumOnClick(Object object);
        public void handleSongOnClick(ArrayList<SongListViewItem> list, int position);
        public void handleGenreOnClick(Object object);

        public void handlePlaylistOnClick(View view);
    }



    public abstract void populateListView(String where, String[] whereParams, MainActivity.MusicCategories category);



    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        Animator animation;
        if (nextAnim == 0 ){
            animation = super.onCreateAnimator(transit, enter, nextAnim);
        } else {
            animation = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
            animation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    ((LinearLayout) getActivity().findViewById(R.id.blockClicksLinearLayout)).setClickable(false);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        return animation;
    }

    public OnFragmentInteractionListener getMListener(){
        return mListener;
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
}
