package edu.calvin.cs262.cs262d.eventconnect.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Event {
    private int current_interest, min_threshold, max_capacity, id;


    private String title, description, host, location, category;
    private Calendar date;
    private boolean confirmed, needs_to_move, interest;
    private double cost;

    public Event() {
        host = "";
        title = "";
        date = Calendar.getInstance();
        location = "";
        cost = 0;
        description = "";
        category = "";
        current_interest = 0;
        min_threshold = 1;
        max_capacity = -1;
        interest = false;
        confirmed = false;
    }

    //id


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //calendar (date) needed for EventDAO to Event conversion in EventConnector
    public Calendar getCalendar(){
        return date;
    }

    public void setCalendar(Calendar calendar){
        this.date = calendar;
    }


    //host
    public String getHost() {
        return host;
    }
    public void setHost(String new_host) {
        host = new_host;
    }
  
    //Category
    public String getCategory() {
        return category;
    }
    public void setCategory(String new_category) throws  RuntimeException {category = new_category;}

    //title
    public String getTitle() {
        return title;
    }
    public void setTitle(String new_title) { title = new_title; }
    //date
    public String getDate() {
        String DateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat, Locale.US);
        return sdf.format(date.getTime());
    }

    /**
     * Sets an event's date field.
     *
     * @param new_date the new date formatted as MM/dd/yy. This date should be after a device's current day.
     * @throws RuntimeException thrown if the new_date is before a device's current date (ASSUMES SINGLE LOCALE).
     * @throws ParseException   thrown if new_date is not in MM/dd/yy format.
     * @author OneTrueAsian
     * @author Littlesnowman88
     */
    public void setDate(String new_date) throws RuntimeException, ParseException {
        final Calendar calendar = Calendar.getInstance();
        final Calendar calendarNow = Calendar.getInstance();
        String DateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat, Locale.US);

        calendar.setTime(sdf.parse(new_date));
        RuntimeException dateTooEarly = new RuntimeException("ERROR: attempt to set date to before current time");

        //thanks to anivaler's stackoverflow response for inspiration
        //see, https://stackoverflow.com/questions/10155523/android-compare-calendar-dates
        //declare variables for comparing year, month, and day_of_month
        int newY = calendar.get(Calendar.YEAR);
        int newM = calendar.get(Calendar.MONTH);
        int newD = calendar.get(Calendar.DAY_OF_MONTH);
        int currentY = calendarNow.get(Calendar.YEAR);
        int currentM = calendarNow.get(Calendar.MONTH);
        int currentD = calendarNow.get(Calendar.DAY_OF_MONTH);


        //first, compare years
        if (newY > currentY) {
            date = calendar;
        } else if (newY < currentY) {
            throw dateTooEarly;
        } else {
            //if years are the same, check months
            if (newM > currentM) {
                date = calendar;
            } else if (newM < currentM) {
                throw dateTooEarly;
            } else {
                //if months are also the same, check days
                if (newD >= currentD) { //NOTE: events are allowed to be created for today
                    date = calendar;
                } else {
                    throw dateTooEarly;
                }
            }
        }
    }


    //time
    public String getTime() {
        String DateFormat = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat, Locale.US);
        return sdf.format(date.getTime());
    }

    /**
     * Sets an event's time field (as in, hours and minutes)
     *
     * @param new_time the new time formatted as hh:mm. This time should be after a device's current time.
     * @throws ParseException   thrown if the new_time is before a device's current time (ASSUMES SINGLE LOCALE AND TIME ZONE).
     * @throws RuntimeException thrown if new_time is not in hh:mm format.
     * @author OneTrueAsian
     * @author Littlesnowman88
     */
    public void setTime(String new_time) throws RuntimeException, ParseException {
        final Calendar calendarNow = Calendar.getInstance();
        final Calendar calendar;
        //first, establish date by trying to use the existing date.
        if (date != null) {
            calendar = date;
        } else {
            //if no existing date has been set, then just use today.
            calendar = Calendar.getInstance();
        }

        //prepare to set the event's potentially new time by building the event's date into a string
        //without a date portion, calendar's date gets set to 1970 when setTime is called. Badness!
        String dateFormat = "MM/dd/yy";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat, Locale.US);
        String dateTag = dateFormatter.format(calendar.getTime());

        //set the event's potentially new time
        String timeFormat = "MM/dd/yy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.US);
        String dateAndTime = dateTag + " " + new_time;
        calendar.setTime(sdf.parse(dateAndTime));

        if (calendar.before(calendarNow)) {
            throw new RuntimeException("ERROR: attempt to set Time to before current time");
        } else {
            date = calendar;
        }
    }

    //location
    public String getLocation() {
        return location;
    }

    public void setLocation(String new_location) {
        location = new_location;
    }

    //cost
    public double getCost() {
        return cost;
    }

    public void setCost(double new_cost) {
        if (new_cost < 0) {
            throw new RuntimeException("ERROR: cost was negative.");
        } else {
            cost = new_cost;
        }
    }

    /* for methods that change current_interest, min_threshold, and max_capacity,
     * I have placed logic blocks that force the program to crash if values go invalid.
     * This is intended to make the program Fail Fast when bugs creep in during the development process.
     */
    //threshold
    public int getMinThreshold() {
        return min_threshold;
    }

    /**
     * Sets minimum threshold, should it need to change
     *
     * @param new_threshold new value for threshold
     * @throws RuntimeException throws an error if attempted to set to an illegal state
     * @author OneTrueAsian
     * @author Littlesnowman88
     */
    public void setMinThreshold(int new_threshold) throws RuntimeException {

        //new threshold must be positive and within the max capacity (unless max capacity is -1, in which case it can be anything positive.
        if (new_threshold > 0 && (new_threshold <= max_capacity || max_capacity == -1)) {
            min_threshold = new_threshold;
        } else {
            throw new RuntimeException("ERROR: attempt to set min threshold to an illegal state.");
        }
    }

    //capacity
    public int getMaxCapacity() {
        return max_capacity;
    }

    /**
     * Sets new capacity to the value passed in
     *
     * @param new_capacity new max capacity to change in the system
     * @throws RuntimeException throws an error if attempted to set to an illegal state
     * @author OneTrueAsian
     * @author Littlesnowman88
     */
    public void setMaxCapacity(int new_capacity) throws RuntimeException {
        if (new_capacity == -1 || (new_capacity > 0 && new_capacity > current_interest
                && new_capacity >= min_threshold)) {
            max_capacity = new_capacity;
        } else {
            throw new RuntimeException("ERROR: attempt to set max capacity to an illegal state.");
        }
    }

    //description
    public String getDescription() {
        return description;
    }

    public void setDescription(String new_description) {
        description = new_description;
    }

    //event interest count.
    public int getCurrentInterest() {
        return current_interest;
    }

    /**
     * @param new_interest
     * @throws RuntimeException
     * @author OneTrueAsian
     * @author Littlesnowman88
     */
    public void setCurrentInterest(int new_interest) throws RuntimeException {
        if (new_interest > -1 && (max_capacity == -1 || (max_capacity > 0 && new_interest <= max_capacity))) {
            current_interest = new_interest;
        } else {
            throw new RuntimeException("ERROR: attempt to set current interest out of its bounds.");
        }
    }

    //interest getter, setter, and clearer. Used to keep track of device interest pressed.
    public boolean getInterest() {
        return interest;
    }

    public void setInterest() {
        interest = true;
    }

    public void clearInterest() {
        interest = false;
    }

    /**
     * checkConfirmed checks the current interest count against the minimum threshold
     * also sets confirmed to true or false, based on the return value's conditions.
     *
     * @return true if current interest >= threshold, false otherwise
     * @author Littlesnowman88
     */
    public boolean checkConfirmed() {
        confirmed = (current_interest >= min_threshold);
        return confirmed;
    }

    /**
     * Comparison operator for events
     *
     * @param otherEvent
     * @author Littlesnowman88
     */
    public boolean isSameAs(Event otherEvent) {
        if (!this.host.equals(otherEvent.getHost()) ||
            !this.title.equals(otherEvent.getTitle()) ||
            !this.description.equals(otherEvent.getDescription()) ||
            !this.location.equals(otherEvent.getLocation()) ||
            !this.category.equals(otherEvent.getCategory()) ||
            this.cost != otherEvent.getCost() ||
            !getDate().equals(otherEvent.getDate()) ||
            !getTime().equals(otherEvent.getTime())
            ) return false;
        return true;

    }
}


