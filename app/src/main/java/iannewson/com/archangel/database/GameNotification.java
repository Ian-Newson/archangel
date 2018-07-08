package iannewson.com.archangel.database;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;

public class GameNotification extends RealmObject {

    private String gameId;
    private Date notifiedAt;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Date getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(Date notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}
