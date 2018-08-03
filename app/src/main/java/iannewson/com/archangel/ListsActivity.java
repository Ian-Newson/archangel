package iannewson.com.archangel;

import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import iannewson.com.archangel.events.NumberSearchingChangedEvent;
import iannewson.com.archangel.fragments.GameListFragment;
import iannewson.com.archangel.fragments.PlayerListFragment;
import iannewson.com.archangel.models.dtos.Stats;

public class ListsActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private TextView txtOnline, txtSearching;

    private RequestQueue mRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);

        txtSearching = $(R.id.txtSearching);
        txtOnline = $(R.id.txtOnline);

        if (null != mRequests) {
            mRequests.stop();
        }
        mRequests = Volley.newRequestQueue(getApplicationContext());
        mRequests.start();
    }

    private Timer mStatsTimer = null;

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mStatsTimer) {
            mStatsTimer.cancel();
            mStatsTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null != mStatsTimer) {
            mStatsTimer.cancel();
        }

        mStatsTimer = new Timer();
        mStatsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshStats();
                    }
                });
            }
        }, 0, 10*1000);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mRequests.stop();
    }

    private Integer mNumberSearching = null;

    public void refreshStats() {
        Request request = new JsonObjectRequest(Request.Method.GET, "https://aa.sdawsapi.com/matchmaking/stats", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Stats stats = new Gson().fromJson(response.toString(), Stats.class);

                int searching = 0;
                if (null != stats.statuses) {
                    searching = stats.statuses.SEARCHING;
                }

                if (null == mNumberSearching ||
                    mNumberSearching != searching) {
                    EventBus.getDefault().post(new NumberSearchingChangedEvent(mNumberSearching, searching));
                    mNumberSearching = searching;
                }

                txtSearching.setText(String.valueOf(searching));
                txtOnline.setText(String.valueOf(stats.total));
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                toast("Error retrieving stats: " + error.getLocalizedMessage());
            }
        });
        mRequests.add(request);
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: return GameListFragment.newInstance();
                case 1: return PlayerListFragment.newInstance();
                default: throw new RuntimeException(String.format("Invalid pager position %d", position));
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Games";
                case 1: return "Rankings";
            }
            return "NOTIMPLEMENTED";
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
