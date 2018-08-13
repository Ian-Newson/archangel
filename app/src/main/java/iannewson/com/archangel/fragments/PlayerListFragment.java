package iannewson.com.archangel.fragments;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.thunder413.datetimeutils.DateTimeUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import iannewson.com.archangel.R;
import iannewson.com.archangel.database.Player;
import iannewson.com.archangel.database.PlayerRepository;
import iannewson.com.archangel.models.dtos.Game;
import iannewson.com.archangel.models.dtos.Leaderboard;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlayerListFragment newInstance() {
        PlayerListFragment fragment = new PlayerListFragment();
        return fragment;
    }

    //public static final String URL = "https://api.myjson.com/bins/1gkl4a";//"https://aa.sdawsapi.com/matchmaking";
    public static final String URL = "https://aa.sdawsapi.com/matchmaking";

    private RecyclerView mList;
    private TextView txtEmpty;
    private SwipeRefreshLayout mRefresh;

    private Player[] mPlayers = new Player[0];

    private RequestQueue mRequests;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_list, container, false);

        ((ViewGroup)rootView.findViewById(R.id.root))
                .getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        mList = rootView.findViewById(R.id.list);
        txtEmpty = rootView.findViewById(R.id.txtEmpty);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mList.setLayoutManager(layoutManager);

        // SwipeRefreshLayout
        mRefresh = rootView.findViewById(R.id.swipe_container);
        mRefresh.setOnRefreshListener(this);
        mRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        mList.setAdapter(new RecyclerView.Adapter<PlayerListFragment.ViewHolder>() {

            @NonNull
            @Override
            public PlayerListFragment.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                ViewGroup view = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.listitem_player, parent, false);
                return new ViewHolder(view);

            }

            @Override
            public void onBindViewHolder(@NonNull PlayerListFragment.ViewHolder viewHolder, int i) {
                Player player = mPlayers[i];
                //viewHolder.txt.setText(String.format("%d player%s", game.playerIds.length, game.playerIds.length == 1 ? "" : "s"));

                viewHolder.txtName.setText(player.getName());
                viewHolder.txtKdr.setText(String.format("%.1f", player.getKdr()));
                viewHolder.txtScore.setText(String.format("%.0f", player.getScore()));
                viewHolder.txtWlr.setText(String.format("%.1f", player.getWlr()));
            }

            @Override
            public int getItemCount() {
                return mPlayers.length;
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (null != mRequests) {
            mRequests.stop();
        }
        mRequests = Volley.newRequestQueue(getContext().getApplicationContext());
        mRequests.start();

        txtEmpty.setVisibility(View.INVISIBLE);

        loadData();
    }

    private boolean hasRetrievedPlayers = false;

    private void loadData() {
        mRefresh.setRefreshing(true);
        txtEmpty.setVisibility(View.GONE);
        mPlayers = new Player[0];
        mList.getAdapter().notifyDataSetChanged();

        Request playersRequest = new JsonObjectRequest(Request.Method.GET, "https://api.operaevent.co/api/archangel_leaderboards", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    Leaderboard leaderboard = new Gson().fromJson(response.toString(), Leaderboard.class);

                    if (null != leaderboard && null != leaderboard.global) {
                        PlayerRepository players = new PlayerRepository();
                        players.sync(leaderboard.global);
                        mPlayers = players.getAll().toArray(new Player[0]);
                    }

                    if (0 == response.length()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                        txtEmpty.animate().alpha(1).start();
                        return;
                    }
                } catch (IllegalArgumentException ex) {
                    Toast.makeText(PlayerListFragment.this.getContext(), String.format("There was an error updating players. %s", ex.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("Message", ex.getLocalizedMessage());
                    FirebaseAnalytics.getInstance(PlayerListFragment.this.getContext()).logEvent("Exception", bundle);
                    mPlayers = new PlayerRepository().getAll().toArray(new Player[0]);
                }

                mRefresh.setRefreshing(false);
                mList.getAdapter().notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                toast(error.getMessage());
                mRefresh.setRefreshing(false);
            }
        });

        mRequests.add(playersRequest);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull ViewGroup itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtKdr = itemView.findViewById(R.id.txtKdr);
            txtScore = itemView.findViewById(R.id.txtScore);
            txtWlr = itemView.findViewById(R.id.txtWlr);
        }

        public TextView txtName, txtKdr, txtScore, txtWlr;
    }

    @Override
    public void onRefresh() {
        loadData();
    }
}
