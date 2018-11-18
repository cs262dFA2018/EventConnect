package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import edu.calvin.cs262.cs262d.eventconnect.R;

public final class AppThemeChanger{

    public static void handleThemeChange(Context context, String currentTheme) {
        if (shouldChangeTheme(context, currentTheme)) {
            setActivityTheme(context);
        }
    }

    public static boolean shouldChangeTheme(Context context, String currentTheme) {
        if (null == currentTheme) return true; //the app must have a theme
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String themePref = sharedPrefs.getString("theme_preference", "Light");
        return !currentTheme.equals(themePref); //should change if the current theme does not match shared preferences
    }

    private static void setActivityTheme(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String themePref = sharedPrefs.getString("theme_preference", "Light");
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
            default:
                throw new RuntimeException("ERROR: mismatch between selected app theme and implemented app themes.");
        }
    }

}
