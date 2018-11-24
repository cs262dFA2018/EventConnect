package edu.calvin.cs262.cs262d.eventconnect.views;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.Event;
import edu.calvin.cs262.cs262d.eventconnect.data.MockDatabase;

public class AddEvent extends AppCompatActivity {
    private EditText eventTitle, eventDescription, eventHost, eventDate, eventLocation, eventCost, eventThreshold, eventCapacity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        //access the UI Edit Texts
        eventTitle = (EditText) findViewById(R.id.title);
        eventDescription = (EditText) findViewById(R.id.description);
        eventHost = (EditText) findViewById(R.id.host);
        eventDate = (EditText) findViewById(R.id.date);
        eventLocation = (EditText) findViewById(R.id.location);
        eventCost = (EditText) findViewById(R.id.cost);
        eventThreshold = (EditText) findViewById(R.id.threshold);
        eventCapacity = (EditText) findViewById(R.id.capacity);

        //setup toolbar bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //then set up toolbar/actionbar's up navigation
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }

    }

    /*unless access to the settings activity is added from here,
     * onOptionItemSelected really needs to care about only the back arrow.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateEventClicked(View view) {
        //access the data from the UI elements
        //TODO: Enforce a title and description??
        String title = eventTitle.getText().toString();
        String desc = eventDescription.getText().toString();
        String host = eventHost.getText().toString();
        String loc = eventLocation.getText().toString();
        String date = eventDate.getText().toString();
        Event event = new Event();

        double cost;
        try {cost = Double.parseDouble(eventCost.getText().toString());}
        catch (java.lang.NumberFormatException e) {cost = 0;}

        int threshold;
        int capacity;

        //TODO: prevent user from putting illegal threshold and capacity in.
        try {threshold = (int) Math.floor(Double.parseDouble(eventThreshold.getText().toString()));
            event.setMinThreshold(threshold);
            }
        catch (java.lang.NumberFormatException e) {
            eventThreshold.setError(getString(R.string.error_invalid_number));
            return;
        }
        catch (RuntimeException e){
            eventThreshold.setError(getString(R.string.error_invalid_MinNumber));
            return;
        }

        try {String capacityText = eventCapacity.getText().toString();
            // if the capacity is not an empty string, turn it into a number
            if (!capacityText.equals("")){
                capacity = (int) Math.floor(Double.parseDouble(capacityText));
                event.setMaxCapacity(capacity);
            }
            // other wise set to -1 which mean there is no max capacity
            else {
                capacity = -1;
            }}
        catch (java.lang.NumberFormatException e) {
            eventCapacity.setError(getString(R.string.error_invalid_number));
            return;
        }
        catch (RuntimeException e){
            eventCapacity.setError(getString(R.string.error_invalid_MaxNumber));
            return;
        }

        //store the data from the UI elements
        event.setTitle(title);
        event.setDescription(desc);
        event.setHost(host);
        event.setLocation(loc);
        event.setDate(date);
        event.setCost(cost);

        //access and update the database.
        //TODO: Make sure data is valid. Else, send a toast about the required fields
        MockDatabase database = MockDatabase.getInstance();
        database.addEvent(event);
        finish();
    }


}
