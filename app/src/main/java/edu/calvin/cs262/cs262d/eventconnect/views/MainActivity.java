package edu.calvin.cs262.cs262d.eventconnect.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.data.EventsData;
import edu.calvin.cs262.cs262d.eventconnect.tools.AppThemeChanger;
import edu.calvin.cs262.cs262d.eventconnect.tools.EventsPoller;
import edu.calvin.cs262.cs262d.eventconnect.tools.PagerAdapter;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private Intent mainToLogin, mainToSettings;
    private String currentUser, currentPass;
    private String currentTheme;

    //Received from EventConnector, this is the intent filter used in appBroadcastReceiver. (see TabFragment)
    private static final String DATA_UPDATE = "processConnections";

    /**
     * creates the Main Activity:
     * builds UI with dark mode or light mode
     * establishes connection with loginActivity and settingsActivity
     * builds an action bar
     * builds the tabs for event cards
     * @param savedInstanceState the last known state of MainActivity
     * @author Littlesnowman88
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //access shared preferences for theme setting first.
        //MUST BE HANDLED BEFORE setContentView is called--in this case, before super.onCreate is called
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppThemeChanger.handleThemeChange(this, currentTheme);
        currentTheme = sharedPrefs.getString("theme_preference", "Light"); //default to Light theme

        //handle some normal MainActivity creation
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        //save the currently logged-in user
        currentUser = getIntent().getStringExtra("UserID");
        currentPass = getIntent().getStringExtra("UserPass");
        //give EventsData the currently logged in Username and Password.
        EventsData.getInstance(null).setCredentials(currentUser, currentPass);

        /*start EventsPoller, the service responsible for polling the server with get requests.
         * EventsPoller also creates an EventConnector for MainActivity, here.
         */
        Intent mainToDataManager = new Intent(context, EventsPoller.class);
        mainToDataManager.setAction(DATA_UPDATE);
        startService(mainToDataManager);

        mainToLogin = new Intent(MainActivity.this, LoginActivity.class);

        //establish connection with other activities
        mainToLogin  = new Intent(context, LoginActivity.class);
        mainToSettings = new Intent(context, SettingsActivity.class);

        //setup action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //next three lines are helper functions to keep onCreate() readable
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        buildTabs(tabLayout);
        buildPagerAdapter(tabLayout);
    }

    /** buildTabs is a helper function refactored from onCreate
     * @param tabs, a TabLayout specified from activity_main.xml
     * takes the tab layout from activity_main.xml,
     *      sets text for each tab,
     *      and sets the tabs to fill the entire layout.
     * EFFECTIVELY, part of onCreate.
     * Preconditions: tab_layout exists in activity_main.xml,
     *               and the strings tab_label_potential, tab_label_confirmed,
     *               tab_label_hosted, and tab_label_my exist in strings.xml
     * Postcondition: Tabs are created for the Main Activity
     * @author Littlesnowman88
     * @author ksn7
     */
    private void buildTabs(TabLayout tabs) {
        //Build the tabs.
        // Create an instance of the tab layout from the view.
        // Set the text for each tab.
        tabs.addTab(tabs.newTab().setText(R.string.tab_label_potential));
        tabs.addTab(tabs.newTab().setText(R.string.tab_label_confirmed));
        tabs.addTab(tabs.newTab().setText(R.string.tab_label_my));
        // Set the tabs to fill the entire layout.
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
    }

    /** buildPagerAdapter is a helper function refactored from onCreate
     * @param tabs
     * Creates a page for each of the "tab" fragments in MainActivity
     * Creates a ViewPager, an adapter.
     * Then gives ViewPager the adapter.
     * Then creates and sets a tab listener, handling the various possible clicks
     * Postcondition: the user can swap between tabs once this function completes.
     * @author Littlesnowman88
     */
    private void buildPagerAdapter(TabLayout tabs) {
        // Using PagerAdapter to manage page views in fragments.
        // Each page is represented by its own fragment.
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabs.getTabCount(), context);
        viewPager.setAdapter(adapter);

        // Setting a listener for clicks.
        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //do nothing because a tab cannot actually be unselected here.
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //do nothing because re-selecting a tab is the same as selecting a tab, here.
            }
        });
    }

    /** creates the action bar option items in MainActivity
     * inflates the menu so a user can click on settings
     * @param menu, the action bar menu
     * @return handled by AppCompatActivity
     * @author Littlesnowman88
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** called whenever the user presses the back button or any menu item on MainActivity's toolbar
     * On the back button pressed, the MainActivity finishes and the app closes.
     * On settings button pressed, the Settings activity is launched.
     * On the addEvent button clicked, the AddEvent activity is launched.
     *
     * @param item, the menu item clicked by the user
     * @return handled by AppCompatActivity
     * @author Littlesnowman88
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                /*IMPORTANT:
                 * Because LoginActivity launches as singleTask (see manifest),
                 * this will not create multiple copies of loginActivity.
                 * Furthermore, finish() will ensure that MainActivity is ended.
                 * Update: b/c LoginActivity is singleTask, finishing MainActivity without
                 *      starting LoginActivity exits the app. Neat!
                 */
                finish();
                break;
            case R.id.action_settings:
                //Open the settings activity
                startActivityForResult(mainToSettings, 1);
                break;
            case R.id.action_add_event:
                Intent addEvent = new Intent(MainActivity.this, AddEvent.class);
                startActivity(addEvent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * called when SettingsActivity finishes, this determines whether to log out of the app or stay on MainActivity.
     * @param requestCode the code sent by startActivityForResult
     * @param resultCode the code returned by SettingsActivity
     * @param data the data packed in SettingsActivity
     * @author Littlesnowman88
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if this is the activity started by action_settings
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK);
                finish();
            }
            //else, do nothing, Settings Activity was backed out of with no logging out.
        }
        //else do nothing. the request code isn't recognized.


    }

    /** called when the user presses the back button.
     * On back button pressed, MainActivity finishes, thus closing the app
     * (this happens because LoginActivity is a singleTask, I think. Neat!)
     *
     * @author Littlesnowman88
     */
    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
