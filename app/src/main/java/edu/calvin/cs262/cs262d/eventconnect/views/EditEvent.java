package edu.calvin.cs262.cs262d.eventconnect.views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.Event;
import edu.calvin.cs262.cs262d.eventconnect.data.EventsData;
import edu.calvin.cs262.cs262d.eventconnect.tools.AppThemeChanger;

/**
 * EditEvent is the activity for adding a new event
 * @author therOn
 *
 */
public class EditEvent extends AppCompatActivity {
    private EditText eventTitle, eventDescription, eventDate, eventLocation, eventCost, eventThreshold, eventCapacity, eventTime;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener date;
    private Spinner eventCat;
    private Context context;
    private TimePickerDialog.OnTimeSetListener Time;
    private String currentTheme;

    /**
     * onCreate initializes the EditEvent activity
     * @param savedInstanceState bundle passed in when the activity is created
     * @author OneTrueAsian (Time & Date Dialog fragments)
     * @author RickRilled (category spinner)
     * @author Littlesnowman88
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //access shared preferences for theme setting first.
        //MUST BE HANDLED BEFORE setContentView is called--in this case, before super.onCreate is called
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppThemeChanger.handleThemeChange(this, currentTheme);
        currentTheme = sharedPrefs.getString("theme_preference", "Light"); //default to Light theme

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        context = getBaseContext();

        //access the UI Edit Texts
        eventTitle = (EditText) findViewById(R.id.title);
        eventDescription = (EditText) findViewById(R.id.description);
        eventDate = (EditText) findViewById(R.id.date);
        eventTime = (EditText) findViewById(R.id.time);
        eventLocation = (EditText) findViewById(R.id.location);
        eventCost = (EditText) findViewById(R.id.cost);
        eventThreshold = (EditText) findViewById(R.id.threshold);
        eventCapacity = (EditText) findViewById(R.id.capacity);

        //Then, set the UI Edit Texts based on the provided Event (given in Intent args)
        Bundle eventInfo = getIntent().getExtras();
        try {
            eventTitle.setText(eventInfo.getString("title"));
        } catch (java.lang.NullPointerException ne) {
            eventTitle.clearComposingText();
        }
        try {
            eventDescription.setText(eventInfo.getString("description"));
        } catch (java.lang.NullPointerException ne) {
            eventDescription.clearComposingText();
        }
        try {
            eventLocation.setText(eventInfo.getString("location"));
        } catch (java.lang.NullPointerException ne) {
            eventLocation.clearComposingText();
        }
        try {
            eventDate.setText(eventInfo.getString("date"));
        } catch (java.lang.NullPointerException ne) {
            eventDate.clearComposingText();
        }
        try {
            eventCost.setText(String.format(Locale.getDefault(), Double.toString(eventInfo.getDouble("cost"))));
        } catch (java.lang.NullPointerException ne) {
            eventCost.clearComposingText();
        } catch (NumberFormatException nfe) {
            eventCost.clearComposingText();
        }
        try {
            eventTime.setText(eventInfo.getString("time"));
        } catch (java.lang.NullPointerException ne){
            eventTime.clearComposingText();
        }

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
                DatePickerDialog datepicker = new DatePickerDialog(EditEvent.this, date,
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
                TimePickerDialog timePicker = new TimePickerDialog(EditEvent.this, Time,
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

        eventCat = (Spinner) findViewById(R.id.event_cat);

        //https://developer.android.com/guide/topics/ui/controls/spinner#java
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> cat_adapter = ArrayAdapter.createFromResource(this,
                R.array.event_cat, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        cat_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        eventCat.setAdapter(cat_adapter);

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
     * Onclick for saving a database event object when the "save event" button is clicked,
     * setting the time, date, title, etc.
     *
     * @param view the viewholder for the event cards
     * @author OneTrueAsian (SetError)
     * @author Littlesnowman88
     */
    public void onSaveEventClicked(View view) throws ParseException {

        //access the data from the UI elements
        String title = eventTitle.getText().toString();
        String desc = eventDescription.getText().toString();
        String loc;
        String cat = eventCat.getSelectedItem().toString();
        TextView CatError = (TextView)eventCat.getSelectedView();
        boolean errorFound = false;
        String date = eventDate.getText().toString();
        String time = eventTime.getText().toString();

        double cost;
        int threshold;
        final int capacity;

        Event event = new Event();

        //EVENT HOST
        event.setHost(EventsData.getInstance(null).getCredentials()[0]);

        //EVENT TITLE
        try{
            if(!title.equals("") && title.length() < 20){
                event.setTitle(title);
            } else {
                eventTitle.setError(getString(R.string.error_faulty_title));
                Toast.makeText(EditEvent.this, context.getString(R.string.error_faulty_title),
                        Toast.LENGTH_SHORT).show();
                errorFound=true;
            }
        } catch ( RuntimeException e){
        }// remember this catch block if we ever throw a runtimeException in the Event class for event.setTitle(title)


        //EVENT DATE
        try {
            event.setDate(date);
            eventDate.setError(null); //clear any possible error messages since EditText is hidden behind a dialog.
        } catch (ParseException e) {
            eventDate.setError(getString(R.string.error_empty_date));
            Toast.makeText(EditEvent.this, context.getString(R.string.error_empty_date),
                    Toast.LENGTH_SHORT).show();
            errorFound=true;
        } catch (RuntimeException e) {
            // this should not run because previous dates are disabled
            eventDate.setError(getString(R.string.error_invalid_date));
            Toast.makeText(EditEvent.this, context.getString(R.string.error_invalid_date),
                    Toast.LENGTH_SHORT).show();
            errorFound=true;
        }

        //EVENT TIME
        try {
            event.setTime(time);
            eventTime.setError(null); //clear any possible error messages since EditText is hidden behind a dialog.
        } catch (ParseException e) {
            eventTime.setError(getString(R.string.error_empty_time));
            Toast.makeText(EditEvent.this, context.getString(R.string.error_empty_time),
                    Toast.LENGTH_SHORT).show();
            errorFound=true;
        } catch (RuntimeException e) {
            eventTime.setError(getString(R.string.error_invalid_time));
            Toast.makeText(EditEvent.this, context.getString(R.string.error_invalid_time), Toast.LENGTH_SHORT).show();
            errorFound=true;
        }

        //EVENT LOCATION
        try {
            loc = eventLocation.getText().toString();
            if (!loc.equals("")) {
                event.setLocation(loc);
            } else {
                eventLocation.setError(getString(R.string.error_empty_location));
                Toast.makeText(EditEvent.this, context.getString(R.string.error_empty_location),
                        Toast.LENGTH_SHORT).show();
                errorFound=true;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }// remember this catch block if we ever throw a runtimeException in the Event class for event.setLocation(loc)

        //EVENT COST
        try {
            cost = Double.parseDouble(eventCost.getText().toString());
        } catch (NumberFormatException e) {
            cost = 0;
        }
        event.setCost(cost);

        //EVENT THRESHOLD (MIN INTEREST)
        try {
            threshold = (int) Math.floor(Double.parseDouble(eventThreshold.getText().toString()));
            event.setMinThreshold(threshold);
        } catch (NumberFormatException e) {
            eventThreshold.setError(getString(R.string.error_invalid_number));
            Toast.makeText(EditEvent.this, context.getString(R.string.error_invalid_number),
                    Toast.LENGTH_SHORT).show();
            errorFound=true;
        } catch (RuntimeException e) {
            eventThreshold.setError(getString(R.string.error_invalid_MinNumber));
            Toast.makeText(EditEvent.this, context.getString(R.string.error_invalid_MinNumber),
                    Toast.LENGTH_SHORT).show();
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
        } catch (NumberFormatException e) {
            eventCapacity.setError(getString(R.string.error_invalid_number));
            Toast.makeText(EditEvent.this, context.getString(R.string.error_invalid_number),
                    Toast.LENGTH_SHORT).show();
            errorFound=true;
        } catch (RuntimeException e) {
            eventCapacity.setError(getString(R.string.error_invalid_MaxNumber));
            Toast.makeText(EditEvent.this, context.getString(R.string.error_invalid_MaxNumber),
                    Toast.LENGTH_SHORT).show();
            errorFound=true;
        }

        //EVENT DESCRIPTION
        event.setDescription(desc);

        //set Category
        try{
            if(cat.equals("Select")){
                event.setCategory("");
            } else {
                event.setCategory(cat);
            }
        }
        catch (RuntimeException e){}

        if (!errorFound) { //if all required event information is entered and information is validated:
            //access and update the database.
            event.setInterest();
            EventsData.getInstance(null).editEvent(event);
            finish();
        }

    }

    /**
     * This method updates the onClick listener for the calendar
     * widget, updating it to the MM/dd/yy format and Local US date
     * @author OneTrueAsian
     **/
    private void updateLabel() {
        String DateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat, Locale.US);
        eventDate.setText(sdf.format(calendar.getTime()));

    }
    /**
     * This method updates the onClick listener for the time
     * widget, updating it to the HH:mm format and Local US date
     * @author OneTrueAsian
     **/
    public void updateTime() {
        String TimeFormat = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(TimeFormat, Locale.US);
        eventTime.setText(sdf.format(calendar.getTime()));

    }

}

