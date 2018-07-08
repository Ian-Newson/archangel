package iannewson.com.archangel.database;

import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class GameNotificationRepository {

    public void setNotified(String uid) {
        GameNotification notification = new GameNotification();
        notification.setGameId(uid);
        notification.setNotifiedAt(new Date());

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(notification);
        realm.commitTransaction();
    }

    public boolean hasNotified(String uid) {
        return getNotificationById(uid).size() > 0;
    }

    public List<GameNotification> getNotificationById(String uid) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GameNotification.class)
                .equalTo("gameId", uid)
                .findAll();
    }

}
