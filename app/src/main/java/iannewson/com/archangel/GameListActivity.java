package iannewson.com.archangel;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NoCache;
import com.github.thunder413.datetimeutils.DateTimeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import iannewson.com.archangel.BaseActivity;
import iannewson.com.archangel.R;

public class GameListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    //public static final String URL = "https://api.myjson.com/bins/1gkl4a";//"https://aa.sdawsapi.com/matchmaking";
    public static final String URL = "https://aa.sdawsapi.com/matchmaking";

    private RecyclerView mList;
    private TextView txtEmpty;
    private SwipeRefreshLayout mRefresh;

    private Game[] mGames = new Game[0];

    private RequestQueue mRequests = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gamelist);

        mList = findViewById(R.id.list);
        txtEmpty = findViewById(R.id.txtEmpty);

        LinearLayoutManager layoutManager = new LinearLayoutManager(GameListActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mList.setLayoutManager(layoutManager);

        // SwipeRefreshLayout
        mRefresh = findViewById(R.id.swipe_container);
        mRefresh.setOnRefreshListener(this);
        mRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mRequests.start();

        mList.setAdapter(new RecyclerView.Adapter<ViewHolder>() {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                ViewGroup view = (ViewGroup) LayoutInflater.from(GameListActivity.this).inflate(R.layout.listitem_game, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
                Game game = mGames[i];
                viewHolder.txt.setText(String.format("%d player%s", game.playerIds.length, game.playerIds.length == 1 ? "" : "s"));
                viewHolder.date.setText(DateTimeUtils.getTimeAgo(GameListActivity.this, game.startTime));
            }

            @Override
            public int getItemCount() {
                return mGames.length;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRequests.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        txtEmpty.setVisibility(View.INVISIBLE);

        loadData();
    }

    private void loadData() {
        mRefresh.setRefreshing(true);
        txtEmpty.setVisibility(View.GONE);
        mGames = new Game[0];
        mList.getAdapter().notifyDataSetChanged();
        Request request = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
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

        mRequests.add(request);
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

    public class Game {
        public int version;
        public UUID uid;
        public String gameVersion;
        public int numPlayersDesired;
        public UUID[] playerIds;
        public Date startTime;
        public String status;
        public int maximumTeamSize;
        public Date dateModified;
    }
}
