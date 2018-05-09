package co.yonomi.thincloud.tcsdk.cq;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import co.yonomi.thincloud.tcsdk.thincloud.APISpec;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudRequest;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudResponse;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudAPI;
import co.yonomi.thincloud.tcsdk.thincloud.models.Command;
import co.yonomi.thincloud.tcsdk.util.AndThenDo;
import retrofit2.Call;
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

    private GenericCommandHandler handler;

    private boolean useJobScheduler = false;

    private CommandQueue(){ }

    /**
     * Set event handler
     * @param _handler
     */
    public void setHandler(GenericCommandHandler _handler){
        handler = _handler;
    }

    public void setUseJobScheduler(boolean bool){
        useJobScheduler = bool;
    }

    public boolean getUseJobScheduler(){
        return useJobScheduler;
    }

    /**
     * Filter out commands who's {@link Command#state} does not equal state parameter
     * @param commands
     * @param state
     * @return
     */
    public List<Command> filterCommandsByState(List<Command> commands, String state){
        Iterator<Command> iterator = commands.iterator();
        state = state.toUpperCase();
        while(iterator.hasNext()){
            Command command = iterator.next();
//            if(!command.state().toUpperCase().equals(state))
//                commands.remove(command);
        }
        return commands;
    }

    /**
     * Handle a command from firebase JobService
     * @param messageData
     * @param andThenDo
     * @throws ThincloudException
     */
    public void handleCommand(final Map<String,String> messageData, final AndThenDo andThenDo) throws ThincloudException {
        if(handler == null) {
            throw new ThincloudException("Handler not defined");
        }
        else{
            final String deviceId = messageData.get("deviceId");
            if(deviceId == null)
                throw new ThincloudException("Cannot resolve commands for a null deviceId");

            APISpec apiSpec = ThincloudAPI.getInstance().getSpec();
            if(apiSpec != null){
                ThincloudResponse<List<Command>> responseHandler = new ThincloudResponse<List<Command>>() {
                    @Override
                    public void handle(Call<List<Command>> call, Response<List<Command>> response, Throwable error) {
                        if(error != null){
                            Log.e(TAG, "Failed to fetch commands", error);
                            if(andThenDo != null)
                                andThenDo.something();
                        } else {
                            List<Command> rawCommands = response.body();
                            if(rawCommands != null) {
                                Log.i(TAG, "Got commands, dispatching");
                                List<Command> commands = filterCommandsByState(rawCommands, "pending");
                                if(handler instanceof CommandListHandler) {
                                    acknowledgeCommands(commands, new AndThenDo() {
                                        @Override
                                        public void something() {
                                            ((CommandListHandler) handler).onEventReceived(commands);
                                        }
                                    });
                                }
                                else if(handler instanceof CommandHandler) {
                                    for (Command command : commands) {
                                        acknowledgeCommand(command, new AndThenDo() {
                                            @Override
                                            public void something() {
                                                ((CommandHandler)handler).onEventReceived(command);
                                            }
                                        });
                                    }
                                }
                                else
                                    Log.e(TAG, "Failed to handle command, unexpected handler implementation.");
                            } else {
                                Log.e(TAG, "Null commands, something went wrong");
                            }
                            if(andThenDo != null)
                                andThenDo.something();
                        }
                    }
                };
                new ThincloudRequest<List<Command>>().create(apiSpec.getCommands(deviceId), responseHandler);
            } else {
                Log.e(TAG, "Failed to fetch commands, API not initialized.");
            }
        }
    }

    /**
     * Update the state of the commands to ack
     * @param commands
     * @param andThenDo
     */
    private void acknowledgeCommands(final List<Command> commands, final AndThenDo andThenDo){
        for (Command command : commands) {
            acknowledgeCommand(command, null);
        }
        if(andThenDo != null)
            andThenDo.something();
    }

    /**
     * Update the state of the command to 'ACK'
     * @param command
     */
    private void acknowledgeCommand(final Command command, final AndThenDo andThenDo){
        APISpec apiSpec = ThincloudAPI.getInstance().getSpec();
        if(apiSpec != null){
            ThincloudResponse<Command> handler = new ThincloudResponse<Command>() {
                @Override
                public void handle(Call<Command> call, Response<Command> response, Throwable error) {
                    if(error != null)
                        Log.e(TAG, "Failed to handle command update", error);
                    else {
                        Log.i(TAG, "Command updated state=ack successfully");
                    }
                    if(andThenDo != null)
                        andThenDo.something();
                }
            };
            Call<Command> call = apiSpec.updateCommand(command.deviceId(), command.commandId(), new Command().state("ack"));
            new ThincloudRequest<Command>().create(call, handler);
        } else {
            Log.e(TAG, "Failed to acknowledge command, API not initialized.");
        }
    }

    /**
     * Submit an update to the command object
     * @param command
     */
    public void handledCommand(Command command){
        APISpec apiSpec = ThincloudAPI.getInstance().getSpec();
        if(apiSpec != null){
            ThincloudResponse<Command> handler = new ThincloudResponse<Command>() {
                @Override
                public void handle(Call<Command> call, Response<Command> response, Throwable error) {
                    if(error != null){
                        Log.e(TAG, "Failed to handle command", error);
                    } else {
                        if(response.code() >= 400){
                            Log.e(TAG, "Failed to handle command, bad response code");
                        } else {
                            Log.i(TAG, "Command updated successfully: " + command.commandId());
                        }
                    }
                }
            };
            Call<Command> call = apiSpec.updateCommand(command.deviceId(), command.commandId(), command);
            new ThincloudRequest<Command>().create(call, handler);
        } else {
            Log.e(TAG, "Failed to handle command, API not initialized.");
        }
    }
}
