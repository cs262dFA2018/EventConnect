package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayDeque;


/**
 * DataManager manages DataConnection requests..
 * This class prevents Read/Write conflicts with the cloud database.
 * <p>
 * LOGIN ACTIVITY SHOULD NOT ACCESS THIS CLASS. MainActivity needs to be the first object to call this class,
 * because this class broadcasts updates to MainActivity.
 *
 * @author Littlesnowman88
 */
public class DataManager extends Service {

    /* TODO:
     * in UI, if something is edited, buttons may need to be disabled so multiple, identical requests cannot be made.
     */

    private static final int ONE_SECOND = 1000;
    private static final int ONE_MINUTE = ONE_SECOND * 60;
    private static final int ONE_HOUR = ONE_MINUTE * 60;
    private static final int NEVER = -1;

    private static final int TIMER_DELAY = ONE_SECOND * 5;

    //assign timer and queue here because only one of each ever needs to exist.
    //timer that triggers event GET requests every TIMER_DELAY seconds.//TODO: Change to shared preference values later.
    private final Handler timer = new Handler();
    //background queue for processing all httpRequests made by the application.
    private final Handler queue = new Handler();

    //self-repeating Runnables that timer and connectionProcessor run.
    private Runnable timerRunner, connectionProcessor;

    //data used by connectionProcessor to keep track of httpRequests.
    private ArrayDeque<DataConnection> connections;
    private DataConnection currentConnection;

    //saved by onStartCommand, this intent allows DataManager to signal MainActivity when UI updates need to be made (after GET requests).
    private Intent startingIntent;

    /**
     * Constructor:
     * Creates the GET request looper and the Connections Processing looper.
     *
     * @author Littlesnowman88
     */
    public DataManager() {
        connections = new ArrayDeque<>();
        //Create the GET request looper
        timerRunner = new Runnable() {
            @Override
            public void run() {
                //after the reset time, check for updates.
                try {
                    makeHTTPRequest("events", "GET", null);
                } catch (Exception e) {
                    Log.w("DataManager", "failed to get data; " + e.getLocalizedMessage());
                }
                //after timer delay, run this runnable again.
                timer.postDelayed(this, TIMER_DELAY);
            }
        };
        //Create the queue processing looper.
        connectionProcessor = new Runnable() {
            @Override
            public void run() {
                //process the connections queue.
                processConnections();

                //once finished processing all requested connections, tell MainActivity to update its UI
                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(startingIntent);
                currentConnection = null; //once all connections have been processed, update state.

                //After 2 Timer Delays (optimization based on Pigeonhole Principle), process auto-generated GET requests.
                queue.postDelayed(this, TIMER_DELAY * 2);
            }
        };
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
    public void makeHTTPRequest(@NonNull String endpoint, @NonNull String httpRequest, @Nullable JSONObject data) throws RuntimeException {
        //if an invalid httpRequest is given, abort the constructor process.
        if (!httpRequest.equals("GET") && !httpRequest.equals("POST")
                && !httpRequest.equals("PUT") && !httpRequest.equals("DELETE")) {
            throw new RuntimeException("ERROR: could not handle DataConnection with httpRequest " + httpRequest + ". Must be GET, POST, PUT, or DELETE.");
        }

        //if a GET request is already being handled, don't ask to make a second one immediately after. Let the GET request timer take care of that.
        if (currentConnection != null && endpoint.equals("GET")) {
            return;
        }
        //add the legal httpRequest to the queue of httpRequests.
        connections.addLast(new DataConnection(endpoint, httpRequest, data));
    }

    /**
     * Triggers the GET request loop and the connectionProcessor loop.
     * Because processConnection() waits for its connection to finish, the queue Handler
     * must be started from onStartCommand for connection processing to happen in background.
     *
     * @param intent  the Intent responsible for starting this Service. (Activity Source: MainActivity).
     * @param flags
     * @param startId
     * @return Service.START_NOT_STICKY, meaning that DataManager will stop running when MainActivity stops running.
     * @author Littlesnowman88
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();

        /*an extra layer of difficulty, I admit, but I am trying to force future developers
         * to be VERY careful about changing this service's design assumption.
         * This service is intended to be run from MainActivity.
         * If a future dev wants to change this assumption, that dev must come to here and
         * (ideally) read this comment at least once. - LS88
         */
        if (intent.getAction() != null && intent.getAction().equals("processConnections")) {
            startingIntent = intent;

            //start GET request timer immediately
            timer.post(timerRunner);
            //after that, start begin the background processing of httpRequests.
            queue.postDelayed(connectionProcessor, 200);
        }
        return START_NOT_STICKY;
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
     *
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
            processConnection(currentConnection);
        }
    }

    /**
     * makes a getRequest for all events and processes that getRequest.
     */
    private void processFinalGet() {
        currentConnection = new DataConnection("events", "GET", null);
        processConnection(currentConnection);
    }

    /**
     * helper function for processConnections. Synchronously runs a single http request.
     *
     * @author Littlesnowman88
     */
    private static void processConnection(DataConnection connection) {
        connection.execute();
        try {
            connection.get();
            Log.d("Request Complete: ", "HTTP request of type " + connection.getRequestType() + " completed.");
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
     * required by the Service class, but the app doesn't do anything with it.
     *
     * @param arg0
     * @return null because the app doesn't need to do anything with binding.
     * @author Littlesnowman88
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * clear any currently and remaining threads before shutting down the service.
     *
     * @author Littlesnowman88
     */
    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
        connections.clear();
        if (currentConnection != null) {
            currentConnection.cancel(true);
        }
    }
}
