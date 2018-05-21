package co.yonomi.thincloud.tcsdk.thincloud;

import java9.util.concurrent.CompletableFuture;

public abstract class TCFuture<T> extends CompletableFuture<T> {

    public abstract boolean complete(T result);

    public abstract boolean completeExceptionally(Throwable e);

}
