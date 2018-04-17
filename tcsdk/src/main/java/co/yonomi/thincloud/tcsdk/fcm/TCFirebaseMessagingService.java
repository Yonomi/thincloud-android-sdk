package co.yonomi.thincloud.tcsdk.fcm;

import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import static com.firebase.jobdispatcher.FirebaseJobDispatcher.SCHEDULE_RESULT_BAD_SERVICE;
import static com.firebase.jobdispatcher.FirebaseJobDispatcher.SCHEDULE_RESULT_NO_DRIVER_AVAILABLE;
import static com.firebase.jobdispatcher.FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS;
import static com.firebase.jobdispatcher.FirebaseJobDispatcher.SCHEDULE_RESULT_UNKNOWN_ERROR;
import static com.firebase.jobdispatcher.FirebaseJobDispatcher.SCHEDULE_RESULT_UNSUPPORTED_TRIGGER;

/**
 * Created by mike on 4/4/18.
 */

public class TCFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TCFirebaseMessagingSVC";
    private static final Gson gson = new Gson();


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        Log.i(TAG, "Got remote message " + remoteMessage.getMessageId());
        scheduleJob(remoteMessage);
    }

    private void scheduleJob(RemoteMessage remoteMessage){
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Bundle bundle = new Bundle();
//        bundle.putParcelable("remoteMessage", remoteMessage);
        bundle.putSerializable("msgPayload", gson.toJson(remoteMessage.getData()));
        Job job = jobDispatcher.newJobBuilder()
                .setService(TCCommandJobService.class)
                .setTag("TCJob_" + remoteMessage.getMessageId())
                .setExtras(bundle)
                .build();
        try{
            jobDispatcher.mustSchedule(job);
        } catch(FirebaseJobDispatcher.ScheduleFailedException e){
            Log.e(TAG, "Failed to schedule job", e);
        }
    }
}
