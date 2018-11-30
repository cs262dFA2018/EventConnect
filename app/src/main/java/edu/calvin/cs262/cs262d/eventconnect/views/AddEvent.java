package edu.calvin.cs262.cs262d.eventconnect.views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.Event;
import edu.calvin.cs262.cs262d.eventconnect.data.MockDatabase;
import edu.calvin.cs262.cs262d.eventconnect.tools.AppThemeChanger;

/**
 * AddEvent is the activity for adding a new event
 */
public class AddEvent extends AppCompatActivity {
    private EditText eventTitle, eventDescription, eventHost, eventDate, eventLocation, eventCost, eventThreshold, eventCapacity, eventTime;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener date;
    private Context context;
    private TimePickerDialog.OnTimeSetListener Time;
    private String currentTheme;

    /**
     * onCreate initializes the AddEvent activity
     *
     * @param savedInstanceState bundle passed in when the activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //access shared preferences for theme setting first.
        //MUST BE HANDLED BEFORE setContentView is called--in this case, before super.onCreate is called
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppThemeChanger.handleThemeChange(this, currentTheme);
        currentTheme = sharedPrefs.getString("theme_preference", "Light"); //default to Light theme

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //access the UI Edit Texts
        eventTitle = (EditText) findViewById(R.id.title);
        eventDescription = (EditText) findViewById(R.id.description);
        eventHost = (EditText) findViewById(R.id.host);
        eventDate = (EditText) findViewById(R.id.date);
        eventTime = (EditText) findViewById(R.id.time);
        eventLocation = (EditText) findViewById(R.id.location);
        eventCost = (EditText) findViewById(R.id.cost);
        eventThreshold = (EditText) findViewById(R.id.threshold);
        eventCapacity = (EditText) findViewById(R.id.capacity);

        // onClick listener for eventDate to pull up the calendar widget
        eventDate.setOnClickListener(new View.OnClickListener() {
            /**
             * onClick for activating the calendar widget
             * once the date EditText is clicked
             *
             * @param view the view the onClick coordinates with
             */
            @Override
            public void onClick(View view) {
                DatePickerDialog datepicker = new DatePickerDialog(AddEvent.this, date,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datepicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datepicker.show();
            }
        });

        // initialize a DatePickerDialog and set name to date for the onClickListener
        calendar = Calendar.getInstance();
        date = new DatePickerDialog.OnDateSetListener() {
          /**
             * onDateSet stores the date information from the Date Picker
             *
             * @param view
             * @param year
             * @param monthOfYear
             * @param dayOfMonth
             */
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        // onClick listener for eventTime to pull up the time picker widget
        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar CurrentTime = Calendar.getInstance();
                TimePickerDialog timePicker = new TimePickerDialog(AddEvent.this, Time,
                        CurrentTime.get(Calendar.HOUR_OF_DAY), CurrentTime.get(Calendar.MINUTE), true);
                timePicker.show();
            }
        });

        // initialize a TimePickerDialog and set name to date for the onClickListener
        Time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateTime();
            }
        };

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

    /**
     * Calls the correct routine for the selected item or finishes the activity
     *
     * @param item selected item from the menu
     * @return result of the super call for the selected item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Onclick for creating a database event object when the "create event" button is clicked,
     * setting the time, date, title, etc.
     *
     * @param view the viewholder for the event cards
     * Onclick for creating a database event object when the "create event" button is clicked,
     * setting the time, date, title, etc.
     *
     */
    public void onCreateEventClicked(View view) {
        boolean errorFound = false;
        String host;
        String title = eventTitle.getText().toString();
        String date = eventDate.getText().toString();
        String time = eventTime.getText().toString();
        String loc;
        double cost;
        int threshold;
        final int capacity;
        String desc = eventDescription.getText().toString();

        Event event = new Event();

        //EVENT HOST
        try {
            host = eventHost.getText().toString();
            if (!host.equals("")) {
                event.setHost(host);
            } else {
                eventHost.setError(getString(R.string.error_empty_host));
                errorFound=true;
            }
        } catch (RuntimeException e) {
        } // remember this catch block if we ever throw a runtimeException in the Event class for event.setHost(host)

        //EVENT TITLE
        event.setTitle(title);

        //EVENT DATE
        try {
            event.setDate(date);
            eventDate.setError(null); //clear any possible error messages since EditText is hidden behind a dialog.
        } catch (ParseException e) {
            eventDate.setError(getString(R.string.error_empty_date));
            errorFound=true;
        } catch (RuntimeException e) {
            // this should not run because previous dates are disabled
            eventDate.setError(getString(R.string.error_invalid_date));
            errorFound=true;
        }

        //EVENT TIME
        try {
            event.setTime(time);
            eventTime.setError(null); //clear any possible error messages since EditText is hidden behind a dialog.
        } catch (ParseException e) {
            eventTime.setError(getString(R.string.error_empty_time));
            errorFound=true;
        } catch (RuntimeException e) {
            eventTime.setError(getString(R.string.error_invalid_time));
            Toast.makeText(AddEvent.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            errorFound=true;
        }

        //EVENT LOCATION
        try {
            loc = eventLocation.getText().toString();
            if (!loc.equals("")) {
                event.setLocation(loc);
            } else {
                eventLocation.setError(getString(R.string.error_empty_location));
                errorFound=true;
            }
        } catch (RuntimeException e) {
        }// remember this catch block if we ever throw a runtimeException in the Event class for event.setLocation(loc)

        //EVENT COST
        try {
            cost = Double.parseDouble(eventCost.getText().toString());
        } catch (java.lang.NumberFormatException e) {
            cost = 0;
        }
        event.setCost(cost);

        //EVENT THRESHOLD (MIN INTEREST)
        try {
            threshold = (int) Math.floor(Double.parseDouble(eventThreshold.getText().toString()));
            event.setMinThreshold(threshold);
        } catch (java.lang.NumberFormatException e) {
            eventThreshold.setError(getString(R.string.error_invalid_number));
            errorFound=true;
        } catch (RuntimeException e) {
            eventThreshold.setError(getString(R.string.error_invalid_MinNumber));
            errorFound=true;
        }

        //EVENT CAPACITY (MAX INTEREST)
        try {
            String capacityText = eventCapacity.getText().toString();
            // if the capacity is not an empty string, turn it into a number
            if (!capacityText.equals("")) {
                capacity = (int) Math.floor(Double.parseDouble(capacityText));
            }
            // other wise set to -1 which mean there is no max capacity
            else {
                capacity = -1;
            }
            event.setMaxCapacity(capacity);
        } catch (java.lang.NumberFormatException e) {
            eventCapacity.setError(getString(R.string.error_invalid_number));
            errorFound=true;
        } catch (RuntimeException e) {
            eventCapacity.setError(getString(R.string.error_invalid_MaxNumber));
            errorFound=true;
        }

        //EVENT DESCRIPTION
        event.setDescription(desc);

        if (!errorFound) { //if all required event information is entered and information is validated:
            //access and update the database.
            MockDatabase database = MockDatabase.getInstance();
            database.addNewEvent(event);
            finish();
        }
    }

    /**
     * This method updates the onClick listener for the calendar
     * widget, updating it to the MM/dd/yy format and Local US date
     **/
    private void updateLabel() {
        String DateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat, Locale.US);
        eventDate.setText(sdf.format(calendar.getTime()));

    }
    /**
     * This method updates the onClick listener for the time
     * widget, updating it to the HH:mm format and Local US date
     **/
    public void updateTime() {
        String TimeFormat = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(TimeFormat, Locale.US);
        eventTime.setText(sdf.format(calendar.getTime()));

    }

}
