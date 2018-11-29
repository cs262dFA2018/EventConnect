package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;


/**
 * DataLoader is the AsyncTaskLoader responsible for fetching data from NetworkUtils
 *
 * @author Littlesnowman88
 */
public class DataLoader extends AsyncTaskLoader<String> {


    private String dbEndpoint;

    /**
     * Constructor: passes context to AsyncTaskLoader and saves the requested
     *
     * @param context the "screen" that created this AsyncTaskLoader
     * @param endpoint the database API endpoint attached at the end of the base url
     */
    public DataLoader(@NonNull Context context, String endpoint) {
        super(context);
        this.dbEndpoint = endpoint;
    }

    /**
     * Forces the AsyncTaskLoader to load
     *
     * @author Littlesnowman88
     */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    /**
     * Fetches data from NetworkUtils in the background so the UI thread isn't held up by data fetching
     *
     * @return information corresponding to the app's http request (GET, POST, PUT, DELETE);
     * @author Littlesnowman88
     */
    @Override
    public String loadInBackground() {
        return NetworkUtils.getEventInfo(dbEndpoint);
    }

}
