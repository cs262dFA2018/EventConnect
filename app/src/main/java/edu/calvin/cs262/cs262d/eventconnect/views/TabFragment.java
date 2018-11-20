package edu.calvin.cs262.cs262d.eventconnect.views;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
    private Context context;
    boolean event_deleted = false;

    public TabFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle my_args = getArguments();
        //get the necessary resources to check which tab I am.
        context = new WeakReference<Context>(getActivity().getApplicationContext()).get();
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

    /**
     * Overriding long click handling for the card_container_adapter
     * Currently not in use, but could be figured out for future use
     * TODO: use or remove this code
     *
     * @param clicked_event event clicked by the user
     * @param action what to do with the event
     */
    @Override
    public void onLongClick(Event clicked_event, String action) {
        switch (action){
            case "Delete Event":
                Toast.makeText(getActivity(),context.getString(R.string.Delete_Event_Worked),
                        Toast.LENGTH_LONG).show();
                break;
            default:
                throw new RuntimeException("Error: In TabFragment, Long Click Action Not Recognized");
        }
    }


    //the class implementing this is responsible for summoning dialogFragments.
    //IMPORTANT: MAIN ACTIVITY SHOULD BE THE ONLY ACTIVITY TO EVER CALL THIS.
    public interface CardExpansionHandler {
        void showExpandedCard(Event event);
    }

    /** Overriding click handling for the card_container_adapter **/
    @Override
    public void onClick(final Event clicked_event, String action) {
        switch (action){
            case "Expand Thy Card":
                showExpandedCard(clicked_event);
                break;
            case "Move Thy Card":
                database.movePotentialEvent(clicked_event);
                Toast.makeText(getActivity(),context.getString(R.string.Event_Confirmed),
                        Toast.LENGTH_LONG).show();
                break;
            case "Un-move Thy Card":
                database.moveCompletedEvent(clicked_event);
                Toast.makeText(getActivity(), context.getString(R.string.Event_Unconfirmed),
                        Toast.LENGTH_LONG).show();
                break;
            case "Delete Event":
                //make two runnables
                //make a new confirmDialog
                //pass in the stuffs
                //create and save instance of confirm dialog
                //have confirm dialog extend alert dialog, so you can still do things like setMessage and setPositiveButton
                //(you might have to make ConfirmDialog implement DialogInterface.OnClickListener() )
                //REMEMBER, your runnable is effectively the onClick. The trick: linking that to the button click.

                //then, check your eventDeleted varaible to see what you should return. Or better, just return eventDeleted.

                Runnable deleteRunnable = new Runnable() {
                    @Override
                    public void run() {
                        deleteEvent(clicked_event);
                    }
                };

                Runnable cancelRunnable = new Runnable() {
                    @Override
                    public void run() {}
                };

                //TODO: possibly put in a check here that the activity in the background hasn't finished? Need a weak reference and "isFinishing"
                new ConfirmDialog("Are you sure you want to delete this event?", "delete",
                        context, deleteRunnable, cancelRunnable);
                if (event_deleted) {
                    //TODO: animate the event deletion
                }


                break;
            default:
                throw new RuntimeException("Error: In TabFragment, Click Action Not Recognized");

        }

    }

    //DEFINE RUNNABLE FUNCTIONS HERE, setting your TabFragment eventDeleted boolean inside of the runnable function.
    public void deleteEvent(Event clicked_event) {
        database.deleteEvent(clicked_event);
        card_container_adapter.removeCard(clicked_event);
        Toast.makeText(getActivity(),context.getString(R.string.Delete_Event_Worked),
                Toast.LENGTH_LONG).show();
        event_deleted = true;
    }

    /**implemented for TabFragment, this method summons an expanded card view
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
}
