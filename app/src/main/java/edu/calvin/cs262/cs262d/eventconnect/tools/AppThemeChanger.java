package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import edu.calvin.cs262.cs262d.eventconnect.R;

public final class AppThemeChanger {

    public static void handleThemeChange(Context context, String currentTheme) {
        if (shouldChangeTheme(context, currentTheme)) {
            setActivityTheme(context, currentTheme);
        }
    }

    public static boolean shouldChangeTheme(Context context, String currentTheme) {
        if (null == currentTheme) return true; //the app must have a theme
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String themePref = sharedPrefs.getString("theme_preference", "Light");
        return !currentTheme.equals(themePref); //should change if the current theme does not match shared preferences
    }

    private static void setActivityTheme(Context context, String currentTheme) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String themePref = sharedPrefs.getString("theme_preference", "Light");
        switch (themePref) {
            case "Dark":
                context.setTheme(R.style.AppThemeDark);
                break;
            case "Auto":
                context.setTheme(R.style.AppThemeDayNight);
                break;
            default:
                context.setTheme(R.style.AppTheme);
        }
    }
}
