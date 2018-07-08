package iannewson.com.archangel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

import iannewson.com.archangel.database.GameNotificationRepository;

public class MyHandler extends NotificationsHandler {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Context ctx;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        checkGamesAndNotify(bundle);
    }

    private void checkGamesAndNotify(Bundle bundle) {
        String nhMessage = bundle.getString("message");
        GameNotificationExtras extras = new Gson().fromJson(bundle.getString("extras"), GameNotificationExtras.class);

        GameNotificationRepository notificationsRepository = new GameNotificationRepository();

        boolean shouldNotify = false;
        for (String uid : extras.gameIds) {
            if (!notificationsRepository.hasNotified(uid)) {
                shouldNotify = true;
            }
        }

        if (shouldNotify) {
            sendNotification(nhMessage);

            for (String uid : extras.gameIds) {
                notificationsRepository.setNotified(uid);
            }
        }
    }

    private void sendNotification(String msg) {

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "gamealerts";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Archangel game alert")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setSound(defaultSoundUri)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(channelId);
            NotificationChannel mChannel = new NotificationChannel(channelId, "Game alert", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}