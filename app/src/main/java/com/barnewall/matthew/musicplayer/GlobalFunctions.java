package com.barnewall.matthew.musicplayer;

import android.app.Activity;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Matthew on 12/31/2015.
 */
public class GlobalFunctions {
    public static final String TAG = "barnewall.musicplayer";

    public static Bitmap getBitmapFromID(long id, int size, Activity activity){
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, id);

        Bitmap bitmap = null;
        try {
            return getThumbnail(albumArtUri, size, activity);
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            return BitmapFactory.decodeResource(activity.getResources(),
                    R.drawable.no_album_art);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap getThumbnail(Uri uri, int size, Activity activity) throws FileNotFoundException, IOException{
        InputStream input = activity.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > size) ? (originalSize / size) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio / 2.0);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = activity.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    public static ArrayList<Comparable> removeDuplicates(ArrayList<Comparable> originalArrayList){

        Collections.sort(originalArrayList);
        for(int i = 0; i < originalArrayList.size() - 1; i++){
            if(originalArrayList.get(i).compareTo(originalArrayList.get(i + 1)) == 0){
                originalArrayList.remove(i + 1);
                i = i - 1;
            }
        }
        return null;
    }
}
