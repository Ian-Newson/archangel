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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import iannewson.com.archangel.R;
import iannewson.com.archangel.database.Player;
import iannewson.com.archangel.database.PlayerRepository;
import iannewson.com.archangel.events.NumberSearchingChangedEvent;
import iannewson.com.archangel.models.dtos.Game;
import iannewson.com.archangel.models.dtos.Leaderboard;

/**
 * A placeholder fragment containing a simple view.
 */
public class GameListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static GameListFragment newInstance() {
        GameListFragment fragment = new GameListFragment();
        return fragment;
    }

    //public static final String URL = "https://api.myjson.com/bins/1gkl4a";//"https://aa.sdawsapi.com/matchmaking";
    public static final String URL = "https://aa.sdawsapi.com/matchmaking";

    private RecyclerView mList;
    private TextView txtEmpty;
    private SwipeRefreshLayout mRefresh;

    private Game[] mGames = new Game[0];

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


        mList.setAdapter(new RecyclerView.Adapter<GameListFragment.ViewHolder>() {

            @NonNull
            @Override
            public GameListFragment.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                ViewGroup view = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.listitem_game, parent, false);
                return new ViewHolder(view);

            }

            @Override
            public void onBindViewHolder(@NonNull GameListFragment.ViewHolder viewHolder, int i) {
                Game game = mGames[i];
                //viewHolder.txt.setText(String.format("%d player%s", game.playerIds.length, game.playerIds.length == 1 ? "" : "s"));

                if (null != game.startTime)
                    viewHolder.date.setText(DateTimeUtils.getTimeAgo(getContext(), game.startTime));
                else
                    viewHolder.date.setText("");

                PlayerRepository playerRepo = new PlayerRepository();
                List<Player> players = new ArrayList<>();
                for (UUID playerId : game.playerIds) {
                    Player player = playerRepo.getPlayerById(playerId);
                    if (null != player) {
                        players.add(player);
                    }
                }

                StringBuilder strNames = new StringBuilder();
                for (int p = 0; p < players.size(); ++p) {
                    if (p > 0) {
                        strNames.append(", ");
                    }
                    strNames.append(players.get(p).getDescription());
                }

                if (strNames.length() == 0) {
                    strNames.append(String.format("%d new player%s", game.playerIds.length, game.playerIds.length != 1 ? "s" : ""));
                } else {
                    if (game.playerIds.length > players.size()) {
                        strNames.append(String.format("(+ %d new players", (game.playerIds.length - players.size())));
                    }
                }
                viewHolder.txt.setText(strNames.toString());
            }

            @Override
            public int getItemCount() {
                return mGames.length;
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

        EventBus.getDefault().register(this);

        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    private boolean hasRetrievedPlayers = false;

    private void loadData() {
        mRefresh.setRefreshing(true);
        txtEmpty.setVisibility(View.GONE);
        mGames = new Game[0];
        mList.getAdapter().notifyDataSetChanged();

        final Request request = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                mRefresh.setRefreshing(false);
                if (0 == response.length()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    txtEmpty.animate().alpha(1).start();
                    return;
                }

                mGames = new Gson().fromJson(response.toString(), Game[].class);

                mList.getAdapter().notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                toast(error.getMessage());
                mRefresh.setRefreshing(false);
            }
        });

        Request playersRequest = new JsonObjectRequest(Request.Method.GET, "https://api.operaevent.co/api/archangel_leaderboards", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    Leaderboard leaderboard = new Gson().fromJson(response.toString(), Leaderboard.class);

                    if (null != leaderboard && null != leaderboard.global) {
                        PlayerRepository players = new PlayerRepository();
                        players.sync(leaderboard.global);
                    }
                } catch (IllegalArgumentException ex) {
                    Toast.makeText(GameListFragment.this.getContext(), String.format("There was an error updating players. %s", ex.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("Message", ex.getLocalizedMessage());
                    FirebaseAnalytics.getInstance(GameListFragment.this.getContext()).logEvent("Exception", bundle);
                }

                mRequests.add(request);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                toast(error.getMessage());
                mRefresh.setRefreshing(false);
            }
        });

        if (hasRetrievedPlayers) {
            mRequests.add(request);
        } else {
            mRequests.add(playersRequest);
            hasRetrievedPlayers = true;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull ViewGroup itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txtName);
            date = itemView.findViewById(R.id.txtDate);
        }

        public TextView txt, date;
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NumberSearchingChangedEvent event) {
        onRefresh();
    }
}
