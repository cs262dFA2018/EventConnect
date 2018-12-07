package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;

import edu.calvin.cs262.cs262d.eventconnect.data.Event;

/**
 * DataManager manages DataConnection requests, enforcing that two requests are not happening simultaneously happening.
 * In essence, this class prevents Read/Write conflicts with the cloud database.
 * Because this manages DataConnections that can be requested from many places, this class is a singleton.
 * LOGIN ACTIVITY SHOULD NOT ACCESS THIS CLASS. MainActivity needs to be the first object to call this class,
 * because this class broadcasts updates to MainActivity.
 *
 * @author Littlesnowman88
 */
public class DataManager  implements TasksCompleted {

    /* TODO:
     * in UI, if something is edited, buttons may need to be disabled so multiple, identical requests cannot be made.
     */

    private static final int ONE_SECOND = 1000;
    private static final int ONE_MINUTE = ONE_SECOND * 60;
    private static final int ONE_HOUR = ONE_MINUTE * 60;
    private static final int NEVER = -1;

    //timer that ticks every 30 seconds to see if the app should GET updates. //TODO: Change to shared preference values later.
    //assign it here because only one ever needs to exist.
    private final Handler timer = new Handler();

    //the Asynchronous task responsible for making asynchronous dataConnections.
    //assign it here because only one ever needs to exist.
    private TaskManager tm;

    private static DataManager uniqueInstance; //implementing Singleton pattern
    private WeakReference<Context> contextRef; //the context to send Broadcasts to (when data has been updated and UI needs to change)

    private ArrayList<Event> potentialEvents;
    private ArrayList<Event> confirmedEvents;

    /**
     * getInstance() enforces that EventConnect can access only one instance of DataManager
     *
     * @return a new DataManager if this is the first call, uniqueInstance otherwise.
     * @author Littlesnowman88
     */
    public synchronized static DataManager getInstance(@NonNull WeakReference<Context> activityContext) {
        if (uniqueInstance == null) {
            uniqueInstance = new DataManager(activityContext);
        }
        return uniqueInstance;
    }

