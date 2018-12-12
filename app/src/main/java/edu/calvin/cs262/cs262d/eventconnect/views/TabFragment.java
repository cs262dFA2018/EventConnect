package edu.calvin.cs262.cs262d.eventconnect.views;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.Event;
import edu.calvin.cs262.cs262d.eventconnect.data.EventsData;
import edu.calvin.cs262.cs262d.eventconnect.tools.CardContainerAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment extends Fragment implements CardContainerAdapter.CardContainerAdapterOnClickHandler {

    private RecyclerView card_container;
    private CardContainerAdapter card_container_adapter;
    private EventsData dataSource = EventsData.getInstance();
    private List<Event> event_data;
    private Context context;

    public TabFragment() {
        // Required empty public constructor
    }

    /**
     * creates the tab fragment
     * @param savedInstanceState bundle of event and card data
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle my_args = getArguments();
        //get the necessary resources to check which tab I am.
        context = new WeakReference<Context>(getActivity().getApplicationContext()).get();
        //get access to the DataManager
        //check which tab I am based on the tab name and what PagerAdapter.java told me I am
        if (context.getString(R.string.tab_label_potential).equals(getArguments().getString("Fragment_id"))) {
            event_data = dataSource.getPotentialEventData();
        } else if (context.getString(R.string.tab_label_confirmed).equals(getArguments().getString("Fragment_id"))) {
            event_data = dataSource.getConfirmedEventData();
        } else {
            //If I am being used for something else and haven't been informed of that, then I shouldn't be created at all!
            throw new RuntimeException("ERROR: tab fragment created for undetermined purpose.");
        }

        //build the adapter for this fragment's recycler view
        card_container_adapter = new CardContainerAdapter(this, context);
        card_container_adapter.setCards(event_data);

        //register with local broadcast receiver to get updates from DataManager
        LocalBroadcastManager.getInstance(context).registerReceiver(updateReceiver,
                new IntentFilter("dataUpdate"));
    }

    /**
     * When the view is created, inflate the container and do other setup
     * @param inflater inflates the layout
     * @param container tells which viewgroup the fragment belongs to
     * @param savedInstanceState data bundle
     * @return the established layout
     */
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

    /**
     * overridden to also unregister this tabFragment from the updateReceiver
     * @author Littlesnowman88
     */
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(updateReceiver);
        super.onDestroy();
    }

    /** Overriding click handling for the card_container_adapter
     *
     * @param clicked_event the Event that a user clicked on. Determined in CardContainerAdapter.ViewHolder
     * @param action, the action passed up by CardContainerAdapter.ViewHolder to be performed.
     * @author Littlesnowman88
     * @author OneTrueAsian
     * @author ksn7
     */
    @Override
    public void onClick(final Event clicked_event, String action) {
        switch (action){
            case "Expand Thy Card":
                showExpandedCard(clicked_event);
                break;
            case "Move Thy Card":
                dataSource.movePotentialEvent(clicked_event);
                Toast.makeText(getActivity(),context.getString(R.string.Event_Confirmed),
                        Toast.LENGTH_LONG).show();
                break;
            case "Un-move Thy Card":
                dataSource.moveCompletedEvent(clicked_event);
                Toast.makeText(getActivity(), context.getString(R.string.Event_Unconfirmed),
                        Toast.LENGTH_LONG).show();
                break;
            case "Delete Event":
                //create the action for the alert Dialog's "delete" option
                Runnable deleteRunnable = new Runnable() {
                    @Override
                    public void run() {
                        deleteEvent(clicked_event);
                    }
                };
                //create the non-action for the alert Dialog's "cancel" option
                Runnable cancelRunnable = new Runnable() {
                    @Override
                    public void run() {}
                };

                //context must be getActivity because the alert dialog can be created in only an Activity context or a Service context.
                //cannot use TabFragment's instance of context, and cannot use getContext() because these provide Application Contexts.
                new ConfirmDialog("Are you sure you want to delete this event?", "delete",
                        getActivity(), deleteRunnable, cancelRunnable);
                //wait to actually delete the event until the deleteRunnable calls deleteEvent
                break;
            default:
                throw new RuntimeException("Error: In TabFragment, Click Action Not Recognized");
        }

    }

    /**
     * called by onClick's runnable object, deleteEvent deletes an event from the database and the UI.
     *
     * @param clicked_event the event that a user confirmed to delete
     * @author ksn7
     * @author Littlesnowman88
     */
    public void deleteEvent(Event clicked_event) {
        try {
            dataSource.deleteEvent(clicked_event);
        }
        catch (RuntimeException e) {
            //event wasn't found in the database
            e.printStackTrace();
        }
        //still delete the event from the adapter, since a user clicked on an event's DELETE button.
        card_container_adapter.deleteEvent(clicked_event);
        //display a message to the user, confirming the deletion of an event
        Toast.makeText(getActivity(),context.getString(R.string.Delete_Event_Worked),
                Toast.LENGTH_LONG).show();
    }

    /**implemented for TabFragment, this method summons an expanded card view
     *
     * @param event, the event clicked on by the user
     * Postcondition: an ExpandedCard is summoned and displayed for the user
     */
    public void showExpandedCard(Event event) {
        //the following code is based on https://developer.android.com/reference/android/app/DialogFragment
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        try {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = fm.findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the expanded card.
            ExpandedCard newExpansion = ExpandedCard.newInstance(event);
            newExpansion.show(ft, "dialog");
        } catch (java.lang.NullPointerException ne) {
            //log the problem to the console and notify the user that a problem happened.
            ne.printStackTrace();
            Toast.makeText(getActivity(), context.getString(R.string.failed_transaction_manager),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d("RECEIVER", "Got message: " + message);
        }
    };
}
