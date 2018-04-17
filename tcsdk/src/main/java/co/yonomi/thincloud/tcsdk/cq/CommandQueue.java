package co.yonomi.thincloud.tcsdk.cq;

import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import co.yonomi.thincloud.tcsdk.thincloud.APISpec;
import co.yonomi.thincloud.tcsdk.thincloud.TCAPIFuture;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudAPI;
import co.yonomi.thincloud.tcsdk.thincloud.models.Command;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by mike on 4/4/18.
 */

public class CommandQueue {

    private static final String TAG = "CommandQueue";

    private static CommandQueue _instance = new CommandQueue();

    private static final Gson gson = new Gson();

    public static CommandQueue getInstance(){
        return _instance;
    }


    private CommandHandler handler;

    private CommandQueue(){

    }

    public void setHandler(CommandHandler _handler){
        handler = _handler;
    }

    public void handleCommand(final JobService jobService, final JobParameters jobParameters) throws ThincloudException {
        if(handler == null) {
            throw new ThincloudException("Handler not defined");
        }
        else{
            final Map<String,String> messageData;
            try{
                messageData = gson.fromJson((String)jobParameters.getExtras().getSerializable("msgPayload"), Map.class);
            } catch(NullPointerException e){
                Log.e(TAG, "Failed to parse payload", e);
                throw new ThincloudException("Failed to parse payload");
            }

            final String deviceId = messageData.get("deviceId");
            if(deviceId == null)
                throw new ThincloudException("Cannot resolve commands for a null deviceId");

            ThincloudAPI.getInstance().spec(new TCAPIFuture() {
                @Override
                public boolean complete(APISpec spec) {
                    spec.getCommands(deviceId).enqueue(new Callback<List<Command>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Command>> call, @NonNull Response<List<Command>> response) {
                            List<Command> commands = response.body();
                            if(commands != null) {
                                for (Command command : commands) {
                                    processCommand(command);
                                }
                            }
                            jobService.jobFinished(jobParameters, false);
                        }

                        @Override
                        public void onFailure(Call<List<Command>> call, Throwable t) {
                            Log.e(TAG, "Failed to fetch commands", t);
                            jobService.jobFinished(jobParameters, false);
                        }
                    });
                    return true;
                }

                @Override
                public boolean completeExceptionally(Throwable e){
                    Log.e(TAG, "Failed to get spec for getCommands", e);
                    return true;
                }
            });
        }
    }

    public void processCommand(final Command command){
        handler.onEventReceived(command);
        try {
            ThincloudAPI.getInstance().spec(new TCAPIFuture() {
                @Override
                public boolean complete(APISpec spec) {
                    spec.updateCommand(
                            command.deviceId(),
                            command.commandId(),
                            new Command()
                                    .state("completed")
                    ).enqueue(new Callback<Command>() {
                        @Override
                        public void onResponse(Call<Command> call, Response<Command> response) {
                            Log.i(TAG, "Command update success");
                        }

                        @Override
                        public void onFailure(Call<Command> call, Throwable t) {
                            Log.e(TAG, "Command update failed", t);
                        }
                    });
                    return true;
                }

                @Override
                public boolean completeExceptionally(Throwable e){
                    Log.e(TAG, "Failed to get spec for updateCommand", e);
                    return true;
                }
            });
        }
        catch(ThincloudException e){
            Log.e(TAG, "Failed to update command in queue", e);
        }
    }

    public void handledCommand(Command command){

    }
}
