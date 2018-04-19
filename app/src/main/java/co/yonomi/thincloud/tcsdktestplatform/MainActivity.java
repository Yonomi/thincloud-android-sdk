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
import co.yonomi.thincloud.tcsdk.cq.CommandHandler;
import co.yonomi.thincloud.tcsdk.thincloud.APISpec;
import co.yonomi.thincloud.tcsdk.thincloud.TCAPIFuture;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudAPI;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudRequest;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudResponse;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.models.Command;
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

        // Configure environment spinner
        ArrayAdapter<CharSequence> envArrayAdapter = ArrayAdapter.createFromResource(this, R.array.env_array, android.R.layout.simple_spinner_item);
        envArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        envSelector.setAdapter(envArrayAdapter);

        final CommandHandler commandHandler = new CommandHandler() {
            @Override
            public void onEventReceived(Command command) {
                Log.i("ICommandHandler", "Received command: " + command.commandId());
                Toast.makeText(MainActivity.this, "Processing command: " + command.commandId(), Toast.LENGTH_SHORT).show();
                Command response = command.respond();
                response.state("completed");
                onEventProcessed(response);
            }
        };

        ThincloudSDK.setHandler(commandHandler);

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
                        .instanceName(appConfig.get("thincloud.instanceName"))
                        .fcmTopic(appConfig.get("thincloud.fcmTopic"))
                        .username(textUsername.getText().toString())
                        .password(textPassword.getText().toString());

                if(!config.validate()){
                    Toast.makeText(MainActivity.this, "Configuration is invalid", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if(!ThincloudSDK.isInitialized()) {
                            ThincloudSDK
                                    .initialize(getApplicationContext(), config);

                            Log.i(TAG, "SDK initialized");
                        } else {
                            Log.i(TAG, "SDK already initialized");
                        }
                        APISpec apiSpec = ThincloudAPI.getInstance().getSpec();
                        if(apiSpec != null){
                            ThincloudResponse<User> handler = new ThincloudResponse<User>() {
                                @Override
                                public void handle(Call<User> call, Response<User> response, Throwable error) {
                                    if(error != null){
                                        Log.e(TAG, "Failed to get user", error);
                                        Toast.makeText(MainActivity.this, "User failed to authenticate", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if(response.code() >= 400){
                                            Log.e(TAG, "Failed to get user");
                                        } else {
                                            Log.i(TAG, "Got user " + response.body().userId());
                                            Toast.makeText(MainActivity.this, "User authenticated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            };
                            new ThincloudRequest<User>().create(apiSpec.getSelf(), handler);
                        } else {
                            Log.e(TAG, "Failed to get user, API not initialized.");
                        }
                    } catch(ThincloudException e){
                        Log.e(TAG, "Failed to initialize SDK", e);
                        Toast.makeText(MainActivity.this, "SDK Init Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private String getAppName(){
//        ApplicationInfo appInfo = getApplicationContext().getApplicationInfo();
//        return getApplicationContext().getString(appInfo.labelRes);
        return getApplicationContext().getPackageName();
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
