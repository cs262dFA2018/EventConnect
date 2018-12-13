package edu.calvin.cs262.cs262d.eventconnect.views;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.Locale;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.Event;
import edu.calvin.cs262.cs262d.eventconnect.data.EventsData;

//based on android's documentation, https://developer.android.com/reference/android/app/DialogFragment
public class ExpandedCard extends DialogFragment {

    private TextView hostLabel, hostView, titleLabel, titleView, descriptionLabel, descriptionView;
    private TextView dateLabel, dateView, locationLabel, locationView, costLabel, costView;
    private TextView catView, catLabel, timeLabel, timeView, thresholdLabel, thresholdView;
    private TextView capacityLabel, capacityView, interestLabel, interestView;
    private boolean interested;
    private String title, description, host, location, date, cat, time;
    private double cost;
    private int threshold, capacity, interestCount;
    private Toolbar toolbar;

    /**
     * Create a new instance of ExpandedCard, providing arguments from an Event
     * @param event event data to put on expanded card
     * @author Littlesnowman88
     * @author OneTrueAsian (back arrow)
     */
    static ExpandedCard newInstance(Event event) {
        ExpandedCard ec = new ExpandedCard();

        //Store the event provided by MainActivity (the event card clicked on)
        Bundle args = new Bundle();
        args.putString("title", event.getTitle());
        args.putString("description", event.getDescription());
        args.putBoolean("interested", event.getInterest());
        args.putString("host", event.getHost());
        args.putString("location", event.getLocation());
        args.putString("date", event.getDate());
        args.putString("cat", event.getCategory());
        args.putDouble("cost", event.getCost());
        args.putString("time", event.getTime());
        args.putInt("threshold", event.getMinThreshold());
        args.putInt("capacity", event.getMaxCapacity());
        args.putInt("interest", event.getCurrentInterest());

        ec.setArguments(args);

        return ec;
    }

