package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import edu.calvin.cs262.cs262d.eventconnect.data.EventConnector;
import edu.calvin.cs262.cs262d.eventconnect.data.EventsData;


/**
 * //TODO: comment this class.
 *
 * @author Littlesnowman88
 */
public class EventsPoller extends Service {

    /* TODO:
     * in UI, if something is edited, buttons may need to be disabled so multiple, identical requests cannot be made.
     */

    private static final int ONE_SECOND = 1000;
    private static final int ONE_MINUTE = ONE_SECOND * 60;
    private static final int ONE_HOUR = ONE_MINUTE * 60;
    private static final int NEVER = -1;

    private static final int TIMER_DELAY = ONE_SECOND * 5;

    //assign queue here because only one ever needs to exist.
    //timer that triggers event GET requests every TIMER_DELAY seconds.//TODO: Change to shared preference values later.
    private final Handler timer = new Handler();
    //self-repeating Runnables that timer and connectionProcessor run.
    private Runnable timerRunner;

    private EventsData dataSource;

    /**
     * Constructor:
     * Creates the GET request looper.
     *
     * @author Littlesnowman88
     */
    public EventsPoller() {
        //Create the GET request looper
        timerRunner = new Runnable() {
            @Override
            public void run() {
                //after the reset time, check for updates.
                    dataSource.updateEvents();
                //after timer delay, run this runnable again.
                timer.postDelayed(this, TIMER_DELAY);
            }
        };
    }

    /**
     * //TODO: COMMENT THIS
     *
     * @param intent  the Intent responsible for starting this Service. (Activity Source: MainActivity).
     * @param flags
     * @param startId
     * @return Service.START_NOT_STICKY, meaning that EventsPoller will stop running when MainActivity stops running.
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
        String action = intent.getAction();

        if (action != null && action.equals("processConnections")) {
            //create an EventConnector for getting data from server.
            EventConnector ec = new EventConnector(getBaseContext());
            //set the database's eventConnector so the database can tell the server to update data.
            dataSource = EventsData.getInstance(ec);

            //start GET request timer immediately
            timer.post(timerRunner);

        }
        return START_NOT_STICKY;
    }


    /**
     * required by the Service class, but the app doesn't do anything with it.
     *
     * @param arg0 not used because the app doesn't need to do anything with binding.
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
    }
}
