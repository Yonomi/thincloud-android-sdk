package co.yonomi.thincloud.tcsdk.cq;

import co.yonomi.thincloud.tcsdk.thincloud.models.Command;

/**
 * Created by mike on 4/4/18.
 */

public abstract class CommandHandler implements GenericCommandHandler<Command> {
    /**
     * Handle a Command
     * @param command
     */
    public abstract void onEventReceived(Command command);

    /**
     * Report that command handling is complete
     * @param command
     */
    public void onEventProcessed(Command command){
        CommandQueue.getInstance().handledCommand(command);
    }
}
