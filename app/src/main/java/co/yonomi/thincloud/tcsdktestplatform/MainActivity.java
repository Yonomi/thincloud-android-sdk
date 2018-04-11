package co.yonomi.thincloud.tcsdktestplatform;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import co.yonomi.thincloud.tcsdk.ThincloudConfig;
import co.yonomi.thincloud.tcsdk.ThincloudSDK;
import co.yonomi.thincloud.tcsdk.thincloud.APISpec;
import co.yonomi.thincloud.tcsdk.thincloud.TCAPIFuture;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudAPI;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Configurator appConfig = Configurator.initWithContext(this);

        final Spinner envSelector = findViewById(R.id.environment);
        final EditText textUsername = findViewById(R.id.username);
        final EditText textPassword = findViewById(R.id.password);
        final Button buttonLogin = findViewById(R.id.button_login);


        textUsername.setText(appConfig.get("thincloud.defaultTestUser", ""));
        textPassword.setText(appConfig.get("thincloud.defaultTestPass", ""));

        // Configure dropdown
        ArrayAdapter<CharSequence> envArrayAdapter = ArrayAdapter.createFromResource(this, R.array.env_array, android.R.layout.simple_spinner_item);
        envArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        envSelector.setAdapter(envArrayAdapter);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
                if(getCurrentFocus() != null)
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                ThincloudConfig config = new ThincloudConfig()
                        .appName(getAppName())
                        .appVersion(getAppVersion())
                        .apiKey(appConfig.get("thincloud.apiKey"))
                        .clientId(appConfig.get("thincloud.clientId"))
                        .company(appConfig.get("thincloud.company"))
                        .fcmTopic(appConfig.get("thincloud.fcmTopic"))
                        .environment(envSelector.getSelectedItem().toString())
                        .username(textUsername.getText().toString())
                        .password(textPassword.getText().toString());

                if(!config.validate()){
                    Toast.makeText(MainActivity.this, "Configuration is invalid", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if(!ThincloudSDK.isInitialized()) {
                            ThincloudSDK.initialize(getApplicationContext(), config);
                            Log.i(TAG, "SDK initialized");
                        } else {
                            Log.i(TAG, "SDK already initialized");
                        }
                        ThincloudAPI.getInstance().spec(new TCAPIFuture() {
                            @Override
                            public boolean complete(APISpec spec) {
                                Log.i(TAG, "Spec resolved");
                                spec.getSelf().enqueue(new Callback<User>() {
                                    @Override
                                    public void onResponse(Call<User> call, Response<User> response) {
                                        Log.i(TAG, "Got user " + response.body().userId());
                                    }

                                    @Override
                                    public void onFailure(Call<User> call, Throwable t) {
                                        Log.e(TAG, "Failed to get user", t);
                                    }
                                });
                                return true;
                            }

                            @Override
                            public boolean completeExceptionally(Throwable e){
                                Log.e(TAG, "Failed to get spec for get user", e);
                                return true;
                            }
                        });
                    } catch(ThincloudException e){
                        Log.e(TAG, "Failed to initialize SDK", e);
                        Toast.makeText(MainActivity.this, "SDK Init Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private String getAppName(){
        ApplicationInfo appInfo = getApplicationContext().getApplicationInfo();
        return getApplicationContext().getString(appInfo.labelRes);
    }

    private String getAppVersion(){
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch(PackageManager.NameNotFoundException e){
            Log.e(TAG, "Failed to get app version", e);
            return "VersionNotFound";
        }
    }
}
