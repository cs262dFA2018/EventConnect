package edu.calvin.cs262.cs262d.eventconnect.data;

import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.jacksonandroidnetworking.JacksonParserFactory;

import java.util.ArrayList;
import java.util.List;

public final class EventsData {
    private static final String TAG = "EventsData";
    private EventConnector ec;
    private List<Event> potentialEvents;
    private List<Event> confirmedEvents;

    private static EventsData uniqueInstance;

    public static  EventsData getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new EventsData();
        }
        return uniqueInstance;
    }

    private EventsData() {
        potentialEvents = new ArrayList<>();
        confirmedEvents = new ArrayList<>();
    }

    public void initializeEventConnector(Context context){
        ec = new EventConnector();
        ec.initialize(context);
    }

    public void updateEvents(){
        clearEvents();
        ec.getEvents();
    }

    /**
     * placeEvent puts an event into its appropriate tab.
     * called by DataConnection AsyncTask's GET request.
     *
     * @author Littlesnomwan88
     */
    public void placeEvent(Event event) {
        if (!event.checkConfirmed()) {
            potentialEvents.add(event);
        } else {
            confirmedEvents.add(event);
        }
    }

    /**
     * clearEvents, called by DataConnection AsyncTask's GET request,
     * clears the Event data for all tabs in preparation for a GET request
     *
     * @author Littlesnowman88
     */
    public void clearEvents() {
        potentialEvents.clear();
        confirmedEvents.clear();
    }

    /**
     * adds a brand new event to the potential events list.
     * called by the AddEvent activity.
     *
     * @author Littlesnowman88
     */
    public void addNewEvent(Event event) {
        potentialEvents.add(event);
    }

    /**
     * deleteEvent removes an event from the database
     *
     * @param eventToDelete passed in event that needs to be deleted
     * @author ksn7
     */
    public void deleteEvent(Event eventToDelete) throws RuntimeException {

        // Iterate through the potential events, and delete the event if its found
        int num_events = potentialEvents.size();
        boolean eventFound = false;
        Event event;
        for (int i = 0; i < num_events; i++) {
            event = potentialEvents.get(i);
            if (event == eventToDelete) {
                potentialEvents.remove(event);
                num_events--;
                eventFound = true;
            }
        }

        // If the event was not found in the potential events, check the confirmed events
        num_events = confirmedEvents.size();
        if (!eventFound) {
            for (int i = 0; i < num_events; i++) {
                event = confirmedEvents.get(i);
                if (event == eventToDelete) {
                    confirmedEvents.remove(event);
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
        int num_events = potentialEvents.size();
        for (int i = 0; i < num_events; i++) {
            Event event = potentialEvents.get(i);
            if (event == eventToMove) {
                confirmedEvents.add(eventToMove);
                potentialEvents.remove(event);
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
        int num_events = confirmedEvents.size();
        for (int i = 0; i < num_events; i++) {
            Event event = confirmedEvents.get(i);
            if (event == eventToMove) {
                potentialEvents.add(eventToMove);
                confirmedEvents.remove(event);
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
    public List<Event> getPotentialEventData() {
        Log.d(TAG, potentialEvents.toString());
        return this.potentialEvents; }


    /**
     * accessor for list of confirmedEvents
     *
     * @return a reference to MockDatabases' confirmed events.
     * @author Littlesnowman88
     */
    public List<Event> getConfirmedEventData() {
        return this.confirmedEvents;
    }
}
