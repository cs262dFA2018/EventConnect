package edu.calvin.cs262.cs262d.eventconnect.data;

import java.util.ArrayList;

public class MockDatabase {
    private ArrayList<Event> potentialEventData, confirmedEventData;
    private static MockDatabase uniqueInstance = null;
    private static final String Birthday = "We welcome everyone to Joe's birthday party." +
            "Please join us as Joe turns 7 today. There will be cake, pinata, and a bounce house." +
            "Presents are welcomed, but not required.";

    private static final String funeral = "Please join us in the celebration of Joe's life. Joseph David " +
            "Anderson lost his life last week, oct 24th 2018 after a short battle with cancer. He was loved by his" +
            "friends, family, and pets. He will be missed, but lets not focus on the negatives but on the positives of his life";

    private static final String GroceryOuting = "Does anyone want to get some groceries at Aldi's? I'm lonely and I need some new friends, " +
            "and my therapist suggested I make some new friends. I'll pay for gas, and it doesn't even have to be long... Please?";
    private MockDatabase(){
        potentialEventData = new ArrayList<Event>();
        confirmedEventData = new ArrayList<Event>();
        Event event = new Event();
        event.setTitle("Happy Birthday Joe! ");
        event.setDescription(Birthday );
        potentialEventData.add(event);

        Event event1 = new Event();
        event1.setTitle("Joe's Funeral");
        event1.setDescription(funeral);
        potentialEventData.add(event1);

        Event event2 = new Event();
        event2.setTitle("Grocery Shopping");
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
