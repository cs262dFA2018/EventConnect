package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.Event;

/**an adapter class used by MainActivity's Fragments' Recycle Views.
 * This class manages the displaying and click handling of event cards
 * and their respective UI elements.
 */
public class CardContainerAdapter extends RecyclerView.Adapter<CardContainerAdapter.CardContainerAdapterViewHolder> {

    private ArrayList<Event> cards;

    private final CardContainerAdapterOnClickHandler click_handler;
    private Context context;
    private final String ExpandCard = "Expand Thy Card";
    private final String MoveCard = "Move Thy Card";
    private final String UnmoveCard = "Un-move Thy Card";

    //the class containing this adapter may need to implement an onClick at the higher level
    public interface CardContainerAdapterOnClickHandler {
        void onClick(Event clicked_event, String action );
    }

    /**
     * Constructor
     **/
    public CardContainerAdapter(CardContainerAdapterOnClickHandler given_click_handler, Context app_context) {
        click_handler = given_click_handler;
        context = app_context;
    }

    /**
     * a viewholder to handle clicks that happen within the recycler view;
     * receives card clicks to open up the expanded event view
     * receives the card's "interested" button click
     * handles clicks by expanding a card or marking interest
     */

    public class CardContainerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView eventTitle, eventDescription;
        private Button interestedButton;
        private CardView eventCard;
        private final int animationTime = 500;
        /**
         * constructor
         * gives this viewholder access to a card's internal xml elements
         * then sets this viewholder to listen for clicks on each of those elements
         **/
        public CardContainerAdapterViewHolder(View view) {
            super(view);
            eventCard = view.findViewById(R.id.card_view);
            eventTitle = (TextView) view.findViewById(R.id.event_title);
            eventDescription = (TextView) view.findViewById(R.id.event_desc);
            interestedButton = (Button) view.findViewById(R.id.interested_button);
            eventTitle.setOnClickListener(this);
            eventDescription.setOnClickListener(this);
            interestedButton.setOnClickListener(this);


        }

        /**
         * this onClick(View v) is called by the recycler view's child views at click
         *
         * @param: View v, a View that was clicked on a card (ex: text views, the "interested" button)
         * once the clicked item type has been determined, this viewholder calls Adapter's
         * click_handler.onClick(), passing relevant information
         */
        @Override
        public void onClick(View view) {
            Event event_clicked = cards.get(getAdapterPosition());
            if (view == eventTitle || view == eventDescription){
                /* show toast
                 * for next deliverable, show full event view
                 */
                click_handler.onClick(event_clicked, ExpandCard);
            }
            else if (view == interestedButton){

                /* If current interest is false, mark they're interested now
                 */
                if (!event_clicked.getInterest()) {
                    event_clicked.setInterest();
                    event_clicked.incrementCurrentInterest();
                    if (event_clicked.shouldMove()) {
                        /*when we have enough interest, get data form data base and reset card
                         */
                        click_handler.onClick(event_clicked, MoveCard);
                        event_clicked.clearMoved();

                        Animation animation = new AlphaAnimation(1.0f, 0.0f);
                        animation.setDuration(animationTime);
                        eventCard.startAnimation(animation);

                        //Wait to remove the event from UI until animation finishes
                        Runnable eventRemover = createRunnable(event_clicked);
                        new Handler().postDelayed(eventRemover, animationTime);
                    } else {
                        interestedButton.setText(context.getString(R.string.interested));
                    }
                }
                /* If current interest is true, mark they're not interested anymore
                 */
                else if (event_clicked.getInterest()) {
                    event_clicked.clearInterest();
                    event_clicked.decrementCurrentInterest();
                    if (event_clicked.shouldMove()) {
                        /* If too little interest, move card back to potential event
                         */
                        click_handler.onClick(event_clicked, UnmoveCard);
                        event_clicked.clearMoved();

                        Animation animation = new AlphaAnimation(1.0f, 0.0f);
                        animation.setDuration(animationTime);
                        eventCard.startAnimation(animation);

                        //Wait to remove the event from UI until animation finishes
                        Runnable eventRemover = createRunnable(event_clicked);
                        new Handler().postDelayed(eventRemover, animationTime);
                    } else {
                        interestedButton.setText(context.getString(R.string.not_interested));
                    }
                }
            }
        }
        // Had to use runnable b/c removeCard reset the UI before the Animation finished
        private Runnable createRunnable(final Event e){

            Runnable removeRunnable = new Runnable(){
                public void run(){
                    removeCard(e);
                }
            };
            return removeRunnable;

        }
    }


    /** called when each ViewHOlder in the RecycleView is created.
     * @param parent: the single view group that contains all the cards
     * @param viewType: an integer indicating different types of items
     * @return: a CardContainerAdapterViewHOlder that contains the Views for each card.
     */
    @NonNull
    @Override
    public CardContainerAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int ListItemId = R.layout.event_card;
        LayoutInflater inflater = LayoutInflater.from(context);
        final boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(ListItemId, parent, shouldAttachToParentImmediately);
        return new CardContainerAdapterViewHolder(view);
    }

    /**onBindViewHolder takes data from Event items and places the data onto the card_UI.
     * @param card_holder the viewholder defined in this adapter class
     * @param position the card at which the adapter is currently looking
     * Postcondition: the card's UI elements are set with its Event information.
     */
    @Override
    public void onBindViewHolder(@NonNull CardContainerAdapterViewHolder card_holder, int position) {
        Event current_card = cards.get(position);
        card_holder.eventTitle.setText(current_card.getTitle());
        card_holder.eventDescription.setText(current_card.getDescription());
        if(current_card.getInterest()){
            card_holder.interestedButton.setText(context.getString(R.string.interested));
        }
    }

    /** get the number of cards in the adapter's data structure **/
    @Override
    public int getItemCount() {
        if (null == cards) return 0;
        return cards.size();
    }

    /** set the cards in the adapter's data structure **/
    public void setCards(ArrayList<Event> new_cards) {
        cards = new_cards;
        notifyDataSetChanged(); //a method inside of Recycler View.
    }

    private void addCard(Event event){
        cards.add(event);
        notifyDataSetChanged();
    }
    private void removeCard(Event event){
        cards.remove(event);
        notifyDataSetChanged();
    }
}
