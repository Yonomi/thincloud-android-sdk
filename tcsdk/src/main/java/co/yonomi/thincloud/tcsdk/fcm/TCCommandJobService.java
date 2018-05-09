package co.yonomi.thincloud.tcsdk.fcm;


import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;

import java.util.Map;

import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.cq.CommandQueue;
import co.yonomi.thincloud.tcsdk.util.AndThenDo;

/**
 * Created by mike on 4/4/18.
 */

public class TCCommandJobService extends JobService {
    private static final String TAG = "TCCommandJobService";

    private static final Gson gson = new Gson();

    @Override
    public boolean onStartJob(final JobParameters jobParameters){
        Log.i(TAG, "Got remote message");
        try {
            final Map<String,String> messageData;
            try{
                messageData = gson.fromJson((String)jobParameters.getExtras().getSerializable("msgPayload"), Map.class);
            } catch(NullPointerException e){
                Log.e(TAG, "Failed to parse payload", e);
                throw new ThincloudException("Failed to parse payload");
            }
            CommandQueue.getInstance().handleCommand(messageData, new AndThenDo() {
                @Override
                public void something() {
                    jobFinished(jobParameters, false);
                }
            });
            Log.i(TAG, "Handled remote message");
        }
        catch(ThincloudException e){
            Log.e(TAG, "Failed to handle remote message.", e);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters){
        return false;
    }
}
