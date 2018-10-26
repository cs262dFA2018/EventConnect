package edu.calvin.cs262.cs262d.eventconnect.views;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.Event;
import edu.calvin.cs262.cs262d.eventconnect.data.MockDatabase;
import edu.calvin.cs262.cs262d.eventconnect.tools.CardContainerAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment extends Fragment implements CardContainerAdapter.CardContainerAdapterOnClickHandler {

    private RecyclerView card_container;
    private CardContainerAdapter card_container_adapter;
    private ArrayList<Event> event_data;
    private MockDatabase database;

    public TabFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle my_args = getArguments();
        //get the necessary resources to check which tab I am.
        Context context = new WeakReference<Context>(getActivity().getApplicationContext()).get();
        database = MockDatabase.getInstance();
        //check which tab I am based on the tab name and what PagerAdapter.java told me I am
        if (context.getString(R.string.tab_label_potential).equals(getArguments().getString("Fragment_id"))) {
            event_data = database.getPotentialEventData();
        } else if (context.getString(R.string.tab_label_confirmed).equals(getArguments().getString("Fragment_id"))) {
            event_data = database.getConfirmedEventData();
        } else {
            //If I am being used for something else and haven't been informed of that, then I shouldn't be created at all!
            throw new RuntimeException("ERROR: tab fragment created for undetermined purpose.");
        }

        //build the adapter for this fragment's recycler view
        card_container_adapter = new CardContainerAdapter(this, context);
        card_container_adapter.setCards(event_data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View frag_layout = inflater.inflate(R.layout.tab_fragment, container, false);
        //build the RecyclerView for this fragment and provide its adapter
        card_container = (RecyclerView) frag_layout.findViewById(R.id.tab_recycler_view);
        card_container.setLayoutManager(new LinearLayoutManager(getActivity()));
        card_container.setAdapter(card_container_adapter);
        return frag_layout;
    }

    /** Overriding click handling for the card_container_adapter **/

    @Override
    public void onClick(Event clicked_event, String action) {

        switch (action){
            case "Expand Thy Card":
                Toast.makeText(getActivity(), " Expanding Thy Card",
                        Toast.LENGTH_SHORT).show();
                break;
            case "Move Thy Card":
                database.movePotentialEvent(clicked_event);
                break;
            default:
                throw new RuntimeException("Error: In TabFragment, Click Action Not Recognized");

        }

    }
}
