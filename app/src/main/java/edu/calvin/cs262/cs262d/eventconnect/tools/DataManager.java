package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.app.Service;
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
import edu.calvin.cs262.cs262d.eventconnect.data.EventsData;

/**
 * DataManager manages DataConnection requests..
 * This class prevents Read/Write conflicts with the cloud database.
 *
 * LOGIN ACTIVITY SHOULD NOT ACCESS THIS CLASS. MainActivity needs to be the first object to call this class,
 * because this class broadcasts updates to MainActivity.
 *
 * @author Littlesnowman88
 */
public class DataManager extends Service implements TasksCompleted {

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


    private WeakReference<Context> contextRef; //the context to send Broadcasts to (when data has been updated and UI needs to change)

    private EventsData dataHolder = EventsData.getInstance();
    private ArrayDeque<DataConnection> connections;
    private DataConnection currentConnection;

    /**
     * Constructor:
     *
     * @author Littlesnowman88
     */
    public DataManager() {
        connections = new ArrayDeque<>();
        Runnable timerRunning = new Runnable() {
            @Override
            public void run() {
                //after the reset time, check for updates.
                try {
                    makeHTTPRequest("events", "GET", null);
                } catch (Exception e) {
                    Log.w("DataManager", "failed to get data; " + e.getLocalizedMessage());
                }
                //after delayMillis milliseconds, run this runnable again.
                timer.postDelayed(this, ONE_SECOND * 5);
            }
        };
        //start timer
        timer.postDelayed(timerRunning, ONE_SECOND);
    }

    /**
     * makeHTTPRequest filters out illegal httpRequests, then adds the legal httpRequest to connections queue.
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
        if (currentConnection != null && endpoint.equals("GET")) {
            return;
        }
        connections.addLast(new DataConnection(endpoint, httpRequest, data));
    }

    /**
     * processes all tasks in the connections queue
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     *
     * @author Littlesnowman88
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //process the tasks?
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * processTasks synchronously handles all http requests in connections arraylist.
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
}
