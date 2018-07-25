package iannewson.com.archangel.database;

import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class PlayerRepository {

    public Player getPlayerById(UUID id) {
        return Realm.getDefaultInstance()
                .where(Player.class)
                .equalTo("id", id.toString())
                .findFirst();
    }

    public void sync(List<iannewson.com.archangel.models.dtos.Player> playerDtos) {
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

}
