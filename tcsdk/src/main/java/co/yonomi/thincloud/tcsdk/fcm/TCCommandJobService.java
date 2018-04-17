package co.yonomi.thincloud.tcsdk.fcm;


import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.cq.CommandQueue;

/**
 * Created by mike on 4/4/18.
 */

public class TCCommandJobService extends JobService {
    private static final String TAG = "TCCommandJobService";

    @Override
    public boolean onStartJob(JobParameters jobParameters){
        Log.i(TAG, "Got remote message");
        try {
            CommandQueue.getInstance().handleCommand(this, jobParameters);
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
