package test.launcher.mummu.chatexamplegooglerecomended.tools;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import test.launcher.mummu.chatexamplegooglerecomended.R;

/**
 * Created by muhammed on 8/5/2016.
 */
public class NotificationBuilderIM {


    private static NotificationBuilderIM notificationBuilderIM;
    private Context context;

    private NotificationBuilderIM(Context context) {
        this.context = context;

    }

    public static NotificationBuilderIM getInstance(Context context) {
        if (notificationBuilderIM == null) {
            notificationBuilderIM = new NotificationBuilderIM(context);
        }
        return notificationBuilderIM;
    }

    public Context getContext() {
        return context;
    }

    public void CreateNotification(String contentTitle, String contentText, Intent intent, int notificationId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext());
        mBuilder.setSmallIcon(R.drawable.stat_sample);
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(contentText);
        mBuilder.setOngoing(false);
        mBuilder.setAutoCancel(true);
        // Adds the Intent that starts the Activity to the top of the stack

        PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(), notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}
