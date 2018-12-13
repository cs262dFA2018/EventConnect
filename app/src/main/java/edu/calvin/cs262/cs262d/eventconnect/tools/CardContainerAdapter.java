package edu.calvin.cs262.cs262d.eventconnect.tools;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.Event;


/**an adapter class used by MainActivity's Fragments' Recycle Views.
 * This class manages the displaying and click handling of event cards
 * and their respective UI elements.
 */
public class CardContainerAdapter extends RecyclerView.Adapter<CardContainerAdapter.CardContainerAdapterViewHolder> {

    private List<Event> cards;

    private final CardContainerAdapterOnClickHandler click_handler;
    private Context context;
    private final String ExpandCard = "Expand Thy Card";
    private final String JoinEvent = "Join Event";
    private final String LeaveEvent = "Leave Event";
    private final String EditCard = "Edit Event";
    private final String DeleteCard = "Delete Event";
    private final String MyCard = "Add to My Events";
    private final String NotMyCard = "Remove from My Events";

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
        private TextView eventTitle, eventDescription, eventMenu;
        private Button interestedButton;
        private CardView eventCard;
//        private Button deleteButton;
        private final int animationTime = 500;

        /**
         * constructor
         * gives this viewholder access to a card's internal xml elements
         * then sets this viewholder to listen for clicks on each of those elements
         **/
        public CardContainerAdapterViewHolder(View view) {
            super(view);
            eventCard = (CardView) view.findViewById(R.id.card_view);
            eventTitle = (TextView) view.findViewById(R.id.event_title);
            eventDescription = (TextView) view.findViewById(R.id.event_desc);
            interestedButton = (Button) view.findViewById(R.id.interested_button);
            eventMenu = (TextView) view.findViewById(R.id.event_options);
//            deleteButton = (Button) view.findViewById(R.id.delete_button);
            eventCard.setOnClickListener(this);
            eventTitle.setOnClickListener(this);
            eventDescription.setOnClickListener(this);
            interestedButton.setOnClickListener(this);
            interestedButton.setEnabled(true);
            eventMenu.setOnClickListener(this);
//            deleteButton.setOnClickListener(this);
//            deleteButton.setEnabled(true);
        }

        /**
         * //TODO: fix this documentation.
         * this onClick(View v) is called by the recycler view's child views at click
         *
         * @param view v, a View that was clicked on a card (ex: text views, the "interested" button)
         * once the clicked item type has been determined, this viewholder calls Adapter's
         * click_handler.onClick(), passing relevant information
         */
        @Override
        public void onClick(View view) {
            final Event event_clicked = cards.get(getAdapterPosition());
            if (view == interestedButton){
                /* If indicating interest, join the event */
                if (!event_clicked.getInterest()) {
                    click_handler.onClick(event_clicked, JoinEvent);
                }
                /* Else, indicating disinterest, so leave the event */
                else {
                    click_handler.onClick(event_clicked, LeaveEvent);
                }
            } else if (view == eventMenu) {
                /* thanks to Belal Khan for inspiration. See
                 * https://www.simplifiedcoding.net/create-options-menu-recyclerview-item-tutorial/#Creating-Menu-Items
                 * for algorithmic pattern.
                 */
                //Create the popup menu with Edit Event and Delete Event
                PopupMenu menu = new PopupMenu(context, eventMenu);
                menu.inflate(R.menu.menu_card);
                //click handler for menu items
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                   @Override
                   public boolean onMenuItemClick(MenuItem item) {
                       switch (item.getItemId()) {
                           case R.id.action_edit_event:
                               click_handler.onClick(event_clicked, EditCard);
                               break;
                           case R.id.action_delete_event:
                               click_handler.onClick(event_clicked, DeleteCard);
                               break;
                       }
                       return false; //I'm not sure why... -LS88
                   }
                });
                //display the popup menu
                menu.show();
                //
            } else if (view == eventTitle || view == eventDescription || view == eventCard) {
                click_handler.onClick(event_clicked, ExpandCard);
            }
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
        card_holder.interestedButton.setEnabled(true);
        if(current_card.getInterest()){
            card_holder.interestedButton.setText(context.getString(R.string.interested));
        } else {
            card_holder.interestedButton.setText(context.getString(R.string.not_interested));
        }
    }

    /** get the number of cards in the adapter's data structure **/
    @Override
    public int getItemCount() {
        if (null == cards) return 0;
        return cards.size();
    }

    /** set the cards in the adapter's data structure **/
    public void setCards(List<Event> new_cards) {
        cards = new_cards;
        notifyDataSetChanged(); //a method inside of Recycler View.
    }
}
