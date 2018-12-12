package edu.calvin.cs262.cs262d.eventconnect.data;

import android.content.Context;
import android.util.Log;
import android.util.Base64;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jacksonandroidnetworking.JacksonParserFactory;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

/**
 * This class GETs, POSTs, PUTs, and DELETEs Events and Users using the EventConnectServer API
 * Each method corresponds to an API endpoint
 *
 * @author Theron Tjapkes (tpt3)
 */

public class EventConnector {

    private static final String TAG = "EventConnector";
    private static final String BASE_URL = "https://calvincs262-fall2018-cs262d.appspot.com/eventconnect/v1/";

    private List<Event> eventList;

    /**
     * This class holds the list of events
     * need because GET to /events returns an array
     *
     * @author Theron Tjapkes (tpt3)
     */
    private static class EventJsonDataHolder {
        @JsonProperty("items")
        public List<EventDAO> EventsDAOList;
    }

    /**
     * Initializes AndroidNetworking and sets the parser
     *
     * @param context Required to initialize AndroidNetworking
     * @author Theron Tjapkes (tpt3)
     */
    public void initialize(Context context) {
        AndroidNetworking.initialize(context);
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
    }


    // Events

    /**
     * Does a GET request and updates EventsData using placeEvent()
     * Corresponds to /events described here:
     * https://github.com/cs262dFA2018/EventConnectServer/wiki/Event-endpoints
     *
     * @author Theron Tjapkes (tpt3)
     */
    public void getEvents() {
        AndroidNetworking.get(BASE_URL + "events")
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsObject(EventJsonDataHolder.class, new ParsedRequestListener<EventJsonDataHolder>() {
                    @Override
                    public void onResponse(EventJsonDataHolder events) {
                        for (EventDAO event : events.EventsDAOList) {
                            EventsData.getInstance().placeEvent(EventDAOtoEvent(event));
                        }
                        postEvent(EventsData.getInstance().getPotentialEventData().get(0), "TestUser", "TestPass");
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                        Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                        Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                        throw new RuntimeException("ERROR: errorCode " + anError.getErrorCode()
                                + " errorBody " + anError.getErrorBody()
                                + " errorDetail " + anError.getErrorDetail());
                    }
                });
    }

    /**
     * UNTESTED since we won't be using it
     * Does a GET request and updates EventsData using placeEvent()
     * Corresponds to /event/{id} described here:
     * https://github.com/cs262dFA2018/EventConnectServer/wiki/Event-endpoints
     *
     * @author Theron Tjapkes (tpt3)
     */
    public void getEvent(int id) {
        AndroidNetworking.get(BASE_URL + "event/" + id)
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsObject(EventDAO.class, new ParsedRequestListener<EventDAO>() {
                    @Override
                    public void onResponse(EventDAO event) {
                        EventsData.getInstance().placeEvent(EventDAOtoEvent(event));
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                        Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                        Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                        throw new RuntimeException("ERROR: errorCode " + anError.getErrorCode()
                                + " errorBody " + anError.getErrorBody()
                                + " errorDetail " + anError.getErrorDetail());
                    }
                });
    }

    /**
     * POSTs an event to the API
     * Corresponds to /event/{token} described here:
     * https://github.com/cs262dFA2018/EventConnectServer/wiki/Event-endpoints
     *
     * @param event    the event to POST
     * @param username Username the event creator
     * @param password Password of the event creator
     * @author Theron Tjapkes (tpt3)
     */
    public void postEvent(Event event, String username, String password) {
        EventDAO eventDAO = EventToEventDAO(event);
        String base64UsernamePassword = encodeBase64(username + ":" + password);
        AndroidNetworking.post(BASE_URL + "event/" + base64UsernamePassword)
                .addBodyParameter(eventDAO) // posting java object
                .setTag(this)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsObject(EventDAO.class, new ParsedRequestListener<EventDAO>() {
                    @Override
                    public void onResponse(EventDAO event) {
                        // do something with created event
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d(TAG, "onError errorCode : " + error.getErrorCode());
                        Log.d(TAG, "onError errorBody : " + error.getErrorBody());
                        Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                        throw new RuntimeException("ERROR: errorCode " + error.getErrorCode()
                                + " errorBody " + error.getErrorBody()
                                + " errorDetail " + error.getErrorDetail());
                    }
                });
    }

