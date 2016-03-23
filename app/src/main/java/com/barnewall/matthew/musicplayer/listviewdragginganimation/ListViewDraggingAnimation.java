//package com.barnewall.matthew.musicplayer.listviewdragginganimation;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.widget.ListView;
//
//import java.util.ArrayList;
//
//import com.example.android.listviewdragginganimation.R;
//
///**
// * This application creates a listview where the ordering of the data set
// * can be modified in response to user touch events.
// *
// * An item in the listview is selected via a long press event and is then
// * moved around by tracking and following the movement of the user's finger.
// * When the item is released, it animates to its new position within the listview.
// */
//public class ListViewDraggingAnimation extends Activity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(.layout.activity_list_view);
//
//        ArrayList<String>mCheeseList = new ArrayList<String>();
//        for (int i = 0; i < ListItem.sCheeseStrings.length; ++i) {
//            mCheeseList.add(ListItem.sCheeseStrings[i]);
//        }
//
//        StableArrayAdapter adapter = new StableArrayAdapter(this, R.layout.text_view, mCheeseList);
//        DynamicListView listView = (DynamicListView) findViewById(R.id.listview);
//
//        listView.setCheeseList(mCheeseList);
//        listView.setAdapter(adapter);
//        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//    }
//}
