package edu.calvin.cs262.cs262d.eventconnect.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import edu.calvin.cs262.cs262d.eventconnect.R;
import edu.calvin.cs262.cs262d.eventconnect.tools.PagerAdapter;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private Intent mainToLogin;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        mainToLogin  = new Intent(context, LoginActivity.class);

        currentUser = getIntent().getStringExtra("UserID");

        //setup action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //then set up toolbar/actionbar's up navigation
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }

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
     *               and the strings tab_label_potential and tab_label_confirmed exist
     *               in strings.xml
     * Postcondition: Tabs are created for the Main Activity
     */
    private void buildTabs(TabLayout tabs) {
        //Build the tabs.
        // Create an instance of the tab layout from the view.
        // Set the text for each tab.
        tabs.addTab(tabs.newTab().setText(R.string.tab_label_potential));
        tabs.addTab(tabs.newTab().setText(R.string.tab_label_confirmed));
        // Set the tabs to fill the entire layout.
        tabs.setTabGravity(tabs.GRAVITY_FILL);
    }

    /** buildPagerAdapter is a helper function refactored from onCreate
     * @param tabs
     * Creates a page for each of the "tab" fragments in MainActivity
     * Creates a ViewPager, an adapter.
     * Then gives ViewPager the adapter.
     * Then creates and sets a tab listener, handling the various possible clicks
     * Postcondition: the user can swap between tabs once this function completes.
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

    /** called whenever the user presses the up button or any menu item on MainActivity's toolbar
     * On the up button pressed, the app "returns" to the login activity
     * @param item, the menu item clicked by the user
     * @return handled by AppCompatActivity, I think.
     * @author: Littlesnowman88
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            /*IMPORTANT:
             * Because LoginActivity launches as singleTask (see manifest), this will not create multiple
             * copies of startActivity. Although better practice suggests using finish(), here finish() usually
             * exited the application instead of navigating to the parent LoginActivity.
             */
            startActivity(mainToLogin);
        }
        return super.onOptionsItemSelected(item);
    }

    /** called when the user presses the back button.
     * On back button pressed, the app "returns" to the login activity
     * @author: Littlesnowman88
     */
    @Override
    public void onBackPressed() {
        startActivity(mainToLogin);
    }

    /**starts up the AddEvent activity**/
    public void addEventClicked(View view) {
        Intent addEvent = new Intent(MainActivity.this, AddEvent.class);
        MainActivity.this.startActivity(addEvent);
    }
}
