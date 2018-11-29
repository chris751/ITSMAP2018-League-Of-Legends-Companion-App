package com.example.christianmaigaard.lolcompanion.Utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.LinearLayout;

import com.example.christianmaigaard.lolcompanion.R;

public class Dialog {

    public static void showAlertDialog(Context c, String Title, String Message) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);

        alertDialog.setTitle(Title);
        alertDialog.setMessage(Message);

        alertDialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog ad = alertDialog.create();
        ad.show();
    }
}
