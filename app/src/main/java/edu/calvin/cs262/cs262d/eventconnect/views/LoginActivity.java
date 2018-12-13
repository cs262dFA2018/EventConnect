package edu.calvin.cs262.cs262d.eventconnect.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.EventConnector;
import edu.calvin.cs262.cs262d.eventconnect.data.UserDAO;
import edu.calvin.cs262.cs262d.eventconnect.tools.AppThemeChanger;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    //app theme setting string
    private String currentTheme;

    //login email and password strings
    private String email, password;
    //list of users who exist in the database. Used for event host features.
    private List<UserDAO> users;
    //used to get Users from the database.
    private static final String BASE_URL = "https://calvincs262-fall2018-cs262d.appspot.com/eventconnect/v1/";
    private static final String LOGIN_FETCH = "fetchUsers";
    private static final String LOGIN_POST = "postNewUser";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Context context;
    private Button mEmailSignInButton;

    /**
     * onCreate builds the LoginActivity screen
     * first, determines the theme for the login screen
     * then, does some normal activity onCreate stuff
     * then accesses UI and sets listeners
     * @param savedInstanceState the last known state of LoginActivity
     * @author OneTrueAsian
     * @author Littlesnowman88 (theme settings)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //access shared preferences for theme setting first.
        //MUST BE HANDLED BEFORE setContentView is called--in this case, before super.onCreate is called
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppThemeChanger.handleThemeChange(this, currentTheme);
        currentTheme = sharedPrefs.getString("theme_preference", "Light"); //default to Light theme

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        //setupActionBar();
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
       // populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();

            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Register the BroadcastManager here to receive messages from EventConnector.
     *
     * @author Littlesnowman88
     */
    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(context).registerReceiver(appMessageReceiver,
                new IntentFilter(LOGIN_FETCH));
        LocalBroadcastManager.getInstance(context).registerReceiver(appMessageReceiver,
                new IntentFilter(LOGIN_POST));
    }

    /**
     * Unregister the BroadcastManager here to stop receiving messages from EventConnector.
     *
     * @author Littlesnowman88
     */
    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(appMessageReceiver);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     * @author Android Studio
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;
        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;

        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;

        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if (users == null) {
                users = new ArrayList<>();
            }
            EventConnector ec = new EventConnector(context);

            EventConnector.getUsersForLogin(this, users, BASE_URL + "users");
        }
    }

    /**
     * Checks to  make sure email is valid
     * Requirements: must contain @ and ends with .com or .edu
     * @param email
     * @return
     * @author Android Studio
     */
    private boolean isEmailValid(String email) {
        //makes sure email ends with .com or .edu
        return email.length() < 20 && email.contains("@") && (email.endsWith(".com") || email.endsWith(".edu"));
    }

    /**
     * Valid Password length must be > 4 char
     * @param password
     * @return
     * @author Android Studio
     * @author OneTrueAsian
     */
    private boolean isPasswordValid(String password) {
        //is a valid password before sending to server
        if (password.length() > 4 && password.length() < 20) return true;
        return false;
    }

    /**
     * handles Broadcasted Intents from EventConnector.
     * If the intent really is from EventConnector, this broadcast receiver handles logging in and starting MainActivity.
     * appMessageReceiver is initialized outside of onCreate because only one ever needs to be made.
     *
     * @author Littlesnowman88
     */
    private final BroadcastReceiver appMessageReceiver = new BroadcastReceiver() {
        /**
         * onReceive handles broadcasts received from EventConnector
         *
         * @param context the context that this receiver listens to.
         * @param intent the message sent out by EventConnector
         *
         * @author Littlesnowman88
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(LOGIN_FETCH)) {
                    //once the users are fetched, see if the current login email already exists.
                    for (UserDAO user : users) {
                        //if the current login email already exists, start MainActivity.
                        if (user.getUsername().equals(email)) {
                            showProgress(false);
                            completeLoginTask(Integer.toString(AppCompatActivity.RESULT_OK), email, password);
                        }
                    }
                    //if the current login email doesn't already exist, post the current login email to the server.
                    EventConnector.postUserFromLogin(context, BASE_URL + "user/", email, password);
                }
                //result from a login email posted to the database
                else if (intent.getAction().equals(LOGIN_POST)) {
                    //startMainActivity.
                    showProgress(false);
                    completeLoginTask(Integer.toString(AppCompatActivity.RESULT_OK), email, password);
                }
            }
        }
    };

    /** starts main Activity when the AsyncTask Login completes
     * @author Littlesnowman88
     * @param result, whether the asynctask succeeded or failed
     */
    public void completeLoginTask(String result, String LoginID, String userPass) {
        if (result.equals(Integer.toString(AppCompatActivity.RESULT_OK))) {
            Intent startMain = new Intent(context, MainActivity.class);
            startMain.putExtra("UserID", LoginID);
            startMain.putExtra("UserPass", userPass);
            startActivityForResult(startMain, 2);
        }
    }

    /**
     * called when MainActivity finishes. If MainActivity finishes because of the
     *  Logout button, don't quit this activity.
     *  If MainActivity finishes because of the Back gutton, quit this activity.
     * @param requestCode 2, an activity identifier of sorts. See above.
     * @param resultCode Activity.RESULT_OK or Activity.RESULT_CANCELED
     * @param data, any actions set by MainActivity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
            //else, stay on this screen.
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * closes the app when the user presses Android's back arrow
     * @author Littlesnowman88
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Shows the progress UI and hides the login form.
     * @author Android Studio
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * @param i
     * @param bundle
     * @return
     * @author Android Studio
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    /**
     * @param cursorLoader
     * @param cursor
     * @author: Android Studio
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    /**
     * @param cursorLoader
     * @author Android Studio
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    /**
     * @param emailAddressCollection
     * @author Android Studio
     */
    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * @author Android Studio
     */
    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}

