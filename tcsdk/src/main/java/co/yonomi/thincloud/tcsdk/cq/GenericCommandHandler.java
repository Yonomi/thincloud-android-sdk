package co.yonomi.thincloud.tcsdk.cq;

/**
 * Created by mike on 4/17/18.
 */

public interface GenericCommandHandler<T> {

    void onEventReceived(T command);
}
