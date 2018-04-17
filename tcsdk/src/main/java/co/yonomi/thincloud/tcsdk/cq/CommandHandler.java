package co.yonomi.thincloud.tcsdk.cq;

import android.os.Parcelable;

import com.google.firebase.messaging.RemoteMessage;

import java.lang.reflect.ParameterizedType;

import co.yonomi.thincloud.tcsdk.thincloud.models.Command;

/**
 * Created by mike on 4/4/18.
 */

public abstract class CommandHandler {
    /**
     * Handle a Command
     * @param command
     */
    public abstract void onEventReceived(Command command);

    public void onEventProcessed(Command command){
        CommandQueue.getInstance().handledCommand(command);
    }
}
