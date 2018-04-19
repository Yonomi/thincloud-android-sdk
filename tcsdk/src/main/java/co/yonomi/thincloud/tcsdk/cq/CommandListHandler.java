package co.yonomi.thincloud.tcsdk.cq;

import java.util.List;

import co.yonomi.thincloud.tcsdk.thincloud.models.Command;

/**
 * Created by mike on 4/17/18.
 */

public abstract class CommandListHandler implements GenericCommandHandler<List<Command>> {

    /**
     * Process a list of commands
     * @param command
     */
    @Override
    public abstract void onEventReceived(List<Command> command);

    /**
     * This method is to be called when command processing is complete
     * @param commands
     */
    public void onEventProcessed(List<Command> commands){
        for (Command command : commands) {
            CommandQueue.getInstance().handledCommand(command);
        }
    }
}
