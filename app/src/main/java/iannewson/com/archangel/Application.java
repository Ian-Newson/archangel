package iannewson.com.archangel;

import com.google.firebase.FirebaseApp;

import io.realm.Realm;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        Realm.init(this);
    }
}
