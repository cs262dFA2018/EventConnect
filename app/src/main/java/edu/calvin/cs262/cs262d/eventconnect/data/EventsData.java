package edu.calvin.cs262.cs262d.eventconnect.data;

import java.util.ArrayList;

public final class EventsData {

    private ArrayList<Event> potentialEvents, confirmedEvents, myEvents;

    private static EventsData uniqueInstance;


    public static synchronized EventsData getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new EventsData();
        }
        return uniqueInstance;
    }

    private EventsData() {
        potentialEvents = new ArrayList<>();
        confirmedEvents = new ArrayList<>();
        myEvents = new ArrayList<>();
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
     * Adds event to myEvents when user indicates interest
     *
     * @param eventInterested to add to myEvents
     * @author ksn7
     */
    public void addInterest(Event eventInterested) {
        if (!myEvents.contains(eventInterested)) {
            myEvents.add(eventInterested);
        }
    }

    /**
     * Removes event from myEvents when user un-indicates interest
     *
     * @param eventNotInterested to remove from myEvents
     * @author ksn7
     */
    public void removeInterest(Event eventNotInterested) {
        try {
            myEvents.remove(eventNotInterested);
        } catch (Exception e) {
            throw new RuntimeException("ERROR: tried to remove interest from an event not in My Events");
        }
    }

    /**
     * accessor for list of potentialEvents
     *
     * @return a reference to MockDatabases' potential events.
     * @author Littlesnowman88
     */
    public ArrayList<Event> getPotentialEventData() {
        return this.potentialEvents;
    }

    /**
     * accessor for list of confirmedEvents
     *
     * @return a reference to MockDatabases' confirmed events.
     * @author Littlesnowman88
     */
    public ArrayList<Event> getConfirmedEventData() {
        return this.confirmedEvents;
    }

    /**
     * accessor for list of myEvents
     *
     * @return a reference to MockDatabases' list of events the device owner has indicated interest in
     * @author ksn7
     */
    public ArrayList<Event> getMyEventData() {
        return this.myEvents;
    }
}
