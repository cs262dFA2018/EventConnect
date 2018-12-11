package edu.calvin.cs262.cs262d.eventconnect.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.tools.AppThemeChanger;

import java.util.List;

/**
 * Following documentation provided by Littlesnowman88:
 * A settings Activity based on the Android studio Settings Activity Template.
 * The settings activity includes PreferenceFragments for various settings categories
 * The settings activity also holds an onPreferenceChangedListener.
 * IMPORTANT: if preferences are added, removed, or in some cases renamed, the application must be reinstalled.
 *
 * Following header documentation provided by Android Studio:
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private String currentTheme;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     * @author Android Studio
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     * @author Android Studio
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     * IMPORTANT: This method also sets an onPreferenceChangeListener. If a different change listener
     *      is needed, then the first line of code in this function must be skipped.
     *      See bindPreferenceSummary(Preference preference)
     *
     * @see #sBindPreferenceSummaryToValueListener
     * @param preference the preference to be listened to and changed.
     * @author Android Studio (function)
     * @author Littlesnowman88 (documentation)
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     * @param preference the preference whose summary needs to be changed.
     * @author Littlesnowman88
     */
    private static void bindPreferenceSummary(Preference preference) {
        // Trigger the Settings Activity listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * ON CREATE IS DOWN HERE
     * NOTE: this is for the whole settings activity.
     * Sets itself to dark mode or light mode and sets up the action bar
     * To access individual preference UI elements, use the fragments made below.
     * @param savedInstanceState the last known state of the Settings Activity
     * @author Littlesnowman88
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //access shared preferences for theme setting first.
        //MUST BE HANDLED BEFORE setContentView is called--in this case, before super.onCreate is called
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppThemeChanger.handleThemeChange(this, currentTheme);
        currentTheme = sharedPrefs.getString("theme_preference", "Light"); //default to Light theme
        //handle the rest of onCreate
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the action bar to show the Settings Label, a back arrow, and not hovering over settings.
     * @author Littlesnowman88
     */
    private void setupActionBar() {
        /*setup action bar
         * thanks to https://gldraphael.com/blog/adding-a-toolbar-to-preference-activity/ for
         *  getting the layout inflater
         */
        //get the toolbar layout before finding the ui element
        getLayoutInflater().inflate(R.layout.settings_toolbar, (ViewGroup)findViewById(android.R.id.content));
        //get the UI element
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);

        //set the support action bar
        setSupportActionBar(toolbar);
        //then set up toolbar/actionbar's up navigation
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }
    }

    /**
     * called when the user presses the actionBar's back arrow, navigating back to MainActivity
     * @param featureId the UI id of the clicked element
     * @param item the clicked item on the actionBar
     * @return true if the back arrow was pressed, otherwise letting AppCompatActivity handle things.
     * @author Littlesnowman88
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                finish();
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     * Checks to see if device can handle multiple panes or not
     * @author Android Studio
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     * loads the settings categories seen on the first page
     * These are found in pref_headers.xml under res -> xml
     * Then, adjusts header icons to contrast with dark or light mode.
     * @author Littlesnowman88
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
        //now access shared preferences to determine if black or white is needed
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String night_mode = sharedPrefs.getString("theme_preference", "Light");
        //if black is needed, set icons to black
        if (night_mode.equals("Light")) {
            target.get(0).iconRes = R.drawable.ic_info_black_24dp;
            target.get(1).iconRes = R.drawable.ic_notifications_black_24dp;
            target.get(2).iconRes = R.drawable.ic_sync_black_24dp;
            target.get(3).iconRes = R.drawable.ic_question_answer_black_24dp;
        }
        //else if white is needed, set icons to white
        else if (night_mode.equals("Dark")) {
            target.get(0).iconRes = R.drawable.ic_info_white_24dp;
            target.get(1).iconRes = R.drawable.ic_notifications_white_24dp;
            target.get(2).iconRes = R.drawable.ic_sync_white_24dp;
            target.get(3).iconRes = R.drawable.ic_question_answer_light_24dp;

        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     * IMPORTANT: If you add settings categories, update this method! -Littlesnowman88
     * @author Littlesnowman88
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || FAQFragment.class.getName().equals(fragmentName);
    }


    /*NOTE FROM LITTLESNOWMAN88:
     * The following sections of code are used for multi-pane settings UIs (such as on tablets).
     * If you add settings categories to the first settings screen, ADD SETTINGS CATEGORIES BELOW.
     */

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     * Modified by: Littlesnowman88
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        //added here specifically for the dark mode preference
        private ListPreference prefDarkMode;
        private Preference.OnPreferenceChangeListener themePrefListener;

        /**
         * Creates the GeneralPreferenceFragment
         * adds Preferences to be listened to, and defines a unique preference listener for dark mode
         * Then, sets preference summaries
         * @param savedInstanceState the last known state of this PreferenceFragment
         * @author Littlesnowman88
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //grab preferences from xml
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            //access the dark mode preference and set a listener
            Preference themePref = findPreference("theme_preference");
            themePref.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        /**
                         * onPreferenceChange recreates the activity if the app theme has been changed
                         * Thus, the theme change takes place right away.
                         * @param preference themePref, the dark mode theme.
                         * @param newValue the value corresponding to the chosen themePref, found in values/strings/pref_dark_mode_values
                         * @return true, acknowledging that the preference changed.
                         * @author Littlesnowman88
                         */
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            //if theme has changed, recreate Settings Activity to apply a changed theme
                            if (AppThemeChanger.shouldChangeTheme(getActivity(), newValue.toString())) {
                                getActivity().recreate();
                            }
                            return true;
                        }
                    }
            );

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"));
            // Override the SettingsActivity Preference listener but still provide same functionality
            bindPreferenceSummary(themePref);
        }

        /**
         * onOptionsItemSelected handles the up arrow that a user can click on
         * @param item the back arrow at the action bar
         * @return true if up arrow pressed, otherwise PreferenceFragment handles things.
         * @author Littlesnowman88
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     * @author Android Stuido
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        /**
         * Creates the NotificationPreferenceFragment
         * adds Preferences to be listened to and sets preference summaries
         * @param savedInstanceState the last known state of this PreferenceFragment
         * @author Android Studio (code)
         * @author Littlesnowman88 (documentation)
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        /**
         * onOptionsItemSelected handles the up arrow that a user can click on
         * @param item the back arrow at the action bar
         * @return true if up arrow pressed, otherwise PreferenceFragment handles things.
         * @author Littlesnowman88
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     * @author Android Studio
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        /**
         * Creates the DataSyncPreferenceFragment
         * adds Preferences to be listened to and sets preference summaries
         * @param savedInstanceState the last known state of this PreferenceFragment
         * @author Android Studio (code)
         * @author Littlesnowman88 (documentation)
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        /**
         * onOptionsItemSelected handles the up arrow that a user can click on
         * @param item the back arrow at the action bar
         * @return true if up arrow pressed, otherwise PreferenceFragment handles things.
         * @author Littlesnowman88
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class FAQFragment extends DialogFragment {
        /**
        TODO doc
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);



            //TODO set text
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View frag_layout = inflater.inflate(R.layout.faq, container, false);
            return frag_layout;
        }

        /**
         * onOptionsItemSelected handles the up arrow that a user can click on
         * @param item the back arrow at the action bar
         * @return true if up arrow pressed, otherwise PreferenceFragment handles things.
         * @author Littlesnowman88
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