    /**
     * Does a PUT request to indicate the user has joined an event
     * Corresponds to /event/{id}/join/{token} described here:
     * https://github.com/cs262dFA2018/EventConnectServer/wiki/Event-endpoints
     *
     * @param event    the event to join MUST HAVE ID SET(which is done by getEvents())
     * @param username Username the user joining
     * @param password Password of the user joining
     * @author Theron Tjapkes (tpt3)
     */
    public void joinEvent(Event event, String username, String password) {
        EventDAO eventDAO = EventToEventDAO(event);
        String base64UsernamePassword = encodeBase64(username + ":" + password);
        AndroidNetworking.put(BASE_URL + "event/" + eventDAO.getId() +
                "/join/" + base64UsernamePassword)
                .setTag(this)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsObject(EventDAO.class, new ParsedRequestListener<EventDAO>() {
                    @Override
                    public void onResponse(EventDAO event) {
                        // do something with joined event
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d(TAG, "onError errorCode : " + error.getErrorCode());
                        Log.d(TAG, "onError errorBody : " + error.getErrorBody());
                        Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                        throw new RuntimeException("ERROR: errorCode " + error.getErrorCode()
                                + " errorBody " + error.getErrorBody()
                                + " errorDetail " + error.getErrorDetail());
                    }
                });
    }

    /**
     * We missed this endpoint when planning the server side so this won't work yet!
     * <p>
     * Does a PUT request to indicate the user has joined an event
     * Corresponds to /event/{id}/unjoin/{token} described here:
     * https://github.com/cs262dFA2018/EventConnectServer/wiki/Event-endpoints
     *
     * @param event    the event to join MUST HAVE ID SET(which is done by getEvents())
     * @param username Username the user joining
     * @param password Password of the user joining
     * @author Theron Tjapkes (tpt3)
     */
    public void unjoinEvent(Event event, String username, String password) {
        EventDAO eventDAO = EventToEventDAO(event);
        String base64UsernamePassword = encodeBase64(username + ":" + password);
        AndroidNetworking.put(BASE_URL + "event/" + eventDAO.getId() +
                "/unjoin/" + base64UsernamePassword)
                .setTag(this)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsObject(EventDAO.class, new ParsedRequestListener<EventDAO>() {
                    @Override
                    public void onResponse(EventDAO event) {
                        // do something with unjoined event
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d(TAG, "onError errorCode : " + error.getErrorCode());
                        Log.d(TAG, "onError errorBody : " + error.getErrorBody());
                        Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                        throw new RuntimeException("ERROR: errorCode " + error.getErrorCode()
                                + " errorBody " + error.getErrorBody()
                                + " errorDetail " + error.getErrorDetail());
                    }
                });
    }


    // Utility

    /**
     * Converts the EventsDAO to an Event used by the rest of the app
     *
     * @param eventDAO the EventDAO to convert
     * @return Event
     * @author Theron Tjapkes (tpt3)
     */
    private Event EventDAOtoEvent(EventDAO eventDAO) {
        Event event = new Event();
        Calendar time = Calendar.getInstance();
        time.setTime(eventDAO.getTime());
        try {
            event.setId(eventDAO.getId());
            event.setHost("TestUser"); //TODO: Fetch user from API
            event.setTitle(eventDAO.getTitle());
            event.setCalendar(time);
            event.setLocation(eventDAO.getLocation());
            event.setCost(eventDAO.getCost());
            event.setMinThreshold(eventDAO.getThreshold());
            event.setMaxCapacity(eventDAO.getCapacity());
            event.setDescription(eventDAO.getDescription());
            event.setCurrentInterest(eventDAO.getCount());
            event.setCategory(eventDAO.getCategory());
//        } catch (ParseException e) {
//            Log.e(TAG, e.getLocalizedMessage());
//            e.printStackTrace();
        } catch (RuntimeException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return event;
    }

    /**
     * Converts an Event used by the rest of the app to an EventDAO for to make requests easier
     *
     * @param event event to convert
     * @return EventDAO
     * @author Theron Tjapkes (tpt3)
     */
    private EventDAO EventToEventDAO(Event event) {
        EventDAO eventDAO = new EventDAO();
        eventDAO.setId(event.getId());
        eventDAO.setUserId(1); //TODO: Fetch user from API
        eventDAO.setTitle(event.getTitle());
        eventDAO.setDescription(event.getDescription());
        eventDAO.setTime(new Timestamp(event.getCalendar().getTimeInMillis()));
        eventDAO.setLocation(event.getLocation());
        eventDAO.setCost((float) event.getCost());
        eventDAO.setThreshold(event.getMinThreshold());
        eventDAO.setCapacity(event.getMaxCapacity());
        eventDAO.setCategory(event.getCategory());
        return eventDAO;
    }

    /*
     * This function will decode a base64 encoded string
     */
    private String decodeBase64(String encodedString) {
        byte[] decodedBytes = Base64.decode(encodedString, Base64.DEFAULT);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }

    /*
     * This function will encode a string to base64
     */
    private String encodeBase64(String decodedString) {
        String encodedString = Base64.encodeToString(decodedString.getBytes(), Base64.DEFAULT);
        return encodedString;
    }

}