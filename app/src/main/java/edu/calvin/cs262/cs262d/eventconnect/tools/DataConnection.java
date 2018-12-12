package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.calvin.cs262.cs262d.eventconnect.data.Event;
import edu.calvin.cs262.cs262d.eventconnect.data.EventsData;


/**
 * DataConnection is the AsyncTask responsible for fetching data from NetworkUtils
 *
 * @author Littlesnowman88
 */
public class DataConnection extends AsyncTask<String, Integer, String> {

    private String dbEndpoint, httpRequest;
    private JSONObject jsonData;
    private EventsData dataHolder = EventsData.getInstance();

    /**
     * Constructor: creates the AsyncTask and sets important class variables.
     *
     * @param endpoint    the database API endpoint attached at the end of the base url
     * @param httpRequest GET, POST, PUT, or DELETE
     * @author Littlesnowman88
     */
    public DataConnection(@NonNull String endpoint, @NonNull String httpRequest, @Nullable JSONObject data) {
        super();
        this.dbEndpoint = endpoint;
        this.httpRequest = httpRequest;
        this.jsonData = data;
    }

    /**
     * accesses the request type (GET, POST, PUT, or DELETE) of this connection.
     * @return httpRequest (GET, POST< PUT, or DELETE)
     * @author Littlesnowman88
     */
    public String getRequestType() {
        return httpRequest;
    }


    /**
     * Fetches data from NetworkUtils in the background so the UI thread isn't held up by data fetching
     *
     * @return information corresponding to the app's http request (GET, POST, PUT, DELETE);
     * @author Littlesnowman88
     */
    @Override
    protected String doInBackground(String... strings) {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        switch (httpRequest) {
            case "GET":
                if (dbEndpoint.equals("events")) {
                    String result = NetworkUtils.getEventInfo(dbEndpoint);
                    parseJSON(result);
                }
                break;
            case "POST":
                throw new RuntimeException("ERROR: POST NOT YET IMPLEMENTED");
                //break;
            case "PUT":
                throw new RuntimeException("ERROR: PUT NOT YET IMPLEMENTED");
                //break;
            case "DELETE":
                throw new RuntimeException("ERROR: DELETE NOT YET IMPLEMENTED.");
                //break;
            default:
                return null;
        }
        return null;
    }

    /**
     * parseJSON takes a JSON received from fetchData and converts valid event json items into event objects.
     * parseJSON also sorts events into potential and confirmed events.
     *
     * @author Littlesnowman88
     */
    private void parseJSON(String data) {
        if (data == null || data.startsWith("ERROR") || data.length() == 0) return;
        try {
            //declare scope-necessary variables
            JSONObject jsonObject = new JSONObject(data);
            Event builtEvent;
            try { //if get request was for events, parse from a list of items.
                dataHolder.clearEvents();
                JSONArray itemsArray = jsonObject.getJSONArray("items");
                int num_events = itemsArray.length();
                for (int i = 0; i < num_events; i++) {
                    JSONObject event = itemsArray.getJSONObject(i); //get the current event
                    try {
                        builtEvent = parseJSONEvent(event); //parse the data
                        dataHolder.placeEvent(builtEvent); //categorize the event
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        Log.d("PARSE EVENT: ", "OFFENDING JSON:\n" + event.toString());
                    }
                }
            } catch (Exception e) {
                //here, the get request was sent to event/:id
                try {
                    builtEvent = parseJSONEvent(jsonObject); //parse the data
                    dataHolder.placeEvent(builtEvent); //categorize the event
                } catch (RuntimeException re) {
                    re.printStackTrace();
                    Log.d("PARSE EVENT: ", "OFFENDING JSON:\n" + jsonObject.toString());
                }
            }
        } catch (Exception e) {
            Log.d("MockDB Parse: ", "ERROR: some connection failed.");
        }

    }

