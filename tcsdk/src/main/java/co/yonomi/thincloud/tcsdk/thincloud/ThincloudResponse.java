package co.yonomi.thincloud.tcsdk.thincloud;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by mike on 4/18/18.
 */

public abstract class ThincloudResponse<T> {
    public abstract void handle(Call<T> call, Response<T> response, Throwable error);
}
