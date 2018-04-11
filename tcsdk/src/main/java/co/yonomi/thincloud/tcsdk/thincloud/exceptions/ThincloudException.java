package co.yonomi.thincloud.tcsdk.thincloud.exceptions;

/**
 * Created by mike on 4/4/18.
 */

public class ThincloudException extends Exception {

    public ThincloudException(){super();}

    public ThincloudException(String message){
        super(message);
    }

    public ThincloudException(String message, Throwable exception){
        super(message, exception);
    }
}
