package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import edu.calvin.cs262.cs262d.eventconnect.data.EventConnector;
import edu.calvin.cs262.cs262d.eventconnect.data.EventsData;


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

    //assign queue here because only one ever needs to exist.
    //timer that triggers event GET requests every TIMER_DELAY seconds.//TODO: Change to shared preference values later.
    private final Handler timer = new Handler();
    //self-repeating Runnables that timer and connectionProcessor run.
    private Runnable timerRunner;

    private EventsData dataSource = EventsData.getInstance();

    /**
     * Constructor:
     * Creates the GET request looper and the Connections Processing looper.
     *
     * @author Littlesnowman88
     */
    public DataManager() {
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
        String action = intent.getAction();

        if (action != null && action.equals("processConnections")) {
            //start GET request timer immediately
            timer.post(timerRunner);
            EventConnector ec = new EventConnector(getBaseContext());
            ec.getEvents();
        }
        else if (action != null && action.equals("loginToServer")) {
            EventConnector ec = new EventConnector(getBaseContext());
            //ec get user, check password, and authenticate??
        }
        return START_NOT_STICKY;
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
    }
}
