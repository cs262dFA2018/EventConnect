package edu.calvin.cs262.cs262d.eventconnect.data;

public class Event {
    private int current_interest, min_threshold, max_capacity;
    private String title, description;
    private boolean confirmed;
    private boolean needs_to_move;
    private boolean interest;

    public Event() {
        current_interest = 0;
        min_threshold = 1;
        max_capacity = -1;
        title = "";
        description = "";
        confirmed = false;
        needs_to_move = false;
        interest = false;
    }

    public String getTitle() {return title;}
    public void setTitle(String new_title) {title = new_title;}
    public String getDescription() {return description;}
    public void setDescription(String new_description) {description = new_description;}
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

    public void setMaxCapacity(int new_capacity) {
        if (new_capacity == -1 || (new_capacity > 0 && new_capacity > current_interest && new_capacity > min_threshold)) {
            max_capacity = new_capacity;
        } else {
            throw new RuntimeException("ERROR: attempt to set max capacity to an illegal state.");
        }
    }

    public int getMinThreshold() {return min_threshold;}

    public void setMinThreshold(int new_threshold) {
        if (new_threshold > 0 && new_threshold <= max_capacity) {
            min_threshold = new_threshold;
        } else {
            throw new RuntimeException("ERROR: attempt to set min threshold to an illegal state.");
        }
    }

    public void clearMoved(){ needs_to_move = false; }
    public boolean getInterest(){return interest;}
    public void setInterest(){interest = true;}
    public void clearInterest(){interest = false;}
}