    /**
     * Constructor:
     * As the DataManager, this class holds the lists of events.
     * the DataManager also passes itself to DataConnection AsyncTasks so events can be fetched, added, modified, and deleted locally.
     * DataConnecton Manager creates DataConnections, but doesn't need to start with one.
     * Queue is abstract, so it doesn't need to be initialized.
     * The constructor is private, in accordance with the Singleton Pattern.
     *
     * @author Littlesnowman88
     */
    private DataManager(@NonNull WeakReference<Context> activityContext) {
        tm = new TaskManager(new WeakReference<>(this));
        contextRef = activityContext;
        potentialEvents = new ArrayList<>();
        confirmedEvents = new ArrayList<>();
        Runnable timerRunning = new Runnable() {
            @Override
            public void run() {
                //after the reset time, check for updates.
                try {
                    //makeHTTPRequest("events", "GET", null);
                } catch (Exception e) {
                    Log.w("DataManager", "failed to get data; " + e.getLocalizedMessage());
                }
                //after delayMillis milliseconds, run this runnable again.
                timer.postDelayed(this, ONE_SECOND * 5);
            }
        };
        //start timer
        timer.postDelayed(timerRunning, ONE_SECOND);
        //FIXME: eliminate this GET request
        DataConnection get = new DataConnection("events", "GET", new WeakReference<>(this), null);
        get.execute();
        try {
            get.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("DATA MANAGER: ", "get finished.");
    }

    /**
     * makeHTTPRequest filters out illegal httpRequests, then sends the legal http request to TaskManager for processing.
     *
     * @param endpoint    the database endpoint of the HTTP request.
     * @param httpRequest GET, POST, PUT, DELETE
     * @param data        the JSON associated with the httpRequest
     * @throws RuntimeException if the httpRequest is invalid.
     * @author Littlesnowman88
     */
    private void makeHTTPRequest(@NonNull String endpoint, @NonNull String httpRequest, @Nullable JSONObject data) throws RuntimeException {
        //if an invalid httpRequest is given, abort the constructor process.
        if (!httpRequest.equals("GET") && !httpRequest.equals("POST")
                && !httpRequest.equals("PUT") && !httpRequest.equals("DELETE")) {
            throw new RuntimeException("ERROR: could not handle DataConnection with httpRequest " + httpRequest + ". Must be GET, POST, PUT, or DELETE.");
        }
        tm.makeRequest(endpoint, httpRequest, data);
    }

    private static class TaskManager extends AsyncTask<String, Integer, String> {

        private ArrayDeque<DataConnection> connections;
        private DataConnection currentConnection;
        private WeakReference<DataManager> dataHolder;

        /**
         * empty public constructor. Connections doesn't need initialization (abstract). myCurrentTask is initialized later.
         *
         * @author Littlesnowman88
         */
        private TaskManager(WeakReference<DataManager> rootManager) {
            dataHolder = rootManager;
            connections = new ArrayDeque<>();
        }

        /**
         * makeRequest is called by DataManager. This method appropriately adds connection requests to the connections queue.
         *
         * @param end     an http request's endpoint
         * @param request an http request's type (GET, POST, PUT, DELETE)
         * @param json    optional JSON data to be sent with the http request.
         * @author Littlesnowman88
         */
        private void makeRequest(@NonNull String end, @NonNull String request, @Nullable JSONObject json) {

            //if TaskManager is already processing other connections, it will do a GET at the end of those. GET here is thus unnecessary.
            if (currentConnection != null && end.equals("GET")) {
                return;
            }

            //now, give the requested connection to the TaskManager for handling.
            connections.addLast(new DataConnection(end, request, dataHolder, json));
            //FIXME: figure out why doInBackground doesn't seem to end...
            if (getStatus() != Status.RUNNING) {
                execute();
            }
        }


        /**
         * doInBackground runs on a separate, AsyncTask thread.
         * Here, doInBackground synchronously handles all http requests that TaskManager has received by the app.
         *
         * @param strings not used.
         * @return null. No status to report.
         * @author Littlesnowman88
         *
         * FIXME: figure out why doInBackground doesn't seem to end...
         */
        @Override
        protected String doInBackground(String... strings) {
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();
            processConnections();
            currentConnection = null; //once all connections have been processed, update state.
            return "finished";
        }

        /**
         * helper function for doInBackground. Runs events in the connections queue.
         *
         * @author Littlesnowman88
         */
        private void processConnections() {
            processWriteConnections();
            //now that all of the writing requests have been made, do a final GET request.
            processFinalGet();
            //now, make sure no other write requests snuck into the queue while processing the final GET request.
            while (connections.peek() != null) {
                DataConnection request = connections.poll();
                if (!request.getRequestType().equals("GET")) {
                    //a write connection snuck into the queue while processing the final GET request, so start over.
                    processConnections();
                }
            }
        }

        /**
         * processWriteConnections iterates through the connections queue, running only PUT, POST, and DELETE requests.
         * @author Littlesnowman88
         */
        private void processWriteConnections() {
            while (connections.peek() != null) {
                //get the first http request.
                currentConnection = connections.poll();
                //get through all write requests before doing any read requests.
                if (currentConnection.getRequestType().equals("GET")) {
                    continue;
                }
                //run the connection and wait for it to finish. This waiting has to happen inside of asynchronous doInBackground.
                processConnection();
            }
        }

        /**
         * makes a getRequest for all events and processes that getRequest.
         */
        private void processFinalGet() {
            currentConnection = new DataConnection("events", "GET", dataHolder, null);
            processConnection();
        }

        /**
         * helper function for processConnections. Synchronously runs a single http request.
         *
         * @author Littlesnowman88
         */
        private void processConnection() {
            currentConnection.execute();
            try {
                currentConnection.get();
                Log.d("Request Complete: ", "HTTP request of type " + currentConnection.getRequestType() + " completed.");
            } catch (java.util.concurrent.ExecutionException ee) {
                //caught if currentConnection throws an exception.
                //TODO: Be more gracious with this statement later, once you have completed more testing.
                throw new RuntimeException("ERROR: SEE LOG:\n--->" + ee.getLocalizedMessage());
            } catch (java.lang.InterruptedException ie) {
                ie.printStackTrace();
                //if a currently running task (especially a GET task) was cancelled, move onto the next task in the queue.
            }
        }

        /**
         * Tells DataManager to update MainActivity's data.
         * @param s the result of doInBackground.
         * @author Littlesnowman88
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null && s.equals("finished")) {
                dataHolder.get().onTasksComplete();
            }
        }
    }

    /**
     * called by TaskManager's onPostExecute, onTaskComplete sends a broadcast to MainActivity
     * that broadcast tells MainActivity's TabFragments to update their EventData.
     * @author Littlesnowman88
     */
    @Override
    public void onTasksComplete() {
        //FIXME: pass more substantive data.
        Intent intent = new Intent("dataUpdate");
        intent.putExtra("message", "THIS MESSAGE is from DataManager. You should see it in TabFragment.");
        LocalBroadcastManager.getInstance(contextRef.get()).sendBroadcast(intent);
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
}
