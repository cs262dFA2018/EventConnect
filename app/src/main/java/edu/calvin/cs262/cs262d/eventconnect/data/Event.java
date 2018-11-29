package edu.calvin.cs262.cs262d.eventconnect.data;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.Toast;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.calvin.cs262.cs262d.eventconnect.R;

public class Event {
    private int current_interest, min_threshold, max_capacity;
    private String title, description, host, location;
    private  Calendar date;
    private boolean confirmed, needs_to_move, interest;
    private double cost;

    public Event() {
        cost = 0;
        current_interest = 0;
        min_threshold = 1;
        max_capacity = -1;
        title = "";
        description = "";
        host = "";
        location = "";
        date = Calendar.getInstance();
        confirmed = false;
        needs_to_move = false;
        interest = false;
    }

    public String getTitle() {return title;}
    public void setTitle(String new_title) {title = new_title;}
    public String getDescription() {return description;}
    public void setDescription(String new_description) {description = new_description;}
    public String getHost() {return host;}
    public void setHost(String new_host) {host = new_host;}
    public String getLocation() {return location;}
    public void setLocation(String new_location) {location = new_location;}
    public double getCost() {return cost;}
    public void setCost(double new_cost) {
        if (new_cost < 0) { throw new RuntimeException("ERROR: cost was negative.");
        } else { cost = new_cost; }
    }
    public String getTime(){
        String DateFormat = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat, Locale.US);
        return sdf.format(date.getTime());
    }

    public String getDate() {
        String DateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat, Locale.US);
        return sdf.format(date.getTime());
    }
    public int getCurrentInterest() {return current_interest;}
    public boolean shouldMove() {return needs_to_move;}


    /** for methods that change current_interest, min_threshold, and max_capacity,
     * I have placed logic blocks that force the program to crash if values go invalid.
     * This is intended to make the program Fail Fast when bugs creep in during the development process.
     */
    public void setCurrentInterest(int new_interest) {
        if (new_interest > -1 || (max_capacity > 0 && new_interest <= max_capacity)) {
            current_interest = new_interest;
        } else {
            //Created to catch mistakes early on; developer, make sure you handle setCurrentInterest with Great Care.
            throw new RuntimeException("ERROR: attempt to set current interest out of its bounds.");
        }
    }
    public void incrementCurrentInterest() {
        if (max_capacity < 0 || current_interest + 1 <= max_capacity) {
            current_interest += 1;
            /* If newly confirmed, confirmed = true
             */
            if (current_interest >= min_threshold && confirmed == false){
                confirmed = true;
                needs_to_move =true;
            }
        } else {
            throw new RuntimeException("ERROR: current_interest exceeded max_capacity.");
        }
    }
    public void decrementCurrentInterest() {
        if (current_interest - 1 > -1 ) {
            current_interest -= 1;
            if (current_interest < min_threshold && confirmed == true){
                confirmed = false;
                needs_to_move =true;
            }
        } else {
            throw new RuntimeException("ERROR: current_interest went below zero.");
        }
    }

    public int getMaxCapacity() {return max_capacity;}

    public void setMaxCapacity(int new_capacity) throws RuntimeException{
        if (new_capacity == -1 || (new_capacity > 0 && new_capacity > current_interest
                && new_capacity >= min_threshold)) {
            max_capacity = new_capacity;
        } else {
            throw new RuntimeException("ERROR: attempt to set max capacity to an illegal state.");
        }
    }

    public int getMinThreshold() {return min_threshold;}

    public void setMinThreshold(int new_threshold) throws RuntimeException {

        //new threshold must be positive and within the max capacity (unless max capacity is -1, in which case it can be anything positive.
        if (new_threshold > 0 && (new_threshold <= max_capacity || max_capacity == -1)) {
            min_threshold = new_threshold;
        } else {
             throw new RuntimeException("ERROR: attempt to set min threshold to an illegal state.");
        }
    }

    public void setDate(String new_date) throws RuntimeException, ParseException {
        final Calendar calendar = Calendar.getInstance();
        String DateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat, Locale.US);

        calendar.setTime(sdf.parse(new_date));

        if (calendar.before(Calendar.getInstance()) && date.equals("")) {
            throw new RuntimeException("ERROR: attempt to set date to before current time");
        } else {
            date = calendar;
        }
    }

    public void setTime(String new_time) throws ParseException{
        final Calendar calendar = Calendar.getInstance();
        String DateFormat = "hh:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat, Locale.US);

        calendar.setTime(sdf.parse(new_time));

        if (calendar.before(Calendar.getInstance()) && date.equals("")) {
            throw new RuntimeException("ERROR: attempt to set Time to before current time");
        } else {
            date = calendar;
        }
    }

    public void clearMoved(){ needs_to_move = false; }
    public boolean getInterest(){return interest;}
    public void setInterest(){interest = true;}
    public void clearInterest(){interest = false;}
}

