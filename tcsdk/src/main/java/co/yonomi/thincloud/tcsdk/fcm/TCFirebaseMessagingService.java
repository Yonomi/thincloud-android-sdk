package co.yonomi.thincloud.tcsdk.fcm;

import android.os.Bundle;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by mike on 4/4/18.
 */

public class TCFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TCFirebaseMessagingSVC";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        scheduleJob(remoteMessage);
    }

    private void scheduleJob(RemoteMessage remoteMessage){
        Bundle bundle = new Bundle();
        bundle.putParcelable("remoteMessage", remoteMessage);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job job = dispatcher.newJobBuilder()
                .setService(TCCommandJobService.class)
                .setTag("tc-job-tag")
                .setExtras(bundle)
                .build();
        dispatcher.schedule(job);
    }

    private void handleNow(){

    }
}
