package edu.calvin.cs262.cs262d.eventconnect.views;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.Event;

//based on android's documentation, https://developer.android.com/reference/android/app/DialogFragment
public class ExpandedCard extends DialogFragment {

    Event my_event;
    TextView hostLabel, host, titleLabel, title, descriptionLabel, description,
        dateLabel, date, locationLabel, location, costLabel, cost;
    int current_interest;
    String ;

    /**
     * Create a new instance of ExpandedCard, providing args?
     */
    ExpandedCard newInstance(Event event) {
        ExpandedCard ec = new ExpandedCard();

        //Store the event provided by MainActivity (the event card clicked on)
        Bundle args = new Bundle();
        args.putInt("current_interest", event.getCurrentInterest());
        args.putString("title", event.getTitle());
        args.putString("description", event.getDescription());
        args.putBoolean("interest", event.getInterest());
        ec.setArguments(args);

        return ec;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //access the event Data

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Access the UI
        View v = inflater.inflate(R.layout.expanded_event_fragment, container, false);
        hostLabel = (TextView) v.findViewById(R.id.host_label_text);
        host = (TextView) v.findViewById(R.id.host_text);
        titleLabel = (TextView) v.findViewById(R.id.title_label_text);
        title = (TextView) v.findViewById(R.id.title_text);
        descriptionLabel = (TextView) v.findViewById(R.id.description_label_text);
        description = (TextView) v.findViewById(R.id.description_text);
        dateLabel = (TextView) v.findViewById(R.id.date_label_text);
        date = (TextView) v.findViewById(R.id.date_text);
        locationLabel = (TextView) v.findViewById(R.id.location_label_text);
        location = (TextView) v.findViewById(R.id.location_text);
        costLabel = (TextView) v.findViewById(R.id.cost_label_text);
        cost = (TextView) v.findViewById(R.id.cost_text);
    }



}
