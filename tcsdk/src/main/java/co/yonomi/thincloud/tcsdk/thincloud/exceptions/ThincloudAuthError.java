package co.yonomi.thincloud.tcsdk.thincloud.exceptions;

/**
 * Created by mike on 4/10/18.
 */

public class ThincloudAuthError extends ThincloudException {

    public ThincloudAuthError(){
        super();
    }

    public ThincloudAuthError(String message){
        super(message);
    }

    public ThincloudAuthError(String message, Throwable e){
        super(message, e);
    }
}
