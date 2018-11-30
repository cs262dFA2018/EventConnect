package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * NetworkUtils handles https requests to EventConnect's server database
 *
 * @author Littlesnowman88
 */
public class NetworkUtils {

    //creating a log tag for debugging purposes
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    //build the base url for the EventConnect database
    private static final String EVENT_BASE_URL = "https://calvincs262-fall2018-cs262d.appspot.com/eventconnect/v1";

    /**
     * getPlayerInfo builds a complete URL, connects to that URL, and fetches player data based on player filter
     *
     * @param endpoint the database api endpoint, appended to the base URL
     * @return //TODO: figure this out...
     * a JSON String to be parsed by MainActivity; the JSONString, if successful connection, contains player data
     * OR, a localized error message if connection failed.
     * @author Littlesnowman88
     */
    public static String getEventInfo(String endpoint) throws RuntimeException {
        //build necessary web-related components outside try-catch scope
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String eventJSONString = null;
        try {
            /* CONNECT TO THE DATABASE on google cloud */
            //Build the URL
            Uri builtURI = Uri.parse(EVENT_BASE_URL).buildUpon()
                    .appendEncodedPath(endpoint)
                    .build();
            URL requestURL = new URL(builtURI.toString());

            //establish url connection and make a GET request
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();


            /* RECEIVE THE JSON and do stuff. */
            //read the response and turn it into a string
            InputStream iStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (iStream == null) {
                //no input stream came back, sadness :(
                throw new RuntimeException("ERROR: connection failed; no input stream could be created.");
            }

            //build the stream reader/parser, putting newlines into the text for debugging aid
            reader = new BufferedReader(new InputStreamReader(iStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n"); //Only and REALLY helpful for debugging
            }
            if (buffer.length() == 0) {
                // input stream was empty, so return null and don't bother parsing.
                throw new RuntimeException("ERROR: No player found for that ID.");
            }

            eventJSONString = buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
            //return an empty string, which will be interpreted as an invalid event
            return "";

        } finally {
            if (urlConnection != null) {
                //we don't want connections lingering around. NO LOITERING.
                urlConnection.disconnect();
            }
            //now, if the input stream was opened
            if (reader != null) {
                try {
                    //clean up the reader, too.
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //Although I normally delete debug statements, I will leave this here because it REALLY helps with debugging. -LS88
        //Log.d(LOG_TAG, (eventJSONString != null) ? eventJSONString : "JSON STRING IS NULL, BADNESS");
        return eventJSONString;
    }
}

