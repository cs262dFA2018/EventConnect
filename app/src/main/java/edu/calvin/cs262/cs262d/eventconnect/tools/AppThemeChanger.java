package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import edu.calvin.cs262.cs262d.eventconnect.R;

/**
 * AppThemeChanger is a class of static methods that implement theme changing (i.e. from dark mode to light mode and vice versa)
 * handleThemeChange actually changes an Activity's theme
 * shouldChangeTheme is public in case a developer has to do some extra theme changes and wants to check if a theme change is necessary first
 * @author Littlesnowman88
 */
public final class AppThemeChanger{

    /**
     * handle themeChange checks to see if a theme needs to be changed, and if so, changes the theme
     * @param context the calling object that needs its theme changed
     * @param currentTheme the calling object's current theme (i.e. "Dark", "Light")
     * @author Littlesnowman88
     */
    public static void handleThemeChange(Context context, String currentTheme) {
        if (shouldChangeTheme(context, currentTheme)) {
            setActivityTheme(context);
        }
    }

    /**
     * shouldChangeTheme checks currentTheme against shared preferences to determine if a context's theme needs to change
     * This method is public so that future developers can ask if the theme needs to be changed but not have to rely on this class to change the theme.
     * @param context the calling object
     * @param currentTheme the calling object's current theme (i.e. "Dark", "Light")
     * @return true if the currentTheme does not match shared Preferences, false if the currentTheme matches shared preferences.
     * @author Littlesnowman88
     */
    public static boolean shouldChangeTheme(Context context, String currentTheme) {
        if (null == currentTheme) return true; //the app must have a theme
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String themePref = sharedPrefs.getString("theme_preference", "Light");
        return !currentTheme.equals(themePref); //should change if the current theme does not match shared preferences
    }

    /**
     * called by handleThemeChange, this method actually changes a calling object's theme to match shared preferences.
     * @param context the calling object having its theme changed
     * @author Littlesnowman88
     */
    private static void setActivityTheme(Context context) {
        //access the dark mode theme
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String themePref = sharedPrefs.getString("theme_preference", "Light");
        //set the calling object's theme to match with what's listed in shared preferences
        switch (themePref) {
            case "Light":
                context.setTheme(R.style.AppTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                context.setTheme(R.style.AppThemeDark);
                //turn off AppThemeDayNight
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            //"Auto" is commented out due to time restraints. I hope to revisit this later. -Littlesnowman88
            /*case "Auto":
                context.setTheme(R.style.AppThemeDayNight);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;*/
            //a Runtime Exception is thrown to ensure that new themes are fully implemented into this case statement. Developers must not be sloppy with their theme changes.
            default:
                throw new RuntimeException("ERROR: mismatch between selected app theme and implemented app themes.");
        }
    }

}
