package edu.calvin.cs262.cs262d.eventconnect.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public final class EventsData {

    private static EventsData uniqueInstance;

    private static final String TAG = "EventsData";
    private static EventConnector ec;
    private ArrayList<Event> potentialEvents, confirmedEvents, myEvents;
    private ArrayList<UserDAO> users;
    private String userEmail, userPass;

    /**
     * Singleton constructor, effectively.
     * NOTE: getInstance must be called once with a NonNull eventConnector for many of the EventsData functions to work!!!
     * @param eventConnector The class responsible for getting/sending data with the server.
     *                       Nullable so Fragments can access the database and make changes.
     *                       MUST BE CALLED AT LEAST ONCE WITH A NONNULL EVENTCONNECTOR for this class to work!
     *                       (ideally, MainActivity)
     * @return uniqueInstance, the one and only ever one instance of EventsData. Yay Singleton pattern!
     *
     * @author Littlesnowman88
     */
    public static synchronized EventsData getInstance(@Nullable EventConnector eventConnector) {
        if (uniqueInstance == null) {
            uniqueInstance = new EventsData();
        }
        if (eventConnector != null) ec = eventConnector;
        return uniqueInstance;
    }

    /**
     * Constructor.
     * Creates blank lists for the TabFragments
     *
     * @author Littlesnowman88
     */
    private EventsData() {
        potentialEvents = new ArrayList<>();
        confirmedEvents = new ArrayList<>();
        myEvents = new ArrayList<>();
        users = new ArrayList<>();
    }
    
    public void setCredentials(@NonNull String username, @NonNull String password) {
        userEmail = username;
        userPass = password;
    }

    public String[] getCredentials() {
        String[] creds = {userEmail, userPass};
        return creds;
    }

    /**
     * updateEvents gets all users, then populates events tabs, then
     *  populates the MyEvents tab.
     *
     * @author Littlesnowman88
     */
    public void updateEvents(){
        ec.getUsers();
        ec.getMyEvents(userEmail, userPass);
        ec.getEvents();

    }

    /**
     * called by EventConnector, setEvents sets the potential and confirmed events
     *
     * @param potentials, the list of potential events fetched from the server
     * @param confirmed, the list of confirmed events fetched form the server
     * @author Littlesnowman88
     */
    public void setEvents(@NonNull ArrayList<Event> potentials,
                          @NonNull ArrayList<Event> confirmed) {
        potentialEvents = potentials;
        confirmedEvents = confirmed;
    }

    /**
     * called by EventConnector, setUsers sets the local data's user list
     *
     * @param existingUsers, the list of users that exist in the database
     * @author Littlesnowman88
     */
    public void setUsers(@NonNull ArrayList<UserDAO> existingUsers) {
        users = existingUsers;
    }

    /**
     * called by EventConnector, setMyEvents sets the MyEvents tab's data.
     *
     * @param my_events, the events fetched from getMyEvents
     * @author Littlesnowman88
     */
    public void setMyEvents(@NonNull ArrayList<Event> my_events) {
        myEvents = my_events;
    }

    /**
     * adds a brand new event to the potential events list.
     * called by the AddEvent activity.
     *
     * @param event the new event created by AddEvent
     * @author Littlesnowman88
     */
    public void addNewEvent(@NonNull Event event) {
        ec.postEvent(event, userEmail, userPass);
        ec.joinEvent(event, userEmail, userPass);
        ec.getEvents();
    }

    /**
     * edits an event.
     * called by EditEvent activity
     *
     * @param event the edited event from EditEvent
     * @author Littlesnomwan88
     */
    public void editEvent(@NonNull Event event) {
        ec.putEvent(event, userEmail, userPass);
        ec.joinEvent(event, userEmail, userPass);
        ec.getEvents();
    }

    /**
     * deleteEvent removes an event from the database
     *
     * @param eventToDelete passed in event that needs to be deleted
     * @author Littlesnowman88
     */
    public void deleteEvent(@NonNull Event eventToDelete) throws RuntimeException {
        ec.deleteEvent(eventToDelete, userEmail, userPass);
        ec.getEvents();
    }

    /**
     * Adds the currently logged in user to an event, thus updating MyEvents and an Event's interest count.
     *
     * @param eventInterested to add to myEvents
     * @author Littlesnowman88
     * @author ksn7
     */
    public void join(Event eventInterested) {
        ec.joinEvent(eventInterested, userEmail, userPass);
        ec.getEvents();
    }


    /**
     * Removes the currently logged in user from an event, thus updating MyEvents and an Event's interest count.
     *
     * @param eventNotInterested to remove from myEvents
     * @author Littlesnowman88
     * @author ksn7
     */
    public void leave(Event eventNotInterested) {
        ec.unjoinEvent(eventNotInterested, userEmail, userPass);
        ec.getEvents();
    }

    /* =========================== */
    /**
     * accessor for list of potentialEvents
     *
     * @return a reference to MockDatabases' potential events.
     * @author Littlesnowman88
     */
    public ArrayList<Event> getPotentialEventData() {
        return this.potentialEvents; }


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


    //Utility methods for EventConnector
    /**
     * getUsername takes a userId, finds the corresponding user, and returns the username.
     * Translates an eventID to an eventHost.
     *
     * @param userId the unique id of a UserDAO user.
     * @return a user's username if found, the stringified userid otherwise.
     */
    public String getUsername(@NonNull int userId) {
        for (UserDAO user : users) {
            if (user.getId() == userId) {
                return user.getUsername();
            }
        }
        return Integer.toString(userId);
    }

    /**
     * getUserId takes a username, finds the corresponding user, and returns the userID.
     * Translates an eventHost into an eventID.
     *
     * @param username the unique id of a UserDAO user.
     * @return a user's id if found, 0 if otherwise.
     */
    public int getUserId(@NonNull String username) {
        for (UserDAO user : users) {
            if (user.getUsername().equals(username)) {
                return user.getId();
            }
        }
        return 0;
    }
}
