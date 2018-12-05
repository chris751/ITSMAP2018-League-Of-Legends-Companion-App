package com.example.christianmaigaard.lolcompanion.Utilities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;

public class AssetHelper {

    // Source: https://xjaphx.wordpress.com/2011/10/02/store-and-use-files-in-assets/
    public static Drawable loadChampImageFromAssets(Context context,String champName) {
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

    public static Drawable loadIconImageFromAssets(Context context,String iconName) {
        // load image
        try {
            // get input stream
            InputStream ims = context.getAssets().open("icons/" + iconName + ".png");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            return d;
        }
        catch(IOException ex) {
            return null;
        }
    }

    public static Drawable loadRankTierImageFromAssets(Context context,String tier) {
        String fileName = "";
        switch (tier){
            case (Constants.RANK_UNRANKED):
                fileName = "";
                return null;
            case (Constants.RANK_IRON):
                fileName = "Iron_Emblem";
                break;
            case (Constants.RANK_BRONZE):
                fileName = "Bronze_Emblem";
                break;
            case (Constants.RANK_SILVER):
                fileName = "Silver_Emblem";
                break;
            case (Constants.RANK_GOLD):
                fileName = "Gold_Emblem";
                break;
            case (Constants.RANK_PLATINUM):
                fileName = "Platinum_Emblem";
                break;
            case (Constants.RANK_DIAMOND):
                fileName = "Diamond_Emblem";
                break;
            case (Constants.RANK_MASTER):
                fileName = "Master_Emblem";
                break;
            case (Constants.RANK_GRANDMASTER):
                fileName = "Grandmaster_Emblem";
                break;
            case (Constants.RANK_CHALLENGER):
                fileName = "Challenger_Emblem";
                break;
        }

        // load image
        try {
            // get input stream
            InputStream ims = context.getAssets().open("rank/" + fileName + ".png");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            return d;
        }
        catch(IOException ex) {
            return null;
        }
    }


}
