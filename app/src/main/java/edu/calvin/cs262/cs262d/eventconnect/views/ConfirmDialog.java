package edu.calvin.cs262.cs262d.eventconnect.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.lang.ref.WeakReference;

/**
 * Confirmation Alert Dialog for checking the user meant to delete an event
 * Code borrowed from https://github.com/LightSys/emailhelper-2/blob/master/app/src/main/java/org/lightsys/emailhelper/ConfirmDialog.java
 * Class does not extend AlertDialog because AlertDialog doesn't have a default constructor
 */
public class ConfirmDialog {

    /**
     * Confirmation Alert Dialog to confirm user wants to delete an event
     *
     * @param message message for the dialog to display
     * @param confirmationWord user presses "delete" to delete
     * @param base_context context to run in, here the current UI activity
     * @param confirm Runnable that deletes the event
     * @param cancel Runnable with no content (does nothing)
     * @edited: ksn7
     */
    public ConfirmDialog(String message, String confirmationWord, Context base_context, final Runnable confirm, final Runnable cancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new WeakReference<Context>(base_context).get());

        // set message on the dialog
        builder.setMessage(message);

        // set positive button to "delete", then link it to the runnable that deletes the event
        builder.setPositiveButton(confirmationWord, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (confirm != null) {
                    confirm.run();
                }
            }
        });

        // set the negative button to "cancel", then link it to the runnable that does nothing
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cancel != null) {
                    cancel.run();
                }
            }
        });

        // show the dialog once its fully built
        builder.create().show();

    }
}
