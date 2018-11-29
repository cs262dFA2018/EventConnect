package edu.calvin.cs262.cs262d.eventconnect.data;

import java.text.ParseException;
import java.util.ArrayList;

public class MockDatabase {
    private ArrayList<Event> potentialEventData, confirmedEventData;
    private static MockDatabase uniqueInstance = null;
    private static final String Birthday = "We welcome everyone to Joe's birthday party. " +
            "Please join us as Joe turns 7 today. There will be cake, pinata, and a bounce house. " +
            "Presents are welcomed, but not required.";

    private static final String funeral = "Please join us in the celebration of Joe's life. Joseph David " +
            "Anderson lost his life last week, oct 24th 2018 after loosing a short battle with a bouncy castle at his party. " +
            "He was loved by his " + "friends, family, and pets. He will be missed, but let's not focus " +
            "on the negatives but on the positives of his life";

    private static final String GroceryOuting = "Does anyone want to get some groceries at Aldi's? I'm lonely and I need some new friends, " +
            "and my therapist suggested I make some new friends. I'll pay for gas, and it doesn't even have to be long... Please?";
    private MockDatabase(){
        potentialEventData = new ArrayList<Event>();
        confirmedEventData = new ArrayList<Event>();
        Event event = new Event();
        event.setHost("Brenda Anderson");
        event.setTitle("Happy Birthday Joe! ");
        try {
            event.setDate("11/8/2018");
        }
        catch (ParseException e){}
        try {
            event.setTime("8:30");
        }
        catch (ParseException e){}
        event.setCost(0.00);
        event.setLocation("Playworld");
        event.setDescription(Birthday );
        potentialEventData.add(event);

        Event event1 = new Event();
        event1.setTitle("Joe's Funeral");
        event1.setHost("Brenda Anderson");
        try {
            event1.setDate("11/9/2018");
        }
        catch (ParseException e){}
        try {
            event1.setTime("12:10");
        }
        catch (ParseException e){}
        event1.setCost(5.00);
        event1.setLocation("Clark Funeral Home");
        event1.setDescription(funeral);
        potentialEventData.add(event1);

        Event event2 = new Event();
        event2.setTitle("Grocery Shopping");
        event2.setHost("Dave Anderson");
        try {
            event2.setDate("11/9/2018");
        }
        catch (ParseException e){}
        try {
            event2.setTime("16:00");
        }
        catch (ParseException e){}
        event2.setCost(10.00);
        event2.setLocation("Aldi's");
        event2.setMinThreshold(2);
        event2.setMaxCapacity(50);
        event2.setDescription(GroceryOuting);
        potentialEventData.add(event2);
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

    /**
     * deleteEvent removes an event from the database
     *
     * @param eventToDelete passed in event that needs to be deleted
     */
    public void deleteEvent (Event eventToDelete) {

        // Iterate through the potential events, and delete the event if its found
        int num_events = potentialEventData.size();
        boolean eventFound = false;
        Event event;
        for (int i=0; i < num_events; i++) {
            event = potentialEventData.get(i);
            if (event == eventToDelete) {
                potentialEventData.remove(event);
                num_events--;
                eventFound = true;
            }
        }

        // If the event was not found in the potential events, check the confirmed events
        num_events = confirmedEventData.size();
        if (!eventFound) {
            for (int i=0; i < num_events; i++) {
                event = confirmedEventData.get(i);
                if (event == eventToDelete) {
                    confirmedEventData.remove(event);
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

    public ArrayList<Event> getPotentialEventData(){
        return this.potentialEventData;
    }

    public ArrayList<Event> getConfirmedEventData(){
        return this.confirmedEventData;
    }

    public void addEvent(Event event){
        potentialEventData.add(event);
    }

}
