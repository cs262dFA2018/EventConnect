package edu.calvin.cs262.cs262d.eventconnect.views;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    }

    public void onCreateEventClicked(View view) {
        String title = eventTitle.getText().toString();
        String desc = eventDescription.getText().toString();
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(desc);
        MockDatabase database = MockDatabase.getInstance();
        database.addEvent(event);
        Intent backToMain = new Intent(AddEvent.this, MainActivity.class);
        AddEvent.this.startActivity(backToMain);
    }

}
