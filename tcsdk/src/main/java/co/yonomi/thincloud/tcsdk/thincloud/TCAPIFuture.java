package co.yonomi.thincloud.tcsdk.thincloud;

import java9.util.concurrent.CompletableFuture;

/**
 * Created by mike on 4/11/18.
 */

public abstract class TCAPIFuture extends CompletableFuture<APISpec> {
    @Override
    public abstract boolean complete(APISpec spec);

    @Override
    public abstract boolean completeExceptionally(Throwable e);
}
