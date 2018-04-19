package co.yonomi.thincloud.tcsdk.thincloud;

import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by mike on 4/18/18.
 */

public class ThincloudRequest<T> {


    public ThincloudRequest(){}

    public void create(final Call<T> originalCall, final ThincloudResponse<T> tcResponse){
        try {
            ThincloudAPI.getInstance().getAuthenticatedScope(new TCAPIFuture() {
                @Override
                public boolean complete(APISpec spec) {
                    createWithoutAuth(originalCall, tcResponse);
                    return true;
                }

                @Override
                public boolean completeExceptionally(Throwable e) {
                    tcResponse.handle(originalCall, null, e);
                    return true;
                }
            });
        }
        catch(ThincloudException e){
            tcResponse.handle(originalCall, null, e);
        }
    }

    public void createWithoutAuth(final Call<T> originalCall, final ThincloudResponse<T> tcResponse){
        originalCall.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                tcResponse.handle(call, response, null);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                tcResponse.handle(call, null, t);
            }
        });
    }
}