    /**
     * onCreate sets up the ExpandedCard view
     * @param savedInstanceState bundle of event data
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* access the event Data */
        Bundle args = getArguments();
        try {
            title = args.getString("title");
        } catch (java.lang.NullPointerException ne) {
            title = "";
        }
        try {
            description = args.getString("description");
        } catch (java.lang.NullPointerException ne) {
            description = "";
        }
        try {
            host = args.getString("host");
        } catch (java.lang.NullPointerException ne) {
            host = "";
        }
        try {
            interested = args.getBoolean("interested");
        } catch (java.lang.NullPointerException ne) {
            interested = false;
        }
        try {
            location = args.getString("location");
        } catch (java.lang.NullPointerException ne) {
            location = "";
        }
        try {
            date = args.getString("date");
        } catch (java.lang.NullPointerException ne) {
            date = "";
        }
        try {
            cost = args.getDouble("cost");
        } catch (java.lang.NullPointerException ne) {
            cost = 0;
        }
        try {
            time = args.getString("time");
        } catch (java.lang.NullPointerException ne){
            time ="";
        }
        try{
            cat = args.getString("cat");
        } catch (java.lang.NullPointerException ne){
            cat ="";
        }
        threshold = args.getInt("threshold");
        capacity = args.getInt("capacity");
        interestCount = args.getInt("interest");
    }

    /**
     * onCreateView uses an inflater to set up the expanded card view.
     * Back arrow uses an ImageButton that the user taps on instead of the traditional button.
     * @param inflater layout inflator to use
     * @param container view group for the expanded card
     * @param savedInstanceState expanded card data
     * @return result of inflater
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.expanded_event_fragment, container, false);
        ImageButton backButton = (ImageButton) v.findViewById(R.id.back_button);

        /**
         * onClick listener for backButton to move to main when clicked.
         */
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;
    }

    /**
     * Applies data to the expanded card
     * @param view view the expanded card is in
     * @param savedInstanceState expanded card data
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set a transparent background so rounded corners can show up. Rounded corners specified in Expanded Card's xml.
        try {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } catch (java.lang.NullPointerException ne) {
            ne.printStackTrace();
            //don't do anything else because this is just a UI Beautification.
        }

        ScrollView rootView = view.findViewById(R.id.expanded_root_layout);

        // set the expanded card's background to correspond with light or dark mode.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String darkMode = sharedPrefs.getString("theme_preference", "Light");
        if (darkMode.equals("Light")) {
            rootView.setBackground(getResources().getDrawable(R.drawable.expanded_card_background_light));
        }
        else if (darkMode.equals("Dark")) {
            rootView.setBackground(getResources().getDrawable(R.drawable.expanded_card_background_dark));
        }

        //access the UI
        hostLabel = (TextView) view.findViewById(R.id.host_label_text);
        hostView = (TextView) view.findViewById(R.id.host_text);
        titleLabel = (TextView) view.findViewById(R.id.title_label_text);
        titleView = (TextView) view.findViewById(R.id.title_text);
        descriptionLabel = (TextView) view.findViewById(R.id.description_label_text);
        descriptionView = (TextView) view.findViewById(R.id.description_text);
        dateLabel = (TextView) view.findViewById(R.id.date_label_text);
        dateView = (TextView) view.findViewById(R.id.date_text);
        locationLabel = (TextView) view.findViewById(R.id.location_label_text);
        locationView = (TextView) view.findViewById(R.id.location_text);
        costLabel = (TextView) view.findViewById(R.id.cost_label_text);
        costView = (TextView) view.findViewById(R.id.cost_text);
        catLabel = view.findViewById(R.id.cat_label_text);
        catView = view.findViewById(R.id.cat_text);
        timeLabel = (TextView) view.findViewById(R.id.time_label_text);
        timeView = (TextView) view.findViewById(R.id.time_text);
        thresholdLabel = (TextView) view.findViewById(R.id.threshold_label_text);
        thresholdView = (TextView) view.findViewById(R.id.threshold_text);
        capacityLabel = (TextView) view.findViewById(R.id.capacity_label_text);
        capacityView = (TextView) view.findViewById(R.id.capacity_text);
        interestLabel = (TextView) view.findViewById(R.id.interest_label_text);
        interestView = (TextView) view.findViewById(R.id.interest_text);

        //set UI text
        hostLabel.setText(getString(R.string.host_label));
        hostView.setText(host);
        titleLabel.setText(getString(R.string.title_label));
        titleView.setText(title);
        descriptionLabel.setText(getString(R.string.description_label));
        descriptionView.setText(description);
        dateLabel.setText(getString(R.string.date_label));
        dateView.setText(date);
        timeLabel.setText(getString(R.string.time_label));
        timeView.setText(time);
        locationLabel.setText(getString(R.string.location_label));
        locationView.setText(location);
        costLabel.setText(getString(R.string.cost_label));
        costView.setText(String.format(Locale.getDefault(), Double.toString(cost), Double.toString(cost)));
        catView.setText(cat);
        thresholdView.setText(Integer.toString(threshold));
        capacityView.setText(Integer.toString(capacity));
        interestView.setText(Integer.toString(interestCount));
        //if I (currently logged in user) own this event, display threshold, capacity, and interest.
        if (host.equals(EventsData.getInstance(null).getCredentials()[0])) {
            thresholdLabel.setVisibility(View.VISIBLE);
            thresholdView.setVisibility(View.VISIBLE);
            capacityLabel.setVisibility(View.VISIBLE);
            capacityView.setVisibility(View.VISIBLE);
            interestLabel.setVisibility(View.VISIBLE);
            interestView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * onStart shows the already created expanded card view
     * Expands previous card from 325 to Fill_Parent
     * @author OneTrueAsian
     * @author Littlesnowman88
     */
    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.FILL_PARENT;
            int height = ViewGroup.LayoutParams.FILL_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

}


