package co.yonomi.thincloud.tcsdk.thincloud.exceptions;

public class ThincloudAccountNotVerifiedException extends ThincloudException {
    public ThincloudAccountNotVerifiedException(String message){
        super(message);
    }

    public ThincloudAccountNotVerifiedException(String message, Throwable e){
        super(message, e);
    }
}