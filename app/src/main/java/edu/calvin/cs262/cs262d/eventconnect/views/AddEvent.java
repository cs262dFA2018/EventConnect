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
    private EditText eventTitle;
    private EditText eventDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        eventTitle = (EditText) findViewById(R.id.title);
        eventDescription = (EditText) findViewById(R.id.description);

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
        String title = eventTitle.getText().toString();
        String desc = eventDescription.getText().toString();
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(desc);
        MockDatabase database = MockDatabase.getInstance();
        database.addEvent(event);
        finish();
    }


}
