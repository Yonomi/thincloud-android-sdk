package co.yonomi.thincloud.tcsdk.fcm;

import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.UUID;

import co.yonomi.thincloud.tcsdk.ThincloudSDK;
import co.yonomi.thincloud.tcsdk.cq.CommandQueue;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;


/**
 * Created by mike on 4/4/18.
 */

public class TCFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TCFirebaseMessagingSVC";
    private static final Gson gson = new Gson();
    private static FirebaseJobDispatcher jobDispatcher;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        Log.i(TAG, "Got remote message " + remoteMessage.getMessageId());
        if(CommandQueue.getInstance().getUseJobScheduler())
            scheduleJob(remoteMessage);
        else
            try{
                CommandQueue.getInstance().handleCommand(remoteMessage.getData(), null);
            } catch(ThincloudException e){
                Log.e(TAG, "Failed to instant-handle command", e);
            }
    }

    private void scheduleJob(RemoteMessage remoteMessage){
        if(ThincloudSDK.isInitialized()) {
            if(jobDispatcher == null)
                jobDispatcher = new FirebaseJobDispatcher(ThincloudSDK.getGooglePlayDriver());
            Bundle bundle = new Bundle();
            bundle.putSerializable("msgPayload", gson.toJson(remoteMessage.getData()));
            Job job = jobDispatcher.newJobBuilder()
                    .setService(TCCommandJobService.class)
                    .setTag("TCJob_" + UUID.randomUUID())
                    .setRecurring(false)
                    .setReplaceCurrent(false)
                    .setExtras(bundle)
                    .setTrigger(Trigger.executionWindow(0, 0))
                    .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                    .build();

            try {
                Log.i(TAG, "Scheduling job " + job.getTag());
                jobDispatcher.mustSchedule(job);
            } catch (FirebaseJobDispatcher.ScheduleFailedException e) {
                Log.e(TAG, "Failed to schedule job", e);
            }
        } else {
            Log.e(TAG, "Failed to schedule job, ThincloudSDK not initialized");
        }
    }
}
