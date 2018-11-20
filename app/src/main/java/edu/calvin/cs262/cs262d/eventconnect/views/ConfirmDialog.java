package edu.calvin.cs262.cs262d.eventconnect.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Confirmation Alert Dialog for checking the user meant to delete an event
 * Code borrowed from https://github.com/LightSys/emailhelper-2/blob/master/app/src/main/java/org/lightsys/emailhelper/ConfirmDialog.java
 * Class does not extend AlertDialog because AlertDialog doesn't have a default constructor
 */
public class ConfirmDialog {
    public ConfirmDialog(String message, String confirmationWord, Context context, final Runnable confirm, final Runnable cancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton(confirmationWord, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (confirm != null) {
                    confirm.run();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cancel != null) {
                    cancel.run();
                }
            }
        });
        builder.create().show();

    }
}