    /**
     * parseJSONEvent is a helper function that turns a JSON event into a java Event class.
     *
     * @param eventObj the JSONObject version of a single event
     * @return constructedEvent, the event built from the JSON
     * else null if the event could not be built.
     * @author Littlesnomwan88
     */
    private static Event parseJSONEvent(JSONObject eventObj) throws RuntimeException {
        String host, title, loc, desc;
        double cost;
        int threshold, capacity;

        Event event = new Event();

        //host
        host = "TO BE DETERMINED";
        event.setHost(host);

        //title
        try {
            title = eventObj.getString("title");
        } catch (org.json.JSONException jse) {
            title = "";
        }
        event.setTitle(title);

        //date and time
        try {
            //first, get the timestamp from the database
            String timestamp = eventObj.getString("time");
            //then, prepare to convert the timestamp into Calendar-compatible and Event-compatible format.
            //start with format strings
            String timestampFormat = "yyyy-MM-dd'T'HH:mm:ss";
            String dateFormat = "MM/dd/yy";
            String timeFormat = "HH:mm";
            //then, create the formatters.
            SimpleDateFormat tsFormatter = new SimpleDateFormat(timestampFormat, Locale.US);
            SimpleDateFormat dFormatter = new SimpleDateFormat(dateFormat, Locale.US);
            SimpleDateFormat tFormatter = new SimpleDateFormat(timeFormat, Locale.US);
            //initialize the Calendars.
            Calendar tsCal = Calendar.getInstance();
            Calendar dCal = Calendar.getInstance();
            Calendar tCal = Calendar.getInstance();
            //set calendar date and/or time.
            try {
                //first, set the timestamp calendar.
                try {
                    tsCal.setTime(tsFormatter.parse(timestamp));
                } catch (ParseException pe) {
                    throw new RuntimeException("ERROR: timestamp was not valid form of yyyy-MM-dd'T'HH:mm:ss");
                }
                //then, derive date calendar
                dCal.set(Calendar.YEAR, tsCal.get(Calendar.YEAR));
                dCal.set(Calendar.MONTH, tsCal.get(Calendar.MONTH));
                dCal.set(Calendar.DAY_OF_MONTH, tsCal.get(Calendar.DAY_OF_MONTH));
                //finally, derive time calendar
                tCal.set(Calendar.HOUR_OF_DAY, tsCal.get(Calendar.HOUR_OF_DAY));
                tCal.set(Calendar.MINUTE, tsCal.get(Calendar.MINUTE));

                //now that the calendars are derived, set the event date and time.
                String eventDate = dFormatter.format(dCal.getTime());
                String eventTime = tFormatter.format(tCal.getTime());
                //runtime exceptions for setDate and setTime should be caught by the "//date and time" try-catch block.above.
                event.setDate(eventDate);
                event.setTime(eventTime);

            } catch (ParseException pe) {
                throw new RuntimeException("ERROR: Date and/or Time incorrect.\n Caused by: " + pe.getLocalizedMessage());
            }
        } catch (org.json.JSONException jse) {
            throw new RuntimeException("ERROR: Date and/or Time not detected.");
        }

        //location
        try {
            loc = eventObj.getString("location");
        } catch (org.json.JSONException jse) {
            loc = "";
        }
        event.setLocation(loc);

        //cost
        try {
            cost = eventObj.getDouble("cost)");
        } catch (org.json.JSONException jse) {
            cost = 0.0;
        }
        event.setCost(cost);

        //threshold
        try {
            threshold = eventObj.getInt("threshold");
            event.setMinThreshold(threshold);
        } catch (org.json.JSONException jse) {
            throw new RuntimeException("ERROR: Minimun Threshold not detected.");
        }

        //capacity
        try {
            capacity = eventObj.getInt("capacity");
            event.setMaxCapacity(capacity);
        } catch (org.json.JSONException jse) {
            throw new RuntimeException("ERROR: Maximum Capacity not detected.");
        }

        //description
        try {
            desc = eventObj.getString("description");
        } catch (org.json.JSONException jse) {
            desc = "";
        }
        event.setDescription(desc);

        //current interest count
        try {
            int current_count = eventObj.getInt("count");
            event.setCurrentInterest(current_count);
        } catch (org.json.JSONException jse) {
            throw new RuntimeException("ERROR: Current Interest not detected.");
        }

        return event;
    }
}
