package edu.calvin.cs262.cs262d.eventconnect.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import edu.calvin.cs262.cs262d.eventconnect.tools.TempDataTask;

/**
 * MockDatabase singleton class provides the app with default event objects to use.
 * Originally, this database contained 3 hard-coded, hand-built events.
 * Now, the MockDatabase makes 1 "GET events" call to the Google Cloud database.
 * After the GET events command, the MockDatabase currently handles all data changes locally.
 * -Littlesnowman88
 */
public class MockDatabase {
    private String jsonData;
    private ArrayList<Event> potentialEventData, confirmedEventData;
    private static MockDatabase uniqueInstance = null;

    /**
     * getInstance, following the singleton pattern, enforces that 1 and only 1 instance of
     * MockDatabase can exist.
     *
     * @return uniqueInstance, the 1 and only 1 instance of MockDatabase.
     * @author Littlesnowman88
     */
    public synchronized static MockDatabase getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new MockDatabase();
        }
        return uniqueInstance;
    }

    /**
     * MockDatabase private constructor makes 2 empty lists for events and then fetches data.
     * Private constructor upholds the singleton pattern. App should have 1 and only 1 instance of MockDatabase.
     *
     * @author Littlesnowman88
     */
    private MockDatabase() {
        potentialEventData = new ArrayList<Event>();
        confirmedEventData = new ArrayList<Event>();
        fetchData();
    }

    /**
     * fetchData creates a TempDataTask AsyncTask to get all events from the server
     * if success, calls parseJSON to build and sort events
     * if fails, aborts the event-building process.
     *
     * @author Littlesnowman88
     */
    private void fetchData() {
        //create and execute the async data fetcher
        TempDataTask fetcher = new TempDataTask("events");
        fetcher.execute();
        try {
            //wait for the async task to complete, and once it has finished, save the json
            jsonData = fetcher.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //if the returned json is a developer-produced error, or if the returned json is blank, abort.
        if (jsonData.startsWith("ERROR") || jsonData.length() == 0) {
            return;
        }
        //else, build and sort events!
        parseJSON();
    }

    /**
     * parseJSON takes a JSON received from fetchData and converts valid event json items into event objects.
     * parseJSON also sorts events into potential and confirmed events.
     *
     * @author Littlesnowman88
     */
    private void parseJSON() {
        try {
            //declare scope-necessary variables
            JSONObject jsonObject = new JSONObject(jsonData);
            Event builtEvent;
            try { //if get request was for events, parse from a list of items.
                JSONArray itemsArray = jsonObject.getJSONArray("items");
                int num_events = itemsArray.length();
                for (int i = 0; i < num_events; i++) {
                    JSONObject event = itemsArray.getJSONObject(i); //get the current event
                    try {
                        builtEvent = parseJSONEvent(event); //parse the data
                        placeEvent(builtEvent); //categorize the event
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        Log.d("PARSE EVENT: ", "OFFENDING JSON:\n" + event.toString());
                    }
                }
            } catch (Exception e) {
                //here, the get request was sent to event/:id
                try {
                    builtEvent = parseJSONEvent(jsonObject); //parse the data
                    placeEvent(builtEvent); //categorize the event
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
    private Event parseJSONEvent(JSONObject eventObj) throws RuntimeException {
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

    /**
     * placeEvent is a helper function that puts an event into its appropriate tab.
     *
     * @author Littlesnomwan88
     */
    private void placeEvent(Event event) {
        if (!event.checkConfirmed()) {
            potentialEventData.add(event);
        } else {
            confirmedEventData.add(event);
        }
    }

    /**
     * adds a brand new event to the potential events list.
     * called by the AddEvent activity.
     *
     * @author Littlesnowman88
     */
    public void addNewEvent(Event event) {
        potentialEventData.add(event);
    }

    /**
     * deleteEvent removes an event from the database
     *
     * @param eventToDelete passed in event that needs to be deleted
     * @author ksn7
     */
    public void deleteEvent(Event eventToDelete) {

        // Iterate through the potential events, and delete the event if its found
        int num_events = potentialEventData.size();
        boolean eventFound = false;
        Event event;
        for (int i = 0; i < num_events; i++) {
            event = potentialEventData.get(i);
            if (event == eventToDelete) {
                potentialEventData.remove(event);
                num_events--;
                eventFound = true;
            }
        }

        // If the event was not found in the potential events, check the confirmed events
        num_events = confirmedEventData.size();
        if (!eventFound) {
            for (int i = 0; i < num_events; i++) {
                event = confirmedEventData.get(i);
                if (event == eventToDelete) {
                    confirmedEventData.remove(event);
                    num_events--;
                    eventFound = true;
                }
            }
        }

        // If the event was not found anywhere, throw an error
        if (!eventFound) {
            throw new RuntimeException("ERROR: tried to delete an event not in the database");
        }
    }

    /**
     * moves an event from the potential tab to the confirmed tab.
     *
     * @param eventToMove the event moving from potentialEvents to confirmedEvents
     * @author ???
     */
    public void movePotentialEvent(Event eventToMove) {
        int num_events = potentialEventData.size();
        for (int i = 0; i < num_events; i++) {
            Event event = potentialEventData.get(i);
            if (event == eventToMove) {
                confirmedEventData.add(eventToMove);
                potentialEventData.remove(event);
                num_events--;
            }
        }
    }

    /**
     * moves an event from the confirmed tab to the potential tab.
     *
     * @param eventToMove the event moving from confirmedEvents to potentialEvents
     * @author ???
     */
    public void moveCompletedEvent(Event eventToMove) {
        int num_events = confirmedEventData.size();
        for (int i = 0; i < num_events; i++) {
            Event event = confirmedEventData.get(i);
            if (event == eventToMove) {
                potentialEventData.add(eventToMove);
                confirmedEventData.remove(event);
                num_events--;
            }
        }
    }

    /**
     * accessor for list of potentialEvents
     *
     * @return a reference to MockDatabases' potential events.
     * @author Littlesnowman88
     */
    public ArrayList<Event> getPotentialEventData() {
        return this.potentialEventData;
    }

    /**
     * accessor for list of confirmedEvents
     *
     * @return a reference to MockDatabases' confirmed events.
     * @author Littlesnowman88
     */
    public ArrayList<Event> getConfirmedEventData() {
        return this.confirmedEventData;
    }
}
