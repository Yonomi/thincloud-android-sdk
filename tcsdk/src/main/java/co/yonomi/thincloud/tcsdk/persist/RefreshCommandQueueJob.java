package co.yonomi.thincloud.tcsdk.persist;


import android.app.job.JobParameters;
import android.app.job.JobService;

/**
 * Created by mike on 4/6/18.
 */

public class RefreshCommandQueueJob extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters){
        // TODO: Implement auto-refresh
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters){
        // TODO: Implement auto-refresh
        return false;
    }
}
