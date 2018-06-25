package co.yonomi.thincloud.tcsdk.thincloud.exceptions;

public class ThincloudUnauthorizedException extends ThincloudException {
    public ThincloudUnauthorizedException(String message){
        super(message);
    }

    public ThincloudUnauthorizedException(String message, Throwable e){
        super(message, e);
    }
}
