package com.example.christianmaigaard.lolcompanion.Utilities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;

public class AssetHelper {

    // Source: https://xjaphx.wordpress.com/2011/10/02/store-and-use-files-in-assets/
    public static Drawable loadChampImageFromAssets(Context context,String champName){
        // load image
        try {
            // get input stream
            InputStream ims = context.getAssets().open("champion/" + champName + ".png");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            return d;
        }
        catch(IOException ex) {
            return null;
        }
    }


}
