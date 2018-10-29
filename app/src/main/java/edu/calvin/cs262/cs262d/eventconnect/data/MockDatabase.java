package edu.calvin.cs262.cs262d.eventconnect.data;

import java.util.ArrayList;

public class MockDatabase {
    private ArrayList<Event> potentialEventData, confirmedEventData;
    private static MockDatabase uniqueInstance = null;
    private static final String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing" +
            " elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad" +
            " minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo" +
            " consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum" +
            " dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt" +
            " in culpa qui officia deserunt mollit anim id est laborum.";
    private MockDatabase(){
        potentialEventData = new ArrayList<Event>();
        confirmedEventData = new ArrayList<Event>();
        for (int i = 0; i < 3; i++) {
            Event event = new Event();
            event.setTitle("Event " + i);
            event.setDescription(loremIpsum);
            potentialEventData.add(i, event);
        }
    }
    public synchronized static MockDatabase getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new MockDatabase();
        }
        return uniqueInstance;
    }
    public void movePotentialEvent(Event eventToMove){
        int num_events = potentialEventData.size();
        for (int i=0; i < num_events; i++){
            Event event = potentialEventData.get(i);
            if(event == eventToMove){
                confirmedEventData.add(eventToMove);
                potentialEventData.remove(event);
                num_events--;
            }
        }
    }
    public void moveCompletedEvent(Event eventToMove){
        int num_events = confirmedEventData.size();
        for (int i=0; i < num_events; i++){
            Event event = confirmedEventData.get(i);
            if(event == eventToMove){
                potentialEventData.add(eventToMove);
                confirmedEventData.remove(event);
                num_events--;
            }
        }
    }

    public ArrayList<Event> getPotentialEventData(){
        return this.potentialEventData;
    }

    public ArrayList<Event> getConfirmedEventData(){
        return this.confirmedEventData;
    }

}
