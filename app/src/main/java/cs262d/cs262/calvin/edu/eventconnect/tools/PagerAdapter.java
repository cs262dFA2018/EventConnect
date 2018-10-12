package cs262d.cs262.calvin.edu.eventconnect.tools;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import cs262d.cs262.calvin.edu.eventconnect.R;
import cs262d.cs262.calvin.edu.eventconnect.views.TabFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int mNumOfTabs;
    private Context context;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, Context app_context) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        context = app_context;
    }

    @Override
    public Fragment getItem(int position) {
        /* android documentation strongly suggests passing information to the fragment with a Bundle,
         * probably to avoid dependency problems and lifecycle problems.
         */
        Bundle fragment_data = new Bundle();
        switch (position) {
            case 0:
                TabFragment tabPotential = new TabFragment();
                //tell the fragment which tab it is
                fragment_data.putString("Fragment_id", context.getString(R.string.tab_label_potential));
                tabPotential.setArguments(fragment_data);
                return tabPotential;
            case 1:
                TabFragment tabConfirmed = new TabFragment();
                //tell the fragment which tab it is
                fragment_data.putString("Fragment_id", context.getString(R.string.tab_label_confirmed));
                tabConfirmed.setArguments(fragment_data);
                return tabConfirmed;
            default:
                throw new RuntimeException("ERROR: unknown tab clicked in main window.");
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
