package iannewson.com.archangel.database;

import android.os.Debug;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class PlayerRepository {

    public Player getPlayerById(String id) {
        return Realm.getDefaultInstance()
                .where(Player.class)
                .equalTo("id", id)
                .findFirst();
    }

    public Player getPlayerByUuid(UUID id) {
        Player player = Realm.getDefaultInstance()
                .where(Player.class)
                .equalTo("uniqueIds.id", id.toString())
                .findFirst();

        if (Debug.isDebuggerConnected()) {
            //This is test code to see if the above code is failing.
            if (null == player) {
                PlayerId playerId = Realm.getDefaultInstance()
                        .where(PlayerId.class)
                        .equalTo("id", id.toString())
                        .findFirst();
                if (null != playerId) {
                    throw new IllegalArgumentException("Hmm, player id found but the player wasn't...");
                }
            }
        }

        return player;
    }

    public void sync(List<iannewson.com.archangel.models.dtos.Player> playerDtos) {
        Trace trace = FirebasePerformance.getInstance().newTrace("player_sync");
        trace.start();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Player> players = realm
                .where(Player.class)
                .findAll();


        realm.beginTransaction();

        for (iannewson.com.archangel.models.dtos.Player playerDto : playerDtos) {
            Player playerDb = getPlayerById(playerDto._id);
            boolean doCommit = false;
            if (null == playerDb) {
                playerDb = new Player(playerDto);
                doCommit = true;
            } else {
                if (!playerDb.equals(playerDto)) {
                    playerDb.copyFrom(playerDto);
                    doCommit = true;
                }
            }
            if (doCommit) {
                update(playerDb);
            }
        }

        realm.commitTransaction();
        trace.stop();
    }

    public void insert(Player player) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insert(player);
        realm.commitTransaction();
    }

    private void update(Player player) {
        Realm realm = Realm.getDefaultInstance();
        realm.copyToRealmOrUpdate(player);
    }

    public List<Player> getAll() {
        return Realm.getDefaultInstance()
                .where(Player.class)
                .sort("score", Sort.DESCENDING)
                .findAll();
    }
}
