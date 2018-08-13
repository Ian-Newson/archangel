package iannewson.com.archangel.database;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PlayerId extends RealmObject {

    @PrimaryKey
    public String id;

    public PlayerId() {
    }

    public PlayerId(String id) {

        this.id = id;
    }

    public String getId() {
        return id;
    }
}
