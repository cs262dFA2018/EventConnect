package edu.calvin.cs262.cs262d.eventconnect.data;

import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONArray;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

public class EventConnector {

    private static class EventJsonDataHolder {
        @JsonProperty("items")
        public List<EventDAO> EventsDAOList;
    }

    private static final String TAG = "EventConnector";
    private static final String BASE_URL = "https://calvincs262-fall2018-cs262d.appspot.com/eventconnect/v1/";

    public void initialize(Context context) {
        AndroidNetworking.initialize(context);
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
    }

    public void getEvents() {
        List<EventDAO> eventDAOList;
        AndroidNetworking.get(BASE_URL + "events")
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsObject(EventJsonDataHolder.class, new ParsedRequestListener<EventJsonDataHolder>() {
                    @Override
                    public void onResponse(EventJsonDataHolder events) {
                        Log.d(TAG, "eventList: " + events);
                        for (EventDAO event : events.EventsDAOList) {
                            Log.d(TAG, "eventTitle: " + event.getTitle());
                            try {
                                EventsData.getInstance().placeEvent(EventDAOtoEvent(event));
                            } catch (ParseException e) {
                                Log.d(TAG, "onResponse: " + e);
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                        Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                        Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());

                    }
                });

    }

    public Event EventDAOtoEvent(EventDAO eventDAO) throws ParseException {
        Event event = new Event();
        event.setHost(Integer.toString(eventDAO.getUserId()));
        event.setTitle(event.getTitle());
        event.setDate(eventDAO.getTimeCalendar().get(Calendar.MONTH) + "/"
                + eventDAO.getTimeCalendar().get(Calendar.DAY_OF_MONTH) + "/"
                + eventDAO.getTimeCalendar().get(Calendar.YEAR));
        event.setLocation(eventDAO.getLocation());
        event.setCost(eventDAO.getCost());
        event.setMinThreshold(eventDAO.getThreshold());
        event.setMaxCapacity(eventDAO.getCapacity());
        event.setDescription(eventDAO.getDescription());
        event.setCurrentInterest(eventDAO.getCount());

        return event;
    }


}
